package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardEffectTracker;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    public abstract ItemStack getItem(int slot);

    @Shadow
    @Final
    public Player player;

    @Shadow
    @Final
    public NonNullList<ItemStack> offhand;

    @WrapOperation(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean gtceu$modifyFindSlotMatcher(ItemStack stack, ItemStack other, Operation<Boolean> original) {
        if (stack.getItem() instanceof IGTTool) {
            return ItemStack.isSameItem(stack, other);
        }
        return original.call(stack, other);
    }

    @WrapOperation(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isDamaged()Z", remap = true))
    private boolean gtceu$damagedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }

    @WrapOperation(method = "findSlotMatchingUnusedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z", remap = true))
    private boolean gtceu$enchantedToolBypass(ItemStack instance, Operation<Boolean> original) {
        if (instance.getItem() instanceof IGTTool) {
            return false;
        }
        return original.call(instance);
    }

    // Capture any and all player inventory changes. why doesn't forge have an event for this???
    @Inject(method = "setItem", at = @At("HEAD"))
    private void gtceu$setItemInSlot(int slot, ItemStack stack, CallbackInfo ci) {
        if (this.player.level().isClientSide) {
            return;
        }
        ItemStack current = !stack.isEmpty() ? stack : this.getItem(slot);
        // force the stack to have a count of 1 in case it's empty.
        if (current != ItemStack.EMPTY) {
            ItemStack old = current;
            int oldCount = old.getCount();
            current.setCount(1);
            current = current.copy();
            old.setCount(oldCount);
        }

        UnificationEntry entry = null;
        if (ConfigHolder.INSTANCE.gameplay.universalHazards) {
            entry = ChemicalHelper.getUnificationEntry(current.getItem());
        } else if (current.getItem() instanceof TagPrefixItem prefixItem) {
            entry = new UnificationEntry(prefixItem.tagPrefix, prefixItem.material);
        }
        if (entry == null || entry.material == null) {
            return;
        }
        if (!entry.material.hasProperty(PropertyKey.HAZARD)) {
            return;
        }
        IHazardEffectTracker tracker = GTCapabilityHelper.getHazardEffectTracker(player);
        if (tracker == null) {
            return;
        }
        if (current.isEmpty()) {
            tracker.removeHazardItem(entry.material.getProperty(PropertyKey.HAZARD));
        } else {
            tracker.addHazardItem(entry.material.getProperty(PropertyKey.HAZARD));
        }
    }

    @Inject(method = "removeItem(II)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private void gtceu$dropitem(int slot, int amount, CallbackInfoReturnable<ItemStack> cir) {
        if (this.player.level().isClientSide) {
            return;
        }
        ItemStack current = this.getItem(slot);

        UnificationEntry entry = null;
        if (ConfigHolder.INSTANCE.gameplay.universalHazards) {
            entry = ChemicalHelper.getUnificationEntry(current.getItem());
        } else if (current.getItem() instanceof TagPrefixItem prefixItem) {
            entry = new UnificationEntry(prefixItem.tagPrefix, prefixItem.material);
        }
        if (entry == null || entry.material == null) {
            return;
        }
        if (!entry.material.hasProperty(PropertyKey.HAZARD)) {
            return;
        }
        IHazardEffectTracker tracker = GTCapabilityHelper.getHazardEffectTracker(player);
        if (tracker == null) {
            return;
        }
        tracker.removeHazardItem(entry.material.getProperty(PropertyKey.HAZARD));
    }
}
