package br.jus.trf2.temis.core.action;

import com.crivano.jbiz.IActor;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.core.Etiqueta;
import br.jus.trf2.temis.core.Evento;
import br.jus.trf2.temis.core.util.ContextInterceptor;
import lombok.Data;

@Data
@Global(singular = "Cancelamento", plural = "Cancelamentos", gender = Gender.SHE, action = "Cancelar", icon = "fa fa-ban")
public class CancelarMiniAction<E extends Entidade, V extends Evento<E, V>> extends Evento<E, V> {

	@Override
	public String getConfirmation(E entity, V event) {
		Global global = event.getClass().getAnnotation(Global.class);
		if (global != null && global.singular() != null && global.gender() != null)
			return "Tem certeza que deseja cancelar " + (global.gender() == Gender.HE ? "o" : "a") + " "
					+ global.singular().toLowerCase() + "?";

		return "Tem certeza que deseja cancelar '" + event.toString() + "'?";
	}

	@Override
	public void execute(IActor actor, IActor onBehalfOf, E entity, V event, Etiqueta tag) throws Exception {
		entity.removeChange(event);
		ContextInterceptor.getDao().persist(entity);
	}

}
