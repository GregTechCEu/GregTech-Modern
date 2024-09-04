package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.item.tool.behavior.LighterBehavior;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class CharcoalPileIgniterMachine extends WorkableMultiblockMachine implements IWorkable {

    private static final Set<Block> WALL_BLOCKS = new ObjectOpenHashSet<>();
    static {
        WALL_BLOCKS.add(Blocks.DIRT);
        WALL_BLOCKS.add(Blocks.COARSE_DIRT);
        WALL_BLOCKS.add(Blocks.PODZOL);
        WALL_BLOCKS.add(Blocks.GRASS_BLOCK);
        WALL_BLOCKS.add(Blocks.DIRT_PATH);
        WALL_BLOCKS.add(Blocks.SAND);
        WALL_BLOCKS.add(Blocks.RED_SAND);

    }
    private final Collection<BlockPos> logPos = new ObjectOpenHashSet<>();

    private static final int MIN_RADIUS = 1;
    private static final int MIN_DEPTH = 2;

    @DescSynced
    private int lDist = 0;
    @DescSynced
    private int rDist = 0;
    @DescSynced
    private int hDist = 0;
    @DescSynced
    @RequireRerender
    private boolean isActive;
    private int progressTime = 0;
    private int maxTime = 0;
    private TickableSubscription burnLogsSubscription;

    public CharcoalPileIgniterMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        updateMaxProgessTime();
        burnLogsSubscription = subscribeServerTick(this::tick);
        tick();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        resetState();
        this.progressTime = 0;
        this.maxTime = 0;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        resetState();
    }

    private void resetState() {
        unsubscribe(burnLogsSubscription);
        isActive = false;
    }

    @Override
    public int getProgress() {
        return progressTime;
    }

    @Override
    public int getMaxProgress() {
        return maxTime;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public BlockPattern getPattern() {
        updateDimensions();

        if (lDist < 1) lDist = MIN_RADIUS;
        if (rDist < 1) rDist = MIN_RADIUS;
        if (hDist < 2) hDist = MIN_RADIUS;

        if (this.getFrontFacing().getAxis() == Direction.Axis.X) {
            int tmp = lDist;
            lDist = rDist;
            rDist = tmp;
        }

        StringBuilder wallBuilder = new StringBuilder();    // " XXX "
        StringBuilder floorBuilder = new StringBuilder();   // " BBB "
        StringBuilder cornerBuilder = new StringBuilder();  // " "
        StringBuilder ctrlBuilder = new StringBuilder();    // " XSX "
        StringBuilder woodBuilder = new StringBuilder();    // "XCCCX"

        wallBuilder.append(" ");
        floorBuilder.append(" ");
        ctrlBuilder.append(" ");
        woodBuilder.append("X");

        for (int i = 0; i < lDist; i++) {
            cornerBuilder.append(" ");
            if (i > 0) {
                wallBuilder.append("X");
                floorBuilder.append("B");
                ctrlBuilder.append("X");
                woodBuilder.append("C");
            }
        }

        wallBuilder.append("X");
        floorBuilder.append("B");
        cornerBuilder.append(" ");
        ctrlBuilder.append("S");
        woodBuilder.append("C");

        for (int i = 0; i < rDist; i++) {
            cornerBuilder.append(" ");
            if (i < rDist - 1) {
                wallBuilder.append("X");
                floorBuilder.append("B");
                ctrlBuilder.append("X");
                woodBuilder.append("C");
            }
        }

        wallBuilder.append(" ");
        floorBuilder.append(" ");
        ctrlBuilder.append(" ");
        woodBuilder.append("X");

        String[] wall = new String[hDist + 1];  // " ", " XXX ", " "
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = cornerBuilder.toString();
        wall[wall.length - 1] = cornerBuilder.toString();

        String[] slice = new String[hDist + 1]; // " BBB ", "XCCCX", " XXX "
        Arrays.fill(slice, woodBuilder.toString());
        slice[0] = floorBuilder.toString();

        String[] center = Arrays.copyOf(slice, slice.length); // " BBB ", "XCCCX", " XSX "
        if (this.getFrontFacing().getAxis() == Direction.Axis.X) {
            center[center.length - 1] = ctrlBuilder.reverse().toString();
        } else {
            center[center.length - 1] = ctrlBuilder.toString();
        }

        slice[slice.length - 1] = wallBuilder.toString();

        return FactoryBlockPattern.start()
                .aisle(wall)
                .aisle(slice).setRepeatable(0, 4)
                .aisle(center)
                .aisle(slice).setRepeatable(0, 4)
                .aisle(wall)
                .where('S', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .where('B', Predicates.blocks(Blocks.BRICKS))
                .where('X', Predicates.blocks(WALL_BLOCKS.toArray(new Block[0])))
                .where('C', logPredicate())
                .where(' ', Predicates.any())
                .build();
    }

    private TraceabilityPredicate logPredicate() {
        return new TraceabilityPredicate(multiblockState -> {
            if (multiblockState.getBlockState().is(BlockTags.LOGS_THAT_BURN)) {
                logPos.add(multiblockState.getPos());
                return true;
            }
            return false;
        }, null);
    }

    public boolean updateDimensions() {
        Level level = getLevel();
        Direction left = getFrontFacing().getOpposite().getCounterClockWise();
        Direction right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos().getX(), getPos().getY(), getPos().getZ())
                .move(Direction.DOWN);
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos().getX(), getPos().getY(), getPos().getZ())
                .move(Direction.DOWN);
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(getPos().getX(), getPos().getY(), getPos().getZ());

        int lDist = 0;
        int rDist = 0;
        int hDist = 0;

        for (int i = 1; i < 6; i++) {
            if (lDist != 0 && rDist != 0 && hDist != 0) break;
            if (lDist == 0 && isBlockWall(level, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockWall(level, rPos, right)) rDist = i;
            if (hDist == 0 && isBlockFloor(level, hPos)) hDist = i;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            onStructureInvalid();
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.hDist = hDist;

        return true;
    }

    private static boolean isBlockWall(Level level, BlockPos.MutableBlockPos pos, Direction direction) {
        return WALL_BLOCKS.contains(level.getBlockState(pos.move(direction)).getBlock());
    }

    private static boolean isBlockFloor(Level level, BlockPos.MutableBlockPos pos) {
        return level.getBlockState(pos.move(Direction.DOWN)).getBlock() == Blocks.BRICKS;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private void updateMaxProgessTime() {
        this.maxTime = Math.max(1, (int) Math.sqrt(logPos.size() * 240_000));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        if (recipeLogic.isWorking()) {
            var pos = this.getPos();
            var facing = Direction.UP;
            float xPos = facing.getStepX() * 0.76F + pos.getX() + 0.5F;
            float yPos = facing.getStepY() * 0.76F + pos.getY() + 0.25F;
            float zPos = facing.getStepZ() * 0.76F + pos.getZ() + 0.5F;

            float ySpd = facing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
            getLevel().addParticle(ParticleTypes.LARGE_SMOKE, xPos, yPos, zPos, 0, ySpd, 0);
        }
    }

    public void tick() {
        if (isActive && maxTime > 0) {
            if (++progressTime == maxTime) {
                progressTime = 0;
                maxTime = 0;
                convertLogBlocks();
                isActive = false;
            }
        }
    }

    private void convertLogBlocks() {
        Level level = getLevel();
        for (BlockPos pos : logPos) {
            level.setBlock(pos, GTBlocks.BRITTLE_CHARCOAL.getDefaultState(), Block.UPDATE_ALL);
        }
        logPos.clear();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof IMachineBlockEntity machineBe) {
            MetaMachine mte = machineBe.getMetaMachine();
            if (mte instanceof CharcoalPileIgniterMachine cpi && cpi.isFormed()) {
                if (world.isClientSide) {
                    player.swing(hand);
                } else if (!cpi.isActive()) {
                    boolean shouldActivate = false;
                    ItemStack stack = player.getItemInHand(hand);
                    if (stack.getItem() instanceof FlintAndSteelItem) {
                        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                        getLevel().playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

                        shouldActivate = true;
                    } else if (stack.getItem() instanceof FireChargeItem) {
                        stack.shrink(1);

                        getLevel().playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

                        shouldActivate = true;
                    } else if (stack.getItem() instanceof ComponentItem compItem) {
                        for (var component : compItem.getComponents()) {
                            if (component instanceof LighterBehavior lighter && lighter.consumeFuel(player, stack)) {
                                getLevel().playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f,
                                        1.0f);

                                shouldActivate = true;
                                break;
                            }
                        }
                    }

                    if (shouldActivate) {
                        cpi.setActive(true);
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }
}
