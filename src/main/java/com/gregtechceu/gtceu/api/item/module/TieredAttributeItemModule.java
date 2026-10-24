package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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

    public double getEnergyCost() {
        return 0;
    }

    public double getEnergyCost(ModuleContext context) {
        double maximum = Math.abs(getMaxAttributeAmount());
        return maximum == 0 ? 0 : getEnergyCost() * Math.abs(getModifier(context)) / maximum;
    }

    protected boolean consumesEnergyThisTick(ModuleContext context, LivingEntity entity) {
        return false;
    }

    @Override
    public boolean keepAttributeActive(ModuleContext context, LivingEntity entity) {
        boolean consume = consumesEnergyThisTick(context, entity);
        double cost = getEnergyCost(context);
        if (consume && cost > 0) cost = Math.max(0.25, cost);
        return ArmorUtils.useModuleEnergy(context.getAppliedTo(), ArmorUtils.toModuleEnergyCredits(cost),
                entity.level().isClientSide || !consume);
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
