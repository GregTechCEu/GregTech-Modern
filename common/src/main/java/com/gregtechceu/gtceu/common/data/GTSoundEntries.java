package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import dev.architectury.injectables.annotations.ExpectPlatform;

import static com.gregtechceu.gtceu.api.registry.GTRegistries.REGISTRATE;

/**
 * @author KilaBash
 * @date 2023/3/3
 * @implNote GTSounds
 */
public class GTSoundEntries {
    // Machine Sounds
    public static final SoundEntry FORGE_HAMMER = REGISTRATE.sound("forge_hammer").build();
    public static final SoundEntry MACERATOR = REGISTRATE.sound("macerator").build();
    public static final SoundEntry CHEMICAL = REGISTRATE.sound("chemical").build();
    public static final SoundEntry ASSEMBLER = REGISTRATE.sound("assembler").build();
    public static final SoundEntry CENTRIFUGE = REGISTRATE.sound("centrifuge").build();
    public static final SoundEntry COMPRESSOR = REGISTRATE.sound("compressor").build();
    public static final SoundEntry ELECTROLYZER = REGISTRATE.sound("electrolyzer").build();
    public static final SoundEntry MIXER = REGISTRATE.sound("mixer").build();
    public static final SoundEntry REPLICATOR = REGISTRATE.sound("replicator").build();
    public static final SoundEntry ARC = REGISTRATE.sound("arc").build();
    public static final SoundEntry BOILER = REGISTRATE.sound("boiler").build();
    public static final SoundEntry FURNACE = REGISTRATE.sound("furnace").build();
    public static final SoundEntry COOLING = REGISTRATE.sound("cooling").build();
    public static final SoundEntry FIRE = REGISTRATE.sound("fire").build();
    public static final SoundEntry BATH = REGISTRATE.sound("bath").build();
    public static final SoundEntry MOTOR = REGISTRATE.sound("motor").build();
    public static final SoundEntry CUT = REGISTRATE.sound("cut").build();
    public static final SoundEntry TURBINE = REGISTRATE.sound("turbine").build();
    public static final SoundEntry COMBUSTION = REGISTRATE.sound("combustion").build();
    public static final SoundEntry MINER = REGISTRATE.sound("miner").build();
    public static final SoundEntry SCIENCE = REGISTRATE.sound("science").build();
    public static final SoundEntry WRENCH_TOOL = REGISTRATE.sound("wrench").build();
    public static final SoundEntry SOFT_MALLET_TOOL = REGISTRATE.sound("soft_hammer").build();
    public static final SoundEntry DRILL_TOOL = REGISTRATE.sound("drill").build();
    public static final SoundEntry PLUNGER_TOOL = REGISTRATE.sound("plunger").build();
    public static final SoundEntry FILE_TOOL = REGISTRATE.sound("file").build();
    public static final SoundEntry SAW_TOOL = REGISTRATE.sound("saw").build();
    public static final SoundEntry SCREWDRIVER_TOOL = REGISTRATE.sound("screwdriver").build();
    public static final SoundEntry CHAINSAW_TOOL = REGISTRATE.sound("chainsaw").build();
    public static final SoundEntry WIRECUTTER_TOOL = REGISTRATE.sound("wirecutter").build();
    public static final SoundEntry SPRAY_CAN_TOOL = REGISTRATE.sound("spray_can").build();
    public static final SoundEntry TRICORDER_TOOL = REGISTRATE.sound("tricorder").build();
    public static final SoundEntry MORTAR_TOOL = REGISTRATE.sound("mortar").build();
    public static final SoundEntry SUS_RECORD = REGISTRATE.sound("sus").build();
    public static final SoundEntry PORTAL_OPENING = REGISTRATE.sound("portal_opening").build();
    public static final SoundEntry PORTAL_CLOSING = REGISTRATE.sound("portal_closing").build();
    public static final SoundEntry METAL_PIPE = REGISTRATE.sound("metal_pipe").build();

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerSounds);
        GTRegistries.SOUNDS.values().forEach(SoundEntry::prepare);
        registerSounds();
    }

    @ExpectPlatform
    private static void registerSounds() {
        throw new AssertionError();
    }

}
