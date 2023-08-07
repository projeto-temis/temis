package br.jus.trf2.temis.cae.report;

import java.util.Date;
import java.util.SortedSet;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.FieldProps;
import com.crivano.juia.annotations.FieldProps.AggregateInJsonArray;
import com.crivano.juia.annotations.FieldProps.Align;
import com.crivano.juia.annotations.FieldProps.Format;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuReport;
import com.crivano.juia.annotations.Show;

import br.jus.trf2.temis.cae.model.CaeAtividade;
import br.jus.trf2.temis.cae.model.CaeTipoDeAtividade;
import br.jus.trf2.temis.cae.model.enm.CaeSituacaoDaInscricaoNaAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeTipoDeAtividadeEnum;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeInscricao;
import br.jus.trf2.temis.core.Relatorio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MenuReport
@Global(singular = "Relatório de Inscritos na Atividade", plural = "Relatórios de Inscritos na Atividade", gender = Gender.HE)
public class CaeRelatorioDeInscritosNaAtividade extends Relatorio {

	@Inject
	transient protected EntityManager em;

	@Edit
	@NotNull
	@ManyToOne
	CaeAtividade atividade;

	@Show(colS = 2)
	LocalDate dataDaAtividade;

	@Show(colS = 2)
	CaeTipoDeAtividadeEnum tipo;

	@Show(colS = 8)
	String tema;

	@AllArgsConstructor(staticName = "of")
	public static class Linha extends LinhaDeRelatorio {
		@FieldProps(align = Align.RIGHT)
		Integer ordem;
		@FieldProps(align = Align.CENTER, format = Format.DATE_HH_MM_SS, aggregateInJsonArray = AggregateInJsonArray.YYYY_MM_DD)
		Date dataDeInscricao;
		@FieldProps(name = "Matrícula")
		String matricula;
		String magistrado;
		CaeSituacaoDaInscricaoNaAtividadeEnum situacao;
	}

	@Override
	public void gerar() {
		dataDaAtividade = atividade.getDataDeInicio();
		tipo = atividade.getTipo();
		tema = atividade.getTema();

		SortedSet<CaeEventoDeAtividadeInscricao> inscrs = atividade
				.getEventosAtivos(CaeEventoDeAtividadeInscricao.class);

		int i = 1;
		for (CaeEventoDeAtividadeInscricao inscr : inscrs) {
			CaeSituacaoDaInscricaoNaAtividadeEnum situacaoDaInscricao = inscr.getSituacaoDaInscricao();
			// String situacaoAsString = situacaoDaInscricao != null ?
			// situacaoDaInscricao.getNome() : null;
			linha(Linha.of(i, inscr.getBegin(), inscr.getPessoa().getSigla(), inscr.getPessoa().getNome(),
					situacaoDaInscricao));
			i++;
		}
	}
}
