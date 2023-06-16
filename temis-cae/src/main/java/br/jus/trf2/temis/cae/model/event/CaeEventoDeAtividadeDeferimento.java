package br.jus.trf2.temis.cae.model.event;

import java.util.SortedSet;

import javax.persistence.Entity;

import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.cae.model.CaeAtividade.CaeEventoDeAtividade;
import br.jus.trf2.temis.core.Acao;
import br.jus.trf2.temis.core.VisibilidadeDeEventoEnum;
import br.jus.trf2.temis.core.action.CancelarMiniAction;
import br.jus.trf2.temis.core.action.ExcluirMiniAction;
import br.jus.trf2.temis.core.util.DescrBuilder;
import br.jus.trf2.temis.crp.model.CrpPessoa;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Global(singular = "Deferimento de Inscrição", plural = "Deferimentos de Inscrições", gender = Gender.HE, action = "Deferir", icon = "fas fa-calendar-plus")
public class CaeEventoDeAtividadeDeferimento extends CaeEventoDeAtividade {

	@Override
	public String getDescr() {
		CrpPessoa pessoa = ((CaeEventoDeAtividadeInscricao) getReferente()).getPessoa();
		return DescrBuilder.builder().add("", pessoa.getDescrCompleta()).build();
	}

	@Override
	protected void addMiniActions(SortedSet<Acao> set) {
		set.add(new ExcluirMiniAction());
		set.add(new CancelarMiniAction());
	}

	@Override
	public VisibilidadeDeEventoEnum getVisibilidade() {
		return VisibilidadeDeEventoEnum.EXIBIR_SE_AUDITANDO;
	}
}
