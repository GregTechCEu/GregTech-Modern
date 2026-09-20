package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NanoMuscleSuite extends ArmorLogicSuite implements IStepAssist {

    @OnlyIn(Dist.CLIENT)
    protected ArmorUtils.ModularHUD HUD;

    public NanoMuscleSuite(ArmorType slot, int energyPerUse, long maxCapacity, int tier) {
        super(energyPerUse, maxCapacity, tier, slot);
        if (GTCEu.isClientSide() && this.shouldDrawHUD()) {
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
        CompoundTag data = itemStack.getOrCreateTag();

        byte toggleStepTimer = data.getByteOr("toggleStepTimer", (byte) 0);
        if (type == ArmorType.BOOTS) {
            boolean stepAssist = data.contains("stepAssist") && data.getBooleanOr("stepAssist", false);
            if (toggleStepTimer == 0 && SyncedKeyMappings.STEP_ASSIST_ENABLE.isKeyDown(player)) {
                stepAssist = !stepAssist;
                toggleStepTimer = 5;
                if (world.isClientSide()) player.sendOverlayMessage(Component
                        .translatable("metaarmor.nms.step_assist." + (stepAssist ? "enabled" : "disabled")));
                data.putBoolean("stepAssist", stepAssist);
            }

            if (toggleStepTimer > 0) toggleStepTimer--;
            data.putInt("toggleStepTimer", toggleStepTimer);
        }

        if (type == ArmorType.HELMET) {
            byte toggleTimer = data.contains("toggleTimer") ? data.getByte("toggleTimer") : 0;
            int nightVisionTimer = data.contains("nightVisionTimer") ? data.getInt("nightVisionTimer") :
                    ArmorUtils.NIGHTVISION_DURATION;
            boolean nightVision = data.contains("nightVision") && data.getBooleanOr("nightVision", false);
            if (toggleTimer == 0 && SyncedKeyMappings.ARMOR_MODE_SWITCH.isKeyDown(player)) {
                nightVision = !nightVision;
                toggleTimer = 5;
                if (item.getCharge() < ArmorUtils.MIN_NIGHTVISION_CHARGE) {
                    nightVision = false;
                    if (world.isClientSide())
                        player.sendOverlayMessage(Component.translatable("metaarmor.nms.nightvision.error"));
                } else {
                    if (world.isClientSide()) player.sendOverlayMessage(Component
                            .translatable("metaarmor.nms.nightvision." + (nightVision ? "enabled" : "disabled")));
                }
            }

            if (nightVision) {
                player.removeEffect(MobEffects.BLINDNESS);
                if (nightVisionTimer <= ArmorUtils.NIGHT_VISION_RESET) {
                    nightVisionTimer = ArmorUtils.NIGHTVISION_DURATION;
                    player.addEffect(
                            new MobEffectInstance(MobEffects.NIGHT_VISION, ArmorUtils.NIGHTVISION_DURATION, 0, true,
                                    false));
                    item.discharge((4), this.tier, true, false, false);
                }
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            data.putBoolean("nightVision", nightVision);
            if (nightVisionTimer > 0) nightVisionTimer--;
            if (toggleTimer > 0) toggleTimer--;
            data.putInt("nightVisionTimer", nightVisionTimer);
            data.putByte("toggleTimer", toggleTimer);
        }
    }

    public static void disableNightVision(@NotNull Level world, Player player, boolean sendMsg) {
        if (!world.isClientSide()) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            if (sendMsg)
                player.sendOverlayMessage(Component.translatable("metaarmor.nms.nightvision.disabled"));
        }
    }

    public boolean handleUnblockableDamage(LivingEntity entity, @NotNull ItemStack armor, DamageSource source,
                                           double damage, ArmorType equipmentSlot) {
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
    public int damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage,
                           EquipmentSlot equipmentSlot) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item != null) {
            item.discharge(energyPerUse / 10L * damage, item.getTier(), true, false, false);
        }
        return super.damageArmor(entity, itemStack, source, damage, equipmentSlot);
    }

    @Override
    public Identifier getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        ItemStack currentChest = Minecraft.getInstance().player.getInventory()
                .getArmor(ArmorType.CHESTPLATE.getSlot().getIndex());
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
    public void drawHUD(ItemStack item, GuiGraphicsExtractor guiGraphics) {
        addCapacityHUD(item, this.HUD);
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        CompoundTag nbtData = itemStack.getOrCreateTag();
        if (type == ArmorType.HELMET) {

            boolean nv = nbtData.getBooleanOr("nightVision", false);
            if (nv) {
                lines.add(Component.translatable("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(Component.translatable("metaarmor.message.nightvision.disabled"));
            }
        } else if (type == ArmorType.BOOTS) {
            if (nbtData.getBooleanOr("stepAssist", false))
                lines.add(Component.translatable("metaarmor.message.step_assist.enabled"));
            else lines.add(Component.translatable("metaarmor.message.step_assist.disabled"));
            lines.add(Component.translatable("metaarmor.tooltip.falldamage"));
        }
    }
}
