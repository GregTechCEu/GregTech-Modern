package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
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
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.machines.GTFissionMachines;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_REACTOR;
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
    private int structureBottomY;

    @SyncToClient
    @Getter
    private int totalHeatGenRate;
    @SyncToClient
    @Getter
    private int totalCoolingRate;
    @SyncToClient
    @Getter
    private int totalCoolingCapacity;
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
    @SaveField
    @SyncToClient
    @Getter
    private boolean meltdownState;
    @SyncToClient
    @Getter
    private float powerOutputMultiplier;
    @SyncToClient
    @Getter
    private float coolingEfficiency;

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
    public void onLoad() {
        super.onLoad();
        if (isFormed() && getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, () -> {
                if (isFormed()) {
                    detectHeight();
                    cachePartReferences();
                    refreshCodeStats();
                    cacheHeatmapLayout();
                    updateSimulation();
                }
            }));
        }
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
        for (long packed : getMultiblockState().cache) {
            int y = BlockPos.getY(packed);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        structureBottomY = minY;
        reactorHeight = minY < maxY ? (maxY - minY - 1) : 3;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        grid.clear();
        running = false;
        meltdownState = false;
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

        boolean wasMeltdown = meltdownState;
        meltdownState = grid.isMeltdownState();

        if (meltdownState && !wasMeltdown) {
            triggerScram();
        }

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
        int[] coolingResult = grid.tick(multipliers[0], multipliers[1],
                coolingBudget, activeCoolant, meltdownState);
        vesselHeat = grid.getVesselHeat();
        totalCoolingRate = coolingResult[0];
        totalCoolingCapacity = coolingResult[1];

        float vesselHeatPct = vesselHeatMax > 0 ? (float) vesselHeat / vesselHeatMax : 0;
        powerOutputMultiplier = coolingResult[2] / 1000.0f;
        coolingEfficiency = ReactorGrid.getCoolingEfficiencyMultiplier(vesselHeatPct);

        int scaledHeatAbsorbed = (int) (totalCoolingRate * powerOutputMultiplier);
        int drained = 0;
        if (scaledHeatAbsorbed > 0 && activeCoolant != null) {
            drained = processCoolant(scaledHeatAbsorbed, activeCoolant);
        }
        coolantFlowRate = drained;
        activeCoolantName = activeCoolant != null ?
                new FluidStack(activeCoolant.getColdFluid(), 1).getDisplayName().getString() : "";

        syncDataHolder.markClientSyncFieldDirty("vesselHeat");
        syncDataHolder.markClientSyncFieldDirty("totalHeatGenRate");
        syncDataHolder.markClientSyncFieldDirty("totalCoolingRate");
        syncDataHolder.markClientSyncFieldDirty("totalCoolingCapacity");
        syncDataHolder.markClientSyncFieldDirty("activeFuelRods");
        syncDataHolder.markClientSyncFieldDirty("totalFuelRods");
        syncDataHolder.markClientSyncFieldDirty("coolantFlowRate");
        syncDataHolder.markClientSyncFieldDirty("activeCoolantName");
        syncDataHolder.markClientSyncFieldDirty("meltdownState");
        syncDataHolder.markClientSyncFieldDirty("powerOutputMultiplier");
        syncDataHolder.markClientSyncFieldDirty("coolingEfficiency");

        if (grid.isVesselCritical() && !meltdownState) {
            meltdownState = true;
            running = false;
            syncDataHolder.markClientSyncFieldDirty("running");
            syncDataHolder.markClientSyncFieldDirty("meltdownState");
            //TODO: FAILURE NOTICE WHEN MASS STATE TESTING :))))))
            GTCEu.LOGGER.warn("[FISSION] Reactor MELTED at {} — vessel heat {}/{}", self().getBlockPos(),
                    vesselHeat, vesselHeatMax);
            // TODO: processVesselFailure() — disabled during balance tuning
        }
    }

    private void triggerScram() {
        for (ReactorComponent comp : grid.getAllComponents()) {
            if (comp.getType() == ReactorComponentType.CONTROL_ROD) {
                comp.setInsertionDepth(15);
            }
        }
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
                inlet.drainInternal(new FluidStack(coldFluid, drainable), IFluidHandler.FluidAction.EXECUTE);
                remaining -= drainable;
            }
        }
        int actualDrained = hotOutput_mB - remaining;
        if (actualDrained <= 0) return 0;

        FluidStack hotStack = new FluidStack(hotFluid, actualDrained);
        for (NotifiableFluidTank outlet : outletTanks) {
            if (hotStack.isEmpty()) break;
            int filled = outlet.fillInternal(hotStack, IFluidHandler.FluidAction.EXECUTE);
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
                if (FuelRodItem.hasLifetimeData(fuelStack)) {
                    comp.setTicksAlive(FuelRodItem.getTicksAlive(fuelStack));
                } else {
                    comp.setTicksAlive(0);
                }
            }
            comp.setActive(true);

            if (comp.getTicksAlive() % 20 == 0) {
                FuelRodItem.setLifetimeData(fuelStack, comp.getTicksAlive(), comp.getMaxLifetimeTicks());
            }
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

    private void processVesselFailure() {
        if (!(getLevel() instanceof ServerLevel serverLevel)) return;

        running = false;
        syncDataHolder.markClientSyncFieldDirty("running");

        var structureCache = getMultiblockState().cache;
        List<BlockPos> allPositions = new ArrayList<>();
        var longIt = structureCache.longIterator();
        while (longIt.hasNext()) {
            allPositions.add(BlockPos.of(longIt.nextLong()));
        }

        for (IMultiPart part : new ArrayList<>(getParts())) {
            part.removedFromController(this);
        }
        getParts().clear();
        grid.clear();

        for (BlockPos pos : allPositions) {
            serverLevel.destroyBlock(pos, false);
            // TODO replace with corium scrap block placement
        }
        structureCache.clear();

        BlockPos center = self().getBlockPos();
        AABB area = new AABB(center).inflate(32);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, area)) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 600, 2));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 1));
        }
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

    private void refreshCodeStats() {
        for (IMultiPart part : getParts()) {
            BlockPos pos = part.self().getBlockPos();
            ReactorComponent existing = grid.getComponent(pos);
            ReactorComponent fresh = createComponentForPart(part);
            if (existing != null && fresh != null && existing.getType() == fresh.getType()) {
                existing.applyBaseStats(fresh.getMaxHeat(), fresh.getBaseHeatGeneration(),
                        fresh.getBaseCoolingRate());
            }
        }
    }

    @Nullable
    private ReactorComponent createComponentForPart(IMultiPart part) {
        if (hasAbility(part, PartAbility.FISSION_FUEL_PORT)) {
            return ReactorComponent.fuelRod(5000, 300);
        } else if (hasAbility(part, PartAbility.FISSION_COOLANT_OUTLET)) {
            return ReactorComponent.coolantChannel(2000, 400);
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

    @Nullable
    private MachineDefinition getBottomCounterpart(IMultiPart topPart) {
        if (hasAbility(topPart, PartAbility.FISSION_FUEL_PORT)) {
            return GTFissionMachines.FISSION_FUEL_ROD_DRAIN;
        } else if (hasAbility(topPart, PartAbility.FISSION_COOLANT_OUTLET)) {
            return GTFissionMachines.FISSION_COOLANT_INLET;
        }
        return null;
    }

    private Map<BlockPos, MachineDefinition> computeBottomLayerRequirements() {
        Map<BlockPos, MachineDefinition> requirements = new LinkedHashMap<>();
        if (!isFormed() || getLevel() == null) return requirements;

        int topY = self().getBlockPos().getY();
        int bottomY = structureBottomY;
        if (bottomY >= topY) return requirements;

        for (IMultiPart part : getParts()) {
            BlockPos partPos = part.self().getBlockPos();
            if (partPos.getY() != topY) continue;

            MachineDefinition bottomDef = getBottomCounterpart(part);
            if (bottomDef == null) continue;

            BlockPos bottomPos = new BlockPos(partPos.getX(), bottomY, partPos.getZ());
            requirements.put(bottomPos, bottomDef);
        }
        return requirements;
    }

    private String applyBottomLayer(Player player) {
        if (!isFormed()) {
            return "gtceu.multiblock.fission.bottom.not_formed";
        }
        if (running) {
            return "gtceu.multiblock.fission.bottom.running";
        }
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return "";
        }

        Map<BlockPos, MachineDefinition> requirements = computeBottomLayerRequirements();
        if (requirements.isEmpty()) {
            return "gtceu.multiblock.fission.bottom.nothing_needed";
        }

        Map<BlockPos, MachineDefinition> toSwap = new HashMap<>();
        Block casingBlock = CASING_REACTOR.get();
        for (var entry : requirements.entrySet()) {
            BlockState currentState = serverLevel.getBlockState(entry.getKey());
            if (currentState.is(casingBlock)) {
                toSwap.put(entry.getKey(), entry.getValue());
            }
        }
        if (toSwap.isEmpty()) {
            return "gtceu.multiblock.fission.bottom.already_configured";
        }

        boolean creative = player.isCreative();
        if (!creative) {
            Map<Item, Integer> needed = new HashMap<>();
            for (MachineDefinition def : toSwap.values()) {
                needed.merge(def.getItem(), 1, Integer::sum);
            }
            for (var entry : needed.entrySet()) {
                if (countItemInInventory(player.getInventory(), entry.getKey()) < entry.getValue()) {
                    return "gtceu.multiblock.fission.bottom.missing_items";
                }
            }

            for (var entry : needed.entrySet()) {
                removeItemsFromInventory(player.getInventory(), entry.getKey(), entry.getValue());
            }
        }

        int casingsReturned = 0;
        for (var entry : toSwap.entrySet()) {
            BlockState machineState = entry.getValue().getBlock().defaultBlockState()
                    .setValue(BlockStateProperties.FACING, Direction.DOWN);
            serverLevel.setBlockAndUpdate(entry.getKey(), machineState);
            casingsReturned++;
        }

        if (!creative && casingsReturned > 0) {
            ItemStack casingReturn = new ItemStack(casingBlock.asItem(), casingsReturned);
            if (!player.getInventory().add(casingReturn)) {
                player.drop(casingReturn, false);
            }
        }

        scheduleStructureRecheck();
        return "gtceu.multiblock.fission.bottom.applied";
    }

    private String revertBottomLayer(Player player) {
        if (!isFormed()) {
            return "gtceu.multiblock.fission.bottom.not_formed";
        }
        if (running) {
            return "gtceu.multiblock.fission.bottom.running";
        }
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            return "";
        }

        int topY = self().getBlockPos().getY();
        int bottomY = structureBottomY;
        if (bottomY >= topY) return "";

        Block casingBlock = CASING_REACTOR.get();
        Block drainBlock = GTFissionMachines.FISSION_FUEL_ROD_DRAIN.getBlock();
        Block inletBlock = GTFissionMachines.FISSION_COOLANT_INLET.getBlock();

        Map<BlockPos, Block> toRevert = new HashMap<>();
        for (long packed : getMultiblockState().cache) {
            if (BlockPos.getY(packed) != bottomY) continue;
            BlockPos pos = BlockPos.of(packed);
            Block block = serverLevel.getBlockState(pos).getBlock();
            if (block == drainBlock || block == inletBlock) {
                toRevert.put(pos, block);
            }
        }

        if (toRevert.isEmpty()) {
            return "gtceu.multiblock.fission.bottom.nothing_to_revert";
        }

        boolean creative = player.isCreative();
        if (!creative) {
            int casingsNeeded = toRevert.size();
            if (countItemInInventory(player.getInventory(), casingBlock.asItem()) < casingsNeeded) {
                return "gtceu.multiblock.fission.bottom.missing_casings";
            }
            removeItemsFromInventory(player.getInventory(), casingBlock.asItem(), casingsNeeded);
        }

        Map<Item, Integer> returned = new HashMap<>();
        for (var entry : toRevert.entrySet()) {
            Item machineItem = entry.getValue().asItem();
            returned.merge(machineItem, 1, Integer::sum);
            serverLevel.setBlockAndUpdate(entry.getKey(), casingBlock.defaultBlockState());
        }

        if (!creative) {
            for (var entry : returned.entrySet()) {
                ItemStack returnStack = new ItemStack(entry.getKey(), entry.getValue());
                if (!player.getInventory().add(returnStack)) {
                    player.drop(returnStack, false);
                }
            }
        }

        scheduleStructureRecheck();
        return "gtceu.multiblock.fission.bottom.reverted";
    }

    private static int countItemInInventory(Inventory inventory, Item item) {
        int count = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItemsFromInventory(Inventory inventory, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(item)) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
    }

    private void scheduleStructureRecheck() {
        onStructureInvalid();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, () -> {
                if (checkPatternWithLock()) {
                    setFlipped(getMultiblockState().isNeededFlip());
                    onStructureFormed();
                }
            }));
        }
    }

    private String bottomLayerStatusText() {
        if (!isFormed()) return "";
        Map<BlockPos, MachineDefinition> requirements = computeBottomLayerRequirements();
        if (requirements.isEmpty()) return "";

        int configured = 0;
        int total = requirements.size();
        if (getLevel() != null) {
            Block casingBlock = CASING_REACTOR.get();
            for (BlockPos pos : requirements.keySet()) {
                if (!getLevel().getBlockState(pos).is(casingBlock)) {
                    configured++;
                }
            }
        }
        return configured + "/" + total;
    }

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        int panelWidth = 320;
        int panelHeight = 230;

        IntSyncValue heatSync = new IntSyncValue(this::getVesselHeat, v -> {});
        IntSyncValue heightSync = new IntSyncValue(this::getReactorHeight, v -> {});
        BooleanSyncValue runningSync = new BooleanSyncValue(this::isRunning, this::setRunning);
        BooleanSyncValue formedSync = new BooleanSyncValue(this::isFormed, v -> {});
        BooleanSyncValue meltdownSync = new BooleanSyncValue(this::isMeltdownState, v -> {});
        IntSyncValue heatGenSync = new IntSyncValue(this::getTotalHeatGenRate, v -> {});
        IntSyncValue coolingSync = new IntSyncValue(this::getTotalCoolingRate, v -> {});
        IntSyncValue coolingCapSync = new IntSyncValue(this::getTotalCoolingCapacity, v -> {});
        IntSyncValue activeRodsSync = new IntSyncValue(this::getActiveFuelRods, v -> {});
        IntSyncValue totalRodsSync = new IntSyncValue(this::getTotalFuelRods, v -> {});
        IntSyncValue coolantFlowSync = new IntSyncValue(this::getCoolantFlowRate, v -> {});
        StringSyncValue coolantNameSync = new StringSyncValue(this::getActiveCoolantName);
        IntSyncValue outputMultSync = new IntSyncValue(
                () -> (int) (getPowerOutputMultiplier() * 1000), v -> {});
        IntSyncValue coolingEffSync = new IntSyncValue(
                () -> (int) (getCoolingEfficiency() * 1000), v -> {});
        StringSyncValue bottomStatusSync = new StringSyncValue(this::bottomLayerStatusText);

        syncManager.syncValue("heat", heatSync);
        syncManager.syncValue("height", heightSync);
        syncManager.syncValue("running", runningSync);
        syncManager.syncValue("formed", formedSync);
        syncManager.syncValue("meltdown", meltdownSync);
        syncManager.syncValue("heat_gen", heatGenSync);
        syncManager.syncValue("cooling", coolingSync);
        syncManager.syncValue("cooling_cap", coolingCapSync);
        syncManager.syncValue("active_rods", activeRodsSync);
        syncManager.syncValue("total_rods", totalRodsSync);
        syncManager.syncValue("coolant_flow", coolantFlowSync);
        syncManager.syncValue("coolant_name", coolantNameSync);
        syncManager.syncValue("output_mult", outputMultSync);
        syncManager.syncValue("cooling_eff", coolingEffSync);
        syncManager.syncValue("bottom_status", bottomStatusSync);

        ByteArraySyncValue heatmapSync = new ByteArraySyncValue(this::buildHeatmapData, null);
        syncManager.syncValue("heatmap", heatmapSync);

        final String[] bottomResultMsg = { "" };
        StringSyncValue bottomResultSync = new StringSyncValue(
                () -> bottomResultMsg[0], v -> bottomResultMsg[0] = v);
        syncManager.syncValue("bottom_result", bottomResultSync);

        syncManager.registerServerSyncedAction("apply_bottom", packet -> {
            Player player = syncManager.getPlayer();
            bottomResultMsg[0] = applyBottomLayer(player);
        });
        syncManager.registerServerSyncedAction("revert_bottom", packet -> {
            Player player = syncManager.getPlayer();
            bottomResultMsg[0] = revertBottomLayer(player);
        });

        var panel = GTGuis.createPanel(this, panelWidth, panelHeight);
        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), panelWidth));

        int statsWidth = panelWidth - 14 - 110 - 6;
        Supplier<Boolean> formed = formedSync::getBoolValue;

        var statsColumn = Flow.column().width(statsWidth).coverChildrenHeight()
                .padding(3, 3, 4, 3)
                .background(GTGuiTextures.BACKGROUND_INVERSE)
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (!formed.get()) {
                        return Component.translatable("gtceu.multiblock.invalid_structure");
                    }
                    return Component
                            .translatable(runningSync.getBoolValue() ? "gtceu.multiblock.fission.status.online" :
                                    "gtceu.multiblock.fission.status.offline")
                            .withStyle(runningSync.getBoolValue() ? ChatFormatting.GREEN : ChatFormatting.BLACK);
                })))
                .child(statLine("gtceu.multiblock.fission.label.vessel_heat", () -> {
                    int heat = heatSync.getIntValue();
                    float pct = vesselHeatMax > 0 ? (float) heat / vesselHeatMax * 100 : 0;
                    return Component.translatable("gtceu.multiblock.fission.value.vessel_heat",
                            heat, vesselHeatMax, String.format("%.1f%%", pct));
                }, formed))
                .child(statLine("gtceu.multiblock.fission.label.heat_gen", () -> Component.translatable(
                        "gtceu.multiblock.fission.value.heat_gen", heatGenSync.getIntValue()), formed))
                .child(statLine("gtceu.multiblock.fission.label.cooling", () -> Component.translatable(
                        "gtceu.multiblock.fission.value.cooling",
                        coolingSync.getIntValue(), coolingCapSync.getIntValue()), formed))
                .child(statLine("gtceu.multiblock.fission.label.fuel_rods", () -> Component.translatable(
                        "gtceu.multiblock.fission.value.fuel_rods",
                        activeRodsSync.getIntValue(), totalRodsSync.getIntValue()), formed))
                .child(statLine("gtceu.multiblock.fission.label.coolant", () -> {
                    String name = coolantNameSync.getStringValue();
                    if (name == null || name.isEmpty())
                        return Component.translatable("gtceu.multiblock.fission.value.coolant.none");
                    return Component.translatable("gtceu.multiblock.fission.value.coolant",
                            coolantFlowSync.getIntValue(), name);
                }, formed))
                .child(statLine("gtceu.multiblock.fission.label.output_mult", () -> Component.translatable(
                        "gtceu.multiblock.fission.value.output_mult",
                        String.format("%.0f%%", outputMultSync.getIntValue() / 10.0f)), formed))
                .child(statLine("gtceu.multiblock.fission.label.cooling_eff", () -> Component.translatable(
                        "gtceu.multiblock.fission.value.cooling_eff",
                        String.format("%.0f%%", coolingEffSync.getIntValue() / 10.0f)), formed))
                .child(new TextWidget<>(IKey.dynamic(() -> {
                    if (meltdownSync.getBoolValue()) {
                        return Component.translatable("gtceu.multiblock.fission.meltdown_warning")
                                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
                    }
                    float pct = vesselHeatMax > 0 ? (float) heatSync.getIntValue() / vesselHeatMax : 0;
                    if (pct > 0.75f) {
                        return Component.translatable("gtceu.multiblock.fission.overheating")
                                .withStyle(ChatFormatting.RED);
                    }
                    return Component.empty();
                })));

        var powerButton = new ToggleButton()
                .value(new BoolValue.Dynamic(runningSync::getBoolValue, runningSync::setBoolValue))
                .selectedBackground(GTGuiTextures.BUTTON_POWER[1])
                .background(false, GTGuiTextures.BUTTON_POWER[0])
                .tooltipBuilder(false, r -> r.addLine(IKey.lang(
                        Component.translatable("gtceu.multiblock.fission.start"))))
                .tooltipBuilder(true, r -> r.addLine(IKey.lang(
                        Component.translatable("gtceu.multiblock.fission.shutdown"))));

        int contentWidth = panelWidth - 14;
        panel.child(Flow.column().width(contentWidth).coverChildrenHeight()
                .left(7).top(4).childPadding(4)
                .child(Flow.row().coverChildren().childPadding(4)
                        .child(Flow.column().coverChildren().padding(3)
                                .background(GTGuiTextures.MUI_DISPLAY)
                                .child(new ReactorHeatmapWidget(heatmapSync).size(110, 101)))
                        .child(statsColumn))
                .child(Flow.column().widthRel(1.0f).coverChildrenHeight()
                        .childPadding(2)
                        .child(new TextWidget<>(IKey.dynamic(() -> {
                            if (!formedSync.getBoolValue()) return Component.empty();
                            String status = bottomStatusSync.getStringValue();
                            if (status == null || status.isEmpty()) return Component.empty();
                            return Component.translatable(
                                    "gtceu.multiblock.fission.bottom.status", status)
                                    .withStyle(ChatFormatting.BLACK);
                        })))
                        .child(Flow.row().coverChildren().childPadding(4).alignX(0.5f)
                                .child(powerButton)
                                .child(new ButtonWidget<>()
                                        .size(80, 16)
                                        .overlay(IKey.lang(Component.translatable(
                                                "gtceu.multiblock.fission.bottom.apply")))
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            syncManager.callSyncedAction("apply_bottom");
                                            return true;
                                        }))
                                .child(new ButtonWidget<>()
                                        .size(80, 16)
                                        .overlay(IKey.lang(Component.translatable(
                                                "gtceu.multiblock.fission.bottom.revert")))
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            syncManager.callSyncedAction("revert_bottom");
                                            return true;
                                        })))
                        .child(new TextWidget<>(IKey.dynamic(() -> {
                            String msg = bottomResultSync.getStringValue();
                            if (msg == null || msg.isEmpty()) return Component.empty();
                            return Component.translatable(msg).withStyle(ChatFormatting.YELLOW);
                        })))));

        int invLeft = (panelWidth - 162) / 2;
        panel.child(SlotGroupWidget.playerInventory(false).left(invLeft).bottom(7));

        return panel;
    }

    private static TextWidget<?> statLine(String labelKey, Supplier<Component> value,
                                          Supplier<Boolean> formed) {
        return new TextWidget<>(IKey.dynamic(() -> {
            if (!formed.get()) return Component.empty();
            return Component.translatable(labelKey).append(": ").append(value.get())
                    .withStyle(ChatFormatting.BLACK);
        }));
    }
}
