package com.gregtechceu.gtceu.api.gui.compass;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.gui.compass.component.animation.Action;
import com.lowdragmc.lowdraglib.gui.compass.component.animation.AnimationFrame;
import com.lowdragmc.lowdraglib.gui.compass.component.animation.BlockAnima;
import com.lowdragmc.lowdraglib.gui.compass.component.animation.CompassScene;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.XmlUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class MultiblockAction extends Action {
    private final BlockAnima animation;
    @Nullable
    private final MultiblockMachineDefinition machineDefinition;
    private final int shapeIndex;
    private final boolean isFormed;
    private final Direction facing;

    public MultiblockAction(Element element) {
        var machineName = XmlUtils.getAsString(element, "machine", "");
        var blockPos = XmlUtils.getAsBlockPos(element, "pos", BlockPos.ZERO);
        shapeIndex = XmlUtils.getAsInt(element, "shape-index", 0);
        facing = XmlUtils.getAsEnum(element, "facing", Direction.class, Direction.NORTH);
        isFormed = XmlUtils.getAsBoolean(element, "formed", true);
        animation = new BlockAnima(blockPos, XmlUtils.getAsVec3(element, "offset", new Vec3(0, 0.7, 0)), XmlUtils.getAsInt(element, "duration", 15));
        if (ResourceLocation.isValidResourceLocation(machineName)) {
            var definition = GTRegistries.MACHINES.get(new ResourceLocation(machineName));
            if (definition instanceof MultiblockMachineDefinition multiblockDefinition) {
                machineDefinition = multiblockDefinition;
                return;
            }
        }
        machineDefinition = null;
    }

    @Override
    public int getDuration() {
        return machineDefinition == null ? 5 : animation.duration() + 5;
    }

    @Override
    public void performAction(AnimationFrame frame, CompassScene scene, boolean anima) {
        if (machineDefinition != null) {
            var shapes = machineDefinition.getMatchingShapes();
            if (!shapes.isEmpty()) {
                MultiblockShapeInfo shape = shapes.get(0);
                if (shapeIndex < shapes.size()) {
                    shape = shapes.get(shapeIndex);
                }
                var blocks = shape.getBlocks();
                Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
                BlockPos offset = BlockPos.ZERO;
                for (int x = 0; x < blocks.length; x++) {
                    BlockInfo[][] aisle = blocks[x];
                    for (int y = 0; y < aisle.length; y++) {
                        BlockInfo[] column = aisle[y];
                        for (int z = 0; z < column.length; z++) {
                            BlockState blockState = column[z].getBlockState();
                            BlockPos pos = animation.pos().offset(x, y, z);
                            if (column[z].getBlockEntity(pos) instanceof IMachineBlockEntity holder
                                    && holder.getMetaMachine() instanceof IMultiController) {
                                offset = pos;
                            }
                            blockMap.put(pos, BlockInfo.fromBlockState(blockState));
                        }
                    }
                }
                BlockPos finalOffset = offset;
                blockMap.forEach((pos, blockInfo) -> scene.addBlock(pos.subtract(finalOffset).offset(animation.pos()), blockInfo, anima ? animation : null));
            }
        }
    }
}
