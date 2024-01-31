package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.client.model.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.pipelike.cable.CableData;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.common.pipelike.cable.LevelEnergyNet;
import com.gregtechceu.gtceu.utils.GTUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

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

    @Override
    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
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
    public boolean canPipesConnect(IPipeNode<Insulation, WireProperties> selfTile, Direction side, IPipeNode<Insulation, WireProperties> sideTile) {
        return selfTile instanceof CableBlockEntity && sideTile instanceof CableBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<Insulation, WireProperties> selfTile, Direction side, @Nullable BlockEntity tile) {
        return tile != null && tile.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, side.getOpposite()).isPresent();
    }

    @Override
    protected PipeModel createPipeModel() {
        return pipeType.createPipeModel(material);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        WireProperties wireProperties = createProperties(defaultBlockState(), stack);
        int tier = GTUtil.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor()) tooltip.add(Component.translatable("gtceu.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage", wireProperties.getVoltage(), GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage", wireProperties.getAmperage()));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block", wireProperties.getLossPerBlock()));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
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
                        //AdvancementTriggers.ELECTROCUTION_DEATH.trigger((EntityPlayerMP) entityLiving);
                    }
                }
            }
        }
    }
}
