package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.ISpoilableItemStackExtension;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ISpoilableItemStackExtension {

    // ************************* //
    // Shadow fields and methods //
    // ************************* //

    @Shadow
    @Mutable
    @Final
    @Nullable
    private Item item;

    @Shadow(remap = false)
    @Mutable
    @Final
    @Nullable
    private Holder.Reference<Item> delegate;

    @Shadow(remap = false)
    protected abstract void forgeInit();

    // ************* //
    // Unique fields //
    // ************* //

    @Shadow
    @Nullable
    private CompoundTag tag;
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

    /**
     * Whether to display a fake "spoils into" tooltip for an item if it's not spoilable.
     * <br>
     * Has a 10% chance of being {@code true} for any stack on april fools.
     * The chance is rolled once in the constructor (injected by {@link ItemStackMixin#gtceu$injectFakeTooltipInit}).
     */
    @Unique
    private boolean gtceu$fakeTooltip;

    @Unique
    private ItemStack gtceu$self() {
        return (ItemStack) (Object) this;
    }

    // ************************* //
    // Interface implementations //
    // ************************* //

    @Unique
    @Override
    public void gtceu$setStack(ItemStack newStack) {
        item = newStack.getItem();
        delegate = ForgeRegistries.ITEMS.getDelegateOrThrow(item);
        count = newStack.getCount();
        tag = newStack.getTag();
        forgeInit();
    }

    @Unique
    public void gtceu$updateFreshness(@NotNull SpoilContext spoilContext, boolean createTag) {
        if (!gtceu$isUpdating) {
            gtceu$isUpdating = true;
            gtceu$self().getCapability(GTCapability.CAPABILITY_SPOILABLE_ITEM)
                    .ifPresent(spoilable -> spoilable.updateFreshness(spoilContext, createTag));
            gtceu$isUpdating = false;
        }
    }

    // ********* //
    // Injectors //
    // ********* //

    @Inject(at = @At("HEAD"), method = { "getItem", "getCount" })
    private void gtceu$injectedFreshnessUpdate(CallbackInfoReturnable<Item> cir) {
        if (gtceu$self().getEntityRepresentation() != null)
            gtceu$updateFreshness(new SpoilContext(gtceu$self().getEntityRepresentation()), true);
        else gtceu$updateFreshness(new SpoilContext(), false);
    }

    @Inject(at = @At("HEAD"), method = "inventoryTick")
    private void gtceu$tickFreshness(Level level, Entity entity, int inventorySlot, boolean isCurrentItem,
                                     CallbackInfo ci) {
        if (entity instanceof Player player) gtceu$updateFreshness(new SpoilContext(player, inventorySlot), true);
        else gtceu$updateFreshness(new SpoilContext(entity), true);
    }

    @Inject(at = @At("HEAD"), method = "onCraftedBy")
    private void gtceu$updateFreshnessOnCraft(Level level, Player player, int amount, CallbackInfo ci) {
        gtceu$updateFreshness(new SpoilContext(player, -1), true);
    }

    @Inject(at = @At("RETURN"),
            method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/nbt/CompoundTag;)V")
    private void gtceu$injectFakeTooltipInit(ItemLike item, int count, CompoundTag tag, CallbackInfo ci) {
        gtceu$fakeTooltip = GTValues.FOOLS.getAsBoolean() && GTValues.RNG.nextFloat() < .1f;
    }

    /**
     * Allows {@link ISpoilableItem} subclasses that implement {@link IAddInformation} to
     * actually display the added tooltip.
     */
    @Inject(at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"),
            method = "getTooltipLines",
            locals = LocalCapture.CAPTURE_FAILSOFT)
    private void gtceu$spoilageTooltip(Player player, TooltipFlag isAdvanced,
                                       CallbackInfoReturnable<List<Component>> cir,
                                       List<Component> list) {
        ISpoilableItem spoilable = GTCapabilityHelper.getSpoilable(gtceu$self());
        if (spoilable instanceof IAddInformation addInformation) {
            addInformation.appendHoverText(gtceu$self(), player == null ? null : player.level(), list,
                    isAdvanced);
        } else if (gtceu$fakeTooltip) {
            list.add(Component.translatable(
                    "gtceu.tooltip.spoil_time_remaining",
                    Component.literal(FormattingUtil.formatTime(100)).withStyle(ChatFormatting.DARK_AQUA)));
            list.add(Component.translatable(
                    "gtceu.tooltip.spoils_into",
                    Items.DIRT.getDefaultInstance().getDisplayName()));
        }
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
        } else if (gtceu$fakeTooltip) {
            cir.setReturnValue(true);
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
        } else if (gtceu$fakeTooltip) {
            cir.setReturnValue(FastColor.ARGB32.color(255, 255, 255, 255));
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
        } else if (gtceu$fakeTooltip) {
            cir.setReturnValue(13);
        }
    }
}
