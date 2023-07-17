package br.jus.trf2.temis.cae.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import com.crivano.juia.annotations.Detail;
import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.EditKindEnum;
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
@Global(singular = "Tipo de Curso", plural = "Tipos de Curso", gender = Gender.HE, locator = "cae-tipo-de-curso", codePrefix = "TC", deletable = true)
public class CaeTipoDeCurso extends Entidade {
	@Search
	@NotNull
	@Edit(colM = 3)
	String nome;
	
	@Search
	@NotNull
	@Edit(caption = "Descrição", colM = 9)
	String descricao;
	
	@NotNull
	@Edit(caption = "Observações", kind = EditKindEnum.TEXTAREA, colM = 12)
	String obs;

	@NotNull
	@Edit(kind = EditKindEnum.TEXTAREA, colM = 12)
	String pedido;

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
