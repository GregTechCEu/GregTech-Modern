package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.IToolable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.common.pipelike.cable.LevelEnergyNet;
import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.blockentity.GTBlockEntities;
import com.gregtechceu.gtceu.data.damagesource.GTDamageTypes;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/3/1
 * @implNote CableBlock
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableBlock extends MaterialPipeBlock<Insulation, WireProperties, LevelEnergyNet> {

    public CableBlock(Properties properties, Insulation insulation, Material material) {
        super(properties, insulation, material);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof CableBlockEntity cableBlockEntity) {
                return cableBlockEntity.getEnergyContainer(side);
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof PipeBlockEntity<?, ?> pipe) {
                return pipe.getCoverContainer();
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_TOOLABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IToolable toolable) {
                return toolable;
            }
            return null;
        }, this);
    }

    @Override
    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter,
                      @Nullable BlockPos blockPos, int index) {
        if (pipeType.isCable && index == 0) {
            return 0x404040;
        }
        return index == 0 || index == 1 ? material.getMaterialRGB() : -1;
    }

    @Override
    protected WireProperties createProperties(Insulation insulation, Material material) {
        return insulation.modifyProperties(material.getProperty(PropertyKey.WIRE));
    }

    @Override
    protected WireProperties createMaterialData() {
        return material.getProperty(PropertyKey.WIRE);
    }

    @Override
    public LevelEnergyNet getWorldPipeNet(ServerLevel level) {
        return LevelEnergyNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<Insulation, WireProperties>> getBlockEntityType() {
        return GTBlockEntities.CABLE.get();
    }

    @Override
    public boolean canPipesConnect(IPipeNode<Insulation, WireProperties> selfTile, Direction side,
                                   IPipeNode<Insulation, WireProperties> sideTile) {
        return selfTile instanceof CableBlockEntity && sideTile instanceof CableBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<Insulation, WireProperties> selfTile, Direction side,
                                         @Nullable BlockEntity tile) {
        return tile != null && tile.getLevel().getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER,
                tile.getBlockPos(), tile.getBlockState(), tile, side.getOpposite()) != null;
    }

    @Override
    protected PipeModel createPipeModel() {
        return pipeType.createPipeModel(material);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        WireProperties wireProperties = createProperties(defaultBlockState(), stack);
        int tier = GTUtil.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor())
            tooltip.add(Component.translatable("gtceu.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage", wireProperties.getVoltage(), GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage", wireProperties.getAmperage()));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block", wireProperties.getLossPerBlock()));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        // dont apply damage if there is a frame box
        var pipeNode = getPipeTile(level, pos);
        if (pipeNode.getFrameMaterial() != null) {
            BlockState frameState = GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, pipeNode.getFrameMaterial())
                    .getDefaultState();
            ((MaterialBlock)frameState.getBlock()).entityInside(frameState, level, pos, entity);
            return;
        }
        if (level.isClientSide) return;

        Insulation insulation = getPipeTile(level, pos).getPipeType();
        if (insulation.insulationLevel == -1 && entity instanceof LivingEntity entityLiving) {
            CableBlockEntity cable = (CableBlockEntity) getPipeTile(level, pos);
            if (cable != null && cable.getFrameMaterial() == null && cable.getNodeData().getLossPerBlock() > 0) {
                long voltage = cable.getCurrentMaxVoltage();
                double amperage = cable.getAverageAmperage();
                if (voltage > 0L && amperage > 0L) {
                    float damageAmount = (float) ((GTUtil.getTierByVoltage(voltage) + 1) * amperage * 4);
                    entityLiving.hurt(GTDamageTypes.ELECTRIC.source(level), damageAmount);
                    if (entityLiving instanceof ServerPlayer) {
                        // TODO advancments
                        // AdvancementTriggers.ELECTROCUTION_DEATH.trigger((EntityPlayerMP) entityLiving);
                    }
                }
            }
        }
    }
}
