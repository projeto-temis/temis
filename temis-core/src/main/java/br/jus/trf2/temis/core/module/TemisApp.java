package br.jus.trf2.temis.core.module;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.Entity;

import org.reflections.Reflections;

import com.crivano.juia.AnnotationViewBuilder;

import br.jus.trf2.temis.core.Objeto;
import br.jus.trf2.temis.core.Relatorio;
import br.jus.trf2.temis.core.util.MimeTypeEnum;
import br.jus.trf2.temis.core.util.SigaEntityManagerFactory;
import br.jus.trf2.temis.core.util.TemisCaptionBuilder;
import br.jus.trf2.temis.core.util.Utils;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeConfiguracaoEnum;

@ApplicationScoped
public class TemisApp {
//	private static TemisApp instance;
	private final Map<String, IResources> map = new HashMap<>();
	private final Map<String, Class<?>> mapClassByLocator = new HashMap<>();
	private final Pattern p = Pattern.compile(
			"^(?<module>[a-z]*)\\/(?:(?<path>[a-z0-9_\\-\\/]+)\\/)?(?<filename>[a-zA-Z0-9_\\-\\.]+?(?:\\.(?<extension>[a-z0-9]+))?)$");

	@Inject
	private SigaEntityManagerFactory jemf;

	@Inject
	private TemisResources resources;

	public TemisApp() {
		System.out.println("TEMIS APP CREATED");
		CrpTipoDeConfiguracaoEnum.mapear(CrpTipoDeConfiguracaoEnum.values());
	}

	public void onStart(@Observes @Initialized(ApplicationScoped.class) Object pointless) {
		AnnotationViewBuilder.setCaptionBuilder(new TemisCaptionBuilder());

		long startTime = System.currentTimeMillis();
		int c = 0;

		// Encontrar todas as classes anotadas com @Entity
		Reflections reflections = new Reflections("br.jus.trf2.temis");
		Set<Class<? extends Objeto>> subTypes = reflections.getSubTypesOf(Objeto.class);
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
		annotated.addAll(reflections.getSubTypesOf(Relatorio.class));

		// Registrar todas elas para fins de JPA e da geração do model.json que é
		// enviado
		// para a aplicação web
		for (Class<?> clazz : annotated) {
			if (clazz.isAnnotation() || clazz.getName().contains(".iam.") || clazz.getName().contains(".pjd."))
				continue;
			// System.out.println("*** registrando classe: " + clazz.getName());
			register(clazz);
			c++;
		}
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime - startTime;
		System.out.println("Têmis: " + c + " classes registradas em " + timeElapsed + "ms");
	}

	public void register(Class clazz) {
		this.jemf.addClass(clazz);
		resources.add(TemisResourceKindEnum.MODEL_JSON, clazz);
		String locator = Utils.localizadorDaClasse(clazz);
		if (locator != null)
			mapClassByLocator.put(locator, clazz);
	}

	public Class getClassByLocator(String localizador) {
		return mapClassByLocator.get(localizador);
	}

	public void addModule(String string, IResources resources) {
		map.put(string, resources);
	}

//
	public TemisDownload getResource(String pathName) throws IOException {
		Matcher m = p.matcher(pathName);
		if (!m.matches())
			throw new FileNotFoundException("file not found: " + pathName);

		String module = m.group("module");
		String path = m.group("path");
		String filename = m.group("filename");
		String extension = m.group("extension");
		MimeTypeEnum mimeType = MimeTypeEnum.getByExtension(extension);
		String contentType = mimeType.contentType;

		InputStream is = null;

		String root = System.getenv("XRP_DEVELOPMENT_ROOT");
//		if (root == null && Services.property.isDevelopment())
//			root = "c:/Repositories/xrp";
//		if (root != null) {
//			String pathname = root + "/xrp-" + module + "/src/main/resources/webapp/" + (path != null ? path + "/" : "")
//					+ filename;
//			File initialFile = new File(pathname);
//			try {
//				is = new FileInputStream(initialFile);
//			} catch (Exception e) {
//				return null;
//				// throw new RuntimeException("can't find file: " + pathname);
//			}
//		} else {
		IResources res = getModuleResources(module);
		String pathname = "/webapp/" + (path != null ? path + "/" : "") + filename;
		is = res.getResourceAsStream(pathname);
		if (is == null)
			return null;
		// throw new RuntimeException("can't find resource: " + pathname);
//		}

		byte[] bytes = Utils.convertStreamToByteArray(is, 32000);
		MessageDigest md;
		String etag = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(bytes);
			etag = new String(Base64.getEncoder().encode(md.digest()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		TemisDownload dl = new TemisDownload(bytes, contentType, filename, false, etag, true, null);
		return dl;

	}

	public String getResourceAsString(String pathName) throws IOException {
		TemisDownload xisd = getResource(pathName);
		if (xisd == null)
			return null;
		String s = new String(xisd.bytes, StandardCharsets.UTF_8);
		return s;
	}

	public IResources getModuleResources(String module) {
		IResources res = map.get(module);
		return res;
	}

	public void addResource(TemisResourceKindEnum kind, String pathname) {
		resources.add(kind, pathname);
	}

	public Set<String> getModules() {
		return map.keySet();
	}

	public void addResource(TemisResourceKindEnum kind, Class<? extends Enum<?>> clazz) {
		resources.add(kind, clazz);
	}

	public TemisDownload getResources(TemisResourceKindEnum kind) throws IOException {
		return resources.getResources(this, kind);
	}

}
