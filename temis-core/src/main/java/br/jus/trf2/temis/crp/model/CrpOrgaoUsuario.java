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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Formula;

import br.jus.trf2.temis.core.util.ModeloUtils.Desconsiderar;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@SuppressWarnings("serial")
@Entity
@Table(name = "corporativo.cp_orgao_usuario")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class CrpOrgaoUsuario implements Historico<CrpOrgaoUsuario> {
//	public static ActiveRecord<Crpario> AR = new ActiveRecord<>(
//			Crpario.class);

	@Column(name = "CGC_ORGAO_USU")
	private Integer cnpj;

	@Column(name = "COD_ORGAO_USU")
	private Integer codigo;

	@Id
	@SequenceGenerator(name = "CP_ORGAO_USUARIO_SEQ", sequenceName = "CORPORATIVO.CP_ORGAO_USUARIO_SEQ")
	@GeneratedValue(generator = "CP_ORGAO_USUARIO_SEQ")
	@Column(name = "ID_ORGAO_USU", unique = true, nullable = false)
	private Long id;

	@Column(name = "BAIRRO_ORGAO_USU", length = 50)
	private String bairro;

	@Column(name = "CEP_ORGAO_USU", length = 8)
	private String cep;

	@Column(name = "END_ORGAO_USU", length = 256)
	private String endereco;

	@Column(name = "NM_RESP_ORGAO_USU", length = 60)
	private String nomeDoResponsavel;

	@Column(name = "RAZAO_SOCIAL_ORGAO_USU", length = 256)
	private String razaoSocial;

	@Column(name = "UF_ORGAO_USU", length = 2)
	private String uf;

	@Column(name = "SIGLA_ORGAO_USU", length = 15)
	private String sigla;

	@Column(name = "MUNICIPIO_ORGAO_USU", length = 50)
	private String municipio;

	@Column(name = "NM_ORGAO_USU", nullable = false, length = 256)
	private String nome;

	@Column(name = "TEL_ORGAO_USU", length = 10)
	private String telefone;

	@Column(name = "ACRONIMO_ORGAO_USU", length = 12)
	private String acronimo;

	@Column(name = "IS_EXTERNO_ORGAO_USU", length = 1)
	private Integer externo;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "HIS_IDC_INI")
//	@Desconsiderar
//	private CrpIdentidade idcTermino;
//
//	@ManyToOne(fetch = FetchType.LAZY)
//	@Desconsiderar
//	@JoinColumn(name = "HIS_IDC_FIM")
//	private CrpIdentidade idcInicio;

	@Column(name = "ID_ORGAO_USU_INICIAL")
	@Desconsiderar
	private Long idInicial;

	@Desconsiderar
	@Column(name = "MARCO_REGULATORIO")
	private String marcoRegulatorio;

	@Desconsiderar
	@Column(name = "DT_ALTERACAO")
	private Date dataAlteracao;

	@Column(name = "HIS_DT_INI")
	@Desconsiderar
	private Date inicio;

	@Column(name = "HIS_DT_FIM")
	@Desconsiderar
	private Date termino;

	@Column(name = "HIS_ATIVO")
	private Integer ativo;

//	@Formula(value = "REMOVE_ACENTO(NM_ORGAO_USU)")
//	private String nomeSemAcentos;

	public boolean equivale(Object other) {
		if (other == null)
			return false;
		return this.getId().longValue() == ((CrpOrgaoUsuario) other)
				.getId().longValue();
	}

}
