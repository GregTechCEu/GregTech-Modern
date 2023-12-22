package com.gregtechceu.gtceu.common.item.armor;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.armor.ArmorLogicSuite;
import gregtech.api.items.armor.ArmorUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class Jetpack extends ArmorLogicSuite implements IJetpack {

    protected ArmorUtils.ModularHUD HUD;

    //client
    public Jetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, EntityEquipmentSlot.CHEST);
        if (ArmorUtils.SIDE.isClient() && this.shouldDrawHUD()) {
            //noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
        }
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, @Nonnull ItemStack stack) {
        NBTTagCompound data = GTUtility.getOrCreateNbtCompound(stack);
        byte toggleTimer = 0;
        boolean hover = false;
        if (data.hasKey("toggleTimer")) toggleTimer = data.getByte("toggleTimer");
        if (data.hasKey("hover")) hover = data.getBoolean("hover");

        if (toggleTimer == 0 && KeyBind.ARMOR_HOVER.isKeyDown(player)) {
            hover = !hover;
            toggleTimer = 5;
            data.setBoolean("hover", hover);
            if (!world.isRemote) {
                if (hover)
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.enable"), true);
                else
                    player.sendStatusMessage(new TextComponentTranslation("metaarmor.jetpack.hover.disable"), true);
            }
        }

        performFlying(player, hover, stack);

        if (toggleTimer > 0) toggleTimer--;

        data.setBoolean("hover", hover);
        data.setByte("toggleTimer", toggleTimer);
        player.inventoryContainer.detectAndSendChanges();
    }

    @Override
    public boolean canUseEnergy(@Nonnull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.canUse(amount);
    }

    @Override
    public void drainEnergy(@Nonnull ItemStack stack, int amount) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return;
        container.discharge(amount, tier, true, false, false);
    }

    @Override
    public boolean hasEnergy(@Nonnull ItemStack stack) {
        IElectricItem container = getIElectricItem(stack);
        if (container == null)
            return false;
        return container.getCharge() > 0;
    }

    private static IElectricItem getIElectricItem(@Nonnull ItemStack stack) {
        return stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/jetpack.png";
    }

    @Override
    public ArmorProperties getProperties(EntityLivingBase player, @Nonnull ItemStack armor, DamageSource source, double damage, EntityEquipmentSlot equipmentSlot) {
        return new ArmorProperties(0, 0, 0);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawHUD(ItemStack item) {
        addCapacityHUD(item, this.HUD);
        NBTTagCompound data = item.getTagCompound();
        if (data != null) {
            if (data.hasKey("hover")) {
                String status = (data.getBoolean("hover") ? I18n.format("metaarmor.hud.status.enabled") : I18n.format("metaarmor.hud.status.disabled"));
                String result = I18n.format("metaarmor.hud.hover_mode", status);
                this.HUD.newString(result);
            }
        }
        this.HUD.draw();
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<String> lines) {
        super.addInfo(itemStack, lines);
        NBTTagCompound data = itemStack.getTagCompound();
        if (data != null) {
            String status = I18n.format("metaarmor.hud.status.disabled");
            if (data.hasKey("hover")) {
                if (data.getBoolean("hover"))
                    status = I18n.format("metaarmor.hud.status.enabled");
            }
            lines.add(I18n.format("metaarmor.hud.hover_mode", status));
        }
    }

    @Override
    public double getVerticalHoverSlowSpeed() {
        return 0.1D;
    }

    @Override
    public double getVerticalAcceleration() {
        return 0.12D;
    }

    @Override
    public double getVerticalSpeed() {
        return 0.3D;
    }

    @Override
    public double getSidewaysSpeed() {
        return 0.08D;
    }

    @Override
    public EnumParticleTypes getParticle() {
        return EnumParticleTypes.SMOKE_NORMAL;
    }
}
