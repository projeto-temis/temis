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
 * Criado em  21/12/2005
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

import br.jus.trf2.temis.core.util.ModeloUtils.Desconsiderar;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@SuppressWarnings("serial")
@Entity
@Table(name = "corporativo.dp_cargo")
//@Cache(region = CpDao.CACHE_CORPORATIVO, usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Getter
@Setter
@EqualsAndHashCode
@FieldNameConstants
public class CrpCargo implements Historico<CrpCargo> {

	@Id
	@SequenceGenerator(name = "DP_CARGO_SEQ", sequenceName = "CORPORATIVO.DP_CARGO_SEQ")
	@GeneratedValue(generator = "DP_CARGO_SEQ")
	@Column(name = "ID_CARGO", unique = true, nullable = false)
	@Desconsiderar
	private Long id;

	/** Campos que geram versionamento de registro **/
	@Column(name = "NOME_CARGO", nullable = false, length = 100)
	private String nome;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DT_FIM_CARGO", length = 19)
	private Date termino;

	@Column(name = "SIGLA_CARGO", length = 30)
	private String sigla;

	@Column(name = "IDE_CARGO", length = 256)
	@Desconsiderar
	private String idExterna;

	/******** ********/

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DT_INI_CARGO", length = 19)
	@Desconsiderar
	private Date inicio;

	@Column(name = "ID_CARGO_INICIAL")
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
//	@Desconsiderar
//	@JoinColumn(name = "HIS_IDC_FIM")
//	private CrpIdentidade idcTermino;

//	@Formula(value = "REMOVE_ACENTO(NOME_CARGO)")
//	@Desconsiderar
//	private String nomeCargoAI;

	public CrpCargo() {

	}
}
