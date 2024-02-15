package fr.crazycat256.subclassrenamer;

import fr.crazycat256.subclassrenamer.util.NodeUtil;
import me.coley.recaf.control.Controller;
import me.coley.recaf.mapping.Mappings;
import me.coley.recaf.util.ClassUtil;
import me.coley.recaf.util.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Main handler for creating new names and applying them.
 * Copied from Recaf's <a href="https://github.com/Recaf-Plugins/Auto-Renamer/blob/master/src/main/java/me/coley/recaf/plugin/rename/Processor.java">AutoRename Plugin</a>
 *
 * @author Matt Coley
 */
public class Processor {
    private final Map<String, String> mappings = new ConcurrentHashMap<>();
    private final Controller controller;
    private final SubclassRenamer plugin;
    private final ClassNode node;
    private final boolean recursive;
    private final NameGenerator generator;

    /**
     * @param controller
     * 		Controller with workspace to pull classes from.
     * @param plugin
     * 		Plugin with config values.
     */
    public Processor(Controller controller, SubclassRenamer plugin, ClassNode node, String pattern, boolean recursive) {
        this.controller = controller;
        this.plugin = plugin;
        this.node = node;
        this.recursive = recursive;
        // Configure name generator
        generator = new NameGenerator(node, pattern);
    }

    /**
     * Analyze the given classes and create new names for them and their members.
     *
     * @param matchedNames
     * 		Set of class names to analyze.
     */
    public void analyze(Set<String> matchedNames) {
        // Reset mappings
        mappings.clear();
        // Analyze each class in separate phases
        // Phase 0: Prepare class nodes
        Set<ClassNode> nodes = collectNodes(matchedNames);
        // Phase 1: Create mappings for class names
        //  - following phases can use these names to enrich their naming logic
        pooled("Analyze: Class names", service -> {
            for (ClassNode node : nodes) {
                service.submit(() -> analyzeClass(node));
            }
        });
    }

    /**
     * @param matchedNames
     * 		Names of classes to collect.
     *
     * @return Set of nodes from the given names.
     */
    private Set<ClassNode> collectNodes(Set<String> matchedNames) {
        Set<ClassNode> nodes = Collections.newSetFromMap(new ConcurrentHashMap<>());
        pooled("Collect-Nodes", service -> {
            for (String name : matchedNames) {
                service.submit(() -> {
                    ClassReader cr = controller.getWorkspace().getClassReader(name);
                    if (cr == null) {
                        Log.warn("SubclassRenamer failed to read class from workspace: " + name);
                        return;
                    }
                    ClassNode node = ClassUtil.getNode(cr, ClassReader.SKIP_FRAMES);
                    nodes.add(node);
                });
            }
        });
        return nodes;
    }

    /**
     * Generate mapping for class.
     *
     * @param node
     * 		Class to rename.
     */
    private void analyzeClass(ClassNode node) {
        try {
            // Skip special cases: 'module-info'/'package-info'
            if (node.name.matches("(?:[\\w\\/]+\\/)?(?:module|package)-info")) {
                return;
            }
            // Class name
            String oldClassName = node.name;
            String newClassName = generator.createClassName(node);
            if (newClassName != null && NodeUtil.isSubclassOf(controller, node, this.node, recursive)) {
                mappings.put(oldClassName, newClassName);
            }
        } catch (Throwable t) {
            Log.error(t, "Error occurred in Processor#analyzeClass");
        }
    }

    /**
     * Applies the mappings created from {@link #analyze(Set) the analysis phase}
     * to the primary resource of the workspace
     */
    public void apply() {
        SortedMap<String, String> sortedMappings = new TreeMap<>(mappings);
        Mappings mapper = new Mappings(controller.getWorkspace());
        mapper.setCheckFieldHierarchy(true);
        mapper.setCheckMethodHierarchy(true);
        if (plugin.noDebug) {
            mapper.setClearDebugInfo(true);
        }
        mapper.setMappings(sortedMappings);
        mapper.accept(controller.getWorkspace().getPrimary());
        Log.info("Done auto-mapping! Applied {} mappings", sortedMappings.size());
    }

    /**
     * Run a task that utilizes {@link ExecutorService} for parallel execution.
     * Pooled
     *
     * @param phaseName
     * 		Task name.
     * @param task
     * 		Task to run.
     */
    private void pooled(String phaseName, Consumer<ExecutorService> task) {
        try {
            long start = System.currentTimeMillis();
            Log.info("SubclassRenamer Processing: Task '{}' starting", phaseName);
            ExecutorService service;
            if (generator.allowMultiThread()) {
                service = Executors.newFixedThreadPool(getThreadCount());
            } else {
                service = Executors.newSingleThreadExecutor();
            }
            task.accept(service);
            service.shutdown();
            service.awaitTermination(plugin.phaseTimeout, TimeUnit.SECONDS);
            Log.info("SubclassRenamer Processing: Task '{}' completed in {}ms", phaseName, (System.currentTimeMillis() - start));
        } catch (Throwable t) {
            Log.error(t, "Failed processor phase '{}', reason: {}", phaseName, t.getMessage());
        }
    }

    private static int getThreadCount() {
        return Runtime.getRuntime().availableProcessors();
    }
}
