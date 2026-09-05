package com.gregtechceu.gtceu.common.data.worldgen;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator;
import com.gregtechceu.gtceu.api.data.worldgen.generator.veins.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.data.worldgen.generator.VeinGenerator.VeinGeneratorType;

@SuppressWarnings("unused")
public class GTVeinGenerators {

    // spotless:off
    private static final DeferredRegister<VeinGeneratorType<?>> VEIN_GENERATORS = DeferredRegister.create(GTRegistries.Keys.VEIN_GENERATOR, GTCEu.MOD_ID);

    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<NoopVeinGenerator>> NO_OP = register("no_op", NoopVeinGenerator.CODEC, () -> NoopVeinGenerator.INSTANCE);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<StandardVeinGenerator>> STANDARD = register("standard", StandardVeinGenerator.CODEC, StandardVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<LayeredVeinGenerator>> LAYER = register("layer", LayeredVeinGenerator.CODEC, LayeredVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<GeodeVeinGenerator>> GEODE = register("geode", GeodeVeinGenerator.CODEC, GeodeVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<DikeVeinGenerator>> DIKE = register("dike", DikeVeinGenerator.CODEC, DikeVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<VeinedVeinGenerator>> VEINED = register("veined", VeinedVeinGenerator.CODEC, VeinedVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<ClassicVeinGenerator>> CLASSIC = register("classic", ClassicVeinGenerator.CODEC, ClassicVeinGenerator::new);
    public static final DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<CuboidVeinGenerator>> CUBOID = register("cuboid", CuboidVeinGenerator.CODEC, CuboidVeinGenerator::new);

    private static <T extends VeinGenerator> DeferredHolder<VeinGeneratorType<?>, VeinGeneratorType<T>> register(String id, MapCodec<T> codec, Supplier<T> function) {
        return VEIN_GENERATORS.register(id, () -> new VeinGenerator.VeinGeneratorType<>(codec, function));
    }
    
    //spotless:on
    public static void init(IEventBus modBus) {}
}
