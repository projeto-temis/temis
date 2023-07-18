package br.jus.trf2.temis.core.util;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Acentuacao {

	public enum Palavra {
		CODIGO("código"),
		HORARIO("horário"),
		HORARIA("horária"),
		MES("mês"),
		NUMERO("número"),
		INICIO("início"),
		TEMATICA("temática"),
		TERMINO("término"),
		ESPECIE("espécie"),
		ORGAO("órgão");

		private final String substituicao;

		Palavra(String substituicao) {
			this.substituicao = substituicao;
		}

		public String getSubstituicao() {
			return substituicao;
		}

		static Map<String, Palavra> map = new HashMap<>();

		public static Palavra getByName(String name) {
			if (map.isEmpty())
				for (Palavra p : Palavra.values())
					map.put(p.name(), p);
			return map.get(name);
		}
	}

	public enum Terminacao {
		CAO("ção"), COES("ções"), SSAO("ssão"), SSOES("ssões");

		private final String substituicao;

		Terminacao(String substituicao) {
			this.substituicao = substituicao;
		}

		public String getSubstituicao() {
			return substituicao;
		}
	}

	public static String acentuarPalavras(String input) {
		StringBuffer output = new StringBuffer();
		Pattern pattern = Pattern.compile("\\b\\p{L}+\\b", Pattern.UNICODE_CHARACTER_CLASS);
		Matcher matcher = pattern.matcher(input);

		while (matcher.find()) {
			String palavra = matcher.group();
			String acentuada = acentuarPalavra(palavra);
			matcher.appendReplacement(output, Matcher.quoteReplacement(acentuada));
		}
		matcher.appendTail(output);

		return output.toString();
	}

	public static String acentuarPalavra(String palavra) {
		String palavraSemAcento = Normalizer.normalize(palavra, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		// Verifica se a palavra sem acento é diferente da palavra original se for, já é
		// acentuada
		if (!palavraSemAcento.equalsIgnoreCase(palavra))
			return palavra;

		String palavraComAcento;
		Palavra p = Palavra.getByName(palavraSemAcento.toUpperCase());
		if (p != null)
			palavraComAcento = p.getSubstituicao();
		else
			palavraComAcento = substituirTerminacao(palavraSemAcento.toLowerCase());

		char[] letras = palavra.toCharArray();
		char[] letrasComAcento = palavraComAcento.toCharArray();

		for (int i = 0; i < letras.length; i++) {
			if (Character.isUpperCase(letras[i])) {
				letrasComAcento[i] = Character.toUpperCase(letrasComAcento[i]);
			} else {
				letrasComAcento[i] = Character.toLowerCase(letrasComAcento[i]);
			}
		}

		String palavraComAcentoECase = new String(letrasComAcento);
		return palavraComAcentoECase;
	}

	public static String substituirTerminacao(String palavra) {
		for (Terminacao terminacao : Terminacao.values()) {
			if (palavra.endsWith(terminacao.name().toLowerCase())) {
				return palavra.substring(0, palavra.length() - terminacao.name().length())
						+ terminacao.getSubstituicao();
			}
		}
		return palavra;
	}
}