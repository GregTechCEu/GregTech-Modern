package com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class FluidVeinWorldEntry {

    public static final Codec<FluidVeinWorldEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BedrockFluidDefinition.CODEC.optionalFieldOf("vein").forGetter(entry -> Optional.ofNullable(
                    entry.getDefinition())),
            Codec.INT.fieldOf("fluid_yield").forGetter(FluidVeinWorldEntry::getFluidYield),
            Codec.INT.fieldOf("operations_remaining").forGetter(FluidVeinWorldEntry::getOperationsRemaining))
            .apply(instance, (definition, fluidYield, operationsRemaining) -> new FluidVeinWorldEntry(
                    definition.orElse(null), fluidYield, operationsRemaining)));

    @Setter
    private Supplier<@Nullable Holder<BedrockFluidDefinition>> definition;
    @Getter
    private int fluidYield;
    @Getter
    private int operationsRemaining;

    public FluidVeinWorldEntry(@Nullable Holder<BedrockFluidDefinition> definition, int fluidYield,
                               int operationsRemaining) {
        this.definition = () -> definition;
        this.fluidYield = fluidYield;
        this.operationsRemaining = operationsRemaining;
    }

    private FluidVeinWorldEntry() {}

    @Nullable
    public Holder<BedrockFluidDefinition> getDefinition() {
        return this.definition.get();
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

        Holder<BedrockFluidDefinition> def = getDefinition();
        if (def != null && def.unwrapKey().isPresent()) {
            tag.putString("vein", def.unwrapKey().get().identifier().toString());
        }
        return tag;
    }

    @NotNull
    public static FluidVeinWorldEntry readFromNBT(@NotNull CompoundTag tag, HolderLookup.Provider provider) {
        FluidVeinWorldEntry info = new FluidVeinWorldEntry();
        info.fluidYield = tag.getIntOr("fluidYield", 0);
        info.operationsRemaining = tag.getIntOr("operationsRemaining", 0);

        if (tag.contains("vein")) {
            Identifier id = Identifier.parse(tag.getStringOr("vein", ""));
            info.setDefinition(GTMemoizer.memoize(() -> {
                return provider.lookup(GTRegistries.BEDROCK_FLUID_REGISTRY)
                        .flatMap(reg -> reg.get(ResourceKey.create(GTRegistries.BEDROCK_FLUID_REGISTRY, id)))
                        .orElse(null);
            }));
        } else {
            info.setDefinition(() -> null);
        }
        return info;
    }
}
