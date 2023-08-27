package com.gregtechceu.gtceu.api.data;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;

public record DirectionalGlobalPos(GlobalPos position, Direction direction) {
}