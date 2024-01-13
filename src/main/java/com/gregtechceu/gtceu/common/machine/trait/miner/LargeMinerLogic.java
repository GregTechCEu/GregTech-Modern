package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import javax.annotation.Nonnull;
import java.util.List;

public class LargeMinerLogic extends MinerLogic {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LargeMinerLogic.class, MinerLogic.MANAGED_FIELD_HOLDER);
    private static final int CHUNK_LENGTH = 16;

    @Setter @Getter
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
    public LargeMinerLogic(IRecipeLogicMachine machine, int fortune, int speed, int maximumRadius) {
        super(machine, fortune, speed, maximumRadius);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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

    private int getDropCountMultiplier() {
        return 3;
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

    @Override
    protected boolean hasPostProcessing() {
        return !isSilkTouchMode;
    }

    @Override
    protected void dropPostProcessing(NonNullList<ItemStack> blockDrops, List<ItemStack> outputs, BlockState blockState, LootParams.Builder builder) {
        for (ItemStack outputStack : outputs) {
            if (ChemicalHelper.getPrefix(outputStack.getItem()) == TagPrefix.crushed) {
                if (getDropCountMultiplier() > 0) {
                    ItemStack fortunePick = pickaxeTool.copy();
                    fortunePick.enchant(Enchantments.BLOCK_FORTUNE, getDropCountMultiplier());
                    outputStack = ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE).build().apply(outputStack,
                            new LootContext.Builder(builder.withParameter(LootContextParams.TOOL, fortunePick).create(LootContextParamSets.BLOCK)).create(null));
                }
            }
            blockDrops.add(outputStack);
        }
    }

    @Override
    protected boolean doPostProcessing(NonNullList<ItemStack> blockDrops, BlockState blockState, LootParams.Builder builder) {
        if (!super.doPostProcessing(blockDrops, blockState, builder) && getDropCountMultiplier() > 0) {
            for (ItemStack drop : blockDrops) {
                drop.setCount(drop.getCount() * getDropCountMultiplier());
            }
        }
        return true;
    }
}
