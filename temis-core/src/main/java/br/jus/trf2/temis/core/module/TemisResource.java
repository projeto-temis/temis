package br.jus.trf2.temis.core.module;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import com.crivano.juia.annotations.Global;
import com.crivano.juia.annotations.MenuCreate;
import com.crivano.juia.annotations.MenuList;
import com.crivano.juia.annotations.MenuReport;
import com.google.gson.JsonObject;

import br.jus.trf2.temis.core.util.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemisResource {
	public TemisResourceKindEnum kind;
	public String pathName;
	public Class clazz;

	public TemisResource(TemisResourceKindEnum kind, String pathName) {
		this.kind = kind;
		this.pathName = pathName;
	}

	public TemisResource(TemisResourceKindEnum kind, Class<? extends Enum<?>> clazz) {
		this.kind = kind;
		this.clazz = clazz;
	}

	public String getResourceAsString(TemisApp app, String pathName) throws IOException {
		TemisDownload xisd = app.getResource(pathName);
		if (xisd == null)
			return null;
		String s = new String(xisd.bytes, StandardCharsets.UTF_8);
		return s;
	}

	public String getAsString(TemisApp app) throws IOException {
		if (pathName != null)
			return getResourceAsString(app, pathName);
		if (clazz != null && Enum.class.isAssignableFrom(clazz))
			return getEnumAsString(clazz);
		if (clazz != null)
			return getModelAsJson(clazz);
		return null;
	}

	public String getEnumAsString(Class<? extends Enum<?>> clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append("window.");
		sb.append(clazz.getSimpleName());
		sb.append(" = {");

		for (Enum e : clazz.getEnumConstants()) {
			sb.append(e.name());
			sb.append(": {name:'");
			sb.append(e.name());
			sb.append("'");

			for (Field f : e.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				Class type = f.getType();
				Object value;
				try {
					value = f.get(e);
				} catch (Exception e1) {
					value = null;
				}
				if (value == null)
					continue;
				if (type == String.class) {
					sb.append(", ");
					sb.append(f.getName());
					sb.append(":'");
					sb.append(value.toString().replace("'", "\\'"));
					sb.append("'");
				}
			}

			sb.append("}, ");
		}

		sb.append("};\n");
		sb.append("if (!window.enums) window.enums = {};\n");
		sb.append("window.enums.");
		sb.append(clazz.getSimpleName());
		sb.append(" = window.");
		sb.append(clazz.getSimpleName());
		sb.append(";\n");
		return sb.toString();
	}

	private String getModelAsJson(Class cls) {
		JsonObject o = new JsonObject();
		o.addProperty("className", cls.getName());
		o.addProperty("classSimpleName", cls.getSimpleName());
		Global juiaGlobal = (Global) cls.getAnnotation(Global.class);
		if (juiaGlobal != null) {
			o.addProperty("singular", juiaGlobal.singular());
			o.addProperty("plural", juiaGlobal.singular());
			o.addProperty("gender", juiaGlobal.gender().name());
			String locator = Utils.localizadorDaClasse(cls);
			o.addProperty("locator", locator);
		}
		MenuList juiaMenuList = (MenuList) cls.getAnnotation(MenuList.class);
		if (juiaMenuList != null) {
			o.addProperty("menuList", true);
			o.addProperty("menuListCaption", juiaMenuList.caption());
			o.addProperty("menuListHint", juiaMenuList.hint());
		}
		MenuCreate juiaMenuCreate = (MenuCreate) cls.getAnnotation(MenuCreate.class);
		if (juiaMenuCreate != null) {
			o.addProperty("menuCreate", true);
			o.addProperty("menuCreateCaption", juiaMenuCreate.caption());
			o.addProperty("menuCreateHint", juiaMenuCreate.hint());
		}
		MenuReport juiaMenuReport = (MenuReport) cls.getAnnotation(MenuReport.class);
		if (juiaMenuReport != null) {
			o.addProperty("menuReport", true);
			o.addProperty("menuReportCaption", juiaMenuReport.caption());
			o.addProperty("menuReportHint", juiaMenuReport.hint());
		}
		return o.toString();
	}
}
