package com.gregtechceu.gtceu.data.pack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Stores contents of a dynamic resource pack in a tree-style format for efficient traversal. This class can safely
 * be accessed from multiple threads; it implements synchronization internally.
 *
 * @author embeddedt
 */
public class GTDynamicPackContents {

    private static class Node {

        /**
         * Holds either a IoSupplier<InputStream> with the data for a given location, or a map of string -> Node.
         */
        Object contents = new Object2ObjectOpenHashMap<>();

        void collectResources(String namespace, String[] pathComponents, int curIndex,
                              PackResources.ResourceOutput output) {
            if (curIndex < pathComponents.length) {
                String component = pathComponents[curIndex];

                Node n = getChild(component);
                if (n != null) {
                    n.collectResources(namespace, pathComponents, curIndex + 1, output);
                }
            } else {
                // We reached the desired path. Collect all resources
                this.outputResources(namespace, String.join("/", pathComponents), output);
            }
        }

        private boolean isTerminalNode() {
            return contents instanceof IoSupplier<?>;
        }

        @SuppressWarnings("unchecked")
        private Map<String, Node> getChildren() {
            if (!(contents instanceof Map<?, ?>)) {
                throw new IllegalStateException("attempting to get children on a terminal node");
            }
            return (Map<String, Node>) contents;
        }

        void outputResources(String namespace, String path, PackResources.ResourceOutput output) {
            if (isTerminalNode()) {
                // This is a terminal node.
                ResourceLocation location = new ResourceLocation(namespace, path);
                output.accept(location, this.createIoSupplier());
            } else {
                for (var entry : getChildren().entrySet()) {
                    entry.getValue().outputResources(namespace, path + "/" + entry.getKey(), output);
                }
            }
        }

        @SuppressWarnings("unchecked")
        IoSupplier<InputStream> createIoSupplier() {
            if (!isTerminalNode()) {
                throw new IllegalStateException("Node has no data");
            }
            // Capture the byte array here to avoid capturing the whole node in the lambda
            return (IoSupplier<InputStream>) contents;
        }

        @Nullable
        Node getChild(String name) {
            if (isTerminalNode()) {
                return null;
            } else {
                return getChildren().get(name);
            }
        }
    }

    private final Node root = new Node();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void addToData(ResourceLocation location, byte[] bytes) {
        addToData(location, () -> new ByteArrayInputStream(bytes));
    }

    public void addToData(ResourceLocation location, IoSupplier<InputStream> supplier) {
        String[] pathComponents = location.getPath().split("/");
        var lock = this.lock.writeLock();
        lock.lock();
        try {
            Node node = root.getChildren().computeIfAbsent(location.getNamespace(), $ -> new Node());
            for (String component : pathComponents) {
                node = node.getChildren().computeIfAbsent(component, $ -> new Node());
            }
            node.contents = supplier;
        } finally {
            lock.unlock();
        }
    }

    public void clearData() {
        var lock = this.lock.writeLock();
        lock.lock();
        try {
            root.getChildren().clear();
        } finally {
            lock.unlock();
        }
    }

    public IoSupplier<InputStream> getResource(ResourceLocation location) {
        var lock = this.lock.readLock();
        lock.lock();
        try {
            Node node = this.root.getChild(location.getNamespace());
            String[] pathComponents = location.getPath().split("/");
            for (String path : pathComponents) {
                if (node == null) {
                    return null;
                }
                node = node.getChild(path);
            }
            if (node == null) {
                return null;
            }
            return node.createIoSupplier();
        } finally {
            lock.unlock();
        }
    }

    public void listResources(String namespace, String path, PackResources.ResourceOutput resourceOutput) {
        var lock = this.lock.readLock();
        lock.lock();
        try {
            Node base = this.root.getChild(namespace);
            if (base == null) {
                return;
            }
            base.collectResources(namespace, path.split("/"), 0, resourceOutput);
        } finally {
            lock.unlock();
        }
    }
}
