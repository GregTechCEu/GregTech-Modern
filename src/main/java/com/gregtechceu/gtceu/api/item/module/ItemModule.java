package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public abstract class ItemModule {

    @SuppressWarnings("unchecked")
    public static final Codec<ItemModule> CODEC = GTRegistries.ITEM_MODULES.codec()
            .dispatch(ItemModule::type, ItemModuleType::codec);

    // spotless:off
    protected static <M extends ItemModule> Products.P2<RecordCodecBuilder.Mu<M>, Boolean, ItemStack> baseCodec(RecordCodecBuilder.Instance<M> instance) {
        return instance.group(Codec.BOOL.fieldOf("enabled").forGetter(ItemModule::isEnabled),
                ItemStack.CODEC.fieldOf("module_item").forGetter(ItemModule::getModuleItem));
    }

    protected static <M extends ItemModule> Codec<M> simpleCodec(BiFunction<Boolean, ItemStack, M> function) {
        return RecordCodecBuilder.create(instance -> baseCodec(instance).apply(instance, function));
    }
    // spotless:on

    @Getter
    private boolean enabled;

    @Getter
    private final ItemStack moduleItem;

    /**
     * The stack that this module is attached to.
     * If this module is not applied to anything, this field is {@code null}.
     */
    @Getter
    @Setter
    private ItemStack appliedTo;
    /**
     * The {@link IModularItem} capability this module is attached to.
     */
    @Getter
    @Setter
    private IModularItem modularItemStack;

    public ItemModule(boolean isEnabled, ItemStack moduleItem) {
        this.enabled = isEnabled;
        this.moduleItem = moduleItem;
    }

    public ItemModule(ItemStack moduleItem) {
        this.enabled = true;
        this.moduleItem = moduleItem;
    }

    public abstract ItemModuleType type();

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        getModularItemStack().saveModuleData();
    }

    public abstract Component getInfo();

    public void onAttach() {}

    public void onRemove() {}

    public void onEquip(LivingEntity entity) {}

    public void onArmorTick(LivingEntity entity) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        long energy = energyUsagePerTick(entity);
        if (electricItem != null) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    public void onUnequip(LivingEntity entity) {}

    /**
     * Called each tick this item is in a player's inventory or equipment slots
     */
    public void onInventoryTick(Player player) {
        if (getModuleItem() == null) return;
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(getAppliedTo());
        long energy = energyUsagePerTick(player);
        if (electricItem != null && useEnergyInInventory(player)) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    /**
     * @return name displayed in the modules UI
     */
    public Component getDisplayName() {
        List<Component> list = new ArrayList<>();
        appendHoverText(null, TooltipFlag.NORMAL, list);
        return list.isEmpty() ? Component.empty() : list.get(0);
    }

    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {}

    public boolean useEnergyInInventory(LivingEntity entity) {
        return true;
    }

    public long energyUsagePerTick(LivingEntity entity) {
        return 0;
    }

    public float changeDamage(LivingEntity entity, float damage, DamageSource source) {
        return damage;
    }

    public boolean canRemove() {
        return true;
    }

    public boolean isPPE() {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        return modularItem != null;
    }

    /**
     * Called when the item this module is attached to is ticked,
     * ignores {@link #isEnabled()}.
     *
     * @param entity the entity in which the item is, {@code null} if the item is not in one
     * @param pos    the position of the block in which the item is, {@code null} if the item is not in one
     */
    public void onTickRaw(@Nullable Entity entity, @NotNull Level level,
                          @Nullable BlockPos pos) {}

    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult onItemUseFirst(UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntity(Player player,
                                                  LivingEntity interactionTarget, InteractionHand usedHand) {
        return InteractionResult.PASS;
    }

    public ItemModuleSettingsBuilder getSettings(PanelSyncManager psm, int id) {
        psm.syncValue("module_enabled", id,
                SyncHandlers.intNumber(() -> isEnabled() ? 0 : 1, x -> setEnabled(x == 0)));
        ItemModuleSettingsBuilder settings = new ItemModuleSettingsBuilder(psm, id);
        return settings
                .bool(Text.lang("gtceu.module.gui.enabled"), this::isEnabled, this::setEnabled);
    }
}
