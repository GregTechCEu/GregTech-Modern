package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {

    @Shadow
    @Final
    ItemStack result;

    @Inject(method = "assemble",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void gtceu$gtToolSmithingTransform1(Container container, RegistryAccess registryAccess,
                                                CallbackInfoReturnable<ItemStack> cir,
                                                @Share("newTag") LocalRef<CompoundTag> sharedTag) {
        ItemStack output = this.result.copy();

        if (!(output.getItem() instanceof IGTTool igtTool)) return;

        CompoundTag originalTag = container.getItem(1).getTag();
        CompoundTag newTag = originalTag != null ? originalTag.copy() : null;
        if (newTag == null) return;

        // Remove old tool stats
        newTag.remove("GT.Tool");

        // Copy stats from the upgraded tool
        ItemStack newStack = ToolHelper.get(igtTool.getToolType(), igtTool.getMaterial());
        Tag newStats = newStack.getTag() != null ? newStack.getTag().get("GT.Tool") : null;
        if (newStats != null) {
            newTag.put("GT.Tool", newStats);
            sharedTag.set(newTag);
        }
    }

    @Redirect(method = "assemble",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void gtceu$gtToolSmithingTransform2(ItemStack itemStack, CompoundTag tag,
                                                @Share("newTag") LocalRef<CompoundTag> sharedTag) {
        itemStack.setTag(sharedTag.get());
    }
}
