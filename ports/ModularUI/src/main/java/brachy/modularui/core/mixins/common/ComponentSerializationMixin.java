package brachy.modularui.core.mixins.common;

import brachy.modularui.drawable.text.ModularComponent;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import brachy.modularui.utils.serialization.codec.CodecUtil;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * We need this since nested components are potentially modular too
 */
@Mixin(ComponentSerialization.class)
public class ComponentSerializationMixin {

    @ModifyVariable(method = "createCodec", at = @At("HEAD"), argsOnly = true)
    private static Codec<Component> modularui$replaceComponentCodec(Codec<Component> originalCodec) {
        // first try the modular component codec and if that fails fall back to the default codec
        return Codec.either(ModularComponent.CODEC.codec(), originalCodec)
                .xmap(CodecUtil::unboxEither, component -> {
                    if (component instanceof ModularComponent modular) return Either.left(modular);
                    else return Either.right(component);
                });
    }
}
