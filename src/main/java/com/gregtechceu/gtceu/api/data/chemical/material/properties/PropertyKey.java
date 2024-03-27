package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.mojang.serialization.Codec;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PropertyKey<T extends IMaterialProperty<T>> {

    private static final Map<String, PropertyKey<?>> KEYS = new HashMap<>();

    public static final PropertyKey<BlastProperty> BLAST = new PropertyKey<>("blast", BlastProperty.class, BlastProperty::new, BlastProperty.CODEC);
    public static final PropertyKey<AlloyBlastProperty> ALLOY_BLAST = new PropertyKey<>("blast_alloy", AlloyBlastProperty.class, AlloyBlastProperty::new, AlloyBlastProperty.CODEC);
    public static final PropertyKey<DustProperty> DUST = new PropertyKey<>("dust", DustProperty.class, DustProperty::new, DustProperty.CODEC);
    public static final PropertyKey<FluidPipeProperties> FLUID_PIPE = new PropertyKey<>("fluid_pipe", FluidPipeProperties.class, FluidPipeProperties::new, FluidPipeProperties.CODEC);
    public static final PropertyKey<FluidProperty> FLUID = new PropertyKey<>("fluid", FluidProperty.class, FluidProperty::new, FluidProperty.CODEC);
    public static final PropertyKey<GemProperty> GEM = new PropertyKey<>("gem", GemProperty.class, GemProperty::new, Codec.unit(GemProperty::new));
    public static final PropertyKey<IngotProperty> INGOT = new PropertyKey<>("ingot", IngotProperty.class, IngotProperty::new, IngotProperty.CODEC);
    public static final PropertyKey<PolymerProperty> POLYMER = new PropertyKey<>("polymer", PolymerProperty.class, PolymerProperty::new, Codec.unit(PolymerProperty::new));
    public static final PropertyKey<ItemPipeProperties> ITEM_PIPE = new PropertyKey<>("item_pipe", ItemPipeProperties.class, ItemPipeProperties::new, ItemPipeProperties.CODEC);
    public static final PropertyKey<OreProperty> ORE = new PropertyKey<>("ore", OreProperty.class, OreProperty::new, OreProperty.CODEC);
    public static final PropertyKey<ToolProperty> TOOL = new PropertyKey<>("tool", ToolProperty.class, ToolProperty::new, ToolProperty.CODEC);
    public static final PropertyKey<RotorProperty> ROTOR = new PropertyKey<>("rotor", RotorProperty.class, RotorProperty::new, RotorProperty.CODEC);
    public static final PropertyKey<WireProperties> WIRE = new PropertyKey<>("wire", WireProperties.class, WireProperties::new, WireProperties.CODEC);
    public static final PropertyKey<WoodProperty> WOOD = new PropertyKey<>("wood", WoodProperty.class, WoodProperty::new, Codec.unit(WoodProperty::new));

    public static final PropertyKey<HazardProperty> HAZARD = new PropertyKey<>("hazard", HazardProperty.class);

    // Empty property used to allow property-less Materials without removing base type enforcement
    public static final PropertyKey<EmptyProperty> EMPTY = new PropertyKey<>("empty", EmptyProperty.class, EmptyProperty::new, Codec.unit(EmptyProperty::new));

    @Getter
    private final String key;
    private final Class<T> type;
    private final Supplier<T> defaultSupplier;
    @Getter
    private final Codec<T> codec;

    public PropertyKey(String key, Class<T> type, Supplier<T> defaultSupplier, Codec<T> codec) {
        this.key = key;
        this.type = type;
        this.defaultSupplier = defaultSupplier;
        this.codec = codec;
        KEYS.put(key, this);
    }

    protected T constructDefault() {
        return defaultSupplier.get();
    }

    public T cast(IMaterialProperty<?> property) {
        return this.type.cast(property);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PropertyKey) {
            return ((PropertyKey<?>) o).getKey().equals(key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key;
    }

    public static PropertyKey<?> getByName(String name) {
        return KEYS.get(name);
    }

    private static class EmptyProperty implements IMaterialProperty<EmptyProperty> {

        @Override
        public void verifyProperty(MaterialProperties properties) {
            // no-op
        }
    }
}
