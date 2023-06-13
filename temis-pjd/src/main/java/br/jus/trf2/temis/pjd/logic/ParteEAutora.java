package br.jus.trf2.temis.pjd.logic;

import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

import br.jus.trf2.temis.pjd.model.event.EventoProcessualInclusaoDeParte;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class ParteEAutora implements Expression {
	EventoProcessualInclusaoDeParte evento;

	@Override
	public boolean eval() {
		return evento.isAutor();
	}

	@Override
	public String explain(boolean result) {
		return JLogic.explain("é parte autora", result);
	}
}
