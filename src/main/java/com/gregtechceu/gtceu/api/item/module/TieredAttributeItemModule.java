package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public abstract class TieredAttributeItemModule extends AttributeItemModule implements ITieredItemModule {

    @Getter
    private final int tier;
    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    private @Nullable Holder<ItemModule>[] otherTierModules;

    private final ResourceLocation baseLocation;

    public TieredAttributeItemModule(ResourceLocation id, int tier) {
        super(id);
        this.tier = tier;
        this.baseLocation = getId().withPath(p -> p.replaceFirst(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_", ""));
    }

    @Override
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modular = GTCapabilityHelper.getModularItem(stack);
        if (modular == null) return false;
        for (Holder<ItemModule> module : otherTierModules) {
            if (module == null) continue;
            if (modular.getModuleContext(module.value()) != null) return false;
        }
        return super.canApplyTo(stack);
    }

    @Override
    public String getLanguageKey() {
        return baseLocation.toLanguageKey("module");
    }

    @Override
    public String getDescriptionLanguageKey() {
        return baseLocation.toLanguageKey("module", "description");
    }

    @Override
    public Component getDisplayName(ModuleContext moduleContext) {
        return Component.translatable(getLanguageKey(),
                GTValues.VNF[getTier()]);
    }
}
