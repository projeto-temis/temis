package br.jus.trf2.temis.cae.model;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;

import com.crivano.jsync.IgnoreForDependecyLevel;
import com.crivano.jsync.IgnoreForSimilarity;
import com.crivano.jsync.IgnoreForSimilarityIfDependent;
import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.EditKindEnum;
import com.crivano.juia.annotations.FieldSet;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuList;
import com.crivano.juia.annotations.Search;
import com.crivano.juia.biz.IJuiaAction;

import br.jus.trf2.temis.cae.model.enm.CaeTipoDeAvaliacaoDeDesempenhoEnum;
import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.core.action.Auditar;
import br.jus.trf2.temis.core.action.Editar;
import br.jus.trf2.temis.core.util.NoSerialization;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
@MenuList
@Global(singular = "Curso", plural = "Cursos", gender = Gender.HE, locator = "cae-curso", codePrefix = "CS", deletable = false)
public class CaeCurso extends Entidade {

	@Search
	@NotNull
	@Edit(caption = "Nome", colM = 6)
	String nome;

	@Entity
	@Getter
	@Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	@FieldNameConstants
	@Global(singular = "Temática", plural = "Temáticas", gender = Gender.SHE, deletable = true)
	public static class XTematica extends Entidade {
		@NoSerialization
		@IgnoreForSimilarityIfDependent
		@ManyToOne(fetch = FetchType.LAZY)
		private CaeCurso curso;

		@Search
		@Edit(caption = "Temática", colM = 8)
		@ManyToOne(fetch = FetchType.LAZY)
		CaeTematica tematica;

		@Edit(caption = "Observações", colM = 4)
		String obs;
	}

	@Entity
	@Getter
	@Setter
	@NoArgsConstructor
	@EqualsAndHashCode
	@FieldNameConstants
	@Global(singular = "Atividade", plural = "Atividades", gender = Gender.SHE, locator = "pro-curso-x-atividade", codePrefix = "CA", deletable = true)
	public static class XAtividade extends Entidade {
		@NoSerialization
		@IgnoreForSimilarityIfDependent
		@ManyToOne(fetch = FetchType.LAZY)
		private CaeCurso curso;

		@Search
		@Edit(colM = 8)
		@ManyToOne(fetch = FetchType.LAZY)
		CaeAtividade atividade;

		@Edit(caption = "Observações", colM = 4)
		String obs;
	}

	// Endereços
	@IgnoreForDependecyLevel
	@IgnoreForSimilarity
	@OneToMany(mappedBy = XTematica.Fields.curso, cascade = CascadeType.ALL)
	@FieldSet(caption = "Comissões Temáticas")
	@Edit()
	private List<XTematica> tematica = new ArrayList<>();

	// Endereços
	@IgnoreForDependecyLevel
	@IgnoreForSimilarity
	@OneToMany(mappedBy = XAtividade.Fields.curso, cascade = CascadeType.ALL)
	@FieldSet(caption = "Atividades")
	@Edit()
	private List<XAtividade> atividade = new ArrayList<>();

//	@Search
//	@NotNull
//	@Edit(colM = 3)
//	CaeTematica tematica;

//	@Search
//	@NotNull
//	@Edit(caption = "Atividade", colM = 3)
//	CaeAtividade atividade;

	@Search
	@FieldSet(caption = "Datas")
	@NotNull
	@Edit(colM = 3)
	LocalDate dataDeAberturaDasInscricoes;

	@NotNull
	@Edit(colM = 3)
	LocalDate dataDeEncerramentoDasInscricoes;

	@NotNull
	@Edit(caption = "Carga Horária Total", colM = 3)
	Integer cargaHoraria;

	@FieldSet(caption = "Avaliação de Desempenho")
	@NotNull
	@Edit(caption = "Tipo de Avaliação", colM = 3)
	CaeTipoDeAvaliacaoDeDesempenhoEnum avaliacao;

	@NotNull
	@Edit(caption = "Observações sobre a Avaliação", kind = EditKindEnum.TEXTAREA, colM = 12)
	String obsDaAvaliacao;

	@FieldSet(caption = "Informações Complementares")
	@NotNull
	@Edit(caption = "Público Alvo", kind = EditKindEnum.TEXTAREA, colM = 12)
	String publicoAlvo;

	@NotNull
	@Edit(caption = "Descrição", kind = EditKindEnum.TEXTAREA, colM = 12)
	String descricao;

	@NotNull
	@Edit(caption = "Observações", kind = EditKindEnum.TEXTAREA, colM = 12)
	String obs;

	@FieldSet(caption = "Informações de Controle")
	@Edit(caption = "Publicar Curso", colM = 4)
	boolean publicar;
	@Edit(caption = "Cancelado", colM = 4)
	boolean cancelada;

	@Override
	public String getDescr() {
		return descricao;
	}

	@Override
	public String getDescrCompleta() {
		return descricao;

	}

	@Override
	public String getSelectFirstLine() {
		return descricao;
	}

	@Override
	public String getTitle() {
		return getCodigo() + " - " + nome;
	}

	@Override
	public void addActions(SortedSet<IJuiaAction> set) {
		super.addActions(set);
		// set.add(new Editar<Processo>());
		set.add(new Editar());
		set.add(new Auditar());
	}

}
