package br.jus.trf2.temis.core;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.core.util.ActiveRecord;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Entity
@Data
@FieldNameConstants
@Global(singular = "Próximo Código", plural = "Próximos Códigos", gender = Gender.HE)
public class ProximoCodigo {
	public static final ActiveRecord<ProximoCodigo> AR = new ActiveRecord<>(ProximoCodigo.class);

	@Id
	String prefixo;

	int contador;
}
