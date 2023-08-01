package com.gregtechceu.gtceu.api.registry.registrate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.json.SimpleIGuiTextureJsonUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/7/31
 * @implNote CompassSectionBuilder
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@Accessors(fluent = true, chain = true)
public class CompassNode {
    @Getter
    private final ResourceLocation sectionID;
    @Getter
    private final ResourceLocation nodeID;
    @Setter
    private ResourceLocation page;
    @Setter
    private int size = 24;
    @Setter @Nullable // null - auto layout
    private Position position = null;
    private final Set<ResourceLocation> preNodes = new HashSet<>();
    private final List<Supplier<? extends Item>> items = new ArrayList<>();
    @Setter @Nullable
    private Supplier<IGuiTexture> icon = null;
    @Setter @Getter
    private String lang;

    private CompassNode(ResourceLocation sectionID, String nodeID) {
        this.sectionID = sectionID;
        lang = FormattingUtil.toEnglishName(nodeID);
        this.nodeID = GTCEu.id(sectionID.getPath() + "/" + nodeID);
        this.page = this.nodeID;
    }

    public static CompassNode getOrCreate(ResourceLocation sectionID, String nodeID) {
        var exist = GTRegistries.COMPASS_NODES.get(GTCEu.id(sectionID.getPath() + "/" + nodeID));
        return exist == null ? new CompassNode(sectionID, nodeID).register() : exist;
    }

    public static CompassNode getOrCreate(CompassSection section, String nodeID) {
        return getOrCreate(section.sectionID(), nodeID);
    }

    public static CompassNode getOrCreate(CompassSection section, Supplier<? extends Item> item) {
        return getOrCreate(section.sectionID(), BuiltInRegistries.ITEM.getKey(item.get()).getPath()).addItem(item);
    }

    private CompassNode register() {
        GTRegistries.COMPASS_NODES.register(nodeID, this);
        return this;
    }

    public String getUnlocalizedKey() {
        return nodeID.toLanguageKey("compass.node");
    }

    public CompassNode position(int x, int y) {
        this.position = new Position(x, y);
        return this;
    }

    public CompassNode addPreNode(ResourceLocation... nodeID) {
        preNodes.addAll(Arrays.stream(nodeID).toList());
        return this;
    }

    public CompassNode addPreNode(CompassNode... node) {
        preNodes.addAll(Arrays.stream(node).map(CompassNode::nodeID).toList());
        return this;
    }

    public CompassNode addItem(Supplier<? extends Item> item) {
        items.add(item);
        return this;
    }

    public static class CompassNodeProvider implements DataProvider {
        private final PackOutput output;
        private final Predicate<ResourceLocation> existingHelper;

        public CompassNodeProvider(PackOutput output, Predicate<ResourceLocation> existingHelper) {
            this.output = output;
            this.existingHelper = existingHelper;
        }

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            return generate(output.getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(GTCEu.MOD_ID), cache);
        }

        @Override
        public String getName() {
            return "GTCEU's Compass Nodes";
        }

        public CompletableFuture<?> generate(Path path, CachedOutput cache) {
            Map<ResourceLocation, List<CompassNode>> nodesNOPosition = new HashMap<>();
            CompletableFuture<?> future = CompletableFuture.completedFuture(null);
            for (var node : GTRegistries.COMPASS_NODES) {
                if (node.position == null) {
                    nodesNOPosition.computeIfAbsent(node.sectionID, k -> new ArrayList<>()).add(node);
                } else {
                    genNodeData(path, cache, node);
                }
            }
            for (List<CompassNode> nodes : nodesNOPosition.values()) {
                var size = nodes.size();
                var row = (int) Math.ceil(Math.sqrt(size));
                for (int i = 0; i < row; i++) {
                    boolean done = false;
                    for (int j = 0; j < row; j++) {
                        int index = i * row + j;
                        if (index < nodes.size()) {
                            var node = nodes.get(index);
                            node.position = new Position(-(row * 50) + 50 * j, 50 * i);
                            Path finalPath = path;
                            future = future.thenComposeAsync(v -> genNodeData(finalPath, cache, node));
                        } else {
                            done = true;
                            break;
                        }
                    }
                    if (done) break;
                }
            }
            return future;
        }

        private CompletableFuture<?> genNodeData(Path path, CachedOutput cache, CompassNode node) {
            if (node.position == null) return CompletableFuture.completedFuture(null);
            var resourcePath = "compass/nodes/" + node.nodeID.getPath() + ".json";
            if (existingHelper.test(GTCEu.id(resourcePath))) CompletableFuture.completedFuture(null);

            JsonObject json = new JsonObject();
            json.addProperty("section", node.sectionID.toString());
            json.addProperty("page", node.page.toString());
            if (node.icon == null) {
                if (!node.items.isEmpty()) {
                    node.icon = () -> new ItemStackTexture(node.items.get(0).get());
                } else {
                    node.icon = () -> IGuiTexture.EMPTY;
                }
            }
            json.add("button_texture",SimpleIGuiTextureJsonUtils.toJson(node.icon.get()));
            if (node.size != 24) {
                json.addProperty("size", node.size);
            }
            var pos = new JsonArray();
            pos.add(node.position.x);
            pos.add(node.position.y);
            json.add("position", pos);
            if (!node.preNodes.isEmpty()) {
                var pre = new JsonArray();
                for (var preNode : node.preNodes) {
                    pre.add(preNode.toString());
                }
                json.add("pre_nodes", pre);
            }
            if (!node.items.isEmpty()) {
                var items = new JsonArray();
                for (var item : node.items) {
                    items.add(BuiltInRegistries.ITEM.getKey(item.get()).toString());
                }
                json.add("items", items);
            }

            return DataProvider.saveStable(cache, json, path.resolve(resourcePath));
        }

    }

}
