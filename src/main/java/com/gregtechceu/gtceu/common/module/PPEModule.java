package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.UnknownNullability;

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
    public void appendHoverText(ModuleContext moduleContext, Level level, TooltipFlag isAdvanced, List<Component> tooltips) {
        super.appendHoverText(moduleContext, level, isAdvanced, tooltips);
        if (moduleContext.getAppliedTo().is(Tags.Items.ARMORS_BOOTS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_boots"));
        if (moduleContext.getAppliedTo().is(Tags.Items.ARMORS_LEGGINGS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_leggings"));
        if (moduleContext.getAppliedTo().is(Tags.Items.ARMORS_CHESTPLATES))
            tooltips.add(Component.translatable("item.gtceu.hazmat_chestpiece"));
        if (moduleContext.getAppliedTo().is(Tags.Items.ARMORS_HELMETS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_headpiece"));
    }
}
