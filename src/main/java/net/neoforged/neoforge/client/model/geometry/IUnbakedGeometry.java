package net.neoforged.neoforge.client.model.geometry;

import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.compat.ItemOverrides;
import com.gregtechceu.gtceu.client.model.compat.ModelState;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public interface IUnbakedGeometry<T extends IUnbakedGeometry<T>> extends UnbakedModel {

    BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                    Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                    ItemOverrides overrides);

    default void resolveParents(Function<Identifier, UnbakedModel> resolver, IGeometryBakingContext context) {}

    @Override
    default net.minecraft.client.resources.model.geometry.UnbakedGeometry geometry() {
        return net.minecraft.client.resources.model.geometry.UnbakedGeometry.EMPTY;
    }
}
