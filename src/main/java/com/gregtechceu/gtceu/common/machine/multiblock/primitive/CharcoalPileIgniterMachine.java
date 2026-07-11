package com.gregtechceu.gtceu.common.machine.multiblock.primitive;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.multiblock.PatternPredicate;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandableMultiblockPatternBuilder;
import com.gregtechceu.gtceu.api.multiblock.pattern.ExpandablePattern;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.item.behavior.LighterBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ItemAbilities;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class CharcoalPileIgniterMachine extends WorkableMultiblockMachine implements IWorkable {

    private static final int MIN_RADIUS = 1;
    private static final int MIN_DEPTH = 2;
    private static final int MAX_DEPTH = 5;
    private static final int MAX_RADIUS = 5;
    private final Collection<BlockPos> logPos = new ObjectOpenHashSet<>();

    private List<Integer> bounds = new ArrayList<>(
            List.of(0, MIN_DEPTH, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS));
    private int maxTime = 0;
    private boolean hasAir = false;

    public CharcoalPileIgniterMachine(BlockEntityCreationInfo info) {
        super(info, new CharcoalRecipeLogic());
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        hasAir = false;
        forEachFormed(DEFAULT_STRUCTURE, (info, pos) -> {
            if (info.getBlockState().is(BlockTags.LOGS)) {
                logPos.add(pos.immutable());
            } else if (info.getBlockState().isAir()) {
                hasAir = true;
            }
        });
        this.getRecipeLogic().setDuration(Math.max(1, (int) Math.sqrt(logPos.size() * 240_000)));
    }

    public static ExpandablePattern.BoundsProvider boundsFunction() {
        return (level, controllerPos, front, up) -> {

            BlockPos down = controllerPos.mutable().move(Direction.DOWN);
            Direction back = front.getOpposite();
            Direction left = front.getCounterClockWise();
            Direction right = left.getOpposite();

            int l = findWallPos(level, left, down.mutable());
            int r = findWallPos(level, right, down.mutable());
            int b = findWallPos(level, back, down.mutable());
            int f = findWallPos(level, front, down.mutable());
            int d = findFloorPos(level, up.getOpposite(), controllerPos.mutable());

            if (d < MIN_DEPTH || l < MIN_RADIUS || r < MIN_RADIUS || b < MIN_RADIUS || f < MIN_RADIUS) {
                return new IntArrayList(new int[] { 0, MIN_DEPTH, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS, MIN_RADIUS });
            }
            return new IntArrayList(new int[] { 0, d, l, r, f, b });
        };
    }

    @Override
    public CharcoalRecipeLogic getRecipeLogic() {
        return (CharcoalRecipeLogic) super.getRecipeLogic();
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

    public static Function<MultiblockMachineDefinition, IBlockPattern> getPattern() {
        return (definition) -> {

            var floor = Predicates.blocks(Blocks.BRICKS);
            var logs = PatternPredicate.AIR.or(logPredicate());
            var walls = wallPredicate();

            return ExpandableMultiblockPatternBuilder
                    .start(RelativeDirection.UP, RelativeDirection.RIGHT, RelativeDirection.FRONT)
                    .boundsProvider(boundsFunction())
                    .constraintProvider(() -> List.of(IntIntPair.of(0, 0), IntIntPair.of(MIN_DEPTH, MAX_DEPTH),
                            IntIntPair.of(MIN_RADIUS, MAX_RADIUS), IntIntPair.of(MIN_RADIUS, MAX_RADIUS),
                            IntIntPair.of(MIN_RADIUS, MAX_RADIUS), IntIntPair.of(MIN_RADIUS, MAX_RADIUS)))
                    .predicateProvider((bp, b) -> {
                        if (bp.equals(BlockPos.ZERO))
                            return Predicates.controller(definition);

                        int intersects = 0;
                        boolean topAisle = bp.getX() == b.get(0);
                        boolean bottomAisle = bp.getX() == -b.get(1);

                        if (topAisle || bottomAisle) intersects++;

                        if (bp.getY() == -b.get(2) || bp.getY() == b.get(3)) intersects++;
                        if (bp.getZ() == b.get(4) || bp.getZ() == -b.get(5)) intersects++;

                        if (intersects >= 2) return PatternPredicate.ANY;

                        if (intersects == 1) {
                            if (bottomAisle) return floor;
                            return walls;
                        }
                        return logs;
                    })
                    .build();
        };
    }

    private static PatternPredicate wallPredicate() {
        return new PatternPredicate("Wall Blocks",
                multiblockState -> {
                    BlockPos p = multiblockState.getBlockPos();
                    return multiblockState.getBlockState().is(CustomTags.CHARCOAL_PILE_IGNITER_WALLS) ?
                            null : new PatternStringError(Component.translatable("gtceu.predicate_error.charcoal.walls",
                                    p.getX(), p.getY(), p.getZ()));
                }, null);
    }

    private static PatternPredicate logPredicate() {
        return new PatternPredicate(multiblockState -> {
            boolean match = multiblockState.getBlockState().is(BlockTags.LOGS_THAT_BURN);
            return match ? null : new PatternStringError(Component.translatable("gtceu.predicate_error.charcoal.logs"));
        }, null);
    }

    private static int findWallPos(Level level, Direction direction, BlockPos.MutableBlockPos pos) {
        for (int i = 1; i <= MAX_RADIUS; i++) {
            BlockState state = level.getBlockState(pos.move(direction));
            if (state.is(CustomTags.CHARCOAL_PILE_IGNITER_WALLS)) {
                return i;
            }
        }
        return -1;
    }

    private static int findFloorPos(Level level, Direction dir, BlockPos.MutableBlockPos pos) {
        for (int i = 1; i <= MAX_DEPTH; i++) {
            BlockState state = level.getBlockState(pos.move(dir));
            if (state.is(Blocks.BRICKS)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        if (isActive()) {
            var pos = this.getBlockPos();
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

    public void convertLogBlocks() {
        Level level = getLevel();
        for (BlockPos pos : logPos) {
            level.setBlockAndUpdate(pos, GTBlocks.BRITTLE_CHARCOAL.getDefaultState());
        }
        logPos.clear();
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        var stack = context.getItemInHand();
        var player = context.getPlayer();
        var hand = context.getHand();

        if (!isFormed() || hasAir) {
            return super.onUseWithItem(context);
        }
        if (!stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT)) {
            return InteractionResult.PASS;
        }

        if (getLevel().isClientSide && !isActive()) {
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
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                shouldActivate = true;
            } else {
                stack.shrink(1);
                shouldActivate = true;
            }

            if (shouldActivate) {
                getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);

                getLevel().playSound(null, getBlockPos(),
                        stack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.CONSUME;
            }
        }
        return super.onUse(context);
    }

    public static class CharcoalRecipeLogic extends RecipeLogic {

        public CharcoalRecipeLogic() {
            super();
        }

        @Override
        public CharcoalPileIgniterMachine getMachine() {
            return (CharcoalPileIgniterMachine) super.getMachine();
        }

        @Override
        public void serverTick() {
            super.serverTick();
            if (isWorking() && duration > 0) {
                if (++progress >= duration) {
                    progress = 0;
                    duration = 0;
                    getMachine().convertLogBlocks();
                    setStatus(Status.IDLE);
                }
            }
        }

        public void setDuration(int max) {
            this.duration = max;
        }
    }
}
