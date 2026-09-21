package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public abstract class TieredItemModule extends ItemModule implements ITieredItemModule {

    @Getter
    private final int tier;
    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    private TieredItemModule[] otherTierModules;

    public TieredItemModule(ResourceLocation id, int tier) {
        super(id);
        this.tier = tier;
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return false;
        for (TieredItemModule module : otherTierModules) if (modularItem.getModuleContext(module) != null) return false;
        return super.canApplyTo(stack);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Level level, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, level, tooltips, isAdvanced);
        tooltips.add(Component.translatable(getId().toLanguageKey("module"),
                GTValues.VNF[getTier()]));
    }
}
