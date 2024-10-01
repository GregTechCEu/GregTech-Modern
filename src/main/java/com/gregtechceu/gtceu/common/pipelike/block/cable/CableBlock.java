package com.gregtechceu.gtceu.common.pipelike.block.cable;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IBurnable;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.block.PipeMaterialBlock;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.client.renderer.pipe.CableModel;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.pipelike.net.energy.EnergyFlowData;
import com.gregtechceu.gtceu.common.pipelike.net.energy.EnergyFlowLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.SuperconductorLogic;
import com.gregtechceu.gtceu.common.pipelike.net.energy.WorldEnergyNet;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CableBlock extends PipeMaterialBlock implements IBurnable {

    private static final Map<Material, Map<CableStructure, CableBlock>> CACHE = new Object2ObjectOpenHashMap<>();

    public CableBlock(BlockBehaviour.Properties properties, CableStructure structure, Material material) {
        super(properties, structure, material);
        CACHE.compute(material, (k, v) -> {
            if (v == null) v = new Object2ObjectOpenHashMap<>();
            v.put(structure, this);
            return v;
        });
    }

    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter,
                      @Nullable BlockPos blockPos, int index) {
        if (getStructure().isInsulated() && index == 1) {
            return CableModel.DEFAULT_INSULATION_COLOR;
        }
        return index == 0 ? material.getMaterialRGB() : -1;
    }

    @Override
    public CableStructure getStructure() {
        return (CableStructure) super.getStructure();
    }

    @Override
    public GTToolType getToolClass() {
        return GTToolType.WIRE_CUTTER;
    }

    @Override
    protected String getConnectLangKey() {
        return "gregtech.tool_action.wire_cutter.connect";
    }

    @Override
    public void partialBurn(BlockState state, Level world, BlockPos pos) {
        CableStructure structure = getStructure();
        if (structure.partialBurnStructure() != null) {
            BlockState newState = CACHE.get(material).get(structure.partialBurnStructure()).defaultBlockState();
            world.setBlockAndUpdate(pos, newState);

            BlockEntity newBlockEntity = world.getBlockEntity(pos);
            if (!(newBlockEntity instanceof PipeBlockEntity pipe)) return;
            for (Direction facing : GTUtil.DIRECTIONS) {
                if (pipe.isConnected(facing)) {
                    PipeBlock.connectTile(pipe, pipe.getPipeNeighbor(facing, false), facing);
                    BlockPos relativePos = pipe.getBlockPos().relative(facing);
                    ChunkAccess chunk = world.getChunk(relativePos);
                    if (chunk instanceof LevelChunk levelChunk) {
                        BlockEntity candidate = levelChunk
                                .getBlockEntity(relativePos, LevelChunk.EntityCreationType.CHECK);
                        if (candidate instanceof PipeBlockEntity otherPipe)
                            PipeBlock.connectTile(pipe, otherPipe, facing);
                    }
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!(level instanceof ServerLevel serverLevel) || getStructure().isInsulated() ||
                !(entity instanceof LivingEntity living))
            return;
        PipeBlockEntity tile = getBlockEntity(level, pos);
        if (tile != null && tile.getFrameMaterial() == null && tile.getOffsetTimer() % 10 == 0) {
            WorldPipeNetNode node = WorldEnergyNet.getWorldNet(serverLevel).getNode(pos);
            if (node != null) {
                if (node.getData().getLogicEntryNullable(SuperconductorLogic.TYPE) != null) return;
                EnergyFlowLogic logic = node.getData().getLogicEntryNullable(EnergyFlowLogic.TYPE);
                if (logic != null) {
                    long tick = Platform.getMinecraftServer().getTickCount();
                    long cumulativeDamage = 0;
                    for (EnergyFlowData data : logic.getFlow(tick)) {
                        cumulativeDamage += (GTUtil.getTierByVoltage(data.voltage()) + 1) * data.amperage() * 4;
                    }
                    if (cumulativeDamage != 0) {
                        living.hurt(GTDamageTypes.ELECTRIC.source(serverLevel), cumulativeDamage);
                        // TODO advancement
                        // if (living instanceof ServerPlayer serverPlayer) {
                        // AdvancementTriggers.ELECTROCUTION_DEATH.trigger(serverPlayer);
                        // }
                    }
                }
            }
        }
    }
}
