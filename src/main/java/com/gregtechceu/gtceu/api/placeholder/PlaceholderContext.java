package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AllArgsConstructor
@Accessors(fluent = true)
@Getter
@Setter
@ToString
public final class PlaceholderContext {

    private Level level;
    private BlockPos pos;
    private Direction side;
    private final @Nullable ItemStackHandler itemStackHandler;
    private final @Nullable CoverBehavior cover;
    private final @Nullable MonitorGroup monitorGroup;
    private final @Nullable MultiLineComponent previousText;
    private final UUID uuid;
    private int index;

    public PlaceholderContext(Level level,
                              BlockPos pos,
                              Direction side,
                              @Nullable ItemStackHandler itemStackHandler,
                              @Nullable CoverBehavior cover,
                              @Nullable MonitorGroup monitorGroup,
                              @Nullable MultiLineComponent previousText,
                              UUID uuid) {
        this(level, pos, side, itemStackHandler, cover, monitorGroup, previousText, uuid, 0);
    }
}
