package com.gregtechceu.gtceu.api.data.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OreVeinWorldEntry {

    @Nullable
    @Getter
    private BedrockOreDefinition definition;
    @Getter
    private int oreYield;
    @Getter
    private int operationsRemaining;

    public OreVeinWorldEntry(@Nullable BedrockOreDefinition vein, int oreYield, int operationsRemaining) {
        this.definition = vein;
        this.oreYield = oreYield;
        this.operationsRemaining = operationsRemaining;
    }

    private OreVeinWorldEntry() {}

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
        tag.putInt("oreYield", oreYield);
        tag.putInt("operationsRemaining", operationsRemaining);
        if (definition != null && GTRegistries.BEDROCK_ORE_DEFINITIONS.getKey(definition) != null) {
            tag.putString("vein", GTRegistries.BEDROCK_ORE_DEFINITIONS.getKey(definition).toString());
        }
        return tag;
    }

    @NotNull
    public static OreVeinWorldEntry readFromNBT(@NotNull CompoundTag tag) {
        OreVeinWorldEntry info = new OreVeinWorldEntry();
        info.oreYield = tag.getInt("oreYield");
        info.operationsRemaining = tag.getInt("operationsRemaining");

        if (tag.contains("vein")) {
            ResourceLocation id = new ResourceLocation(tag.getString("vein"));
            if (GTRegistries.BEDROCK_ORE_DEFINITIONS.containKey(id)) {
                info.definition = GTRegistries.BEDROCK_ORE_DEFINITIONS.get(id);
            }
        }
        return info;
    }
}
