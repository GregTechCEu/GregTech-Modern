package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NanoMuscleSuite extends ArmorLogicSuite implements IStepAssist {

    @OnlyIn(Dist.CLIENT)
    protected ArmorUtils.ModularHUD HUD;

    public NanoMuscleSuite(ArmorItem.Type slot, int energyPerUse, long maxCapacity, int tier) {
        super(energyPerUse, maxCapacity, tier, slot);
        if (Platform.isClient() && this.shouldDrawHUD()) {
            // noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
        }
    }

    @Override
    public void onArmorTick(Level world, Player player, ItemStack itemStack) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item == null) {
            return;
        }
        GTArmor data = itemStack.getOrDefault(GTDataComponents.ARMOR_DATA, new GTArmor());
        byte toggleTimer = data.toggleTimer();
        int nightVisionTimer = data.nightVisionTimer();
        if (type == ArmorItem.Type.HELMET) {
            boolean nightVision = data.nightVision();
            if (toggleTimer == 0 && KeyBind.ARMOR_MODE_SWITCH.isKeyDown(player)) {
                nightVision = !nightVision;
                toggleTimer = 5;
                if (item.getCharge() < ArmorUtils.MIN_NIGHTVISION_CHARGE) {
                    nightVision = false;
                    player.displayClientMessage(Component.translatable("metaarmor.nms.nightvision.error"), true);
                } else {
                    player.displayClientMessage(Component
                            .translatable("metaarmor.nms.nightvision." + (nightVision ? "enabled" : "disabled")), true);
                }
            }

            if (nightVision) {
                player.removeEffect(MobEffects.BLINDNESS);
                if (nightVisionTimer <= ArmorUtils.NIGHT_VISION_RESET) {
                    nightVisionTimer = ArmorUtils.NIGHTVISION_DURATION;
                    player.addEffect(
                            new MobEffectInstance(MobEffects.NIGHT_VISION, ArmorUtils.NIGHTVISION_DURATION, 0, true,
                                    false));
                    item.discharge(4, this.tier, true, false, false);
                }
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            final boolean finalNightvision = nightVision;
            itemStack.update(GTDataComponents.ARMOR_DATA, new GTArmor(),
                    data1 -> data1.setNightVision(finalNightvision));
        } else if (type == ArmorItem.Type.BOOTS) {
            updateStepHeight(player);
        }

        if (nightVisionTimer > 0) nightVisionTimer--;
        if (toggleTimer > 0) toggleTimer--;

        final int finalNightVisionTimer = nightVisionTimer;
        final byte finalToggleTimer = toggleTimer;
        itemStack.update(GTDataComponents.ARMOR_DATA, new GTArmor(),
                data1 -> data1.setNightVisionTimer(finalNightVisionTimer)
                        .setToggleTimer(finalToggleTimer));
    }

    public static void disableNightVision(@NotNull Level world, Player player, boolean sendMsg) {
        if (!world.isClientSide) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            if (sendMsg)
                player.displayClientMessage(Component.translatable("metaarmor.nms.nightvision.disabled"), true);
        }
    }

    public boolean handleUnblockableDamage(LivingEntity entity, @NotNull ItemStack armor, DamageSource source,
                                           double damage, ArmorItem.Type equipmentSlot) {
        return source.is(DamageTypes.FALL);
    }

    /*
     * @Override
     * public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
     * double damage, ArmorItem.Type equipmentSlot) {
     * IElectricItem container = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
     * int damageLimit = Integer.MAX_VALUE;
     * if (source == DamageSource.FALL && this.getEquipmentSlot(armor) == ArmorItem.Type.FEET) {
     * if (energyPerUse > 0 && container != null) {
     * damageLimit = (int) Math.min(damageLimit, 25.0 * container.getCharge() / (energyPerUse * 10.0D));
     * }
     * return new ArmorProperties(10, (damage < 8.0) ? 1.0 : 0.875, damageLimit);
     * }
     * return super.getProperties(player, armor, source, damage, equipmentSlot);
     * }
     */

    @Override
    public void damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item != null) {
            item.discharge((long) energyPerUse / 10 * damage, item.getTier(), true, false, false);
        }
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
        ItemStack currentChest = Minecraft.getInstance().player.getInventory()
                .getArmor(ArmorItem.Type.CHESTPLATE.getSlot().getIndex());
        ItemStack advancedChest = GTItems.NANO_CHESTPLATE_ADVANCED.asStack();
        String armorTexture = "nano_muscule_suite";
        if (advancedChest.is(currentChest.getItem())) armorTexture = "advanced_nano_muscle_suite";
        return slot != EquipmentSlot.LEGS ?
                GTCEu.id(String.format("textures/armor/%s_1.png", armorTexture)) :
                GTCEu.id(String.format("textures/armor/%s_2.png", armorTexture));
    }

    @Override
    public double getDamageAbsorption() {
        return 1.0D;
    }

    @Override
    public float getHeatResistance() {
        return 0.75f;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(ItemStack item, GuiGraphics guiGraphics) {
        addCapacityHUD(item, this.HUD);
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        if (type == ArmorItem.Type.HELMET) {
            GTArmor data = itemStack.getOrDefault(GTDataComponents.ARMOR_DATA, new GTArmor());
            boolean nv = data.nightVision();
            if (nv) {
                lines.add(Component.translatable("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(Component.translatable("metaarmor.message.nightvision.disabled"));
            }
        } else if (type == ArmorItem.Type.BOOTS) {
            lines.add(Component.translatable("metaarmor.tooltip.stepassist"));
            lines.add(Component.translatable("metaarmor.tooltip.falldamage"));
        }
    }
}
