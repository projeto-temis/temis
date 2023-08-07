/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package br.jus.trf2.temis.crp.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.core.Objeto;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeServicoEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@SuppressWarnings("serial")
@Entity
@Table(name = "corporativo.cp_servico")
@Immutable
//@Cache(region = CpDao.CACHE_CORPORATIVO, usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Global(singular = "Serviço", plural = "Serviços", gender = Gender.HE)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class CrpServico extends Objeto {
	public static ActiveRecord<CrpServico> AR = new ActiveRecord<>(CrpServico.class);

	@Id
	@SequenceGenerator(name = "CP_SERVICO_SEQ", sequenceName = "CORPORATIVO.CP_SERVICO_SEQ")
	@GeneratedValue(generator = "CP_SERVICO_SEQ")
	@Column(name = "ID_SERVICO", unique = true, nullable = false)
	private Long id;

	@Column(name = "SIGLA_SERVICO", nullable = false, length = 35)
	private String sigla;

	@Column(name = "DESC_SERVICO", nullable = false, length = 200)
	private String descr;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_SERVICO_PAI")
	private CrpServico pai;

	@Column(name = "ID_TP_SERVICO", nullable = false)
	private CrpTipoDeServicoEnum tipo;

	@Column(name = "LABEL_SERVICO", length = 35)
	private String label;

	/**
	 * @return retorna a sigla retirando a parte inicial pertencente ao seviço pai.
	 * 
	 */
	public String getSiglaSemPartePai() {
		String siglaSrv = getSigla();
		if (pai != null) {
			String siglaPai = pai.getSigla();
			int pos = siglaSrv.indexOf(siglaPai);
			int tam = siglaPai.length();
			if (pos >= 0 && tam > 0) {
				siglaSrv = siglaSrv.substring(pos + tam, siglaSrv.length());
			}
			if (siglaSrv.charAt(0) == '-') {
				siglaSrv = siglaSrv.substring(1);
			}
		}
		return siglaSrv;
	}

	/**
	 * @return retorna o nível hierárquico do serviço
	 */
	public int getNivelHierarquico() {
		if (pai == null) {
			return 0;
		} else {
			return pai.getNivelHierarquico() + 1;
		}
	}
}
