package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ReactorGrid implements INBTSerializable<CompoundTag> {

    @Getter
    private final int vesselHeatMax;
    @Getter
    private int vesselHeat;
    private final Map<BlockPos, ReactorComponent> components = new LinkedHashMap<>();
    private final Map<BlockPos, List<BlockPos>> adjacencyCache = new HashMap<>();

    public ReactorGrid(int vesselHeatMax) {
        this.vesselHeatMax = vesselHeatMax;
    }

    public void addComponent(BlockPos pos, ReactorComponent component) {
        components.put(pos, component);
        adjacencyCache.clear();
    }

    public void clear() {
        components.clear();
        adjacencyCache.clear();
        vesselHeat = 0;
    }

    public ReactorComponent getComponent(BlockPos pos) {
        return components.get(pos);
    }

    public Collection<ReactorComponent> getAllComponents() {
        return components.values();
    }

    public void replaceComponents(Map<BlockPos, ReactorComponent> newComponents) {
        components.clear();
        components.putAll(newComponents);
        adjacencyCache.clear();
    }

    public void removeComponent(BlockPos pos) {
        components.remove(pos);
        adjacencyCache.clear();
    }

    public boolean isVesselCritical() {
        return vesselHeat >= vesselHeatMax;
    }

    public List<BlockPos> getNeighbors(BlockPos pos) {
        return adjacencyCache.computeIfAbsent(pos, p -> {
            List<BlockPos> neighbors = new ArrayList<>(4);
            for (BlockPos candidate : List.of(
                    p.north(), p.south(), p.east(), p.west())) {
                if (components.containsKey(candidate)) {
                    neighbors.add(candidate);
                }
            }
            return neighbors;
        });
    }

    private static final float AMBIENT_BLEED_FRACTION = 0.02f;

    public int[] tick(float heightBurnMultiplier, float heightOutputMultiplier,
                      int coolingBudget, @Nullable CoolantDefinition coolantDef,
                      boolean meltdownState) {
        float reactionMultiplier = meltdownState ? 2.0f : 1.0f;
        float coolantMultiplier = meltdownState ? 4.0f : 1.0f;

        // Phase 1: fuel rods generate heat + age
        int totalHeatGenerated = 0;
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.FUEL_ROD) continue;
            if (!comp.isActive() || comp.isDepleted()) continue;

            comp.tickFuelAge();

            int adjacentRods = countAdjacentOfType(entry.getKey(), ReactorComponentType.FUEL_ROD);
            float adjacentModBonus = sumAdjacentModeratorBonus(entry.getKey());
            int adjacentReflectors = countAdjacentOfType(entry.getKey(), ReactorComponentType.NEUTRON_REFLECTOR);

            float rodMultiplier = 1.0f + adjacentRods * 0.5f;
            float modMultiplier = 1.0f + adjacentModBonus;
            float reflectorMultiplier = 1.0f + adjacentReflectors * 0.15f;
            float totalMultiplier = rodMultiplier * modMultiplier * reflectorMultiplier * heightBurnMultiplier *
                    reactionMultiplier;

            int heatGenerated = (int) (comp.getEffectiveHeatGeneration() * totalMultiplier);
            comp.addHeat(heatGenerated);
            totalHeatGenerated += heatGenerated;
        }

        // Phase 1b: ambient vessel bleed
        vesselHeat += (int) (totalHeatGenerated * AMBIENT_BLEED_FRACTION);

        // Phase 2: natural heat diffusion
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            for (BlockPos n : getNeighbors(entry.getKey())) {
                var neighbor = components.get(n);
                int diff = comp.getHeat() - neighbor.getHeat();
                if (diff > 0) {
                    int transfer = Math.max(1, (int) (diff * 0.1f));
                    comp.removeHeat(transfer);
                    neighbor.addHeat(transfer);
                }
            }
        }

        // Phase 3: heat exchangers equalize among neighbors (efficiency-scaled)
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.HEAT_EXCHANGER) continue;

            List<BlockPos> neighbors = getNeighbors(entry.getKey());
            if (neighbors.isEmpty()) continue;

            float efficiency = comp.getThermalEfficiency();
            int totalHeat = comp.getHeat();
            int count = 1;
            for (BlockPos n : neighbors) {
                totalHeat += components.get(n).getHeat();
                count++;
            }
            int avg = totalHeat / count;

            int selfDelta = (int) ((avg - comp.getHeat()) * efficiency);
            comp.addHeat(selfDelta);
            for (BlockPos n : neighbors) {
                var neighbor = components.get(n);
                int neighborDelta = (int) ((avg - neighbor.getHeat()) * efficiency);
                neighbor.addHeat(neighborDelta);
            }
        }

        // Phase 4: coolant channels absorb heat (efficiency + budget limited)
        int[] coolingResult = coolantCooling(
                (int) (coolingBudget * coolantMultiplier), coolantDef);
        int totalHeatAbsorbed = coolingResult[0];

        // Phase 5: control rods suppress neighbors
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.CONTROL_ROD) continue;
            if (!comp.isActive() || comp.getInsertionDepth() <= 0) continue;

            float suppressionFraction = comp.getInsertionDepth() / 15.0f;
            if (meltdownState) suppressionFraction *= 0.5f;
            List<BlockPos> neighbors = getNeighbors(entry.getKey());
            for (BlockPos n : neighbors) {
                var neighbor = components.get(n);
                if (neighbor.getType() == ReactorComponentType.FUEL_ROD) {
                    int suppression = (int) (neighbor.getBaseHeatGeneration() * suppressionFraction * 0.5f);
                    neighbor.removeHeat(suppression);
                }
            }
        }

        // Phase 6: spillover — excess component heat → vessel
        for (var comp : components.values()) {
            int overflow = comp.getHeat() - comp.getMaxHeat();
            if (overflow > 0) {
                comp.setHeat(comp.getMaxHeat());
                vesselHeat += overflow;
            }
        }

        // Phase 7: proportional passive vessel cooling
        if (vesselHeat > 0) {
            vesselHeat = Math.max(0, vesselHeat - Math.max(1, vesselHeat / 200));
        }
        vesselHeat = Math.min(vesselHeat, vesselHeatMax);

        return coolingResult;
    }

    private static final float REFERENCE_HEAT_CAPACITY = 10.0f;

    private int[] coolantCooling(int coolingBudget, @Nullable CoolantDefinition coolantDef) {
        if (coolingBudget <= 0) return new int[] { 0, 0 };

        int channelCount = 0;
        for (var comp : components.values()) {
            if (comp.getType() == ReactorComponentType.COOLANT_CHANNEL && comp.isActive()) {
                channelCount++;
            }
        }
        if (channelCount == 0) return new int[] { 0, 0 };
        int budgetPerChannel = coolingBudget / channelCount;

        float coolantPotency = coolantDef != null ? coolantDef.getHeatCapacity() / REFERENCE_HEAT_CAPACITY : 1.0f;

        int totalAbsorbed = 0;
        int totalCapacity = 0;
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.COOLANT_CHANNEL) continue;
            if (!comp.isActive()) continue;
            int channelCap = (int) (comp.getBaseCoolingRate() * coolantPotency);
            totalCapacity += channelCap;
            int effectiveBudget = Math.min(budgetPerChannel, channelCap);
            float channelEfficiency = comp.getThermalEfficiency();
            if (coolantDef != null) {
                channelEfficiency *= coolantDef.getEfficiency(comp.getHeat(), comp.getMaxHeat());
            }
            effectiveBudget = (int) (effectiveBudget * channelEfficiency);
            int selfAbsorb = Math.min(comp.getHeat(), effectiveBudget / 2);
            comp.removeHeat(selfAbsorb);
            int absorbed = selfAbsorb;
            List<BlockPos> neighbors = getNeighbors(entry.getKey());
            if (!neighbors.isEmpty()) {
                int neighborBudget = effectiveBudget - selfAbsorb;
                int perNeighbor = neighborBudget / neighbors.size();
                for (BlockPos n : neighbors) {
                    var neighbor = components.get(n);
                    int removed = Math.min(neighbor.getHeat(), perNeighbor);
                    neighbor.removeHeat(removed);
                    absorbed += removed;
                }
            }
            totalAbsorbed += absorbed;
        }

        return new int[] { totalAbsorbed, totalCapacity };
    }

    public float getVesselHeatPercent() {
        return vesselHeatMax > 0 ? (float) vesselHeat / vesselHeatMax : 0;
    }

    public boolean isMeltdownState() {
        return getVesselHeatPercent() > 0.9f;
    }

    private int countAdjacentOfType(BlockPos pos, ReactorComponentType type) {
        int count = 0;
        for (BlockPos n : getNeighbors(pos)) {
            if (components.get(n).getType() == type) count++;
        }
        return count;
    }

    private float sumAdjacentModeratorBonus(BlockPos pos) {
        float bonus = 0;
        for (BlockPos n : getNeighbors(pos)) {
            var neighbor = components.get(n);
            if (neighbor.getType() == ReactorComponentType.MODERATOR) {
                bonus += 0.5f * neighbor.getThermalEfficiency();
            }
        }
        return bonus;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("vesselHeat", vesselHeat);
        ListTag componentList = new ListTag();
        for (var entry : components.entrySet()) {
            CompoundTag compTag = new CompoundTag();
            compTag.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            compTag.put("data", entry.getValue().serializeToNbt());
            componentList.add(compTag);
        }
        tag.put("components", componentList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        vesselHeat = tag.getInt("vesselHeat");
        components.clear();
        adjacencyCache.clear();
        ListTag componentList = tag.getList("components", Tag.TAG_COMPOUND);
        for (int i = 0; i < componentList.size(); i++) {
            CompoundTag compTag = componentList.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(compTag.getCompound("pos"));
            ReactorComponent comp = ReactorComponent.deserializeFromNbt(compTag.getCompound("data"));
            components.put(pos, comp);
        }
    }
}
