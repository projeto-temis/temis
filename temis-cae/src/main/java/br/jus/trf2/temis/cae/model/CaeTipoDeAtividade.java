package br.jus.trf2.temis.cae.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuList;
import com.crivano.juia.annotations.Search;

import br.jus.trf2.temis.core.Entidade;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Getter
@Setter
@FieldNameConstants
@MenuList
@Global(singular = "Tipo de Atividade", plural = "Tipos de Atividades", gender = Gender.HE, codePrefix = "TA", deletable = true)
public class CaeTipoDeAtividade extends Entidade {
	@Search
	@NotNull
	@Edit(caption = "Descrição", colM = 3)
	String descricao;

	@Override
	public String getDescr() {
		return descricao;
	}

	@Override
	public String getDescrCompleta() {
		return descricao;
	}

	@Override
	public String getTitle() {
		return getCodigo() + " - " + descricao;
	}

	@Override
	public String getSelectFirstLine() {
		return descricao;

	}
}
