package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CentralMonitorMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CentralMonitorMachine.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final Block[] VALID_BLOCKS = new Block[] {
            GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()
    };
    private static final Set<MachineDefinition[]> VALID_MACHINES = Set.of(
            GTMachines.HULL,
            GTMachines.ENERGY_INPUT_HATCH,
            GTMachines.ENERGY_INPUT_HATCH_4A,
            GTMachines.ENERGY_INPUT_HATCH_16A,
            new MachineDefinition[] {
                    GTResearchMachines.DATA_ACCESS_HATCH,
                    GTResearchMachines.ADVANCED_DATA_ACCESS_HATCH,
                    GTResearchMachines.CREATIVE_DATA_ACCESS_HATCH
            });
    public static final TraceabilityPredicate BLOCK_PREDICATE = Predicates.abilities(PartAbility.INPUT_ENERGY)
            .setExactLimit(1)
            .or(Predicates.abilities(PartAbility.DATA_ACCESS).setMaxGlobalLimited(1))
            .or(Predicates.machines(GTMachines.HULL))
            .or(Predicates.blocks(VALID_BLOCKS));

    private int leftDist = 0, rightDist = 0, upDist = 0, downDist = 0;

    public CentralMonitorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public boolean isValidMonitorBlock(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (Arrays.stream(VALID_BLOCKS).anyMatch(block -> state == block.defaultBlockState())) return true;
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBlockEntity) {
            return VALID_MACHINES.stream().anyMatch(
                    definitions -> Arrays.stream(definitions).anyMatch(
                            definition -> definition == machineBlockEntity.getDefinition()));
        }
        return false;
    }

    public void updateStructureDimensions() {
        Level level = getLevel();
        if (level == null) return;

        Direction front = getFrontFacing();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        BlockPos.MutableBlockPos posLeft = getPos().mutable().move(left);
        BlockPos.MutableBlockPos posRight = getPos().mutable().move(right);
        BlockPos.MutableBlockPos posUp = getPos().mutable().move(Direction.UP);
        BlockPos.MutableBlockPos posDown = getPos().mutable().move(Direction.DOWN);

        leftDist = 0;
        rightDist = 0;
        upDist = 0;
        downDist = 0;

        while (isValidMonitorBlock(level, posLeft)) {
            posLeft.move(left);
            leftDist++;
        }
        while (isValidMonitorBlock(level, posRight)) {
            posRight.move(right);
            rightDist++;
        }
        while (isValidMonitorBlock(level, posUp)) {
            posUp.move(Direction.UP);
            upDist++;
        }
        while (isValidMonitorBlock(level, posDown)) {
            posDown.move(Direction.DOWN);
            downDist++;
        }
    }

    @Override
    public BlockPattern getPattern() {
        updateStructureDimensions();
        if (leftDist + rightDist + upDist + downDist == 0) {
            leftDist = 3;
            rightDist = 0;
            upDist = 1;
            downDist = 1;
        }
        StringBuilder[] pattern = new StringBuilder[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) {
            pattern[i] = new StringBuilder(leftDist + rightDist + 1);
            for (int j = 0; j < leftDist + rightDist + 1; j++) {
                if (i == upDist && j == leftDist)
                    pattern[i].append('C'); // controller
                else
                    pattern[i].append('B'); // any valid block
            }
        }
        String[] tmp = new String[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) tmp[i] = pattern[i].toString();
        return FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT)
                .aisle(tmp)
                .where('B', BLOCK_PREDICATE)
                .where('C', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .build();
    }

    @Override
    public int getTier() {
        return 2;
    }
}
