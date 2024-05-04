package com.gregtechceu.gtceu.common.entity;

import com.gregtechceu.gtceu.core.mixins.PrimedTntAccessor;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class GTExplosiveEntity extends PrimedTnt {
    private static final GameProfile EXPLOSION_TEST = new GameProfile(UUID.fromString("b671a0e1-909b-455d-9470-cb921b1ea953"), "EXPLOSION");

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type, Level level, double x, double y, double z, @Nullable LivingEntity owner) {
        this(type, level);
        this.setPos(x, y, z);
        double d = level.random.nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(d) * 0.02, 0.2F, -Math.cos(d) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        ((PrimedTntAccessor)this).setOwner(owner);
    }

    public GTExplosiveEntity(EntityType<? extends GTExplosiveEntity> type, Level world) {
        super(type, world);
    }

    /**
     * @return The strength of the explosive.
     */
    protected abstract float getStrength();

    /**
     * @return Whether to drop all blocks, or use default logic
     */
    public abstract boolean dropsAllBlocks();

    /**
     * @return The range of the explosive, if {@link #dropsAllBlocks} is true.
     */
    protected int getRange() {
        return 2;
    }

    /**
     * @return The block state of the block this explosion entity is created by.
     */
    public abstract @NotNull BlockState getExplosiveState();


    @Override
    public void tick() {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.xOld = this.getZ();
        Vec3 motion = this.getDeltaMovement();
        if (!this.isNoGravity()) {
            motion = new Vec3(motion.x, motion.y - 0.03999999910593033D, motion.z);
        }

        this.move(MoverType.SELF, motion);
        motion = new Vec3(motion.x * 0.9800000190734863D, motion.y * 0.9800000190734863D, motion.z * 0.9800000190734863D);
        if (this.onGround()) {
            motion = new Vec3(motion.x * 0.699999988079071D, motion.y * -0.5D, motion.z * 0.699999988079071D);
        }
        this.setDeltaMovement(motion);

        setFuse(this.getFuse() - 1);
        if (this.getFuse() <= 0) {
            this.kill();
            if (!this.level().isClientSide) {
                this.explodeTNT();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D,
                0.0D);
        }
    }

    protected void explodeTNT() {
        Explosion explosion = this.level().explode(this, this.getX(), this.getY() + (double) (this.getBbHeight() / 16.0F), this.getZ(),
            getStrength(), dropsAllBlocks() ? Level.ExplosionInteraction.NONE : Level.ExplosionInteraction.TNT);

        // If we don't drop all blocks, then skip the drop capture logic
        if (!dropsAllBlocks())
            return;

        // Create the fake explosion but don't destroy any blocks in water, per MC behavior
        if (this.isInFluidType())
            return;

        Player player = FakePlayerFactory.get((ServerLevel) level(), EXPLOSION_TEST);

        int range = getRange();
        for (BlockPos pos : BlockPos.betweenClosed(this.getOnPos().offset(-range, -range, -range), this.getOnPos().offset(range, range, range))) {
            BlockState state = level().getBlockState(pos);

            if (level().isEmptyBlock(pos))
                continue;
            if (state.getFluidState().is(FluidTags.WATER) || state.getFluidState().is(FluidTags.LAVA))
                continue;

            float hardness = state.getDestroySpeed(level(), pos);
            float resistance = state.getExplosionResistance(level(), pos, explosion);

            if (hardness >= 0.0f && resistance < 100 && level().isInWorldBounds(pos)) {
                List<ItemStack> drops = attemptBreakBlockAndObtainDrops(pos, state, player);

                for (ItemStack stack : drops) {
                    ItemEntity entity = new ItemEntity(level(), pos.getX(), pos.getY(), pos.getZ(), stack);
                    entity.setDefaultPickUpDelay();
                    level().addFreshEntity(entity);
                }
            }
        }
    }

    private List<ItemStack> attemptBreakBlockAndObtainDrops(BlockPos pos, BlockState state, Player player) {
        if (state.getBlock().onDestroyedByPlayer(state, level(), pos, player, true, level().getFluidState(pos))) {
            level().levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            state.getBlock().destroy(level(), pos, state);

            return Block.getDrops(state, (ServerLevel) level(), pos, level().getBlockEntity(pos), player, ItemStack.EMPTY);
        }
        return Collections.emptyList();
    }
}