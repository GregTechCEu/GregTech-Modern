package com.lowdragmc.gtceu.common.block;

import com.lowdragmc.gtceu.api.GTValues;
import com.lowdragmc.gtceu.api.block.MaterialPipeBlock;
import com.lowdragmc.gtceu.api.blockentity.PipeBlockEntity;
import com.lowdragmc.gtceu.api.capability.ICoverable;
import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.lowdragmc.gtceu.api.data.chemical.material.properties.WireProperties;
import com.lowdragmc.gtceu.api.item.tool.GTToolType;
import com.lowdragmc.gtceu.client.model.PipeModel;
import com.lowdragmc.gtceu.common.item.CoverPlaceBehavior;
import com.lowdragmc.gtceu.common.libs.GTBlockEntities;
import com.lowdragmc.gtceu.common.pipelike.cable.CableData;
import com.lowdragmc.gtceu.common.pipelike.cable.Insulation;
import com.lowdragmc.gtceu.common.pipelike.cable.LevelEnergyNet;
import com.lowdragmc.gtceu.utils.GTUtil;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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
public class CableBlock extends MaterialPipeBlock<Insulation, CableData, LevelEnergyNet> {

    public CableBlock(Properties properties, Insulation insulation, Material material) {
        super(properties, insulation, material);
    }

    @Override
    public int tinted(BlockState blockState, @Nullable BlockAndTintGetter blockAndTintGetter, @Nullable BlockPos blockPos, int index) {
        if (pipeType.isCable) {
            return 0x404040;
        }
        return material.getMaterialRGB();
    }

    @Override
    protected CableData createMaterialData() {
        return new CableData(material.getProperty(PropertyKey.WIRE), (byte) 0);
    }

    @Override
    public LevelEnergyNet getWorldPipeNet(ServerLevel level) {
        return LevelEnergyNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<Insulation, CableData>> getBlockEntityType() {
        return GTBlockEntities.CABLE.get();
    }

    @Override
    protected PipeModel createPipeModel() {
        return pipeType.createPipeModel(material);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        WireProperties wireProperties = createRawData(defaultBlockState(), stack).properties();
        int tier = GTUtil.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor()) tooltip.add(Component.translatable("gtceu.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gtceu.cable.voltage", wireProperties.getVoltage(), GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gtceu.cable.amperage", wireProperties.getAmperage()));
        tooltip.add(Component.translatable("gtceu.cable.loss_per_block", wireProperties.getLossPerBlock()));
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext context) {
        var pipeNode = getPileTile(pLevel, pPos);
        if (context instanceof EntityCollisionContext entityCtx && entityCtx.getEntity() instanceof Player player && pipeNode != null){
            var coverable = pipeNode.getCoverContainer();
            var held = player.getMainHandItem();
            if (held.is(GTToolType.WIRE_CUTTER.itemTag) || held.is(GTToolType.WRENCH.itemTag) ||
                    CoverPlaceBehavior.isCoverBehaviorItem(held, coverable::hasAnyCover, coverDef -> ICoverable.canPlaceCover(coverDef, coverable))) {
                return Shapes.block();
            }
        }
        return super.getShape(pState, pLevel, pPos, context);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
