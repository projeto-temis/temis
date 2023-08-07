package br.jus.trf2.temis.cae.model;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;

import com.crivano.jbiz.ITag;
import com.crivano.jsync.IgnoreForSimilarity;
import com.crivano.juia.annotations.Detail;
import com.crivano.juia.annotations.DetailGroup;
import com.crivano.juia.annotations.Edit;
import com.crivano.juia.annotations.EditKindEnum;
import com.crivano.juia.annotations.FieldSet;
import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.Global.Gender;
import com.crivano.juia.annotations.MenuCreate;
import com.crivano.juia.annotations.MenuList;
import com.crivano.juia.annotations.Search;
import com.crivano.juia.annotations.ShowGroup;
import com.crivano.juia.biz.IJuiaAction;

import br.jus.trf2.temis.cae.model.enm.CaeEspecieDeAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeModalidadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeOrgaoEnum;
import br.jus.trf2.temis.cae.model.enm.CaeParticipacaoEnum;
import br.jus.trf2.temis.cae.model.enm.CaeSituacaoDaInscricaoNaAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeTipoDeAtividadeEnum;
import br.jus.trf2.temis.cae.model.enm.CaeTurnoEnum;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeAprovacao;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeDeferimento;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeIndeferimento;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeInscricao;
import br.jus.trf2.temis.cae.model.event.CaeEventoDeAtividadeReprovacao;
import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.core.Etiqueta;
import br.jus.trf2.temis.core.Evento;
import br.jus.trf2.temis.core.action.Auditar;
import br.jus.trf2.temis.core.action.Editar;
import br.jus.trf2.temis.core.enm.MarcadorEnum;
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
@MenuCreate
@Global(singular = "Atividade", plural = "Atividades", gender = Gender.SHE, codePrefix = "AT", deletable = true)
public class CaeAtividade extends Entidade {

	@Entity
	@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
	@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, length = 64)
	@Getter
	@Setter
	@FieldNameConstants
	@Global(singular = "Evento de Atividade", plural = "Eventos de Atividades", gender = Gender.HE, codePrefix = "EA")
	public abstract static class CaeEventoDeAtividade extends Evento<CaeAtividade, CaeEventoDeAtividade> {
		@NoSerialization
		@ManyToOne(fetch = FetchType.LAZY)
		private CaeAtividade atividade;

		public <T extends CaeEventoDeAtividade> T getReferenciaDaClasse(Class<T> clazz) {
			for (T p : getAtividade().getEventos(clazz)) {
				if (p.getReferente() == this)
					return p;
			}
			return null;
		}

		public <T extends CaeEventoDeAtividade> boolean temReferenciaDaClasse(Class<T> clazz) {
			return getReferenciaDaClasse(clazz) != null;
		}

		public boolean isDeferida() {
			return temReferenciaDaClasse(CaeEventoDeAtividadeDeferimento.class);
		}

		public boolean isIndeferida() {
			return temReferenciaDaClasse(CaeEventoDeAtividadeIndeferimento.class);
		}

		public boolean isAprovada() {
			return temReferenciaDaClasse(CaeEventoDeAtividadeAprovacao.class);
		}

		public boolean isReprovada() {
			return temReferenciaDaClasse(CaeEventoDeAtividadeReprovacao.class);
		}

		public CaeSituacaoDaInscricaoNaAtividadeEnum getSituacaoDaInscricao() {
			if (isReprovada())
				return CaeSituacaoDaInscricaoNaAtividadeEnum.REPROVADA;
			if (isAprovada())
				return CaeSituacaoDaInscricaoNaAtividadeEnum.APROVADA;
			if (isIndeferida())
				return CaeSituacaoDaInscricaoNaAtividadeEnum.INDEFERIDA;
			if (isDeferida())
				return CaeSituacaoDaInscricaoNaAtividadeEnum.DEFERIDA;
			return CaeSituacaoDaInscricaoNaAtividadeEnum.PENDENTE_DE_DEFERIMENTO;
		}
	}

	@IgnoreForSimilarity
	@OneToMany(mappedBy = CaeEventoDeAtividade.Fields.atividade, cascade = CascadeType.ALL)
	@OrderBy(Evento.Fields.dtIni)
	private SortedSet<CaeEventoDeAtividade> evento = new TreeSet<>();

	@Search
	@NotNull
	@Edit(caption = "Espécie", colM = 3)
	CaeEspecieDeAtividadeEnum especie;

	@Search
	@NotNull
	@Edit(caption = "Orgão", colM = 3)
	CaeOrgaoEnum orgao;

	@Search
	@NotNull
	@Edit(caption = "Tipo", colM = 3)
	CaeTipoDeAtividadeEnum tipo;

	@Search
	@Edit(caption = "Temática", colM = 3)
	@ManyToOne(fetch = FetchType.LAZY)
//	@NotNull
	CaeTematica tematica;

	@NotNull
	@Edit(caption = "Participações", colM = 3)
	CaeParticipacaoEnum participacao;

	@Search
	@ShowGroup(caption = "{{data.codigo + ' - ' + data.tema}}")
	@DetailGroup(caption = "Informações")
	@Detail
	@NotNull
	@Edit(caption = "Tema", colM = 6)
	String tema;

	@Search
	@NotNull
	@Detail
	@Edit(caption = "Número de Vagas", colM = 3)
	Integer vagas;

	@NotNull
	@Edit(caption = "Carga Horária CAE", colM = 3)
	Integer cargaHorariaCae;

	@NotNull
	@Edit(caption = "Carga Horária", colM = 3)
	Integer cargaHoraria;

	@NotNull
	@Edit(caption = "Carga Horária OAB", colM = 3)
	Integer cargaHorariaOab;

	@NotNull
	@Detail
	@Edit(caption = "Local", colM = 3)
	String local;

	@NotNull
	@Edit(caption = "Modalidade", colM = 3)
	CaeModalidadeEnum modalidade;

	@NotNull
	@Edit(colM = 3)
	CaeTurnoEnum turno;

	@NotNull
	@Edit(colM = 3)
	String hora;

	@Search
	@FieldSet(caption = "Datas")
	@NotNull
	@Detail
	@Edit(colM = 3)
	LocalDate dataDeInicio;

	@NotNull
	@Detail
	@Edit(colM = 3)
	LocalDate dataDeFim;

	@NotNull
	@Detail
	@Edit(colM = 3)
	LocalDate dataDeAberturaDasInscricoes;

	@NotNull
	@Detail
	@Edit(colM = 3)
	LocalDate dataDeEncerramentoDasInscricoes;

	@FieldSet(caption = "Informações Complementares")
	@NotNull
	@Detail
	@Edit(caption = "Público Alvo", kind = EditKindEnum.TEXTAREA, colM = 12)
	String publicoAlvo;

	@NotNull
	@Detail
	@Edit(caption = "Descrição", kind = EditKindEnum.TEXTAREA, colM = 12)
	String descricao;

	@NotNull
	@Detail
	@Edit(caption = "Observações", kind = EditKindEnum.TEXTAREA, colM = 12)
	String obs;

	@FieldSet(caption = "Informações de Controle")
	@Edit(caption = "Publicar Atividade", colM = 4)
	boolean publicarAtividade;
	@Edit(colM = 4)
	boolean ativarDetalhamento;
	@Edit(colM = 4)
	boolean cancelada;

	@Override
	public String getCode() {
		return getId().toString();
	}

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
		return getTitle();
	}

	@Override
	public String getTitle() {
		return getCodigo() + " - " + tema;
	}

	@Override
	public void addActions(SortedSet<IJuiaAction> set) {
		super.addActions(set);
		// set.add(new Editar<Processo>());
		set.add(new Editar());
		set.add(new Auditar());
		set.add(new CaeEventoDeAtividadeInscricao());
	}

	@Override
	public void addTags(SortedSet<ITag> set) {
		super.addTags(set);

		if (!isCancelada()) {
			set.add(Etiqueta.of(null, this, null, getPessoaTitular(), getLotacaoTitular(),
					MarcadorEnum.EM_ELABORACAO, this.getBegin(), this.getDataDeInicio().toDate()));
			set.add(Etiqueta.of(null, this, null, getPessoaTitular(), getLotacaoTitular(),
					MarcadorEnum.EM_CURSO, this.getDataDeInicio().toDate(), this.getDataDeFim().toDate()));
			if (isPendenteDeResultados()) {
				set.add(Etiqueta.of(null, this, null, getPessoaTitular(), getLotacaoTitular(),
						MarcadorEnum.PENDENTE_DE_RESULTADOS, this.getDataDeFim().toDate(), null));
			} else {
				set.add(Etiqueta.of(null, this, null, null, null,
						MarcadorEnum.CONCLUIDO, this.getDataDeFim().toDate(), null));
			}

			// Acrescenta marcadores para todas as pessoas inscritas. Os marcadores
			// indicarão a situação da inscrição, aprovação, reprovação, etc.
			SortedSet<CaeEventoDeAtividadeInscricao> inscrs = getEventosAtivos(CaeEventoDeAtividadeInscricao.class);
			for (CaeEventoDeAtividadeInscricao inscr : inscrs) {
				CaeSituacaoDaInscricaoNaAtividadeEnum situacaoDaInscricao = inscr.getSituacaoDaInscricao();
				CaeEventoDeAtividade referencia = inscr.getReferenciaDaClasse(situacaoDaInscricao.getClazz());
				set.add(Etiqueta.of(null, this, null, inscr.getPessoa(), null,
						situacaoDaInscricao.getMarcador(),
						referencia != null ? referencia.getBegin() : inscr.getBegin(), null));
			}
		} else {
			set.add(Etiqueta.of(null, this, null, null, null,
					MarcadorEnum.CANCELADO, null, null));
		}
	}

	private boolean isPendenteDeResultados() {
		SortedSet<CaeEventoDeAtividadeInscricao> inscrs = getEventosAtivos(CaeEventoDeAtividadeInscricao.class);
		for (CaeEventoDeAtividadeInscricao inscr : inscrs) {
			switch (inscr.getSituacaoDaInscricao()) {
			case APROVADA:
			case REPROVADA:
			case INDEFERIDA:
				continue;
			default:
				return true;
			}
		}
		return false;
	}

	public <T extends CaeEventoDeAtividade> SortedSet<T> getEventos(Class<T> clazz) {
		TreeSet<T> set = new TreeSet<T>();
		if (getEvento() == null || getEvento().size() == 0)
			return set;

		for (Evento e : getEvento())
			if (clazz.isAssignableFrom(e.getClass()))
				set.add((T) e);
		return set;
	}

}
