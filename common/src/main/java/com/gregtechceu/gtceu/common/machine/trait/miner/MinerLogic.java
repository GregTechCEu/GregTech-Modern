package com.gregtechceu.gtceu.common.machine.trait.miner;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.DummyRecipeCapabilityHolder;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.client.renderer.impl.IModelRenderer;
import com.lowdragmc.lowdraglib.misc.ItemTransferList;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

public class MinerLogic extends RecipeLogic {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MinerLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);
    public static final ItemStack PICKAXE_TOOL = GTItems.TOOL_ITEMS.get(GTMaterials.Neutronium.getToolTier(), GTToolType.PICKAXE).asStack();

    private static final short MAX_SPEED = Short.MAX_VALUE;
    private static final byte POWER = 5;
    private static final byte TICK_TOLERANCE = 20;
    private static final double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);

    private final IModelRenderer pipeModel;
    private final ItemStack pickaxeToolFortune = PICKAXE_TOOL;
    private final DummyRecipeCapabilityHolder breakRecipeSearchHolder;

    protected final IMiner miner;

    @Nullable
    private ItemTransferList cachedItemTransfer = null;

    private final int fortune;
    private final int speed;
    private final int maximumRadius;

    private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();

    @Persisted
    protected int x = Integer.MAX_VALUE;
    @Persisted
    protected int y = Integer.MAX_VALUE;
    @Persisted
    protected int z = Integer.MAX_VALUE;
    @Persisted
    protected int startX = Integer.MAX_VALUE;
    @Persisted
    protected int startZ = Integer.MAX_VALUE;
    @Persisted
    protected int startY = Integer.MAX_VALUE;
    @Persisted
    protected int pipeY = Integer.MAX_VALUE;
    @Persisted
    protected int mineX = Integer.MAX_VALUE;
    @Persisted
    protected int mineZ = Integer.MAX_VALUE;
    @Persisted
    protected int mineY = Integer.MAX_VALUE;

    private int minBuildHeight = Integer.MAX_VALUE;

    @Getter
    @Persisted @DescSynced @RequireRerender
    private int pipeLength = 0;
    @Persisted
    private int currentRadius;
    @Persisted
    private boolean isDone;

    /**
     * Creates the general logic for all in-world ore block miners
     *
     * @param machine the {@link MetaMachine} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public MinerLogic(@Nonnull IRecipeLogicMachine machine, int fortune, int speed, int maximumRadius, IModelRenderer pipeModel) {
        super(machine);
        this.miner = (IMiner) machine;
        this.fortune = fortune;
        this.speed = speed;
        this.currentRadius = maximumRadius;
        this.maximumRadius = maximumRadius;
        this.isDone = false;
        pickaxeToolFortune.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        this.pipeModel = Platform.isClient() ? pipeModel : null;

        this.breakRecipeSearchHolder = new DummyRecipeCapabilityHolder(getMachine().getHolder());
        this.breakRecipeSearchHolder.addCapability(IO.IN, ItemRecipeCapability.CAP, List.of(new NotifiableItemStackHandler(breakRecipeSearchHolder, 1, IO.IN)));
        this.breakRecipeSearchHolder.addCapability(IO.OUT, ItemRecipeCapability.CAP, List.of(new NotifiableItemStackHandler(breakRecipeSearchHolder, 10, IO.OUT)));
        this.breakRecipeSearchHolder.addCapability(IO.IN, EURecipeCapability.CAP, List.of(new NotifiableEnergyContainer(breakRecipeSearchHolder, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE) {
            @Override
            public long getEnergyStored() {
                return Integer.MAX_VALUE;
            }

            @Override
            public void setEnergyStored(long energyStored) {

            }

            @Override
            public long changeEnergy(long energyToAdd) {
                return 0;
            }
        }));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void inValid() {
        super.inValid();
        cachedItemTransfer = null;
        this.pipeLength = 0;
    }

    private static BlockState oreReplacementBlock = findMiningReplacementBlock();

    private static BlockState findMiningReplacementBlock() {
        if (oreReplacementBlock == null) {
            try {
                oreReplacementBlock = BlockStateParser.parseForBlock(Registry.BLOCK, ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith, false).blockState();
            } catch (CommandSyntaxException ignored) {
                GTCEu.LOGGER.error("failed to parse replaceMinedBlocksWith, invalid BlockState: {}", ConfigHolder.INSTANCE.machines.replaceMinedBlocksWith);
                oreReplacementBlock = Blocks.COBBLESTONE.defaultBlockState();
            }
        }

        return oreReplacementBlock;
    }

    /**
     * Performs the actual mining in world
     * Call this method every tick in update
     */
    public void serverTick() {
        // Needs to be server side
        if (getMachine().isRemote())
            return;

        // Inactive miners do nothing
        if (!this.isWorkingEnabled())
            return;

        // check if mining is possible
        if (!checkCanMine())
            return;

        // if the inventory is not full, drain energy etc. from the miner
        // the storages have already been checked earlier
        if (!miner.isInventoryFull()) {
            // always drain storages when working, even if blocksToMine ends up being empty
            drainStorages(false);

            // since energy is being consumed the miner is now active
            if (!this.isActive())
                setStatus(Status.WORKING);
        } else {
            // the miner cannot drain, therefore it is inactive
            if (this.isActive()) {
                setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ").append(ItemRecipeCapability.CAP.getTraslateComponent()));
            }
        }

        // drill a hole beneath the miner and extend the pipe downwards by one
        ServerLevel world = (ServerLevel) getMachine().getLevel();
        if (mineY < pipeY) {
            BlockPos miningPos = getMiningPos();
            world.destroyBlock(new BlockPos(miningPos.getX(), pipeY, miningPos.getZ()), false);
            --pipeY;
            incrementPipeLength();
        }

        // check if the miner needs new blocks to mine and get them if needed
        checkBlocksToMine();

        // if there are blocks to mine and the correct amount of time has passed, do the mining
        if (getMachine().getOffsetTimer() % this.speed == 0 && !blocksToMine.isEmpty()) {
            NonNullList<ItemStack> blockDrops = NonNullList.create();
            BlockState blockState = world.getBlockState(blocksToMine.getFirst());

            // check to make sure the ore is still there,
            while (!blockState.is(CustomTags.ORE_BLOCKS)) {
                blocksToMine.removeFirst();
                if (blocksToMine.isEmpty()) break;
                blockState = world.getBlockState(blocksToMine.getFirst());
            }
            // When we are here we have an ore to mine! I'm glad we aren't threaded
            if (!blocksToMine.isEmpty() & blockState.is(CustomTags.ORE_BLOCKS)) {
                // get the small ore drops, if a small ore
                getSmallOreBlockDrops(blockDrops, world, blocksToMine.getFirst(), blockState);
                // get the block's drops.
                getRegularBlockDrops(blockDrops, world, blocksToMine.getFirst(), blockState);
                // try to insert them
                mineAndInsertItems(blockDrops, world);
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

        if (isSuspend()) {
            // machine isn't working enabled
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
        if (checkShouldStop()) {
            // if the miner is not finished and has invalid coordinates, get new and valid starting coordinates
            if (!isDone && checkCoordinatesInvalid(x, y, z))
                initPos(getMiningPos(), currentRadius);

            // don't do anything else this time
            return false;
        }
        return true;
    }

    protected boolean checkShouldStop() {
        return isDone || checkCoordinatesInvalid(x, y, z) || !drainStorages(true);
    }

    /**
     * Called after each block is mined, used to perform additional actions afterwards
     */
    protected void onMineOperation() {

    }

    /**
     * called in order to drain anything the miner needs to drain in order to run
     * only drains energy by default
     */
    protected boolean drainStorages(boolean simulate) {
        return miner.drainEnergy(simulate);
    }

    /**
     * called to handle mining small ores
     *
     * @param blockDrops  the List of items to fill after the operation
     * @param world       the {@link ServerLevel} the miner is in
     * @param blockToMine the {@link BlockPos} of the block being mined
     * @param blockState  the {@link BlockState} of the block being mined
     */
    // todo implement small ores
    protected void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, BlockState blockState) {
        /*small ores
            if orePrefix of block in blockPos is small
                applyTieredHammerNoRandomDrops...
            else
                current code...
        */
    }

    /**
     * called to handle mining regular ores and blocks
     *
     * @param blockDrops  the List of items to fill after the operation
     * @param world       the {@link ServerLevel} the miner is in
     * @param blockToMine the {@link BlockPos} of the block being mined
     * @param blockState  the {@link BlockState} of the block being mined
     */
    protected void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, @Nonnull BlockState blockState) {
        blockDrops.addAll(blockState.getBlock().getDrops(blockState, new LootContext.Builder(world)
                .withRandom(world.random)
                .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(blockToMine))
                .withParameter(LootContextParams.TOOL, pickaxeToolFortune)));
    }

    /**
     * called to handle mining regular ores and blocks with silk touch
     *
     * @param blockDrops  the List of items to fill after the operation
     * @param world       the {@link ServerLevel} the miner is in
     * @param blockToMine the {@link BlockPos} of the block being mined
     * @param blockState  the {@link BlockState} of the block being mined
     */
    protected void getSilkTouchDrops(NonNullList<ItemStack> blockDrops, ServerLevel world, BlockPos blockToMine, @Nonnull BlockState blockState) {
        blockDrops.add(new ItemStack(blockState.getBlock()));
    }

    private ItemTransferList getCachedItemTransfer() {
        if (cachedItemTransfer == null) {
            cachedItemTransfer = new ItemTransferList(machine.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).stream().map(IItemTransfer.class::cast).toList());
        }

        return cachedItemTransfer;
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
        ItemTransferList transfer = getCachedItemTransfer();
        if (transfer != null) {
            if (GTTransferUtils.addItemsToItemHandler(transfer, true, blockDrops)) {
                GTTransferUtils.addItemsToItemHandler(transfer, false, blockDrops);
                world.setBlock(blocksToMine.getFirst(), oreReplacementBlock, 3);
                mineX = blocksToMine.getFirst().getX();
                mineZ = blocksToMine.getFirst().getZ();
                mineY = blocksToMine.getFirst().getY();
                blocksToMine.removeFirst();
                onMineOperation();

                // if the inventory was previously considered full, mark it as not since an item was able to fit
                if (miner.isInventoryFull())
                    miner.setInventoryFull(false);
            } else {
                // the ore block was not able to fit, so the inventory is considered full
                miner.setInventoryFull(true);
            }
        }

    }

    /**
     * This method designates the starting position for mining blocks
     *
     * @param pos           the {@link BlockPos} of the miner itself
     * @param currentRadius the currently set mining radius
     */
    public void initPos(@Nonnull BlockPos pos, int currentRadius) {
        x = pos.getX() - currentRadius;
        z = pos.getZ() - currentRadius;
        y = pos.getY() - 1;
        startX = pos.getX() - currentRadius;
        startZ = pos.getZ() - currentRadius;
        startY = pos.getY();
        pipeY = pos.getY() - 1;
        mineX = pos.getX() - currentRadius;
        mineZ = pos.getZ() - currentRadius;
        mineY = pos.getY() - 1;
    }

    /**
     * Checks if the current coordinates are invalid
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return {@code true} if the coordinates are invalid, else false
     */
    private static boolean checkCoordinatesInvalid(int x, int y, int z) {
        return x == Integer.MAX_VALUE && y == Integer.MAX_VALUE && z == Integer.MAX_VALUE;
    }

    /**
     * Checks whether there are any more blocks to mine, if there are currently none queued
     */
    public void checkBlocksToMine() {
        if (blocksToMine.isEmpty())
            blocksToMine.addAll(getBlocksToMine());
    }

    /**
     * Recalculates the mining area, refills the block list and restarts the miner, if it was done
     */
    public void resetArea() {
        initPos(getMiningPos(), currentRadius);
        if (this.isDone) this.setWorkingEnabled(false);
        this.isDone = false;
        blocksToMine.clear();
        checkBlocksToMine();
    }

    /**
     * Gets the blocks to mine
     *
     * @return a {@link LinkedList} of {@link BlockPos} for each ore to mine
     */
    private LinkedList<BlockPos> getBlocksToMine() {
        LinkedList<BlockPos> blocks = new LinkedList<>();

        // determine how many blocks to retrieve this time
        double quotient = getQuotient(getMeanTickTime(getMachine().getLevel()));
        int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        int calculated = 0;

        if (this.minBuildHeight == Integer.MAX_VALUE) this.minBuildHeight = this.getMachine().getLevel().getMinBuildHeight();

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y > minBuildHeight) {
                // moving across the z-axis
                if (z <= startZ + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x <= startX + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockState state = getMachine().getLevel().getBlockState(blockPos);
                        if (state.getBlock().defaultDestroyTime() >= 0 && getMachine().getLevel().getBlockEntity(blockPos) == null && state.is(CustomTags.ORE_BLOCKS)) {
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
                    --y;
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
    private static long mean(@Nonnull long[] values) {
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
    private static double getMeanTickTime(@Nonnull Level world) {
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

    protected static final WeakHashMap<BlockState, ItemStack> DROP_CACHE = new WeakHashMap<>();

    /**
     * Applies a fortune hammer to block drops based on a tier value, intended for small ores
     *
     * @param logic               the miner logic
     * @param world               the level
     * @param blockToMine         the position of the block being mined
     * @param blockState          the block being mined
     * @param drops               where the drops are stored to
     * @param dropCountMultiplier multiply all crushed drops by this, if > 0
     * @param recipeType          the recipemap from which to get the drops
     * @param tier                the tier at which the operation is performed, used for calculating the chanced output boost
     */
    protected static void applyTieredHammerNoRandomDrops(MinerLogic logic, ServerLevel world, BlockPos blockToMine, @Nonnull BlockState blockState, NonNullList<ItemStack> drops, int dropCountMultiplier, @Nonnull GTRecipeType recipeType, int tier) {
        ItemStack fortunePick = PICKAXE_TOOL.copy();
        fortunePick.enchant(Enchantments.BLOCK_FORTUNE, dropCountMultiplier);

        ItemStack oreDrop;
        LootContext.Builder builder = new LootContext.Builder(world)
                .withRandom(world.random)
                .withParameter(LootContextParams.BLOCK_STATE, blockState)
                .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(blockToMine))
                .withParameter(LootContextParams.TOOL, PICKAXE_TOOL);
        if (!DROP_CACHE.containsKey(blockState)) {
            oreDrop = blockState.getDrops(builder).get(0);
            oreDrop.setCount(1);
            DROP_CACHE.put(blockState, oreDrop);
            oreDrop = oreDrop.copy();
        } else {
            oreDrop = DROP_CACHE.get(blockState).copy();
        }

        NotifiableItemStackHandler inputItemHandler = (NotifiableItemStackHandler)logic.breakRecipeSearchHolder.getCapabilitiesProxy().get(IO.IN, ItemRecipeCapability.CAP).get(0);
        NotifiableItemStackHandler outputItemHandler = (NotifiableItemStackHandler)logic.breakRecipeSearchHolder.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0);
        inputItemHandler.setStackInSlot(0, oreDrop);

        GTRecipeType lastRecipeType = logic.miner.getRecipeType();
        logic.miner.setRecipeType(recipeType);
        logic.findAndHandleRecipe();
        logic.miner.setRecipeType(lastRecipeType);

        if (logic.lastRecipe != null) {
            GTRecipe recipe = logic.lastRecipe;
            if (GTUtil.getTierByVoltage(EURecipeCapability.CAP.of(recipe.getTickInputContents(EURecipeCapability.CAP).get(0).getContent())) > tier) return;

            if (recipe.handleRecipeIO(IO.OUT, logic.breakRecipeSearchHolder)) {
                drops.clear();

                List<ItemStack> outputs = new ArrayList<>();
                for (int i = 0; i < outputItemHandler.getSlots(); ++i) {
                    ItemStack stack = outputItemHandler.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    outputs.add(stack);
                }
                for (ItemStack outputStack : outputs) {
                    if (outputStack.isEmpty()) continue;
                    outputStack = outputStack.copy();
                    if (ChemicalHelper.getPrefix(outputStack.getItem()) == TagPrefix.crushed) {
                        if (dropCountMultiplier > 0) {
                            outputStack = ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE).build().apply(outputStack, builder.withParameter(LootContextParams.TOOL, fortunePick).create(LootContextParamSets.BLOCK));
                        }
                    }
                    drops.add(outputStack);
                }
            }
            for (int i = 0; i < outputItemHandler.getSlots(); ++i) {
                outputItemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        } else {
            if (logic instanceof LargeMinerLogic largeMinerLogic) {
                largeMinerLogic.getNormalRegularBlockDrops(drops, (ServerLevel) logic.getMachine().getLevel(), blockToMine, blockState);
                largeMinerLogic.multiplyDrops(drops, dropCountMultiplier);
            } else {
                logic.getRegularBlockDrops(drops, (ServerLevel) logic.getMachine().getLevel(), blockToMine, blockState);
            }
        }
        inputItemHandler.setStackInSlot(0, ItemStack.EMPTY);
    }

    protected List<GTRecipe> searchRecipe() {
        return machine.getRecipeType().searchRecipe(getRecipeManager(), this.breakRecipeSearchHolder);
    }

    public GTRecipe.ActionResult handleTickRecipe(GTRecipe recipe) {
        if (recipe.hasTick()) {
            var result = recipe.matchTickRecipe(this.breakRecipeSearchHolder);
            if (result.isSuccess()) {
                recipe.handleTickRecipeIO(IO.IN, this.breakRecipeSearchHolder);
                recipe.handleTickRecipeIO(IO.OUT, this.breakRecipeSearchHolder);
            } else {
                return result;
            }
        }
        return GTRecipe.ActionResult.SUCCESS;
    }

    public void setupRecipe(GTRecipe recipe) {
        if (handleFuelRecipe()) {
            recipe.preWorking(this.breakRecipeSearchHolder);
            if (recipe.handleRecipeIO(IO.IN, this.breakRecipeSearchHolder)) {
                recipeDirty = false;
                lastRecipe = recipe;
            }
        }
    }

    /**
     * Increments the pipe rendering length by one, signaling that the miner's y level has moved down by one
     */
    private void incrementPipeLength() {
        this.pipeLength++;
        this.getMachine().markDirty();
    }

    /**
     * renders the pipe beneath the miner
     */
    @Environment(EnvType.CLIENT)
    public void renderPipe(PoseStack stack, MultiBufferSource buffer, @Nullable Direction modelFacing, int combinedLight, int combinedOverlay) {
        stack.pushPose();
        for (int i = 0; i < getPipeLength(); ++i) {
            stack.translate(0, -1, 0);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(stack.last(), buffer.getBuffer(RenderType.cutoutMipped()), null, pipeModel.getRotatedModel(modelFacing), 1, 1, 1, combinedLight, combinedOverlay);
        }
        stack.popPose();
    }

    /**
     * @return the current x value
     */
    public int getX() {
        return x;
    }

    /**
     * @return the current y value
     */
    public int getY() {
        return y;
    }

    /**
     * @return the current z value
     */
    public int getZ() {
        return z;
    }

    /**
     * @return the previously mined x value
     */
    public int getMineX() {
        return mineX;
    }

    /**
     * @return the previously mined y value
     */
    public int getMineY() {
        return mineY;
    }

    /**
     * @return the previously mined z value
     */
    public int getMineZ() {
        return mineZ;
    }

    /**
     * @return the starting x value
     */
    public int getStartX() {
        return startX;
    }

    /**
     * @return the starting y value
     */
    public int getStartY() {
        return startY;
    }

    /**
     * @return the starting z value
     */
    public int getStartZ() {
        return startZ;
    }

    /**
     * @return the pipe y value
     */
    public int getPipeY() {
        return pipeY;
    }

    /**
     * @return the miner's maximum radius
     */
    public int getMaximumRadius() {
        return this.maximumRadius;
    }

    /**
     * @return the miner's current radius
     */
    public int getCurrentRadius() {
        return this.currentRadius;
    }

    /**
     * @param currentRadius the radius to set the miner to use
     */
    public void setCurrentRadius(int currentRadius) {
        this.currentRadius = currentRadius;
    }

    /**
     * @return true if the miner is finished working
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     * @return the miner's fortune level
     */
    public int getFortune() {
        return this.fortune;
    }

    /**
     * @return the miner's speed in ticks
     */
    public int getSpeed() {
        return this.speed;
    }

    /**
     * @return the position to start mining from
     */
    public BlockPos getMiningPos() {
        return getMachine().getPos();
    }
}
