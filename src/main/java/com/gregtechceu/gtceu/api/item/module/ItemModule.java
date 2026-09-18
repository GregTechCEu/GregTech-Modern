package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemModule {

    @Getter
    private final ResourceLocation id;

    public ItemModule(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ItemModule[%s]".formatted(id);
    }

    public abstract Component getInfo();

    public void onAttach(ModuleData moduleData) {}

    public void onRemove(ModuleData moduleData) {}

    public void onEquip(LivingEntity entity, ModuleData module) {}

    public void onArmorTick(LivingEntity entity, ModuleData module) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        long energy = energyUsagePerTick(entity, module);
        if (electricItem != null) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    public void onUnequip(LivingEntity entity, ModuleData module) {}

    /**
     * Called each tick this item is in a player's inventory or equipment slots
     */
    public void onInventoryTick(Player player, ModuleData module) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getAppliedTo());
        long energy = energyUsagePerTick(player, module);
        if (electricItem != null && useEnergyInInventory(player, module)) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    /**
     * @return name displayed in the modules UI
     */
    public Component getDisplayName(ModuleData module) {
        List<Component> list = new ArrayList<>();
        appendHoverText(null, TooltipFlag.NORMAL, list, module);
        return list.isEmpty() ? Component.empty() : list.get(0);
    }

    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                ModuleData module) {}

    public boolean useEnergyInInventory(LivingEntity entity, ModuleData module) {
        return true;
    }

    public long energyUsagePerTick(LivingEntity entity, ModuleData module) {
        return 0;
    }

    public float changeDamage(LivingEntity entity, ModuleData modifier, float damage, DamageSource source) {
        return damage;
    }

    public boolean canRemove(ModuleData module) {
        return true;
    }

    public boolean isPPE(ModuleData module) {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        return modularItem != null && modularItem.getModuleData(this) == null;
    }

    public boolean isEnabled(ModuleData module) {
        if (!module.getTag().contains("enabled")) {
            setEnabled(module, true);
        }
        return module.getTag().getBoolean("enabled");
    }

    public void setEnabled(ModuleData module, boolean enabled) {
        module.getTag().putBoolean("enabled", enabled);
    }

    /**
     * If {@code true}, converts any null NBT tag into an empty tag when
     * a module item is set for this module.
     * Basically ensures that {@code appliedItemModule.getModuleItem().getTag()} is never {@code null}.
     * Required for items that may have their NBT modified when in the module, such as batteries.
     */
    public boolean forceModuleItemNBT() {
        return false;
    }

    /**
     * Called when the item this module is attached to is ticked,
     * ignores {@link #isEnabled(ModuleData)}.
     *
     * @param entity the entity in which the item is, {@code null} if the item is not in one
     * @param pos    the position of the block in which the item is, {@code null} if the item is not in one
     */
    public void onTickRaw(ModuleData module, @Nullable Entity entity, Level level,
                          @Nullable BlockPos pos) {}

    public InteractionResultHolder<ItemStack> use(ModuleData module, Level level, Player player,
                                                  InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public InteractionResult useOn(ModuleData module, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult onItemUseFirst(ModuleData module, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntity(ModuleData module, Player player,
                                                  LivingEntity interactionTarget, InteractionHand usedHand) {
        return InteractionResult.PASS;
    }

    public ItemModuleSettingsBuilder getSettings(ModuleData module, PanelSyncManager psm, int id) {
        psm.syncValue("module_enabled", id,
                SyncHandlers.intNumber(() -> isEnabled(module) ? 0 : 1, x -> setEnabled(module, x == 0)));
        ItemModuleSettingsBuilder settings = new ItemModuleSettingsBuilder(psm, id);
        return settings
                .bool(Text.lang("gtceu.module.gui.enabled"), () -> isEnabled(module), b -> setEnabled(module, b));
    }
}
