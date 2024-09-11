package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.api.RotationState;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote GTBlock
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MetaMachineBlock extends AppearanceBlock implements IMachineBlock {

    @Getter
    public final MachineDefinition definition;
    @Getter
    public final RotationState rotationState;

    public MetaMachineBlock(Properties properties, MachineDefinition definition) {
        super(properties);
        this.definition = definition;
        this.rotationState = RotationState.get();
        if (rotationState != RotationState.NONE) {
            BlockState defaultState = this.defaultBlockState().setValue(rotationState.property,
                    rotationState.defaultDirection);
            if (definition instanceof MultiblockMachineDefinition multi && multi.isAllowExtendedFacing()) {
                defaultState = defaultState.setValue(IMachineBlock.UPWARDS_FACING_PROPERTY, Direction.NORTH);
            }
            registerDefaultState(defaultState);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(BlockProperties.SERVER_TICK);
        RotationState rotationState = RotationState.get();
        if (rotationState != RotationState.NONE) {
            pBuilder.add(rotationState.property);
            if (MachineDefinition.getBuilt() instanceof MultiblockMachineDefinition multi &&
                    multi.isAllowExtendedFacing()) {
                pBuilder.add(IMachineBlock.UPWARDS_FACING_PROPERTY);
            }
        }
    }

    @Nullable
    public MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        return MetaMachine.getMachine(level, pos);
    }

    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return definition.getRenderer();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getRotationState() == RotationState.NONE ? definition.getShape(Direction.NORTH) :
                definition.getShape(pState.getValue(getRotationState().property));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        var machine = getMachine(level, pos);
        if (machine != null) {
            machine.animateTick(random);
        }
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity player,
                            ItemStack pStack) {
        if (!pLevel.isClientSide) {
            var machine = getMachine(pLevel, pPos);
            if (machine instanceof IDropSaveMachine dropSaveMachine) {
                CustomData tag = pStack.get(DataComponents.BLOCK_ENTITY_DATA);
                if (tag != null) {
                    dropSaveMachine.loadFromItem(tag.copyTag());
                }
            }
            if (machine instanceof IMachineLife machineLife) {
                machineLife.onMachinePlaced(player, pStack);
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // needed to trigger block updates so machines connect to open cables properly.
        level.updateNeighbourForOutputSignal(pos, this);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        RotationState rotationState = getRotationState();
        var player = context.getPlayer();
        var blockPos = context.getClickedPos();
        var state = defaultBlockState();
        if (player != null && rotationState != RotationState.NONE) {
            if (rotationState == RotationState.Y_AXIS) {
                state = state.setValue(rotationState.property, Direction.UP);
            } else {
                state = state.setValue(rotationState.property, player.getDirection().getOpposite());
            }
            Vec3 pos = player.position();
            if (Math.abs(pos.x - (double) ((float) blockPos.getX() + 0.5F)) < 2.0D &&
                    Math.abs(pos.z - (double) ((float) blockPos.getZ() + 0.5F)) < 2.0D) {
                double d0 = pos.y + (double) player.getEyeHeight();
                if (d0 - (double) blockPos.getY() > 2.0D && rotationState.test(Direction.UP)) {
                    state = state.setValue(rotationState.property, Direction.UP);
                }
                if ((double) blockPos.getY() - d0 > 0.0D && rotationState.test(Direction.DOWN)) {
                    state = state.setValue(rotationState.property, Direction.DOWN);
                }
            }
            if (getDefinition() instanceof MultiblockMachineDefinition multi && multi.isAllowExtendedFacing()) {
                Direction frontFacing = state.getValue(rotationState.property);
                if (frontFacing == Direction.UP) {
                    state = state.setValue(IMachineBlock.UPWARDS_FACING_PROPERTY, player.getDirection());
                } else if (frontFacing == Direction.DOWN) {
                    state = state.setValue(IMachineBlock.UPWARDS_FACING_PROPERTY, player.getDirection().getOpposite());
                }
            }
        }
        return state;
    }

    public Direction getFrontFacing(BlockState state) {
        return getRotationState() == RotationState.NONE ? Direction.NORTH : state.getValue(getRotationState().property);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
                                       Player player) {
        ItemStack itemStack = super.getCloneItemStack(state, target, level, pos, player);
        if (getMachine(level, pos) instanceof IDropSaveMachine dropSaveMachine && dropSaveMachine.savePickClone()) {
            CompoundTag tag = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            dropSaveMachine.saveToItem(tag);
            itemStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        }
        return itemStack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, List<Component> tooltip,
                                TooltipFlag flag) {
        definition.getTooltipBuilder().accept(stack, tooltip);
        String mainKey = String.format("%s.machine.%s.tooltip", definition.getId().getNamespace(),
                definition.getId().getPath());
        if (LocalizationUtils.exist(mainKey)) {
            tooltip.add(1, Component.translatable(mainKey));
        }
    }

    @Override
    public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile != null) {
            return tile.triggerEvent(pId, pParam);
        }
        return false;
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        if (this.rotationState == RotationState.NONE) {
            return pState;
        }
        return pState.setValue(this.rotationState.property,
                pRotation.rotate(pState.getValue(this.rotationState.property)));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity tileEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        var drops = super.getDrops(state, builder);
        if (tileEntity instanceof IMachineBlockEntity holder) {
            var machine = holder.getMetaMachine();
            if (machine instanceof IMachineModifyDrops machineModifyDrops) {
                machineModifyDrops.onDrops(drops);
            }
            if (machine instanceof IDropSaveMachine dropSaveMachine && dropSaveMachine.saveBreak()) {
                for (ItemStack drop : drops) {
                    if (drop.getItem() instanceof MetaMachineItem item && item.getBlock() == this) {
                        CompoundTag tag = drop.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY)
                                .copyTag();
                        dropSaveMachine.saveToItem(tag);
                        tag.remove("id");
                        BlockEntity.addEntityType(tag, tileEntity.getType());
                        drop.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
                        // break here to not dupe contents if a machine drops multiple of itself for whatever reason.
                        break;
                    }
                }
            }
        }
        return drops;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity()) {
            if (!pState.is(pNewState.getBlock())) { // new block
                MetaMachine machine = getMachine(pLevel, pPos);
                if (machine instanceof IMachineLife machineLife) {
                    machineLife.onMachineRemoved();
                }
                if (machine != null) {
                    for (Direction direction : GTUtil.DIRECTIONS) {
                        machine.getCoverContainer().removeCover(direction, null);
                    }
                }

                pLevel.updateNeighbourForOutputSignal(pPos, this);
                pLevel.removeBlockEntity(pPos);
            } else if (rotationState != RotationState.NONE) { // old block different facing
                var oldFacing = pState.getValue(rotationState.property);
                var newFacing = pNewState.getValue(rotationState.property);
                if (newFacing != oldFacing) {
                    var machine = getMachine(pLevel, pPos);
                    if (machine != null) {
                        machine.onRotated(oldFacing, newFacing);
                    }
                }
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        var machine = getMachine(world, pos);
        boolean shouldOpenUi = true;

        Set<GTToolType> types = ToolHelper.getToolTypes(stack);
        if (machine != null && !types.isEmpty() && ToolHelper.canUse(stack)) {
            var result = machine.onToolClick(types, stack,
                    new UseOnContext(player, hand, hit));
            if (result.getSecond() == ItemInteractionResult.CONSUME && player instanceof ServerPlayer serverPlayer) {
                ToolHelper.playToolSound(result.getFirst(), serverPlayer);

                if (!serverPlayer.isCreative()) {
                    ToolHelper.damageItem(stack, serverPlayer, 1);
                }
            }
            if (result.getSecond().result() != InteractionResult.PASS) return result.getSecond();
        }

        if (stack.is(GTItems.PORTABLE_SCANNER.get())) {
            return getFromInteractionResult(stack.getItem().use(world, player, hand).getResult());
        }

        if (stack.getItem() instanceof IGTTool gtToolItem) {
            shouldOpenUi = gtToolItem
                    .definition$shouldOpenUIAfterUse(new UseOnContext(player, hand, hit));
        }

        if (machine instanceof IInteractedMachine interactedMachine) {
            var result = interactedMachine.onUse(state, world, pos, player, hand, hit);
            if (result.result() != InteractionResult.PASS) return result;
        }
        if (shouldOpenUi && machine instanceof IUIMachine uiMachine) {
            return uiMachine.tryToOpenUI(player, hand, hit);
        }
        return shouldOpenUi ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.CONSUME;
    }

    public boolean canConnectRedstone(BlockGetter level, BlockPos pos, Direction side) {
        return getMachine(level, pos).canConnectRedstone(side);
    }

    @Override
    @SuppressWarnings("deprecation") // This is fine to override, just not to be called.
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getMachine(level, pos).getOutputSignal(direction);
    }

    @Override
    @SuppressWarnings("deprecation") // This is fine to override, just not to be called.
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getMachine(level, pos).getOutputDirectSignal(direction);
    }

    @Override
    @SuppressWarnings("deprecation") // This is fine to override, just not to be called.
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getMachine(level, pos).getAnalogOutputSignal();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                boolean isMoving) {
        var machine = getMachine(level, pos);
        if (machine != null) {
            machine.onNeighborChanged(block, fromPos, isMoving);
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                         BlockState sourceState, BlockPos sourcePos) {
        var machine = getMachine(level, pos);
        if (machine != null) {
            return machine.getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
        }
        return super.getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
    }

    public static ItemInteractionResult getFromInteractionResult(InteractionResult result) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case FAIL -> ItemInteractionResult.FAIL;
        };
    }
}
