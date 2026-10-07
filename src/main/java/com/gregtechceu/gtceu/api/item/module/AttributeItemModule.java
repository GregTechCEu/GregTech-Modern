package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;

import java.util.Objects;

public abstract class AttributeItemModule extends ItemModule {

    public AttributeItemModule(ResourceLocation id) {
        super(id);
    }

    @Override
    public MapCodec<? extends ModuleData> moduleDataCodec() {
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

    protected void attachAttribute(ModuleContext moduleContext) {
        AttributeModifier attributeModifier = getAttributeModifier(moduleContext);
        var modifiers = moduleContext.getAppliedTo().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);

        if (modifiers.modifiers().stream().anyMatch(v -> v.modifier().id().equals(getId()))) return;

        modifiers = modifiers.withModifierAdded(getAttribute(moduleContext), attributeModifier,
                EquipmentSlotGroup.bySlot(getSlot(moduleContext.getAppliedTo())));

        moduleContext.getAppliedTo().set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
    }

    private EquipmentSlot getSlot(ItemStack stack) {
        Equipable equipable = Equipable.get(stack);
        if (equipable != null) return equipable.getEquipmentSlot();
        return EquipmentSlot.MAINHAND;
    }

    protected boolean attributeAttached(ModuleContext moduleContext) {
        var modifiers = moduleContext.getAppliedTo().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);
        return modifiers.modifiers().stream().anyMatch(v -> v.modifier().id().equals(getId()));
    }

    protected void detachAttribute(ModuleContext moduleContext) {
        var modifiers = moduleContext.getAppliedTo().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.EMPTY);

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        modifiers.modifiers().forEach(v -> {
            if (v.modifier().id().equals(getId())) return;
            builder.add(v.attribute(), v.modifier(), v.slot());
        });

        moduleContext.getAppliedTo().set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    public double getMinModifier(ModuleContext moduleContext) {
        double original = getMaxAttributeAmount();
        double neutral = getNeutralModifier(moduleContext);
        return Math.min(neutral, original);
    }

    public double getMaxModifier(ModuleContext moduleContext) {
        double original = getMaxAttributeAmount();
        double neutral = getNeutralModifier(moduleContext);
        return Math.max(neutral, original);
    }

    protected AttributeModifier getAttributeModifier(ModuleContext moduleContext) {
        double modifiedAmount = getModifier(moduleContext);
        return new AttributeModifier(getId(), modifiedAmount, getModifierOperation());
    }

    public double getModifier(ModuleContext moduleContext) {
        return moduleContext.getData(AttributeModuleData.class).getModifierAmount();
    }

    public void setModifier(ModuleContext moduleContext, double modifier) {
        moduleContext.setData(moduleContext.getData(AttributeModuleData.class).withModifierAmount(modifier));
        detachAttribute(moduleContext);
        attachAttribute(moduleContext);
    }

    protected String getSliderString(ModuleContext moduleContext, double value) {
        return switch (getModifierOperation()) {
            case ADD_VALUE -> "+%.2f".formatted(value);
            case ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL -> "+%.2f%%".formatted(value * 100);
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

    @Override
    public void onAttach(ModuleContext moduleContext) {
        super.onAttach(moduleContext);
        attachAttribute(moduleContext);
    }

    @Override
    public void onRemove(ModuleContext moduleContext) {
        super.onRemove(moduleContext);
        detachAttribute(moduleContext);
    }

    @Override
    public void setEnabled(ModuleContext moduleContext, boolean enabled) {
        super.setEnabled(moduleContext, enabled);
        if (this.isEnabled(moduleContext)) {
            this.attachAttribute(moduleContext);
        } else this.detachAttribute(moduleContext);
    }

    public abstract Holder<Attribute> getAttribute(ModuleContext moduleContext);

    public abstract double getMaxAttributeAmount();

    public abstract AttributeModifier.Operation getModifierOperation();

    protected double getNeutralModifier(ModuleContext moduleContext) {
        return 0;
    }

    /**
     * Called each tick while the item module is equipped, to determine if the attribute should be active.
     */
    public boolean keepAttributeActive(ModuleContext moduleContext, LivingEntity entity) {
        return true;
    }

    @Override
    public void onArmorTick(ModuleContext moduleContext, LivingEntity entity) {
        var currentlyAttached = attributeAttached(moduleContext);
        var shouldKeepAttached = keepAttributeActive(moduleContext, entity);
        if (shouldKeepAttached && !currentlyAttached) attachAttribute(moduleContext);
        if (!shouldKeepAttached && currentlyAttached) detachAttribute(moduleContext);
    }

    @Override
    public void onUnequip(ModuleContext moduleContext, LivingEntity entity) {
        if (!attributeAttached(moduleContext)) attributeAttached(moduleContext);
    }

    public static class AttributeModuleData extends ModuleData {

        // spotless:off
        public static final MapCodec<AttributeModuleData> CODEC = RecordCodecBuilder.mapCodec(instance -> baseCodec(instance).and(
                Codec.DOUBLE.fieldOf("modifier_amount").forGetter(AttributeModuleData::getModifierAmount)
        ).apply(instance, AttributeModuleData::new));
        //spotless:on

        @Getter
        private final double modifierAmount;

        public AttributeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                   double modifierAmount) {
            super(slot, module, moduleItem, enabled);
            this.modifierAmount = modifierAmount;
        }

        public AttributeModuleData(int slot, ItemModule module, ItemStack moduleItem, double modifierAmount) {
            super(slot, module, moduleItem, true);
            this.modifierAmount = modifierAmount;
        }

        public AttributeModuleData withModifierAmount(double modifierAmount) {
            return new AttributeModuleData(slot, module, moduleItem, enabled, modifierAmount);
        }

        @Override
        public ModuleData copy() {
            return new AttributeModuleData(slot, module, moduleItem.copy(), enabled, modifierAmount);
        }

        @Override
        public ModuleData withModuleItem(ItemStack moduleItem) {
            return new AttributeModuleData(slot, module, moduleItem, enabled, modifierAmount);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new AttributeModuleData(slot, module, moduleItem, enabled, modifierAmount);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AttributeModuleData other)) return false;
            return super.equals(obj) && modifierAmount == other.modifierAmount;
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, modifierAmount);
        }
    }
}
