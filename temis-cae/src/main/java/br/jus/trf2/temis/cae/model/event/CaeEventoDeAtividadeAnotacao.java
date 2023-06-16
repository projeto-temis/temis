package br.jus.trf2.temis.cae.model.event;

import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.joda.time.LocalDate;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.Required;

import br.jus.trf2.temis.cae.model.CaeAtividade;
import br.jus.trf2.temis.cae.model.CaeTematica;
import br.jus.trf2.temis.cae.model.CaeAtividade.CaeEventoDeAtividade;
import br.jus.trf2.temis.cae.model.enm.CaeEspecieDeAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeModalidadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeOrgaoEnum;
import br.jus.trf2.temis.cae.model.enm.CaeParticipacaoEnum;
import br.jus.trf2.temis.cae.model.enm.CaeTipoDeAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeTurnoEnum;
import br.jus.trf2.temis.core.Acao;
import br.jus.trf2.temis.core.action.ExcluirMiniAction;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Global(singular = "Anotação", plural = "Anotações", gender = Gender.SHE, action = "Anotar", icon = "far fa-sticky-note")
public class CaeEventoDeAtividadeAnotacao extends CaeAtividade.CaeEventoDeAtividade {
	@Required
	@NonNull
	@Edit(caption = "Texto da Anotação", colXS = 12, attr = "validate=required")
	@Column(length = 2048)
	String texto;

	@Override
	public String getDescr() {
		return texto;
	}

	@Override
	protected void addMiniActions(SortedSet<Acao> set) {
		set.add(new ExcluirMiniAction());
	}

}
