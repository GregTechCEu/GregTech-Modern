package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.armor.ModularArmorItem;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(SmithingTransformRecipe.class)
public class SmithingTransformRecipeMixin {

    /**
     * Smithing transform recipes can't modify the item's tag, this mixin fixes that for tools & netherite armor plating
     */
    @ModifyArg(method = "assemble",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/world/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"))
    private CompoundTag gtceu$applyTagsOnAssemble(CompoundTag newTag, @Local ItemStack output) {

        var recipe = (SmithingTransformRecipe)(Object)this;

        if (recipe.getId().getPath().startsWith("netherite_plated_") && recipe.getId().getNamespace().equals(GTCEu.MOD_ID)) {
            newTag.putBoolean(ModularArmorItem.NETHERITE_PLATED_TAG, true);
            return newTag;
        }

        if (output.getItem() instanceof IGTTool gtTool) {
            // Copy stats from the upgraded tool
            ItemStack newStack = ToolHelper.get(gtTool.getToolType(), gtTool.getMaterial());
            if (!newStack.hasTag()) {
                return newTag;
            }

            Tag newStats = newStack.getTagElement("GT.Tool");
            if (newStats != null) {
                // newTag is already a copy of the original stack's tag, so we don't need to copy it again.

                // put() removes the old entry in the NBT, so this also removes old tool stats
                // do a defensive copy of the stat tag
                newTag.put("GT.Tool", newStats.copy());
            }
            return newTag;
        }
        return newTag;
    }

}
