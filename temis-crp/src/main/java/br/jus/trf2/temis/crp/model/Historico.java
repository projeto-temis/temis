package br.jus.trf2.temis.crp.model;

import java.util.Date;

import javax.enterprise.inject.spi.CDI;

import br.jus.trf2.temis.core.util.Dao;

public interface Historico<T extends Historico> {
	public Long getId();

	public void setId(Long id);

	public Long getIdInicial();

	public void setIdInicial(Long idInicial);

	public Date getInicio();

	public void setInicio(Date inicio);

	public Date getTermino();

	public void setTermino(Date termino);

//	public Integer getHisAtivo();
//
//	public void setHisAtivo(Integer hisAtivo);

	default T atual() {
		Dao dao = CDI.current().select(Dao.class).get();
		return (T) dao.obterAtual(this);
	}

	default T inicial() {
		Dao dao = CDI.current().select(Dao.class).get();
		return (T) dao.obterInicial(this);
	}
}