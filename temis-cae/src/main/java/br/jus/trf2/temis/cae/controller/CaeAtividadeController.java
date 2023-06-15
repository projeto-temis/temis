package br.jus.trf2.temis.cae.controller;

import java.util.List;

import com.crivano.juia.util.JuiaUtils;
import com.webfirmframework.wffweb.tag.html.AbstractHtml;
import com.webfirmframework.wffweb.tag.html.attributewff.CustomAttribute;
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div;
import com.webfirmframework.wffweb.tag.htmlwff.CustomTag;

import br.com.caelum.vraptor.Controller;
import br.com.caelum.vraptor.Path;
import br.jus.trf2.temis.cae.model.CaeAtividade;
import br.jus.trf2.temis.core.controller.ControllerSupport;

@Controller
@Path("app/cae-atividade")
public class CaeAtividadeController extends ControllerSupport<CaeAtividade> {

	@Override
	protected Div templateExibir(Class<CaeAtividade> clazz) throws Exception {
		Div div = super.templateExibir(clazz);
		Div p = (Div) JuiaUtils.findChildByAttribute(div, new CustomAttribute("id", "content"));
		Div col = (Div) p.getParent();

		List<AbstractHtml> children = col.getChildren();
		col.removeAllChildren();
		new CustomTag("cae-atividade-show", col, new CustomAttribute("ref", "atividadeShow"),
				new CustomAttribute(":data", "data"), new CustomAttribute(":action", "action"),
				new CustomAttribute(":event", "event"));
		col.appendChildren(children);
		return div;
	}
}
