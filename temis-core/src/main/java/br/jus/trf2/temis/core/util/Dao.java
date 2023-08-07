package br.jus.trf2.temis.core.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.beanutils.PropertyUtils;

import com.crivano.jsync.Operation;
import com.crivano.jsync.OperatorWithHistory;
import com.crivano.jsync.Synchronizer;
import com.crivano.juia.annotations.Global;
import com.crivano.swaggerservlet.SwaggerUtils;

import br.gov.jfrj.siga.cp.util.MatriculaUtils;
import br.jus.trf2.temis.core.Arquivo;
import br.jus.trf2.temis.core.Entidade;
import br.jus.trf2.temis.core.Evento;
import br.jus.trf2.temis.core.IEntidade;
import br.jus.trf2.temis.crp.model.CrpConfiguracaoCache;
import br.jus.trf2.temis.crp.model.CrpIdentidade;
import br.jus.trf2.temis.crp.model.CrpServico;
import br.jus.trf2.temis.crp.model.Historico;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeConfiguracaoEnum;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeIdentidadeEnum;
import br.jus.trf2.temis.iam.model.Endereco;
import br.jus.trf2.temis.iam.model.Pessoa;
import br.jus.trf2.temis.iam.model.Unidade;
import lombok.NonNull;

@RequestScoped
public class Dao {
	private EntityManager em;

	@Inject
	private void setEm(EntityManager em) {
		this.em = em;
	}

	public static Dao getInstance() {
		return CDI.current().select(Dao.class).get();
	}

	public <T> List<T> listarTodos(Class<T> clazz, String orderBy) {
		CriteriaQuery<T> q = cb().createQuery(clazz);
		Root<T> c = q.from(clazz);
		q.select(c);
		if (orderBy != null) {
			q.orderBy(cb().asc(c.get(orderBy)));
		}
		return em.createQuery(q).getResultList();
	}

	public List listarDocumentosPorPessoaOuLotacao(Pessoa pessoa, Unidade unidade) {

		long tempoIni = System.nanoTime();
		Query query = em.createQuery("select e, t from Entidade e" + " inner join e.etiqueta t"
				+ " where (t.inicio is null or t.inicio < CURRENT_TIMESTAMP)"
				+ " and (t.termino is null or t.termino > CURRENT_TIMESTAMP)" + " and (t.pessoa.id = :pessoa or "
				+ " t.pessoa.id is null) and (t.unidade.id = :unidade or t.unidade is null)");

		if (pessoa != null)
			query.setParameter("pessoa", pessoa.getId());
		else
			query.setParameter("pessoa", null);
		if (unidade != null)
			query.setParameter("unidade", unidade.getId());
		else
			query.setParameter("unidade", null);

		List l = query.getResultList();
		long tempoTotal = System.nanoTime() - tempoIni;
		// System.out.println("consultarPorFiltroOtimizado: " + tempoTotal
		// / 1000000 + " ms -> " + query + ", resultado: " + l);
		return l;
	}

	public Date consultarDataEHoraDoServidor() {
		return new Date();
	}

	public void remove(Object entity) {
		em.remove(entity);
	}

	protected boolean isVersionable(Class<?> clazz) {
		Global global = clazz.getAnnotation(Global.class);
		if (global == null)
			return false;
		return global.versionable();
	}

	protected boolean isEntity(Class<?> clazz) {
		return Entidade.class.isAssignableFrom(clazz);
	}

	public void persist(Object entity) {
		em.persist(entity);
	}

	public void persistEntidade(IEntidade data) {
		try {
			Synchronizer sync = new Synchronizer();

			Long id = (Long) getIdentifier(data);
			Date dt = consultarDataEHoraDoServidor();
			List<Field> fields = ModeloUtils.getFieldList(data.getClass());

			// Se a entidade já existe
			if (id != null) {
				// Acrescenta a versão antiga na lista para sincronismo
				IEntidade oldData = em.find(data.getClass(), id);
				sync.addOld(oldData);

				// Varre todos os campos e localiza coleções de entidades
				for (Field fld : fields) {
					OneToMany o2m = fld.getAnnotation(OneToMany.class);

					// lista marcada com @OneToMany
					if (o2m != null && Utils.sorn(o2m.mappedBy()) != null) {
						Collection<Object> l = (Collection<Object>) fld.get(oldData);
						if (l != null) {
							int i = 0;
							for (Object oOld : l) {
								if (oOld instanceof IEntidade) {
									if (((IEntidade) oOld).getTermino() != null)
										continue;
									// Acrescenta as entidades antigas na lista velha para sincronismo.
									// Isso não parece fazer muito sentido, pois se a entidade é nova, imagino que
									// as listas de entidades subordinadas não devam vir preenchidas com entidades
									// existentes no banco.
									sync.addOld((IEntidade) oOld);
								}
							}
						}
					}
				}
			}

			// Acrescenta a versão antiga na lista para sincronismo
			sync.addNew(data);

			// Varre todos os campos
			for (Field fld : fields) {
				ManyToOne m2o = fld.getAnnotation(ManyToOne.class);
				OneToMany o2m = fld.getAnnotation(OneToMany.class);

				if (fld.getName().equals("lotacaoTitular"))
					System.out.println(fld.getName());

				// Campo marcado com @ManyToOne
				Object o = fld.get(data);
				if (o != null && m2o != null && o.getClass().getAnnotation(Entity.class) != null) {
					System.out.println(fld.getName());
					Object identifier = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(o);
					if (identifier != null) {
						// Carrega a entidade referenciada do banco, eliminando qualquer possível
						// alteração introduzida pela interface gráfica
						fld.set(data, em.find(o.getClass(), identifier));
					} else {
						// Garante que o campo está preenchido com null, em vez de
						// estar com um objeto sem nenhuma propriedade
						fld.set(data, null);
					}
				}

				// lista marcada com @OneToMany
				if (o2m != null && Utils.sorn(o2m.mappedBy()) != null) {
					Collection<Object> l = (Collection<Object>) o;
					if (l != null) {
						int i = 0;
						for (Object oOriginal : l) {
							if (!(oOriginal instanceof Entidade))
								return;
							Entidade oOrig = (Entidade) oOriginal;
							// Atribui o campo de ordenação, começando por 1
							i++;
							Global juiaGlobal = oOrig.getClass().getAnnotation(Global.class);
							if (juiaGlobal != null && juiaGlobal.sortField().length() > 0)
								PropertyUtils.setProperty(oOrig, juiaGlobal.sortField(), i);
							// Faz o link reverso preenchendo o campo indicado por mappedBy()
							for (Field fldObj : ModeloUtils.getFieldList(oOrig.getClass())) {
								if (o2m.mappedBy().equals(fldObj.getName())) {
									fldObj.set(oOrig, data);
								}
							}
							if (oOrig.getTermino() != null)
								continue;
							sync.addNew(oOrig);
						}
					}
				}
			}
			sync.sync(new OperatorWithHistory<Entidade>() {
				@Override
				public void init(Operation<Entidade> opr) {
					Entidade novo = opr.getNew();
					Entidade antigo = opr.getOld();
					SwaggerUtils.log(Dao.class).info("inicializando: " + opr);
					if (novo != null) {
						novo.setId(null);
						novo.setInicio(dt);
					}
					if (antigo != null)
						antigo.setTermino(dt);
				}

				@Override
				public Entidade insert(Entidade novo) {
					SwaggerUtils.log(Dao.class).info("acrescentando: " + novo.toString());
					em.persist(novo);
					novo.setIdInicial(novo.getId());
					return novo;
				}

				@Override
				public Entidade remove(Entidade antigo) {
					SwaggerUtils.log(Dao.class).info("removendo: " + antigo.toString());
					// em.remove(antigo);
					return antigo;
				}

				@Override
				public Entidade update(Entidade antigo, Entidade novo) {
					SwaggerUtils.log(Dao.class).info("alterando: " + novo.toString());
					novo.setIdInicial(antigo.getIdInicial());

					// Remove a id para que seja criado um novo registro
//					novo.setId(null);

					// Transfere os eventos para essa nova entidade
					try {
						for (Field fld : ModeloUtils.getFieldList(antigo.getClass()))
							if (fld.getName().equals("evento")) {
								OneToMany o2m = fld.getAnnotation(OneToMany.class);
								if (o2m != null && Utils.sorn(o2m.mappedBy()) != null) {
									String mappedBy = o2m.mappedBy();
									for (Evento evt : antigo.getEvento()) {
										for (Field fld2 : ModeloUtils.getFieldList(evt.getClass()))
											if (fld2.getName().equals(mappedBy))
												fld2.set(evt, novo);
									}
								}
							}
					} catch (Exception e) {
						throw new RuntimeException("Não consegui transferir eventos", e);
					}
					em.persist(novo);
					return novo;
				}
			});
		} catch (

		Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void ajustarListas(Entidade data, Date dt) {
		try {
			for (Field fld : ModeloUtils.getFieldList(data.getClass())) {
				OneToMany o2m = fld.getAnnotation(OneToMany.class);
				if (o2m == null || Utils.sorn(o2m.mappedBy()) == null)
					continue;

				// lista marcada com @OneToMany
				Collection<Entidade> l = (Collection<Entidade>) fld.get(data);
				if (l != null) {
					int i = 0;
					for (Entidade oOrig : l) {
						Long id = (Long) getIdentifier(data);
						if (id != null) {
							Entidade oldData = em.find(oOrig.getClass(), id);
							persist(oldData, data, dt);
						}
						data.setInicio(dt);
						// Remove a ID da entidade na lista para forçar a regravação
						if (oOrig instanceof Entidade)
							((Entidade) oOrig).setId(null);
						// Atribui o campo de ordenação, começando por 1
						i++;
						Global juiaGlobal = oOrig.getClass().getAnnotation(Global.class);
						if (juiaGlobal != null && juiaGlobal.sortField().length() > 0)
							PropertyUtils.setProperty(oOrig, juiaGlobal.sortField(), i);
						// Faz o link reverso preenchendo o campo indicado por mappedBy()
						for (Field fldObj : ModeloUtils.getFieldList(oOrig.getClass())) {
							if (o2m.mappedBy().equals(fldObj.getName())) {
								fldObj.set(oOrig, data);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void persist(Entidade oldData, Entidade newData, Date dt) {
		oldData.setTermino(dt);
		em.persist(oldData);
		newData.setInicio(dt);
		newData.setIdInicial(oldData.getIdInicial());
		newData.setId(null);
	}

	public void flush() {
		em.flush();
	}

	public Long getIdentifier(Object entity) {
		return (Long) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
	}

	public List<Pessoa> listarPessoaPorCpfCnpj(@NonNull String cpfcnpj) {
		TypedQuery<Pessoa> q = em.createQuery("from Pessoa where " + field(Pessoa.Fields.cpfcnpj, "="), Pessoa.class);
		q.setParameter(Pessoa.Fields.cpfcnpj, cpfcnpj);
		return q.getResultList();
	}

	public List<Endereco> listarEndercoPorIdPessoa(@NonNull Pessoa pessoa) {
		TypedQuery<Endereco> q = em.createQuery("from Endereco e where " + field(Endereco.Fields.pessoa, "="),
				Endereco.class);
		q.setParameter(Endereco.Fields.pessoa, pessoa);
		return q.getResultList();
	}

	private String field(String fieldName, String operand) {
		return fieldName + " " + operand + " :" + fieldName;
	}

	public Arquivo find(Class<Arquivo> clazz, Long key) {
		return em.find(clazz, key);
	}

//	public List<Jurisprudencia> listarJurisprudenciaPorNumeroDeProcesso(@NonNull String numeroProcesso) {
//		TypedQuery<Jurisprudencia> q = em.createQuery(
//				"from Jurisprudencia where " + field(Jurisprudencia.Fields.numeroProcesso, "="), Jurisprudencia.class);
//		q.setParameter(Jurisprudencia.Fields.numeroProcesso, numeroProcesso);
//		return q.getResultList();
//	}

//	public List<Norma> listarNormaPorIdentificador(Norma normaPai, DispositivoDeNormaEnum dispositivo, String sigla) {
//		TypedQuery<Norma> q = em.createQuery(
//				"from Norma where " + (normaPai != null ? field(Norma.Fields.normaPai, "=") + " and " : "")
//						+ field(Norma.Fields.dispositivo, "=") + " and " + field(Norma.Fields.identificador, "="),
//				Norma.class);
//		if (normaPai != null)
//			q.setParameter(Norma.Fields.normaPai, normaPai);
//		q.setParameter(Norma.Fields.dispositivo, dispositivo);
//		q.setParameter(Norma.Fields.identificador, Norma.buildFragmentoDeIdentificador(sigla));
//		return q.getResultList();
//	}

	public <T> T obter(Object id, final Class<T> clazz) {
		return em.find(clazz, id);
	}

	public <T extends Historico> T obterAtual(Object id, final Class<T> clazz) {
		T o = obter(id, clazz);
		return obterAtual(o);
	}

	public <T extends Historico> T obterInicial(Object id, final Class<T> clazz) {
		return em.find(clazz, id);
	}

	public <T extends Historico> T obterAtual(final T u) {
		return selecionarVersao(u, true);
	}

	public <T extends Historico> T obterInicial(final T u) {
		return selecionarVersao(u, false);
	}

	private <T extends Historico> T selecionarVersao(final T u, boolean atual) {
		if (u.getTermino() == null)
			return u;
		CriteriaBuilder builder = em.getCriteriaBuilder();

		Class<? extends Historico> clazz = (Class<? extends Historico>) Utils.getClassForHibernateObject(u);

		CriteriaQuery query = builder.createQuery(clazz);

		Subquery<Date> sub = query.subquery(Date.class);
		Root subFrom = sub.from(clazz);
		if (atual)
			sub.select(builder.greatest(subFrom.<Date>get("inicio")));
		else // inicial
			sub.select(builder.least(subFrom.<Date>get("inicio")));
		sub.where(builder.equal(subFrom.get("idInicial"), u.getIdInicial()));

		Root from = query.from(clazz);
		CriteriaQuery select = query.select(from);
		select.where(builder.and(builder.equal(from.get("idInicial"), u.getIdInicial()),
				builder.equal(from.get("inicio"), sub)));

		TypedQuery typedQuery = em.createQuery(query);
		return (T) typedQuery.getSingleResult();
	}

	protected CriteriaBuilder cb() {
		return em.getCriteriaBuilder();
	}

	//
	// CONFIGURAÇÕES E SERVIÇOS
	//

	public List<CrpConfiguracaoCache> consultarCacheDeConfiguracoesAtivas() {
		Query query = em.createNamedQuery("consultarCacheDeConfiguracoesAtivas");
		query.setParameter("tipos", CrpTipoDeConfiguracaoEnum.getValoresMapeados());
		return query.getResultList();
	}

	public Date consultarDataUltimaAtualizacao() {
		Query sql = (Query) em.createNamedQuery("consultarDataUltimaAtualizacao");

		List result = sql.getResultList();
		Date dtIni = (Date) ((Object[]) (result.get(0)))[0];
		Date dtFim = (Date) ((Object[]) (result.get(0)))[1];
		return DataUtils.max(dtIni, dtFim);
	}

	public List<CrpConfiguracaoCache> consultarConfiguracoesDesde(Date desde) {
		CriteriaQuery<CrpConfiguracaoCache> q = cb().createQuery(CrpConfiguracaoCache.class);
		Root<CrpConfiguracaoCache> c = q.from(CrpConfiguracaoCache.class);
		q.select(c);
		if (desde != null) {
			Predicate confsAtivas = cb().greaterThan(c.<Date>get("hisDtIni"), desde);
			Predicate confsInativas = cb().greaterThan(c.<Date>get("hisDtFim"), desde);
			Predicate tipos = c.get("cpTipoConfiguracao").in(CrpTipoDeConfiguracaoEnum.getValoresMapeados());
			q.where(cb().and(tipos, cb().or(confsAtivas, confsInativas)));
		}
		return em.createQuery(q).getResultList();
	}

	private static Map<String, CrpServico> cacheServicos = null;

	@SuppressWarnings("unchecked")
	public CrpServico consultarCpServicoPorChave(String chave) {
		StringBuilder sb = new StringBuilder(50);
		boolean supress = false;
		boolean separator = false;
		for (int i = 0; i < chave.length(); i++) {
			final char ch = chave.charAt(i);
			if (ch == ';') {
				supress = false;
				separator = true;
				continue;
			}
			if (ch == ':') {
				supress = true;
				continue;
			}
			if (!supress) {
				if (separator) {
					sb.append('-');
					separator = false;
				}
				sb.append(ch);
			}
		}
		String sigla = sb.toString();

		if (cacheServicos == null)
			inicializarCacheDeServicos();
		return cacheServicos.get(sigla);
	}

	public void inicializarCacheDeServicos() {
		synchronized (Dao.class) {
			cacheServicos = new TreeMap<>();
			List<CrpServico> l = listarTodos(CrpServico.class, "sigla");
			for (CrpServico s : l) {
				cacheServicos.put(s.getSigla(), s);
			}
		}
	}

	public CrpServico acrescentarServico(CrpServico srv) {
		synchronized (Dao.class) {
			em.persist(srv);
			cacheServicos.put(srv.getSigla(), srv);
			return srv;
		}
	}

	//
	// Identidade
	//

	@SuppressWarnings("unchecked")
	public CrpIdentidade consultaIdentidadesCadastrante(final String nmUsuario, boolean fAtiva) {
		final Query qry = em
				.createNamedQuery(fAtiva ? "consultarIdentidadeCadastranteAtiva" : "consultarIdentidadeCadastrante");
		if (Pattern.matches("\\d+", nmUsuario)) {
			qry.setParameter("cpf", Long.valueOf(nmUsuario));
			qry.setParameter("nmUsuario", null);
			qry.setParameter("sesbPessoa", null);
		} else {
			qry.setParameter("nmUsuario", nmUsuario);
			qry.setParameter("sesbPessoa", MatriculaUtils.getSiglaDoOrgaoDaMatricula(nmUsuario));
			qry.setParameter("cpf", null);
		}

		/* Constantes para Evitar Parse Oracle */
		qry.setParameter("cpfZero", 0L);
		qry.setParameter("sfp1", "1");
		qry.setParameter("sfp2", "2");
		qry.setParameter("sfp4", "4");
		qry.setParameter("sfp11", "11");
		qry.setParameter("sfp12", "12");
		qry.setParameter("sfp22", "22");
		qry.setParameter("sfp31", "31");
		qry.setParameter("sfp36", "36");
		qry.setParameter("sfp38", "38");

		List<CrpTipoDeIdentidadeEnum> listaTipo = new ArrayList<>();
		listaTipo.add(CrpTipoDeIdentidadeEnum.FORMULARIO);
		listaTipo.add(CrpTipoDeIdentidadeEnum.CERTIFICADO);

		qry.setParameter("listaTipo", listaTipo);

		// Cache was disabled because it would interfere with the
		// "change password" action.
//			qry.setHint("org.hibernate.cacheable", true);
//			qry.setHint("org.hibernate.cacheRegion", CACHE_QUERY_SECONDS);
		final List<CrpIdentidade> lista = (List<CrpIdentidade>) qry.getResultList();
		if (lista.size() == 0)
			return null;
		return lista.get(0);
	}

}
