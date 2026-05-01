package com.lowdragmc.lowdraglib.gui.util;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TreeBuilder<K, V> {

    private final K key;
    private final Map<K, Object> children = new LinkedHashMap<>();

    public TreeBuilder(K key) {
        this.key = key;
    }

    public static <K, V> TreeBuilder<K, V> start(K key) {
        return new TreeBuilder<>(key);
    }

    public TreeBuilder<K, V> branch(K key, Consumer<TreeBuilder<K, V>> consumer) {
        TreeBuilder<K, V> branch = new TreeBuilder<>(key);
        consumer.accept(branch);
        children.put(key, branch);
        return this;
    }

    public TreeBuilder<K, V> startBranch(K key) {
        return branch(key, ignored -> {});
    }

    public TreeBuilder<K, V> endBranch() {
        return this;
    }

    public TreeBuilder<K, V> leaf(K key, V value) {
        children.put(key, value);
        return this;
    }

    public TreeBuilder<K, V> remove(K key) {
        children.remove(key);
        return this;
    }

    public TreeNode<K, V> build() {
        return new TreeNode<>(key);
    }

    public static class Menu {

        private final Map<String, Runnable> leaves = new LinkedHashMap<>();

        public static Menu start() {
            return new Menu();
        }

        public Menu leaf(IGuiTexture icon, String name, Runnable runnable) {
            leaves.put(name, runnable);
            return this;
        }

        public Menu branch(String name, Consumer<Menu> consumer) {
            Menu menu = new Menu();
            consumer.accept(menu);
            return this;
        }

        public Menu remove(String name) {
            leaves.remove(name);
            return this;
        }
    }
}
