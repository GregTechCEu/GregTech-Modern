package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.client.model.SpriteOverrider;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote BlockModelMixin
 */
@Mixin(BlockModel.class)
public class BlockModelMixin {
    @Shadow public String name;
    ThreadLocal<SpriteOverrider> spriteOverriderThreadLocal = ThreadLocal.withInitial(() -> null);

    // We want to remap our materials
    @Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;",
            at =@At(value = "HEAD")
    )
    private void beforeBake(ModelBaker baker, BlockModel model, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
        if (spriteGetter instanceof SpriteOverrider spriteOverrider) {
            spriteOverriderThreadLocal.set(spriteOverrider);
        }
    }

    @Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/renderer/block/model/BlockModel;Ljava/util/function/Function;Lnet/minecraft/client/resources/model/ModelState;Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/resources/model/BakedModel;",
            at =@At(value = "RETURN")
    )
    private void afterBake(ModelBaker baker, BlockModel model, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state, ResourceLocation location, boolean guiLight3d, CallbackInfoReturnable<BakedModel> cir) {
        if (spriteGetter instanceof SpriteOverrider) {
            spriteOverriderThreadLocal.remove();
        }
    }


    // We want to remap our materials
    @Inject(method = "findTextureEntry", at =@At(value = "HEAD"), cancellable = true)
    private void remapTextureEntry(String name, CallbackInfoReturnable<Either<Material, String>> cir) {
        SpriteOverrider overrider = spriteOverriderThreadLocal.get();
        if (overrider != null && overrider.override().containsKey(name)) {
            var mat = overrider.override().get(name);
            if (mat != null) {
                cir.setReturnValue(ModelFactory.parseBlockTextureLocationOrReference(mat.toString()));
            }
        }
    }

}
