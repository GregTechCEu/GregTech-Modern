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
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
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
        return getOrCreate(section.sectionID(), Registry.ITEM.getKey(item.get()).getPath()).addItem(item);
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
        private final DataGenerator generator;
        private final Predicate<ResourceLocation> existingHelper;

        public CompassNodeProvider(DataGenerator generator, Predicate<ResourceLocation> existingHelper) {
            this.generator = generator;
            this.existingHelper = existingHelper;
        }

        @Override
        public void run(CachedOutput cache) {
            generate(generator.getOutputFolder(), cache);
        }

        @Override
        public String getName() {
            return "GTCEU's Compass Nodes";
        }

        public void generate(Path path, CachedOutput cache) {
            path = path.resolve("assets/" + GTCEu.MOD_ID);
            Map<ResourceLocation, List<CompassNode>> nodesNOPosition = new HashMap<>();
            try {
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
                                genNodeData(path, cache, node);
                            } else {
                                done = true;
                                break;
                            }
                        }
                        if (done) break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void genNodeData(Path path, CachedOutput cache, CompassNode node) throws IOException {
            if (node.position == null) return;
            var resourcePath = "compass/nodes/" + node.nodeID.getPath() + ".json";
            if (existingHelper.test(GTCEu.id(resourcePath))) return;

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
                    items.add(Registry.ITEM.getKey(item.get()).toString());
                }
                json.add("items", items);
            }

            DataProvider.saveStable(cache, json, path.resolve(resourcePath));
        }

    }

}
