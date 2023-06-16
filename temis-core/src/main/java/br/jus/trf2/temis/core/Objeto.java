package br.jus.trf2.temis.core;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

public class Objeto {
	@Override
	public String toString() {
		Map<Object, Boolean> visitedObjects = new IdentityHashMap<>();
		return toString(this, visitedObjects);
	}

	private String toString(Object object, Map<Object, Boolean> visitedObjects) {
		if (object == null) {
			return "null";
		}

		Class<?> objectClass = object.getClass();
		if (visitedObjects.containsKey(object)) {
			return objectClass.getSimpleName() + " (visited)";
		}
		visitedObjects.put(object, true);

		if (objectClass.isArray()) {
			return arrayToString(object, visitedObjects);
		}

		if (object instanceof Collection) {
			return collectionToString((Collection<?>) object, visitedObjects);
		}

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

	private String arrayToString(Object array, Map<Object, Boolean> visitedObjects) {
		StringBuilder sb = new StringBuilder("[");
		int length = Array.getLength(array);

		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);
			sb.append(toString(element, visitedObjects)).append(", ");
		}

		int lastIndex = sb.length() - 2;
		if (lastIndex >= 0 && sb.charAt(lastIndex) == ',') {
			sb.delete(lastIndex, lastIndex + 2);
		}

		sb.append("]");
		return sb.toString();
	}

	private String collectionToString(Collection<?> collection, Map<Object, Boolean> visitedObjects) {
		StringBuilder sb = new StringBuilder("[");
		for (Object element : collection) {
			sb.append(toString(element, visitedObjects)).append(", ");
		}

		int lastIndex = sb.length() - 2;
		if (lastIndex >= 0 && sb.charAt(lastIndex) == ',') {
			sb.delete(lastIndex, lastIndex + 2);
		}

		sb.append("]");
		return sb.toString();
	}
}
