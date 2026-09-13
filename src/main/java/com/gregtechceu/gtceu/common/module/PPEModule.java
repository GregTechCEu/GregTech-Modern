package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

import com.mojang.serialization.Codec;

import java.util.List;

public class PPEModule extends ItemModule {

    public static final Codec<PPEModule> CODEC = simpleCodec(PPEModule::new);

    public PPEModule(boolean isEnabled, ItemStack moduleItem) {
        super(isEnabled, moduleItem);
    }

    public PPEModule(ItemStack moduleItem) {
        super(moduleItem);
    }

    @Override
    public ItemModuleType<PPEModule> type() {
        return GTItemModules.PPE;
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.ppe");
    }

    @Override
    public boolean isPPE() {
        return true;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        if (getAppliedTo().is(Tags.Items.ARMORS_BOOTS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_boots"));
        if (getAppliedTo().is(Tags.Items.ARMORS_LEGGINGS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_leggings"));
        if (getAppliedTo().is(Tags.Items.ARMORS_CHESTPLATES))
            tooltips.add(Component.translatable("item.gtceu.hazmat_chestpiece"));
        if (getAppliedTo().is(Tags.Items.ARMORS_HELMETS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_headpiece"));
    }
}
