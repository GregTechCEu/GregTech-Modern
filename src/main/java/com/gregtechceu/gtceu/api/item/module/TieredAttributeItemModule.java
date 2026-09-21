package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
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

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class TieredAttributeItemModule extends TieredItemModule {

    public TieredAttributeItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    @Override
    public Codec<? extends ModuleData> moduleDataCodec() {
        return TieredAttributeModuleData.CODEC;
    }

    @Override
    public Class<? extends ModuleData> moduleDataClass() {
        return TieredAttributeModuleData.class;
    }

    @Override
    public ModuleData defaultModuleData(int slot, ItemModule module, ItemStack moduleStack) {
        return new TieredAttributeModuleData(slot, module, moduleStack, getMaxAttributeAmount());
    }

    private void attachAttribute(ModuleContext moduleContext) {
        var data = moduleContext.getData(TieredAttributeModuleData.class);

        if (data.getModifierUUID() != null) return;

        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(moduleContext.getAppliedTo());

        AttributeModifier attributeModifier = applySettingsToModifier(moduleContext,
                getAttributeModifier(moduleContext));

        moduleContext.getAppliedTo().addAttributeModifier(getAttribute(moduleContext), attributeModifier, slot);

        moduleContext.setData(moduleContext.getData(TieredAttributeModuleData.class));
        moduleContext.setData(data.withModifierUUID(attributeModifier.getId()));
    }

    private void detachAttribute(ModuleContext moduleContext) {
        var data = moduleContext.getData(TieredAttributeModuleData.class);

        UUID uuid = data.getModifierUUID();
        ListTag listTag = moduleContext.getAppliedTo().getOrCreateTag().getList("AttributeModifiers",
                Tag.TAG_COMPOUND);
        Iterator<Tag> it = listTag.iterator();
        while (it.hasNext()) {
            Tag tag = it.next();
            if (tag instanceof CompoundTag compoundTag) {
                AttributeModifier attributeModifier = AttributeModifier.load(compoundTag);
                if (attributeModifier != null && attributeModifier.getId().equals(uuid)) it.remove();
            }
        }
        moduleContext.setData(data.withModifierUUID(null));
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
        return moduleContext.getData(TieredAttributeModuleData.class).getModifierAmount();
    }

    public void setModifier(ModuleContext moduleContext, double modifier) {
        moduleContext.setData(moduleContext.getData(TieredAttributeModuleData.class).withModifierAmount(modifier));
        detachAttribute(moduleContext);
        attachAttribute(moduleContext);
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
                .num(Text.lang("module.gtceu.gui.power"),
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

    public abstract Attribute getAttribute(ModuleContext moduleContext);

    public abstract double getMaxAttributeAmount();

    public abstract AttributeModifier getAttributeModifier(ModuleContext moduleContext);

    public static class TieredAttributeModuleData extends ModuleData {

        // spotless:off
        public static final Codec<TieredAttributeModuleData> CODEC = RecordCodecBuilder.create(instance -> baseCodec(instance).and(instance.group(
                UUIDUtil.CODEC.optionalFieldOf("modifier_uuid").forGetter(TieredAttributeModuleData::getModifierUUIDOptional),
                Codec.DOUBLE.fieldOf("modifier_amount").forGetter(TieredAttributeModuleData::getModifierAmount)
        )).apply(instance, TieredAttributeModuleData::new));
        //spotless:on

        @Getter
        private final @Nullable UUID modifierUUID;

        @Getter
        private final double modifierAmount;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public TieredAttributeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                         Optional<UUID> modifierUUID, double modifierAmount) {
            super(slot, module, moduleItem, enabled);
            this.modifierUUID = modifierUUID.orElse(null);
            this.modifierAmount = modifierAmount;
        }

        public TieredAttributeModuleData(int slot, ItemModule module, ItemStack moduleItem, boolean enabled,
                                         @Nullable UUID modifierUUID, double modifierAmount) {
            super(slot, module, moduleItem, enabled);
            this.modifierUUID = modifierUUID;
            this.modifierAmount = modifierAmount;
        }

        public TieredAttributeModuleData(int slot, ItemModule module, ItemStack moduleItem, double modifierAmount) {
            super(slot, module, moduleItem, true);
            this.modifierUUID = null;
            this.modifierAmount = modifierAmount;
        }

        public Optional<UUID> getModifierUUIDOptional() {
            return Optional.ofNullable(modifierUUID);
        }

        public TieredAttributeModuleData withModifierUUID(@Nullable UUID modifierUUID) {
            return new TieredAttributeModuleData(slot, module, moduleItem, enabled, modifierUUID, modifierAmount);
        }

        public TieredAttributeModuleData withModifierAmount(double modifierAmount) {
            return new TieredAttributeModuleData(slot, module, moduleItem, enabled, modifierUUID, modifierAmount);
        }

        @Override
        public ModuleData withEnabled(boolean enabled) {
            return new TieredAttributeModuleData(slot, module, moduleItem, enabled, modifierUUID, modifierAmount);
        }

        @Override
        public ModuleData copy() {
            return new TieredAttributeModuleData(slot, module, moduleItem.copy(), enabled, modifierUUID,
                    modifierAmount);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TieredAttributeModuleData other)) return false;
            return super.equals(obj) && modifierAmount == other.modifierAmount &&
                    Objects.equals(modifierUUID, other.modifierUUID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, module, moduleItem, enabled, modifierUUID, modifierAmount);
        }
    }
}
