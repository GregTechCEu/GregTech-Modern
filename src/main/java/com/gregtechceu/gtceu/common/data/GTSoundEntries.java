package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.addon.AddonFinder;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistries;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class GTSoundEntries {

    static {
        GTRegistries.SOUNDS.unfreeze();
    }

    // Machine Sounds
    public static final SoundEntry FORGE_HAMMER = REGISTRATE.sound("forge_hammer").subtitleLang("Forge Hammer thumping")
            .build();
    public static final SoundEntry MACERATOR = REGISTRATE.sound("macerator").subtitleLang("Macerator crushing").build();
    public static final SoundEntry CHEMICAL = REGISTRATE.sound("chemical").subtitleLang("Chemical bubbling").build();
    public static final SoundEntry ASSEMBLER = REGISTRATE.sound("assembler").subtitleLang("Assembler constructing")
            .build();
    public static final SoundEntry CENTRIFUGE = REGISTRATE.sound("centrifuge").subtitleLang("Centrifuge spinning")
            .build();
    public static final SoundEntry COMPRESSOR = REGISTRATE.sound("compressor").subtitleLang("Compressor squeezing")
            .build();
    public static final SoundEntry ELECTROLYZER = REGISTRATE.sound("electrolyzer").subtitleLang("Electrolyzer sparking")
            .build();
    public static final SoundEntry MIXER = REGISTRATE.sound("mixer").subtitleLang("Mixer sloshing").build();
    public static final SoundEntry REPLICATOR = REGISTRATE.sound("replicator").subtitleLang("Replicator copying")
            .build();
    public static final SoundEntry ARC = REGISTRATE.sound("arc").subtitleLang("Arcs buzzing").build();
    public static final SoundEntry BOILER = REGISTRATE.sound("boiler").subtitleLang("Boiler heating").build();
    public static final SoundEntry FURNACE = REGISTRATE.sound("furnace").subtitleLang("Furnace heating").build();
    public static final SoundEntry COOLING = REGISTRATE.sound("cooling").subtitleLang("Freezer humming").build();
    public static final SoundEntry FIRE = REGISTRATE.sound("fire").subtitleLang("Fire crackling").build();
    public static final SoundEntry BATH = REGISTRATE.sound("bath").subtitleLang("Bath fizzing").build();
    public static final SoundEntry MOTOR = REGISTRATE.sound("motor").subtitleLang("Motor humming").build();
    public static final SoundEntry CUT = REGISTRATE.sound("cut").subtitleLang("Cutter whirring").build();
    public static final SoundEntry TURBINE = REGISTRATE.sound("turbine").subtitleLang("Turbine whizzing").build();
    public static final SoundEntry COMBUSTION = REGISTRATE.sound("combustion").subtitleLang("Combusting").build();
    public static final SoundEntry COMPUTATION = REGISTRATE.sound("computation").subtitleLang("Computer beeps").build();
    public static final SoundEntry MINER = REGISTRATE.sound("miner").subtitleLang("Miner excavating").build();
    public static final SoundEntry SCIENCE = REGISTRATE.sound("science").subtitleLang("s c i e n c e").build();
    public static final SoundEntry JET_ENGINE = REGISTRATE.sound("jet_engine").subtitleLang("Jet roaring").build();
    public static final SoundEntry WRENCH_TOOL = REGISTRATE.sound("wrench").subtitleLang("Wrench rattling").build();
    public static final SoundEntry SOFT_MALLET_TOOL = REGISTRATE.sound("soft_hammer").subtitleLang("Soft tap").build();
    public static final SoundEntry DRILL_TOOL = REGISTRATE.sound("drill").subtitleLang("Drilling").build();
    public static final SoundEntry PLUNGER_TOOL = REGISTRATE.sound("plunger").subtitleLang("Plunger popping").build();
    public static final SoundEntry FILE_TOOL = REGISTRATE.sound("file").subtitleLang("File rasping").build();
    public static final SoundEntry SAW_TOOL = REGISTRATE.sound("saw").subtitleLang("Sawing").build();
    public static final SoundEntry SCREWDRIVER_TOOL = REGISTRATE.sound("screwdriver").subtitleLang("Screwing").build();
    public static final SoundEntry CHAINSAW_TOOL = REGISTRATE.sound("chainsaw").subtitleLang("Chainsaw revving")
            .build();
    public static final SoundEntry WIRECUTTER_TOOL = REGISTRATE.sound("wirecutter").subtitleLang("Wire snipped")
            .build();
    public static final SoundEntry SPRAY_CAN_TOOL = REGISTRATE.sound("spray_can").subtitleLang("Spraying").build();
    public static final SoundEntry PORTABLE_SCANNER = REGISTRATE.sound("portable_scanner").subtitleLang("Scanning")
            .build();
    public static final SoundEntry MORTAR_TOOL = REGISTRATE.sound("mortar").subtitleLang("Mortar crushing").build();
    public static final SoundEntry SUS_RECORD = REGISTRATE.sound("sus").subtitleLang("Sus...").build();
    public static final SoundEntry PORTAL_OPENING = REGISTRATE.sound("portal_opening").subtitleLang("Portal opens")
            .build();
    public static final SoundEntry PORTAL_CLOSING = REGISTRATE.sound("portal_closing").subtitleLang("Portal closes")
            .build();
    public static final SoundEntry METAL_PIPE = REGISTRATE.sound("metal_pipe")
            .subtitleLang("Destruction_Metal_Pole_L_Wave_2_0_0.wav").build();

    public static void init() {
        AddonFinder.getAddons().forEach(IGTAddon::registerSounds);
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.SOUNDS, SoundEntry.class));
        GTRegistries.SOUNDS.forEach(SoundEntry::prepare);
        registerSounds();

        GTRegistries.SOUNDS.freeze();
    }

    private static void registerSounds() {
        for (SoundEntry entry : GTRegistries.SOUNDS) {
            entry.register(soundEvent -> ForgeRegistries.SOUND_EVENTS.register(soundEvent.getLocation(), soundEvent));
        }
    }
}
