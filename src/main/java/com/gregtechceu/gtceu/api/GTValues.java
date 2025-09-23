package com.gregtechceu.gtceu.api;

import net.minecraft.util.RandomSource;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.IntFunction;

import static net.minecraft.ChatFormatting.*;

/**
 * Made for static imports, this Class is just a Helper.
 */
public class GTValues {

    /**
     * <p/>
     * This is worth exactly one normal Item.
     * This Constant can be divided by many commonly used Numbers such as
     * 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 24, ... 64 or 81
     * without loosing precision and is for that reason used as Unit of Amount.
     * But it is also small enough to be multiplied with larger Numbers.
     * <p/>
     * This is used to determine the amount of Material contained inside a prefixed Ore.
     * For example Nugget = M / 9 as it contains out of 1/9 of an Ingot.
     */
    public static final long M = 3628800;

    /**
     * Renamed from "FLUID_MATERIAL_UNIT" to just "L"
     * <p/>
     * Fluid per Material Unit (Prime Factors: 3 * 3 * 2 * 2 * 2 * 2)
     */
    public static final int L = 144;
    public static final RandomSource RNG = RandomSource.createThreadSafe();

    /**
     * The Item WildCard Tag. Even shorter than the "-1" of the past
     */

    // public static final short W = OreDictionary.WILDCARD_VALUE;

    /** Current time on the Client. Will always be zero on the server. */
    public static long CLIENT_TIME = 0;

    /**
     * The Voltage Tiers. Use this Array instead of the old named Voltage Variables
     */
    public static final long[] V = { 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608,
            33554432, 134217728, 536870912, 2147483648L };

    /**
     * The Voltage Tiers divided by 2.
     */
    public static final int[] VH = { 4, 16, 64, 256, 1024, 4096, 16384, 65536, 262144, 1048576, 4194304, 16777216,
            67108864, 268435456, 1073741824 };

    /**
     * The Voltage Tiers adjusted for cable loss. Use this for recipe EU/t to avoid full-amp recipes
     */
    public static final int[] VA = { 7, 30, 120, 480, 1920, 7680, 30720, 122880, 491520, 1966080, 7864320,
            31457280, 125829120, 503316480, 2013265920 };

    /**
     * The Voltage Tiers adjusted for cable loss, divided by 2.
     */
    public static final int[] VHA = { 3, 15, 60, 240, 960, 3840, 15360, 61440, 245760, 983040, 3932160, 15728640,
            62914560, 251658240, 1006632960 };

    /**
     * The Voltage Tiers. Use this Array instead of the old named Voltage Variables
     */
    public static final long[] VEX = { 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608,
            33554432, 134217728, 536870912, 2147483648L, 8589934592L, 34359738368L, 137438953472L, 549755813888L,
            2199023255552L, 8796093022208L, 35184372088832L, 140737488355328L, 562949953421312L, 2251799813685248L,
            9007199254740992L, 36028797018963968L, 144115188075855872L, 576460752303423488L, 2305843009213693952L,
            Long.MAX_VALUE };

    public static final int ULV = 0;
    public static final int LV = 1;
    public static final int MV = 2;
    public static final int HV = 3;
    public static final int EV = 4;
    public static final int IV = 5;
    public static final int LuV = 6;
    public static final int ZPM = 7;
    public static final int UV = 8;
    public static final int UHV = 9;
    public static final int UEV = 10;
    public static final int UIV = 11;
    public static final int UXV = 12;
    public static final int OpV = 13;
    public static final int MAX = 14;
    public static final int MAX_TRUE = 30;

    public static final int[] ALL_TIERS = new int[] { ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV, UEV, UIV, UXV, OpV,
            MAX };
    public static final int TIER_COUNT = ALL_TIERS.length;

    public static int[] tiersBetween(int minInclusive, int maxInclusive) {
        return Arrays.stream(ALL_TIERS).dropWhile(tier -> tier < minInclusive).takeWhile(tier -> tier <= maxInclusive)
                .toArray();
    }

    public static final String MODID_TOP = "theoneprobe",
            MODID_JEI = "jei",
            MODID_REI = "roughlyenoughitems",
            MODID_EMI = "emi",
            MODID_APPENG = "ae2",
            MODID_KUBEJS = "kubejs",
            MODID_IRIS = "iris",
            MODID_OCULUS = "oculus",
            MODID_SODIUM = "sodium",
            MODID_RUBIDIUM = "rubidium",
            MODID_EMBEDDIUM = "embeddium",
            MODID_CREATE = "create",
            MODID_CURIOS = "curios",
            MODID_AE2WTLIB = "ae2wtlib",
            MODID_SHIMMER = "shimmer",
            MODID_MODERNFIX = "modernfix",
            MODID_JOURNEYMAP = "journeymap",
            MODID_XAEROS_MINIMAP = "xaerominimap",
            MODID_XAEROS_WORLDMAP = "xaeroworldmap",
            MODID_FTB_CHUNKS = "ftbchunks",
            MODID_JAVD = "javd",
            MODID_FTB_TEAMS = "ftbteams",
            MODID_ARGONAUTS = "argonauts",
            MODID_HERACLES = "heracles",
            MODID_GAMESTAGES = "gamestages",
            MODID_FTB_QUEST = "ftbquests",
            MODID_CCTWEAKED = "computercraft",
            MODID_ENDERIO = "enderio",
            MODID_ENSORCELLATION = "ensorcellation";

    /**
     * Spray painting compat modids
     */
    public static final String MODID_TINTED = "tinted";

    /**
     * The short names for the voltages, used for registration primarily
     */
    public static final String[] VN = new String[] { "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV",
            "UEV", "UIV", "UXV", "OpV", "MAX" };

    public static final IntFunction<String> MAX_PLUS_FORMAT = (value) -> "" + RED + BOLD + "M" +
            GREEN + BOLD + "A" +
            BLUE + BOLD + "X" +
            YELLOW + BOLD + "+" +
            RED + BOLD + value;

    /**
     * The short names for the voltages, formatted for text
     */
    public static final String[] VNF = new String[] {
            DARK_GRAY + "ULV",
            GRAY + "LV",
            AQUA + "MV",
            GOLD + "HV",
            DARK_PURPLE + "EV",
            BLUE + "IV",
            LIGHT_PURPLE + "LuV",
            RED + "ZPM",
            DARK_AQUA + "UV",
            DARK_RED + "UHV",
            GREEN + "UEV",
            DARK_GREEN + "UIV",
            YELLOW + "UXV",
            BLUE.toString() + BOLD + "OpV",
            RED.toString() + BOLD + "MAX",
            MAX_PLUS_FORMAT.apply(1),
            MAX_PLUS_FORMAT.apply(2),
            MAX_PLUS_FORMAT.apply(3),
            MAX_PLUS_FORMAT.apply(4),
            MAX_PLUS_FORMAT.apply(5),
            MAX_PLUS_FORMAT.apply(6),
            MAX_PLUS_FORMAT.apply(7),
            MAX_PLUS_FORMAT.apply(8),
            MAX_PLUS_FORMAT.apply(9),
            MAX_PLUS_FORMAT.apply(10),
            MAX_PLUS_FORMAT.apply(11),
            MAX_PLUS_FORMAT.apply(12),
            MAX_PLUS_FORMAT.apply(13),
            MAX_PLUS_FORMAT.apply(14),
            MAX_PLUS_FORMAT.apply(15),
            MAX_PLUS_FORMAT.apply(16),
    };

    public static final String[] VCF = new String[] {
            DARK_GRAY.toString(),
            GRAY.toString(),
            AQUA.toString(),
            GOLD.toString(),
            DARK_PURPLE.toString(),
            BLUE.toString(),
            LIGHT_PURPLE.toString(),
            RED.toString(),
            DARK_AQUA.toString(),
            DARK_RED.toString(),
            GREEN.toString(),
            DARK_GREEN.toString(),
            YELLOW.toString(),
            BLUE.toString() + BOLD.toString(),
            RED.toString() + BOLD.toString() };

    public static final String[] VLVH = new String[] {
            "Primitive", // not doing the gray color for these first two because it looks weird
            "Basic",
            AQUA + "Advanced",
            GOLD + "Advanced",
            DARK_PURPLE + "Advanced",
            BLUE + "Elite",
            LIGHT_PURPLE + "Elite",
            RED + "Elite",
            DARK_AQUA + "Ultimate",
            DARK_RED + "Epic",
            GREEN + "Epic",
            DARK_GREEN + "Epic",
            YELLOW + "Epic",
            BLUE.toString() + BOLD + "Legendary",
            RED.toString() + BOLD + "MAX" };

    public static final String[] VLVT = new String[] {
            "" + RESET,
            "" + RESET,
            "" + RESET,
            "II" + RESET,
            "III" + RESET,
            "" + RESET,
            "II" + RESET,
            "III" + RESET,
            "" + RESET,
            "" + RESET,
            "II" + RESET,
            "III" + RESET,
            "IV" + RESET,
            "" + RESET,
            "" + RESET };

    public static final String[] LVT = new String[] {
            "",
            "I",
            "II",
            "III",
            "IV",
            "V",
            "VI",
            "VII",
            "VIII",
            "IX",
            "X",
            "XI",
            "XII",
            "XIII",
            "XIV",
    };

    /**
     * Color values for the voltages
     */
    public static final int[] VC = new int[] { 0xC80000, 0xDCDCDC, 0xFF6400, 0xFFFF1E, 0x808080, 0xF0F0F5, 0xE99797,
            0x7EC3C4, 0x7EB07E, 0xBF74C0, 0x0B5CFE, 0x914E91, 0x488748, 0x8C0000, 0x2828F5 };

    // Main colour for each tier
    public static final int[] VCM = new int[] {
            DARK_GRAY.getColor(),
            GRAY.getColor(),
            AQUA.getColor(),
            GOLD.getColor(),
            DARK_PURPLE.getColor(),
            BLUE.getColor(),
            LIGHT_PURPLE.getColor(),
            RED.getColor(),
            DARK_AQUA.getColor(),
            DARK_RED.getColor(),
            GREEN.getColor(),
            DARK_GREEN.getColor(),
            YELLOW.getColor(),
            BLUE.getColor(),
            RED.getColor()
    };

    // Main color for steam machines
    public static final int VC_LP_STEAM = 0xBB8E53;
    public static final int VC_HP_STEAM = 0x79756F;

    /**
     * The long names for the voltages
     */
    public static final String[] VOLTAGE_NAMES = new String[] { "Ultra Low Voltage", "Low Voltage", "Medium Voltage",
            "High Voltage", "Extreme Voltage", "Insane Voltage", "Ludicrous Voltage", "ZPM Voltage", "Ultimate Voltage",
            "Ultra High Voltage", "Ultra Excessive Voltage", "Ultra Immense Voltage", "Ultra Extreme Voltage",
            "Overpowered Voltage", "Maximum Voltage" };

    /**
     * Used to tell if any high-tier machine (UHV+) was registered.
     */
    public static boolean HT = false;

    public static BooleanSupplier FOOLS = () -> {
        var now = LocalDate.now();
        return now.getMonth() == Month.APRIL && now.getDayOfMonth() == 1;
    };

    public static BooleanSupplier XMAS = () -> {
        var now = LocalDate.now();
        return now.getMonth() == Month.DECEMBER && (now.getDayOfMonth() == 24 || now.getDayOfMonth() == 25);
    };

    public static final String CUSTOM_TAG_SOURCE = "GTCEu Custom Tags";
}
