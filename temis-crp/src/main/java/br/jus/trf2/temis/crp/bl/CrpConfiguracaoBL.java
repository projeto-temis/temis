package br.jus.trf2.temis.crp.bl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;

import br.jus.trf2.temis.core.util.ContextoPersistencia;
import br.jus.trf2.temis.core.util.Dao;
import br.jus.trf2.temis.crp.model.CrpConfiguracaoCache;
import br.jus.trf2.temis.crp.model.CrpIdentidade;
import br.jus.trf2.temis.crp.model.CrpLotacao;
import br.jus.trf2.temis.crp.model.CrpPessoa;
import br.jus.trf2.temis.crp.model.CrpServico;
import br.jus.trf2.temis.crp.model.enm.CrpSituacaoDeConfiguracaoEnum;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeConfiguracaoEnum;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeConfiguracaoInterface;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeMovimentacaoInterface;
import br.jus.trf2.temis.crp.model.enm.CrpTipoDeServicoEnum;

@RequestScoped
public class CrpConfiguracaoBL {

	public static final long ID_USUARIO_ROOT = 1L;
	public static final long MATRICULA_USUARIO_ROOT = 99999L;
	public static final long CPF_ROOT = 11111111111L;
	public static final long ID_ORGAO_ROOT = 999999999L;
	public static final String SIGLA_ORGAO_ROOT = "ZZ";

	private final static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(CrpConfiguracaoBL.class);

	protected Date dtUltimaAtualizacaoCache = null;
	protected boolean cacheInicializado = false;

	protected Comparator<CrpConfiguracaoCache> comparator = new CrpConfiguracaoCacheComparator();

	protected HashMap<CrpTipoDeConfiguracaoInterface, TreeSet<CrpConfiguracaoCache>> hashListas = new HashMap<>();

	public static int PESSOA = 1;

	public static int LOTACAO = 2;

	public static int FUNCAO = 3;

	public static int ORGAO = 4;

	public static int CARGO = 5;

	public static int SERVICO = 6;

	public static int IDENTIDADE = 7;

	public static int TIPO_LOTACAO = 8;

	public static int GRUPO = 9;

	public static int COMPLEXO = 10;

	public static int PESSOA_OBJETO = 11;

	public static int LOTACAO_OBJETO = 12;

	public static int FUNCAO_OBJETO = 13;

	public static int ORGAO_OBJETO = 14;

	public static int CARGO_OBJETO = 15;

	public static int COMPLEXO_OBJETO = 16;

	public Comparator<CrpConfiguracaoCache> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<CrpConfiguracaoCache> comparator) {
		this.comparator = comparator;
	}

	public Dao dao() {
		return CDI.current().select(Dao.class).get();
	}

	public CrpConfiguracaoCache createNewConfiguracao() {
		return new CrpConfiguracaoCache();
	}

	public void reiniciarCache() {
		cacheInicializado = false;
		dtUltimaAtualizacaoCache = null;
		hashListas = new HashMap<>();
		inicializarCacheSeNecessario();
	}

	public synchronized void inicializarCacheSeNecessario() {
		if (cacheInicializado)
			return;
		long inicio = System.currentTimeMillis();
		List<CrpConfiguracaoCache> results = dao().consultarCacheDeConfiguracoesAtivas();

		long inicioLazy = System.currentTimeMillis();
//		evitarLazy(results);
		long fimLazy = System.currentTimeMillis();

		hashListas.clear();
		for (CrpConfiguracaoCache a : results) {
			atualizarDataDeAtualizacaoDoCache(a);
			// Verifica se existe o tipo da configuracao
			if (a.cpTipoConfiguracao == null)
				continue;
			CrpTipoDeConfiguracaoInterface idTpConfiguracao = a.cpTipoConfiguracao;
			TreeSet<CrpConfiguracaoCache> tree = hashListas.get(idTpConfiguracao);
			if (tree == null) {
				tree = new TreeSet<CrpConfiguracaoCache>(comparator);
				hashListas.put(idTpConfiguracao, tree);
			}
			tree.add(a);
		}
		if (hashListas.size() == 0 && results.size() > 0)
			throw new RuntimeException("Ocorreu um erro na inicialização do cache.");
		cacheInicializado = true;

		long fim = System.currentTimeMillis();

		Logger.getLogger("siga.conf.cache")
				.info("Cache de configurações inicializado via " + this.getClass().getSimpleName() + " em "
						+ (fim - inicio) + "ms, select: " + (inicioLazy - inicio) + "ms, lazy: "
						+ (fimLazy - inicioLazy) + "ms, tree: " + (fim - fimLazy) + "ms");

	}

	public HashMap<CrpTipoDeConfiguracaoInterface, TreeSet<CrpConfiguracaoCache>> getHashListas() {
		if (!cacheInicializado) {
			inicializarCacheSeNecessario();
		}

		return hashListas;
	}

	public TreeSet<CrpConfiguracaoCache> getListaPorTipo(CrpTipoDeConfiguracaoInterface idTipoConfig) {
		return getHashListas().get(idTipoConfig);
	}

	private void atualizarCache() {
		if (!cacheInicializado) {
			inicializarCacheSeNecessario();
			return;
		}
		Date dt = dao().consultarDataUltimaAtualizacao();

		if (dt != null && (dtUltimaAtualizacaoCache == null || dt.after(dtUltimaAtualizacaoCache))) {
			procederAtualizacaoDeCache(dt);
		}
	}

	private synchronized void procederAtualizacaoDeCache(Date dt) {
		if (dtUltimaAtualizacaoCache != null && !dt.after(dtUltimaAtualizacaoCache))
			return;

		// sfCpDao.evict(CpConfiguracao.class);

		List<CrpConfiguracaoCache> alteracoes = dao().consultarConfiguracoesDesde(dtUltimaAtualizacaoCache);

		Logger.getLogger("siga.conf.cache").fine("Numero de alteracoes no cache: " + alteracoes.size());
		if (alteracoes.size() > 0) {
			// evitarLazy(alteracoes);

			for (CrpConfiguracaoCache cpConfiguracao : alteracoes) {
				atualizarDataDeAtualizacaoDoCache(cpConfiguracao);
				CrpTipoDeConfiguracaoInterface idTpConf = cpConfiguracao.cpTipoConfiguracao;
				TreeSet<CrpConfiguracaoCache> tree = hashListas.get(idTpConf);
				if (tree == null) {
					tree = new TreeSet<CrpConfiguracaoCache>(comparator);
					hashListas.put(idTpConf, tree);
				}
				if (cpConfiguracao.ativaNaData(dt)) {
					int i = tree.size();
					removeById(tree, cpConfiguracao.idConfiguracao);
					tree.add(cpConfiguracao);
					if (tree.size() != i + 1)
						Logger.getLogger("siga.conf.cache")
								.fine("Configuração atualizada: " + cpConfiguracao.toString());
					else
						Logger.getLogger("siga.conf.cache")
								.fine("Configuração adicionada: " + cpConfiguracao.toString());
				} else {
					int i = tree.size();
					removeById(tree, cpConfiguracao.idConfiguracao);
					if (tree.size() != i - 1)
						Logger.getLogger("siga.conf.cache")
								.fine("Configuração previamente removida: " + cpConfiguracao.toString());
					else
						Logger.getLogger("siga.conf.cache").fine("Configuração removida: " + cpConfiguracao.toString());
				}
			}
		}

		dtUltimaAtualizacaoCache = dt;
	}

	public void atualizarDataDeAtualizacaoDoCache(CrpConfiguracaoCache cpConfiguracao) {
		if (cpConfiguracao.hisDtIni != null
				&& (dtUltimaAtualizacaoCache == null || cpConfiguracao.hisDtIni.after(dtUltimaAtualizacaoCache)))
			dtUltimaAtualizacaoCache = cpConfiguracao.hisDtIni;
		if (cpConfiguracao.hisDtFim != null && cpConfiguracao.hisDtFim.after(dtUltimaAtualizacaoCache))
			dtUltimaAtualizacaoCache = cpConfiguracao.hisDtFim;
	}

	private void removeById(TreeSet<CrpConfiguracaoCache> tree, long id) {
		List<CrpConfiguracaoCache> found = new ArrayList<>();
		for (CrpConfiguracaoCache cfg : tree)
			if (cfg.idConfiguracao == id)
				found.add(cfg);
		for (CrpConfiguracaoCache cfg : found)
			tree.remove(cfg);
	}

	/**
	 * Limpa o cache do hibernate. Como as configurações são mantidas em cache por
	 * motivo de performance, as alterações precisam ser atualizadas para que possam
	 * valer imediatamente.
	 * 
	 * @throws Exception
	 */
	public void limparCacheSeNecessario() throws Exception {
		atualizarCache();
	}

	/**
	 * 
	 * Obtém a configuração a partir de um filtro, como uma consulta comum a uma
	 * entidade. O parâmetro atributoDesconsideradoFiltro deve-se ao seguinte: para
	 * se escolher a configuração a ser retornada do bando, são consideradas na base
	 * as configurações que não possuam algum campo preenchido que nulo no filtro, a
	 * não ser que esse atributo tenha sido passado através desse parãmetro. Se
	 * nenhuma lista de configurações for informada, busca todas as configurações
	 * para o TipoDeConfiguracao constante no filtro.
	 * 
	 * @param cpConfiguracaoFiltro
	 * @param atributoDesconsideradoFiltro
	 * @param lista
	 * @return
	 * @throws Exception
	 */
	public CrpConfiguracaoCache buscaConfiguracao(CrpConfiguracaoCache filtroConfiguracaoCache,
			int atributoDesconsideradoFiltro[], Date dtEvn) {
		deduzFiltro(filtroConfiguracaoCache);

		Set<Integer> atributosDesconsiderados = new LinkedHashSet<Integer>();
		for (int i = 0; i < atributoDesconsideradoFiltro.length; i++) {
			atributosDesconsiderados.add(atributoDesconsideradoFiltro[i]);
		}

//		SortedSet<CpPerfil> perfis = null;
//		if (cpConfiguracaoFiltro.isBuscarPorPerfis()
//				|| (cpConfiguracaoFiltro.getCpTipoConfiguracao() == CpTipoDeConfiguracao.UTILIZAR_SERVICO)) {
//			perfis = consultarPerfisPorPessoaELotacao(cpConfiguracaoFiltro.getDpPessoa(),
//					cpConfiguracaoFiltro.getLotacao(), dtEvn);
//
//			// Quando o filtro especifica um perfil, ou seja, estamos tentando
//			// avaliar as permissões de um determinado perfil, ele e todos os
//			// seus pais devem ser inseridos na lista de perfis
//			if (cpConfiguracaoFiltro.getCpGrupo() != null) {
//				perfis = new TreeSet<CpPerfil>();
//				Object g = cpConfiguracaoFiltro.getCpGrupo();
//				while (true) {
//					if (g instanceof HibernateProxy) {
//						g = ((HibernateProxy) g).getHibernateLazyInitializer().getImplementation();
//					}
//					if (g instanceof CpPerfil) {
//						perfis.add((CpPerfil) g);
//						g = ((CpGrupo) g).getCpGrupoPai();
//						if (g == null)
//							break;
//					} else
//						break;
//				}
//				if (perfis.size() == 0)
//					perfis = null;
//			}
//		}

		TreeSet<CrpConfiguracaoCache> lista = null;
		if (filtroConfiguracaoCache.cpTipoConfiguracao != null)
			lista = getListaPorTipo(filtroConfiguracaoCache.cpTipoConfiguracao);
		if (lista == null)
			return null;

		for (CrpConfiguracaoCache cpConfiguracao : lista) {
			if ((!cpConfiguracao.ativaNaData(dtEvn)) || (cpConfiguracao.situacao != null
					&& cpConfiguracao.situacao == CrpSituacaoDeConfiguracaoEnum.IGNORAR_CONFIGURACAO_ANTERIOR)
					|| !atendeExigencias(filtroConfiguracaoCache, atributosDesconsiderados, cpConfiguracao
//							, perfis
					))
				continue;
			return cpConfiguracao;
		}
		return null;
	}

//	public SortedSet<CpPerfil> consultarPerfisPorPessoaELotacao(DpPessoa pessoa, DpLotacao lotacao, Date dtEvn) {
//		if (pessoa == null && lotacao == null)
//			return null;
//
//		if (pessoa != null) {
//			Object p = pessoa;
//			if (p instanceof HibernateProxy) {
//				pessoa = (DpPessoa) ((HibernateProxy) p).getHibernateLazyInitializer().getImplementation();
//			}
//		}
//
//		if (lotacao != null) {
//			Object l = lotacao;
//			if (l instanceof HibernateProxy) {
//				lotacao = (DpLotacao) ((HibernateProxy) l).getHibernateLazyInitializer().getImplementation();
//			}
//		}
//
//		TreeSet<CpConfiguracaoCache> lista = getListaPorTipo(CpTipoDeConfiguracao.PERTENCER);
//
//		SortedSet<CpPerfil> perfis = new TreeSet<CpPerfil>();
//		if (lista != null && pessoa != null) {
//			for (CpConfiguracaoCache cfg : lista) {
//				if (cfg.cpGrupo == 0 || cfg.cpPerfil == null || !cfg.ativaNaData(dtEvn))
//					continue;
//
//				if (cfg.dpPessoa != 0 && cfg.dpPessoa != pessoa.getIdInicial())
//					continue;
//
//				if (cfg.cargo != 0 && cfg.cargo != pessoa.getCargo().getIdInicial())
//					continue;
//
//				if (cfg.funcaoConfianca != 0 && (pessoa.getFuncaoConfianca() != null && cfg.funcaoConfianca != pessoa.getFuncaoConfianca().getIdInicial()))
//					continue;
//
//				if (cfg.lotacao != 0 && cfg.lotacao != lotacao.getIdInicial())
//					continue;
//
//				if (cfg.orgaoUsuario != 0 && cfg.lotacao != lotacao.getIdInicial())
//					continue;
//
//				Object g = dao().consultar(cfg.cpGrupo,CpPerfil.class,false);
//				
//				if (g instanceof CpPerfil && cfg.dscFormula != null) {
//					Map<String, DpPessoa> pessoaMap = new HashMap<String, DpPessoa>();
//					pessoaMap.put("pessoa", pessoa);
//					if (!(Boolean) MVEL.eval(cfg.dscFormula, pessoaMap)) {
//						continue;
//					}
//				}
//
//				CpPerfil perfil = cfg.cpPerfil;
//				while (perfil != null) {
//					perfis.add(perfil);
//					CpGrupo grp = cfg.cpPerfil.getCpGrupoPai();
//					if (!(grp instanceof CpPerfil))
//						break;
//					perfil = (CpPerfil) grp; 
//					if (perfil != null && perfil instanceof HibernateProxy) 
//						perfil = (CpPerfil) ((HibernateProxy) perfil).getHibernateLazyInitializer().getImplementation();
//				}
//			}
//		}
//		return perfis;
//	}

	/**
	 * 
	 * Obtém a situação a partir de um filtro, como uma consulta comum a uma
	 * entidade. O parâmetro atributoDesconsideradoFiltro deve-se ao seguinte: para
	 * se escolher a situação a ser retornada, são consideradas na base as
	 * configurações que não possuam algum campo preenchido que nulo no filtro, a
	 * não ser que esse atributo tenha sido passado através desse parãmetro. Caso
	 * nenhuma configuração seja selecionada, a situação default do tipo de
	 * configuração será retornada.
	 * 
	 * @param cpConfiguracaoFiltro
	 * @param atributoDesconsideradoFiltro
	 * @return
	 * @throws Exception
	 */
//	public CpSituacaoDeConfiguracaoEnum buscaSituacao(CpConfiguracao cpConfiguracaoFiltro, int atributoDesconsideradoFiltro[],
//			TreeSet<CpConfiguracao> lista) throws Exception {
//		CpConfiguracaoCache cfg = buscaConfiguracao(cpConfiguracaoFiltro, atributoDesconsideradoFiltro, null);
//		if (cfg != null) {
//			return cfg.situacao;
//		}
//		
//		return cpConfiguracaoFiltro.getCpTipoConfiguracao().getSituacaoDefault();
//	}

	/**
	 * @param cfgFiltro
	 * @param atributosDesconsiderados
	 * @param cfg
	 * @param perfis
	 */
	public boolean atendeExigencias(CrpConfiguracaoCache cfgFiltro, Set<Integer> atributosDesconsiderados,
			CrpConfiguracaoCache cfg
//			, SortedSet<CpPerfil> perfis
	) {
		if (cfgFiltro == null)
			cfgFiltro = new CrpConfiguracaoCache();

		if (desigual(cfg.cpServico, cfgFiltro.cpServico, atributosDesconsiderados, SERVICO))
			return false;

//		if (cfg.cpGrupo != 0 && (cfgFiltro.cpGrupo != 0 && cfg.cpGrupo != cfgFiltro.cpGrupo
//				|| ((cfgFiltro.cpGrupo == 0) && !atributosDesconsiderados.contains(GRUPO))
//						&& (perfis == null || !perfisContemGrupo(cfg, perfis))))
//			return false;

		if (desigual(cfg.cpIdentidade, cfgFiltro.cpIdentidade, atributosDesconsiderados, IDENTIDADE))
			return false;

		if (desigual(cfg.dpPessoa, cfgFiltro.dpPessoa, atributosDesconsiderados, PESSOA))
			return false;

		if (desigual(cfg.lotacao, cfgFiltro.lotacao, atributosDesconsiderados, LOTACAO))
			return false;

		if (desigual(cfg.complexo, cfgFiltro.complexo, atributosDesconsiderados, COMPLEXO))
			return false;

		if (desigual(cfg.funcaoConfianca, cfgFiltro.funcaoConfianca, atributosDesconsiderados, FUNCAO))
			return false;

		if (desigual(cfg.orgaoUsuario, cfgFiltro.orgaoUsuario, atributosDesconsiderados, ORGAO))
			return false;

		if (desigual(cfg.cargo, cfgFiltro.cargo, atributosDesconsiderados, CARGO))
			return false;

		if (desigual(cfg.cpTipoLotacao, cfgFiltro.cpTipoLotacao, atributosDesconsiderados, TIPO_LOTACAO))
			return false;

		if (desigual(cfg.pessoaObjeto, cfgFiltro.pessoaObjeto, atributosDesconsiderados, PESSOA_OBJETO))
			return false;

		if (desigual(cfg.lotacaoObjeto, cfgFiltro.lotacaoObjeto, atributosDesconsiderados, LOTACAO_OBJETO))
			return false;

		if (desigual(cfg.complexoObjeto, cfgFiltro.complexoObjeto, atributosDesconsiderados, COMPLEXO_OBJETO))
			return false;

		if (desigual(cfg.funcaoConfiancaObjeto, cfgFiltro.funcaoConfiancaObjeto, atributosDesconsiderados,
				FUNCAO_OBJETO))
			return false;

		if (desigual(cfg.orgaoObjeto, cfgFiltro.orgaoObjeto, atributosDesconsiderados, ORGAO_OBJETO))
			return false;

		if (desigual(cfg.cargoObjeto, cfgFiltro.cargoObjeto, atributosDesconsiderados, CARGO_OBJETO))
			return false;

		return true;
	}

	protected boolean desigual(long cfg, long filtro, Set<Integer> atributosDesconsiderados, int atributo) {
		return cfg != 0
				&& ((filtro != 0 && cfg != filtro) || ((filtro == 0) && !atributosDesconsiderados.contains(atributo)));
	}

	protected boolean desigual(CrpTipoDeMovimentacaoInterface cfg, CrpTipoDeMovimentacaoInterface filtro,
			Set<Integer> atributosDesconsiderados, int atributo) {
		return cfg != null
				&& ((filtro != null && cfg != filtro)
						|| ((filtro == null) && !atributosDesconsiderados.contains(atributo)));
	}

	/**
	 * Verifica se a configuracao refere-se a um perfil ao qual a pessoa/lotacao
	 * pertence
	 * 
	 * @param cfg    - A configuração a ser verificada
	 * @param perfis - os perfis da pessoa/lotacao
	 * @return
	 */
//	private boolean perfisContemGrupo(CpConfiguracaoCache cfg, SortedSet<CpPerfil> perfis) {
//		for (CpPerfil cpPerfil : perfis) {
//			if (cpPerfil.getIdInicial() == cfg.cpGrupo) {
//				return true;
//			}
//		}
//
//		return false;
//	}

	/**
	 * 
	 * Método com implementação completa, chamado pelas outras sobrecargas
	 * 
	 * @param cpTpDoc
	 * @param cpFormaDoc
	 * @param cpMod
	 * @param cpClassificacao
	 * @param cpVia
	 * @param cpTpMov
	 * @param cargo
	 * @param cpOrgaoUsu
	 * @param dpFuncaoConfianca
	 * @param dpLotacao
	 * @param dpPessoa
	 * @param nivelAcesso
	 * @param idTpConf
	 * @throws Exception
	 */
	public boolean podePorConfiguracao(long cpOrgaoUsu, long dpLotacao, long cargo,
			long dpFuncaoConfianca, long dpPessoa, long cpServico, long cpIdentidade,
			long cpGrupo, long cpTpLotacao, CrpTipoDeConfiguracaoInterface idTpConf) throws Exception {
		try {
			CrpSituacaoDeConfiguracaoEnum situacao = situacaoPorConfiguracao(cpOrgaoUsu, dpLotacao, cargo,
					dpFuncaoConfianca, dpPessoa, cpServico, cpIdentidade,
					cpGrupo, cpTpLotacao, idTpConf);
			return situacaoPermissiva(situacao);
		} catch (Exception ex) {
			log.error(ex);
			ex.printStackTrace();
			throw ex;
		}
	}

	public CrpSituacaoDeConfiguracaoEnum situacaoPorConfiguracao(long cpOrgaoUsu, long dpLotacao, long cargo,
			long dpFuncaoConfianca, long dpPessoa, long cpServico, long cpIdentidade,
			long cpGrupo, long cpTpLotacao, CrpTipoDeConfiguracaoInterface idTpConf) throws Exception {
		try {
			CrpConfiguracaoCache cfg = buscaConfiguracao(cpOrgaoUsu, dpLotacao, cargo,
					dpFuncaoConfianca, dpPessoa, cpServico, cpIdentidade,
					cpGrupo, cpTpLotacao, idTpConf);
			CrpSituacaoDeConfiguracaoEnum situacao = null;
			if (cfg != null)
				situacao = cfg.situacao;
			else if (idTpConf != null)
				situacao = idTpConf.getSituacaoDefault();
			return situacao;
		} catch (Exception ex) {
			log.error(ex);
			ex.printStackTrace();
			throw ex;
		}
	}

	public CrpConfiguracaoCache buscaConfiguracao(long cpOrgaoUsu, long dpLotacao, long cargo,
			long dpFuncaoConfianca, long dpPessoa, long cpServico, long cpIdentidade,
			long cpGrupo, long cpTpLotacao, CrpTipoDeConfiguracaoInterface idTpConf) throws Exception {
		try {
			CrpConfiguracaoCache cfgFiltro = createNewConfiguracao();

			cfgFiltro.cargo = cargo;
			cfgFiltro.orgaoUsuario = cpOrgaoUsu;
			cfgFiltro.funcaoConfianca = dpFuncaoConfianca;
			cfgFiltro.lotacao = dpLotacao;
			cfgFiltro.dpPessoa = dpPessoa;
			cfgFiltro.cpServico = cpServico;
			cfgFiltro.cpIdentidade = cpIdentidade;
//			cfgFiltro.setCpGrupo(cpGrupo);
			cfgFiltro.cpTipoLotacao = cpTpLotacao;

			cfgFiltro.cpTipoConfiguracao = idTpConf;

			return buscaConfiguracao(cfgFiltro, new int[] { 0 }, null);
		} catch (Exception ex) {
			log.error(ex);
			ex.printStackTrace();
			throw ex;
		}
	}

	public static boolean situacaoPermissiva(CrpSituacaoDeConfiguracaoEnum situacao) {
		if (situacao != null && (situacao == CrpSituacaoDeConfiguracaoEnum.PODE
				|| situacao == CrpSituacaoDeConfiguracaoEnum.DEFAULT
				|| situacao == CrpSituacaoDeConfiguracaoEnum.OBRIGATORIO))
			return true;
		return false;
	}

	/**
	 * 
	 * Usado para se verificar se uma pessoa pode realizar uma determinada operação
	 * no documento
	 * 
	 * @param dpPessoa
	 * @param dpLotacao
	 * @param idTpConf
	 * @throws Exception
	 */
//	public boolean podePorConfiguracao(DpPessoa dpPessoa, DpLotacao dpLotacao, ITipoDeConfiguracao idTpConf) throws Exception {
//		return podePorConfiguracao(null, dpLotacao, null, null, dpPessoa, null, null, null, null, idTpConf);
//
//	}
//
//	public boolean podePorConfiguracao(DpPessoa dpPessoa, DpLotacao dpLotacao, CpServico cpServico, ITipoDeConfiguracao idTpConf)
//			throws Exception {
//		return podePorConfiguracao(null, dpLotacao, null, null, dpPessoa, cpServico, null, null, null, idTpConf);
//
//	}
//
//	public boolean podePorConfiguracao(DpPessoa dpPessoa, ITipoDeConfiguracao idTpConf) throws Exception {
//		return podePorConfiguracao(null, null, null, null, dpPessoa, null, null, null, null, idTpConf);
//	}
//
//	public boolean podePorConfiguracao(DpLotacao dpLotacao, ITipoDeConfiguracao idTpConf) throws Exception {
//		return podePorConfiguracao(null, dpLotacao, null, null, null, null, null, null, null, idTpConf);
//	}
//
//	public boolean podePorConfiguracao(CpIdentidade cpIdentidade, ITipoDeConfiguracao idTpConf) throws Exception {
//		return podePorConfiguracao(null, null, null, null, null, null, cpIdentidade, null, null, idTpConf);
//	}
//
//	public boolean podePorConfiguracao(DpPessoa dpPessoa, DpLotacao dpLotacao, CpGrupo cpGrupo, ITipoDeConfiguracao idTpConf)
//			throws Exception {
//		return podePorConfiguracao(null, dpLotacao, null, null, dpPessoa, null, null, cpGrupo, null, idTpConf);
//	}

	/**
	 * Infere configurações óbvias. Por exemplo, se for informada a pessoa, a
	 * lotação, órgão etc. já serão preenchidos automaticamente.
	 * 
	 * @param cpConfiguracao
	 */
	public void deduzFiltro(CrpConfiguracaoCache cpConfiguracao) {

		if (cpConfiguracao == null)
			return;

		if (cpConfiguracao.cpIdentidade != 0L) {
			if (cpConfiguracao.dpPessoa == 0L)
				cpConfiguracao.dpPessoa = dao().obter(cpConfiguracao.cpIdentidade, CrpIdentidade.class).getPessoa()
						.getIdInicial();
		}

		if (cpConfiguracao.dpPessoa != 0L) {
			CrpPessoa pessoa = dao().obterAtual(cpConfiguracao.dpPessoa, CrpPessoa.class);
			if (cpConfiguracao.lotacao == 0L && pessoa.getLotacao() != null)
				cpConfiguracao.lotacao = pessoa.getLotacao().getIdInicial();
			if (cpConfiguracao.cargo == 0L && pessoa.getCargo() != null)
				cpConfiguracao.cargo = pessoa.getCargo().getIdInicial();
			if (cpConfiguracao.funcaoConfianca == 0L && pessoa.getFuncaoConfianca() != null) {
				cpConfiguracao.funcaoConfianca = pessoa.getFuncaoConfianca().getIdInicial();
			}
		}

		if (cpConfiguracao.lotacao != 0L)
			if (cpConfiguracao.orgaoUsuario == 0L) {
				CrpLotacao lotacao = dao().obterAtual(cpConfiguracao.lotacao, CrpLotacao.class);
				if (lotacao != null) {
					if (lotacao.getOrgao() != null)
						cpConfiguracao.orgaoUsuario = lotacao.getOrgao().getIdInicial();
					if (lotacao.getTipo() != null)
						cpConfiguracao.cpTipoLotacao = lotacao.getTipo().getId();
				}
			}

		if (cpConfiguracao.pessoaObjeto != 0L) {
			CrpPessoa pessoaObjeto = dao().obterAtual(cpConfiguracao.pessoaObjeto, CrpPessoa.class);
			if (cpConfiguracao.lotacaoObjeto == 0L && pessoaObjeto.getLotacao() != null)
				cpConfiguracao.lotacaoObjeto = pessoaObjeto.getLotacao().getIdInicial();
			if (cpConfiguracao.cargoObjeto == 0L && pessoaObjeto.getCargo() != null)
				cpConfiguracao.cargoObjeto = pessoaObjeto.getCargo().getIdInicial();
			if (cpConfiguracao.funcaoConfiancaObjeto == 0L && pessoaObjeto.getFuncaoConfianca() != null)
				cpConfiguracao.funcaoConfiancaObjeto = pessoaObjeto.getFuncaoConfianca().getIdInicial();
		}

		if (cpConfiguracao.lotacaoObjeto != 0L)
			if (cpConfiguracao.orgaoObjeto == 0L) {
				CrpLotacao lotacaoObjeto = dao().obterAtual(cpConfiguracao.lotacaoObjeto, CrpLotacao.class);
				if (lotacaoObjeto.getOrgao() != null)
					cpConfiguracao.orgaoObjeto = lotacaoObjeto.getOrgao().getIdInicial();
			}

	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("static-access")
	public boolean podeUtilizarServicoPorConfiguracao(CrpPessoa titular, CrpLotacao lotaTitular, String servicoPath) {
		try {
			if (titular == null || lotaTitular == null)
				return false;

			CrpServico srv = null;
			CrpServico srvPai = null;
			CrpServico srvRecuperado = null;

			srvRecuperado = dao().consultarCpServicoPorChave(servicoPath);
			if (srvRecuperado == null) {
				// Constroi uma linha completa, tipo full path
				for (String s : servicoPath.split(";")) {
					String[] asParts = s.split(":"); // Separa a sigla da
														// descrição
					String sSigla = asParts[0];
					String sigla = srvPai != null ? srvPai.getSigla() + "-" + sSigla : sSigla;
					Long idPai = srvPai != null ? srvPai.getId() : null;

					if (srvPai != null)
						srvRecuperado = CrpServico.AR.findFirstBy("sigla = ?1 and pai.id = ?2", sigla, srvPai.getId());
					else
						srvRecuperado = CrpServico.AR.findFirstBy("sigla = ?1", sigla);

					if (srvRecuperado == null) {
						srv = new CrpServico();
						String sDesc = (asParts.length > 1 ? asParts[1] : "");
						srv.setDescr(sDesc);
						srv.setPai(srvPai);
						srv.setSigla(sigla);
						srv.setTipo(CrpTipoDeServicoEnum.SISTEMA);
						boolean upgraded = ContextoPersistencia.upgradeToTransactional();
						dao().acrescentarServico(srv);
						if (upgraded)
							ContextoPersistencia.flushTransactionAndDowngradeToNonTransactional();
						srvRecuperado = srv;
					}
					srvPai = srvRecuperado;
				}
			}
			return podePorConfiguracao(0L, lotaTitular.getIdInicial(), 0L, 0L, titular.getIdInicial(),
					srvRecuperado.getId(), 0L, 0L, 0L, CrpTipoDeConfiguracaoEnum.UTILIZAR_SERVICO);
		} catch (Exception e) {
			throw new RuntimeException("Não foi possível calcular acesso ao serviço " + servicoPath, e);
		}
	}

	/**
	 * Localiza as ConfiguracaoGrupoEmail pertencentes a um determinado grupo
	 * 
	 * @param CpGrupo p_grpGrupo - O grupo que deseja localizar.
	 * 
	 * @throws Exception
	 */
//	public ArrayList<ConfiguracaoGrupo> obterCfgGrupo(CpGrupo grp) {
//
//		ArrayList<ConfiguracaoGrupo> aCfgGrp = new ArrayList<ConfiguracaoGrupo>();
//		ConfiguracaoGrupoFabrica fabrica = new ConfiguracaoGrupoFabrica();
//		try {
//			TreeSet<CpConfiguracaoCache> l = Cp.getInstance().getConf()
//					.getListaPorTipo(CpTipoDeConfiguracao.PERTENCER);
//			if (l != null) {
//				for (CpConfiguracaoCache cfg : l) {
//					if (cfg.cpGrupo == 0 || cfg.cpGrupo != grp.getIdInicial() || cfg.hisDtFim != null)
//						continue;
//					CpConfiguracao c = dao().consultar(cfg.idConfiguracao, CpConfiguracao.class, false);
//					ConfiguracaoGrupo cfgGrp = fabrica.getInstance(c);
//					aCfgGrp.add(cfgGrp);
//				}
//			}
//		} catch (Exception e) {
//			throw new AplicacaoException("Erro obtendo configurações", 0, e);
//		}
//		return aCfgGrp;
//	}

	/**
	 * Retorna as pessoas que podem acessar o grupos de segurança da lotação
	 * 
	 * @param lot
	 * @return
	 */
//	public Set<DpPessoa> getPessoasGrupoSegManual(DpLotacao lot) {
//
//		Set<DpPessoa> resultado = new HashSet<DpPessoa>();
//		try {
//			limparCacheSeNecessario();
//			Set<CpConfiguracaoCache> configs = Cp.getInstance().getConf()
//					.getListaPorTipo(CpTipoDeConfiguracao.UTILIZAR_SERVICO_OUTRA_LOTACAO);
//			if (configs != null) {
//				for (CpConfiguracaoCache c : configs) {
//					DpPessoa pesAtual = CpDao.getInstance().consultarPorIdInicial(c.dpPessoa);
//					if (pesAtual != null) {
//						if (c.dpPessoa == pesAtual.getIdInicial()) {
//							if (c.hisDtFim == null && pesAtual.getDataFim() == null && c.lotacao == lot.getIdInicial()) {
//								resultado.add(pesAtual);
//							}
//						}
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//		return resultado;
//
//	}

//	public Set<DpLotacao> getLotacoesGrupoSegManual(DpPessoa pes) {
//		Set<DpLotacao> resultado = new HashSet<DpLotacao>();
//		try {
//			limparCacheSeNecessario();
//			Set<CpConfiguracaoCache> configs = Cp.getInstance().getConf()
//					.getListaPorTipo(CpTipoDeConfiguracao.UTILIZAR_SERVICO_OUTRA_LOTACAO);
//			for (CpConfiguracaoCache c : configs) {
//				DpLotacao lotacaoAtual = CpDao.getInstance().consultarPorIdInicial(DpLotacao.class, c.lotacao);
//				if (lotacaoAtual != null) {
//					// System.out.println("Lotação atual : " + lotacaoAtual);
//					if (c.hisDtFim == null && lotacaoAtual.getDataFim() == null && c.dpPessoa == pes.getIdInicial()) {
//						resultado.add(lotacaoAtual);
//					}
//				}
//			}
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		return resultado;
//
//	}

//	public void excluirPessoaExtra(DpPessoa pes, DpLotacao lot, ITipoDeConfiguracao tpConf,
//			CpIdentidade identidadeCadastrante) {
//		ModeloDao.iniciarTransacao();
//		try {
//			Set<CpConfiguracaoCache> configs = getListaPorTipo(tpConf);
//			for (CpConfiguracaoCache c : configs) {
//				if (c.hisDtFim == null && c.dpPessoa == pes.getIdInicial() && c.lotacao == lot.getIdInicial()) {
//					CpConfiguracao cfg = dao().consultar(c.idConfiguracao, CpConfiguracao.class, false);
//					cfg.setHisDtFim(dao().consultarDataEHoraDoServidor());
//					dao().gravarComHistorico(cfg, identidadeCadastrante);
//				}
//			}
//			ModeloDao.commitTransacao();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	public boolean podeGerirAlgumGrupo(DpPessoa titular, DpLotacao lotaTitular, Long idCpTipoGrupo) throws Exception {
//		return dao().getGruposGeridos(titular, lotaTitular, idCpTipoGrupo).size() > 0;
//	}
//
//	public boolean podeGerirGrupo(DpPessoa titular, DpLotacao lotaTitular, Long idCpGrupo, Long idCpTipoGrupo) {
//
//		try {
//			CpGrupoDaoFiltro flt = new CpGrupoDaoFiltro();
//			CpGrupo cpGrp = CpDao.getInstance().consultar(idCpGrupo, CpGrupo.class, false);
//			flt.setIdTpGrupo(idCpTipoGrupo.intValue());
//			CpConfiguracaoBL bl = Cp.getInstance().getConf();
//
//			return bl.podePorConfiguracao(titular, lotaTitular, cpGrp, CpTipoDeConfiguracao.GERENCIAR_GRUPO);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return false;
//	}

	/**
	 * 
	 * Retorna uma lista de (ex)configurações vigentes de acordo com um certo tipo
	 * 
	 * @param CpConfiguracao
	 * 
	 */
//	public List<CpConfiguracao> buscarConfiguracoesVigentes(final CpConfiguracao exemplo) {
//		Date hoje = new Date();
//		List<CpConfiguracao> todasConfig = CpDao.getInstance().consultar(exemplo);
//		List<CpConfiguracao> configVigentes = new ArrayList<CpConfiguracao>();
//
//		for (CpConfiguracao cfg : todasConfig) { 
//			if (!cfg.ativaNaData(hoje))
//				continue;
//			configVigentes.add(cfg);
//		}
//		return (configVigentes);
//	}

//	public CpSituacaoDeConfiguracaoEnum situacaoPorConfiguracao(CpServico cpServico, DpCargo cargo,
//			CpOrgaoUsuario cpOrgaoUsu, DpFuncaoConfianca dpFuncaoConfianca,
//			DpLotacao dpLotacao, DpPessoa dpPessoa, CpTipoLotacao cpTpLotacao,
//			ITipoDeConfiguracao idTpConf, DpPessoa pessoaObjeto, 
//			DpLotacao lotacaoObjeto, CpComplexo complexoObjeto, DpCargo cargoObjeto, 
//			DpFuncaoConfianca funcaoConfiancaObjeto, CpOrgaoUsuario orgaoObjeto) {
//
//		CpConfiguracao config = new CpConfiguracao();
//		config.setCargo(cargo);
//		config.setOrgaoUsuario(cpOrgaoUsu);
//		config.setFuncaoConfianca(dpFuncaoConfianca);
//		config.setLotacao(dpLotacao);
//		config.setDpPessoa(dpPessoa);
//		config.setCpTipoConfiguracao(idTpConf);
//		config.setCpTipoLotacao(cpTpLotacao);
//
//		config.setCpServico(cpServico);
//		
//		config.setPessoaObjeto(pessoaObjeto);
//		config.setLotacaoObjeto(lotacaoObjeto);
//		config.setComplexoObjeto(complexoObjeto);
//		config.setCargoObjeto(cargoObjeto);
//		config.setFuncaoConfiancaObjeto(funcaoConfiancaObjeto);
//		config.setOrgaoObjeto(orgaoObjeto);
//
//		CpConfiguracaoCache cfg = (CpConfiguracaoCache) buscaConfiguracao(config,
//				new int[] { 0 }, null);
//
//		CpSituacaoDeConfiguracaoEnum situacao = null;
//		if (cfg != null)
//			situacao = cfg.situacao;
//		else
//			if (config.getCpTipoConfiguracao() != null)
//				situacao = config.getCpTipoConfiguracao().getSituacaoDefault();
//		
//		return situacao;
//	}

}
