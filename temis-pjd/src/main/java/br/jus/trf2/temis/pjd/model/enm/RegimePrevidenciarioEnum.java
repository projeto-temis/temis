package br.jus.trf2.temis.pjd.model.enm;

import com.crivano.jbiz.IEnum;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Global(singular = "Regime Previdenciário", plural = "Regimes Previdenciários", gender = Gender.HE)
@AllArgsConstructor
@Getter
public enum RegimePrevidenciarioEnum implements IEnum {
	APOSENTADORIA_INTEGRAL("Aposentadoria Integral"),
	//
	REGRA_TRANSITORIA("Regra Transitória"),
	//
	FUNPRESP("FUNPRESP");

	private final String nome;

	@Override
	public Long getId() {
		return (long) this.ordinal();
	}

	@Override
	public String getCode() {
		return this.name();
	}

	@Override
	public String getDescr() {
		return this.nome;
	}
}
