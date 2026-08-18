package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.capability.ModularItemStack;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AppliedItemModule {

    private final CompoundTag moduleTag;
    @Getter
    private final int slot;

    @Getter
    private ItemModule module;

    @Getter
    private CompoundTag tag;

    @Getter
    private ItemStack moduleItem;

    /**
     * The stack that this module is applied to.
     * If this module is not applied to anything, this field is {@code null}.
     */
    @Getter
    @Setter
    private ItemStack appliedTo;

    /**
     * Create an applied module and fill the provided tag with module data
     */
    public AppliedItemModule(CompoundTag moduleTag, ItemModule module, int slot) {
        this.moduleTag = moduleTag;
        this.moduleTag.put("module", module.serializeNBT());
        this.moduleTag.put("tag", new CompoundTag());
        this.module = module;
        this.tag = this.moduleTag.getCompound("tag");
        this.slot = slot;
    }

    /**
     * Load an applied module from an existing module tag
     */
    public AppliedItemModule(CompoundTag moduleTag, @Nullable ItemStack appliedTo, int slot) {
        this.moduleTag = moduleTag;
        if (!this.moduleTag.contains("module")) {
            GTCEu.LOGGER.warn("Created an AppliedItemModule from an invalid tag, module will be null!");
        }
        this.module = ItemModule.fromNBT(this.moduleTag.getCompound("module"));
        if (!this.moduleTag.contains("tag")) this.moduleTag.put("tag", new CompoundTag());
        this.tag = this.moduleTag.getCompound("tag");
        this.slot = slot;
        if (this.moduleTag.contains("item")) {
            this.moduleItem = ItemStack.of(this.moduleTag.getCompound("item"));
        }
        this.appliedTo = appliedTo;
    }

    public void setModule(ItemModule module) {
        this.moduleTag.put("module", module.serializeNBT());
        this.module = module;
    }

    public void setModuleItem(ItemStack stack) {
        if (module.forceModuleItemNBT())
            stack.getOrCreateTag();
        this.moduleItem = stack;
        this.moduleTag.put("item", stack.serializeNBT());
    }

    public void setTag(CompoundTag tag) {
        this.moduleTag.put("tag", tag);
        this.tag = tag;
    }

    public void detach() {
        if (this.appliedTo == null || !this.module.canRemove(this)) return;
        this.module.onRemove(this);
        this.appliedTo.getOrCreateTagElement(ModularItemStack.MODULES_TAG).remove(String.valueOf(slot));
        this.appliedTo = null;
    }

    public void inventoryTick(Player player) {
        if (this.isEnabled()) this.module.onInventoryTick(player, this);
        this.module.onTickRaw(this, player, player.level(), null);
    }

    public void armorTick(@NotNull LivingEntity entity) {
        if (this.isEnabled()) this.module.onArmorTick(entity, this);
    }

    public void tick(@NotNull Level level, @Nullable BlockPos pos) {
        this.module.onTickRaw(this, null, level, pos);
    }

    public void equip(@NotNull LivingEntity entity) {
        this.module.onEquip(entity, this);
    }

    public void unequip(@NotNull LivingEntity entity) {
        this.module.onUnequip(entity, this);
    }

    public float changeDamage(LivingEntity entity, float damage, DamageSource source) {
        return this.isEnabled() ? this.module.changeDamage(entity, this, damage, source) : damage;
    }

    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        this.module.appendHoverText(level, isAdvanced, tooltips, this);
    }

    public boolean isEnabled() {
        return this.module.isEnabled(this);
    }

    public void setEnabled(boolean enabled) {
        this.module.setEnabled(this, enabled);
    }

    public boolean isPPE() {
        return this.module.isPPE(this) && this.isEnabled();
    }

    public boolean canRemove() {
        return this.module.canRemove(this);
    }
}
