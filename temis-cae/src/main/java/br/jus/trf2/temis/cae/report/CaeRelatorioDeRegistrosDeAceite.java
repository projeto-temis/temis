package br.jus.trf2.temis.cae.report;

import java.util.Date;

import javax.validation.constraints.NotNull;

import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.FieldProps;
import com.crivano.juia.annotations.FieldProps.Align;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuReport;

import br.jus.trf2.temis.core.Relatorio;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@MenuReport
@Global(singular = "Relatório de Registros de Aceite", plural = "Relatórios de Registros de Aceite", gender = Gender.HE)
public class CaeRelatorioDeRegistrosDeAceite extends Relatorio {

	@NotNull
	@Edit(colM = 3)
	Integer ano = 2023;

	@AllArgsConstructor(staticName = "of")
	public static class Linha extends LinhaDeRelatorio {
		String magistrado;

		@FieldProps(align = Align.CENTER)
		Date data;

		String aceite;
	}

	@Override
	public void gerar() {
		linha(Linha.of("Renato", new Date(), "Sim"));
		linha(Linha.of("Mônica", new Date(), "Não"));
	}
}
