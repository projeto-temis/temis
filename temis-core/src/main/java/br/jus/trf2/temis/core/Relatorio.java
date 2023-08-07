package br.jus.trf2.temis.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;

import com.crivano.jbiz.IEnum;
import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.FieldProps;
import com.crivano.juia.annotations.FieldProps.AggregateInJsonArray;
import com.crivano.juia.annotations.FieldProps.Align;
import com.crivano.juia.annotations.FieldProps.Format;
import com.crivano.juia.annotations.FieldSet;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Show;
import com.crivano.juia.biz.IJuiaAction;
import com.crivano.swaggerservlet.DefaultDateAdapter;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.gson.JsonArray;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import br.jus.trf2.temis.core.util.ModeloUtils;
import br.jus.trf2.temis.core.util.TemisCaptionBuilder;
import lombok.Data;
import lombok.Getter;

public abstract class Relatorio extends Objeto implements IJuiaAction {
	public static enum TipoDeLinhaDeRelatorioEnum {
		DETALHE, TOTAL;
	}

	@Data
	public static class LinhaDeRelatorio {
		private TipoDeLinhaDeRelatorioEnum tipoDeLinha = TipoDeLinhaDeRelatorioEnum.DETALHE;

		public LinhaDeRelatorio total() {
			tipoDeLinha = TipoDeLinhaDeRelatorioEnum.TOTAL;
			return this;
		}
	}

	@Getter
	private List<LinhaDeRelatorio> linhas = new ArrayList<>();
	private List<Field> fields = null;

	abstract public void gerar();

	public void linha(LinhaDeRelatorio linha) {
		linhas.add(linha);
		if (fields == null) {
			fields = ModeloUtils.getFieldList(linhas.get(0).getClass());
			fields.remove(0);
		}
	}

	public String gerarCsv() {
		final StringBuilder out = new StringBuilder();
		try {
//			List<Field> fields = ModeloUtils.getFieldList(linhas.get(0).getClass());

			// Colunas
			List<String> header = new ArrayList<>();
			for (Field f : fields) {
				if (f.getName().equals("tipoDeLinha"))
					continue;
				header.add(getFieldName(f));
			}
			final CSVPrinter printer = CSVFormat.EXCEL.withDelimiter(';')
					.withHeader(header.toArray(new String[header.size()])).print(out);

			// Linhas
			for (LinhaDeRelatorio l : linhas) {
				if (l.getTipoDeLinha() == TipoDeLinhaDeRelatorioEnum.DETALHE) {
					for (Field f : fields) {
						if (f.getName().equals("tipoDeLinha"))
							continue;
						printer.print(f.get(l));
					}
					printer.println();
				}
			}
			return out.toString();
		} catch (IOException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public String gerarJsonArray() {
		final JsonArray r = new JsonArray();

		try {
			// Colunas
			JsonArray header = new JsonArray();
			for (Field f : fields) {
				if (f.getName().equals("tipoDeLinha"))
					continue;
				FieldProps props = f.getAnnotation(FieldProps.class);
				if (props != null && props.aggregateInJsonArray() != null
						&& props.aggregateInJsonArray() == AggregateInJsonArray.YYYY_MM_DD) {
					String suffix = getFieldName(f);
					if (suffix.startsWith("Data"))
						suffix = suffix.substring(4);
					else
						suffix = " de " + suffix;
					header.add("Ano" + suffix);
					header.add("Mês" + suffix);
					header.add("Dia" + suffix);
				} else
					header.add(getFieldName(f));
			}
			r.add(header);
			// Linhas
			for (LinhaDeRelatorio l : linhas) {
				if (l.getTipoDeLinha() == TipoDeLinhaDeRelatorioEnum.DETALHE) {
					JsonArray line = new JsonArray();
					for (Field f : fields) {
						if (f.getName().equals("tipoDeLinha"))
							continue;
						Object value = f.get(l);

						// Agregar datas em 3 campos
						FieldProps props = f.getAnnotation(FieldProps.class);
						if (props != null && props.aggregateInJsonArray() != null
								&& props.aggregateInJsonArray() == AggregateInJsonArray.YYYY_MM_DD) {
							if (value == null) {
								line.add((Number) null);
								line.add((Number) null);
								line.add((Number) null);
								continue;
							}
							String data = formatar(l, f);
							line.add(data.substring(6, 11));
							line.add(data.substring(3, 5));
							line.add(data.substring(0, 2));
							continue;
						}

						if (value == null)
							line.add((Number) null);
						else if (value instanceof Boolean)
							line.add((Boolean) value);
						else if (value instanceof String)
							line.add((String) value);
						else if (value instanceof Double)
							line.add((Double) value);
						else if (value instanceof Float)
							line.add((Float) value);
						else if (value instanceof Integer)
							line.add((Integer) value);
						else if (value instanceof Long)
							line.add((Long) value);
						else if (value instanceof Date)
							line.add(new DefaultDateAdapter().format((Date) value));
						else if (value instanceof IEntidade)
							line.add(((IEntidade) value).getDescr());
						else if (value instanceof IEnum)
							line.add(((IEnum) value).getDescr());
						else
							throw new RuntimeException("Tipo de dado não suportado: " + value.getClass().getName());
					}
					r.add(line);
				}
			}
			return r.toString();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private String getFieldName(Field f) {
		FieldProps ann = f.getAnnotation(FieldProps.class);
		if (ann != null && !ann.name().isEmpty())
			return ann.name();
		TemisCaptionBuilder cb = new TemisCaptionBuilder();
		return cb.buildCaptionFromName(f.getName());
	}

	private String getFieldAlign(Field f) {
		FieldProps ann = f.getAnnotation(FieldProps.class);
		if (ann != null) {
			Align align = ann.align();
			if (align == Align.DEFAULT)
				align = Align.CENTER;
			return "text-align: " + align.name().toLowerCase() + ";";
		}
		return "";
	}

	private String getActionFieldName(Field f) {
		Edit ann = f.getAnnotation(Edit.class);
		if (ann != null && !ann.caption().isEmpty())
			return ann.caption();
		TemisCaptionBuilder cb = new TemisCaptionBuilder();
		return cb.buildCaptionFromName(f.getName());
	}

	public String gerarHtml() throws Exception {
//		List<Field> fields = ModeloUtils.getFieldList(linhas.get(0).getClass());
		StringBuilder b = new StringBuilder();

		b.append("<table class=\"table table-striped table-sm\"><thead class=\"table-dark\"><tr>");
		// Colunas
		for (Field f : fields)
			b.append("<th style=\"" + getFieldAlign(f) + "\">" + TemisCaptionBuilder.build(getFieldName(f)) + "</th>");
		b.append("</tr></thead><tbody>");

		// Linhas
		for (LinhaDeRelatorio l : getLinhas()) {
			if (l.tipoDeLinha == TipoDeLinhaDeRelatorioEnum.TOTAL)
				b.append("<tr class=\"table-info\" style=\"font-weight: bold;\">");
			else
				b.append("<tr>");
			for (Field f : fields)
				b.append("<td style=\"" + getFieldAlign(f) + "\">" + formatar(l, f) + "</td>");
			b.append("</tr>");
		}
		b.append("</tbody></table>");
		return b.toString();
	}

	public void gerarCabecalhoHtmlGrupoAbre(StringBuilder b, String label) {
		b.append("<div class=\"report-group row\">");
		b.append("<div class=\"report-title col col-12\">" + label + "</div>");
	}

	public void gerarCabecalhoHtmlGrupoFecha(StringBuilder b) {
		b.append("</div>");
	}

	public void gerarCabecalhoHtmlCampo(StringBuilder b, String label, String value, int cols) {
		b.append("<div class=\"report-field col col-" + cols + "\">");
		b.append("<div class=\"report-label\">");
		b.append("<div>" + label + "</div>");
		b.append("</div>");
		b.append("<div class=\"report-value\">" + value + "</div>");
		b.append("</div>");
	}

	public int getColsForXL(Show ann) {
		if (ann.colXL() > 0)
			return ann.colXL();
		if (ann.colL() > 0)
			return ann.colL();
		if (ann.colM() > 0)
			return ann.colM();
		if (ann.colS() > 0)
			return ann.colS();
		return ann.colXS();
	}

	public String gerarCabecalhoHtml() {
		boolean fFS = false;
		StringBuilder b = new StringBuilder();
		b.append("<table style=\"width: 100%;\"><tr><td style=\"padding:0;\">");
		b.append("<h1 style=\"margin:0;\">" + this.getClass().getAnnotation(Global.class).singular() + "</h1>");
		b.append("</td><td style=\"text-align: right;\">");
		b.append(
				"<img height=\"2em\" src=\"data:image/png;base64,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\"/>");
		b.append(
				"<img height=\"2em\" src=\"data:image/png;base64,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\"/>");
		b.append("</td></tr></table>");

		for (Field fld : ModeloUtils.getFieldList(this.getClass())) {
			Show ann = fld.getAnnotation(Show.class);
			if (ann != null) {
				FieldSet annFS = fld.getAnnotation(FieldSet.class);
				if (annFS != null || !fFS) {
					if (fFS)
						gerarCabecalhoHtmlGrupoFecha(b);
					gerarCabecalhoHtmlGrupoAbre(b, annFS != null ? annFS.caption() : "");
					fFS = true;
				}
				Object o = null;
				gerarCabecalhoHtmlCampo(b, getActionFieldName(fld), formatar(this, fld), getColsForXL(ann));
			}
		}
		return b.toString();
	}

	public byte[] gerarPdf() throws Exception {
		String html = gerarCabecalhoHtml();
		html += gerarHtml();

		String padrao = SwaggerUtils.convertStreamToString(Relatorio.class.getResourceAsStream("relatorio.html"));
//	    padrao = padrao.replace("size: A4 portrait;", "size: " + m.group("format") + ";");

		html = extrair(padrao, "<body>", "</body>", html).strRestante;
		Document doc = Jsoup.parse(html);
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withW3cDocument(new W3CDom().fromJsoup(doc), "/");
			builder.toStream(os);
			builder.run();
			return os.toByteArray();
		}
	}

	private static Extraido extrair(String str, String ini, String fim, String substituto) {
		Extraido e = new Extraido();
		int iIni = str.indexOf(ini);
		if (iIni == -1)
			return null;
		int iFim = str.substring(iIni).indexOf(fim) + iIni;

		if (iIni == -1 || iFim == -1)
			return null;
		String sPre = str.substring(0, iIni);
		String sSuf = str.substring(iFim + fim.length());
		e.strExtraida = str.substring(iIni + ini.length(), iFim);
		e.strRestante = sPre + substituto + sSuf;
		return e;
	}

	private static class Extraido {
		String strExtraida;
		String strRestante;
	}

	private String formatar(Object l, Field f) {
		Object o;
		try {
			o = f.get(l);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		if (o == null)
			return "";
		if (o instanceof IEntidade)
			return ((IEntidade) o).getDescr();
		if (o instanceof IEnum)
			return ((IEnum) o).getDescr();
		if (o instanceof LocalDate) {
			DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");
			return dtfOut.print((LocalDate) o);
		}
		if (o instanceof Date) {
			FieldProps props = f.getAnnotation(FieldProps.class);
			if (props != null && props.format() != null) {
				if (props.format() == Format.DATE) {
					SimpleDateFormat formatadorDATE = new SimpleDateFormat("dd/MM/yyyy");
					return formatadorDATE.format(o);
				} else if (props.format() == Format.DATE_HH_MM_SS) {
					SimpleDateFormat formatadorDATE_HH_MM_SS = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					return formatadorDATE_HH_MM_SS.format(o);
				}
			}
		}
		return o.toString();
	}

}
