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
import com.mojang.serialization.Codec;
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

    /**
     * A codec used to serialise/deserialise persistent data for this module.<br>
     */
    public Codec<? extends ModuleData> moduleDataCodec() {
        return ModuleData.BASE_CODEC;
    }

    /**
     * The class for the data object which this module uses.<br>
     */
    public Class<? extends ModuleData> moduleDataClass() {
        return ModuleData.BaseData.class;
    }

    /**
     * Creates the default module data for this module.
     */
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new ModuleData.BaseData(slot, module, moduleStack, true);
    }

    public String getLanguageKey() {
        return id.toLanguageKey("module");
    }

    public String getDescriptionLanguageKey() {
        return id.toLanguageKey("module", "description");
    }

    public boolean isEnabled(ModuleContext moduleContext) {
        return moduleContext.getData().isEnabled();
    }

    public void setEnabled(ModuleContext moduleContext, boolean enabled) {
        moduleContext.setData(moduleContext.getData().withEnabled(enabled));
    }

    @Override
    public String toString() {
        return "ItemModule[%s]".formatted(id);
    }

    public abstract Component getInfo();

    public void onAttach(ModuleContext moduleContext) {}

    public void onRemove(ModuleContext moduleContext) {}

    public void onEquip(ModuleContext moduleContext, LivingEntity entity) {}

    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        long energy = energyUsagePerTick(moduleContext, entity);
        if (electricItem != null) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    public void onUnequip(ModuleContext moduleContext, LivingEntity entity) {}

    /**
     * Called each tick this item is in a player's inventory or equipment slots
     */
    public void onInventoryTick(ModuleContext moduleContext, Player player) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(moduleContext.getAppliedTo());
        long energy = energyUsagePerTick(moduleContext, player);
        if (electricItem != null && useEnergyInInventory(moduleContext, player)) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    /**
     * @return name displayed in the modules UI
     */
    public Component getDisplayName(ModuleContext moduleContext) {
        List<Component> list = new ArrayList<>();
        appendHoverText(moduleContext, null, list, TooltipFlag.NORMAL);
        return list.isEmpty() ? Component.empty() : list.get(0);
    }

    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        tooltips.add(Component.translatable(getLanguageKey()));
    }

    public boolean useEnergyInInventory(ModuleContext moduleContext, LivingEntity entity) {
        return true;
    }

    public long energyUsagePerTick(ModuleContext moduleContext, LivingEntity entity) {
        return 0;
    }

    public float changeDamage(ModuleContext moduleContext, LivingEntity entity, float damage, DamageSource source) {
        return damage;
    }

    public boolean canRemove(ModuleContext moduleContext) {
        return true;
    }

    public boolean isPPE(ModuleContext moduleContext) {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        return modularItem != null && modularItem.getModuleContext(this) == null;
    }

    /**
     * Called when the item this module is attached to is ticked,
     * ignores {@link #isEnabled(ModuleContext)}.
     *
     * @param entity the entity in which the item is, {@code null} if the item is not in one
     * @param pos    the position of the block in which the item is, {@code null} if the item is not in one
     */
    public void onTickRaw(ModuleContext moduleContext, @Nullable Entity entity, Level level,
                          @Nullable BlockPos pos) {}

    public InteractionResultHolder<ItemStack> use(ModuleContext moduleContext, Level level, Player player,
                                                  InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public InteractionResult useOn(ModuleContext moduleContext, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult onItemUseFirst(ModuleContext moduleContext, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntity(ModuleContext moduleContext, Player player,
                                                  LivingEntity interactionTarget, InteractionHand usedHand) {
        return InteractionResult.PASS;
    }

    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        psm.syncValue("module_enabled", id,
                SyncHandlers.intNumber(() -> isEnabled(moduleContext) ? 0 : 1, x -> setEnabled(moduleContext, x == 0)));
        ItemModuleSettingsBuilder settings = new ItemModuleSettingsBuilder(psm, id);
        return settings
                .bool(Text.lang("gui.gtceu.item_module.enabled"), () -> isEnabled(moduleContext),
                        b -> setEnabled(moduleContext, b));
    }
}
