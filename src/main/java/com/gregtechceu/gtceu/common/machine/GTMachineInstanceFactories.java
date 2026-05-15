package com.gregtechceu.gtceu.common.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineInstanceFactory;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.machine.multiblock.part.hpca.HPCAComponentPartMachine;
import com.gregtechceu.gtceu.common.machine.trait.hpca.HPCAComponentTrait;
import com.gregtechceu.gtceu.common.machine.trait.multiblock.MultiblockFluidRendererTrait;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

@ApiStatus.NonExtendable
public interface GTMachineInstanceFactories {

    MachineInstanceFactory<WorkableElectricMultiblockMachine> LARGE_CHEM_BATH = (info) -> {
        var machine = new WorkableElectricMultiblockMachine(info);

        // Attach fluid render behaviour
        machine.attachPersistentTrait("fluidRendererTrait", new MultiblockFluidRendererTrait(() -> {
            var frontFace = machine.getFrontFacing();
            var upwardFace = machine.getUpwardsFacing();
            Direction up = RelativeDirection.UP.getRelative(frontFace, upwardFace, machine.isFlipped());
            Direction back = frontFace.getOpposite();
            Direction clockWise = RelativeDirection.RIGHT.getRelative(frontFace, upwardFace, machine.isFlipped());
            Direction counterClockWise = RelativeDirection.LEFT.getRelative(frontFace, upwardFace,
                    machine.isFlipped());

            BlockPos pos = machine.getBlockPos();
            BlockPos center = pos.relative(up);

            Set<BlockPos> offsets = new HashSet<>();

            for (int i = 0; i < 5; i++) {
                center = center.relative(back);
                offsets.add(center.subtract(pos));
                offsets.add(center.relative(clockWise).subtract(pos));
                offsets.add(center.relative(counterClockWise).subtract(pos));
            }
            return offsets;
        }));

        return machine;
    };

    MachineInstanceFactory<WorkableElectricMultiblockMachine> LARGE_MIXER = (info) -> {
        var machine = new WorkableElectricMultiblockMachine(info);

        // Attach fluid render behaviour
        machine.attachPersistentTrait("fluidRendererTrait", new MultiblockFluidRendererTrait(() -> {
            var frontFace = machine.getFrontFacing();
            var upwardFace = machine.getUpwardsFacing();
            var flipped = machine.isFlipped();
            Direction up = RelativeDirection.UP.getRelative(frontFace, upwardFace, flipped);
            Direction back = frontFace.getOpposite();
            Direction clockWise = RelativeDirection.RIGHT.getRelative(frontFace, upwardFace, flipped);
            Direction counterClockWise = RelativeDirection.LEFT.getRelative(frontFace, upwardFace, flipped);

            BlockPos pos = machine.getBlockPos();
            BlockPos center = pos.relative(up, 3);

            Set<BlockPos> offsets = new HashSet<>();

            for (int i = 0; i < 3; i++) {
                center = center.relative(back);
                if (i % 2 == 0) {
                    offsets.add(center.subtract(pos));
                }
                offsets.add(center.relative(clockWise).subtract(pos));
                offsets.add(center.relative(counterClockWise).subtract(pos));
            }
            return offsets;
        }));

        return machine;
    };

    MachineInstanceFactory.Tiered<SimpleTieredMachine> ROCK_CRUSHER = (info, tier) -> {
        var machine = new SimpleTieredMachine(info, tier);
        machine.getEnvironmentalExplosionTrait().setEnableEnvironmentalExplosions(false);
        return machine;
    };

    ///// HPCA stuff

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_EMPTY = (info) -> {
        var hpcaTrait = new HPCAComponentTrait(0, 0, false, false);
        return new HPCAComponentPartMachine(info, false, GTGuiTextures.HPCA_EMPTY_COMPONENT, GTGuiTextures.HPCA_EMPTY_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_BRIDGE = (info) -> {
        var hpcaTrait = new HPCAComponentTrait(GTValues.VA[GTValues.IV], GTValues.VA[GTValues.IV], false, true);
        return new HPCAComponentPartMachine(info, true, GTGuiTextures.HPCA_BRIDGE_COMPONENT, GTGuiTextures.HPCA_BRIDGE_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COMPUTATION = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCAComputationTrait(false);
        return new HPCAComponentPartMachine(info, false, GTGuiTextures.HPCA_COMPUTATION_COMPONENT, GTGuiTextures.HPCA_DAMAGED_COMPUTATION_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COMPUTATION_ADVANCED = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCAComputationTrait(true);
        return new HPCAComponentPartMachine(info, true, GTGuiTextures.HPCA_ADVANCED_COMPUTATION_COMPONENT, GTGuiTextures.HPCA_DAMAGED_ADVANCED_COMPUTATION_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COOLER = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCACoolerTrait(false);
        return new HPCAComponentPartMachine(info, false, GTGuiTextures.HPCA_HEAT_SINK_COMPONENT, GTGuiTextures.HPCA_HEAT_SINK_COMPONENT, hpcaTrait);
    };

    MachineInstanceFactory<HPCAComponentPartMachine> HPCA_COOLER_ADVANCED = (info) -> {
        var hpcaTrait = HPCAComponentPartMachine.createHPCACoolerTrait(true);
        return new HPCAComponentPartMachine(info, true, GTGuiTextures.HPCA_ACTIVE_COOLER_COMPONENT, GTGuiTextures.HPCA_ACTIVE_COOLER_COMPONENT, hpcaTrait);
    };
}
