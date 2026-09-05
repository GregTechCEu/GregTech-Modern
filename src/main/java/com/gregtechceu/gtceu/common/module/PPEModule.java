package com.gregtechceu.gtceu.common.module;

import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.ItemModule;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;

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
    public boolean isPPE(AppliedItemModule module) {
        return true;
    }

    @Override
    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {
        super.appendHoverText(level, isAdvanced, tooltips, module);
        if (module.getAppliedTo().is(Tags.Items.ARMORS_BOOTS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_boots"));
        if (module.getAppliedTo().is(Tags.Items.ARMORS_LEGGINGS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_leggings"));
        if (module.getAppliedTo().is(Tags.Items.ARMORS_CHESTPLATES))
            tooltips.add(Component.translatable("item.gtceu.hazmat_chestpiece"));
        if (module.getAppliedTo().is(Tags.Items.ARMORS_HELMETS))
            tooltips.add(Component.translatable("item.gtceu.hazmat_headpiece"));
    }
}
