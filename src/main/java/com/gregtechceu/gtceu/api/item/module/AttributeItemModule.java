package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class AttributeItemModule extends ItemModule {

    public AttributeItemModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public Codec<? extends ModuleData> moduleDataCodec() {
        return AttributeModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return AttributeModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new AttributeModuleData(slot, module, moduleStack, getMaxAttributeAmount());
    }

    public @Nullable AttributeModifier getModifierToAdd(ModuleContext moduleContext) {
        if (!isEnabled(moduleContext)) return null;
        if (!moduleContext.getData(AttributeModuleData.class).isKeepActive()) return null;

        return applySettingsToModifier(moduleContext,
                getAttributeModifier(moduleContext));
    }

    protected double getNeutralModifier(ModuleContext moduleContext) {
        return 0;
    }

    public double getMinModifier(ModuleContext moduleContext) {
        AttributeModifier attributeModifier = getAttributeModifier(moduleContext);
        double original = attributeModifier.getAmount();
        double neutral = getNeutralModifier(moduleContext);
        return Math.min(neutral, original);
    }

    public double getMaxModifier(ModuleContext moduleContext) {
        double original = getMaxAttributeAmount();
        double neutral = getNeutralModifier(moduleContext);
        return Math.max(neutral, original);
    }

    protected AttributeModifier applySettingsToModifier(ModuleContext moduleContext, AttributeModifier modifier) {
        double modifiedAmount = getModifier(moduleContext);
        return new AttributeModifier(modifier.getId(), modifier.getName(), modifiedAmount, modifier.getOperation());
    }

    public double getModifier(ModuleContext moduleContext) {
        return moduleContext.getData(AttributeModuleData.class).getModifierAmount();
    }

    public void setModifier(ModuleContext moduleContext, double modifier) {
        moduleContext.setData(moduleContext.getData(AttributeModuleData.class).withModifierAmount(modifier));
    }

    protected String getSliderString(ModuleContext moduleContext, double value) {
        return switch (getAttributeModifier(moduleContext).getOperation()) {
            case ADDITION -> "+%.2f".formatted(value);
            case MULTIPLY_BASE, MULTIPLY_TOTAL -> "+%.2f%%".formatted(value * 100);
        };
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(ModuleContext moduleContext, PanelSyncManager psm, int id) {
        return super.getSettings(moduleContext, psm, id)
                .num(Text.lang("gui.gtceu.item_module.power"),
                        () -> getModifier(moduleContext),
                        x -> setModifier(moduleContext, x),
                        getMinModifier(moduleContext), getMaxModifier(moduleContext),
                        x -> getSliderString(moduleContext, x));
    }

    public abstract Attribute getAttribute(ModuleContext moduleContext);

    public abstract double getMaxAttributeAmount();

    public abstract AttributeModifier getAttributeModifier(ModuleContext moduleContext);

    /**
     * Called each tick while the item module is equipped, to determine if the attribute should be active.
     */
    public boolean keepAttributeActive(ModuleContext moduleContext, LivingEntity entity) {
        return true;
    }

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        var keepActive = keepAttributeActive(moduleContext, entity);
        if (keepActive == moduleContext.getData(AttributeModuleData.class).keepActive) return;
        moduleContext.setData(moduleContext.getData(AttributeModuleData.class).withKeepActive(keepActive));
    }

    public static class AttributeModuleData extends ModuleData {

        // spotless:off
        public static final Codec<AttributeModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(instance.group(
                Codec.DOUBLE.fieldOf("modifier_amount").forGetter(AttributeModuleData::getModifierAmount),
                Codec.BOOL.fieldOf("keep_active").forGetter(AttributeModuleData::isKeepActive)
        )).apply(instance, AttributeModuleData::new));
        // spotless:on

        @Getter
        private final double modifierAmount;
        @Getter
        private final boolean keepActive;

        public AttributeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                   double modifierAmount, boolean keepActive) {
            super(slot, module, moduleItem, enabled);
            this.modifierAmount = modifierAmount;
            this.keepActive = keepActive;
        }

        public AttributeModuleData(int slot, ItemModule module, ItemStack moduleItem, double modifierAmount) {
            super(slot, module, moduleItem, true);
            this.modifierAmount = modifierAmount;
            this.keepActive = true;
        }

        public AttributeModuleData withModifierAmount(double modifierAmount) {
            return new AttributeModuleData(slot, module, moduleItem, enabled, modifierAmount, keepActive);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new AttributeModuleData(slot, module, moduleItem, enabled, modifierAmount, keepActive);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new AttributeModuleData(slot, module, moduleItem, enabled, modifierAmount, keepActive);
        }

        public AttributeModuleData withKeepActive(boolean keepActive) {
            return new AttributeModuleData(slot, module, moduleItem, enabled, modifierAmount, keepActive);
        }

        @Override
        public ModuleData copy() {
            return new AttributeModuleData(slot, module, moduleItem.copy(), enabled,
                    modifierAmount, keepActive);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AttributeModuleData other)) return false;
            return super.equals(obj) && modifierAmount == other.modifierAmount && keepActive == other.keepActive;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, modifierAmount, keepActive);
        }
    }
}
