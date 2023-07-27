package com.gregtechceu.gtceu.api.gui.compass;

import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.json.SimpleIGuiTextureJsonUtils;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.realmsclient.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/7/27
 * @implNote CompassNode
 */
public class CompassSection {
    @Getter
    private final JsonObject config;
    public final ResourceLocation sectionName;
    public final int priority;
    public final Map<ResourceLocation, CompassNode> nodes;
    /**
     * pre nodes of this node.
     */
    public final Map<CompassNode, CompassNode[]> preNodes;
    /**
     * child nodes of this node.
     */
    public final Map<CompassNode, CompassNode[]> childNodes;
    @Getter
    protected float chance;
    @Getter @Setter
    protected Supplier<IGuiTexture> buttonTexture;
    @Getter @Setter
    protected Supplier<IGuiTexture> backgroundTexture;

    public CompassSection(ResourceLocation sectionName, JsonObject config) {
        this.config = config;
        this.sectionName = sectionName;
        this.nodes = new HashMap<>();
        this.preNodes = new HashMap<>();
        this.childNodes = new HashMap<>();
        this.priority = JsonUtils.getIntOr("priority", config, 0);
        this.setButtonTexture(Suppliers.memoize(() -> SimpleIGuiTextureJsonUtils.fromJson(config.get("button_texture").getAsJsonObject())));
        this.setBackgroundTexture(Suppliers.memoize(() -> SimpleIGuiTextureJsonUtils.fromJson(config.get("background_texture").getAsJsonObject())));
    }

    public void addNode(CompassNode compassNode) {
        nodes.put(compassNode.getNodeName(), compassNode);
    }

    public void initRelation() {
        nodes.values().forEach(CompassNode::initRelation);
    }

    @Override
    public final String toString() {
        return sectionName.toString();
    }

    public CompassNode getNode(ResourceLocation nodeName) {
        return nodes.get(nodeName);
    }

    public void addPreRelation(CompassNode node, CompassNode... pres) {
        preNodes.put(node, ArrayUtils.addAll(preNodes.get(node), pres));
        for (var pre : pres) {
            childNodes.put(pre, ArrayUtils.add(childNodes.get(pre), node));
        }
    }
}
