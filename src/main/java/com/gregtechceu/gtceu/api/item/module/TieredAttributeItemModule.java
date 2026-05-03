package com.gregtechceu.gtceu.api.item.module;

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

    public TieredAttributeItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    private void attachAttribute(AppliedItemModule appliedItemModule) {
        if (appliedItemModule.getTag().contains("modifierUUID")) return;
        if (appliedItemModule.getAppliedTo() == null) return;
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(appliedItemModule.getAppliedTo());
        AttributeModifier attributeModifier = getAttributeModifier(appliedItemModule);
        appliedItemModule.getAppliedTo().addAttributeModifier(getAttribute(appliedItemModule), attributeModifier, slot);
        appliedItemModule.getTag().putUUID("modifierUUID", attributeModifier.getId());
    }

    private void detachAttribute(AppliedItemModule appliedItemModule) {
        UUID uuid = appliedItemModule.getTag().getUUID("modifierUUID");
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
        appliedItemModule.getTag().remove("modifierUUID");
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
