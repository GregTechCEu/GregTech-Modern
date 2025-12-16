package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.common.capability.PlaceholderSavedData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import lombok.Getter;

import java.util.List;

public abstract class Placeholder {

    @Getter
    private final String name;
    @Getter
    private final int priority;

    public abstract MultiLineComponent apply(PlaceholderContext ctx,
                                             List<MultiLineComponent> args) throws PlaceholderException;

    public Placeholder(String name) {
        this(name, 0);
    }

    public Placeholder(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    protected CompoundTag getData(PlaceholderContext ctx) {
        CompoundTag placeholderData = PlaceholderSavedData.getOrCreate((ServerLevel) ctx.level())
                .getPlaceholderData(this);
        if (!placeholderData.contains(ctx.uuid().toString()))
            placeholderData.put(ctx.uuid().toString(), new CompoundTag());
        return placeholderData.getCompound(ctx.uuid().toString());
    }
}
