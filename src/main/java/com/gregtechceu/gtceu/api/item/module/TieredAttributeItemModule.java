package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.api.item.module.ui.ItemModuleSettingsBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.value.sync.PanelSyncManager;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Iterator;
import java.util.UUID;

public abstract class TieredAttributeItemModule extends TieredItemModule {

    private static final String MODIFIER_UUID_KEY = "modifier_id";
    private static final String MODIFIER_AMOUNT_KEY = "modifier_amount";

    public TieredAttributeItemModule(ResourceLocation id, int tier) {
        super(id, tier);
    }

    private void attachAttribute(ModuleContext moduleContext) {
        if (moduleContext.getData().getTag().contains(MODIFIER_UUID_KEY)) return;
        EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(moduleContext.getAppliedTo());
        AttributeModifier attributeModifier = applySettingsToModifier(moduleContext,
                getAttributeModifier(moduleContext));
        moduleContext.getAppliedTo().addAttributeModifier(getAttribute(moduleContext), attributeModifier, slot);
        moduleContext.getData().getTag().putUUID(MODIFIER_UUID_KEY, attributeModifier.getId());
    }

    private void detachAttribute(ModuleContext moduleContext) {
        UUID uuid = moduleContext.getData().getTag().getUUID(MODIFIER_UUID_KEY);
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
        moduleContext.getData().getTag().remove(MODIFIER_UUID_KEY);
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
        AttributeModifier attributeModifier = getAttributeModifier(moduleContext);
        double original = attributeModifier.getAmount();
        double neutral = getNeutralModifier(moduleContext);
        return Math.max(neutral, original);
    }

    protected AttributeModifier applySettingsToModifier(ModuleContext moduleContext, AttributeModifier modifier) {
        double modifiedAmount = getModifier(moduleContext);
        return new AttributeModifier(modifier.getId(), modifier.getName(), modifiedAmount, modifier.getOperation());
    }

    public double getModifier(ModuleContext moduleContext) {
        if (moduleContext.getData().getTag().contains(MODIFIER_AMOUNT_KEY))
            return moduleContext.getData().getTag().getDouble(MODIFIER_AMOUNT_KEY);
        return getAttributeModifier(moduleContext).getAmount();
    }

    public void setModifier(ModuleContext moduleContext, double modifier) {
        moduleContext.getData().getTag().putDouble(MODIFIER_AMOUNT_KEY, modifier);
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
                .num(Text.lang("gtceu.module.gui.power"),
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

    public abstract AttributeModifier getAttributeModifier(ModuleContext moduleContext);
}
