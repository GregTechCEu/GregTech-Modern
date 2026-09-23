package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.neoforged.neoforge.registries.DeferredHolder;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTSoundEntries {

    // Machine Sounds
    // spotless:off
    public static final DeferredHolder<SoundEntry, SoundEntry> FORGE_HAMMER = REGISTRATE.sound("forge_hammer").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> MACERATOR = REGISTRATE.sound("macerator").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> CHEMICAL = REGISTRATE.sound("chemical").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> ASSEMBLER = REGISTRATE.sound("assembler").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> CENTRIFUGE = REGISTRATE.sound("centrifuge").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> COMPRESSOR = REGISTRATE.sound("compressor").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> ELECTROLYZER = REGISTRATE.sound("electrolyzer").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> MIXER = REGISTRATE.sound("mixer").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> REPLICATOR = REGISTRATE.sound("replicator").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> ARC = REGISTRATE.sound("arc").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> BOILER = REGISTRATE.sound("boiler").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> FURNACE = REGISTRATE.sound("furnace").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> COOLING = REGISTRATE.sound("cooling").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> FIRE = REGISTRATE.sound("fire").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> BATH = REGISTRATE.sound("bath").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> MOTOR = REGISTRATE.sound("motor").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> CUT = REGISTRATE.sound("cut").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> TURBINE = REGISTRATE.sound("turbine").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> COMBUSTION = REGISTRATE.sound("combustion").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> COMPUTATION = REGISTRATE.sound("computation").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> MINER = REGISTRATE.sound("miner").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> SCIENCE = REGISTRATE.sound("science").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> JET_ENGINE = REGISTRATE.sound("jet_engine").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> WRENCH_TOOL = REGISTRATE.sound("wrench").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> SOFT_MALLET_TOOL = REGISTRATE.sound("soft_hammer").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> DRILL_TOOL = REGISTRATE.sound("drill").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> PLUNGER_TOOL = REGISTRATE.sound("plunger").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> FILE_TOOL = REGISTRATE.sound("file").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> SAW_TOOL = REGISTRATE.sound("saw").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> SCREWDRIVER_TOOL = REGISTRATE.sound("screwdriver").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> CHAINSAW_TOOL = REGISTRATE.sound("chainsaw").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> WIRECUTTER_TOOL = REGISTRATE.sound("wirecutter").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> SPRAY_CAN_TOOL = REGISTRATE.sound("spray_can").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> PORTABLE_SCANNER = REGISTRATE.sound("portable_scanner").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> MORTAR_TOOL = REGISTRATE.sound("mortar").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> SUS_RECORD = REGISTRATE.sound("sus").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> PORTAL_OPENING = REGISTRATE.sound("portal_opening").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> PORTAL_CLOSING = REGISTRATE.sound("portal_closing").register();
    public static final DeferredHolder<SoundEntry, SoundEntry> METAL_PIPE = REGISTRATE.sound("metal_pipe").register();
    //spotless:on

    public static void init() {}
}
