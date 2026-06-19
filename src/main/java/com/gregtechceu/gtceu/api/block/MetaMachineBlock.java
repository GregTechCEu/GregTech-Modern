package com.gregtechceu.gtceu.api.block;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.capability.compat.EnergyStorageList;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.misc.EnergyInfoProviderList;
import com.gregtechceu.gtceu.api.misc.LaserContainerList;
import com.gregtechceu.gtceu.api.sync_system.ManagedSyncBlockEntity;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MetaMachineBlock extends Block implements EntityBlock {

    @Getter
    public final MachineDefinition definition;

    public MetaMachineBlock(Properties properties, MachineDefinition definition) {
        super(properties);
        this.definition = definition;
        RotationState rotationState = definition.getRotationState();
        if (rotationState != RotationState.NONE) {
            BlockState defaultState = this.defaultBlockState().setValue(rotationState.property,
                    rotationState.defaultDirection);
            if (definition.isAllowExtendedFacing()) {
                defaultState = defaultState.setValue(GTBlockStateProperties.UPWARDS_FACING, Direction.NORTH);
            }
            registerDefaultState(defaultState);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return getDefinition().getBlockEntityType().create(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        RotationState rotationState = MachineDefinition.getBuilt().getRotationState();
        if (rotationState != RotationState.NONE) {
            pBuilder.add(rotationState.property);
            if (MachineDefinition.getBuilt().isAllowExtendedFacing()) {
                pBuilder.add(GTBlockStateProperties.UPWARDS_FACING);
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getRotationState() == RotationState.NONE ? definition.getShape(Direction.NORTH) :
                definition.getShape(pState.getValue(getRotationState().property));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        var machine = MetaMachine.getMachine(level, pos);
        if (machine != null) {
            machine.animateTick(random);
        }
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity player,
                            ItemStack pStack) {
        if (!pLevel.isClientSide) {
            var machine = MetaMachine.getMachine(pLevel, pPos);
            if (machine != null) {
                machine.onMachinePlaced(player, pStack);
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
            if (getDefinition().isAllowExtendedFacing()) {
                Direction frontFacing = state.getValue(rotationState.property);
                if (frontFacing == Direction.UP) {
                    state = state.setValue(GTBlockStateProperties.UPWARDS_FACING, player.getDirection());
                } else if (frontFacing == Direction.DOWN) {
                    state = state.setValue(GTBlockStateProperties.UPWARDS_FACING, player.getDirection().getOpposite());
                }
            }
        }
        return state;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext level, List<Component> tooltip,
                                TooltipFlag flag) {
        definition.getTooltipBuilder().accept(stack, tooltip);
        String mainKey = String.format("%s.machine.%s.tooltip", definition.getId().getNamespace(),
                definition.getId().getPath());
        if (GTUtil.isShiftDown()) {
            if (definition instanceof MultiblockMachineDefinition multiblockDefinition) {
                var pattern = multiblockDefinition.getPatternFactory().get();
                if (pattern != null) {
                    var aisleDims = pattern.getDimensions();
                    assert aisleDims.length == 3;
                    tooltip.add(Component.translatable("gtceu.multiblock.dimension", aisleDims[0], aisleDims[1],
                            aisleDims[2]));
                }
            }
        }
        if (Language.getInstance().has(mainKey)) {
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
        if (getRotationState() == RotationState.NONE) {
            return pState;
        }
        return pState.setValue(getRotationState().property,
                pRotation.rotate(pState.getValue(getRotationState().property)));
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        var drops = super.getDrops(state, builder);

        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MetaMachine machine) {
            machine.modifyDrops(drops);
            for (ItemStack drop : drops) {
                if (drop.getItem() instanceof MetaMachineItem item && item.getBlock() == this) {
                    machine.saveToItem(drop, be.getLevel().registryAccess());
                    // break here to not dupe contents if a machine drops multiple of itself for whatever reason.
                    break;
                }
            }
        }
        return drops;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity()) {
            if (!pState.is(pNewState.getBlock())) { // new block
                MetaMachine machine = MetaMachine.getMachine(pLevel, pPos);
                if (machine != null) {
                    machine.onMachineDestroyed();
                }

                pLevel.updateNeighbourForOutputSignal(pPos, this);
                pLevel.removeBlockEntity(pPos);
            } else if (getRotationState() != RotationState.NONE) { // old block different facing
                var oldFacing = pState.getValue(getRotationState().property);
                var newFacing = pNewState.getValue(getRotationState().property);
                if (newFacing != oldFacing) {
                    var machine = MetaMachine.getMachine(pLevel, pPos);
                    if (machine != null) {
                        machine.onRotated(oldFacing, newFacing);
                    }
                }
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return ItemInteractionResult.FAIL;
        ItemStack itemStack = player.getItemInHand(hand);
        boolean shouldOpenUi = true;

        if (machine.getOwnerUUID() == null && player instanceof ServerPlayer sPlayer) {
            machine.setOwnerUUID(sPlayer.getUUID());
        }

        InteractionResult machineInteractResult;
        if (itemStack.isEmpty()) {
            machineInteractResult = machine.onUse(new ExtendedUseOnContext(player, hand, hit));
        } else {
            machineInteractResult = machine.onUseWithItem(new ExtendedUseOnContext(player, hand, hit));
        }

        if (machineInteractResult != InteractionResult.PASS) return getFromInteractionResult(machineInteractResult);

        if (stack.is(GTItems.PORTABLE_SCANNER.get())) {
            return getFromInteractionResult(stack.getItem().use(level, player, hand).getResult());
        }

        if (stack.getItem() instanceof IGTTool gtToolItem) {
            shouldOpenUi = gtToolItem.definition$shouldOpenUIAfterUse(new UseOnContext(player, hand, hit));
        }

        if (shouldOpenUi && machine instanceof IUIMachine uiMachine &&
                MachineOwner.canOpenOwnerMachine(player, machine)) {
            return uiMachine.tryToOpenUI(player, hand, hit);
        }
        return shouldOpenUi ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return InteractionResult.PASS;
        if (machine instanceof IUIMachine uiMachine &&
                MachineOwner.canOpenOwnerMachine(player, machine)) {
            uiMachine.tryToOpenUI(player, InteractionHand.MAIN_HAND, hit).result();
        }
        return machine.onUse(new ExtendedUseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    //////////////////////////////////////
    // ***** Redstone Signals ****//
    //////////////////////////////////////

    public boolean canConnectRedstone(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return false;
        return machine.canConnectRedstone(side);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return 0;
        return machine.getOutputSignal(direction);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return 0;
        return machine.getOutputDirectSignal(direction);
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine == null) return 0;
        return machine.getAnalogOutputSignal();
    }

    /////////

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                boolean isMoving) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine != null) {
            machine.onNeighborChanged(block, fromPos, isMoving);
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    public static int colorTinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                                  int index) {
        if (level != null && pos != null) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                return machine.tintColor(index);
            }
        }
        return -1;
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                    @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        var machine = MetaMachine.getMachine(level, pos);
        if (machine != null) {
            return machine.getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
        }
        return super.getAppearance(state, level, pos, side, sourceState, sourcePos);
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

    public RotationState getRotationState() {
        return getDefinition().getRotationState();
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                return machine.getCoverContainer();
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_WORKABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IWorkable workable) {
                    return workable;
                }
                for (MachineTrait trait : machine.getTraitHolder().getAllTraits()) {
                    if (trait instanceof IWorkable workable) {
                        return workable;
                    }
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_CONTROLLABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IControllable controllable) {
                    return controllable;
                }
                for (MachineTrait trait : machine.getTraitHolder().getAllTraits()) {
                    if (trait instanceof IControllable controllable) {
                        return controllable;
                    }
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_RECIPE_LOGIC, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                for (MachineTrait trait : machine.getTraitHolder().getAllTraits()) {
                    if (trait instanceof RecipeLogic recipeLogic) {
                        return recipeLogic;
                    }
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IEnergyContainer energyContainer) {
                    return energyContainer;
                }
                var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), side,
                        IEnergyContainer.class);
                if (!list.isEmpty()) {
                    return list.size() == 1 ? list.getFirst() : new EnergyContainerList(list);
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IEnergyInfoProvider energyInfoProvider) {
                    return energyInfoProvider;
                }
                var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), side,
                        IEnergyInfoProvider.class);
                if (!list.isEmpty()) {
                    return list.size() == 1 ? list.getFirst() : new EnergyInfoProviderList(list);
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_MAINTENANCE_MACHINE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof IMaintenanceMachine maintenance) {
                return maintenance;
            }
            return null;
        }, this);
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                return machine.getItemHandlerCap(side, true);
            }
            return null;
        }, this);
        event.registerBlock(Capabilities.FluidHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                return machine.getFluidHandlerCap(side, true);
            }
            return null;
        }, this);
        event.registerBlock(Capabilities.EnergyStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IEnergyStorage energyStorage) {
                    return energyStorage;
                }
                var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), side,
                        IEnergyStorage.class);
                if (!list.isEmpty()) {
                    return list.size() == 1 ? list.getFirst() : new EnergyStorageList(list);
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_LASER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof ILaserContainer laserContainer) {
                    return laserContainer;
                }
                var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), side,
                        ILaserContainer.class);
                if (!list.isEmpty()) {
                    return list.size() == 1 ? list.getFirst() : new LaserContainerList(list);
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IOpticalComputationProvider computationProvider) {
                    return computationProvider;
                }
                var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), side,
                        IOpticalComputationProvider.class);
                if (!list.isEmpty()) {
                    return list.getFirst();
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_DATA_ACCESS, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IDataAccessHatch dataAccess) {
                    return dataAccess;
                }
                var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), side,
                        IDataAccessHatch.class);
                if (!list.isEmpty()) {
                    return list.getFirst();
                }
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_MONITOR_COMPONENT, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof MetaMachine machine) {
                if (machine instanceof IMonitorComponent monitorComponent) {
                    return monitorComponent;
                }
                var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), side,
                        IMonitorComponent.class);
                if (!list.isEmpty()) {
                    return list.getFirst();
                }
            }
            return null;
        }, this);
        if (GTCEu.Mods.isAE2Loaded()) {
            event.registerBlock(AECapabilities.IN_WORLD_GRID_NODE_HOST, (level, pos, state, blockEntity, side) -> {
                if (blockEntity instanceof MetaMachine machine) {
                    if (machine instanceof IInWorldGridNodeHost nodeHost) {
                        return nodeHost;
                    }
                    var list = getCapabilitiesFromTraits(machine.getTraitHolder().getAllTraits(), null,
                            IInWorldGridNodeHost.class);
                    if (!list.isEmpty()) {
                        // TODO wrap list in the future (or not.)
                        return list.getFirst();
                    }
                }
                return null;
            }, this);
        }
    }

    static <T> List<T> getCapabilitiesFromTraits(List<MachineTrait> traits, @Nullable Direction accessSide,
                                                 Class<T> capability) {
        if (traits.isEmpty()) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (MachineTrait trait : traits) {
            if (trait.hasCapability(accessSide) && capability.isInstance(trait)) {
                list.add(capability.cast(trait));
            }
        }
        return list;
    }

    public Direction getFrontFacing(BlockState state) {
        return getRotationState() == RotationState.NONE ? Direction.NORTH : state.getValue(getRotationState().property);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (!level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    pTile.setChanged();
                    if (pTile instanceof MetaMachine metaMachine) {
                        metaMachine.serverTick();
                    }
                    if (pTile instanceof ManagedSyncBlockEntity syncObj) {
                        syncObj.updateTick();
                    }
                };
            } else {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof MetaMachine metaMachine) {
                        metaMachine.clientTick();
                    }
                };
            }
        }
        return null;
    }
}
