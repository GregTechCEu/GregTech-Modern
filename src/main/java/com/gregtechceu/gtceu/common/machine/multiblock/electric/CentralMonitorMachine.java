package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.multiblock.*;
import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.pattern.*;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.item.behavior.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.machine.trait.CentralMonitorLogic;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.IDrawable;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CentralMonitorMachine extends WorkableElectricMultiblockMachine
                                   implements IMonitorComponent, IDataInfoProvider {

    @SaveField
    @SyncToClient
    @Getter
    private int leftDist = 0, rightDist = 0, upDist = 0, downDist = 0;
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    @Getter
    private List<MonitorGroup> monitorGroups = new ArrayList<>();

    private static @Nullable PatternPredicate MULTI_PREDICATE = null;

    public CentralMonitorMachine(BlockEntityCreationInfo info) {
        super(info, new CentralMonitorLogic());
    }

    public static PatternPredicate getMultiPredicate() {
        if (MULTI_PREDICATE == null) {
            MULTI_PREDICATE = Predicates.machines(GTMachines.MONITOR)
                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY)
                            .setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.DATA_ACCESS).setPreviewCount(1)
                            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_4).setPreviewCount(0))
                            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_16).setPreviewCount(0))
                            .setMaxGlobalLimited(4))
                    .or(Predicates.machines(GTMachines.HULL).setPreviewCount(0))
                    .or(Predicates.machines(GTMachines.ADVANCED_MONITOR))
                    .or(Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()));
        }
        return MULTI_PREDICATE;
    }

    @Override
    public CentralMonitorLogic getRecipeLogic() {
        return (CentralMonitorLogic) super.getRecipeLogic();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateStructureDimensions();
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        updateStructureDimensions();
    }

    public @Nullable EnergyContainerList getFormedEnergyContainer() {
        return this.energyContainer;
    }

    public void tick() {
        Level level = getLevel();
        if (level == null) {
            return;
        }

        for (MonitorGroup group : monitorGroups) {
            ItemStack stack = group.getItemStackHandler().getStackInSlot(0);
            if (stack.isEmpty() || !(stack.getItem() instanceof IComponentItem componentItem)) {
                continue;
            }

            for (IItemComponent component : componentItem.getComponents()) {
                if (!(component instanceof IMonitorModuleItem module)) {
                    continue;
                }
                module.tick(stack, this, group);
                GTNetwork.sendToAllPlayersTrackingChunk(level.getChunkAt(getBlockPos()),
                        new SCPacketMonitorGroupNBTChange(stack, group, this));
            }
        }
        getSyncDataHolder().markClientSyncFieldDirty("monitorGroups");
        recipeLogic.getSyncDataHolder().markClientSyncFieldDirty("status");
    }

    public static boolean isValidMonitorBlock(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return false;
        CurrentBlockInfo info = new CurrentBlockInfo();
        info.setLevel(level);
        info.setCurrentPos(pos);
        return getMultiPredicate().test(info, new Object2IntOpenHashMap<>(), null).isEmpty();
    }

    public void updateStructureDimensions() {
        Level level = getLevel();
        if (level == null) return;

        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();
        IntList bounds = getBounds(level, getBlockPos().mutable(), front, spin);
        this.upDist = bounds.getInt(0);
        this.downDist = bounds.getInt(1);
        this.leftDist = bounds.getInt(2);
        this.rightDist = bounds.getInt(3);

        getSyncDataHolder().markClientSyncFieldDirty("leftDist");
        getSyncDataHolder().markClientSyncFieldDirty("rightDist");
        getSyncDataHolder().markClientSyncFieldDirty("upDist");
        getSyncDataHolder().markClientSyncFieldDirty("downDist");
    }

    public static IntList getBounds(Level level, BlockPos.MutableBlockPos controllerPos, Direction front,
                                    Direction upFace) {
        Direction left = RelativeDirection.LEFT.getRelativeFacing(front, upFace, false);
        Direction right = RelativeDirection.RIGHT.getRelativeFacing(front, upFace, false);
        Direction up = RelativeDirection.UP.getRelativeFacing(front, upFace, false);
        Direction down = RelativeDirection.DOWN.getRelativeFacing(front, upFace, false);
        BlockPos.MutableBlockPos posLeft = controllerPos.mutable().move(left);
        BlockPos.MutableBlockPos posRight = controllerPos.mutable().move(right);
        BlockPos.MutableBlockPos posUp = controllerPos.mutable().move(up);
        BlockPos.MutableBlockPos posDown = controllerPos.mutable().move(down);
        int leftDist = 0;
        int rightDist = 0;
        int upDist = 0;
        int downDist = 0;

        while (isValidMonitorBlock(level, posLeft)) {
            posLeft.move(left);
            leftDist++;
        }
        while (isValidMonitorBlock(level, posRight)) {
            posRight.move(right);
            rightDist++;
        }
        while (isValidMonitorBlockRow(level, posUp, leftDist, rightDist, left, right)) {
            posUp.move(up);
            upDist++;
        }
        while (isValidMonitorBlockRow(level, posDown, leftDist, rightDist, left, right)) {
            posDown.move(down);
            downDist++;
        }

        if (leftDist + rightDist + upDist + downDist == 0) {
            upDist = 1;
            downDist = 1;
            leftDist = 3;
        }

        return IntList.of(upDist, downDist, leftDist, rightDist, 0, 0);
    }

    private static boolean isValidMonitorBlockRow(Level level, BlockPos pos, int leftDist, int rightDist,
                                                  Direction left,
                                                  Direction right) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        mutable.move(left, leftDist);
        for (int i = 0; i < leftDist + rightDist; i++) {
            if (!isValidMonitorBlock(level, mutable)) return false;
            mutable.move(right);
        }
        return isValidMonitorBlock(level, mutable);
    }

    public static List<IntIntPair> getConstraints() {
        return List.of(
                IntIntPair.of(0, 10),
                IntIntPair.of(0, 10),
                IntIntPair.of(0, 10),
                IntIntPair.of(0, 10),
                IntIntPair.of(0, 0),
                IntIntPair.of(0, 0));
    }

    public static IBlockPattern getPattern(MultiblockMachineDefinition definition) {
        PatternPredicate predicate = getMultiPredicate();
        return ExpandableMultiblockPatternBuilder
                .start()
                .boundsProvider(CentralMonitorMachine::getBounds)
                .constraintProvider(CentralMonitorMachine::getConstraints)
                .predicateProvider((pos, bounds) -> {
                    if (pos.equals(BlockPos.ZERO))
                        return Predicates.controller(definition);
                    return predicate;
                })
                .build();
    }

    @Override
    public IBlockPattern getDefaultStructurePattern() {
        updateStructureDimensions();
        if (leftDist + rightDist < 1 || upDist + downDist < 1) {
            leftDist = 3;
            rightDist = 0;
            upDist = 1;
            downDist = 1;
        }

        StringBuilder[] pattern = new StringBuilder[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) {
            pattern[i] = new StringBuilder(leftDist + rightDist + 1);
            for (int j = 0; j < leftDist + rightDist + 1; j++) {
                if (i == downDist && j == rightDist)
                    pattern[i].append('C'); // controller
                else
                    pattern[i].append('B'); // any valid block
            }
        }

        String[] slice = new String[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) {
            slice[i] = pattern[i].toString();
        }

        return MultiblockPatternBuilder.start()
                .slice(slice)
                .where('B', getMultiPredicate())
                .where('C', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .build();
    }

    public BlockPos toRelative(BlockPos pos) {
        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();
        boolean flipped = isFlipped();
        Direction right = RelativeDirection.RIGHT.getRelativeFacing(front, spin, flipped);
        Direction up = RelativeDirection.UP.getRelativeFacing(front, spin, flipped);

        BlockPos tmp = getBlockPos().mutable().move(right, rightDist).move(up, upDist);

        return new BlockPos(Math.abs(tmp.get(right.getAxis()) - pos.get(right.getAxis())),
                Math.abs(tmp.get(up.getAxis()) - pos.get(up.getAxis())),
                0);
    }

    @Nullable
    public IMonitorComponent getComponent(int row, int col) {
        Level level = getLevel();
        if (level == null) return null;

        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();
        boolean flipped = isFlipped();

        Direction left = RelativeDirection.LEFT.getRelativeFacing(front, spin, flipped);
        Direction up = RelativeDirection.UP.getRelativeFacing(front, spin, flipped);

        col = leftDist + rightDist - col;
        BlockPos pos = getBlockPos().relative(left, leftDist - col).relative(up, upDist - row);

        return GTCapabilityHelper.getMonitorComponent(level, pos, null);
    }

    public void setMonitorGroups(List<MonitorGroup> groups) {
        if (!(monitorGroups instanceof ArrayList<MonitorGroup>)) monitorGroups = new ArrayList<>(monitorGroups);
        monitorGroups.clear();
        monitorGroups.addAll(groups);
        getSyncDataHolder().markClientSyncFieldDirty("monitorGroups");
    }

    @Override
    public IDrawable getIcon() {
        return GTGuiTextures.GREGTECH_LOGO;
    }

    @Override
    public List<Component> getDebugInfo(Player player, int logLevel,
                                        PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.translatable("gtceu.central_monitor.size", leftDist, rightDist, upDist, downDist));
    }

    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.translatable("gtceu.central_monitor.size", leftDist, rightDist, upDist, downDist));
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        for (MonitorGroup group : monitorGroups) {
            group.getItemStackHandler().dropInventoryInWorld(getLevel(), getBlockPos());
            group.getPlaceholderSlotsHandler().dropInventoryInWorld(getLevel(), getBlockPos());
        }
    }
}
