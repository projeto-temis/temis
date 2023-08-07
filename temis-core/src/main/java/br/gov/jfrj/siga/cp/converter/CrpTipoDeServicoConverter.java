package br.gov.jfrj.siga.cp.converter;

import javax.persistence.Converter;

import br.jus.trf2.temis.crp.model.enm.CrpTipoDeServicoEnum;

@Converter(autoApply = true)
public class CrpTipoDeServicoConverter extends EnumWithIdConverter<CrpTipoDeServicoEnum> {
}
