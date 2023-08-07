package br.jus.trf2.temis.core.util;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.caelum.vraptor.AroundCall;
import br.com.caelum.vraptor.Intercepts;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.interceptor.SimpleInterceptorStack;
import br.com.caelum.vraptor.view.Results;
import br.gov.jfrj.siga.cp.auth.AutenticadorJwtCookie;
import br.jus.trf2.temis.crp.model.CrpIdentidade;
import br.jus.trf2.temis.crp.model.CrpLotacao;
import br.jus.trf2.temis.crp.model.CrpPessoa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Intercepts
@RequestScoped
@NoArgsConstructor
public class ContextInterceptor {
	HttpServletRequest request;
	HttpServletResponse response;
	private Result result;
	Dao dao;
	AutenticadorJwtCookie auth;

	@Data
	@AllArgsConstructor
	public static class Context {
		HttpServletRequest request;
		HttpServletResponse response;
		Dao dao;
		CrpIdentidade identidade;
		CrpPessoa cadastrante;
		CrpLotacao lotaCadastrante;
		CrpPessoa titular;
		CrpLotacao lotaTitular;
	}

	private static ThreadLocal<Context> context = new ThreadLocal<>();

	@Inject
	public ContextInterceptor(HttpServletRequest request, HttpServletResponse response, Result result, Dao dao,
			AutenticadorJwtCookie auth) {
		this.request = request;
		this.response = response;
		this.result = result;
		this.dao = dao;
		this.auth = auth;
	}

	@AroundCall
	public void intercept(SimpleInterceptorStack stack) {
		CrpIdentidade identidade = null;
		CrpPessoa cadastrante = null;
		CrpLotacao lotaCadastrante = null;
		CrpPessoa titular = null;
		CrpLotacao lotaTitular = null;
		auth = new AutenticadorJwtCookie();
		String principal = null;
		try {
			principal = auth.obterPrincipal(request, response);
		} catch (Exception e) {
			response.setStatus(401);
			result.use(Results.json()).from("Unauthorized", "errormsg").serialize();
			return; // throw new RuntimeException(e);
		}
		if (principal != null) {
			identidade = dao.consultaIdentidadesCadastrante(principal, true);
			cadastrante = identidade.getPessoa();
			lotaCadastrante = cadastrante.getLotacao();
			titular = cadastrante;
			lotaTitular = lotaCadastrante;
		}
		context.set(
				new Context(request, response, dao, identidade, cadastrante, lotaCadastrante, titular, lotaTitular));
		stack.next();
		context.set(null);
	}

	public boolean accepts(ControllerMethod method) {
		return true; // Will intercept all requests
	}

	public static Context getContext() {
		return context.get();
	}

	public static Dao getDao() {
		return getContext().getDao();
	}

	@Inject
	public void setAuth(AutenticadorJwtCookie auth) {
		this.auth = auth;
	}
}