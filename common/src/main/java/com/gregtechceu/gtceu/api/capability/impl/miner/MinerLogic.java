package com.gregtechceu.gtceu.api.capability.impl.miner;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IMiner;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.impl.DummyRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MinerLogic extends RecipeLogic implements IWorkable {
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

    private final int fortune;
    private final int speed;
    private final int maximumRadius;

    private final LinkedList<BlockPos> blocksToMine = new LinkedList<>();

    @Persisted
    private final AtomicInteger x = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger y = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger z = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger startX = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger startZ = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger startY = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger pipeY = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger mineX = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger mineZ = new AtomicInteger(Integer.MAX_VALUE);
    @Persisted
    private final AtomicInteger mineY = new AtomicInteger(Integer.MAX_VALUE);

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

        this.breakRecipeSearchHolder = new DummyRecipeCapabilityHolder();
        this.breakRecipeSearchHolder.addCapability(IO.IN, ItemRecipeCapability.CAP, List.of(new NotifiableItemStackHandler(machine.self(), 1, IO.IN, IO.BOTH)));
        this.breakRecipeSearchHolder.addCapability(IO.OUT, ItemRecipeCapability.CAP, List.of(new NotifiableItemStackHandler(machine.self(), 10, IO.OUT, IO.BOTH)));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
        if (machine.self().isRemote())
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
        ServerLevel world = (ServerLevel) machine.self().getLevel();
        if (mineY.get() < pipeY.get()) {
            world.destroyBlock(new BlockPos(machine.self().getPos().getX(), pipeY.get(), machine.self().getPos().getZ()), false);
            pipeY.decrementAndGet();
            incrementPipeLength();
        }

        // check if the miner needs new blocks to mine and get them if needed
        checkBlocksToMine();

        // if there are blocks to mine and the correct amount of time has passed, do the mining
        if (machine.self().getOffsetTimer() % this.speed == 0 && !blocksToMine.isEmpty()) {
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
            x.set(mineX.get());
            y.set(mineY.get());
            z.set(mineZ.get());

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
                initPos(machine.self().getPos(), currentRadius);

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
                .withParameter(LootContextParams.TOOL, PICKAXE_TOOL))); // regular ores do not get fortune applied
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
        ItemTransferList transfer = machine.self().getItemTransferCap(null);
        if (transfer != null) {
            if (GTTransferUtils.addItemsToItemHandler(transfer, true, blockDrops)) {
                GTTransferUtils.addItemsToItemHandler(transfer, false, blockDrops);
                world.setBlock(blocksToMine.getFirst(), oreReplacementBlock, 3);
                mineX.set(blocksToMine.getFirst().getX());
                mineZ.set(blocksToMine.getFirst().getZ());
                mineY.set(blocksToMine.getFirst().getY());
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
        x.set(pos.getX() - currentRadius);
        z.set(pos.getZ() - currentRadius);
        y.set(pos.getY() - 1);
        startX.set(pos.getX() - currentRadius);
        startZ.set(pos.getZ() - currentRadius);
        startY.set(pos.getY());
        pipeY.set(pos.getY() - 1);
        mineX.set(pos.getX() - currentRadius);
        mineZ.set(pos.getZ() - currentRadius);
        mineY.set(pos.getY() - 1);
    }

    /**
     * Checks if the current coordinates are invalid
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return {@code true} if the coordinates are invalid, else false
     */
    private static boolean checkCoordinatesInvalid(@Nonnull AtomicInteger x, @Nonnull AtomicInteger y, @Nonnull AtomicInteger z) {
        return x.get() == Integer.MAX_VALUE && y.get() == Integer.MAX_VALUE && z.get() == Integer.MAX_VALUE;
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
        initPos(machine.self().getPos(), currentRadius);
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
        double quotient = getQuotient(getMeanTickTime(machine.self().getLevel()));
        int calcAmount = quotient < 1 ? 1 : (int) (Math.min(quotient, Short.MAX_VALUE));
        int calculated = 0;

        if (this.minBuildHeight == Integer.MAX_VALUE) this.minBuildHeight = this.machine.self().getLevel().getMinBuildHeight();

        // keep getting blocks until the target amount is reached
        while (calculated < calcAmount) {
            // moving down the y-axis
            if (y.get() > minBuildHeight) {
                // moving across the z-axis
                if (z.get() <= startZ.get() + currentRadius * 2) {
                    // check every block along the x-axis
                    if (x.get() <= startX.get() + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x.get(), y.get(), z.get());
                        BlockState state = machine.self().getLevel().getBlockState(blockPos);
                        if (state.getBlock().defaultDestroyTime() >= 0 && machine.self().getLevel().getBlockEntity(blockPos) == null && state.is(CustomTags.ORE_BLOCKS)) {
                            blocks.addLast(blockPos);
                        }
                        // move to the next x position
                        x.incrementAndGet();
                    } else {
                        // reset x and move to the next z layer
                        x.set(startX.get());
                        z.incrementAndGet();
                    }
                } else {
                    // reset z and move to the next y layer
                    z.set(startZ.get());
                    y.decrementAndGet();
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

    /**
     * Applies a fortune hammer to block drops based on a tier value, intended for small ores
     *
     * @param blockState   the block being mined
     * @param drops        where the drops are stored to
     * @param fortuneLevel the level of fortune used
     * @param map          the recipemap from which to get the drops
     * @param tier         the tier at which the operation is performed, used for calculating the chanced output boost
     */
    protected static void applyTieredHammerNoRandomDrops(MinerLogic logic, @Nonnull BlockState blockState, List<ItemStack> drops, int fortuneLevel, @Nonnull GTRecipeType map, int tier) {
        NotifiableItemStackHandler outputItemHandler = (NotifiableItemStackHandler)logic.breakRecipeSearchHolder.getCapabilitiesProxy().get(IO.OUT, ItemRecipeCapability.CAP).get(0);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(Ingredient.of(blockState.getBlock()));
        outputItemHandler.setStackInSlot(0, new ItemStack(blockState.getBlock()));
        List<GTRecipe> possibleRecipes = map.searchRecipe(logic.machine.getRecipeLogic().getRecipeManager(), logic.breakRecipeSearchHolder);
        if (possibleRecipes.size() > 0) {
            GTRecipe recipe = possibleRecipes.get(0);
            if (GTUtil.getTierByVoltage(EURecipeCapability.CAP.of(recipe.getTickInputContents(EURecipeCapability.CAP).get(0).getContent())) > tier) return;

            List<Ingredient> left = outputItemHandler.handleRecipe(IO.OUT, recipe, ingredients, null);
            if (left == null) {
                drops.clear();

                List<ItemStack> outputs = new ArrayList<>();
                for (int i = 0; i < outputItemHandler.getSlots(); ++i) {
                    outputs.set(i, outputItemHandler.getStackInSlot(i));
                }
                for (ItemStack outputStack : outputs) {
                    outputStack = outputStack.copy();
                    if (ChemicalHelper.getPrefix(outputStack.getItem()) == TagPrefix.crushed) {
                        if (fortuneLevel > 0) {
                            outputStack.grow(outputStack.getCount() * fortuneLevel);
                        }
                    }
                    drops.add(outputStack);
                }
            }
            for (int i = 0; i < outputItemHandler.getSlots(); ++i) {
                outputItemHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }

    }

    /**
     * Increments the pipe rendering length by one, signaling that the miner's y level has moved down by one
     */
    private void incrementPipeLength() {
        this.pipeLength++;
        this.machine.self().markDirty();
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
    public AtomicInteger getX() {
        return x;
    }

    /**
     * @return the current y value
     */
    public AtomicInteger getY() {
        return y;
    }

    /**
     * @return the current z value
     */
    public AtomicInteger getZ() {
        return z;
    }

    /**
     * @return the previously mined x value
     */
    public AtomicInteger getMineX() {
        return mineX;
    }

    /**
     * @return the previously mined y value
     */
    public AtomicInteger getMineY() {
        return mineY;
    }

    /**
     * @return the previously mined z value
     */
    public AtomicInteger getMineZ() {
        return mineZ;
    }

    /**
     * @return the starting x value
     */
    public AtomicInteger getStartX() {
        return startX;
    }

    /**
     * @return the starting y value
     */
    public AtomicInteger getStartY() {
        return startY;
    }

    /**
     * @return the starting z value
     */
    public AtomicInteger getStartZ() {
        return startZ;
    }

    /**
     * @return the pipe y value
     */
    public AtomicInteger getPipeY() {
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
}
