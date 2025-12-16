package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LargeMinerLogic extends MinerLogic {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(LargeMinerLogic.class,
            MinerLogic.MANAGED_FIELD_HOLDER);
    private static final int CHUNK_LENGTH = 16;
    private static final LootItemFunction DROP_MULTIPLIER = ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)
            .build();

    @Setter
    @Getter
    private int voltageTier;
    @Getter
    @Setter
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
     * @param machine       the {@link IRecipeLogicMachine} this logic belongs to
     * @param fortune       the fortune amount to apply when mining ores
     * @param speed         the speed in ticks per block mined
     * @param maximumRadius the maximum radius (square shaped) the miner can mine in
     */
    public LargeMinerLogic(IRecipeLogicMachine machine, int fortune, int speed, int maximumRadius) {
        super(machine, fortune, speed, maximumRadius);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void initPos(@NotNull BlockPos pos, int currentRadius) {
        if (!isChunkMode) {
            super.initPos(pos, currentRadius);
        } else {
            Direction dir = super.getDir();
            ServerLevel world = (ServerLevel) this.getMachine().getLevel();
            ChunkAccess origin = world.getChunk(pos);
            ChunkPos startPos = (world.getChunk(origin.getPos().x - currentRadius / CHUNK_LENGTH,
                    origin.getPos().z - currentRadius / CHUNK_LENGTH)).getPos();
            x = startPos.getMinBlockX();
            if (dir == Direction.UP) {
                y = pos.getY() + 1;
            } else {
                y = pos.getY() - 1;
            }
            z = startPos.getMinBlockZ();
            startX = startPos.getMinBlockX();
            startY = pos.getY();
            startZ = startPos.getMinBlockZ();
            mineX = startPos.getMinBlockX();
            if (dir == Direction.UP) {
                mineY = pos.getY() + 1;
            } else {
                mineY = pos.getY() - 1;
            }
            mineZ = startPos.getMinBlockZ();
            if (dir == Direction.UP) {
                pipeY = pos.getY() + 1;
            } else {
                pipeY = pos.getY() - 1;
            }
        }
    }

    private int getDropCountMultiplier() {
        return 5;
    }

    public void setChunkMode(boolean isChunkMode) {
        if (!isWorking()) {
            this.isChunkMode = isChunkMode;
            if (!getMachine().isRemote()) {
                resetArea(true);
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
    protected void dropPostProcessing(NonNullList<ItemStack> blockDrops, List<ItemStack> outputs, BlockState blockState,
                                      LootParams.Builder builder) {
        if (getDropCountMultiplier() <= 0) {
            super.dropPostProcessing(blockDrops, outputs, blockState, builder);
            return;
        }
        ItemStack fortunePick = this.pickaxeTool.copy();
        fortunePick.enchant(Enchantments.BLOCK_FORTUNE, getDropCountMultiplier());
        LootParams params = builder.withParameter(LootContextParams.TOOL, fortunePick)
                .create(LootContextParamSets.BLOCK);
        LootContext context = new LootContext.Builder(params).create(null);

        for (ItemStack outputStack : outputs) {
            if (ChemicalHelper.getPrefix(outputStack.getItem()) == TagPrefix.crushed) {
                outputStack = DROP_MULTIPLIER.apply(outputStack, context);
            }
            blockDrops.add(outputStack);
        }
    }
}
