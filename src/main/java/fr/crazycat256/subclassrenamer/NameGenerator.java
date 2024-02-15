package fr.crazycat256.subclassrenamer;

import org.objectweb.asm.tree.ClassNode;


/**
 * Name generator that creates names based on a pattern.
 * Copied from Recaf's <a href="https://github.com/Recaf-Plugins/Auto-Renamer/blob/master/src/main/java/me/coley/recaf/plugin/rename/NameGenerator.java">AutoRename Plugin</a>
 *
 * @author Matt Coley
 */
public class NameGenerator {
	private final String packageOverride;
	private final ClassNode superClass;
	private final String pattern;
	private int idx = 0;

	/**
	 * @param superClass
	 * 		The super class to base names off of.
	 * @param pattern
	 * 		The pattern to use for naming.
	 */
	public NameGenerator(ClassNode superClass, String pattern) {
		this.packageOverride = null;
		this.superClass = superClass;
		this.pattern = pattern;
	}

	/**
	 * @return {@code true} when the name generation implementation is safe to be executed with multiple threads.
	 */
	public boolean allowMultiThread() {
		return !pattern.contains(SubclassRenamer.INDEX_PLACEHOLDER);
	}

	/**
	 * @param node
	 * 		Class to rename.
	 *
	 * @return New internal name, or {@code null} if the naming scope does not apply to the class.
	 */
	public String createClassName(ClassNode node) {
		idx++;
		// Skip if the current name does not match the target scope.
		String currentName = node.name;

		// Create the new name for the class.
		String nodeSimpleName = currentName.substring(currentName.lastIndexOf('/') + 1);
		String superName = superClass.name.substring(superClass.name.lastIndexOf('/') + 1);
		String newName = pattern.replace(SubclassRenamer.NAME_PLACEHOLDER, nodeSimpleName).replace(SubclassRenamer.INDEX_PLACEHOLDER, String.valueOf(idx));

		// Skip if the new name is the same as the super class name
		if (newName.equals(superName)) {
			return null;
		}
		// Put all renamed classes into the given package.
		if (packageOverride != null) {
			return packageOverride + newName;
		}
		// Return the new name
        return newName;
	}
}
