package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICentralMonitor;
import com.gregtechceu.gtceu.api.capability.IMonitorComponent;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.IMonitorModuleItem;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.*;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.machine.trait.CentralMonitorLogic;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;
import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CentralMonitorMachine extends WorkableElectricMultiblockMachine
                                   implements IMonitorComponent, IDataInfoProvider, IMachineLife, ICentralMonitor {

    @SaveField
    @SyncToClient
    @Getter
    private int leftDist = 0, rightDist = 0, upDist = 0, downDist = 0;
    @SaveField
    @SyncToClient
    @Getter
    @RerenderOnChanged
    private List<MonitorGroup> monitorGroups = new ArrayList<>();
    private final Set<IMonitorComponent> selectedComponents = new HashSet<>();
    private final List<IMonitorComponent> selectedTargets = new ArrayList<>();

    private MultiblockState patternFindingState;

    private static TraceabilityPredicate MULTI_PREDICATE = null;

    public CentralMonitorMachine(BlockEntityCreationInfo info) {
        super(info, CentralMonitorLogic::new);
    }

    public static TraceabilityPredicate getMultiPredicate() {
        if (MULTI_PREDICATE == null) {
            MULTI_PREDICATE = Predicates.abilities(PartAbility.INPUT_ENERGY)
                    .setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1)
                    .or(Predicates.abilities(PartAbility.DATA_ACCESS).setPreviewCount(1)
                            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_4).setPreviewCount(0))
                            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_16).setPreviewCount(0))
                            .setMaxGlobalLimited(4))
                    .or(Predicates.machines(GTMachines.HULL))
                    .or(Predicates.machines(GTMachines.MONITOR))
                    .or(Predicates.machines(GTMachines.ADVANCED_MONITOR))
                    .or(Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()));
        }
        return MULTI_PREDICATE;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.clearPatternFindingState();
    }

    @Override
    public CentralMonitorLogic getRecipeLogic() {
        return (CentralMonitorLogic) super.getRecipeLogic();
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
    }

    @Override
    public void onUnload() {
        super.onUnload();
        this.clearPatternFindingState();
    }

    protected void clearPatternFindingState() {
        if (this.patternFindingState != null)
            this.patternFindingState.clean();
        this.patternFindingState = null;
    }

    protected MultiblockState getPatternFindingState() {
        if (this.patternFindingState == null) {
            this.patternFindingState = new MultiblockState(getLevel(), getBlockPos());
            this.patternFindingState.clean();
        }
        return this.patternFindingState;
    }

    public boolean isValidMonitorBlock(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return false;

        MultiblockState state = getPatternFindingState();
        if (!state.update(pos, getMultiPredicate())) {
            return false;
        }
        state.io = IO.BOTH;

        return Stream.concat(state.predicate.common.stream(), state.predicate.limited.stream())
                .anyMatch(predicate -> predicate.test(state));
    }

    public void updateStructureDimensions() {
        Level level = getLevel();
        if (level == null) return;

        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();

        Direction left = RelativeDirection.LEFT.getRelative(front, spin, false);
        Direction right = RelativeDirection.RIGHT.getRelative(front, spin, false);
        Direction up = RelativeDirection.UP.getRelative(front, spin, false);
        Direction down = RelativeDirection.DOWN.getRelative(front, spin, false);
        BlockPos.MutableBlockPos posLeft = getBlockPos().mutable().move(left);
        BlockPos.MutableBlockPos posRight = getBlockPos().mutable().move(right);
        BlockPos.MutableBlockPos posUp = getBlockPos().mutable().move(up);
        BlockPos.MutableBlockPos posDown = getBlockPos().mutable().move(down);
        this.leftDist = 0;
        this.rightDist = 0;
        this.upDist = 0;
        this.downDist = 0;

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
    }

    private boolean isValidMonitorBlockRow(Level level, BlockPos pos, int leftDist, int rightDist, Direction left,
                                           Direction right) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        mutable.move(left, leftDist);
        for (int i = 0; i < leftDist + rightDist; i++) {
            if (!isValidMonitorBlock(level, mutable)) return false;
            mutable.move(right);
        }
        return isValidMonitorBlock(level, mutable);
    }

    @Override
    public BlockPattern getPattern() {
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

        String[] aisle = new String[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) {
            aisle[i] = pattern[i].toString();
        }

        return FactoryBlockPattern.start()
                .aisle(aisle)
                .where('B', getMultiPredicate())
                .where('C', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .build();
    }

    public BlockPos toRelative(BlockPos pos) {
        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();
        boolean flipped = isFlipped();
        Direction right = RelativeDirection.RIGHT.getRelative(front, spin, flipped);
        Direction up = RelativeDirection.UP.getRelative(front, spin, flipped);

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

        Direction left = RelativeDirection.LEFT.getRelative(front, spin, flipped);
        Direction up = RelativeDirection.UP.getRelative(front, spin, flipped);

        col = leftDist + rightDist - col;
        BlockPos pos = getBlockPos().relative(left, leftDist - col).relative(up, upDist - row);

        return GTCapabilityHelper.getMonitorComponent(level, pos, null);
    }

    public boolean isMonitor(int row, int col) {
        IMonitorComponent component = this.getComponent(row, col);
        if (component == null) return false;
        return component.isMonitor();
    }

    private IGuiTexture getComponentTexture(int row, int col) {
        if (row < 0 || col < 0 || row > downDist + upDist + 1 || col > leftDist + rightDist + 1)
            return GuiTextures.BLANK_TRANSPARENT;
        IMonitorComponent component = getComponent(row, col);
        if (component == null) return GuiTextures.BLANK_TRANSPARENT;
        return component.getComponentIcon();
    }

    private boolean isInAnyGroup(IMonitorComponent component) {
        return monitorGroups.stream().anyMatch(group -> group.contains(component.getBlockPos()));
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .addWorkingStatusLine();
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    @Override
    public IGuiTexture getComponentIcon() {
        return ResourceTexture.fromSpirit(GTCEu.id("block/multiblock/network_switch/overlay_front_active"));
    }

    @Override
    public @NotNull List<Component> getDebugInfo(Player player, int logLevel,
                                                 PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.translatable("gtceu.central_monitor.size", leftDist, rightDist, upDist, downDist));
    }

    @Override
    public @NotNull List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.translatable("gtceu.central_monitor.size", leftDist, rightDist, upDist, downDist));
    }

    @Override
    public void onMachineRemoved() {
        for (MonitorGroup group : monitorGroups) {
            clearInventory(group.getItemStackHandler());
            clearInventory(group.getPlaceholderSlotsHandler());
        }
    }
}
