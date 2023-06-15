package br.jus.trf2.temis.core.util;

import com.crivano.juia.CaptionBuilder;

public class TemisCaptionBuilder implements CaptionBuilder {
	@Override
	public String buildCaptionFromName(String name) {
		String s = name;
		String result = "";
		for (int i = 0; i < s.length(); i++) {
			String ch = s.substring(i, i + 1);
			if (i == 0)
				ch = ch.toUpperCase();
			else if (ch.toUpperCase().equals(ch))
				ch = " " + ch;
			result += ch;
		}
		s = result;

		s = Texto.maiusculasEMinusculas(s);
		s = Acentuacao.acentuarPalavra(s);
		return s;
	};
}
