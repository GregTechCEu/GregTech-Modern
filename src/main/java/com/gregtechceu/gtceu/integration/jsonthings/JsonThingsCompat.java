package com.gregtechceu.gtceu.integration.jsonthings;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistry;
import com.gregtechceu.gtceu.integration.jsonthings.parsers.MachineParser;
import com.gregtechceu.gtceu.integration.jsonthings.parsers.MaterialParser;
import com.gregtechceu.gtceu.integration.jsonthings.serializers.MachineBuilderType;
import com.gregtechceu.gtceu.integration.jsonthings.utils.MetaMachineFlexBlock;

import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.javafmlmod.FMLModContainer;

import dev.gigaherz.jsonthings.things.ThingRegistries;
import dev.gigaherz.jsonthings.things.parsers.ThingResourceManager;
import dev.gigaherz.jsonthings.things.serializers.FlexBlockType;
import dev.gigaherz.jsonthings.things.shapes.BasicShape;
import dev.gigaherz.jsonthings.things.shapes.DynamicShape;

public class JsonThingsCompat {

    public static final GTRegistry.String<MachineBuilderType<?, ?>> MACHINE_BUILDER_TYPES = new GTRegistry.String<>(
            GTCEu.id("machine_builder_types"));

    public static final FlexBlockType<MetaMachineFlexBlock> META_MACHINE = FlexBlockType.register("gtceu:machine",
            data -> (props, builder) -> new MetaMachineFlexBlock(props, MachineDefinition.getBuilt()),
            "cutout", false);

    public static final DynamicShape FULL_BLOCK = new DynamicShape(new BasicShape(0, 0, 0, 16, 16, 16), null);

    public static void init(IEventBus gtBus) {
        IEventBus jsonThingsBus = ((FMLModContainer) ModList.get()
                .getModContainerById(GTValues.MODID_JSONTHINGS)
                .get())
                .getEventBus();

        MachineBuilderType.init();

        ThingResourceManager manager = ThingResourceManager.instance();
        manager.registerParser(new MaterialParser(jsonThingsBus));
        manager.registerParser(new MachineParser(gtBus));

        Registry.register(ThingRegistries.DYNAMIC_SHAPES, "full_block", FULL_BLOCK);
    }
}
