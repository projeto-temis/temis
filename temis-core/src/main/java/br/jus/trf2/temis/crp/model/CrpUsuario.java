package br.jus.trf2.temis.crp.model;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.crivano.jbiz.IActor;
import com.crivano.jbiz.IPersistent;
import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.Search;

import br.jus.trf2.temis.iam.model.Empresa;
import br.jus.trf2.temis.iam.model.enm.AgentePapelEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
@Global(singular = "Usuário", plural = "Usuários", gender = Gender.HE)
public class CrpUsuario implements IActor, IPersistent {
	public static class XPapel implements Comparable<XPapel> {
		public String nome;
		public String siglaUnidade;
		@Enumerated(EnumType.STRING)
		public AgentePapelEnum agentePapel;
		public CrpOrgaoUsuario empresa;
		public CrpPessoa pessoa;
//		public CrpPapel papel;
		public boolean ativo;
		public String nomePessoa;

		@Override
		public int compareTo(XPapel o) {
			int i;
			if (empresa == null && o.empresa != null)
				return 1;
			if (empresa != null && o.empresa == null)
				return -1;
			if (empresa != null && o.empresa != null) {
				i = empresa.getId().compareTo(o.empresa.getId());
				if (i != 0)
					return i;
			}

			if (pessoa == null && o.pessoa != null)
				return 1;
			if (pessoa != null && o.pessoa == null)
				return -1;
			if (pessoa != null && o.pessoa != null) {
				i = pessoa.getId().compareTo(o.pessoa.getId());
				if (i != 0)
					return i;
			}

//			if (papel == null && o.papel != null)
//				return 1;
//			if (papel != null && o.papel == null)
//				return -1;
//			if (papel != null && o.papel != null) {
//				i = papel.getId().compareTo(o.papel.getId());
//				if (i != 0)
//					return i;
//			}

			return 0;
		}
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Search
	@Edit(caption = "Email", colM = 4)
	private String email;

	@Search
	@Edit(caption = "Empresa Corrente", colM = 4)
	CrpOrgaoUsuario empresa;

	@Search
	@Edit(caption = "Agente Papel", colM = 4, attr = "disabled=true")
	@Enumerated(EnumType.STRING)
	AgentePapelEnum agentePapel;

	@Search
	@Edit(caption = "Pessoa Corrente", colM = 6)
	CrpPessoa pessoa;

//	@Search
//	@Edit(caption = "Papel Corrente", colM = 6)
//	Papel papel;

	// private UsuarioConfig config;

	@Transient
	private boolean admin;

	@Transient
	private String loginUrl;

	@Transient
	private String logoutUrl;

	@Transient
	Set<String> modules;

	@Transient
	SortedSet<XPapel> xPapel;

	public boolean isLoggedIn() {
		return email != null;
	}

	public String getSelectFirstLine() {
		return email;
	}

	public String getSelectSecondLine() {
		if (pessoa != null) {
			CrpPessoa p = pessoa;
			return p.getNome() + " (" + p.getCpf() + ")";
		}
		return null;
	}

	private String identificadorEmpresa(Empresa empresa) {
		if (empresa == null)
			return "";
		return empresa.getIdentificador() + " ";
	}

	public void updateNonPersistentMembers() {
	}

	public String getCode() {
		return this.email;
	}

	public String getDescr() {
//		if (this.pessoa != null) {
//			CrpPessoa pessoa = this.pessoa;
//			if (pessoa.getApelido() != null)
//				return pessoa.getApelido();
//		}
		return this.email.replace("@gmail.com", "");
	}

	public boolean isAgenteProprietarioOuAdministrador() {
		if (agentePapel == null)
			return false;
		return agentePapel == AgentePapelEnum.ADMINISTRADOR || agentePapel == AgentePapelEnum.PROPRIETARIO;
	}
}