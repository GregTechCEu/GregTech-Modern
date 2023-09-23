package com.gregtechceu.gtceu.api.item.armor;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.IItemHUDProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class ArmorLogicSuite implements IArmorLogic, IItemHUDProvider {

    protected final int energyPerUse;
    protected final int tier;
    protected final long maxCapacity;
    protected final EquipmentSlot slot;

    protected ArmorLogicSuite(int energyPerUse, long maxCapacity, int tier, EquipmentSlot slot) {
        this.energyPerUse = energyPerUse;
        this.maxCapacity = maxCapacity;
        this.tier = tier;
        this.slot = slot;
    }

    @Override
    public abstract void onArmorTick(Level Level, Entity player, ItemStack itemStack);

    /*
    @Override
    public int getArmorDisplay(Player player, ItemStack armor, int slot) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(armor);
        if (item == null) return 0;
        if (item.getCharge() >= energyPerUse) {
            return (int) Math.round(20.0F * this.getAbsorption(armor) * this.getDamageAbsorption());
        } else {
            return (int) Math.round(4.0F * this.getAbsorption(armor) * this.getDamageAbsorption());
        }
    }
     */

    @Override
    public void addToolComponents(ArmorComponentItem mvi) {
        mvi.attachComponents(new ElectricStats(maxCapacity, tier, true, false) {

            @Override
            public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand usedHand) {
                return onRightClick(level, player, usedHand);
            }

            @Override
            public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
                addInfo(stack, tooltipComponents);
            }
        });
    }

    public void addInfo(ItemStack itemStack, List<Component> lines) {
        int armor = (int) Math.round(20.0F * this.getAbsorption() * this.getDamageAbsorption());
        if (armor > 0)
            lines.add(Component.translatable("attribute.modifier.plus.0", armor, Component.translatable("attribute.name.generic.armor")));
    }

    public InteractionResultHolder<ItemStack> onRightClick(Level Level, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof ArmorComponentItem) {
            ItemStack armor = player.getItemInHand(hand);
            if (armor.getItem() instanceof ArmorComponentItem && player.getInventory().armor.get(slot.getIndex()).isEmpty() && !player.isCrouching()) {
                player.getInventory().armor.set(slot.getIndex(), armor.copy());
                player.setItemInHand(hand, ItemStack.EMPTY);
                player.playSound(SoundEvents.ARMOR_EQUIP_GENERIC);
                return InteractionResultHolder.success(armor);
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return slot;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "";
    }

    public double getDamageAbsorption() {
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    protected static void addCapacityHUD(ItemStack stack, ArmorUtils.ModularHUD hud) {
        IElectricItem cont = GTCapabilityHelper.getElectricItem(stack);
        if (cont == null) return;
        if (cont.getCharge() == 0) return;
        float energyMultiplier = cont.getCharge() * 100.0F / cont.getMaxCharge();
        hud.newString(Component.translatable("metaarmor.hud.energy_lvl", String.format("%.1f", energyMultiplier) + "%"));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldDrawHUD() {
        return this.slot == EquipmentSlot.CHEST;
    }

    public int getEnergyPerUse() {
        return this.energyPerUse;
    }

    protected float getAbsorption() {
        return switch (this.getEquipmentSlot()) {
            case HEAD, FEET ->
                    0.15F;
            case CHEST ->
                    0.4F;
            case LEGS ->
                    0.3F;
            default ->
                    0.0F;
        };
    }
}
