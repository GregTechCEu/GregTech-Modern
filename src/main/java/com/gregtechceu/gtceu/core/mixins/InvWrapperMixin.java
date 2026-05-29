package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.component.SpoilContext;
import com.gregtechceu.gtceu.api.item.component.SpoilUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = InvWrapper.class, remap = false)
public abstract class InvWrapperMixin {
    @Shadow
    @NotNull
    public abstract ItemStack getStackInSlot(int slot);

    @Shadow
    @Final
    private Container inv;

    @Unique
    private SpoilContext gtceu$getSpoilContext() {
        SpoilContext ctx = new SpoilContext();
        if (inv instanceof BlockEntity blockEntity) {
            return ctx
                    .withLevel(blockEntity.getLevel())
                    .withPos(blockEntity.getBlockPos());
        }
        return ctx;
    }

    @WrapOperation(method = {"setStackInSlot", "insertItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void gtceu$spoilInsertedItem(Container instance, int slot, ItemStack itemStack, Operation<Void> original) {
        SpoilUtils.update(itemStack, gtceu$getSpoilContext().withSlot(slot));
        original.call(instance, slot, itemStack);
    }
}
