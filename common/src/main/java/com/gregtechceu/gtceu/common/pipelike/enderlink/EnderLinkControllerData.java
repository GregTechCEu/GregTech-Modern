package com.gregtechceu.gtceu.common.pipelike.enderlink;

import net.minecraft.core.GlobalPos;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record EnderLinkControllerData(@NotNull GlobalPos pos, @NotNull UUID uuid) {
}
