package br.jus.trf2.temis.cae.report;

import javax.validation.constraints.NotNull;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.FieldProps;
import com.crivano.juia.annotations.FieldProps.Align;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuReport;
import com.crivano.juia.annotations.Search;

import br.jus.trf2.temis.core.Relatorio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MenuReport
@Global(singular = "Relatório de Carga Horária", plural = "Relatórios de Carga Horária", gender = Gender.HE)
public class CaeRelatorioDeCargaHoraria extends Relatorio {

	@Search
	@NotNull
	@Edit(colM = 3)
	Integer ano = 2023;

	@AllArgsConstructor(staticName = "of")
	public static class Linha extends LinhaDeRelatorio {
		@FieldProps(name = "Magistrado")
		String magistrado;

		@FieldProps(align = Align.RIGHT)
		Integer cargaHoraria;
	}

	@Override
	public void gerar() {
		linha(Linha.of("Renato", 2023));
		linha(Linha.of("Mônica", 2023));
		linha(Linha.of("Total", 2).total());
	}
}
