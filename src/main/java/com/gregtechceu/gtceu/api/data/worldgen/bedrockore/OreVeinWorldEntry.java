package com.gregtechceu.gtceu.api.data.worldgen.bedrockore;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OreVeinWorldEntry {

    @Nullable
    @Getter
    @Setter
    private Holder<BedrockOreDefinition> definition;
    @Getter
    private int oreYield;
    @Getter
    private int operationsRemaining;

    public OreVeinWorldEntry(@Nullable Holder<BedrockOreDefinition> vein, int oreYield, int operationsRemaining) {
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
        if (definition != null && definition.unwrapKey().isPresent()) {
            tag.putString("vein", definition.unwrapKey().get().location().toString());
        }
        return tag;
    }

    @NotNull
    public static OreVeinWorldEntry readFromNBT(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        OreVeinWorldEntry info = new OreVeinWorldEntry();
        info.oreYield = tag.getInt("oreYield");
        info.operationsRemaining = tag.getInt("operationsRemaining");

        if (tag.contains("vein")) {
            ResourceLocation id = ResourceLocation.parse(tag.getString("vein"));
            var maybeDef = provider.lookup(GTRegistries.BEDROCK_ORE_REGISTRY).get()
                    .get(ResourceKey.create(GTRegistries.BEDROCK_ORE_REGISTRY, id));
            maybeDef.ifPresent(info::setDefinition);
        }
        return info;
    }
}
