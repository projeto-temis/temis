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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.jus.trf2.temis.core.util.ModeloUtils.Desconsiderar;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeIdentidadeEnum;
import lombok.Data;

@NamedQueries({
		@NamedQuery(name = "consultarIdentidadeCadastrante", query = "select u from CrpIdentidade u, CrpPessoa pes "
				+ "     where u.login = :nmUsuario"
				+ "      and u.pessoa.cpf = pes.cpf"
				+ "      and pes.sesb = :sesbPessoa"
				+ "      and pes.termino is null"
				+ "		 and u.tipo in (:listaTipo)"),
		@NamedQuery(name = "consultarIdentidadeCadastranteAtiva", query = "select u from CrpIdentidade u , CrpPessoa pes "
				+ "where ((u.login = :nmUsuario and pes.sesb = :sesbPessoa and pes.sesb is not null) or "
				+ " (pes.cpf is not null and pes.cpf <> :cpfZero and pes.cpf = :cpf)) "
				+ "and u.pessoa.idInicial = pes.idInicial "
				+ "and u.termino is null "
				+ "and u.cancelamento is null "
				+ "and (u.expiracao is null or u.expiracao > current_date()) "
				+ "and pes.termino is null "
				+ "and (pes.situacaoFuncional = :sfp1 "
				+ "or pes.situacaoFuncional = :sfp2 "
				+ "or pes.situacaoFuncional = :sfp4 "
				+ "or pes.situacaoFuncional = :sfp11 "
				+ "or pes.situacaoFuncional = :sfp12 "
				+ "or pes.situacaoFuncional = :sfp22 "
				+ "or pes.situacaoFuncional = :sfp31 "
				+ "or pes.situacaoFuncional = :sfp36 "
				+ "or pes.situacaoFuncional = :sfp38) "
				+ "and u.tipo in (:listaTipo)") })

@SuppressWarnings("serial")
@Entity
@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Table(name = "corporativo.cp_identidade")
@Data
public class CrpIdentidade {

	public static final long PIN_LENGTH = 8L;
	public static final int PIN_NUM_MAX_TENTATIVAS = 5;

	@Id
	@SequenceGenerator(name = "CP_IDENTIDADE_SEQ", sequenceName = "CORPORATIVO.CP_IDENTIDADE_SEQ")
	@GeneratedValue(generator = "CP_IDENTIDADE_SEQ")
	@Column(name = "ID_IDENTIDADE", unique = true, nullable = false)
	@Desconsiderar
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORGAO_USU", nullable = false)
	private CrpOrgaoUsuario orgao;

	@Column(name = "ID_TP_IDENTIDADE")
	private CrpTipoDeIdentidadeEnum tipo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PESSOA")
	private CrpPessoa pessoa;

	@Column(name = "SENHA_IDENTIDADE", length = 40)
	private String senha;

	@Column(name = "SENHA_IDENTIDADE_CRIPTO")
	private String senhaCripto;

	@Column(name = "SENHA_IDENTIDADE_CRIPTO_SINC")
	@Desconsiderar
	private String senhaCriptoSinc;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATA_CANCELAMENTO_IDENTIDADE", length = 19)
	private Date cancelamento;

	@Column(name = "DATA_CRIACAO_IDENTIDADE", length = 19)
	@Temporal(TemporalType.TIMESTAMP)
	private Date criacao;

	@Column(name = "DATA_EXPIRACAO_IDENTIDADE", length = 19)
	@Temporal(TemporalType.TIMESTAMP)
	private Date expiracao;

	@Column(name = "LOGIN_IDENTIDADE", length = 20)
	private String login;

	@Column(name = "PIN_IDENTIDADE")
	private String pin;

	@Column(name = "PIN_CONTADOR_TENTATIVA")
	@Desconsiderar
	private Integer pinContadorTentativa;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "HIS_DT_INI")
	@Desconsiderar
	private Date inicio;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "HIS_DT_FIM")
	@Desconsiderar
	private Date termino;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "HIS_ID_INI", insertable = false, updatable = false)
	@Desconsiderar
	private CrpIdentidade idInicial;

	@Column(name = "HIS_ATIVO")
	private Integer ativo;
}
