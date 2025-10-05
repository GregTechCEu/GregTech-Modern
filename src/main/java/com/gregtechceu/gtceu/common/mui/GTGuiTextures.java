package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import org.jetbrains.annotations.NotNull;

public class GTGuiTextures {

    /** Keys used for GT assets registered for use in Themes */
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gregtech_standard_bg";
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
            .imageSize(176, 166)
            .adaptable(3)
            .name(IDs.STANDARD_BACKGROUND)
            .canApplyTheme()
            .build();

    public static final UITexture BACKGROUND_POPUP = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_popup.png")
            .imageSize(195, 136)
            .adaptable(4)
            .name(IDs.COVER_BACKGROUND)
            .canApplyTheme()
            .build();

    // todo BORDERED/BOXED backgrounds will not be ported, if possible

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
            .imageSize(143, 75)
            .adaptable(2)
            .canApplyTheme()
            .build();

    public static final UITexture DISPLAY_BRONZE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display_bronze.png")
            .imageSize(143, 75)
            .adaptable(2)
            .build();

    public static final UITexture DISPLAY_STEEL = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display_steel.png")
            .imageSize(143, 75)
            .adaptable(2)
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

    public static final UITexture[] BUTTON_BLACKLIST = slice("textures/gui/widget/button_blacklist.png",
            16, 32, 16, 16, true);
    public static final UITexture[] BUTTON_IGNORE_DAMAGE = slice("textures/gui/widget/button_filter_damage.png",
            16, 32, 16, 16, true);
    public static final UITexture[] BUTTON_IGNORE_NBT = slice("textures/gui/widget/button_filter_nbt.png",
            16, 32, 16, 16, true);

    public static final UITexture[] BUTTON_CASE_SENSITIVE = slice(
            "textures/gui/widget/ore_filter/button_case_sensitive.png",
            16, 32, 16, 16, true);

    public static final UITexture[] BUTTON_MATCH_ALL = slice("textures/gui/widget/ore_filter/button_match_all.png",
            16, 32, 16, 16, true);
    public static final UITexture BUTTON_LOCK = fullImage("textures/gui/widget/button_lock.png");

    public static final UITexture OREDICT_ERROR = fullImage("textures/gui/widget/ore_filter/error.png");
    public static final UITexture OREDICT_INFO = fullImage("textures/gui/widget/ore_filter/info.png");
    public static final UITexture OREDICT_MATCH = fullImage("textures/gui/widget/ore_filter/match.png");
    public static final UITexture OREDICT_NO_MATCH = fullImage("textures/gui/widget/ore_filter/no_match.png");
    public static final UITexture OREDICT_SUCCESS = fullImage("textures/gui/widget/ore_filter/success.png");
    public static final UITexture OREDICT_WAITING = fullImage("textures/gui/widget/ore_filter/waiting.png");
    public static final UITexture OREDICT_WARN = fullImage("textures/gui/widget/ore_filter/warn.png");

    public static final IDrawable PLUS = IKey.str("+").asIcon().marginLeft(1);
    public static final IDrawable MINUS = IKey.str("-").asIcon().marginLeft(1);

    public static final UITexture[] MANUAL_IO_OVERLAY_IN = slice("textures/gui/overlay/manual_io_overlay_in.png",
            18, 18 * 3, 18, 18, true);
    public static final UITexture[] MANUAL_IO_OVERLAY_OUT = slice("textures/gui/overlay/manual_io_overlay_out.png",
            18, 18 * 3, 18, 18, true);
    public static final UITexture[] CONVEYOR_MODE_OVERLAY = slice("textures/gui/overlay/conveyor_mode_overlay.png",
            18, 18 * 2, 18, 18, true);

    public static final UITexture[] TRANSFER_MODE_OVERLAY = slice("textures/gui/overlay/transfer_mode_overlay.png",
            18, 18 * 3, 18, 18, true);

    public static final UITexture[] FLUID_TRANSFER_MODE_OVERLAY = slice(
            "textures/gui/overlay/fluid_transfer_mode_overlay.png",
            18, 18 * 3, 18, 18, true);

    public static final UITexture[] DISTRIBUTION_MODE_OVERLAY = slice(
            "textures/gui/widget/button_distribution_mode.png",
            16, 48, 16, 16, true);

    public static final UITexture[] VOIDING_MODE_OVERLAY = slice(
            "textures/gui/overlay/voiding_mode_overlay.png",
            16, 32, 16, 16, true);

    public static final UITexture[] FILTER_MODE_OVERLAY = slice(
            "textures/gui/overlay/filter_mode_overlay.png",
            16, 48, 16, 16, true);

    public static final UITexture[] PRIVATE_MODE_BUTTON = slice(
            "textures/gui/widget/button_public_private.png",
            18, 36, 18, 18, true);

    public static final UITexture MENU_OVERLAY = fullImage("textures/gui/overlay/menu_overlay.png");

    public static final UITexture RECIPE_LOCK = fullImage("textures/gui/widget/lock.png");

    // todo bronze/steel/primitive fluid slots?

    // SLOT OVERLAYS
    public static final UITexture ATOMIC_OVERLAY_1 = fullImage("textures/gui/overlay/atomic_overlay_1.png", true);
    public static final UITexture ATOMIC_OVERLAY_2 = fullImage("textures/gui/overlay/atomic_overlay_2.png", true);
    public static final UITexture ARROW_INPUT_OVERLAY = fullImage("textures/gui/overlay/arrow_input_overlay.png", true);
    public static final UITexture ARROW_OUTPUT_OVERLAY = fullImage("textures/gui/overlay/arrow_output_overlay.png",
            true);
    public static final UITexture BATTERY_OVERLAY = fullImage("textures/gui/overlay/battery_overlay.png", true);
    public static final UITexture BEAKER_OVERLAY_1 = fullImage("textures/gui/overlay/beaker_overlay_1.png", true);
    public static final UITexture BEAKER_OVERLAY_2 = fullImage("textures/gui/overlay/beaker_overlay_2.png", true);
    public static final UITexture BEAKER_OVERLAY_3 = fullImage("textures/gui/overlay/beaker_overlay_3.png", true);
    public static final UITexture BEAKER_OVERLAY_4 = fullImage("textures/gui/overlay/beaker_overlay_4.png", true);
    public static final UITexture BENDER_OVERLAY = fullImage("textures/gui/overlay/bender_overlay.png", true);
    public static final UITexture BOX_OVERLAY = fullImage("textures/gui/overlay/box_overlay.png", true);
    public static final UITexture BOXED_OVERLAY = fullImage("textures/gui/overlay/boxed_overlay.png", true);
    public static final UITexture BREWER_OVERLAY = fullImage("textures/gui/overlay/brewer_overlay.png", true);
    public static final UITexture CANNER_OVERLAY = fullImage("textures/gui/overlay/canner_overlay.png", true);
    public static final UITexture CHARGER_OVERLAY = fullImage("textures/gui/overlay/charger_slot_overlay.png", true);
    public static final UITexture CANISTER_OVERLAY = fullImage("textures/gui/overlay/canister_overlay.png", true);
    public static final UITexture CANISTER_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/canister_overlay_bronze.png");
    public static final UITexture CANISTER_OVERLAY_STEEL = fullImage("textures/gui/overlay/canister_overlay_steel.png");
    public static final UITexture CENTRIFUGE_OVERLAY = fullImage("textures/gui/overlay/centrifuge_overlay.png", true);
    public static final UITexture CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/circuit_overlay.png", true);
    public static final UITexture COAL_OVERLAY_BRONZE = fullImage("textures/gui/overlay/coal_overlay_bronze.png");
    public static final UITexture COAL_OVERLAY_STEEL = fullImage("textures/gui/overlay/coal_overlay_steel.png");
    public static final UITexture COMPRESSOR_OVERLAY = fullImage("textures/gui/overlay/compressor_overlay.png", true);
    public static final UITexture COMPRESSOR_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/compressor_overlay_bronze.png");
    public static final UITexture COMPRESSOR_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/compressor_overlay_steel.png");
    public static final UITexture CRACKING_OVERLAY_1 = fullImage("textures/gui/overlay/cracking_overlay_1.png", true);
    public static final UITexture CRACKING_OVERLAY_2 = fullImage("textures/gui/overlay/cracking_overlay_2.png", true);
    public static final UITexture CRUSHED_ORE_OVERLAY = fullImage("textures/gui/overlay/crushed_ore_overlay.png", true);
    public static final UITexture CRUSHED_ORE_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/crushed_ore_overlay_bronze.png");
    public static final UITexture CRUSHED_ORE_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/crushed_ore_overlay_steel.png");
    public static final UITexture CRYSTAL_OVERLAY = fullImage("textures/gui/overlay/crystal_overlay.png", true);
    public static final UITexture CUTTER_OVERLAY = fullImage("textures/gui/overlay/cutter_overlay.png", true);
    public static final UITexture DARK_CANISTER_OVERLAY = fullImage("textures/gui/overlay/dark_canister_overlay.png",
            true);
    public static final UITexture DUST_OVERLAY = fullImage("textures/gui/overlay/dust_overlay.png", true);
    public static final UITexture DUST_OVERLAY_BRONZE = fullImage("textures/gui/overlay/dust_overlay_bronze.png");
    public static final UITexture DUST_OVERLAY_STEEL = fullImage("textures/gui/overlay/dust_overlay_steel.png");
    public static final UITexture EXTRACTOR_OVERLAY = fullImage("textures/gui/overlay/extractor_overlay.png", true);
    public static final UITexture EXTRACTOR_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/extractor_overlay_bronze.png");
    public static final UITexture EXTRACTOR_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/extractor_overlay_steel.png");
    public static final UITexture FILTER_SLOT_OVERLAY = fullImage("textures/gui/overlay/filter_slot_overlay.png", true);
    public static final UITexture FILTER_SETTINGS_OVERLAY = fullImage(
            "textures/gui/overlay/filter_settings_overlay.png",
            true);
    public static final UITexture FURNACE_OVERLAY_1 = fullImage("textures/gui/overlay/furnace_overlay_1.png", true);
    public static final UITexture FURNACE_OVERLAY_2 = fullImage("textures/gui/overlay/furnace_overlay_2.png", true);
    public static final UITexture FURNACE_OVERLAY_BRONZE = fullImage("textures/gui/overlay/furnace_overlay_bronze.png");
    public static final UITexture FURNACE_OVERLAY_STEEL = fullImage("textures/gui/overlay/furnace_overlay_steel.png");
    public static final UITexture HAMMER_OVERLAY = fullImage("textures/gui/overlay/hammer_overlay.png", true);
    public static final UITexture HAMMER_OVERLAY_BRONZE = fullImage("textures/gui/overlay/hammer_overlay_bronze.png");
    public static final UITexture HAMMER_OVERLAY_STEEL = fullImage("textures/gui/overlay/hammer_overlay_steel.png");
    public static final UITexture HEATING_OVERLAY_1 = fullImage("textures/gui/overlay/heating_overlay_1.png", true);
    public static final UITexture HEATING_OVERLAY_2 = fullImage("textures/gui/overlay/heating_overlay_2.png", true);
    public static final UITexture IMPLOSION_OVERLAY_1 = fullImage("textures/gui/overlay/implosion_overlay_1.png", true);
    public static final UITexture IMPLOSION_OVERLAY_2 = fullImage("textures/gui/overlay/implosion_overlay_2.png", true);
    public static final UITexture IN_SLOT_OVERLAY = fullImage("textures/gui/overlay/in_slot_overlay.png", true);
    public static final UITexture IN_SLOT_OVERLAY_BRONZE = fullImage("textures/gui/overlay/in_slot_overlay_bronze.png");
    public static final UITexture IN_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/in_slot_overlay_steel.png");
    public static final UITexture INGOT_OVERLAY = fullImage("textures/gui/overlay/ingot_overlay.png", true);
    public static final UITexture INT_CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/int_circuit_overlay.png", true);
    public static final UITexture LENS_OVERLAY = fullImage("textures/gui/overlay/lens_overlay.png", true);
    public static final UITexture LIGHTNING_OVERLAY_1 = fullImage("textures/gui/overlay/lightning_overlay_1.png", true);
    public static final UITexture LIGHTNING_OVERLAY_2 = fullImage("textures/gui/overlay/lightning_overlay_2.png", true);
    public static final UITexture MOLD_OVERLAY = fullImage("textures/gui/overlay/mold_overlay.png", true);
    public static final UITexture MOLECULAR_OVERLAY_1 = fullImage("textures/gui/overlay/molecular_overlay_1.png", true);
    public static final UITexture MOLECULAR_OVERLAY_2 = fullImage("textures/gui/overlay/molecular_overlay_2.png", true);
    public static final UITexture MOLECULAR_OVERLAY_3 = fullImage("textures/gui/overlay/molecular_overlay_3.png", true);
    public static final UITexture MOLECULAR_OVERLAY_4 = fullImage("textures/gui/overlay/molecular_overlay_4.png", true);
    public static final UITexture OUT_SLOT_OVERLAY = fullImage("textures/gui/overlay/out_slot_overlay.png", true);
    public static final UITexture OUT_SLOT_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/out_slot_overlay_bronze.png");
    public static final UITexture OUT_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/out_slot_overlay_steel.png");
    public static final UITexture PAPER_OVERLAY = fullImage("textures/gui/overlay/paper_overlay.png", true);
    public static final UITexture PRINTED_PAPER_OVERLAY = fullImage("textures/gui/overlay/printed_paper_overlay.png",
            true);
    public static final UITexture PIPE_OVERLAY_2 = fullImage("textures/gui/overlay/pipe_overlay_2.png", true);
    public static final UITexture PIPE_OVERLAY_1 = fullImage("textures/gui/overlay/pipe_overlay_1.png", true);
    public static final UITexture PRESS_OVERLAY_1 = fullImage("textures/gui/overlay/press_overlay_1.png", true);
    public static final UITexture PRESS_OVERLAY_2 = fullImage("textures/gui/overlay/press_overlay_2.png", true);
    public static final UITexture PRESS_OVERLAY_3 = fullImage("textures/gui/overlay/press_overlay_3.png", true);
    public static final UITexture PRESS_OVERLAY_4 = fullImage("textures/gui/overlay/press_overlay_4.png", true);
    public static final UITexture SAWBLADE_OVERLAY = fullImage("textures/gui/overlay/sawblade_overlay.png", true);
    public static final UITexture SOLIDIFIER_OVERLAY = fullImage("textures/gui/overlay/solidifier_overlay.png", true);
    public static final UITexture STRING_SLOT_OVERLAY = fullImage("textures/gui/overlay/string_slot_overlay.png", true);
    public static final UITexture TOOL_SLOT_OVERLAY = fullImage("textures/gui/overlay/tool_slot_overlay.png", true);
    public static final UITexture TURBINE_OVERLAY = fullImage("textures/gui/overlay/turbine_overlay.png", true);
    public static final UITexture VIAL_OVERLAY_1 = fullImage("textures/gui/overlay/vial_overlay_1.png", true);
    public static final UITexture VIAL_OVERLAY_2 = fullImage("textures/gui/overlay/vial_overlay_2.png", true);
    public static final UITexture WIREMILL_OVERLAY = fullImage("textures/gui/overlay/wiremill_overlay.png", true);
    public static final UITexture POSITIVE_MATTER_OVERLAY = fullImage(
            "textures/gui/overlay/positive_matter_overlay.png", true);
    public static final UITexture NEUTRAL_MATTER_OVERLAY = fullImage("textures/gui/overlay/neutral_matter_overlay.png",
            true);
    public static final UITexture DATA_ORB_OVERLAY = fullImage("textures/gui/overlay/data_orb_overlay.png", true);
    public static final UITexture SCANNER_OVERLAY = fullImage("textures/gui/overlay/scanner_overlay.png", true);
    public static final UITexture DUCT_TAPE_OVERLAY = fullImage("textures/gui/overlay/duct_tape_overlay.png", true);
    public static final UITexture RESEARCH_STATION_OVERLAY = fullImage(
            "textures/gui/overlay/research_station_overlay.png", true);

    // BUTTONS

    public static final UITexture BUTTON = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/button.png")
            .imageSize(18, 18)
            .adaptable(2)
            .name(IDs.STANDARD_BUTTON)
            .canApplyTheme()
            .build();

    public static final UITexture MC_BUTTON = new UITexture.Builder()
            .location("modularui", "gui/widgets/mc_button.png") // todo
            .imageSize(16, 32)
            .uv(0.0f, 0.0f, 1.0f, 0.5f)
            .adaptable(2)
            .build();

    public static final UITexture MC_BUTTON_DISABLED = new UITexture.Builder()
            .location("modularui", "gui/widgets/mc_button_disabled.png") // todo
            .imageSize(16, 16)
            .adaptable(2)
            .build();

    // BUTTON OVERLAYS

    public static final UITexture BUTTON_ITEM_OUTPUT = fullImage("textures/gui/widget/button_item_output_overlay.png");
    public static final UITexture BUTTON_FLUID_OUTPUT = fullImage(
            "textures/gui/widget/button_fluid_output_overlay.png");
    public static final UITexture BUTTON_AUTO_COLLAPSE = fullImage(
            "textures/gui/widget/button_auto_collapse_overlay.png");
    public static final UITexture BUTTON_X = fullImage("textures/gui/widget/button_x_overlay.png", true);
    public static final UITexture BUTTON_CLEAR_GRID = fullImage("textures/gui/widget/button_clear_grid.png", false);

    public static final UITexture BUTTON_CROSS = fullImage("textures/gui/widget/button_cross.png");
    public static final UITexture BUTTON_REDSTONE_ON = fullImage("textures/gui/widget/button_redstone_on.png");
    public static final UITexture BUTTON_REDSTONE_OFF = fullImage("textures/gui/widget/button_redstone_off.png");
    public static final UITexture BUTTON_THROTTLE_PLUS = fullImage("textures/gui/widget/button_throttle_plus.png");
    public static final UITexture BUTTON_THROTTLE_MINUS = fullImage("textures/gui/widget/button_throttle_minus.png");

    // PROGRESS BARS
    public static final UITexture PROGRESS_BAR_ARC_FURNACE = progressBar(
            "textures/gui/progress_bar/progress_bar_arc_furnace.png", true);
    public static final UITexture PROGRESS_BAR_ARROW = progressBar("textures/gui/progress_bar/progress_bar_arrow.png",
            true);
    public static final UITexture PROGRESS_BAR_ARROW_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_arrow_bronze.png");
    public static final UITexture PROGRESS_BAR_ARROW_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_arrow_steel.png");
    public static final UITexture PROGRESS_BAR_ARROW_MULTIPLE = progressBar(
            "textures/gui/progress_bar/progress_bar_arrow_multiple.png", true);
    public static final UITexture PROGRESS_BAR_BATH = progressBar("textures/gui/progress_bar/progress_bar_bath.png",
            true);
    public static final UITexture PROGRESS_BAR_BENDING = progressBar(
            "textures/gui/progress_bar/progress_bar_bending.png", true);
    public static final UITexture PROGRESS_BAR_CANNER = progressBar("textures/gui/progress_bar/progress_bar_canner.png",
            true);
    public static final UITexture PROGRESS_BAR_CIRCUIT = progressBar(
            "textures/gui/progress_bar/progress_bar_circuit.png", true);
    public static final UITexture PROGRESS_BAR_CIRCUIT_ASSEMBLER = progressBar(
            "textures/gui/progress_bar/progress_bar_circuit_assembler.png", true);
    public static final UITexture PROGRESS_BAR_COMPRESS = progressBar(
            "textures/gui/progress_bar/progress_bar_compress.png", true);
    public static final UITexture PROGRESS_BAR_COMPRESS_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_compress_bronze.png");
    public static final UITexture PROGRESS_BAR_COMPRESS_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_compress_steel.png");
    public static final UITexture PROGRESS_BAR_CRACKING = progressBar(
            "textures/gui/progress_bar/progress_bar_cracking.png", true);
    public static final UITexture PROGRESS_BAR_CRACKING_INPUT = progressBar(
            "textures/gui/progress_bar/progress_bar_cracking_2.png", 21, 38, true);
    public static final UITexture PROGRESS_BAR_CRYSTALLIZATION = progressBar(
            "textures/gui/progress_bar/progress_bar_crystallization.png", true);
    public static final UITexture PROGRESS_BAR_EXTRACT = progressBar(
            "textures/gui/progress_bar/progress_bar_extract.png", true);
    public static final UITexture PROGRESS_BAR_EXTRACT_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_extract_bronze.png");
    public static final UITexture PROGRESS_BAR_EXTRACT_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_extract_steel.png");
    public static final UITexture PROGRESS_BAR_EXTRUDER = progressBar(
            "textures/gui/progress_bar/progress_bar_extruder.png", true);
    public static final UITexture PROGRESS_BAR_FUSION = progressBar("textures/gui/progress_bar/progress_bar_fusion.png",
            true);
    public static final UITexture PROGRESS_BAR_GAS_COLLECTOR = progressBar(
            "textures/gui/progress_bar/progress_bar_gas_collector.png", true);
    public static final UITexture PROGRESS_BAR_HAMMER = progressBar("textures/gui/progress_bar/progress_bar_hammer.png",
            true);
    public static final UITexture PROGRESS_BAR_HAMMER_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_hammer_bronze.png");
    public static final UITexture PROGRESS_BAR_HAMMER_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_hammer_steel.png");
    public static final UITexture PROGRESS_BAR_HAMMER_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_hammer_base.png", true);
    public static final UITexture PROGRESS_BAR_HAMMER_BASE_BRONZE = fullImage(
            "textures/gui/progress_bar/progress_bar_hammer_base_bronze.png");
    public static final UITexture PROGRESS_BAR_HAMMER_BASE_STEEL = fullImage(
            "textures/gui/progress_bar/progress_bar_hammer_base_steel.png");
    public static final UITexture PROGRESS_BAR_LATHE = progressBar("textures/gui/progress_bar/progress_bar_lathe.png",
            true);
    public static final UITexture PROGRESS_BAR_LATHE_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_lathe_base.png", true);
    public static final UITexture PROGRESS_BAR_MACERATE = progressBar(
            "textures/gui/progress_bar/progress_bar_macerate.png", true);
    public static final UITexture PROGRESS_BAR_MACERATE_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_macerate_bronze.png");
    public static final UITexture PROGRESS_BAR_MACERATE_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_macerate_steel.png");
    public static final UITexture PROGRESS_BAR_MAGNET = progressBar("textures/gui/progress_bar/progress_bar_magnet.png",
            true);
    public static final UITexture PROGRESS_BAR_MASS_FAB = progressBar(
            "textures/gui/progress_bar/progress_bar_mass_fab.png", true);
    public static final UITexture PROGRESS_BAR_MIXER = progressBar("textures/gui/progress_bar/progress_bar_mixer.png",
            true);
    public static final UITexture PROGRESS_BAR_PACKER = progressBar("textures/gui/progress_bar/progress_bar_packer.png",
            true);
    public static final UITexture PROGRESS_BAR_RECYCLER = progressBar(
            "textures/gui/progress_bar/progress_bar_recycler.png", true);
    public static final UITexture PROGRESS_BAR_REPLICATOR = progressBar(
            "textures/gui/progress_bar/progress_bar_replicator.png", true);
    public static final UITexture PROGRESS_BAR_SIFT = progressBar("textures/gui/progress_bar/progress_bar_sift.png",
            true);
    public static final UITexture PROGRESS_BAR_SLICE = progressBar("textures/gui/progress_bar/progress_bar_slice.png",
            true);
    public static final UITexture PROGRESS_BAR_UNPACKER = progressBar(
            "textures/gui/progress_bar/progress_bar_unpacker.png", true);
    public static final UITexture PROGRESS_BAR_WIREMILL = progressBar(
            "textures/gui/progress_bar/progress_bar_wiremill.png", true);

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
            "textures/gui/progress_bar/progress_bar_boiler_heat.png", true);
    public static final UITexture PROGRESS_BAR_ASSEMBLY_LINE = progressBar(
            "textures/gui/progress_bar/progress_bar_assembly_line.png", 54, 144, true);
    public static final UITexture PROGRESS_BAR_ASSEMBLY_LINE_ARROW = progressBar(
            "textures/gui/progress_bar/progress_bar_assembly_line_arrow.png", 10, 36, true);
    public static final UITexture PROGRESS_BAR_COKE_OVEN = progressBar(
            "textures/gui/progress_bar/progress_bar_coke_oven.png", 36, 36, true);
    public static final UITexture PROGRESS_BAR_DISTILLATION_TOWER = progressBar(
            "textures/gui/progress_bar/progress_bar_distillation_tower.png", 66, 116, true);
    public static final UITexture PROGRESS_BAR_SOLAR_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_solar_bronze.png", 10, 20);
    public static final UITexture PROGRESS_BAR_SOLAR_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_solar_steel.png", 10, 20);
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_1 = progressBar(
            "textures/gui/progress_bar/progress_bar_research_station_1.png", 54, 10, true);
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_2 = progressBar(
            "textures/gui/progress_bar/progress_bar_research_station_2.png", 10, 36, true);
    public static final UITexture PROGRESS_BAR_RESEARCH_STATION_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_research_station_base.png", true);
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

    // MISC

    public static void init() {/**/}

    private static UITexture fullImage(String path) {
        return fullImage(path, false);
    }

    private static UITexture fullImage(String path, boolean canApplyTheme) {
        return UITexture.fullImage(GTCEu.MOD_ID, path, canApplyTheme);
    }

    @SuppressWarnings("SameParameterValue")
    private static UITexture[] slice(String path, int imageWidth, int imageHeight, int sliceWidth, int sliceHeight,
                                     boolean canApplyTheme) {
        if (imageWidth % sliceWidth != 0 || imageHeight % sliceHeight != 0)
            throw new IllegalArgumentException("Slice height and slice width must divide the image evenly!");

        int countX = imageWidth / sliceWidth;
        int countY = imageHeight / sliceHeight;
        UITexture[] slices = new UITexture[countX * countY];

        for (int indexX = 0; indexX < countX; indexX++) {
            for (int indexY = 0; indexY < countY; indexY++) {
                slices[(indexX * countX) + indexY] = UITexture.builder()
                        .location(GTCEu.MOD_ID, path)
                        .canApplyTheme(canApplyTheme)
                        .imageSize(imageWidth, imageHeight)
                        .uv(indexX * sliceWidth, indexY * sliceHeight, sliceWidth, sliceHeight)
                        .build();
            }
        }
        return slices;
    }

    private static UITexture progressBar(String path) {
        return progressBar(path, false);
    }

    private static UITexture progressBar(String path, boolean canApplyTheme) {
        return progressBar(path, 20, 40, canApplyTheme);
    }

    private static UITexture progressBar(String path, int width, int height) {
        return progressBar(path, width, height, false);
    }

    private static UITexture progressBar(String path, int width, int height, boolean canApplyTheme) {
        UITexture.Builder builder = new UITexture.Builder()
                .location(GTCEu.MOD_ID, path)
                .imageSize(width, height);
        if (canApplyTheme) builder.canApplyTheme();
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
