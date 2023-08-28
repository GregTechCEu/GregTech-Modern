package com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author KilaBash
 * @date 2023/7/11
 * @implNote FluidVeinWorldEntry
 */
public class FluidVeinWorldEntry {
    @Nullable
    @Getter
    private BedrockFluidDefinition vein;
    @Getter
    private int fluidYield;
    @Getter
    private int operationsRemaining;

    public FluidVeinWorldEntry(@Nullable BedrockFluidDefinition vein, int fluidYield, int operationsRemaining) {
        this.vein = vein;
        this.fluidYield = fluidYield;
        this.operationsRemaining = operationsRemaining;
    }

    private FluidVeinWorldEntry() {

    }

    public BedrockFluidDefinition getDefinition() {
        return this.vein;
    }

    @SuppressWarnings("unused")
    public void setOperationsRemaining(int amount) {
        this.operationsRemaining = amount;
    }

    public void decreaseOperations(int amount) {
        operationsRemaining = ConfigHolder.INSTANCE.worldgen.infiniteBedrockOresFluids ? operationsRemaining : Math.max(0, operationsRemaining - amount);
    }

    public CompoundTag writeToNBT() {
        var tag = new CompoundTag();
        tag.putInt("fluidYield", fluidYield);
        tag.putInt("operationsRemaining", operationsRemaining);
        if (vein != null) {
            tag.putString("vein", GTRegistries.BEDROCK_FLUID_DEFINITIONS.getKey(vein).toString());
        }
        return tag;
    }

    @Nonnull
    public static FluidVeinWorldEntry readFromNBT(@Nonnull CompoundTag tag) {
        FluidVeinWorldEntry info = new FluidVeinWorldEntry();
        info.fluidYield = tag.getInt("fluidYield");
        info.operationsRemaining = tag.getInt("operationsRemaining");

        if (tag.contains("vein")) {
            info.vein = GTRegistries.BEDROCK_FLUID_DEFINITIONS.get(new ResourceLocation(tag.getString("vein")));
        }
        return info;
    }
}
