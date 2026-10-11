package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.jetpack.AbstractJetpackLogic;
import com.gregtechceu.gtceu.api.item.jetpack.ElectricJetpackLogic;
import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic;
import com.gregtechceu.gtceu.api.item.jetpack.IJetpackLogic.JetpackStats;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Jetpack extends ArmorLogicSuite {

    @OnlyIn(Dist.CLIENT)
    protected ArmorUtils.ModularHUD HUD;
    protected final AbstractJetpackLogic jetpackLogic;

    public Jetpack(int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, ArmorItem.Type.CHESTPLATE);
        this.jetpackLogic = new ElectricJetpackLogic(getJetpackStats(), getHoverJetpackStats());
        if (GTCEu.isClientSide() && this.shouldDrawHUD()) {
            // noinspection NewExpressionSideOnly
            HUD = new ArmorUtils.ModularHUD();
        }
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        jetpackLogic.addInformation(itemStack, lines);
    }

    protected JetpackStats getJetpackStats() {
        return new JetpackStats(0.0265, 0.3, 0.12, 0.15, 0.006, 1.23,
                getEnergyPerUse(), getEnergyPerUse(),
                ParticleTypes.SMOKE);
    }

    protected @Nullable JetpackStats getHoverJetpackStats() {
        return null;
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/jetpack.png");
    }

    /*
     * @Override
     * public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
     * double damage, ArmorItem.Type equipmentSlot) {
     * return new ArmorProperties(0, 0, 0);
     * }
     */

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(ItemStack item, GuiGraphics guiGraphics, MutableInt drawOffsetY) {
        addCapacityHUD(item, this.HUD);
        jetpackLogic.addHUDLines(item, this.HUD);
        this.HUD.draw(guiGraphics, drawOffsetY);
        this.HUD.reset();
    }

    @Override
    public IJetpackLogic getJetpackLogic(ItemStack stack) {
        return jetpackLogic;
    }
}
