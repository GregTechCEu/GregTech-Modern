package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public class PropertyKey<T extends IMaterialProperty<T>> {

    private static final Map<String, PropertyKey<?>> KEYS = new HashMap<>();

    public static final PropertyKey<BlastProperty> BLAST = new PropertyKey<>("blast", BlastProperty.class);
    public static final PropertyKey<AlloyBlastProperty> ALLOY_BLAST = new PropertyKey<>("blast_alloy",
            AlloyBlastProperty.class);
    public static final PropertyKey<DustProperty> DUST = new PropertyKey<>("dust", DustProperty.class);
    public static final PropertyKey<FluidPipeProperties> FLUID_PIPE = new PropertyKey<>("fluid_pipe",
            FluidPipeProperties.class);
    public static final PropertyKey<FluidProperty> FLUID = new PropertyKey<>("fluid", FluidProperty.class);
    public static final PropertyKey<GemProperty> GEM = new PropertyKey<>("gem", GemProperty.class,
            Codec.unit(GemProperty::new));
    public static final PropertyKey<IngotProperty> INGOT = new PropertyKey<>("ingot", IngotProperty.class,
            IngotProperty.CODEC);
    public static final PropertyKey<PolymerProperty> POLYMER = new PropertyKey<>("polymer", PolymerProperty.class,
            Codec.unit(PolymerProperty::new));
    public static final PropertyKey<ItemPipeProperties> ITEM_PIPE = new PropertyKey<>("item_pipe",
            ItemPipeProperties.class, ItemPipeProperties.CODEC);
    public static final PropertyKey<OreProperty> ORE = new PropertyKey<>("ore", OreProperty.class, OreProperty.CODEC);
    public static final PropertyKey<ToolProperty> TOOL = new PropertyKey<>("tool", ToolProperty.class,
            ToolProperty.CODEC);
    public static final PropertyKey<RotorProperty> ROTOR = new PropertyKey<>("rotor", RotorProperty.class,
            RotorProperty.CODEC);
    public static final PropertyKey<WireProperties> WIRE = new PropertyKey<>("wire", WireProperties.class,
            WireProperties.CODEC);
    public static final PropertyKey<WoodProperty> WOOD = new PropertyKey<>("wood", WoodProperty.class,
            Codec.unit(WoodProperty::new));

    public static final PropertyKey<HazardProperty> HAZARD = new PropertyKey<>("hazard", HazardProperty.class);

    // Empty property used to allow property-less Materials without removing base type enforcement
    public static final PropertyKey<EmptyProperty> EMPTY = new PropertyKey<>("empty", EmptyProperty.class,
            Codec.unit(EmptyProperty::new));

    private final String key;
    private final Class<T> type;

    public PropertyKey(String key, Class<T> type, Codec<T> codec) {
        this.key = key;
        this.type = type;
        KEYS.put(key, this);
    }

    protected String getKey() {
        return key;
    }

    protected T constructDefault() {
        try {
            return type.newInstance();
        } catch (Exception e) {
            return null;
        }
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
