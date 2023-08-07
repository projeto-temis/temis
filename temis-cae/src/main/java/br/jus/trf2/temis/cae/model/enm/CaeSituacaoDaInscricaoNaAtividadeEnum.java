package br.jus.trf2.temis.cae.model.enm;

import com.crivano.jbiz.IEnum;

import br.jus.trf2.temis.cae.model.CaeAtividade.CaeEventoDeAtividade;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeAprovacao;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeDeferimento;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeIndeferimento;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeInscricao;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeReprovacao;
import br.jus.trf2.temis.core.enm.MarcadorEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CaeSituacaoDaInscricaoNaAtividadeEnum implements IEnum {
	PENDENTE_DE_DEFERIMENTO("Pendente de Deferimento", MarcadorEnum.PENDENTE_DE_DEFERIMENTO,
			CaeEventoDeAtividadeInscricao.class),
	//
	DEFERIDA("Deferida", MarcadorEnum.DEFERIDO, CaeEventoDeAtividadeDeferimento.class),
	//
	INDEFERIDA("Indeferida", MarcadorEnum.INDEFERIDO, CaeEventoDeAtividadeIndeferimento.class),
	//
	APROVADA("Aprovada", MarcadorEnum.APROVADO, CaeEventoDeAtividadeAprovacao.class),
	//
	REPROVADA("Reprovada", MarcadorEnum.REPROVADO, CaeEventoDeAtividadeReprovacao.class);

	private final String nome;
	private final MarcadorEnum marcador;
	private final Class<? extends CaeEventoDeAtividade> clazz;

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
