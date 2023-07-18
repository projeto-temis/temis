package br.jus.trf2.temis.cae.model;

import java.util.SortedSet;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;

import com.crivano.jbiz.ITag;
import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuList;
import com.crivano.juia.annotations.Search;
import com.crivano.juia.biz.IJuiaAction;

import br.jus.trf2.temis.cae.model.CaeAtividade.CaeEventoDeAtividade;
import br.jus.trf2.temis.cae.model.enm.CaeEspecieDeAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeModalidadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeOrgaoEnum;
import br.jus.trf2.temis.cae.model.enm.CaeParticipacaoEnum;
import br.jus.trf2.temis.cae.model.enm.CaeTipoDeAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeTurnoEnum;
import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.core.action.Auditar;
import br.jus.trf2.temis.core.action.Editar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
@MenuList
@Global(singular = "Instituição de Ensino", plural = "Instituições de Ensino", gender = Gender.SHE, codePrefix = "IE", deletable = true)
public class CaeInstituicaoDeEnsino extends Entidade {
	@Search
	@NotNull
	@Edit(caption = "Nome", colM = 3)
	String nome;

	@Search
	@NotNull
	@Edit(caption = "Descrição", colM = 9)
	String descricao;

	@Override
	public String getDescr() {
		return descricao;
	}

	@Override
	public void addActions(SortedSet<IJuiaAction> set) {
		super.addActions(set);
		// set.add(new Editar<Processo>());
		set.add(new Editar());
		set.add(new Auditar());
	}

	@Override
	public void addTags(SortedSet<ITag> set) {
		super.addTags(set);

//		if (!isAutuado())
//			set.add(Etiqueta.of(null, this, null, getPessoaCadastrante(), getUnidadeCadastrante(),
//					MarcadorEnum.EM_ELABORACAO, this.getBegin(), null));
//		if (isAutuado())
//			set.add(Etiqueta.of(null, this, null, getPessoaCadastrante(), getUnidadeCadastrante(),
//					MarcadorEnum.AGUARDANDO_DECISAO, this.getBegin(), null));
	}

	@Override
	public String getTitle() {
		return getCodigo() + " - " + nome;
	}
}
