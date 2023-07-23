package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.pipenet.longdist.ILDEndpoint;
import com.gregtechceu.gtceu.api.pipenet.longdist.LongDistanceNetwork;
import com.gregtechceu.gtceu.api.pipenet.longdist.LongDistancePipeType;
import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockLongDistancePipe extends Block {

    private final LongDistancePipeType pipeType;

    public BlockLongDistancePipe(LongDistancePipeType pipeType) {
        super(Material.IRON);
        this.pipeType = pipeType;
        setTranslationKey("long_distance_" + pipeType.getName() + "_pipeline");
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    @Override
    public void onBlockPlacedBy(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, @Nonnull ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (worldIn.isRemote) return;
        // first find all neighbouring networks
        List<LongDistanceNetwork> networks = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(pos).move(facing);
            LongDistanceNetwork network = LongDistanceNetwork.get(worldIn, offsetPos);
            if (network != null && pipeType == network.getPipeType()) {
                ILDEndpoint endpoint = ILDEndpoint.tryGet(worldIn, offsetPos);
                // only count the network as connected if it's not an endpoint or the endpoints input or output face is connected
                if (endpoint == null || endpoint.getFrontFacing().getAxis() == facing.getAxis()) {
                    networks.add(network);
                }
            }
        }
        offsetPos.release();
        if (networks.isEmpty()) {
            // create network
            LongDistanceNetwork network = this.pipeType.createNetwork(worldIn);
            network.onPlacePipe(pos);
        } else if (networks.size() == 1) {
            // add to connected network
            networks.get(0).onPlacePipe(pos);
        } else {
            // merge all connected networks together
            LongDistanceNetwork main = networks.get(0);
            main.onPlacePipe(pos);
            networks.remove(0);
            for (LongDistanceNetwork network : networks) {
                main.mergePipeNet(network);
            }
        }
    }

    @Override
    public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (worldIn.isRemote) return;
        LongDistanceNetwork network = LongDistanceNetwork.get(worldIn, pos);
        if (network != null) {
            network.onRemovePipe(pos);
        }
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
        if (itemIn == GregTechAPI.TAB_GREGTECH) {
            items.add(new ItemStack(this));
        }
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("gregtech.block.tooltip.no_mob_spawning"));
    }
}
