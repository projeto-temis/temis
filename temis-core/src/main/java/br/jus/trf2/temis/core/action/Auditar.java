package br.jus.trf2.temis.core.action;

import com.crivano.jbiz.IActor;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.core.Acao;
import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.core.Evento;
import lombok.Data;

@Data
@Global(singular = "Auditoria", plural = "Auditorias", gender = Gender.SHE, action = "Auditar", icon = "fas fa-user-secret")
public class Auditar<E extends Entidade, T extends Evento<E, T>> implements Acao<E, IActor, T> {

	@Override
	public String getClick(E entidade, T evento) {
		return "this.toggleAudit()";
	}
}
