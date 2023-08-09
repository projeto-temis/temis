package br.jus.trf2.temis.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.Hibernate;

import com.crivano.jbiz.IEvent;
import com.crivano.jbiz.ITag;
import com.crivano.jsync.SyncUtils;
import com.crivano.jsync.Synchronizable;
import com.crivano.juia.biz.IJuiaEntity;

import br.jus.trf2.temis.core.util.ContextInterceptor;
import br.jus.trf2.temis.core.util.ModeloUtils;

public interface IEntidade extends IJuiaEntity, Synchronizable {

	Long getId();

	String getSelectFirstLine();

	@Override
	public default void removeChange(IEvent c) {
		IJuiaEntity.super.removeChange(c);
		ContextInterceptor.getDao().remove(c);
	}

	SortedSet<Etiqueta> getEtiqueta();

	void prePersistAndUpdate() throws Exception;

	SortedSet<? extends Evento<?, ?>> getEvento();

	Date getTermino();

	String getDescrCompleta();

	default String toString(Object object, Map<Object, Boolean> visitedObjects) {
		if (object == null) {
			return "null";
		}

		Class<?> objectClass = object.getClass();
		if (visitedObjects.containsKey(object)) {
			return objectClass.getSimpleName() + " (visited)";
		}
		visitedObjects.put(object, true);

		StringBuilder sb = new StringBuilder(objectClass.getSimpleName()).append(" [");
		Field[] fields = objectClass.getDeclaredFields();

		for (Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				field.setAccessible(true);
				try {
					Object fieldValue = field.get(object);
					sb.append(field.getName()).append("=").append(toString(fieldValue, visitedObjects)).append(", ");
				} catch (IllegalAccessException e) {
					// Handle exception if needed
				}
			}
		}

		int lastIndex = sb.length() - 2;
		if (lastIndex >= 0 && sb.charAt(lastIndex) == ',') {
			sb.delete(lastIndex, lastIndex + 2);
		}

		sb.append("]");
		return sb.toString();
	}

	default <T extends Evento<?, ?>> SortedSet<T> getEventosAtivos(Class<T> clazz) {
		SortedSet<T> set = new TreeSet<>();
		for (Evento<?, ?> e : getEvento()) {
			if (e.isAtiva() && e.getClass().isAssignableFrom(clazz))
				set.add((T) e);
		}
		return set;
	};

	@Override
	default SortedSet<ITag> getTags() {
		SortedSet<ITag> tags = IJuiaEntity.super.getTags();
		for (Etiqueta t : (SortedSet<Etiqueta>) (SortedSet) tags) {
			// Ajustar para pessoas e unidades iniciais
			if (t.getPessoa() != null)
				t.setPessoa(t.getPessoa().inicial());
			if (t.getUnidade() != null)
				t.setUnidade(t.getUnidade().inicial());
		}
		return tags;
	}

	default void updateTags() throws Exception {
		SortedSet<Etiqueta> lOrig = (SortedSet<Etiqueta>) (SortedSet) getTags();
		SortedSet<Etiqueta> lDest = getEtiqueta();
		SortedSet<Etiqueta> lToRemove = new TreeSet<>();
		;

//		SortedSet<Etiqueta> lTemp = new TreeSet<>();
//		lTemp.addAll(lDest);
//		lDest.clear();
		for (Etiqueta oDest : lDest) {
			boolean encontrado = false;
			// remover itens de destino que não existem na origem
			for (Etiqueta oOrig : lOrig) {
				if (ModeloUtils.semelhante(oOrig, oDest)) {
					encontrado = true;
					break;
				}
			}
			if (!encontrado) {
				lToRemove.add(oDest);
				ContextInterceptor.getDao().remove(oDest);
			}
		}
		lDest.removeAll(lToRemove);

		for (Etiqueta oOrig : lOrig) {
			boolean encontrado = false;
			// remover itens de destino que não existem na origem,
			// atualizar itens que existem nos dois e
			for (Etiqueta oDest : lDest) {
				if (ModeloUtils.semelhante(oOrig, oDest)) {
					lDest.add(oDest);
					encontrado = true;
					break;
				}
			}
			if (!encontrado) {
				// inserir itens que só existem na origem
				lDest.add(oOrig);
			}
		}
	}

	@Override
	default boolean isSyncSimilar(Synchronizable obj, int level) {
		return SyncUtils.alike((Synchronizable) Hibernate.unproxy(this), (Synchronizable) Hibernate.unproxy(obj),
				level);
	}

}
