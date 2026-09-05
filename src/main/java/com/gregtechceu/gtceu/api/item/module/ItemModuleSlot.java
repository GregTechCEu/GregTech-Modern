package com.gregtechceu.gtceu.api.item.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.IDrawable;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemModuleSlot {

    private static final Map<ResourceLocation, ItemModuleSlot> SLOTS = new HashMap<>();

    @Getter
    private final ResourceLocation id;

    public static @Nullable ItemModuleSlot getById(ResourceLocation id) {
        return SLOTS.get(id);
    }

    protected ItemModuleSlot(ResourceLocation id) {
        this.id = id;
        SLOTS.put(id, this);
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        return tag;
    }

    public static ItemModuleSlot fromNBT(CompoundTag tag) {
        return getById(ResourceLocation.tryParse(tag.getString("id")));
    }

    public abstract boolean acceptsModule(ItemModule module);

    public abstract Component getDisplayName();

    public IDrawable getSlotTexture() {
        return null;
    }
}
