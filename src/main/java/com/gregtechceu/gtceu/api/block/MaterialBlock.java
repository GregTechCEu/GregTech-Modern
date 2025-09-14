package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MaterialBlock extends Block {

    public final TagPrefix tagPrefix;
    public final Material material;

    public MaterialBlock(Properties properties, TagPrefix tagPrefix, Material material, boolean registerModel) {
        super(properties);
        this.material = material;
        this.tagPrefix = tagPrefix;
        if (registerModel && GTCEu.isClientSide()) {
            MaterialBlockRenderer.create(this, tagPrefix.materialIconType(), material.getMaterialIconSet());
        }
    }

    public MaterialBlock(Properties properties, TagPrefix tagPrefix, Material material) {
        this(properties, tagPrefix, material, true);
    }

    @OnlyIn(Dist.CLIENT)
    public static BlockColor tintedColor() {
        return (state, reader, pos, tintIndex) -> {
            if (state.getBlock() instanceof MaterialBlock block) {
                return block.material.getLayerARGB(tintIndex);
            }
            return -1;
        };
    }

    public static VoxelShape FRAME_COLLISION_BOX = Shapes.box(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (this.tagPrefix == TagPrefix.frameGt) {
            return FRAME_COLLISION_BOX;
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    /** Start falling ore stuff */
    @SuppressWarnings("deprecation")
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (TagPrefix.ORES.containsKey(this.tagPrefix) && TagPrefix.ORES.get(tagPrefix).isSand() &&
                ConfigHolder.INSTANCE.worldgen.sandOresFall) {
            level.scheduleTick(pos, this, this.getDelayAfterPlace());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                                  BlockPos currentPos, BlockPos neighborPos) {
        if (TagPrefix.ORES.containsKey(this.tagPrefix) && TagPrefix.ORES.get(tagPrefix).isSand() &&
                ConfigHolder.INSTANCE.worldgen.sandOresFall) {
            level.scheduleTick(currentPos, this, this.getDelayAfterPlace());
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!FallingBlock.isFree(level.getBlockState(pos.below())) || pos.getY() < level.getMinBuildHeight()) {
            return;
        }
        FallingBlockEntity.fall(level, pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!TagPrefix.ORES.containsKey(this.tagPrefix) || !TagPrefix.ORES.get(tagPrefix).isSand() ||
                !ConfigHolder.INSTANCE.worldgen.sandOresFall)
            return;
        if (random.nextInt(16) == 0 && FallingBlock.isFree(level.getBlockState(pos.below()))) {
            double d = (double) pos.getX() + random.nextDouble();
            double e = (double) pos.getY() - 0.05;
            double f = (double) pos.getZ() + random.nextDouble();
            level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
        }
    }

    /**
     * Gets the amount of time in ticks this block will wait before attempting to start falling.
     */
    protected int getDelayAfterPlace() {
        return 2;
    }

    /** End falling ore stuff */

    @Override
    public String getDescriptionId() {
        return tagPrefix.getUnlocalizedName(material);
    }

    @Override
    public MutableComponent getName() {
        return tagPrefix.getLocalizedName(material);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (this.tagPrefix != TagPrefix.frameGt) {
            return super.use(state, level, pos, player, hand, hit);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty())
            return InteractionResult.PASS;

        if (stack.getItem() instanceof PipeBlockItem) {
            return replaceWithFramedPipe(level, pos, state, player, stack, hit) ? InteractionResult.SUCCESS :
                    InteractionResult.PASS;
        }

        Set<GTToolType> types = ToolHelper.getToolTypes(stack);
        if (!types.isEmpty() && ToolHelper.canUse(stack) && types.contains(GTToolType.CROWBAR)) {
            return removeFrame(level, pos, player, stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        var frameBlock = getFrameboxFromItem(stack);
        if (frameBlock == null) return InteractionResult.PASS;

        BlockPos.MutableBlockPos blockPos = pos.mutable();
        for (int i = 0; i < 32; i++) {
            if (level.getBlockState(blockPos).getBlock() instanceof MaterialBlock matBlock &&
                    matBlock.tagPrefix == TagPrefix.frameGt) {
                blockPos.move(Direction.UP);
                continue;
            }
            BlockEntity te = level.getBlockEntity(blockPos);
            if (te instanceof PipeBlockEntity<?, ?> pbe && !pbe.getFrameMaterial().isNull()) {
                blockPos.move(Direction.UP);
                continue;
            }
            if (canSupportRigidBlock(level, blockPos.below())) {
                level.setBlock(blockPos, frameBlock.defaultBlockState(), Block.UPDATE_ALL);
                if (!player.isCreative())
                    stack.shrink(1);
                return InteractionResult.SUCCESS;
            } else if (te instanceof PipeBlockEntity<?, ?> pbe && pbe.getFrameMaterial().isNull()) {
                pbe.setFrameMaterial(frameBlock.material);

                if (!player.isCreative())
                    stack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    public static MaterialBlock getFrameboxFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BlockItem ib) {
            Block block = ib.getBlock();
            if (block instanceof MaterialBlock matBlock)
                return matBlock.tagPrefix == TagPrefix.frameGt ? matBlock : null;
        }
        return null;
    }

    public boolean removeFrame(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof PipeBlockEntity<?, ?> pipeTile) {
            Material mat = pipeTile.getFrameMaterial();
            if (!mat.isNull()) {
                pipeTile.setFrameMaterial(GTMaterials.NULL);
                Block.popResource(level, pos, this.asItem().getDefaultInstance());
                ToolHelper.damageItem(stack, player);
                ToolHelper.playToolSound(GTToolType.CROWBAR, (ServerPlayer) player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        if (this.tagPrefix == TagPrefix.frameGt && useContext.getItemInHand().getItem() instanceof PipeBlockItem &&
                !useContext.getPlayer().isCrouching())
            return true;
        return super.canBeReplaced(state, useContext);
    }

    public boolean replaceWithFramedPipe(Level level, BlockPos pos, BlockState state, Player player,
                                         ItemStack stackInHand, BlockHitResult hit) {
        PipeBlock<?, ?, ?> pipeBlock = (PipeBlock<?, ?, ?>) ((PipeBlockItem) stackInHand.getItem()).getBlock();
        if (pipeBlock.pipeType.getThickness() < 1) {
            PipeBlockItem itemBlock = (PipeBlockItem) stackInHand.getItem();
            BlockState pipeState = pipeBlock.defaultBlockState();
            BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stackInHand,
                    hit);
            BlockState original = level.getBlockState(context.getClickedPos());
            itemBlock.placeBlock(context, pipeState);
            var pipeTile = pipeBlock.getPipeTile(level, pos);
            if (pipeTile instanceof PipeBlockEntity<?, ?> pipeBlockEntity) {
                pipeBlockEntity.setFrameMaterial(material);
            } else {
                // reset the state if we didn't place correctly
                level.setBlockAndUpdate(context.getClickedPos(), original);
                return false;
            }

            SoundType type = VanillaRecipeHelper.isMaterialWood(pipeTile.getFrameMaterial()) ? SoundType.WOOD :
                    SoundType.METAL;
            level.playSound(player, pos,
                    type.getPlaceSound(), SoundSource.BLOCKS,
                    (type.getVolume() + 1.0F) / 2.0F, type.getPitch() * 0.8F);
            if (!player.isCreative())
                stackInHand.shrink(1);
            return true;
        }
        return false;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (this.tagPrefix == TagPrefix.frameGt && entity instanceof LivingEntity livingEntity) {
            double currentAccel = 0.15D * (livingEntity.getDeltaMovement().y < 0.3D ? 2.5D : 1.0D);
            double currentSpeedVertical = 0.9D * (livingEntity.isInWater() ? 0.4D : 1.0D);
            Vec3 deltaMovement = livingEntity.getDeltaMovement();
            livingEntity.resetFallDistance();
            float f = 0.15F;
            double d0 = Mth.clamp(deltaMovement.x, -f, f);
            double d1 = Mth.clamp(deltaMovement.z, -f, f);
            double d2 = Math.max(deltaMovement.y, -f);
            if (d2 < 0.0 && !livingEntity.getFeetBlockState().isScaffolding(livingEntity) &&
                    livingEntity.isSuppressingSlidingDownLadder() &&
                    livingEntity instanceof Player) {
                d2 = Math.min(deltaMovement.y + currentAccel, 0.0D);
            }
            if (livingEntity.horizontalCollision) {
                d2 = 0.3;
            }
            deltaMovement = new Vec3(d0, d2, d1);
            entity.setDeltaMovement(deltaMovement);
        }
    }
}
