package br.jus.trf2.temis.crp.model.enm;

import br.gov.jfrj.siga.cp.converter.IEnumWithId;

public enum CrpTipoDeIdentidadeEnum implements IEnumWithId {
	FORMULARIO(1, "Formulário"),
	CERTIFICADO(2, "Certificado"),
	LOGIN_DOE(3, "Login DOE");

	private int id;
	private String descr;

	CrpTipoDeIdentidadeEnum(int id, String descr) {
		this.id = id;
		this.descr = descr;
	}

	public String getDescr() {
		return this.descr;
	}

	public Integer getId() {
		return id;
	}

	public static CrpTipoDeIdentidadeEnum getById(Integer id) {
		return IEnumWithId.getEnumFromId(id, CrpTipoDeIdentidadeEnum.class);
	}
}
