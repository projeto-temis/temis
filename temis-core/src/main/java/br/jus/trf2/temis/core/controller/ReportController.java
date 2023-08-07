package br.jus.trf2.temis.core.controller;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.crivano.jlogic.Expression;
import com.crivano.juia.AnnotationViewBuilder;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.biz.IJuiaAction;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;

import br.com.caelum.iogi.parameters.Parameters;
import br.com.caelum.iogi.reflection.Target;
import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.http.iogi.InstantiatorWithErrors;
import br.com.caelum.vraptor.observer.download.ByteArrayDownload;
import br.com.caelum.vraptor.observer.download.Download;
import br.com.caelum.vraptor.validator.Message;
import br.com.caelum.vraptor.validator.SimpleMessage;
import br.com.caelum.vraptor.validator.ValidationException;
import br.com.caelum.vraptor.validator.Validator;
import br.com.caelum.vraptor.view.Results;
import br.jus.trf2.temis.core.ArquivoPdf;
import br.jus.trf2.temis.core.Relatorio;
import br.jus.trf2.temis.core.enm.FormatoDeRelatorioEnum;
import br.jus.trf2.temis.core.module.TemisApp;
import br.jus.trf2.temis.core.util.BadRequestException;
import br.jus.trf2.temis.core.util.Dao;
import br.jus.trf2.temis.core.util.ModeloUtils;
import br.jus.trf2.temis.core.util.Utils;
import lombok.NoArgsConstructor;

@Controller
@NoArgsConstructor
public class ReportController {
	Logger logger = Logger.getLogger(this.getClass().getCanonicalName());

	@Inject
	protected Result result;
	@Inject
	protected HttpServletRequest request;
	@Inject
	protected HttpServletResponse response;
	@Inject
	protected EntityManager em;
	@Inject
	protected Dao dao;
	@Inject
	protected TemisApp app;

	private InstantiatorWithErrors instantiator;

	private Validator validator;

	@Inject
	public void injectInstantiator(InstantiatorWithErrors instantiator, Validator validator) {
		this.instantiator = instantiator;
		this.validator = validator;
		validator.onErrorSendBadRequest();
	}

	private static class DtoAcaoQuery {
		String confirmation;
		String html;
		IJuiaAction act;
	}

	@Get
	@Path("app/relatorio/{localizador}/form")
	public void form(String localizador) throws Exception {

		Class myClass = app.getClassByLocator(localizador);
		IJuiaAction acao = (IJuiaAction) myClass.newInstance();
		DtoAcaoQuery daq = new DtoAcaoQuery();

		daq.act = acao;

		if (!AnnotationViewBuilder.isSkipEdit(acao.getClass())) {
			AbstractHtml html = acao.getHtml();
			if (html != null)
				daq.html = html.toHtmlString();
		}

		result.use(Results.json()).withoutRoot().from(daq).recursive().serialize();
		return;

	}

	@Get
	@Path("app/relatorio/{localizador}/exec")
	public Download exec(String localizador, FormatoDeRelatorioEnum formato) throws Exception {
//		Usuario usuario = assertUsuarioAutorizado(key);
		if (formato == null)
			formato = FormatoDeRelatorioEnum.HTML;

		Class myClass = app.getClassByLocator(localizador);

		// carrega os parametros do request como ParameterA anteri
		Parameters parameters = parseParameters(request);
		// cria o target para o Iogi
		Target<?> target = new Target<>(myClass, "act");
		// carega as mensagens de erro
		List<Message> errors = new ArrayList<>();
		// instancia
		Relatorio acao = (Relatorio) instantiator.instantiate(target, parameters, errors);

		for (Field fld : ModeloUtils.getFieldList(acao.getClass())) {
			if (fld.getAnnotation(Inject.class) != null && fld.getType().isAssignableFrom(EntityManager.class)) {
				fld.set(acao, em);
			}

			if (fld.getAnnotation(ManyToOne.class) != null) {
				Object o = fld.get(acao);
				Object odb = em.find(o.getClass(), dao.getIdentifier(o));
				fld.set(acao, odb);
			}
		}

		// se tiver erros, carrega no validador
		validator.addAll(errors);
		// Utils.copyAllNotNullFields(acao, newObject);
		// acao.beforeValidate(data);

		validator.validate(acao);

		// Verifica se a ação pode ser executada
		Expression e = acao.getActive(null, null);
		if (e != null && !e.eval()) {
			Global juiaGlobal = acao.getClass().getAnnotation(Global.class);
			String s = "Ação proibida";
			if (juiaGlobal != null && juiaGlobal.action() != null)
				s = "Não é permitido " + juiaGlobal.action().toLowerCase();
			validator.add(new SimpleMessage(s + ", motivo: " + Utils.formatExplanation(e.explain(false)), null));
		}

//		flushValidationError();

		try {
			acao.gerar();
			if (acao.getLinhas().size() == 0)
				throw new RuntimeException("Nenhum dado retornado pela consulta.");
			switch (formato) {
			case CSV:
				String csv = acao.gerarCsv();
				return new ByteArrayDownload(csv.getBytes(StandardCharsets.ISO_8859_1), "text/csv",
						localizador + ".csv");
			case JSON_ARRAY:
				String json = acao.gerarJsonArray();
				return new ByteArrayDownload(json.getBytes(StandardCharsets.UTF_8), "application/json",
						localizador + ".json");
			case PDF:
				byte[] pdf = acao.gerarPdf();
				return new ByteArrayDownload(pdf, ArquivoPdf.APPLICATION_PDF, localizador + ".pdf");
			case HTML:
			default:
				String html = acao.gerarHtml();
				return new ByteArrayDownload(html.getBytes(StandardCharsets.UTF_8), "text/html", localizador + ".html");
			}
		} catch (Exception ex) {
			this.response.setStatus(400);
			if (e instanceof BadRequestException)
				throw new ValidationException(null);
			else
				throw ex;
		}
	}

	private Parameters parseParameters(HttpServletRequest request) {
		Map<String, String[]> parameters = request.getParameterMap();
		List<br.com.caelum.iogi.parameters.Parameter> parameterList = new ArrayList<>(parameters.size() * 2);

		for (Map.Entry<String, String[]> param : parameters.entrySet()) {
			for (String value : param.getValue()) {
				parameterList.add(new br.com.caelum.iogi.parameters.Parameter(param.getKey(), value));
			}
		}

		return new Parameters(parameterList);
	}
}
