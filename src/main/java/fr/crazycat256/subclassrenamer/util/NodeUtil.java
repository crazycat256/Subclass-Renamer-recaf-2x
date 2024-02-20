package fr.crazycat256.subclassrenamer.util;

import me.coley.recaf.control.Controller;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.ClassReader;

/**
 * Utility for working with ClassNodes.
 *
 * @author crazycat256
 */
public class NodeUtil {

    /**
     * Check if a class is a subclass of another class.
     *
     * @param controller Controller to act on.
     * @param subclass   Class to check.
     * @param parent     Parent class to check against.
     * @param recursive  Whether to check recursively through the class hierarchy.
     * @return {@code true} if the class is a subclass of the parent class.
     */
    public static boolean isSubclassOf(Controller controller, ClassNode subclass, ClassNode parent, boolean recursive) {
        if (subclass.superName.equals(parent.name) || subclass.interfaces.contains(parent.name)) {
            return true;
        }

        if (recursive) {
            String superClass = subclass.superName;
            while (superClass != null) {
                if (superClass.equals(parent.name) || subclass.interfaces.contains(parent.name)) {
                    return true;
                }

                try {
                    superClass = getClassNode(controller, superClass).superName;
                } catch (java.lang.IllegalArgumentException e) {
                    return false;
                }

            }
        }
        return false;
    }

    /**
     * Get a ClassNode from a class name.
     *
     * @param controller Controller to act on.
     * @param className  Name of the class.
     * @return ClassNode for the class.
     */
    public static ClassNode getClassNode(Controller controller, String className) {
        ClassReader reader = controller.getWorkspace().getClassReader(className);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.SKIP_CODE);
        return node;
    }

}
