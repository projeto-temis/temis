package br.jus.trf2.temis.core.util;

import com.crivano.juia.CaptionBuilder;

public class TemisCaptionBuilder implements CaptionBuilder {
	@Override
	public String buildCaptionFromName(String name) {
		String s = Texto.quebrarCamelCase(name);
		s = Texto.maiusculasEMinusculas(s);
		s = Acentuacao.acentuarPalavra(s);
		return s;
	}

	public static String build(String name) {
		return new TemisCaptionBuilder().buildCaptionFromName(name);
	}
}
