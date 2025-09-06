package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record PlaceholderContext(Level level,
                                 BlockPos pos,
                                 Direction side,
                                 @Nullable ItemStackHandler itemStackHandler,
                                 @Nullable CoverBehavior cover,
                                 @Nullable MultiLineComponent previousText,
                                 UUID uuid) {}
