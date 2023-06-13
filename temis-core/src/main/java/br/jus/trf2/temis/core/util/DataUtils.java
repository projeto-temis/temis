package br.jus.trf2.temis.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

public class DataUtils {

	// Verifica se a data está entre o ano 2000 e o ano 2100
	@SuppressWarnings("deprecation")
	public static Boolean dataDentroSeculo21(Date data) {

		if ((data.before(new Date(100, 0, 1)) || data.after(new Date(200, 0, 1)))) {
			return false;
		} else {
			return true;
		}

	}

	public static String formatDDMMYY_AS_HHMMSS(Date dt) {
		if (dt != null) {
			final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy _ HH:mm:ss");
			return df.format(dt).replaceAll("_", "às");
		}
		return null;
	}

	public static String calcularTempoRelativo(Date anterior) {
		PrettyTime p = new PrettyTime(new Date(), new Locale("pt"));

		String tempo = p.format(anterior);
		tempo = tempo.replace(" atrás", "");
		tempo = tempo.replace(" dias", " dias");
		tempo = tempo.replace(" horas", "h");
		tempo = tempo.replace(" minutos", "min");
		tempo = tempo.replace(" segundos", "s");
		tempo = tempo.replace("agora há pouco", "agora");
		return tempo;
	}
}
