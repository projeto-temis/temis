package br.jus.trf2.temis.cae.report;

import java.util.Date;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.FieldProps;
import com.crivano.juia.annotations.FieldProps.Align;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuReport;
import com.crivano.juia.annotations.Search;

import br.jus.trf2.temis.cae.model.enm.CaeTipoDeAtividadeEnum;
import br.jus.trf2.temis.core.Relatorio;
import br.jus.trf2.temis.crp.model.CrpPessoa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MenuReport
@Global(singular = "Relatório de Histórico de Participação", plural = "Relatórios de Histórico de Participação", gender = Gender.HE)
public class CaeRelatorioDeHistoricoDeParticipacao extends Relatorio {

	@Edit(caption = "Magistrado", colM = 9)
	@NotNull
	@ManyToOne
	CrpPessoa magistrado;

	@Search
	@NotNull
	@Edit(colM = 3)
	Integer ano = 2023;

	@AllArgsConstructor(staticName = "of")
	public static class Linha extends LinhaDeRelatorio {
		@FieldProps(align = Align.CENTER)
		Date data;

		@FieldProps(align = Align.CENTER)
		CaeTipoDeAtividadeEnum tipoDeAtividade;

		@FieldProps(align = Align.CENTER)
		String tema;

		@FieldProps(align = Align.CENTER)
		String comissaoTematica;

		@FieldProps(align = Align.CENTER)
		String tipoDeParticipacao;

		@FieldProps(align = Align.CENTER)
		String aproveitamentoDeParticipacao;
	}

	@Override
	public void gerar() {
		linha(Linha.of(new Date(), CaeTipoDeAtividadeEnum.CIVIL_CURSO_DE_AMBIENTACAO, "Tema do Curso",
				"Commisão temática", "Inscrito/Coordenador", "Sim"));
	}
}
