package br.jus.trf2.temis.core.module;

import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.persistence.Entity;

import org.reflections.Reflections;

import com.crivano.juia.annotations.Global;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Path;
import br.jus.trf2.temis.core.Objeto;
import br.jus.trf2.temis.core.controller.ControllerSupport;
import lombok.NoArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.type.TypeDescription;

@NoArgsConstructor
public class TemisAutoController implements Extension {

	public void beforeBean(final @Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
		long startTime = System.currentTimeMillis();
		int c = 0;

		Reflections reflections = new Reflections("br.jus.trf2.temis");
		Set<Class<? extends Objeto>> subTypes = reflections.getSubTypesOf(Objeto.class);
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);

		// Registrar controllers adicionais
		for (Class<?> clazz : annotated) {
			// Não fazer para entidades subordinadas
			String name = clazz.getName();
			if (name.contains("$") || name.contains(".event.") || !name.contains(".model."))
				continue;
			Class<?> clazzController = generateController(clazz);
			if (clazzController != null) {
				beforeBeanDiscovery.addAnnotatedType(beanManager.createAnnotatedType(clazzController),
						clazzController.getSimpleName());
				// System.out.println("Criando controller automático: " +
				// clazzController.getName());
				c++;
			}
		}
		long endTime = System.currentTimeMillis();
		long timeElapsed = endTime - startTime;
		System.out.println("Têmis: " + c + " controllers automáticos criados em " + timeElapsed + "ms");
	}

	public Class generateControllerNew(Class<?> clazz) {
		String classname = clazz.getName().replace(".model.", ".controller.") + "Controller";
		Global global = clazz.getAnnotation(Global.class);
		if (global == null)
			return null;
		String[] path = new String[] { "app/" + global.locator() };
		try {
			Class<?> controller = new ByteBuddy()
					.subclass(TypeDescription.Generic.Builder.parameterizedType(ControllerSupport.class, clazz).build())
					.name(classname).annotateType(AnnotationDescription.Builder.ofType(RequestScoped.class).build())
					.annotateType(AnnotationDescription.Builder.ofType(Controller.class).build())
					.annotateType(AnnotationDescription.Builder.ofType(Path.class).defineArray("value", path).build())
					.make().load(getClass().getClassLoader()).getLoaded();
			return controller;
		} catch (java.lang.IllegalStateException e) {
			return null;
		}
	}

	public Class generateController(Class<?> clazz) {
		String classname = clazz.getName().replace(".model.", ".controller.") + "Controller";
		try {
			this.getClass().getClassLoader().loadClass(classname);
			return null;
		} catch (ClassNotFoundException e) {
			Global global = clazz.getAnnotation(Global.class);
			if (global == null)
				return null;
			String[] path = new String[] { "app/" + global.locator() };
			Class<?> controller = new ByteBuddy()
					.subclass(TypeDescription.Generic.Builder.parameterizedType(ControllerSupport.class, clazz).build())
					.name(classname).annotateType(AnnotationDescription.Builder.ofType(RequestScoped.class).build())
					.annotateType(AnnotationDescription.Builder.ofType(Controller.class).build())
					.annotateType(AnnotationDescription.Builder.ofType(Path.class).defineArray("value", path).build())
					.make().load(getClass().getClassLoader()).getLoaded();
			return controller;
		}
	}
}
