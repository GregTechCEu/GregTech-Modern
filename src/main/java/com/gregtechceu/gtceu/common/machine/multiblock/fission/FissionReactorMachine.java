package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.ByteArraySyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.StringSyncValue;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_REACTOR_VESSEL;

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

    @SyncToClient
    @Getter
    private int totalHeatGenRate;
    @SyncToClient
    @Getter
    private int totalCoolingRate;
    @SyncToClient
    @Getter
    private int activeFuelRods;
    @SyncToClient
    @Getter
    private int totalFuelRods;
    @SyncToClient
    @Getter
    private int coolantFlowRate;
    @SyncToClient
    @Getter
    private String activeCoolantName = "";

    private final List<long[]> heatmapLayout = new ArrayList<>();

    private final List<NotifiableFluidTank> inletTanks = new ArrayList<>();
    private final List<NotifiableFluidTank> outletTanks = new ArrayList<>();
    private final List<FuelRodPortPartMachine> fuelPorts = new ArrayList<>();
    private final List<FuelRodDrainPartMachine> fuelDrains = new ArrayList<>();

    public FissionReactorMachine(BlockEntityCreationInfo info, int vesselHeatMax) {
        super(info);
        this.vesselHeatMax = vesselHeatMax;
        this.grid = new ReactorGrid(vesselHeatMax);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        detectHeight();
        cachePartReferences();
        rebuildGrid();
        cacheHeatmapLayout();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateSimulation));
        }
    }

    private void cachePartReferences() {
        inletTanks.clear();
        outletTanks.clear();
        fuelPorts.clear();
        fuelDrains.clear();
        for (IMultiPart part : getParts()) {
            if (part instanceof CoolantInletPartMachine inlet) {
                inletTanks.add(inlet.getTank());
            } else if (part instanceof CoolantOutletPartMachine outlet) {
                outletTanks.add(outlet.getTank());
            } else if (part instanceof FuelRodPortPartMachine port) {
                fuelPorts.add(port);
            } else if (part instanceof FuelRodDrainPartMachine drain) {
                fuelDrains.add(drain);
            }
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
        heatmapLayout.clear();
        inletTanks.clear();
        outletTanks.clear();
        fuelPorts.clear();
        fuelDrains.clear();
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

        updateFuelRodActivity();
        updateControlRods();

        int heatGenSum = 0;
        int activeRods = 0;
        int totalRods = 0;
        for (ReactorComponent comp : grid.getAllComponents()) {
            if (comp.getType() == ReactorComponentType.FUEL_ROD) {
                totalRods++;
                if (comp.isActive() && !comp.isDepleted()) {
                    activeRods++;
                    heatGenSum += comp.getEffectiveHeatGeneration();
                }
            }
        }
        totalHeatGenRate = heatGenSum;
        activeFuelRods = activeRods;
        totalFuelRods = totalRods;

        int coolingBudget = 0;
        CoolantDefinition activeCoolant = null;
        for (NotifiableFluidTank inlet : inletTanks) {
            FluidStack cold = inlet.getFluidInTank(0);
            if (!cold.isEmpty()) {
                CoolantDefinition def = CoolantRegistry.getCoolant(cold.getFluid());
                if (def != null) {
                    activeCoolant = def;
                    coolingBudget += cold.getAmount() * def.getHeatCapacity();
                }
            }
        }

        float[] multipliers = getHeightMultipliers();
        int heatAbsorbed = grid.tick(multipliers[0], multipliers[1], coolingBudget, activeCoolant);
        vesselHeat = grid.getVesselHeat();
        totalCoolingRate = heatAbsorbed;

        int drained = 0;
        if (heatAbsorbed > 0 && activeCoolant != null) {
            drained = processCoolant(heatAbsorbed, activeCoolant);
        }
        coolantFlowRate = drained;
        activeCoolantName = activeCoolant != null ?
                new FluidStack(activeCoolant.getColdFluid(), 1).getDisplayName().getString() : "";

        syncDataHolder.markClientSyncFieldDirty("vesselHeat");
        syncDataHolder.markClientSyncFieldDirty("totalHeatGenRate");
        syncDataHolder.markClientSyncFieldDirty("totalCoolingRate");
        syncDataHolder.markClientSyncFieldDirty("activeFuelRods");
        syncDataHolder.markClientSyncFieldDirty("totalFuelRods");
        syncDataHolder.markClientSyncFieldDirty("coolantFlowRate");
        syncDataHolder.markClientSyncFieldDirty("activeCoolantName");

        processMeltdowns();
    }

    private int processCoolant(int heatAbsorbed, CoolantDefinition coolantDef) {
        int hotOutput_mB = heatAbsorbed / coolantDef.getHeatCapacity();
        if (hotOutput_mB <= 0) return 0;

        Fluid coldFluid = coolantDef.getColdFluid();
        Fluid hotFluid = coolantDef.getHotFluid();

        int remaining = hotOutput_mB;
        for (NotifiableFluidTank inlet : inletTanks) {
            if (remaining <= 0) break;
            FluidStack inTank = inlet.getFluidInTank(0);
            if (!inTank.isEmpty() && inTank.getFluid() == coldFluid) {
                int drainable = Math.min(remaining, inTank.getAmount());
                inlet.drain(new FluidStack(coldFluid, drainable), IFluidHandler.FluidAction.EXECUTE);
                remaining -= drainable;
            }
        }
        int actualDrained = hotOutput_mB - remaining;
        if (actualDrained <= 0) return 0;

        FluidStack hotStack = new FluidStack(hotFluid, actualDrained);
        for (NotifiableFluidTank outlet : outletTanks) {
            if (hotStack.isEmpty()) break;
            int filled = outlet.fill(hotStack, IFluidHandler.FluidAction.EXECUTE);
            hotStack.shrink(filled);
        }
        return actualDrained;
    }

    private void updateFuelRodActivity() {
        for (FuelRodPortPartMachine port : fuelPorts) {
            BlockPos pos = port.self().getBlockPos();
            ReactorComponent comp = grid.getComponent(pos);
            if (comp == null || comp.getType() != ReactorComponentType.FUEL_ROD) continue;

            NotifiableItemStackHandler inv = port.getInventory();
            ItemStack fuelStack = inv.getStackInSlot(0);

            if (comp.isDepleted()) {
                FuelRodDefinition def = fuelStack.isEmpty() ? null :
                        FuelRodRegistry.getFuelDefinition(fuelStack.getItem());
                if (def != null) {
                    ItemStack depletedStack = new ItemStack(def.getDepletedItem());
                    if (tryOutputDepleted(depletedStack)) {
                        fuelStack.shrink(1);
                        comp.setActive(false);
                        comp.setTicksAlive(0);
                        comp.setMaxLifetimeTicks(0);
                    }
                } else {
                    comp.setActive(false);
                }
                continue;
            }

            if (fuelStack.isEmpty()) {
                comp.setActive(false);
                continue;
            }

            FuelRodDefinition def = FuelRodRegistry.getFuelDefinition(fuelStack.getItem());
            if (def == null) {
                comp.setActive(false);
                continue;
            }

            if (comp.getMaxLifetimeTicks() <= 0) {
                comp.setBaseHeatGeneration(def.getBaseHeatGeneration());
                comp.setMaxLifetimeTicks(def.getTotalLifetimeTicks());
                comp.setEndOfLifeMultiplier(def.getEndOfLifeHeatMultiplier());
                comp.setTicksAlive(0);
            }
            comp.setActive(true);
        }
    }

    private boolean tryOutputDepleted(ItemStack depletedStack) {
        for (FuelRodDrainPartMachine drain : fuelDrains) {
            NotifiableItemStackHandler inv = drain.getInventory();
            ItemStack existing = inv.getStackInSlot(0);
            if (existing.isEmpty()) {
                inv.setStackInSlot(0, depletedStack.copy());
                return true;
            } else if (ItemStack.isSameItemSameTags(existing, depletedStack) &&
                    existing.getCount() < existing.getMaxStackSize()) {
                        existing.grow(1);
                        return true;
                    }
        }
        return false;
    }

    private void updateControlRods() {
        if (getLevel() == null) return;
        for (IMultiPart part : getParts()) {
            if (hasAbility(part, PartAbility.FISSION_CONTROL_ROD)) {
                BlockPos pos = part.self().getBlockPos();
                ReactorComponent comp = grid.getComponent(pos);
                if (comp != null && comp.getType() == ReactorComponentType.CONTROL_ROD) {
                    int signal = getLevel().getBestNeighborSignal(pos);
                    comp.setInsertionDepth(signal);
                }
            }
        }
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

    private void cacheHeatmapLayout() {
        heatmapLayout.clear();
        BlockPos controllerPos = self().getBlockPos();
        int capstoneY = controllerPos.getY();
        if (getLevel() == null) return;

        var structureCache = getMultiblockState().cache;
        if (structureCache == null) return;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        var longIt = structureCache.longIterator();
        while (longIt.hasNext()) {
            BlockPos pos = BlockPos.of(longIt.nextLong());
            if (pos.getY() != capstoneY) continue;
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        if (minX > maxX) return;

        Set<Long> visited = new java.util.HashSet<>();
        java.util.Deque<BlockPos> queue = new java.util.ArrayDeque<>();
        queue.add(controllerPos);
        visited.add(controllerPos.asLong());

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            for (BlockPos neighbor : List.of(
                    current.north(), current.south(), current.east(), current.west())) {
                long nLong = neighbor.asLong();
                if (visited.contains(nLong)) continue;
                if (neighbor.getX() < minX || neighbor.getX() > maxX) continue;
                if (neighbor.getZ() < minZ || neighbor.getZ() > maxZ) continue;

                boolean vessel = getLevel().getBlockState(neighbor).is(
                        CASING_REACTOR_VESSEL.get());
                visited.add(nLong);
                if (!vessel) {
                    queue.add(neighbor);
                }
            }
        }

        for (long posLong : visited) {
            BlockPos pos = BlockPos.of(posLong);
            int relX = pos.getX() - controllerPos.getX();
            int relZ = pos.getZ() - controllerPos.getZ();

            int baseType;
            if (relX == 0 && relZ == 0) {
                baseType = ReactorComponentType.CONTROLLER.ordinal();
            } else if (getLevel().getBlockState(pos).is(
                    CASING_REACTOR_VESSEL.get())) {
                        baseType = ReactorComponentType.VESSEL.ordinal();
                    } else {
                        baseType = ReactorComponentType.CASING.ordinal();
                    }
            heatmapLayout.add(new long[] { posLong, relX, relZ, baseType });
        }
    }

    byte[] buildHeatmapData() {
        if (heatmapLayout.isEmpty()) return new byte[0];

        byte[] data = new byte[heatmapLayout.size() * 4];
        int idx = 0;
        for (long[] entry : heatmapLayout) {
            BlockPos pos = BlockPos.of(entry[0]);
            int relX = (int) entry[1];
            int relZ = (int) entry[2];
            int baseType = (int) entry[3];

            int type = baseType;
            int heatByte = 0;
            int flags = 1;

            ReactorComponent comp = grid.getComponent(pos);
            if (comp != null) {
                type = comp.getType().ordinal() & 0xF;
                heatByte = (int) (comp.heatPercent() * 255) & 0xFF;
                flags = (comp.isActive() ? 1 : 0) | (comp.isDepleted() ? 2 : 0);
            }

            int encodedX = (relX + 128) & 0xFF;
            int encodedZ = (relZ + 128) & 0xFF;
            int packed = (encodedX << 24) | (encodedZ << 16) | (type << 12) | (heatByte << 4) | flags;
            data[idx++] = (byte) (packed >> 24);
            data[idx++] = (byte) (packed >> 16);
            data[idx++] = (byte) (packed >> 8);
            data[idx++] = (byte) packed;
        }
        return data;
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
        int panelHeight = 280;

        IntSyncValue heatSync = new IntSyncValue(this::getVesselHeat, v -> {});
        IntSyncValue heightSync = new IntSyncValue(this::getReactorHeight, v -> {});
        BooleanSyncValue runningSync = new BooleanSyncValue(this::isRunning, this::setRunning);
        BooleanSyncValue formedSync = new BooleanSyncValue(this::isFormed, v -> {});
        BooleanSyncValue overheatingSync = new BooleanSyncValue(() -> grid.isOverheating(), v -> {});
        IntSyncValue heatGenSync = new IntSyncValue(this::getTotalHeatGenRate, v -> {});
        IntSyncValue coolingSync = new IntSyncValue(this::getTotalCoolingRate, v -> {});
        IntSyncValue activeRodsSync = new IntSyncValue(this::getActiveFuelRods, v -> {});
        IntSyncValue totalRodsSync = new IntSyncValue(this::getTotalFuelRods, v -> {});
        IntSyncValue coolantFlowSync = new IntSyncValue(this::getCoolantFlowRate, v -> {});
        StringSyncValue coolantNameSync = new StringSyncValue(this::getActiveCoolantName);

        syncManager.syncValue("heat", heatSync);
        syncManager.syncValue("height", heightSync);
        syncManager.syncValue("running", runningSync);
        syncManager.syncValue("formed", formedSync);
        syncManager.syncValue("overheating", overheatingSync);
        syncManager.syncValue("heat_gen", heatGenSync);
        syncManager.syncValue("cooling", coolingSync);
        syncManager.syncValue("active_rods", activeRodsSync);
        syncManager.syncValue("total_rods", totalRodsSync);
        syncManager.syncValue("coolant_flow", coolantFlowSync);
        syncManager.syncValue("coolant_name", coolantNameSync);

        ByteArraySyncValue heatmapSync = new ByteArraySyncValue(this::buildHeatmapData, null);
        syncManager.syncValue("heatmap", heatmapSync);

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
                    return Component.translatable("gtceu.multiblock.fission.heat_gen",
                            heatGenSync.getIntValue());
                })))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (!formedSync.getBoolValue()) return Component.empty();
                    return Component.translatable("gtceu.multiblock.fission.cooling",
                            coolingSync.getIntValue());
                })))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (!formedSync.getBoolValue()) return Component.empty();
                    return Component.translatable("gtceu.multiblock.fission.fuel_rods",
                            activeRodsSync.getIntValue(), totalRodsSync.getIntValue());
                })))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (!formedSync.getBoolValue()) return Component.empty();
                    String name = coolantNameSync.getStringValue();
                    if (name == null || name.isEmpty()) {
                        return Component.translatable("gtceu.multiblock.fission.coolant_type", "None");
                    }
                    return Component.translatable("gtceu.multiblock.fission.coolant_flow",
                            coolantFlowSync.getIntValue())
                            .append(" (").append(name).append(")");
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
                .child(new ReactorHeatmapWidget(heatmapSync).size(140, 120))
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
