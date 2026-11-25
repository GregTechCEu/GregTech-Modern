package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import java.util.Comparator;

@FunctionalInterface
public interface PartSorter {

    PartSorter DEFAULT = (controller) -> (part1, part2) -> 0;

    Comparator<MultiblockPartMachine> getSorter(MultiblockControllerMachine controller);
}
