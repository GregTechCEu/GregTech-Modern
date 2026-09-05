package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.TieredAttributeItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;

import java.util.List;
import java.util.UUID;

public class BlockReachItemModule extends TieredAttributeItemModule {

    private static final UUID ADD_BLOCK_REACH_UUID = UUID.fromString("c5bd81ea-b3af-4cca-8866-f3e62f5f68f1");

    public BlockReachItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.block_reach", getTier() / 2d);
    }

    @Override
    public Attribute getAttribute(AppliedItemModule module) {
        return ForgeMod.BLOCK_REACH.get();
    }

    @Override
    public AttributeModifier getAttributeModifier(AppliedItemModule module) {
        double add = getTier() / 2d;
        return new AttributeModifier(ADD_BLOCK_REACH_UUID, "Block Reach Modifier", add,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        tooltips.add(Component.translatable("metaarmor.tooltip.modifier.block_reach",
                GTValues.VNF[getTier()]));
    }
}
