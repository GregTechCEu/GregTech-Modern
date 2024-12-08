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
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

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

import java.util.*;

import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;

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

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            CharcoalPileIgniterMachine.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private final Collection<BlockPos> logPos = new ObjectOpenHashSet<>();

    private static final int MIN_RADIUS = 1;
    private static final int MIN_DEPTH = 2;

    @DescSynced
    private int lDist = 0;
    @DescSynced
    private int rDist = 0;
    @DescSynced
    private int bDist = 0;
    @DescSynced
    private int fDist = 0;
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
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
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
    public boolean isWorkingEnabled() {
        return true;
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

        StringBuilder[] floorLayer = new StringBuilder[fDist + bDist + 1];
        List<StringBuilder[]> wallLayers = new ArrayList<>();
        StringBuilder[] ceilingLayer = new StringBuilder[fDist + bDist + 1];

        for (int i = 0; i < floorLayer.length; i++) {
            floorLayer[i] = new StringBuilder(lDist + rDist + 1);
            ceilingLayer[i] = new StringBuilder(lDist + rDist + 1);
        }

        for (int i = 0; i < hDist - 1; i++) {
            wallLayers.add(new StringBuilder[fDist + bDist + 1]);
            for (int j = 0; j < fDist + bDist + 1; j++) {
                var s = new StringBuilder(lDist + rDist + 3);
                wallLayers.get(i)[j] = s;
            }
        }

        for (int i = 0; i < lDist + rDist + 1; i++) {
            for (int j = 0; j < fDist + bDist + 1; j++) {
                if (i == 0 || i == lDist + rDist || j == 0 || j == fDist + bDist) { // all edges
                    floorLayer[i].append('A'); // floor edge
                    for (int k = 0; k < hDist - 1; k++) {
                        if ((i == 0 || i == lDist + rDist) && (j == 0 || j == fDist + bDist)) {
                            wallLayers.get(k)[i].append('A');
                        } else {
                            wallLayers.get(k)[i].append('W'); // walls
                        }
                    }
                    ceilingLayer[i].append('A'); // ceiling edge
                } else { // not edges
                    if (i == lDist && j == fDist) { // very center
                        floorLayer[i].append('B');
                    } else {
                        floorLayer[i].append('B'); // floor valid blocks
                    }
                    for (int k = 0; k < hDist - 1; k++) {
                        wallLayers.get(k)[i].append('L'); // log or air
                    }
                    if (i == lDist && j == fDist) { // very center
                        ceilingLayer[i].append('S'); // controller
                    } else {
                        ceilingLayer[i].append('W'); // grass top
                    }
                }
            }
        }

        String[] f = new String[bDist + fDist + 1];
        for (int i = 0; i < floorLayer.length; i++) {
            f[i] = floorLayer[i].toString();
        }
        String[] m = new String[bDist + fDist + 1];
        for (int i = 0; i < wallLayers.get(0).length; i++) {
            m[i] = wallLayers.get(0)[i].toString();
        }
        String[] c = new String[bDist + fDist + 1];
        for (int i = 0; i < ceilingLayer.length; i++) {
            c[i] = ceilingLayer[i].toString();
        }

        return FactoryBlockPattern.start(LEFT, FRONT, UP)
                .aisle(f)
                .aisle(m).setRepeatable(wallLayers.size())
                .aisle(c)
                .where('S', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .where('B', Predicates.blocks(Blocks.BRICKS))
                .where('W', wallPredicate())
                .where('L', logPredicate())
                .where('A', Predicates.any())
                .build();
    }

    private TraceabilityPredicate wallPredicate() {
        return new TraceabilityPredicate(multiblockState -> {
            boolean match = false;
            for (var b : WALL_BLOCKS) {
                if (multiblockState.getBlockState().getBlock() == b) {
                    match = true;
                    break;
                }
            }
            return match;
        }, null);
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

    public void updateDimensions() {
        Level level = getLevel();
        if (level == null) return;
        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = getPos().mutable().move(Direction.DOWN);
        BlockPos.MutableBlockPos rPos = getPos().mutable().move(Direction.DOWN);
        BlockPos.MutableBlockPos fPos = getPos().mutable().move(Direction.DOWN);
        BlockPos.MutableBlockPos bPos = getPos().mutable().move(Direction.DOWN);
        BlockPos.MutableBlockPos hPos = getPos().mutable();

        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int fDist = 0;
        int hDist = 0;

        for (int i = 1; i < 6; i++) {
            if (lDist != 0 && rDist != 0 && hDist != 0) break;
            if (lDist == 0 && isBlockWall(level, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockWall(level, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockWall(level, bPos, back)) bDist = i;
            if (fDist == 0 && isBlockWall(level, fPos, front)) fDist = i;
            if (hDist == 0 && isBlockFloor(level, hPos)) hDist = i;
        }

        if (Math.abs(lDist - rDist) > 1 || Math.abs(bDist - fDist) > 1) {
            this.isFormed = false;
            return;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || fDist < MIN_RADIUS || bDist < MIN_RADIUS || hDist < MIN_DEPTH) {
            this.isFormed = false;
            return;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.fDist = fDist;
        this.bDist = bDist;
        this.hDist = hDist;
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
        if (isActive) {
            var pos = this.getPos();
            var facing = Direction.UP;
            float xPos = facing.getStepX() * 0.76F + pos.getX() + 0.25F + GTValues.RNG.nextFloat() / 2.0F;
            float yPos = facing.getStepY() * 0.76F + pos.getY() + 0.25F;
            float zPos = facing.getStepZ() * 0.76F + pos.getZ() + 0.25F + GTValues.RNG.nextFloat() / 2.0F;

            float ySpd = facing.getStepY() * 0.1F + 0.01F * GTValues.RNG.nextFloat();
            float horSpd = 0.03F * GTValues.RNG.nextFloat();
            float horSpd2 = 0.03F * GTValues.RNG.nextFloat();

            if (GTValues.RNG.nextFloat() < 0.1F) {
                getLevel().playLocalSound(xPos, yPos, zPos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 1.0F,
                        1.0F, false);
            }
            for (float xi = xPos - 1; xi <= xPos + 1; xi++) {
                for (float zi = zPos - 1; zi <= zPos + 1; zi++) {
                    if (GTValues.RNG.nextFloat() < .9F)
                        continue;
                    getLevel().addParticle(ParticleTypes.LARGE_SMOKE, xi, yPos, zi, horSpd, ySpd, horSpd2);
                }
            }
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
