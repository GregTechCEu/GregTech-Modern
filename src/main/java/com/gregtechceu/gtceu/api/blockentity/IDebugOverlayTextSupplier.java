package com.gregtechceu.gtceu.api.blockentity;

import java.util.function.Consumer;

public interface IDebugOverlayTextSupplier {

    void addDebugOverlayText(Consumer<String> leftLines, Consumer<String> rightLines);
}
