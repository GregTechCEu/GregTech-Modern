package com.gregtechceu.gtceu.common.machine.multiblock.fission;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import lombok.Getter;

import java.util.*;

/**
 * 2D heat simulation grid for the fission reactor.
 * Each cell represents a column in the reactor, identified by its top-layer BlockPos informally known as a 'CAPSTONE'.
 */
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

    public Set<BlockPos> collectMeltdowns() {
        Set<BlockPos> melted = new HashSet<>();
        for (var entry : components.entrySet()) {
            if (entry.getValue().getHeat() >= entry.getValue().getMaxHeat()) {
                melted.add(entry.getKey());
            }
        }
        return melted;
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

    /**
     * Run one simulation tick. It's the Controllers serverTick() :P
     */
    public void tick(float heightBurnMultiplier, float heightOutputMultiplier) {
        // Phase 1: fuel rods generate heat
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.FUEL_ROD) continue;
            if (!comp.isActive()) continue;

            int adjacentRods = countAdjacentOfType(entry.getKey(), ReactorComponentType.FUEL_ROD);
            int adjacentModerators = countAdjacentOfType(entry.getKey(), ReactorComponentType.MODERATOR);
            int adjacentReflectors = countAdjacentOfType(entry.getKey(), ReactorComponentType.NEUTRON_REFLECTOR);

            float rodMultiplier = 1.0f + adjacentRods * 0.5f;
            float modMultiplier = 1.0f + adjacentModerators * 0.5f;
            float reflectorMultiplier = 1.0f + adjacentReflectors * 0.15f;
            float totalMultiplier = rodMultiplier * modMultiplier * reflectorMultiplier * heightBurnMultiplier;

            int heatGenerated = (int) (comp.getBaseHeatGeneration() * totalMultiplier);
            comp.addHeat(heatGenerated);
        }

        // Phase 2: natural heat diffusion — all adjacent components bleed heat toward equilibrium
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

        // Phase 3: heat exchangers equalize among neighbors
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.HEAT_EXCHANGER) continue;

            List<BlockPos> neighbors = getNeighbors(entry.getKey());
            if (neighbors.isEmpty()) continue;

            int totalHeat = comp.getHeat();
            int count = 1;
            for (BlockPos n : neighbors) {
                totalHeat += components.get(n).getHeat();
                count++;
            }
            int avg = totalHeat / count;

            comp.setHeat(avg);
            for (BlockPos n : neighbors) {
                components.get(n).setHeat(avg);
            }
        }

        // Phase 4: coolant channels remove heat
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.COOLANT_CHANNEL) continue;
            if (!comp.isActive()) continue;

            int coolingRate = comp.getBaseCoolingRate();
            comp.removeHeat(coolingRate / 2);
            List<BlockPos> neighbors = getNeighbors(entry.getKey());
            int perNeighbor = neighbors.isEmpty() ? 0 : (coolingRate / 2) / neighbors.size();
            for (BlockPos n : neighbors) {
                components.get(n).removeHeat(perNeighbor);
            }
        }

        // Phase 5: control rods suppress neighbors
        for (var entry : components.entrySet()) {
            var comp = entry.getValue();
            if (comp.getType() != ReactorComponentType.CONTROL_ROD) continue;
            if (!comp.isActive()) continue;

            List<BlockPos> neighbors = getNeighbors(entry.getKey());
            for (BlockPos n : neighbors) {
                var neighbor = components.get(n);
                if (neighbor.getType() == ReactorComponentType.FUEL_ROD) {
                    neighbor.removeHeat(neighbor.getBaseHeatGeneration() / 2);
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

        // Phase 7: passive vessel cooling
        if (vesselHeat > 0) {
            vesselHeat = Math.max(0, vesselHeat - 1);
        }

        vesselHeat = Math.min(vesselHeat, vesselHeatMax);
    }

    public float getVesselHeatPercent() {
        return vesselHeatMax > 0 ? (float) vesselHeat / vesselHeatMax : 0;
    }

    public boolean isOverheating() {
        return getVesselHeatPercent() > 0.75f;
    }

    private int countAdjacentOfType(BlockPos pos, ReactorComponentType type) {
        int count = 0;
        for (BlockPos n : getNeighbors(pos)) {
            if (components.get(n).getType() == type) count++;
        }
        return count;
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
