package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.drawable.ColorType;
import com.gregtechceu.gtceu.api.mui.drawable.TabTexture;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;

import org.jetbrains.annotations.NotNull;

public class GTGuiTextures {

    /** Keys used for GT assets registered for use in Themes */
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gregtech_standard_bg";
        public static final String STANDARD_BACKGROUND_INVERSE = "gregtech_standard_inverse_bg";
        public static final String COVER_BACKGROUND = "gregtech_cover_bg";
        public static final String BRONZE_BACKGROUND = "gregtech_bronze_bg";
        public static final String STEEL_BACKGROUND = "gregtech_steel_bg";
        public static final String PRIMITIVE_BACKGROUND = "gregtech_primitive_bg";

        public static final String STANDARD_SLOT = "gregtech_standard_slot";
        public static final String BRONZE_SLOT = "gregtech_bronze_slot";
        public static final String STEEL_SLOT = "gregtech_steel_slot";
        public static final String PRIMITIVE_SLOT = "gregtech_primitive_slot";

        public static final String STANDARD_FLUID_SLOT = "gregtech_standard_fluid_slot";

        public static final String STANDARD_BUTTON = "gregtech_standard_button";
    }

    // ICONS

    /** @apiNote You may want {@link GTGuiTextures#getLogo} instead. */
    public static final UITexture GREGTECH_LOGO = fullImage("textures/gui/icon/gregtech_logo.png");
    /** @apiNote You may want {@link GTGuiTextures#getLogo} instead. */
    public static final UITexture GREGTECH_LOGO_XMAS = fullImage("textures/gui/icon/gregtech_logo_xmas.png");
    public static final UITexture GREGTECH_LOGO_DARK = fullImage("textures/gui/icon/gregtech_logo_dark.png");
    // todo blinking GT logos

    public static final UITexture INDICATOR_NO_ENERGY = fullImage("textures/gui/base/indicator_no_energy.png");
    public static final UITexture INDICATOR_NO_STEAM_BRONZE = fullImage(
            "textures/gui/base/indicator_no_steam_bronze.png");
    public static final UITexture INDICATOR_NO_STEAM_STEEL = fullImage(
            "textures/gui/base/indicator_no_steam_steel.png");
    public static final UITexture TANK_ICON = fullImage("textures/gui/base/tank_icon.png");

    // BACKGROUNDS
    public static final UITexture BACKGROUND = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background.png")
            .imageSize(16, 16)
            .adaptable(4)
            .name(IDs.STANDARD_BACKGROUND)
            .defaultColorType()
            .build();

    public static final UITexture BACKGROUND_POPUP = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_popup.png")
            .imageSize(195, 136)
            .adaptable(4)
            .name(IDs.COVER_BACKGROUND)
            .canApplyTheme()
            .build();

    public static final UITexture BACKGROUND_TITLE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background.png")
            .imageSize(16, 16)
            .adaptable(4)
            .subAreaUV(0, 0, 1f, .75f)
            .build();

    public static final UITexture BACKGROUND_INVERSE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_inverse.png")
            .imageSize(16, 16)
            .adaptable(3)
            .name(IDs.STANDARD_BACKGROUND_INVERSE)
            .canApplyTheme()
            .build();

    public static final UITexture BACKGROUND_BRONZE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_bronze.png")
            .imageSize(176, 166)
            .adaptable(3)
            .name(IDs.BRONZE_BACKGROUND)
            .build();

    public static final UITexture BACKGROUND_STEEL = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_steel.png")
            .imageSize(176, 166)
            .adaptable(3)
            .name(IDs.STEEL_BACKGROUND)
            .build();

    // todo move to textures/gui/base
    public static final UITexture BACKGROUND_PRIMITIVE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/primitive/primitive_background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .name(IDs.PRIMITIVE_BACKGROUND)
            .build();

    // todo clipboard backgrounds, may deserve some redoing

    // DISPLAYS
    public static final UITexture DISPLAY = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display.png")
            .imageSize(182, 117)
            .canApplyTheme()
            .build();

    public static final UITexture DISPLAY_BRONZE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display_bronze.png")
            .imageSize(162, 121)
            .build();

    public static final UITexture DISPLAY_STEEL = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display_steel.png")
            .imageSize(162, 121)
            .adaptable(1)
            .build();

    // todo primitive display?

    // SLOTS
    public static final UITexture SLOT = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.STANDARD_SLOT)
            .canApplyTheme()
            .build();

    public static final UITexture SLOT_BRONZE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/slot_bronze.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.BRONZE_SLOT)
            .build();

    public static final UITexture SLOT_STEEL = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/slot_steel.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.STEEL_SLOT)
            .build();

    // todo move to textures/gui/base
    public static final UITexture SLOT_PRIMITIVE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/primitive/primitive_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.PRIMITIVE_SLOT)
            .build();

    public static final UITexture FLUID_SLOT = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/fluid_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.STANDARD_FLUID_SLOT)
            .canApplyTheme()
            .build();

    public static final UITexture[] BUTTON_POWER = slice("textures/gui/widget/button_power.png", 16, 32, 16, 16,
            ColorType.DEFAULT);

    public static final UITexture[] BUTTON_BLACKLIST = slice("textures/gui/widget/button_blacklist.png",
            16, 32, 16, 16, ColorType.DEFAULT);
    public static final UITexture[] BUTTON_IGNORE_DAMAGE = slice("textures/gui/widget/button_filter_damage.png",
            16, 32, 16, 16, ColorType.DEFAULT);
    public static final UITexture[] BUTTON_IGNORE_NBT = slice("textures/gui/widget/button_filter_nbt.png",
            16, 32, 16, 16, ColorType.DEFAULT);

    public static final UITexture[] BUTTON_CASE_SENSITIVE = slice(
            "textures/gui/widget/ore_filter/button_case_sensitive.png",
            16, 32, 16, 16, ColorType.DEFAULT);

    public static final UITexture[] BUTTON_MATCH_ALL = slice("textures/gui/widget/ore_filter/button_match_all.png",
            16, 32, 16, 16, ColorType.DEFAULT);
    public static final UITexture BUTTON_LOCK = fullImage("textures/gui/widget/button_lock.png");

    public static final UITexture OREDICT_ERROR = fullImage("textures/gui/widget/ore_filter/error.png");
    public static final UITexture OREDICT_INFO = fullImage("textures/gui/widget/ore_filter/info.png");
    public static final UITexture OREDICT_MATCH = fullImage("textures/gui/widget/ore_filter/match.png");
    public static final UITexture OREDICT_NO_MATCH = fullImage("textures/gui/widget/ore_filter/no_match.png");
    public static final UITexture OREDICT_SUCCESS = fullImage("textures/gui/widget/ore_filter/success.png");
    public static final UITexture OREDICT_WAITING = fullImage("textures/gui/widget/ore_filter/waiting.png");
    public static final UITexture OREDICT_WARN = fullImage("textures/gui/widget/ore_filter/warn.png");

    // public static final IDrawable PLUS = IKey.str("+").asIcon().marginLeft(1);
    // public static final IDrawable MINUS = IKey.str("-").asIcon().marginLeft(1);

    public static final UITexture[] MANUAL_IO_OVERLAY_IN = slice("textures/gui/overlay/manual_io_overlay_in.png",
            18, 18 * 3, 18, 18, ColorType.DEFAULT);
    public static final UITexture[] MANUAL_IO_OVERLAY_OUT = slice("textures/gui/overlay/manual_io_overlay_out.png",
            18, 18 * 3, 18, 18, ColorType.DEFAULT);
    public static final UITexture[] CONVEYOR_MODE_OVERLAY = slice("textures/gui/overlay/conveyor_mode_overlay.png",
            18, 18 * 2, 18, 18, ColorType.DEFAULT);

    public static final UITexture[] TRANSFER_MODE_OVERLAY = slice("textures/gui/overlay/transfer_mode_overlay.png",
            18, 18 * 3, 18, 18, ColorType.DEFAULT);

    public static final UITexture[] FLUID_TRANSFER_MODE_OVERLAY = slice(
            "textures/gui/overlay/fluid_transfer_mode_overlay.png",
            18, 18 * 3, 18, 18, ColorType.DEFAULT);

    public static final UITexture[] DISTRIBUTION_MODE_OVERLAY = slice(
            "textures/gui/widget/button_distribution_mode.png",
            16, 48, 16, 16, ColorType.DEFAULT);

    public static final UITexture BUTTON_VOID = fullImage("textures/gui/widget/button_void.png");

    public static final UITexture BUTTON_VOID_PARTIAL = fullImage("textures/gui/widget/button_void_partial.png");

    public static final UITexture[] BUTTON_VOID_MULTIBLOCK = slice("textures/gui/widget/button_void_multiblock.png",
            16, 48, 16, 16, ColorType.DEFAULT);

    public static final UITexture[] FILTER_MODE_OVERLAY = slice(
            "textures/gui/overlay/filter_mode_overlay.png",
            16, 48, 16, 16, ColorType.DEFAULT);

    public static final UITexture[] PRIVATE_MODE_BUTTON = slice(
            "textures/gui/widget/button_public_private.png",
            18, 36, 18, 18, ColorType.DEFAULT);

    public static final UITexture MENU_OVERLAY = fullImage("textures/gui/overlay/menu_overlay.png");

    public static final UITexture RECIPE_LOCK = fullImage("textures/gui/widget/lock.png");

    // todo bronze/steel/primitive fluid slots?

    // SLOT OVERLAYS
    public static final UITexture ATOMIC_OVERLAY_1 = fullImage("textures/gui/overlay/atomic_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture ATOMIC_OVERLAY_2 = fullImage("textures/gui/overlay/atomic_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture ARROW_INPUT_OVERLAY = fullImage("textures/gui/overlay/arrow_input_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture ARROW_OUTPUT_OVERLAY = fullImage("textures/gui/overlay/arrow_output_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture BATTERY_OVERLAY = fullImage("textures/gui/overlay/battery_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture BEAKER_OVERLAY_1 = fullImage("textures/gui/overlay/beaker_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture BEAKER_OVERLAY_2 = fullImage("textures/gui/overlay/beaker_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture BEAKER_OVERLAY_3 = fullImage("textures/gui/overlay/beaker_overlay_3.png",
            ColorType.DEFAULT);
    public static final UITexture BEAKER_OVERLAY_4 = fullImage("textures/gui/overlay/beaker_overlay_4.png",
            ColorType.DEFAULT);
    public static final UITexture BENDER_OVERLAY = fullImage("textures/gui/overlay/bender_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture BOX_OVERLAY = fullImage("textures/gui/overlay/box_overlay.png", ColorType.DEFAULT);
    public static final UITexture BOXED_OVERLAY = fullImage("textures/gui/overlay/boxed_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture BREWER_OVERLAY = fullImage("textures/gui/overlay/brewer_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture CANNER_OVERLAY = fullImage("textures/gui/overlay/canner_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture CHARGER_OVERLAY = fullImage("textures/gui/overlay/charger_slot_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture CANISTER_OVERLAY = fullImage("textures/gui/overlay/canister_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture CANISTER_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/canister_overlay_bronze.png");
    public static final UITexture CANISTER_OVERLAY_STEEL = fullImage("textures/gui/overlay/canister_overlay_steel.png");
    public static final UITexture CENTRIFUGE_OVERLAY = fullImage("textures/gui/overlay/centrifuge_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/circuit_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture COAL_OVERLAY_BRONZE = fullImage("textures/gui/overlay/coal_overlay_bronze.png");
    public static final UITexture COAL_OVERLAY_STEEL = fullImage("textures/gui/overlay/coal_overlay_steel.png");
    public static final UITexture COMPRESSOR_OVERLAY = fullImage("textures/gui/overlay/compressor_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture COMPRESSOR_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/compressor_overlay_bronze.png");
    public static final UITexture COMPRESSOR_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/compressor_overlay_steel.png");
    public static final UITexture CRACKING_OVERLAY_1 = fullImage("textures/gui/overlay/cracking_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture CRACKING_OVERLAY_2 = fullImage("textures/gui/overlay/cracking_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture CRUSHED_ORE_OVERLAY = fullImage("textures/gui/overlay/crushed_ore_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture CRUSHED_ORE_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/crushed_ore_overlay_bronze.png");
    public static final UITexture CRUSHED_ORE_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/crushed_ore_overlay_steel.png");
    public static final UITexture CRYSTAL_OVERLAY = fullImage("textures/gui/overlay/crystal_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture CUTTER_OVERLAY = fullImage("textures/gui/overlay/cutter_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture DARK_CANISTER_OVERLAY = fullImage("textures/gui/overlay/dark_canister_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture DUST_OVERLAY = fullImage("textures/gui/overlay/dust_overlay.png", ColorType.DEFAULT);
    public static final UITexture DUST_OVERLAY_BRONZE = fullImage("textures/gui/overlay/dust_overlay_bronze.png");
    public static final UITexture DUST_OVERLAY_STEEL = fullImage("textures/gui/overlay/dust_overlay_steel.png");
    public static final UITexture EXTRACTOR_OVERLAY = fullImage("textures/gui/overlay/extractor_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture EXTRACTOR_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/extractor_overlay_bronze.png");
    public static final UITexture EXTRACTOR_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/extractor_overlay_steel.png");
    public static final UITexture FILTER_SLOT_OVERLAY = fullImage("textures/gui/overlay/filter_slot_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture FILTER_SETTINGS_OVERLAY = fullImage(
            "textures/gui/overlay/filter_settings_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture FURNACE_OVERLAY_1 = fullImage("textures/gui/overlay/furnace_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture FURNACE_OVERLAY_2 = fullImage("textures/gui/overlay/furnace_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture FURNACE_OVERLAY_BRONZE = fullImage("textures/gui/overlay/furnace_overlay_bronze.png");
    public static final UITexture FURNACE_OVERLAY_STEEL = fullImage("textures/gui/overlay/furnace_overlay_steel.png");
    public static final UITexture HAMMER_OVERLAY = fullImage("textures/gui/overlay/hammer_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture HAMMER_OVERLAY_BRONZE = fullImage("textures/gui/overlay/hammer_overlay_bronze.png");
    public static final UITexture HAMMER_OVERLAY_STEEL = fullImage("textures/gui/overlay/hammer_overlay_steel.png");
    public static final UITexture HEATING_OVERLAY_1 = fullImage("textures/gui/overlay/heating_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture HEATING_OVERLAY_2 = fullImage("textures/gui/overlay/heating_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture IMPLOSION_OVERLAY_1 = fullImage("textures/gui/overlay/implosion_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture IMPLOSION_OVERLAY_2 = fullImage("textures/gui/overlay/implosion_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture IN_SLOT_OVERLAY = fullImage("textures/gui/overlay/in_slot_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture IN_SLOT_OVERLAY_BRONZE = fullImage("textures/gui/overlay/in_slot_overlay_bronze.png");
    public static final UITexture IN_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/in_slot_overlay_steel.png");
    public static final UITexture INGOT_OVERLAY = fullImage("textures/gui/overlay/ingot_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture INT_CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/int_circuit_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture LENS_OVERLAY = fullImage("textures/gui/overlay/lens_overlay.png", ColorType.DEFAULT);
    public static final UITexture LIGHTNING_OVERLAY_1 = fullImage("textures/gui/overlay/lightning_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture LIGHTNING_OVERLAY_2 = fullImage("textures/gui/overlay/lightning_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture MOLD_OVERLAY = fullImage("textures/gui/overlay/mold_overlay.png", ColorType.DEFAULT);
    public static final UITexture MOLECULAR_OVERLAY_1 = fullImage("textures/gui/overlay/molecular_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture MOLECULAR_OVERLAY_2 = fullImage("textures/gui/overlay/molecular_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture MOLECULAR_OVERLAY_3 = fullImage("textures/gui/overlay/molecular_overlay_3.png",
            ColorType.DEFAULT);
    public static final UITexture MOLECULAR_OVERLAY_4 = fullImage("textures/gui/overlay/molecular_overlay_4.png",
            ColorType.DEFAULT);
    public static final UITexture OUT_SLOT_OVERLAY = fullImage("textures/gui/overlay/out_slot_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture OUT_SLOT_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/out_slot_overlay_bronze.png");
    public static final UITexture OUT_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/out_slot_overlay_steel.png");
    public static final UITexture PAPER_OVERLAY = fullImage("textures/gui/overlay/paper_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture PRINTED_PAPER_OVERLAY = fullImage("textures/gui/overlay/printed_paper_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture PIPE_OVERLAY_2 = fullImage("textures/gui/overlay/pipe_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture PIPE_OVERLAY_1 = fullImage("textures/gui/overlay/pipe_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture PRESS_OVERLAY_1 = fullImage("textures/gui/overlay/press_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture PRESS_OVERLAY_2 = fullImage("textures/gui/overlay/press_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture PRESS_OVERLAY_3 = fullImage("textures/gui/overlay/press_overlay_3.png",
            ColorType.DEFAULT);
    public static final UITexture PRESS_OVERLAY_4 = fullImage("textures/gui/overlay/press_overlay_4.png",
            ColorType.DEFAULT);
    public static final UITexture SAWBLADE_OVERLAY = fullImage("textures/gui/overlay/sawblade_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture SOLIDIFIER_OVERLAY = fullImage("textures/gui/overlay/solidifier_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture STRING_SLOT_OVERLAY = fullImage("textures/gui/overlay/string_slot_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture TOOL_SLOT_OVERLAY = fullImage("textures/gui/overlay/tool_slot_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture TURBINE_OVERLAY = fullImage("textures/gui/overlay/turbine_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture VIAL_OVERLAY_1 = fullImage("textures/gui/overlay/vial_overlay_1.png",
            ColorType.DEFAULT);
    public static final UITexture VIAL_OVERLAY_2 = fullImage("textures/gui/overlay/vial_overlay_2.png",
            ColorType.DEFAULT);
    public static final UITexture WIREMILL_OVERLAY = fullImage("textures/gui/overlay/wiremill_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture POSITIVE_MATTER_OVERLAY = fullImage(
            "textures/gui/overlay/positive_matter_overlay.png", ColorType.DEFAULT);
    public static final UITexture NEUTRAL_MATTER_OVERLAY = fullImage("textures/gui/overlay/neutral_matter_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture DATA_ORB_OVERLAY = fullImage("textures/gui/overlay/data_orb_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture SCANNER_OVERLAY = fullImage("textures/gui/overlay/scanner_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture DUCT_TAPE_OVERLAY = fullImage("textures/gui/overlay/duct_tape_overlay.png",
            ColorType.DEFAULT);
    public static final UITexture RESEARCH_STATION_OVERLAY = fullImage(
            "textures/gui/overlay/research_station_overlay.png", ColorType.DEFAULT);
    public static final UITexture OVERLAY_REDSTONE_ON = fullImage("textures/gui/overlay/redstone_on.png");
    public static final UITexture OVERLAY_REDSTONE_OFF = fullImage("textures/gui/overlay/redstone_off.png");

    // BUTTONS

    public static final UITexture BUTTON = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/button.png")
            .imageSize(32, 32)
            .adaptable(2)
            .name(IDs.STANDARD_BUTTON)
            .canApplyTheme()
            .build();

    // BUTTON OVERLAYS

    public static final UITexture BUTTON_ITEM_OUTPUT = fullImage("textures/gui/widget/button_item_output_overlay.png");
    public static final UITexture BUTTON_FLUID_OUTPUT = fullImage(
            "textures/gui/widget/button_fluid_output_overlay.png");
    public static final UITexture BUTTON_AUTO_COLLAPSE = fullImage(
            "textures/gui/widget/button_auto_collapse_overlay.png");
    public static final UITexture BUTTON_X = fullImage("textures/gui/widget/button_x_overlay.png", ColorType.DEFAULT);
    public static final UITexture BUTTON_CLEAR_GRID = fullImage("textures/gui/widget/button_clear_grid.png", null);

    public static final UITexture BUTTON_CROSS = fullImage("textures/gui/widget/button_clear_grid.png");
    public static final UITexture BUTTON_REDSTONE_ON = fullImage("textures/gui/widget/button_redstone_on.png");
    public static final UITexture BUTTON_REDSTONE_OFF = fullImage("textures/gui/widget/button_redstone_off.png");
    public static final UITexture BUTTON_THROTTLE_PLUS = fullImage("textures/gui/widget/button_throttle_plus.png");
    public static final UITexture BUTTON_THROTTLE_MINUS = fullImage("textures/gui/widget/button_throttle_minus.png");
    public static final UITexture BUTTON_EU = fullImage("textures/gui/overlay/mode_eu.png");
    public static final UITexture BUTTON_PERCENT = fullImage("textures/gui/overlay/mode_percent.png");
    public static final UITexture BUTTON_MAINTENANCE = fullImage("textures/gui/widget/button_maintenance.png");

    public static final UITexture BUTTON_DISTINCT = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/button_distinct_buses.png")
            .subAreaXYWH(0, 0, 16, 16)
            .build();

    // PROGRESS BARS
    public static final UITexture PROGRESS_BAR_ARC_FURNACE = progressBar(
            "textures/gui/progress_bar/progress_bar_arc_furnace.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_ARROW = progressBar("textures/gui/progress_bar/progress_bar_arrow.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_ARROW_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_arrow_bronze.png");
    public static final UITexture PROGRESS_BAR_ARROW_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_arrow_steel.png");
    public static final UITexture PROGRESS_BAR_ARROW_MULTIPLE = progressBar(
            "textures/gui/progress_bar/progress_bar_arrow_multiple.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_ASSEMBLER = progressBar(
            "textures/gui/progress_bar/progress_bar_assembler.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_BATH = progressBar("textures/gui/progress_bar/progress_bar_bath.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_BENDING = progressBar(
            "textures/gui/progress_bar/progress_bar_bending.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_CANNER = progressBar("textures/gui/progress_bar/progress_bar_canner.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_CIRCUIT = progressBar(
            "textures/gui/progress_bar/progress_bar_circuit.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_CIRCUIT_ASSEMBLER = progressBar(
            "textures/gui/progress_bar/progress_bar_circuit_assembler.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_COMPRESS = progressBar(
            "textures/gui/progress_bar/progress_bar_compress.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_COMPRESS_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_compress_bronze.png");
    public static final UITexture PROGRESS_BAR_COMPRESS_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_compress_steel.png");
    public static final UITexture PROGRESS_BAR_CRACKING = progressBar(
            "textures/gui/progress_bar/progress_bar_cracking.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_CRACKING_INPUT = progressBar(
            "textures/gui/progress_bar/progress_bar_cracking_2.png", 21, 38, ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_CRYSTALLIZATION = progressBar(
            "textures/gui/progress_bar/progress_bar_crystallization.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_EXTRACT = progressBar(
            "textures/gui/progress_bar/progress_bar_extract.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_EXTRACT_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_extract_bronze.png");
    public static final UITexture PROGRESS_BAR_EXTRACT_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_extract_steel.png");
    public static final UITexture PROGRESS_BAR_EXTRUDER = progressBar(
            "textures/gui/progress_bar/progress_bar_extruder.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_FUSION = progressBar("textures/gui/progress_bar/progress_bar_fusion.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_GAS_COLLECTOR = progressBar(
            "textures/gui/progress_bar/progress_bar_gas_collector.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_HAMMER = progressBar("textures/gui/progress_bar/progress_bar_hammer.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_HAMMER_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_hammer_bronze.png");
    public static final UITexture PROGRESS_BAR_HAMMER_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_hammer_steel.png");
    public static final UITexture PROGRESS_BAR_HAMMER_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_hammer_base.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_HAMMER_BASE_BRONZE = fullImage(
            "textures/gui/progress_bar/progress_bar_hammer_base_bronze.png");
    public static final UITexture PROGRESS_BAR_HAMMER_BASE_STEEL = fullImage(
            "textures/gui/progress_bar/progress_bar_hammer_base_steel.png");
    public static final UITexture PROGRESS_BAR_LATHE = progressBar("textures/gui/progress_bar/progress_bar_lathe.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_LATHE_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_lathe_base.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_MACERATE = progressBar(
            "textures/gui/progress_bar/progress_bar_macerate.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_MACERATE_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_macerate_bronze.png");
    public static final UITexture PROGRESS_BAR_MACERATE_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_macerate_steel.png");
    public static final UITexture PROGRESS_BAR_MAGNET = progressBar("textures/gui/progress_bar/progress_bar_magnet.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_MASS_FAB = progressBar(
            "textures/gui/progress_bar/progress_bar_mass_fab.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_MIXER = progressBar("textures/gui/progress_bar/progress_bar_mixer.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_PACKER = progressBar("textures/gui/progress_bar/progress_bar_packer.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_RECYCLER = progressBar(
            "textures/gui/progress_bar/progress_bar_recycler.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_REPLICATOR = progressBar(
            "textures/gui/progress_bar/progress_bar_replicator.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_SIFT = progressBar("textures/gui/progress_bar/progress_bar_sift.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_SLICE = progressBar("textures/gui/progress_bar/progress_bar_slice.png",
            ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_UNPACKER = progressBar(
            "textures/gui/progress_bar/progress_bar_unpacker.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_WIREMILL = progressBar(
            "textures/gui/progress_bar/progress_bar_wiremill.png", ColorType.DEFAULT);

    // more custom progress bars
    // todo these boiler empty bars can probably be replaced by using a resized steam slot texture
    public static final UITexture PROGRESS_BAR_BOILER_EMPTY_BRONZE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/progress_bar/progress_bar_boiler_empty_bronze.png")
            .imageSize(10, 54)
            .adaptable(1)
            .build();
    public static final UITexture PROGRESS_BAR_BOILER_EMPTY_STEEL = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/progress_bar/progress_bar_boiler_empty_steel.png")
            .imageSize(10, 54)
            .adaptable(1)
            .build();
    public static final UITexture PROGRESS_BAR_BOILER_FUEL_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_boiler_fuel_bronze.png", 18, 36);
    public static final UITexture PROGRESS_BAR_BOILER_FUEL_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_boiler_fuel_steel.png", 18, 36);
    public static final UITexture PROGRESS_BAR_BOILER_HEAT = progressBar(
            "textures/gui/progress_bar/progress_bar_boiler_heat.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_ASSEMBLY_LINE = progressBar(
            "textures/gui/progress_bar/progress_bar_assembly_line.png", 54, 144, ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_ASSEMBLY_LINE_ARROW = progressBar(
            "textures/gui/progress_bar/progress_bar_assembly_line_arrow.png", 10, 36, ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_COKE_OVEN = progressBar(
            "textures/gui/progress_bar/progress_bar_coke_oven.png", 36, 36, ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_DISTILLATION_TOWER = progressBar(
            "textures/gui/progress_bar/progress_bar_distillation_tower.png", 66, 116, ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_SOLAR_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_solar_bronze.png", 10, 20);
    public static final UITexture PROGRESS_BAR_SOLAR_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_solar_steel.png", 10, 20);
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_1 = progressBar(
            "textures/gui/progress_bar/progress_bar_research_station_1.png", 54, 10, ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_2 = progressBar(
            "textures/gui/progress_bar/progress_bar_research_station_2.png", 10, 36, ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_research_station_base.png", ColorType.DEFAULT);
    public static final UITexture PROGRESS_BAR_FUSION_ENERGY = progressBar(
            "textures/gui/progress_bar/progress_bar_fusion_energy.png", 94, 14);
    public static final UITexture PROGRESS_BAR_FUSION_HEAT = progressBar(
            "textures/gui/progress_bar/progress_bar_fusion_heat.png", 94, 14);
    public static final UITexture PROGRESS_BAR_MULTI_ENERGY_YELLOW = progressBar(
            "textures/gui/progress_bar/progress_bar_multi_energy_yellow.png", 190, 14);
    public static final UITexture PROGRESS_BAR_HPCA_COMPUTATION = progressBar(
            "textures/gui/progress_bar/progress_bar_hpca_computation.png", 94, 14);
    public static final UITexture PROGRESS_BAR_LCE_FUEL = progressBar(
            "textures/gui/progress_bar/progress_bar_lce_fuel.png", 62, 14);
    public static final UITexture PROGRESS_BAR_LCE_LUBRICANT = progressBar(
            "textures/gui/progress_bar/progress_bar_lce_lubricant.png", 62, 14);
    public static final UITexture PROGRESS_BAR_LCE_OXYGEN = progressBar(
            "textures/gui/progress_bar/progress_bar_lce_oxygen.png", 62, 14);
    public static final UITexture PROGRESS_BAR_TURBINE_ROTOR_SPEED = progressBar(
            "textures/gui/progress_bar/progress_bar_turbine_rotor_speed.png", 62, 14);
    public static final UITexture PROGRESS_BAR_TURBINE_ROTOR_DURABILITY = progressBar(
            "textures/gui/progress_bar/progress_bar_turbine_rotor_durability.png", 62, 14);
    public static final UITexture PROGRESS_BAR_FLUID_RIG_DEPLETION = progressBar(
            "textures/gui/progress_bar/progress_bar_fluid_rig_depletion.png", 190, 14);

    // BASE MUI2

    public static final UITexture GEAR = fullImage("textures/gui/icon/mui2/gear.png");
    public static final UITexture MORE = fullImage("textures/gui/icon/mui2/more.png");
    public static final UITexture SAVED = fullImage("textures/gui/icon/mui2/saved.png");
    public static final UITexture SAVE = fullImage("textures/gui/icon/mui2/save.png");
    public static final UITexture ADD = fullImage("textures/gui/icon/mui2/add.png");
    public static final UITexture DUPE = fullImage("textures/gui/icon/mui2/dupe.png");
    public static final UITexture REMOVE = fullImage("textures/gui/icon/mui2/remove.png");
    public static final UITexture POSE = fullImage("textures/gui/icon/mui2/pose.png");
    public static final UITexture FILTER = fullImage("textures/gui/icon/mui2/filter.png");
    public static final UITexture MOVE_UP = fullImage("textures/gui/icon/mui2/move_up.png");
    public static final UITexture MOVE_DOWN = fullImage("textures/gui/icon/mui2/move_down.png");
    public static final UITexture LOCKED = fullImage("textures/gui/icon/mui2/locked.png");
    public static final UITexture UNLOCKED = fullImage("textures/gui/icon/mui2/unlocked.png");
    public static final UITexture COPY = fullImage("textures/gui/icon/mui2/copy.png");
    public static final UITexture PASTE = fullImage("textures/gui/icon/mui2/paste.png");
    public static final UITexture CUT = fullImage("textures/gui/icon/mui2/cut.png");
    public static final UITexture REFRESH = fullImage("textures/gui/icon/mui2/refresh.png");

    public static final UITexture DOWNLOAD = fullImage("textures/gui/icon/mui2/download.png");
    public static final UITexture UPLOAD = fullImage("textures/gui/icon/mui2/upload.png");
    public static final UITexture SERVER = fullImage("textures/gui/icon/mui2/server.png");
    public static final UITexture FOLDER = fullImage("textures/gui/icon/mui2/folder.png");
    public static final UITexture IMAGE = fullImage("textures/gui/icon/mui2/image.png");
    public static final UITexture EDIT = fullImage("textures/gui/icon/mui2/edit.png");
    public static final UITexture MATERIAL = fullImage("textures/gui/icon/mui2/material.png");
    public static final UITexture CLOSE = fullImage("textures/gui/icon/mui2/close.png");
    public static final UITexture LIMB = fullImage("textures/gui/icon/mui2/limb.png");
    public static final UITexture CODE = fullImage("textures/gui/icon/mui2/code.png");
    public static final UITexture MOVE_LEFT = fullImage("textures/gui/icon/mui2/move_left.png");
    public static final UITexture MOVE_RIGHT = fullImage("textures/gui/icon/mui2/move_right.png");
    public static final UITexture HELP = fullImage("textures/gui/icon/mui2/help.png");
    public static final UITexture LEFT_HANDLE = fullImage("textures/gui/icon/mui2/left_handle.png");
    public static final UITexture MAIN_HANDLE = fullImage("textures/gui/icon/mui2/main_handle.png");
    public static final UITexture RIGHT_HANDLE = fullImage("textures/gui/icon/mui2/right_handle.png");
    public static final UITexture REVERSE = fullImage("textures/gui/icon/mui2/reverse.png");
    public static final UITexture BLOCK = fullImage("textures/gui/icon/mui2/block.png");

    public static final UITexture FAVORITE = fullImage("textures/gui/icon/mui2/favorite.png");
    public static final UITexture VISIBLE = fullImage("textures/gui/icon/mui2/visible.png");
    public static final UITexture INVISIBLE = fullImage("textures/gui/icon/mui2/invisible.png");
    public static final UITexture PLAY = fullImage("textures/gui/icon/mui2/play.png");
    public static final UITexture PAUSE = fullImage("textures/gui/icon/mui2/pause.png");
    public static final UITexture MAXIMIZE = fullImage("textures/gui/icon/mui2/maximize.png");
    public static final UITexture MINIMIZE = fullImage("textures/gui/icon/mui2/minimize.png");
    public static final UITexture STOP = fullImage("textures/gui/icon/mui2/stop.png");
    public static final UITexture FULLSCREEN = fullImage("textures/gui/icon/mui2/fullscreen.png");
    public static final UITexture ALL_DIRECTIONS = fullImage("textures/gui/icon/mui2/all_directions.png");
    public static final UITexture SPHERE = fullImage("textures/gui/icon/mui2/sphere.png");
    public static final UITexture SHIFT_TO = fullImage("textures/gui/icon/mui2/shift_to.png");
    public static final UITexture SHIFT_FORWARD = fullImage("textures/gui/icon/mui2/shift_forward.png");
    public static final UITexture SHIFT_BACKWARD = fullImage("textures/gui/icon/mui2/shift_backward.png");
    public static final UITexture MOVE_TO = fullImage("textures/gui/icon/mui2/move_to.png");
    public static final UITexture GRAPH = fullImage("textures/gui/icon/mui2/graph.png");

    public static final UITexture WRENCH = fullImage("textures/gui/icon/mui2/wrench.png");
    public static final UITexture EXCLAMATION = fullImage("textures/gui/icon/mui2/exclamation.png");
    public static final UITexture LEFTLOAD = fullImage("textures/gui/icon/mui2/leftload.png");
    public static final UITexture RIGHTLOAD = fullImage("textures/gui/icon/mui2/rightload.png");
    public static final UITexture BUBBLE = fullImage("textures/gui/icon/mui2/bubble.png");
    public static final UITexture FILE = fullImage("textures/gui/icon/mui2/file.png");
    public static final UITexture PROCESSOR = fullImage("textures/gui/icon/mui2/processor.png");
    public static final UITexture MAZE = fullImage("textures/gui/icon/mui2/maze.png");
    public static final UITexture BOOKMARK = fullImage("textures/gui/icon/mui2/bookmark.png");
    public static final UITexture SOUND = fullImage("textures/gui/icon/mui2/sound.png");
    public static final UITexture SEARCH = fullImage("textures/gui/icon/mui2/search.png");

    public static final UITexture CHECKBOARD = fullImage("textures/gui/icon/mui2/checkboard.png");
    public static final UITexture DISABLED = fullImage("textures/gui/icon/mui2/disabled.png");
    public static final UITexture CURSOR = fullImage("textures/gui/icon/mui2/cursor.png");

    public static final UITexture MUI_LOGO = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/icon/modular_ui_logo.png")
            .imageSize(603, 603)
            .name("logo")
            .build();

    public static final UITexture MC_BUTTON = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/mc_button.png")
            .imageSize(16, 32) // texture is 32x64, but this looks nicer
            .subAreaUV(0f, 0f, 1f, 0.5f)
            .adaptable(2).tiled()
            .name("mc_button")
            .defaultColorType()
            .build();

    public static final UITexture CYCLE_BUTTON = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/button_distribution_mode.png")
            .imageSize(20, 60)
            .name("cycle")
            .build();
    // 16, 48, 16, 16, true);

    public static final UITexture MC_BUTTON_PRESSED = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/mc_button.png")
            .imageSize(16, 32)
            .subAreaUV(0f, 0.5f, 1f, 1f)
            .adaptable(2).tiled()
            .name("mc_button_pressed")
            .defaultColorType()
            .build();

    public static final UITexture MC_BUTTON_HOVERED = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/mc_button_hovered.png")
            .imageSize(16, 32)
            .subAreaUV(0f, 0f, 1f, 0.5f)
            .adaptable(2).tiled()
            .name("mc_button_hovered")
            .build();

    public static final UITexture MC_BUTTON_HOVERED_PRESSED = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/mc_button_hovered.png")
            .imageSize(16, 32)
            .subAreaUV(0f, 0.5f, 1f, 1f)
            .adaptable(2).tiled()
            .name("mc_button_hovered_pressed")
            .build();

    public static final UITexture MC_BUTTON_DISABLED = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/mc_button_disabled.png")
            .imageSize(16, 16)
            .subAreaUV(0f, 0f, 1f, 0.5f)
            .adaptable(1).tiled()
            .name("mc_button_disabled")
            .build();

    public static final UITexture MUI_DISPLAY = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/mui_display.png")
            .imageSize(143, 75)
            .adaptable(2)
            .name("display")
            .build();

    public static final UITexture MUI_DISPLAY_SMALL = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/mui_display_small.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name("display_small")
            .build();

    public static final UITexture CHECK_BOX = fullImage("textures/gui/icon/mui2/toggle_config.png");
    public static final UITexture CROSS = fullImage("textures/gui/icon/mui2/cross.png");
    public static final UITexture CROSS_TINY = fullImage("textures/gui/icon/mui2/cross_tiny.png");

    public static final TabTexture TAB_TOP = TabTexture.of(
            fullImage("textures/gui/tab/tabs_top.png", ColorType.DEFAULT), GuiAxis.Y,
            false,
            28, 32, 4);
    public static final TabTexture TAB_BOTTOM = TabTexture.of(
            fullImage("textures/gui/tab/tabs_bottom.png", ColorType.DEFAULT),
            GuiAxis.Y,
            true, 28, 32, 4);
    public static final TabTexture TAB_LEFT = TabTexture.of(
            fullImage("textures/gui/tab/tabs_left.png", ColorType.DEFAULT),
            GuiAxis.X, false,
            32, 28, 4);
    public static final TabTexture TAB_RIGHT = TabTexture.of(
            fullImage("textures/gui/tab/tabs_right.png", ColorType.DEFAULT),
            GuiAxis.X, true,
            32, 28, 4);

    // MISC

    public static void init() {/**/}

    private static UITexture fullImage(String path) {
        return fullImage(path, null);
    }

    private static UITexture fullImage(String path, ColorType colorType) {
        return UITexture.fullImage(GTCEu.MOD_ID, path, colorType);
    }

    @SuppressWarnings("SameParameterValue")
    private static UITexture[] slice(String path, int imageWidth, int imageHeight, int sliceWidth, int sliceHeight,
                                     ColorType colorType) {
        if (imageWidth % sliceWidth != 0 || imageHeight % sliceHeight != 0)
            throw new IllegalArgumentException("Slice height and slice width must divide the image evenly!");

        int countX = imageWidth / sliceWidth;
        int countY = imageHeight / sliceHeight;
        UITexture[] slices = new UITexture[countX * countY];

        for (int indexX = 0; indexX < countX; indexX++) {
            for (int indexY = 0; indexY < countY; indexY++) {
                slices[(indexX * countX) + indexY] = UITexture.builder()
                        .location(GTCEu.MOD_ID, path)
                        .imageSize(imageWidth, imageHeight)
                        .colorType(colorType)
                        .subAreaXYWH(indexX * sliceWidth, indexY * sliceHeight, sliceWidth, sliceHeight)
                        .build();
            }
        }
        return slices;
    }

    private static UITexture progressBar(String path) {
        return progressBar(path, null);
    }

    private static UITexture progressBar(String path, ColorType colorType) {
        return progressBar(path, 20, 40, colorType);
    }

    private static UITexture progressBar(String path, int width, int height) {
        return progressBar(path, width, height, null);
    }

    private static UITexture progressBar(String path, int width, int height, ColorType colorType) {
        UITexture.Builder builder = new UITexture.Builder()
                .location(GTCEu.MOD_ID, path)
                .imageSize(width, height)
                .colorType(colorType);
        return builder.build();
    }

    // todo steam logos? multi indicator blinking logos?
    public static @NotNull UITexture getLogo(GTGuiTheme theme) {
        if (theme != null) {
            UITexture logo = theme.getLogo();
            if (logo != null) return logo;
        }
        return GTValues.XMAS.getAsBoolean() ? GREGTECH_LOGO_XMAS : GREGTECH_LOGO;
    }
}
