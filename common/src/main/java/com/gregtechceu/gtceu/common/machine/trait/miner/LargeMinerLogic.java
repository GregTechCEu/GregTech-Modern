package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import javax.annotation.Nonnull;

public class LargeMinerLogic extends MinerLogic {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LargeMinerLogic.class, MinerLogic.MANAGED_FIELD_HOLDER);

    private static final int CHUNK_LENGTH = 16;

    private final GTRecipeType blockDropRecipeMap;

    @Setter
    private int voltageTier;
    @Getter @Setter
    private int overclockAmount = 0;

    @Getter
    @Persisted
    private boolean isChunkMode;
    @Getter
    @Persisted
    private boolean isSilkTouchMode;

    /**
     * Creates the logic for multiblock ore block miners
     *
     * @param machine the {@link IRecipeLogicMachine} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public LargeMinerLogic(IRecipeLogicMachine machine, int fortune, int speed, int maximumRadius, IModelRenderer pipeTexture, GTRecipeType blockDropRecipeMap) {
        super(machine, fortune, speed, maximumRadius, pipeTexture);
        this.blockDropRecipeMap = blockDropRecipeMap;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected boolean drainStorages(boolean simulate) {
        return super.drainStorages(simulate) && miner.drainFluid(simulate);
    }

    @Override
    protected void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, BlockState blockState) {
        // Small ores: use (fortune bonus + overclockAmount) value here for fortune, since every overclock increases the yield for small ores
        super.getSmallOreBlockDrops(blockDrops, world, blockToMine, blockState);
    }

    @Override
    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, @Nonnull BlockState blockState) {
        if (!isSilkTouchMode) // 3X the ore compared to the single blocks
            applyTieredHammerNoRandomDrops(this, world, blockToMine, blockState, blockDrops, 3, this.blockDropRecipeMap, this.voltageTier);
        else
            this.getSilkTouchDrops(blockDrops, world, blockToMine, blockState);
    }

    protected void getNormalRegularBlockDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, @Nonnull BlockState blockState) {
        super.getRegularBlockDrops(blockDrops, world, blockToMine, blockState);
    }

    protected void multiplyDrops(NonNullList<ItemStack> drops, int dropCountMultiplier) {
        for (ItemStack drop : drops) {
            drop.setCount(drop.getCount() * dropCountMultiplier);
        }
    }

    @Override
    public void initPos(@Nonnull BlockPos pos, int currentRadius) {
        if (!isChunkMode) {
            super.initPos(pos, currentRadius);
        } else {
            ServerLevel world = (ServerLevel) this.getMachine().getLevel();
            ChunkAccess origin = world.getChunk(pos);
            ChunkPos startPos = (world.getChunk(origin.getPos().x - currentRadius / CHUNK_LENGTH, origin.getPos().z - currentRadius / CHUNK_LENGTH)).getPos();
            x = startPos.getMinBlockX();
            y = pos.getY() - 1;
            z = startPos.getMinBlockZ();
            startX = startPos.getMinBlockX();
            startY = pos.getY();
            startZ = startPos.getMinBlockZ();
            mineX = startPos.getMinBlockX();
            mineY = pos.getY() - 1;
            mineZ = startPos.getMinBlockZ();
            pipeY = pos.getY() - 1;
        }
    }

    public void setChunkMode(boolean isChunkMode) {
        if (!isWorking()) {
            this.isChunkMode = isChunkMode;
            if (!getMachine().isRemote()) {
                resetArea();
            }
        }
    }

    public void setSilkTouchMode(boolean isSilkTouchMode) {
        if (!isWorking()) {
            this.isSilkTouchMode = isSilkTouchMode;
        }
    }

    @Override
    public BlockPos getMiningPos() {
        return getMachine().getPos().relative(getMachine().getFrontFacing().getOpposite());
    }
}
