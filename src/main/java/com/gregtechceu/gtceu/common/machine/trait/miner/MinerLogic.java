package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.item.MaterialBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.IgnoreEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.misc.ItemRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.transfer.item.NotifiableAccountedInvWrapper;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MinerLogic extends RecipeLogic implements IRecipeCapabilityHolder {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MinerLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);
    private static final short MAX_SPEED = Short.MAX_VALUE;
    private static final byte POWER = 5;
    private static final byte TICK_TOLERANCE = 20;
    private static final double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);
    protected final IMiner miner;
    @Nullable
    private NotifiableAccountedInvWrapper cachedItemHandler = null;
    @Getter
    private final int fortune;
    @Getter
    private final int speed;
    @Getter
    private final int maximumRadius;
    @Getter
    public ItemStack pickaxeTool;
    private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();
    private int blocksToMineOriginalCount = 0;
    @Getter
    @Persisted
    protected int x = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int y = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int z = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int startX = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int startZ = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int startY = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int pipeY = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int mineX = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int mineZ = Integer.MAX_VALUE;
    @Getter
    @Persisted
    protected int mineY = Integer.MAX_VALUE;
    @Getter
    private int minBuildHeight = Integer.MAX_VALUE;
    @Getter
    private int maxBuildHeight = Integer.MAX_VALUE;
    @Getter
    @Persisted
    private int pipeLength = 0;
    @Getter
    @Setter
    @Persisted
    private int currentRadius;
    @Getter
    @Persisted
    private boolean isDone;
    @Getter
    private boolean isInventoryFull;
    @Getter
    private final Map<IO, List<RecipeHandlerList>> capabilitiesProxy;
    @Getter
    protected final Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> capabilitiesFlat;
    private final ItemRecipeHandler inputItemHandler, outputItemHandler;
    private final IgnoreEnergyRecipeHandler inputEnergyHandler;
    @Setter
    @Getter
    private Direction dir = Direction.DOWN;

    /**
     * Creates the general logic for all in-world ore block miners
     *
     * @param machine       the {@link MetaMachine} this logic belongs to
     * @param fortune       the fortune amount to apply when mining ores
     * @param speed         the speed in ticks per block mined
     * @param maximumRadius the maximum radius (square shaped) the miner can mine in
     */
    public MinerLogic(@NotNull IRecipeLogicMachine machine, int fortune, int speed, int maximumRadius) {
        super(machine);
        this.miner = (IMiner) machine;
        this.fortune = fortune;
        this.speed = speed;
        this.currentRadius = maximumRadius;
        this.maximumRadius = maximumRadius;
        this.isDone = false;
        this.pickaxeTool = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Neutronium, GTToolType.PICKAXE).get().get();
        this.capabilitiesProxy = new EnumMap<>(IO.class);
        this.capabilitiesFlat = new EnumMap<>(IO.class);
        this.inputItemHandler = new ItemRecipeHandler(IO.IN,
                machine.getRecipeType().getMaxInputs(ItemRecipeCapability.CAP));
        this.outputItemHandler = new ItemRecipeHandler(IO.OUT,
                machine.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP));
        this.inputEnergyHandler = new IgnoreEnergyRecipeHandler();

        RecipeHandlerList inHandlers = RecipeHandlerList.of(IO.IN, inputItemHandler, inputEnergyHandler);
        RecipeHandlerList outHandlers = RecipeHandlerList.of(IO.OUT, outputItemHandler);

        addHandlerList(inHandlers);
        addHandlerList(outHandlers);
    }

    @Override
    public void resetRecipeLogic() {
        super.resetRecipeLogic();
        resetArea(false);
        this.cachedItemHandler = null;
        this.pipeLength = 0;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void inValid() {
        super.inValid();
        this.cachedItemHandler = null;
        this.pipeLength = 0;
    }

    private static BlockState findMiningReplacementBlock(Level level, BlockPos pos) {
        if (ConfigHolder.INSTANCE.machines.replaceWithCobbleVersion) {
            BlockState oreState = level.getBlockState(pos);
            if (oreState.getBlock().asItem() instanceof MaterialBlockItem matBlockItem) {
                var prefix = matBlockItem.tagPrefix;
                if (!GTBlocks.COBBLE_BLOCKS.containsKey(prefix)) return Blocks.COBBLESTONE.defaultBlockState();
                return GTBlocks.COBBLE_BLOCKS.get(prefix).get();
            }
        }

        try {
            return BlockStateParser.parseForBlock(level.holderLookup(Registries.BLOCK),
                    ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith, false).blockState();
        } catch (CommandSyntaxException ignored) {
            GTCEu.LOGGER.error("failed to parse replaceMinedBlocksWith, invalid BlockState: {}",
                    ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith);
            return Blocks.COBBLESTONE.defaultBlockState();
        }
    }

    /**
     * Performs the actual mining in world
     * Call this method every tick in update
     */
    public void serverTick() {
        if (!isSuspend() && getMachine().getLevel() instanceof ServerLevel serverLevel && checkCanMine()) {
            // if the inventory is not full, drain energy etc. from the miner
            // the storages have already been checked earlier
            if (!isInventoryFull()) {
                // always drain storages when working, even if blocksToMine ends up being empty
                miner.drainInput(false);
                // since energy is being consumed the miner is now active
                setStatus(Status.WORKING);
            } else {
                // the miner cannot drain, therefore it is inactive
                if (this.isWorking()) {
                    setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ")
                            .append(ItemRecipeCapability.CAP.getName()));
                }
            }

            // drill a hole beneath the miner and extend the pipe downwards by one
            if ((dir == Direction.DOWN && mineY < pipeY) || (dir == Direction.UP && mineY > pipeY)) {
                var miningPos = getMiningPos();
                var pipePos = new BlockPos(miningPos.getX(), pipeY, miningPos.getZ());
                if (serverLevel.getBlockState(pipePos).getDestroySpeed(serverLevel, pipePos) < 0) {
                    isDone = true;
                    setStatus(Status.IDLE);
                    return;
                }
                serverLevel.destroyBlock(pipePos, false);
                if (dir == Direction.UP) {
                    ++pipeY;
                } else {
                    --pipeY;
                }
                incrementPipeLength();
            }

            // check if the miner needs new blocks to mine and get them if needed
            checkBlocksToMine();

            // if there are blocks to mine and the correct amount of time has passed, do the mining
            if (getMachine().getOffsetTimer() % this.speed == 0 && !blocksToMine.isEmpty()) {
                NonNullList<ItemStack> blockDrops = NonNullList.create();
                BlockState blockState = serverLevel.getBlockState(blocksToMine.getFirst());

                // check to make sure the ore is still there,
                while (!blockState.is(Tags.Blocks.ORES)) {
                    blocksToMine.removeFirst();
                    if (blocksToMine.isEmpty()) break;
                    blockState = serverLevel.getBlockState(blocksToMine.getFirst());
                }
                // When we are here we have an ore to mine! I'm glad we aren't threaded
                if (!blocksToMine.isEmpty() & blockState.is(Tags.Blocks.ORES)) {
                    LootParams.Builder builder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.BLOCK_STATE, blockState)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(blocksToMine.getFirst()))
                            .withParameter(LootContextParams.TOOL, getPickaxeTool());

                    // get the small ore drops, if a small ore
                    getSmallOreBlockDrops(blockDrops, blockState, builder);
                    // get the block's drops.
                    if (isSilkTouchMode()) {
                        getSilkTouchDrops(blockDrops, blockState, builder);
                    } else {
                        getRegularBlockDrops(blockDrops, blockState, builder);
                    }
                    // handle recipe type
                    if (hasPostProcessing()) {
                        doPostProcessing(blockDrops, blockState, builder);
                    }
                    // try to insert them
                    mineAndInsertItems(blockDrops, serverLevel);
                }
            }

            if (blocksToMine.isEmpty()) {
                // there were no blocks to mine, so the current position is the previous position
                x = mineX;
                y = mineY;
                z = mineZ;

                // attempt to get more blocks to mine, if there are none, the miner is done mining
                blocksToMine.addAll(getBlocksToMine());
                if (blocksToMine.isEmpty()) {
                    this.isDone = true;
                    this.setStatus(Status.IDLE);
                }
            }
        } else {
            // machine isn't working enabled
            this.setStatus(Status.IDLE);
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    /**
     * @return true if the miner is able to mine, else false
     */
    protected boolean checkCanMine() {
        // if the miner is finished, the target coordinates are invalid, or it cannot drain storages, stop
        // if the miner is not finished and has invalid coordinates, get new and valid starting coordinates
        if (!isDone && checkCoordinatesInvalid()) {
            initPos(getMiningPos(), currentRadius);
        }
        return !isDone && miner.drainInput(true);
    }

    /**
     * Called after each block is mined, used to perform additional actions afterwards
     */
    protected void onMineOperation() {}

    /**
     * called to handle mining small ores
     *
     * @param blockDrops the List of items to fill after the operation
     * @param blockState the {@link BlockState} of the block being mined
     */
    // todo implement small ores
    protected void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                         LootParams.Builder builder) {
        /*
         * small ores
         * if orePrefix of block in blockPos is small
         * applyTieredHammerNoRandomDrops...
         * else
         * current code...
         */
    }

    /**
     * Should we apply additional processing according to the recipe type.
     */
    protected boolean hasPostProcessing() {
        return false;
    }

    protected boolean isSilkTouchMode() {
        return false;
    }

    /**
     * called to handle mining regular ores and blocks
     *
     * @param blockDrops the List of items to fill after the operation
     * @param blockState the {@link BlockState} of the block being mined
     */
    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                        LootParams.Builder builder) {
        blockDrops.addAll(blockState.getDrops(builder));
    }

    protected int getVoltageTier() {
        return 0;
    }

    protected boolean doPostProcessing(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                       LootParams.Builder builder) {
        ItemStack oreDrop = new ItemStack(blockState.getBlock());
        if (oreDrop.isEmpty()) return false;

        // create dummy recipe handler
        inputItemHandler.storage.setStackInSlot(0, oreDrop);
        outputItemHandler.storage.clear();

        var matches = machine.getRecipeType().searchRecipe(this, r -> RecipeHelper.matchContents(this, r).isSuccess());

        GTRecipe recipe = null; // attempt ore block that has a static gt recipe
        while (matches.hasNext()) {
            GTRecipe match = matches.next();
            if (match == null) continue;
            recipe = match;
            break;
        }

        if (recipe != null) {
            long eut = recipe.getInputEUt().getTotalEU();
            if (GTUtil.getTierByVoltage(eut) <= getVoltageTier()) {
                if (RecipeHelper.handleRecipeIO(this, recipe, IO.OUT, this.chanceCaches).isSuccess()) {
                    blockDrops.clear();
                    var result = new ArrayList<ItemStack>();
                    for (int i = 0; i < outputItemHandler.storage.getSlots(); ++i) {
                        var stack = outputItemHandler.storage.getStackInSlot(i);
                        if (stack.isEmpty()) continue;
                        result.add(stack);
                    }
                    dropPostProcessing(blockDrops, result, blockState, builder);
                    return true;
                }
            }
        }
        return false;
    }

    protected void dropPostProcessing(NonNullList<ItemStack> blockDrops, List<ItemStack> outputs, BlockState blockState,
                                      LootParams.Builder builder) {
        blockDrops.addAll(outputs);
    }

    /**
     * called to handle mining regular ores and blocks with silk touch
     *
     * @param blockDrops the List of items to fill after the operation
     * @param blockState the {@link BlockState} of the block being mined
     */
    protected void getSilkTouchDrops(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                     LootParams.Builder builder) {
        blockDrops.add(new ItemStack(blockState.getBlock()));
    }

    protected NotifiableAccountedInvWrapper getCachedItemHandler() {
        if (cachedItemHandler == null) {
            cachedItemHandler = new NotifiableAccountedInvWrapper(machine
                    .getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).stream()
                    .map(IItemHandlerModifiable.class::cast)
                    .toArray(IItemHandlerModifiable[]::new));
        }
        return cachedItemHandler;
    }

    /**
     * called in order to insert the mined items into the inventory and actually remove the block in world
     * marks the inventory as full if the items cannot fit, and not full if it previously was full and items could fit
     *
     * @param blockDrops the List of items to insert
     * @param world      the {@link ServerLevel} the miner is in
     */
    private void mineAndInsertItems(NonNullList<ItemStack> blockDrops, ServerLevel world) {
        // If the block's drops can fit in the inventory, move the previously mined position to the block
        // replace the ore block with cobblestone instead of breaking it to prevent mob spawning
        // remove the ore block's position from the mining queue
        var handler = getCachedItemHandler();
        if (handler != null) {
            if (GTTransferUtils.addItemsToItemHandler(handler, true, blockDrops)) {
                GTTransferUtils.addItemsToItemHandler(handler, false, blockDrops);
                var pos = blocksToMine.getFirst();
                world.setBlock(pos, findMiningReplacementBlock(world, pos), 3);
                mineX = pos.getX();
                mineZ = pos.getZ();
                mineY = pos.getY();
                blocksToMine.removeFirst();
                onMineOperation();

                // if the inventory was previously considered full, mark it as not since an item was able to fit
                isInventoryFull = false;
            } else {
                // the ore block was not able to fit, so the inventory is considered full
                isInventoryFull = true;
            }
        }
    }

    /**
     * This method designates the starting position for mining blocks
     *
     * @param pos           the {@link BlockPos} of the miner itself
     * @param currentRadius the currently set mining radius
     */
    public void initPos(@NotNull BlockPos pos, int currentRadius) {
        x = pos.getX() - currentRadius;
        z = pos.getZ() - currentRadius;
        if (dir == Direction.UP) {
            y = pos.getY() + 1;
        } else {
            y = pos.getY() - 1;
        }
        startX = pos.getX() - currentRadius;
        startZ = pos.getZ() - currentRadius;
        startY = pos.getY();
        if (dir == Direction.UP) {
            pipeY = pos.getY() + 1;
        } else {
            pipeY = pos.getY() - 1;
        }
        mineX = pos.getX() - currentRadius;
        mineZ = pos.getZ() - currentRadius;
        if (dir == Direction.UP) {
            mineY = pos.getY() + 1;
        } else {
            mineY = pos.getY() - 1;
        }
        onRemove();
    }

    /**
     * Checks if the current coordinates are invalid
     *
     * @return {@code true} if the coordinates are invalid, else false
     */
    private boolean checkCoordinatesInvalid() {
        return x == Integer.MAX_VALUE && y == Integer.MAX_VALUE && z == Integer.MAX_VALUE;
    }

    /**
     * Checks whether there are any more blocks to mine, if there are currently none queued
     */
    public void checkBlocksToMine() {
        if (blocksToMine.isEmpty()) {
            blocksToMine.addAll(getBlocksToMine());
            blocksToMineOriginalCount = blocksToMine.size();
        }
    }

    /**
     * Recalculates the mining area, refills the block list and restarts the miner, if it was done
     */
    public void resetArea(boolean checkToMine) {
        initPos(getMiningPos(), currentRadius);
        if (this.isDone) this.setWorkingEnabled(false);
        this.isDone = false;
        if (checkToMine) {
            blocksToMine.clear();
            checkBlocksToMine();
        }
    }

    /**
     * Gets the blocks to mine
     *
     * @return a {@link LinkedList} of {@link BlockPos} for each ore to mine
     */
    private LinkedList<BlockPos> getBlocksToMine() {
        LinkedList<BlockPos> blocks = new LinkedList<>();

        // determine how many blocks to retrieve this time
        var level = getMachine().getLevel();
        assert level != null;
        double quotient = getQuotient(getMeanTickTime(level));
        int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        int calculated = 0;

        if (this.minBuildHeight == Integer.MAX_VALUE)
            this.minBuildHeight = level.getMinBuildHeight();

        if (this.maxBuildHeight == Integer.MAX_VALUE)
            this.maxBuildHeight = level.getMaxBuildHeight();

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y > minBuildHeight && y < maxBuildHeight) {
                // moving across the z-axis
                if (z <= startZ + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x <= startX + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockState state = level.getBlockState(blockPos);
                        if (state.getDestroySpeed(level, blockPos) >= 0 &&
                                level.getBlockEntity(blockPos) == null &&
                                state.is(Tags.Blocks.ORES)) {
                            blocks.addLast(blockPos);
                        }
                        // move to the next x position
                        ++x;
                    } else {
                        // reset x and move to the next z layer
                        x = startX;
                        ++z;
                    }
                } else {
                    // reset z and move to the next y layer
                    z = startZ;
                    if (dir == Direction.UP) {
                        ++y;
                    } else {
                        --y;
                    }
                }
            } else
                return blocks;

            // only count iterations where blocks were found
            if (!blocks.isEmpty())
                calculated++;
        }
        return blocks;
    }

    /**
     * @param values to find the mean of
     * @return the mean value
     */
    private static long mean(@NotNull long[] values) {
        if (values.length == 0L)
            return 0L;

        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    /**
     * @param world the {@link Level} to get the average tick time of
     * @return the mean tick time
     */
    private static double getMeanTickTime(@NotNull Level world) {
        return mean(Objects.requireNonNull(world.getServer()).tickTimes) * 1.0E-6D;
    }

    /**
     * gets the quotient for determining the amount of blocks to mine
     *
     * @param base is a value used for calculation, intended to be the mean tick time of the world the miner is in
     * @return the quotient
     */
    private static double getQuotient(double base) {
        return DIVIDEND / Math.pow(base, POWER);
    }

    /**
     * Increments the pipe rendering length by one, signaling that the miner's y level has moved down by one
     */
    private void incrementPipeLength() {
        this.pipeLength++;
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            var pos = getMiningPos().relative(dir, this.pipeLength);
            serverLevel.setBlockAndUpdate(pos, GTBlocks.MINER_PIPE.getDefaultState());
        }
    }

    /**
     * @return the position to start mining from
     */
    public BlockPos getMiningPos() {
        return getMachine().getPos();
    }

    public void onRemove() {
        pipeLength = 0;
        if (getMachine().getLevel() instanceof ServerLevel serverLevel) {
            var pos = getMiningPos().relative(dir);
            while (serverLevel.getBlockState(pos).is(GTBlocks.MINER_PIPE.get())) {
                serverLevel.removeBlock(pos, false);
                pos = pos.relative(dir);
            }
        }
    }

    @Override
    public boolean hasCustomProgressLine() {
        return true;
    }

    @Nullable
    @Override
    public Component getCustomProgressLine() {
        return Component.translatable("gtceu.machine.miner.progress", blocksToMineOriginalCount - blocksToMine.size(),
                blocksToMineOriginalCount);
    }
}
