package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class TieredItemModule extends ItemModule implements ITieredItemModule {

    @Getter
    private final int tier;
    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    private @Nullable Holder<ItemModule>[] otherTierModules;

    public TieredItemModule(ResourceLocation id, int tier) {
        super(id);
        this.tier = tier;
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        if (modularItem == null) return false;
        for (Holder<ItemModule> module : otherTierModules) {
            if (module == null) continue;
            if (modularItem.getModuleContext(module.value()) != null) return false;
        }
        return super.canApplyTo(stack);
    }

    @Override
    public void appendHoverText(ModuleContext moduleContext, Item.TooltipContext context, List<Component> tooltips,
                                TooltipFlag isAdvanced) {
        super.appendHoverText(moduleContext, context, tooltips, isAdvanced);
        tooltips.add(Component.translatable(getId().toLanguageKey("module"),
                GTValues.VNF[getTier()]));
    }
}
