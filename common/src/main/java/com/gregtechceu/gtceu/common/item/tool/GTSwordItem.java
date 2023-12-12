package com.gregtechceu.gtceu.common.item.tool;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Supplier;

public class GTSwordItem extends SwordItem implements IGTTool {
    public GTSwordItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public void setToolDefinition(IGTToolDefinition definition) {

    }

    @Override
    public Material getMaterial() {
        return null;
    }

    @Override
    public boolean isElectric() {
        return false;
    }

    @Override
    public int getElectricTier() {
        return 0;
    }

    @Override
    public IGTToolDefinition getToolStats() {
        return null;
    }

    @Nullable
    @Override
    public SoundEntry getSound() {
        return null;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return false;
    }

    @Nullable
    @Override
    public Supplier<ItemStack> getMarkerItem() {
        return null;
    }

    @Override
    public Set<GTToolType> getToolClasses(ItemStack stack) {
        return null;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return null;
    }
}
