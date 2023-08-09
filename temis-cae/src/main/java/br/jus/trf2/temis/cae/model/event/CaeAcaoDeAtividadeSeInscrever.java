package br.jus.trf2.temis.cae.model.event;

import com.crivano.jbiz.IActor;
import com.crivano.jlogic.Expression;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.cae.model.CaeAtividade;
import br.jus.trf2.temis.cae.model.CaeAtividade.CaeEventoDeAtividade;
import br.jus.trf2.temis.core.Acao;
import br.jus.trf2.temis.core.Etiqueta;
import br.jus.trf2.temis.core.logic.PodeSim;
import br.jus.trf2.temis.core.util.ContextInterceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Global(singular = "Inscrição de Participante", plural = "Inscrições de Participantes", gender = Gender.SHE, action = "Se Inscrever", icon = "fas fa-plus")
@FieldNameConstants
public class CaeAcaoDeAtividadeSeInscrever implements Acao<CaeAtividade, IActor, CaeEventoDeAtividade> {
	@Override
	public Expression getActive(CaeAtividade element, CaeEventoDeAtividade event) {
		return new PodeSim();
	}

	@Override
	public void execute(IActor actor, IActor onBehalfOf, CaeAtividade entity, CaeEventoDeAtividade event, Etiqueta tag)
			throws Exception {
		CaeEventoDeAtividadeInscricao e = new CaeEventoDeAtividadeInscricao();
		e.setPessoa(ContextInterceptor.getContext().getCadastrante());
		e.execute(actor, onBehalfOf, entity, event, tag);
	}
}
