package brachy.modularui.core.mixins.common;

import brachy.modularui.core.extensions.IRegistryFriendlyByteBufExtension;

import net.minecraft.network.RegistryFriendlyByteBuf;

import org.spongepowered.asm.mixin.Mixin;

// implement IRegistryFriendlyByteBufExtension on RegistryFriendlyByteBuf at runtime
@Mixin(RegistryFriendlyByteBuf.class)
public class RegistryFriendlyByteBufMixin implements IRegistryFriendlyByteBufExtension {
}
