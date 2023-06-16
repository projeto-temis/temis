package br.jus.trf2.temis.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.SortedSet;

import com.crivano.jbiz.IEvent;
import com.crivano.jsync.Synchronizable;
import com.crivano.juia.biz.IJuiaEntity;

import br.jus.trf2.temis.core.util.ContextInterceptor;

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
}
