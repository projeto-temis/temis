package br.jus.trf2.temis.cae.model.event;

import java.util.SortedSet;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.Required;

import br.jus.trf2.temis.cae.model.CaeAtividade.CaeEventoDeAtividade;
import br.jus.trf2.temis.core.Acao;
import br.jus.trf2.temis.core.VisibilidadeDeEventoEnum;
import br.jus.trf2.temis.core.action.CancelarMiniAction;
import br.jus.trf2.temis.core.action.ExcluirMiniAction;
import br.jus.trf2.temis.core.util.DescrBuilder;
import br.jus.trf2.temis.crp.model.CrpPessoa;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@Global(singular = "Indeferimento de Inscrição", plural = "Indeferimentos de Inscrições", gender = Gender.SHE, action = "Indeferir", icon = "fas fa-calendar-times")
public class CaeEventoDeAtividadeIndeferimento extends CaeEventoDeAtividade {

	@Required
	@NonNull
	@Edit(caption = "Justificativa", colXS = 12, attr = "validate=required")
	@Column(length = 2048)
	String texto;

	@Override
	public String getDescr() {
		@NonNull
		CrpPessoa pessoa = ((CaeEventoDeAtividadeInscricao) getReferente()).getPessoa();
		return DescrBuilder.builder().add("", pessoa.getDescrCompleta()).add("", " (", texto, ")").build();
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
