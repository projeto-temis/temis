package br.gov.jfrj.siga.cp.auth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.crivano.swaggerservlet.SwaggerAuthorizationException;

import br.jus.trf2.temis.core.util.Prop;

@ApplicationScoped
public class AutenticadorJwtCookie implements Autenticador {
	static final String PROVIDER_ISSUER = "sigaidp";

	@Override
	public String criarCookie(HttpServletRequest req, HttpServletResponse resp, String principal) throws Exception {
		final long iat = System.currentTimeMillis() / 1000L; // issued at claim
		final long exp = iat + Autenticador.TIME_TO_EXPIRE_IN_S;

		Algorithm algorithm = Algorithm.HMAC256(Prop.get("/siga.jwt.secret"));
		String token = JWT.create()
				.withIssuer(PROVIDER_ISSUER)
				.withClaim(Autenticador.TOKEN_TYPE_VALUE, Autenticador.TOKEN_TYPE_VALUE)
				.withClaim("iat", iat)
				.withClaim("nbf", iat)
				.withClaim("exp", exp)
				.withClaim("sub", principal)
//	        .withClaim("mod", options.getModulo())
//	        .withClaim("cfg", config)
				.sign(algorithm);

//		DecodedJWT decodedToken = validarToken(token);
//		Cp.getInstance().getBL().logAcesso(AbstractCpAcesso.CpTipoAcessoEnum.AUTENTICACAO,
//				(String) decodedToken.get("sub"), (Integer) decodedToken.get("iat"),
//				(Integer) decodedToken.get("exp"), HttpRequestUtils.getIpAudit(req));

		Cookie cookie = buildCookie(token);
		addCookie(req, resp, cookie);
		return token;
	}

	private String renovarToken(String token) throws Exception {
		final long iat = System.currentTimeMillis() / 1000L; // issued at claim
		final long exp = iat + Autenticador.TIME_TO_EXPIRE_IN_S;
		if (token == null)
			throw new RuntimeException("");
		DecodedJWT claims = validarToken(token);
		Algorithm algorithm = Algorithm.HMAC256(Prop.get("/siga.jwt.secret"));
		return JWT.create()
				.withIssuer(claims.getIssuer())
				.withClaim(Autenticador.TOKEN_TYPE_VALUE, claims.getClaim(Autenticador.TOKEN_TYPE_VALUE).asString())
				.withClaim("iat", iat)
				.withClaim("nbf", iat)
				.withClaim("exp", exp)
				.withClaim("sub", claims.getSubject())
//		        .withClaim("mod", options.getModulo())
//		        .withClaim("cfg", config)
				.sign(algorithm);
	}

	@Override
	public String obterPrincipal(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		String token = extrairAuthorization(req);
		DecodedJWT decodedToken = validarToken(token);
		String principal = decodedToken.getClaim("sub").asString();
		if (principal == null)
			throw new SwaggerAuthorizationException("Token inválido para autenticação");
		String typ = decodedToken.getClaim(Autenticador.TOKEN_TYPE_VALUE).asString();
		if (!Autenticador.TOKEN_TYPE_VALUE.equals(typ))
			throw new SwaggerAuthorizationException("Tipo de token inválido para autenticação");
		final long now = System.currentTimeMillis() / 1000L;
//		if (decodedToken.getClaim("exp").asInt() < now + Autenticador.TIME_TO_RENEW_IN_S) {
//			String tokenNew = renovarToken(token);
//			validarToken(token);
//			Cookie cookie = buildCookie(tokenNew);
//			addCookie(req, resp, cookie);
//		}
		return principal;
	}

	@Override
	public void removerCookie(HttpServletRequest req, HttpServletResponse resp) throws Exception {
		addCookie(req, resp, buildEraseCookie());
	}

	private String extrairModulo(HttpServletRequest request) throws IOException, ServletException, JSONException {
		String opcoes = request.getHeader("Jwt-Options");
		if (opcoes != null) {
			String modulo = new JSONObject(opcoes).optString("mod");
			if (modulo == null || modulo.length() == 0) {
				throw new ServletException(
						"O parâmetro mod deve ser informado no HEADER Jwt-Options do request. Ex: {\"mod\":\"siga-wf\"}");
			}
			return modulo;
		}
		return null;
	}

	private String extrairAuthorization(HttpServletRequest request) {
		String auth = request.getHeader("Authorization");
		if (auth != null) {
			return request.getHeader("Authorization").replaceAll(".* ", "").trim();
		}
		Cookie[] cookies = request.getCookies();
		String token = null;
		ArrayList<String> tokens = new ArrayList<String>();

		if (cookies != null) {
			// Percorre lista cookie e extrai tokens
			for (Cookie c : cookies) {
				if (getNameCookie().equals(c.getName())) {
					tokens.add(c.getValue());
				}
			}
			if (!tokens.isEmpty()) {
				// Se houver apenas 1, retorna para rotina principal validar
				if (tokens.size() == 1) {
					return tokens.get(0);
				} else {
					// Se houver mais de 1, tenta localizar algum token válido
					for (String t : tokens) {
						token = t;
						try {
							validarToken(token);
							return token; // Se houver algum Token Válido Retorna para Rotina Principal
						} catch (Exception e) {
							// Passa para Próximo Token.
						}
					}
					return token; // Se não há nenhum token válido na lista, retorna para rotina explorar o erro
				}
			}
		}
		return null; // Se não há Tokens
	}

	private DecodedJWT validarToken(String token) throws Exception {
		if (token == null) {
			throw new RuntimeException("");
		}
		String cookieDomain = Prop.get("/siga.jwt.cookie.domain");
		DecodedJWT decodedJWT;
		Algorithm algorithm = Algorithm.HMAC256(Prop.get("/siga.jwt.secret"));
		Verification builder = JWT.require(algorithm)
				.withIssuer(PROVIDER_ISSUER);
		if (cookieDomain != null)
			builder.withAudience(cookieDomain);
		JWTVerifier verifier = builder.build();
		return verifier.verify(token);
	}

	private Cookie buildCookie(String tokenNew) {
		Cookie cookie = new Cookie(getNameCookie(), tokenNew);
		cookie.setPath("/");

		if (getCookieDomain() != null) {
			cookie.setDomain(getCookieDomain());
		}

		cookie.setMaxAge(TIME_TO_EXPIRE_IN_S);

		return cookie;
	}

	private Cookie buildEraseCookie() {
		Cookie cookie = new Cookie(getNameCookie(), "");
		cookie.setPath("/");
		if (getCookieDomain() != null) {
			cookie.setDomain(getCookieDomain());
		}
		cookie.setMaxAge(0);
		return cookie;
	}

	/**
	 * Este cookie é utilizado na sessão do usuário. Gera o nome do cookie de acordo
	 * com o valor da variavel de name=ambiente que se encontra no standalone.xml do
	 * server. Este metodo é importante para resolver o problema de compatibilidade
	 * de sessao quando se tem mais de uma aplicação aberta no mesmo navegador.
	 * 
	 * @return String
	 */
	private String getNameCookie() {

		if (SIGA_JWT_AUTH_COOKIE_NAME.equals(Prop.get("/siga.jwt.cookie.name")))
			return SIGA_JWT_AUTH_COOKIE_NAME;
		else
			// Add prefixo + name cookie para desambiguacao
			return SIGA_JWT_AUTH_COOKIE_NAME + "-" + Prop.get("/siga.jwt.cookie.name");
	}

	private String getCookieDomain() {
		return Prop.get("/siga.jwt.cookie.domain");
	}

	private void addCookie(HttpServletRequest request, HttpServletResponse response, Cookie cookie) {
		response.setHeader("Set-Cookie",
				removeSpecial(cookie.getName()) + "=" + removeSpecial(cookie.getValue())
						+ "; Path=" + cookie.getPath()
						+ (cookie.getDomain() == null ? "" : "; Domain=" + removeSpecial(cookie.getDomain()))
						+ "; Max-Age=" + cookie.getMaxAge()
						+ "; Expires=" + new Date(new Date().getTime() + cookie.getMaxAge() * 1000)
						+ "; HttpOnly"
						+ (!Prop.get("/siga.base.url").contains("https") ? "" : "; Secure; SameSite=None"));
	}

	private String removeSpecial(String str) {
		// return str.replaceAll("[a-zA-Z0-9]+", ""); Ajustar REGEX para suportar .,
		// traço e Número
		return str;
	}

}
