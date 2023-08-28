package com.gregtechceu.gtceu.api.data.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
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
public class OreVeinWorldEntry {
    @Nullable
    @Getter
    private GTOreDefinition vein;
    @Getter
    private int oreYield;
    @Getter
    private int operationsRemaining;

    public OreVeinWorldEntry(@Nullable GTOreDefinition vein, int oreYield, int operationsRemaining) {
        this.vein = vein;
        this.oreYield = oreYield;
        this.operationsRemaining = operationsRemaining;
    }

    private OreVeinWorldEntry() {

    }

    public GTOreDefinition getDefinition() {
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
        tag.putInt("oreYield", oreYield);
        tag.putInt("operationsRemaining", operationsRemaining);
        if (vein != null) {
            tag.putString("vein", GTRegistries.ORE_VEINS.getKey(vein).toString());
        }
        return tag;
    }

    @Nonnull
    public static OreVeinWorldEntry readFromNBT(@Nonnull CompoundTag tag) {
        OreVeinWorldEntry info = new OreVeinWorldEntry();
        info.oreYield = tag.getInt("oreYield");
        info.operationsRemaining = tag.getInt("operationsRemaining");

        if (tag.contains("vein")) {
            info.vein = GTRegistries.ORE_VEINS.get(new ResourceLocation(tag.getString("vein")));
        }
        return info;
    }
}
