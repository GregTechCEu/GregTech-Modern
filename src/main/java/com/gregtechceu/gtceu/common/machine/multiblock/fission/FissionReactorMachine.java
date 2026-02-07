package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FissionReactorMachine extends MultiblockControllerMachine implements IMuiMachine {

    @Getter
    private final int vesselHeatMax;
    @SaveField
    @Getter
    private final ReactorGrid grid;
    @Nullable
    private TickableSubscription simulationSubs;
    @SaveField
    @SyncToClient
    @Getter
    private int vesselHeat;
    @SaveField
    @SyncToClient
    @Getter
    private boolean running;
    @Getter
    private int reactorHeight;

    public FissionReactorMachine(BlockEntityCreationInfo info, int vesselHeatMax) {
        super(info);
        this.vesselHeatMax = vesselHeatMax;
        this.grid = new ReactorGrid(vesselHeatMax);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        detectHeight();
        rebuildGrid();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSimulation));
        }
    }

    private void detectHeight() {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (IMultiPart part : getParts()) {
            int y = part.self().getBlockPos().getY();
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        reactorHeight = minY < maxY ? (maxY - minY - 1) : 3;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        grid.clear();
        running = false;
        if (simulationSubs != null) {
            simulationSubs.unsubscribe();
            simulationSubs = null;
        }
    }

    @Override
    public void onUnload() {
        if (simulationSubs != null) {
            simulationSubs.unsubscribe();
            simulationSubs = null;
        }
        super.onUnload();
    }

    private void updateSimulation() {
        if (isFormed() && running) {
            simulationSubs = subscribeServerTick(simulationSubs, this::simulationTick);
        } else if (simulationSubs != null) {
            simulationSubs.unsubscribe();
            simulationSubs = null;
        }
    }

    private void simulationTick() {
        if (!isFormed() || !running) {
            updateSimulation();
            return;
        }

        // TODO: re-enable fuel/coolant activity checks once fuel items and coolant fluids are defined
        // updateFuelRodActivity();
        // updateCoolantActivity();

        float[] multipliers = getHeightMultipliers();
        grid.tick(multipliers[0], multipliers[1]);
        vesselHeat = grid.getVesselHeat();
        syncDataHolder.markClientSyncFieldDirty("vesselHeat");

        processMeltdowns();
    }

    // This prevents two invalidation paths related to the structure itself
    // reactor has to maintain formation while parts break:
    // 1. Part onUnload() -> controller.onPartUnload() -> async re-form attempt
    // 2. LevelMixin onBlockStateChanged() -> pattern re-check -> structure invalid
    // 3. If you remove this the entire multi breaks! So DO NOT.
    private void processMeltdowns() {
        Set<BlockPos> melted = grid.collectMeltdowns();
        if (melted.isEmpty()) return;

        if (!(getLevel() instanceof ServerLevel serverLevel)) return;

        for (BlockPos pos : melted) {
            grid.removeComponent(pos);
        }
        var structureCache = getMultiblockState().cache;
        for (BlockPos pos : melted) {
            IMultiPart part = findPartAt(pos);
            if (part != null) {
                part.removedFromController(this);
                getParts().remove(part);
            }
            structureCache.remove(pos.asLong());
            serverLevel.destroyBlock(pos, false);
        }
        updatePartPositions();
    }

    @Nullable
    private IMultiPart findPartAt(BlockPos pos) {
        for (IMultiPart part : getParts()) {
            if (part.self().getBlockPos().equals(pos)) {
                return part;
            }
        }
        return null;
    }

    private float[] getHeightMultipliers() {
        return switch (reactorHeight) {
            case 4 -> new float[] { 1.15f, 1.1f };
            case 5 -> new float[] { 1.3f, 1.2f };
            case 6 -> new float[] { 1.5f, 1.3f };
            case 7 -> new float[] { 1.75f, 1.4f };
            default -> new float[] { 1.0f, 1.0f };
        };
    }

    public void setRunning(boolean running) {
        this.running = running;
        syncDataHolder.markClientSyncFieldDirty("running");
        updateSimulation();
    }

    private void rebuildGrid() {
        Map<BlockPos, ReactorComponent> freshComponents = new LinkedHashMap<>();
        for (IMultiPart part : getParts()) {
            BlockPos pos = part.self().getBlockPos();
            ReactorComponent fresh = createComponentForPart(part);
            if (fresh != null) {
                freshComponents.put(pos, fresh);
            }
        }

        for (var entry : freshComponents.entrySet()) {
            ReactorComponent existing = grid.getComponent(entry.getKey());
            if (existing != null && existing.getType() == entry.getValue().getType()) {
                entry.getValue().setHeat(existing.getHeat());
                entry.getValue().setActive(existing.isActive());
            }
        }

        grid.replaceComponents(freshComponents);
    }

    @Nullable
    private ReactorComponent createComponentForPart(IMultiPart part) {
        if (hasAbility(part, PartAbility.FISSION_FUEL_PORT)) {
            return ReactorComponent.fuelRod(5000, 500);
        } else if (hasAbility(part, PartAbility.FISSION_COOLANT_OUTLET)) {
            return ReactorComponent.coolantChannel(2000, 150);
        } else if (hasAbility(part, PartAbility.FISSION_HEAT_EXCHANGER)) {
            return ReactorComponent.heatExchanger(3000);
        } else if (hasAbility(part, PartAbility.FISSION_NEUTRON_REFLECTOR)) {
            return ReactorComponent.neutronReflector(4000);
        } else if (hasAbility(part, PartAbility.FISSION_MODERATOR)) {
            return ReactorComponent.moderator(2000);
        } else if (hasAbility(part, PartAbility.FISSION_CONTROL_ROD)) {
            return ReactorComponent.controlRod(1500);
        }
        return null;
    }

    private boolean hasAbility(IMultiPart part, PartAbility ability) {
        return ability.isApplicable(part.self().getBlockState().getBlock());
    }


    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        int panelWidth = 176;
        int panelHeight = 120;

        IntSyncValue heatSync = new IntSyncValue(this::getVesselHeat, v -> {});
        IntSyncValue heightSync = new IntSyncValue(this::getReactorHeight, v -> {});
        BooleanSyncValue runningSync = new BooleanSyncValue(this::isRunning, this::setRunning);
        BooleanSyncValue formedSync = new BooleanSyncValue(this::isFormed, v -> {});
        BooleanSyncValue overheatingSync = new BooleanSyncValue(() -> grid.isOverheating(), v -> {});

        syncManager.syncValue("heat", heatSync);
        syncManager.syncValue("height", heightSync);
        syncManager.syncValue("running", runningSync);
        syncManager.syncValue("formed", formedSync);
        syncManager.syncValue("overheating", overheatingSync);

        var panel = GTGuis.createPanel(this, panelWidth, panelHeight);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), panelWidth));
        panel.child(Flow.column()
                .coverChildren().padding(8).top(4).alignX(0.5f).childPadding(2)
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (!formedSync.getBoolValue()) {
                        return Component.translatable("gtceu.multiblock.invalid_structure");
                    }
                    return Component.translatable("gtceu.multiblock.fission.status",
                            runningSync.getBoolValue() ? "ONLINE" : "OFFLINE");
                })))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (!formedSync.getBoolValue()) return Component.empty();
                    int heat = heatSync.getIntValue();
                    float pct = vesselHeatMax > 0 ? (float) heat / vesselHeatMax * 100 : 0;
                    return Component.translatable("gtceu.multiblock.fission.vessel_heat",
                            heat, vesselHeatMax)
                            .append(" (").append(String.format("%.1f%%", pct)).append(")");
                })))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (!formedSync.getBoolValue()) return Component.empty();
                    return Component.translatable("gtceu.multiblock.fission.height",
                            heightSync.getIntValue());
                })))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (overheatingSync.getBoolValue()) {
                        return Component.translatable("gtceu.multiblock.fission.overheating")
                                .withStyle(ChatFormatting.RED);
                    }
                    return Component.empty();
                })))
                .child(new ToggleButton()
                        .value(new BoolValue.Dynamic(runningSync::getBoolValue, runningSync::setBoolValue))
                        .selectedBackground(GTGuiTextures.BUTTON_POWER[1])
                        .background(false, GTGuiTextures.BUTTON_POWER[0])
                        .tooltipBuilder(false, r -> r.addLine(IKey.lang(
                                Component.translatable("gtceu.multiblock.fission.start"))))
                        .tooltipBuilder(true, r -> r.addLine(IKey.lang(
                                Component.translatable("gtceu.multiblock.fission.shutdown"))))));

        return panel;
    }
}
