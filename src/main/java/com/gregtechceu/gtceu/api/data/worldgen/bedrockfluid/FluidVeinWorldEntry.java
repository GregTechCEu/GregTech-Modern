package com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;
import com.gregtechceu.gtceu.utils.memoization.MemoizedSupplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidVeinWorldEntry {

    private final MemoizedSupplier<BedrockFluidDefinition> vein;
    @Nullable
    @Getter
    private String vein_id;
    @Getter
    private int fluidYield;
    @Getter
    private int operationsRemaining;

    public FluidVeinWorldEntry(@Nullable BedrockFluidDefinition vein, int fluidYield, int operationsRemaining) {
        this(GTMemoizer.memoize(() -> vein));
        if (vein != null) {
            ResourceLocation key = GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(vein);
            if (key != null)
                this.vein_id = key.toString();
        }
        this.fluidYield = fluidYield;
        this.operationsRemaining = operationsRemaining;
    }

    private FluidVeinWorldEntry(MemoizedSupplier<BedrockFluidDefinition> vein) {
        this.vein = vein;
    }

    @Nullable
    public BedrockFluidDefinition getVein() {
        return this.vein.get();
    }

    @Nullable
    public BedrockFluidDefinition getDefinition() {
        return this.vein.get();
    }

    @SuppressWarnings("unused")
    public void setOperationsRemaining(int amount) {
        this.operationsRemaining = amount;
    }

    public void decreaseOperations(int amount) {
        operationsRemaining = ConfigHolder.INSTANCE.worldgen.oreVeins.infiniteBedrockOresFluids ? operationsRemaining :
                Math.max(0, operationsRemaining - amount);
    }

    public CompoundTag writeToNBT() {
        var tag = new CompoundTag();
        tag.putInt("fluidYield", fluidYield);
        tag.putInt("operationsRemaining", operationsRemaining);
        if (vein_id != null) {
            tag.putString("vein", vein_id);
        }
        return tag;
    }

    @NotNull
    public static FluidVeinWorldEntry readFromNBT(@NotNull CompoundTag tag) {
        String vein_id;
        MemoizedSupplier<BedrockFluidDefinition> vein;

        if (tag.contains("vein")) {
            vein_id = tag.getString("vein");
            vein = GTMemoizer.memoize(() -> {
                ResourceLocation key = new ResourceLocation(vein_id);
                return GTRegistries.BEDROCK_FLUID_DEFINITIONS.get(key);
            });
        } else {
            vein_id = null;
            vein = GTMemoizer.memoize(() -> null);
        }

        FluidVeinWorldEntry info = new FluidVeinWorldEntry(vein);
        info.vein_id = vein_id;
        info.fluidYield = tag.getInt("fluidYield");
        info.operationsRemaining = tag.getInt("operationsRemaining");
        return info;
    }
}
