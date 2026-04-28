package net.neoforged.neoforge.client.model.geometry;

import com.gregtechceu.gtceu.client.model.compat.ItemTransforms;

import net.minecraft.client.resources.model.sprite.Material;

import com.mojang.math.Transformation;

public interface IGeometryBakingContext {

    default ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    default Transformation getRootTransform() {
        return Transformation.IDENTITY;
    }

    default boolean isGui3d() {
        return true;
    }

    default boolean useBlockLight() {
        return true;
    }

    default boolean useAmbientOcclusion() {
        return true;
    }

    default boolean hasMaterial(String name) {
        return false;
    }

    default Material getMaterial(String name) {
        throw new IllegalArgumentException("No material named " + name);
    }
}
