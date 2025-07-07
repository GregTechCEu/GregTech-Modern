package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.item.tool.behavior.LighterBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.gregtechceu.gtceu.api.pattern.util.RelativeDirection.*;

public class CharcoalPileIgniterMachine extends WorkableMultiblockMachine implements IWorkable {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            CharcoalPileIgniterMachine.class,
            WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    private static final int MIN_RADIUS = 1;
    private static final int MIN_DEPTH = 2;

    private final Collection<BlockPos> logPos = new ObjectOpenHashSet<>();

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

    private boolean hasAir = false;

    public CharcoalPileIgniterMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        hasAir = false;
        if (getMultiblockState().getMatchContext().containsKey("logPos")) {
            Long2BooleanMap logPositions = getMultiblockState().getMatchContext().get("logPos");
            for (var entry : logPositions.long2BooleanEntrySet()) {
                if (entry.getBooleanValue()) {
                    logPos.add(BlockPos.of(entry.getLongKey()));
                } else {
                    hasAir = true;
                }
            }
        }
        this.getRecipeLogic().setDuration(Math.max(1, (int) Math.sqrt(logPos.size() * 240_000)));
    }

    @Override
    protected @NotNull CharcoalRecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new CharcoalRecipeLogic(this);
    }

    @Override
    public @NotNull CharcoalRecipeLogic getRecipeLogic() {
        return (CharcoalRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean isActive() {
        return recipeLogic.isWorking();
    }

    @Override
    public boolean isWorkingEnabled() {
        return true;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {}

    @Override
    public BlockPattern getPattern() {
        updateDimensions();

        if (lDist < MIN_RADIUS) lDist = MIN_RADIUS;
        if (rDist < MIN_RADIUS) rDist = MIN_RADIUS;
        if (fDist < MIN_RADIUS) fDist = MIN_RADIUS;
        if (bDist < MIN_RADIUS) bDist = MIN_RADIUS;
        if (hDist < MIN_DEPTH) hDist = MIN_DEPTH;

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
                    floorLayer[j].append('A'); // floor edge
                    for (int k = 0; k < hDist - 1; k++) {
                        if ((i == 0 || i == lDist + rDist) && (j == 0 || j == fDist + bDist)) {
                            wallLayers.get(k)[j].append('A');
                        } else {
                            wallLayers.get(k)[j].append('W'); // walls
                        }
                    }
                    ceilingLayer[j].append('A'); // ceiling edge
                } else { // not edges
                    floorLayer[j].append('B');
                    for (int k = 0; k < hDist - 1; k++) {
                        wallLayers.get(k)[j].append('L'); // log or air
                    }
                    if (i == lDist && j == fDist) { // very center
                        ceilingLayer[j].append('S'); // controller
                    } else {
                        ceilingLayer[j].append('W'); // grass top
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
                .where('W', Predicates.blockTag(CustomTags.CHARCOAL_PILE_IGNITER_WALLS))
                .where('L', logPredicate())
                .where('A', Predicates.any())
                .build();
    }

    protected static TraceabilityPredicate logPredicate() {
        return new TraceabilityPredicate(multiblockState -> {
            BlockState state = multiblockState.getBlockState();
            long pos = multiblockState.getPos().asLong();
            boolean log = state.is(BlockTags.LOGS_THAT_BURN);
            if (log || state.isAir()) {
                multiblockState.getMatchContext().getOrCreate("logPos", Long2BooleanOpenHashMap::new).put(pos, log);
                return true;
            }
            return false;
            // copied from PredicateBlockTag to display the preview logs properly
        }, () -> BuiltInRegistries.BLOCK.getTag(BlockTags.LOGS_THAT_BURN)
                .stream()
                .flatMap(HolderSet.Named::stream)
                .map(Holder::value)
                .map(BlockInfo::fromBlock)
                .toArray(BlockInfo[]::new));
    }

    public void updateDimensions() {
        Level level = getLevel();
        if (level == null) return;
        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = RelativeDirection.LEFT.getRelativeFacing(front, getUpwardsFacing(), false);
        Direction right = RelativeDirection.RIGHT.getRelativeFacing(front, getUpwardsFacing(), false);

        BlockPos down = getPos().relative(Direction.DOWN);

        BlockPos.MutableBlockPos lPos = down.mutable();
        BlockPos.MutableBlockPos rPos = down.mutable();
        BlockPos.MutableBlockPos fPos = down.mutable();
        BlockPos.MutableBlockPos bPos = down.mutable();
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
        return level.getBlockState(pos.move(direction)).is(CustomTags.CHARCOAL_PILE_IGNITER_WALLS);
    }

    private static boolean isBlockFloor(Level level, BlockPos.MutableBlockPos pos) {
        return level.getBlockState(pos.move(Direction.DOWN)).is(Blocks.BRICKS);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        if (isActive()) {
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

    private void convertLogBlocks() {
        Level level = getLevel();
        for (BlockPos pos : logPos) {
            level.setBlockAndUpdate(pos, GTBlocks.BRITTLE_CHARCOAL.getDefaultState());
        }
        logPos.clear();
    }

    @Override
    public InteractionResult onUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isFormed() || hasAir) {
            return super.onUse(state, level, pos, player, hand, hit);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(CustomTags.TOOLS_IGNITER)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide && !isActive()) {
            return InteractionResult.SUCCESS;
        } else if (!isActive()) {
            boolean shouldActivate = false;
            if (stack.getItem() instanceof ComponentItem compItem) {
                for (var component : compItem.getComponents()) {
                    if (component instanceof LighterBehavior lighter && lighter.consumeFuel(player, stack)) {
                        shouldActivate = true;
                        break;
                    }
                }
            } else if (stack.isDamageableItem()) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                shouldActivate = true;
            } else {
                stack.shrink(1);
                shouldActivate = true;
            }

            if (shouldActivate) {
                getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);

                level.playSound(null, pos,
                        stack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.CONSUME;
            }
        }
        return super.onUse(state, level, pos, player, hand, hit);
    }

    public static class CharcoalRecipeLogic extends RecipeLogic {

        private final CharcoalPileIgniterMachine machine;

        public CharcoalRecipeLogic(CharcoalPileIgniterMachine machine) {
            super(machine);
            this.machine = machine;
        }

        @Override
        public void serverTick() {
            super.serverTick();
            if (isWorking() && duration > 0) {
                if (++progress >= duration) {
                    progress = 0;
                    duration = 0;
                    this.machine.convertLogBlocks();
                    setStatus(Status.IDLE);
                }
            }
        }

        public void setDuration(int max) {
            this.duration = max;
        }
    }
}
