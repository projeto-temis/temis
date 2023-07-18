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
@Global(singular = "Relatório de Magistrados por Mês", plural = "Relatórios de Magistrados por Mês", gender = Gender.HE)
public class CaeRelatorioDeMagistradosPorMes extends Relatorio {

	@Search
	@NotNull
	@Edit(colM = 3)
	Integer ano = 2023;

	@AllArgsConstructor(staticName = "of")
	public static class Linha extends LinhaDeRelatorio {
		String mes;
		String magistrado;
	}

	@Override
	public void gerar() {
		linha(Linha.of("Janeiro", "Renato"));
		linha(Linha.of("Janeiro", "Mônica"));
	}
}
