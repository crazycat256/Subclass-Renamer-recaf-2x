package fr.crazycat256.subclassrenamer;

import fr.crazycat256.subclassrenamer.ui.RenameSubclassesPane;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.stage.Stage;
import me.coley.recaf.util.Log;
import me.coley.recaf.config.Conf;
import me.coley.recaf.config.FieldWrapper;
import me.coley.recaf.control.Controller;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.ui.ContextBuilder;
import me.coley.recaf.ui.controls.ActionMenuItem;
import me.coley.recaf.ui.controls.NumberSlider;
import me.coley.recaf.workspace.JavaResource;
import org.plugface.core.annotations.Plugin;
import me.coley.recaf.plugin.api.*;

import java.util.Map;
import java.util.function.Function;

import static me.coley.recaf.util.LangUtil.translate;

/**
 * A plugin that adds context menus to rename every class that extends/implments a given class.
 *
 * @author crazycat256
 */
@Plugin(name = "Subclass Renamer")
public class SubclassRenamer implements StartupPlugin, ContextMenuInjectorPlugin, ConfigurablePlugin {

	public static final String NAME_PLACEHOLDER = "{name}";
	public static final String INDEX_PLACEHOLDER = "{index}";

	@Conf(value = "config.phasetimeout")
	public long phaseTimeout = 30;
	
	@Conf(value = "config.nodebug")
	public boolean noDebug;


	private Controller controller;

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getDescription() {
		return translate("plugin.description");
	}

	@Override
	public String getConfigTabTitle() {
		return this.getName();
	}

	@Override
	public void onStart(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void addFieldEditors(Map<String, Function<FieldWrapper, Node>> editors) {
		editors.put("display.phasetimeout", field -> new NumberSlider<Integer>((GuiController) controller, field, 5, 180, 5));
	}

	@Override
	public void forClass(ContextBuilder builder, ContextMenu menu, String name) {
        menu.getItems().add(new ActionMenuItem(translate("ui.renamesubclasses"),
                () -> renameSubclasses(name, builder.getResource())));
    }

	public void renameSubclasses(String className, JavaResource resource) {
		try {
			// For some reason, java installed in "C:\Program Files\Common Files\Oracle\Java\javapath\" causes an error
			controller.getWorkspace().getClassReader("java.lang.Object");
		} catch (java.lang.IllegalArgumentException e) {
			Log.error("SubclassRenamer: Failed to perform rename operation. Please use a valid JDK.");
			return;
		}
        RenameSubclassesPane pane = new RenameSubclassesPane(this, (GuiController) controller, className, resource);
        Stage stage = ((GuiController) controller).windows().window(
                translate("ui.renamesubclasses") + ": " + resource,
                pane, 700, 150);
        stage.show();
        stage.toFront();
    }
}