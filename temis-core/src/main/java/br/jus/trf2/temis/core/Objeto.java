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
		return toString(this, visitedObjects, 0);
	}

	private String toString(Object object, Map<Object, Boolean> visitedObjects, int depth) {
		if (depth == 5)
			return "...";
		if (object == null) {
			return "null";
		}

		Class<?> objectClass = object.getClass();
		if (visitedObjects.containsKey(object)) {
			return objectClass.getSimpleName() + " (visited)";
		}
		visitedObjects.put(object, true);

		if (objectClass.isAssignableFrom(String.class))
			return object.toString();

		if (objectClass.isArray()) {
			return arrayToString(object, visitedObjects, depth + 1);
		}

		if (object instanceof Collection) {
			return collectionToString((Collection<?>) object, visitedObjects, depth + 1);
		}

		StringBuilder sb = new StringBuilder(objectClass.getSimpleName()).append(" [");
		Field[] fields = objectClass.getDeclaredFields();

		for (Field field : fields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				field.setAccessible(true);
				try {
					Object fieldValue = field.get(object);
					sb.append(field.getName()).append("=").append(toString(fieldValue, visitedObjects, depth + 1))
							.append(", ");
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

	private String arrayToString(Object array, Map<Object, Boolean> visitedObjects, int depth) {
		StringBuilder sb = new StringBuilder("[");
		int length = Array.getLength(array);

		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);
			sb.append(toString(element, visitedObjects, depth + 1)).append(", ");
		}

		int lastIndex = sb.length() - 2;
		if (lastIndex >= 0 && sb.charAt(lastIndex) == ',') {
			sb.delete(lastIndex, lastIndex + 2);
		}

		sb.append("]");
		return sb.toString();
	}

	private String collectionToString(Collection<?> collection, Map<Object, Boolean> visitedObjects, int depth) {
		StringBuilder sb = new StringBuilder("[");
		for (Object element : collection) {
			sb.append(toString(element, visitedObjects, depth + 1)).append(", ");
		}

		int lastIndex = sb.length() - 2;
		if (lastIndex >= 0 && sb.charAt(lastIndex) == ',') {
			sb.delete(lastIndex, lastIndex + 2);
		}

		sb.append("]");
		return sb.toString();
	}
}
