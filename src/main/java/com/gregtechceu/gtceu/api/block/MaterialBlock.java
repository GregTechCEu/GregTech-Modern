package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.PipeBlockItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.client.renderer.block.MaterialBlockRenderer;
import com.gregtechceu.gtceu.config.ConfigHolder;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/27
 * @implNote MaterialBlock
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MaterialBlock extends AppearanceBlock {

    public final TagPrefix tagPrefix;
    public final Material material;

    public MaterialBlock(Properties properties, TagPrefix tagPrefix, Material material, boolean registerModel) {
        super(properties);
        this.material = material;
        this.tagPrefix = tagPrefix;
        if (registerModel && Platform.isClient()) {
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.isEmpty())
            return InteractionResult.PASS;

        if(stack.getItem() instanceof PipeBlockItem) {
            return replaceWithFramedPipe(level, pos, state, player, stack, hit) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        Set<GTToolType> types = ToolHelper.getToolTypes(stack);
        if (!types.isEmpty() && ToolHelper.canUse(stack) && types.contains(GTToolType.CROWBAR)) {
            return removeFrame(level, pos, player, stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        var frameBlock = getFrameboxFromItem(stack);
        if(frameBlock == null) return InteractionResult.PASS;

        BlockPos.MutableBlockPos blockPos = BlockPos.ZERO.mutable();
        blockPos.set(pos);
        for(int i = 0; i < 32; i++) {
            if(level.getBlockState(blockPos).getBlock() instanceof MaterialBlock matBlock && matBlock.tagPrefix == TagPrefix.frameGt) {
                blockPos.move(Direction.UP);
                continue;
            }
            BlockEntity te = level.getBlockEntity(blockPos);
            if(te instanceof PipeBlockEntity<?,?> pbe && pbe.getFrameMaterial() != null) {
                blockPos.move(Direction.UP);
                continue;
            }
            if(canSupportRigidBlock(level, blockPos.below())) {
                level.setBlock(blockPos, frameBlock.defaultBlockState(), 3);
                if(!player.isCreative())
                    stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            else if(te instanceof PipeBlockEntity<?,?> pbe && pbe.getFrameMaterial() == null) {
                pbe.setFrameMaterial(frameBlock.material);

                if(!player.isCreative())
                    stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            else {
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    public static MaterialBlock getFrameboxFromItem(ItemStack stack) {
        Item item = stack.getItem();
        if(item instanceof BlockItem ib) {
            Block block =  ib.getBlock();
            if(block instanceof MaterialBlock matBlock) return matBlock.tagPrefix == TagPrefix.frameGt ? matBlock : null;
        }
        return null;
    }

    public boolean removeFrame(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockEntity te = level.getBlockEntity(pos);
        if(te instanceof PipeBlockEntity<?,?> pipeTile) {
            Material mat = pipeTile.getFrameMaterial();
            if(mat !=  null) {
                pipeTile.setFrameMaterial(null);
                Block.popResource(level, pos, this.asItem().getDefaultInstance());
                ToolHelper.damageItem(stack, player);
                ToolHelper.playToolSound(GTToolType.CROWBAR, (ServerPlayer)player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
        if(this.tagPrefix == TagPrefix.frameGt && useContext.getItemInHand().getItem() instanceof PipeBlockItem)
            return true;
        return super.canBeReplaced(state, useContext);
    }

    public boolean replaceWithFramedPipe(Level level, BlockPos pos, BlockState state, Player player, ItemStack stackInHand, BlockHitResult hit) {
        PipeBlock<?, ?, ?> pipeBlock = (PipeBlock<?, ?, ?>) ((PipeBlockItem)stackInHand.getItem()).getBlock();
        if(pipeBlock.pipeType.getThickness() < 1) {
            PipeBlockItem itemBlock = (PipeBlockItem)stackInHand.getItem();
            BlockState pipeState = pipeBlock.defaultBlockState();
            BlockPlaceContext context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stackInHand, hit);
            itemBlock.placeBlock(context, pipeState);
            var pipeTile = pipeBlock.getPipeTile(level, pos);
            if(pipeTile instanceof PipeBlockEntity) {
                ((PipeBlockEntity<? extends Enum<?>, ?>) pipeTile).setFrameMaterial(material);
            } else {
                GTCEu.LOGGER.error("Pipe was not placed!");
                return false;
            }
            if(!player.isCreative())
                stackInHand.shrink(1);
            return true;
        }
        return false;
    }
}
