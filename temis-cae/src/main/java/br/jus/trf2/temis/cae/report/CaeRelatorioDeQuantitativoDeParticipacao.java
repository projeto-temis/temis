package br.jus.trf2.temis.cae.report;

import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.FieldProps;
import com.crivano.juia.annotations.FieldProps.Align;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuReport;
import com.crivano.juia.annotations.Search;

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
@Global(singular = "Relatório de Quantitativo de Participação", plural = "Relatórios de Quantitativo de Participação", gender = Gender.HE)
public class CaeRelatorioDeQuantitativoDeParticipacao extends Relatorio {

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
		@FieldProps(name = "Magistrado")
		String magistrado;

		@FieldProps(align = Align.RIGHT)
		Integer ano;
		String atividade;
	}

	@Override
	public void gerar() {
		linha(Linha.of("Renato", 2023, "Curso 1"));
		linha(Linha.of("Mônica", 2023, "Curso 2"));
		linha(Linha.of("Total", null, "2").total());
	}
}
