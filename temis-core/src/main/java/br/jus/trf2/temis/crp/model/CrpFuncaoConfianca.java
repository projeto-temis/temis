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
/*
 * Criado em  20/12/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;

import br.jus.trf2.temis.core.util.ModeloUtils.Desconsiderar;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Table(name = "corporativo.dp_funcao_confianca")
//@Cache(region = CpDao.CACHE_CORPORATIVO, usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Global(singular = "Lotação", plural = "Lotações", gender = Gender.SHE, locator = "crp-lotacao", versionable = true)
@Getter
@Setter
@EqualsAndHashCode
@FieldNameConstants
public class CrpFuncaoConfianca implements Historico<CrpFuncaoConfianca> {

	@Id
	@SequenceGenerator(name = "DP_FUNCAO_CONFIANCA_SEQ", sequenceName = "CORPORATIVO.DP_FUNCAO_CONFIANCA_SEQ")
	@GeneratedValue(generator = "DP_FUNCAO_CONFIANCA_SEQ")
	@Column(name = "ID_FUNCAO_CONFIANCA", unique = true, nullable = false)
	@Desconsiderar
	private Long id;

	/** Campos que geram versionamento de registro **/
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DT_FIM_FUNCAO_CONFIANCA", length = 19)
	private Date termino;

	@Column(name = "CATEGORIA_FUNCAO_CONFIANCA", length = 15)
	@Desconsiderar
	private String categoria;

	@Column(name = "COD_FOLHA_FUNCAO_CONFIANCA")
	@Desconsiderar
	private Integer codigo;

	@Column(name = "IDE_FUNCAO_CONFIANCA", length = 256)
	@Desconsiderar
	private String idExterna;

	@Desconsiderar
	@Column(name = "ID_FUNCAO_CONFIANCA_PAI")
	private Long pai;

	@Desconsiderar
	@Column(name = "NIVEL_FUNCAO_CONFIANCA")
	private Integer nivel;

	@Column(name = "NOME_FUNCAO_CONFIANCA", nullable = false, length = 100)
	private String nome;

	@Column(name = "SIGLA_FUNCAO_CONFIANCA", length = 30)
	private String sigla;

	/******** ***********/

	@Desconsiderar
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DT_INI_FUNCAO_CONFIANCA", length = 19)
	private Date inicio;

	@Column(name = "ID_FUN_CONF_INI")
	@Desconsiderar
	private Long idInicial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORGAO_USU", nullable = false)
	private CrpOrgaoUsuario orgao;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "HIS_IDC_INI")
//	@Desconsiderar
//	private CrpIdentidade idcInicio;
//
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "HIS_IDC_FIM")
//	@Desconsiderar
//	private CrpIdentidade idcTermino;

//	@Formula(value = "REMOVE_ACENTO(NOME_FUNCAO_CONFIANCA)")
//	@Desconsiderar
//	private String nomeSemAcentos;

	public CrpFuncaoConfianca() {
		super();
	}

	// @Override
	// public String getNomeFuncao() {
	// String nome = super.getNomeFuncao();
	// if (super.getNomeFuncao().indexOf("(") > 0)
	// nome = super.getNomeFuncao().substring(0,
	// super.getNomeFuncao().indexOf("(")).trim();
	// return nome;
	// }

	public String iniciais(String s) {
		final StringBuilder sb = new StringBuilder(10);
		boolean f = true;

		s = s.replace(" E ", " ");
		s = s.replace(" DA ", " ");
		s = s.replace(" DE ", " ");
		s = s.replace(" DO ", " ");

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (f) {
				sb.append(c);
				f = false;
			}
			if (c == ' ') {
				f = true;
			}
		}
		return sb.toString();
	}

	public String getIniciais() {
		return iniciais(getNome());
	}

//	public String getDescricaoIniciaisMaiusculas() {
//		return Texto.maiusculasEMinusculas(getDescricao());
//	}

//	public boolean semelhante(Assemelhavel obj, int profundidade) {
//		return SincronizavelSuporte.semelhante(this, obj, profundidade);
//	}

	public boolean equivale(Object other) {
		if (other == null)
			return false;
		return this.getIdInicial().longValue() == ((CrpFuncaoConfianca) other)
				.getIdInicial().longValue();
	}

}
