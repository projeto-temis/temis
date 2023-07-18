package br.jus.trf2.temis.cae.model;

import java.util.SortedSet;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.FieldSet;
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
@FieldNameConstants
@MenuList
@Global(singular = "Comissão Temática", plural = "Comissões Temáticas", gender = Gender.SHE, codePrefix = "TM", deletable = true, inactivable = true)
public class CaeTematica extends Entidade {

	@Search
	@NotNull
	@Edit(caption = "Nome", colM = 6)
	String nome;

	@Search
	@NotNull
	@Edit(colM = 3)
	LocalDate dataDeInicio;

	@Search
	@NotNull
	@Edit(colM = 3)
	LocalDate dataDeFim;

	@Override
	public String getDescr() {
		return nome;
	}

	@Override
	public String getDescrCompleta() {
		return nome;

	}

	@Override
	public String getSelectFirstLine() {
		return getTitle();
	}

	@Override
	public String getTitle() {
		return getCodigo() + " - " + nome;
	}

	@Override
	public void addActions(SortedSet<IJuiaAction> set) {
		super.addActions(set);
		// set.add(new Editar<Processo>());
		set.add(new Editar());
		set.add(new Auditar());
	}

}
