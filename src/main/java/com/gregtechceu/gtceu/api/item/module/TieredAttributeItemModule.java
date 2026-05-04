package com.gregtechceu.gtceu.api.item.module;

import brachy.modularui.api.drawable.IKey;
import brachy.modularui.value.sync.PanelSyncManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.Iterator;
import java.util.UUID;

public abstract class TieredAttributeItemModule extends TieredItemModule {
    private static final String MODIFIER_UUID_KEY = "modifier_id";
    private static final String MODIFIER_AMOUNT_KEY = "modifier_amount";

    public TieredAttributeItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    private void attachAttribute(AppliedItemModule appliedItemModule) {
        if (appliedItemModule.getTag().contains(MODIFIER_UUID_KEY)) return;
        if (appliedItemModule.getAppliedTo() == null) return;
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(appliedItemModule.getAppliedTo());
        AttributeModifier attributeModifier = applySettingsToModifier(appliedItemModule, getAttributeModifier(appliedItemModule));
        appliedItemModule.getAppliedTo().addAttributeModifier(getAttribute(appliedItemModule), attributeModifier, slot);
        appliedItemModule.getTag().putUUID(MODIFIER_UUID_KEY, attributeModifier.getId());
    }

    private void detachAttribute(AppliedItemModule appliedItemModule) {
        UUID uuid = appliedItemModule.getTag().getUUID(MODIFIER_UUID_KEY);
        ListTag listTag = appliedItemModule.getAppliedTo().getOrCreateTag().getList("AttributeModifiers",
                Tag.TAG_COMPOUND);
        Iterator<Tag> it = listTag.iterator();
        while (it.hasNext()) {
            Tag tag = it.next();
            if (tag instanceof CompoundTag compoundTag) {
                AttributeModifier attributeModifier = AttributeModifier.load(compoundTag);
                if (attributeModifier != null && attributeModifier.getId().equals(uuid)) it.remove();
            }
        }
        appliedItemModule.getTag().remove(MODIFIER_UUID_KEY);
    }

    protected double getNeutralModifier(AppliedItemModule module) {
        return 0;
    }

    public double getMinModifier(AppliedItemModule module) {
        AttributeModifier attributeModifier = getAttributeModifier(module);
        double original = attributeModifier.getAmount();
        double neutral = getNeutralModifier(module);
        return Math.min(neutral, original);
    }

    public double getMaxModifier(AppliedItemModule module) {
        AttributeModifier attributeModifier = getAttributeModifier(module);
        double original = attributeModifier.getAmount();
        double neutral = getNeutralModifier(module);
        return Math.max(neutral, original);
    }

    protected AttributeModifier applySettingsToModifier(AppliedItemModule module, AttributeModifier modifier) {
        double modifiedAmount = getModifier(module);
        return new AttributeModifier(modifier.getId(), modifier.getName(), modifiedAmount, modifier.getOperation());
    }

    public double getModifier(AppliedItemModule module) {
        if (module.getTag().contains(MODIFIER_AMOUNT_KEY))
            return module.getTag().getDouble(MODIFIER_AMOUNT_KEY);
        return getAttributeModifier(module).getAmount();
    }

    public void setModifier(AppliedItemModule module, double modifier) {
        module.getTag().putDouble(MODIFIER_AMOUNT_KEY, modifier);
        detachAttribute(module);
        attachAttribute(module);
    }

    protected String getSliderFormatString(AppliedItemModule module) {
        return switch (getAttributeModifier(module).getOperation()) {
            case ADDITION -> "+%.2f";
            case MULTIPLY_BASE, MULTIPLY_TOTAL -> "+%.2f%%";
        };
    }

    @Override
    public Settings getSettings(AppliedItemModule module, PanelSyncManager psm, int id) {
        return super.getSettings(module, psm, id)
                .num(IKey.lang("gtceu.module.gui.power"),
                        () -> getModifier(module),
                        x -> setModifier(module, x),
                        getMinModifier(module), getMaxModifier(module),
                        getSliderFormatString(module));
    }

    @Override
    public void onAttach(AppliedItemModule appliedItemModule) {
        super.onAttach(appliedItemModule);
        attachAttribute(appliedItemModule);
    }

    @Override
    public void onRemove(AppliedItemModule appliedItemModule) {
        super.onRemove(appliedItemModule);
        detachAttribute(appliedItemModule);
    }

    @Override
    public void setEnabled(AppliedItemModule module, boolean enabled) {
        super.setEnabled(module, enabled);
        if (this.isEnabled(module)) {
            this.attachAttribute(module);
        } else this.detachAttribute(module);
    }

    public abstract Attribute getAttribute(AppliedItemModule module);

    public abstract AttributeModifier getAttributeModifier(AppliedItemModule module);
}
