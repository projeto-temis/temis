package br.jus.trf2.temis.crp.model.enm;

import br.gov.jfrj.siga.cp.converter.IEnumWithId;

public enum CrpTipoDeServicoEnum implements IEnumWithId {
	SISTEMA(1, "Sistema"),
	//
	DIRETORIO(2, "Diretório");

	private int id;
	private String descr;

	CrpTipoDeServicoEnum(int id, String descr) {
		this.id = id;
		this.descr = descr;
	}

	public String getDescr() {
		return this.descr;
	}

	public Integer getId() {
		return id;
	}

	public static CrpTipoDeServicoEnum getById(Integer id) {
		return IEnumWithId.getEnumFromId(id, CrpTipoDeServicoEnum.class);
	}
}
