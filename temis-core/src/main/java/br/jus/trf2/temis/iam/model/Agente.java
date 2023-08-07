package br.jus.trf2.temis.iam.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.crivano.jbiz.IActor;
import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.crp.model.CrpIdentidade;
import br.jus.trf2.temis.crp.model.CrpLotacao;
import br.jus.trf2.temis.crp.model.CrpOrgaoUsuario;
import br.jus.trf2.temis.crp.model.CrpTipoDeLotacao;

import com.crivano.juia.annotations.Search;

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
@Global(singular = "Agente", plural = "Agentes", gender = Gender.HE, locator = "iam-agente")
public abstract class Agente extends Entidade implements IActor {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Edit
	@Search
	String identidade;
	@Edit
	String senha;
}
