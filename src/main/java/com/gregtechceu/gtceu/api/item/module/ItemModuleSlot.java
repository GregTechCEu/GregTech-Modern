package com.gregtechceu.gtceu.api.item.module;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.IDrawable;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class ItemModuleSlot {

    public abstract boolean acceptsModule(ItemModule module);

    public abstract Component getDisplayName();

    public IDrawable getSlotTexture() {
        return null;
    }
}
