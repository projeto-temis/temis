package br.gov.jfrj.siga.cp.util;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import br.jus.trf2.temis.core.util.BadRequestException;
import br.jus.trf2.temis.core.util.Dao;
import br.jus.trf2.temis.crp.model.CrpOrgaoUsuario;

public abstract class MatriculaUtils {

	private static class Separado {
		String sigla;
		String complemento;
	}

	public static Separado separar(String junto, boolean complementoNumerico) {
		Separado separado = new Separado();

		Map<String, CrpOrgaoUsuario> mapAcronimo = new TreeMap<String, CrpOrgaoUsuario>();
		for (CrpOrgaoUsuario ou : Dao.getInstance().listarTodos(CrpOrgaoUsuario.class, null)) {
			mapAcronimo.put(ou.getAcronimo(), ou);
			mapAcronimo.put(ou.getSigla(), ou);
		}

		SortedSet<String> set = new TreeSet<>(new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				int i = Integer.compare(arg1.length(), arg0.length());
				if (i != 0)
					return i;
				return arg0.compareTo(arg1);
			}
		});
		set.addAll(mapAcronimo.keySet());
		String acronimos = "";
		for (String s : set) {
			if (acronimos.length() > 0)
				acronimos += "|";
			acronimos += s;
		}

		final Pattern p1 = Pattern.compile("^(?<orgao>" + acronimos
				+ "){0,1}-?(?<complemento>[0-9"
				+ (!complementoNumerico ? "A-Za-z\\-\\/\\º\\ª\\_\\ " : "") + "]{1,20})$");
		final Matcher m1 = p1.matcher(junto);

		if (m1.find()) {
			String orgao = m1.group("orgao");
			String complemento = m1.group("complemento");

			if (orgao != null && orgao.length() > 0) {
				if (mapAcronimo.containsKey(orgao)) {
					separado.sigla = orgao;
				}
			}
			if (complemento != null) {
				separado.complemento = complemento;
			}
		}
		return separado;
	}

	protected static void validaPreenchimentoMatricula(String matricula)
			throws BadRequestException {
		if (StringUtils.isBlank(matricula) || matricula.length() <= 2) {
			throw new BadRequestException(
					"A matrícula informada é nula ou inválida. Matrícula: "
							+ matricula);
		}
	}

	public static String getSiglaDoOrgaoDaMatricula(String matricula)
			throws BadRequestException {
		validaPreenchimentoMatricula(matricula);
		Separado separado = separar(matricula, true);
		String sigla = separado.sigla;
		if (StringUtils.isNumeric(sigla)) {
			throw new BadRequestException(
					"A sigla da matrícula é inválida. Matrícula: " + matricula
							+ ". Sigla: " + sigla);
		}

		return sigla;
	}
}
