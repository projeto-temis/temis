package br.jus.trf2.temis.iam.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.Search;

import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.core.util.Utils;
import br.jus.trf2.temis.crp.model.CrpIdentidade;
import br.jus.trf2.temis.crp.model.CrpLotacao;
import br.jus.trf2.temis.crp.model.CrpOrgaoUsuario;
import br.jus.trf2.temis.crp.model.CrpTipoDeLotacao;
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
@Global(singular = "Unidade", plural = "Unidades", gender = Gender.SHE, locator = "iam-unidade")
public class Unidade extends Entidade implements Comparable<Unidade> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Search(caption = "Sigla")
	@Edit(caption = "Sigla", colM = 6)
	private String sigla;

	@Search
	@Edit(caption = "Nome", colM = 6)
	private String nome;

	public String getSelectFirstLine() {
		return Utils.concat(sigla, nome);
	}

	public String getSelectSecondLine() {
		return null;
	}

	@Override
	public String getCode() {
		return this.sigla;
	}

	@Override
	public String getDescr() {
		return this.getNome();
	}

	@Override
	public int compareTo(Unidade o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
