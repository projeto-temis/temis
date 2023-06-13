package br.jus.trf2.temis.cae.logic;

import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

import br.jus.trf2.temis.cae.model.CaeAtividade.CaeEventoDeAtividade;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class CaeParticipacaoEmAtividadeEstaAprovada implements Expression {
	CaeEventoDeAtividade evento;

	@Override
	public boolean eval() {
		return evento.isAprovada();
	}

	@Override
	public String explain(boolean result) {
		return JLogic.explain("está aprovada", result);
	}
}
