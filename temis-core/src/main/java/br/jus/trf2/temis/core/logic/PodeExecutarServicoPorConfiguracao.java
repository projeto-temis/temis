package br.jus.trf2.temis.core.logic;

import com.crivano.jlogic.Expression;
import com.crivano.jlogic.JLogic;

import br.jus.trf2.temis.core.util.ContextInterceptor;
import br.jus.trf2.temis.crp.bl.CrpConfiguracaoBL;
import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class PodeExecutarServicoPorConfiguracao implements Expression {
	String servico;

	@Override
	public boolean eval() {
		CrpConfiguracaoBL cfgbl = new CrpConfiguracaoBL();
		return cfgbl.podeUtilizarServicoPorConfiguracao(ContextInterceptor.getContext().getTitular(),
				ContextInterceptor.getContext().getLotaTitular(), servico);
	}

	@Override
	public String explain(boolean result) {
		return JLogic.explain("pode executar serviço " + servico + " por configuração", result);
	}
};