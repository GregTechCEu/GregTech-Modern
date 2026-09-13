package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.UUID;

public abstract class TieredAttributeItemModule extends TieredItemModule {

    // spotless:off
    protected static <M extends TieredAttributeItemModule> Codec<M> tieredAttributeCodec(Function4<Boolean, ItemStack, Integer, Double, M> func) {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("enabled").forGetter(ItemModule::isEnabled),
                ItemStack.CODEC.fieldOf("module_item").forGetter(ItemModule::getModuleItem),
                Codec.INT.fieldOf("tier").forGetter(TieredItemModule::getTier),
                Codec.DOUBLE.fieldOf("modifier_amount").forGetter(TieredAttributeItemModule::getModifierAmount)
        ).apply(instance, func));
    }
    //spotless:on

    @Getter
    private double modifierAmount;

    private @Nullable UUID attributeUUID;

    public TieredAttributeItemModule(boolean isEnabled, ItemStack moduleItem, int tier, double modifierAmount) {
        super(isEnabled, moduleItem, tier);
        this.modifierAmount = modifierAmount;
    }

    public TieredAttributeItemModule(ItemStack moduleItem, int tier) {
        super(moduleItem, tier);
        modifierAmount = getMaxModifier();
        attributeUUID = getAttributeModifier().getId();
    }

    private void attachAttribute() {
        if (attributeUUID != null) return;

        if (getAppliedTo() == null) return;
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(getAppliedTo());
        AttributeModifier attributeModifier = applySettingsToModifier(getAttributeModifier());
        getAppliedTo().addAttributeModifier(getAttribute(), attributeModifier, slot);

        attributeUUID = attributeModifier.getId();
    }

    private void detachAttribute() {
        ListTag listTag = getAppliedTo().getOrCreateTag().getList("AttributeModifiers",
                Tag.TAG_COMPOUND);
        Iterator<Tag> it = listTag.iterator();
        while (it.hasNext()) {
            Tag tag = it.next();
            if (tag instanceof CompoundTag compoundTag) {
                AttributeModifier attributeModifier = AttributeModifier.load(compoundTag);
                if (attributeModifier != null && attributeModifier.getId().equals(attributeUUID)) it.remove();
            }
        }

        attributeUUID = null;
    }

    protected double getNeutralModifier() {
        return 0;
    }

    public double getMinModifier() {
        AttributeModifier attributeModifier = getAttributeModifier();
        double original = attributeModifier.getAmount();
        double neutral = getNeutralModifier();
        return Math.min(neutral, original);
    }

    public double getMaxModifier() {
        AttributeModifier attributeModifier = getAttributeModifier();
        double original = attributeModifier.getAmount();
        double neutral = getNeutralModifier();
        return Math.max(neutral, original);
    }

    protected AttributeModifier applySettingsToModifier(AttributeModifier modifier) {
        double modifiedAmount = getModifierAmount();
        return new AttributeModifier(modifier.getId(), modifier.getName(), modifiedAmount, modifier.getOperation());
    }

    public void setModifier(double modifier) {
        this.modifierAmount = modifier;
        detachAttribute();
        attachAttribute();
    }

    protected String getSliderString(double value) {
        return switch (getAttributeModifier().getOperation()) {
            case ADDITION -> "+%.2f".formatted(value);
            case MULTIPLY_BASE, MULTIPLY_TOTAL -> "+%.2f%%".formatted(value * 100);
        };
    }

    @Override
    public ItemModuleSettingsBuilder getSettings(PanelSyncManager psm, int id) {
        return super.getSettings(psm, id)
                .num(Text.lang("gtceu.module.gui.power"),
                        this::getModifierAmount,
                        this::setModifier,
                        getMinModifier(), getMaxModifier(),
                        this::getSliderString);
    }

    @Override
    public void onAttach() {
        super.onAttach();
        attachAttribute();
    }

    @Override
    public void onRemove() {
        super.onRemove();
        detachAttribute();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.isEnabled()) {
            this.attachAttribute();
        } else this.detachAttribute();
    }

    public abstract Attribute getAttribute();

    public abstract AttributeModifier getAttributeModifier();
}
