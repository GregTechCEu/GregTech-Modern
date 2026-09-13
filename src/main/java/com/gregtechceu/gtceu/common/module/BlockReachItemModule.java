package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.ItemModuleType;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;
import com.gregtechceu.gtceu.common.data.GTItemModules;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.UUID;

public class BlockReachItemModule extends TieredAttributeItemModule {

    private static final UUID ADD_BLOCK_REACH_UUID = UUID.fromString("c5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    // spotless:off
    public static final Codec<BlockReachItemModule> CODEC = tieredAttributeCodec(BlockReachItemModule::new);
    //spotless:on

    public BlockReachItemModule(boolean isEnabled, ItemStack moduleItem, int tier, double modifierAmount) {
        super(isEnabled, moduleItem, tier, modifierAmount);
    }

    public BlockReachItemModule(ItemStack moduleItem, int tier) {
        super(moduleItem, tier);
    }

    @Override
    public ItemModuleType<BlockReachItemModule> type() {
        return GTItemModules.BLOCK_REACH[getTier()];
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.block_reach", getTier() / 2d);
    }

    @Override
    public Attribute getAttribute() {
        return ForgeMod.BLOCK_REACH.get();
    }

    @Override
    public AttributeModifier getAttributeModifier() {
        double add = getTier() / 2d;
        return new AttributeModifier(ADD_BLOCK_REACH_UUID, "Block Reach Modifier", add,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(level, isAdvanced, tooltips);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.block_reach",
                GTValues.VNF[getTier()]));
    }
}
