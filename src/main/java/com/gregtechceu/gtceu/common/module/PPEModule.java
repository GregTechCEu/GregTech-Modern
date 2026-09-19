package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class PPEModule extends ItemModule {

    public PPEModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Component getInfo() {
        return Component.translatable("gtceu.module.ppe");
    }

    @Override
    public boolean isPPE(ModuleContext moduleContext) {
        return super.isEnabled(moduleContext);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, TooltipFlag isAdvanced,
                                List<Component> tooltips) {
        super.appendHoverText(moduleContext, context, isAdvanced, tooltips);
        if (moduleContext.getAppliedTo().is(ItemTags.FOOT_ARMOR))
            tooltips.add(Component.translatable("item.gtceu.hazmat_boots"));
        if (moduleContext.getAppliedTo().is(ItemTags.LEG_ARMOR))
            tooltips.add(Component.translatable("item.gtceu.hazmat_leggings"));
        if (moduleContext.getAppliedTo().is(ItemTags.CHEST_ARMOR))
            tooltips.add(Component.translatable("item.gtceu.hazmat_chestpiece"));
        if (moduleContext.getAppliedTo().is(ItemTags.HEAD_ARMOR))
            tooltips.add(Component.translatable("item.gtceu.hazmat_headpiece"));
    }
}
