package fr.crazycat256.subclassrenamer.ui;

import fr.crazycat256.subclassrenamer.Processor;
import fr.crazycat256.subclassrenamer.SubclassRenamer;
import fr.crazycat256.subclassrenamer.util.NodeUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.ui.controls.SubLabeled;
import me.coley.recaf.ui.controls.pane.ColumnPane;
import me.coley.recaf.workspace.JavaResource;
import org.objectweb.asm.tree.ClassNode;

import static me.coley.recaf.util.LangUtil.translate;

/**
 * Pane for handling renaming of subclasses.
 *
 * @author crazycat256
 */
public class RenameSubclassesPane extends ColumnPane {


    private final SubclassRenamer plugin;
    private final GuiController controller;
    private final String className;
    private final JavaResource resource;
    private final TextField patternField = new TextField();
    private final CheckBox recursiveBox = new CheckBox();
    private final Label errorLabel = new Label();
    private final Button renameButton = new Button(translate("ui.pane.rename.button"));

    /**
     * @param plugin     The plugin.
     * @param controller Controller to act on.
     * @param className  Name of the class to rename.
     * @param resource   Resource containing the class.
     */
    public RenameSubclassesPane(SubclassRenamer plugin, GuiController controller, String className, JavaResource resource) {
        this.plugin = plugin;
        this.controller = controller;
        this.className = className;
        this.resource = resource;

        String packageName = className.contains("/") ? className.substring(0, className.lastIndexOf("/")) : "";
        String simpleClassName = className.contains("/") ? className.substring(className.lastIndexOf("/") + 1) : className;
        patternField.setText(packageName.isEmpty() ? simpleClassName : packageName + "/" + simpleClassName + "_" + SubclassRenamer.NAME_PLACEHOLDER);

        renameButton.setOnAction(this::doRename);

        errorLabel.styleProperty().set("-fx-text-fill: #fe3f3f;");

        add(new SubLabeled(translate("ui.pane.namepattern.name"), String.format(translate("ui.pane.namepattern.description"), SubclassRenamer.NAME_PLACEHOLDER, SubclassRenamer.INDEX_PLACEHOLDER)), patternField);
        add(new SubLabeled(translate("ui.pane.recursive.name"), translate("ui.pane.recursive.description")), recursiveBox);
        add(errorLabel, renameButton);

    }


    private void doRename(ActionEvent e) {
        String pattern = patternField.getText();
        if (pattern.isEmpty()) {
            errorLabel.setText(translate("ui.pane.rename.error.emptypattern"));
            return;
        }
        if (!pattern.contains(SubclassRenamer.NAME_PLACEHOLDER) && !pattern.contains(SubclassRenamer.INDEX_PLACEHOLDER)) {
            errorLabel.setText(String.format(translate("ui.pane.rename.error.invalidpattern"), SubclassRenamer.NAME_PLACEHOLDER, SubclassRenamer.INDEX_PLACEHOLDER));
            return;
        }

        ClassNode node = NodeUtil.getClassNode(controller, className);

        Processor processor = new Processor(controller, plugin, node, pattern, recursiveBox.isSelected());

        processor.analyze(resource.getClasses().keySet());
        processor.apply();
    }


}
