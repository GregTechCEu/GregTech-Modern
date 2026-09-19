package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IMergeableDataComponent;
import com.gregtechceu.gtceu.api.item.ISpoilableItemStackExtension;
import com.gregtechceu.gtceu.api.item.component.*;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ISpoilableItemStackExtension {

    @Shadow
    @Mutable
    @Final
    @Nullable
    private Item item;

    @Mutable
    @Final
    @Shadow
    @Nullable
    PatchedDataComponentMap components;

    @Shadow
    private int count;
    /**
     * Whether {@link ItemStackMixin#gtceu$updateFreshness(SpoilContext, boolean)}
     * was called and did not return yet.
     * <br>
     * Used to prevent stack overflows.
     */
    @Unique
    private boolean gtceu$isUpdating = false;

    @Unique
    private ItemStack gtceu$self() {
        return (ItemStack) (Object) this;
    }

    // ************************* //
    // Interface implementations //
    // ************************* //

    @Unique
    @Override
    public void gtceu$forceContentTo(ItemStack newStack) {
        this.item = newStack.getItem();
        this.count = newStack.getCount();
        this.components = new PatchedDataComponentMap(newStack.getComponents());
    }

    @Unique
    private void gtceu$updateFreshness(@NotNull SpoilContext spoilContext, boolean createTag) {
        if (!gtceu$isUpdating) {
            gtceu$isUpdating = true;
            ISpoilableItem spoilable = gtceu$self().getCapability(GTCapability.CAPABILITY_SPOILABLE_ITEM);
            if (spoilable != null)
                spoilable.updateFreshness(spoilContext, createTag);
            gtceu$isUpdating = false;
        }
    }

    // ********* //
    // Injectors //
    // ********* //

    @Inject(at = @At("HEAD"), method = { "getItem", "getCount" })
    private void gtceu$injectedFreshnessUpdate(CallbackInfoReturnable<Item> cir) {
        if (gtceu$self().getEntityRepresentation() != null)
            gtceu$updateFreshness(new SpoilContext(gtceu$self().getEntityRepresentation(), 0), true);
        else gtceu$updateFreshness(new SpoilContext(), false);
    }

    @Inject(at = @At("HEAD"), method = "inventoryTick")
    private void gtceu$tickFreshness(Level level, Entity entity, int inventorySlot, boolean isCurrentItem,
                                     CallbackInfo ci) {
        if (entity instanceof Player player) gtceu$updateFreshness(new SpoilContext(player, inventorySlot), true);
        else gtceu$updateFreshness(new SpoilContext(entity, 0), true);
    }

    @Inject(at = @At("HEAD"), method = "onCraftedBy")
    private void gtceu$updateFreshnessOnCraft(Level level, Player player, int amount, CallbackInfo ci) {
        gtceu$updateFreshness(new SpoilContext(player, -1), true);
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IDurabilityBar} to
     * actually display the bar.
     */
    @Inject(at = @At("HEAD"), method = "isBarVisible", cancellable = true)
    private void gtceu$spoilageBarVisible(CallbackInfoReturnable<Boolean> cir) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IDurabilityBar durabilityBar) {
            cir.setReturnValue(durabilityBar.isBarVisible(gtceu$self()));
        }
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IDurabilityBar} to
     * actually display the bar.
     */
    @Inject(at = @At("HEAD"), method = "getBarColor", cancellable = true)
    private void gtceu$spoilageBarColor(CallbackInfoReturnable<Integer> cir) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IDurabilityBar durabilityBar) {
            cir.setReturnValue(durabilityBar.getBarColor(gtceu$self()));
        }
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IDurabilityBar} to
     * actually display the bar.
     */
    @Inject(at = @At("HEAD"), method = "getBarWidth", cancellable = true)
    private void gtceu$spoilageBarWidth(CallbackInfoReturnable<Integer> cir) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IDurabilityBar durabilityBar) {
            cir.setReturnValue(durabilityBar.getBarWidth(gtceu$self()));
        }
    }

    /**
     * Allows all data components that are instances of {@link IMergeableDataComponent} to run their code before
     * comparison.
     * 
     * @implNote the performance impact should be negligible as usually items don't have over 20 data components, and
     *           casting is O(1)
     */
    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z"),
            method = "isSameItemSameComponents")
    private static void gtceu$injectComponentEqualizer(ItemStack stack, ItemStack other,
                                                       CallbackInfoReturnable<Boolean> cir) {
        stack.getComponents().stream()
                .map(TypedDataComponent::value)
                .filter(IMergeableDataComponent.class::isInstance)
                .map(IMergeableDataComponent.class::cast)
                .forEach(mergeable -> mergeable.prepareForComparisonWith(stack, other));
        if (other != null)
            other.getComponents().stream()
                    .map(TypedDataComponent::value)
                    .filter(IMergeableDataComponent.class::isInstance)
                    .map(IMergeableDataComponent.class::cast)
                    .forEach(mergeable -> mergeable.prepareForComparisonWith(other, stack));
    }
}
