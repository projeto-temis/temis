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
 */
package br.jus.trf2.temis.crp.model;

import java.util.Date;
import java.util.SortedSet;

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

import com.crivano.jbiz.IEvent;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.Search;

import br.jus.trf2.temis.core.Etiqueta;
import br.jus.trf2.temis.core.Evento;
import br.jus.trf2.temis.core.IEntidade;
import br.jus.trf2.temis.core.util.ModeloUtils.Desconsiderar;
import br.jus.trf2.temis.core.util.Utils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Table(name = "corporativo.dp_lotacao")
@Entity
// @SqlResultSetMapping(name = "scalar", columns = @ColumnResult(name = "dt"))
//@Cache(region = CpDao.CACHE_CORPORATIVO, usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Global(singular = "Lotação", plural = "Lotações", gender = Gender.SHE, locator = "crp-lotacao", versionable = true)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class CrpLotacao implements IEntidade, Comparable<CrpLotacao>, Historico<CrpLotacao> {

	@Id
	@SequenceGenerator(name = "DP_LOTACAO_SEQ", sequenceName = "CORPORATIVO.DP_LOTACAO_SEQ")
	@GeneratedValue(generator = "DP_LOTACAO_SEQ")
	@Column(name = "ID_LOTACAO", unique = true, nullable = false)
	private Long id;

	@Column(name = "ID_LOTACAO_INI")
	private Long idInicial;

	@Search
	@Column(name = "SIGLA_LOTACAO", length = 30)
	private String sigla;

	@Search
	@Column(name = "NOME_LOTACAO", nullable = false, length = 120)
	private String nome;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATA_FIM_LOT", length = 19)
	private Date termino;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATA_INI_LOT", nullable = false, length = 19)
	private Date inicio;

	@Column(name = "IDE_LOTACAO")
	private String idExterna;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_LOTACAO_PAI")
	private CrpLotacao pai;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORGAO_USU", nullable = false)
	private CrpOrgaoUsuario orgao;

//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "lotacaoPai")
//	@Desconsiderar
//	private Set<DpLotacao> dpLotacaoSubordinadosSet = new TreeSet<DpLotacao>();

	// @OneToMany(fetch = FetchType.LAZY, mappedBy = "lotacao")
//	@Where(clause = "DATA_FIM_PESSOA is null")
//	@Desconsiderar
//	private Set<DpPessoa> dpPessoaLotadosSet = new TreeSet<DpPessoa>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_TP_LOTACAO")
	private CrpTipoDeLotacao tipo;
//	
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "ID_LOCALIDADE")
//	private CpLocalidade localidade;

	@Column(name = "IS_EXTERNA_LOTACAO")
	private Integer externa;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "HIS_IDC_INI")
	@Desconsiderar
	private CrpIdentidade identidadeInicio;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "HIS_IDC_FIM")
	@Desconsiderar
	private CrpIdentidade identidadeTermino;

	@Column(name = "IS_SUSPENSA")
	private Integer suspensa;

	@Column(name = "MOTIVO_INATIVACAO", length = 500)
	private String motivoInativacao;

	@Column(name = "MOTIVO_ATIVACAO", length = 500)
	private String motivoAtivacao;

	// @Override
	public String getFirstLine() {
		return sigla;
	}

	@Override
	public Date getBegin() {
		return inicio;
	}

	@Override
	public void setBegin(Date dt) {
	}

	@Override
	public SortedSet<? extends IEvent<?>> getEvent() {
		return null;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getCode() {
		return sigla;
	}

	@Override
	public String getDescr() {
		return nome;
	}

	@Override
	public String getSelectFirstLine() {
		return getCode();
	}

	@Override
	public SortedSet<Etiqueta> getEtiqueta() {
		return null;
	}

	@Override
	public void prePersistAndUpdate() throws Exception {
	}

	@Override
	public SortedSet<? extends Evento<?, ?>> getEvento() {
		return null;
	}

	@Override
	public String getDescrCompleta() {
		return sigla + " - " + nome;
	}

	@Override
	public int compareTo(CrpLotacao o) {
		return Utils.compare(this.getIdInicial(), o.getIdInicial());
	}
}
