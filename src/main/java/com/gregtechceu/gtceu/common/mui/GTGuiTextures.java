package com.gregtechceu.gtceu.common.mui;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.mui.SteamTextureSet;
import com.gregtechceu.gtceu.api.recipe.gui.ProgressBarTextureSet;

import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.drawable.ColorType;
import brachy.modularui.drawable.TabTexture;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.ProgressDrawable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@ApiStatus.NonExtendable
public interface GTGuiTextures {

    /** Keys used for GT assets registered for use in Themes */
    interface IDs {

        String STANDARD_BACKGROUND = "gregtech_standard_bg";
        String STANDARD_BACKGROUND_INVERSE = "gregtech_standard_inverse_bg";
        String COVER_BACKGROUND = "gregtech_cover_bg";
        String BRONZE_BACKGROUND = "gregtech_bronze_bg";
        String STEEL_BACKGROUND = "gregtech_steel_bg";
        String PRIMITIVE_BACKGROUND = "gregtech_primitive_bg";

        String STANDARD_SLOT = "gregtech_standard_slot";
        String BRONZE_SLOT = "gregtech_bronze_slot";
        String STEEL_SLOT = "gregtech_steel_slot";
        String PRIMITIVE_SLOT = "gregtech_primitive_slot";

        String STANDARD_FLUID_SLOT = "gregtech_standard_fluid_slot";

        String STANDARD_BUTTON = "gregtech_standard_button";
    }

    ResourceLocation MONOCRAFT_FONT = GTCEu.id("monocraft");

    // ICONS

    /** @apiNote You may want {@link GTGuiTextures#getLogo} instead. */
    UITexture GREGTECH_LOGO = fullImage("textures/gui/icon/gregtech_logo.png");
    /** @apiNote You may want {@link GTGuiTextures#getLogo} instead. */
    UITexture GREGTECH_LOGO_XMAS = fullImage("textures/gui/icon/gregtech_logo_xmas.png");
    UITexture GREGTECH_LOGO_DARK = fullImage("textures/gui/icon/gregtech_logo_dark.png");
    // todo blinking GT logos

    UITexture INDICATOR_NO_ENERGY = fullImage("textures/gui/base/indicator_no_energy.png");
    UITexture INDICATOR_NO_STEAM_BRONZE = fullImage(
            "textures/gui/base/indicator_no_steam_bronze.png");
    UITexture INDICATOR_NO_STEAM_STEEL = fullImage(
            "textures/gui/base/indicator_no_steam_steel.png");
    UITexture TANK_ICON = fullImage("textures/gui/base/tank_icon.png");

    // BACKGROUNDS
    UITexture BACKGROUND = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background.png")
            .imageSize(16, 16)
            .adaptable(4)
            .name(IDs.STANDARD_BACKGROUND)
            .defaultColorType()
            .build();

    UITexture BACKGROUND_POPUP = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_popup.png")
            .imageSize(195, 136)
            .adaptable(4)
            .name(IDs.COVER_BACKGROUND)
            .canApplyTheme()
            .build();

    UITexture BACKGROUND_INVERSE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_inverse.png")
            .imageSize(16, 16)
            .adaptable(3)
            .name(IDs.STANDARD_BACKGROUND_INVERSE)
            .canApplyTheme()
            .build();

    UITexture BACKGROUND_BRONZE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_bronze.png")
            .imageSize(16, 16)
            .adaptable(4)
            .name(IDs.BRONZE_BACKGROUND)
            .build();

    UITexture BACKGROUND_STEEL = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/background_steel.png")
            .imageSize(16, 16)
            .adaptable(4)
            .name(IDs.STEEL_BACKGROUND)
            .build();

    UITexture BLANK_TRANSPARENT = fullImage("textures/gui/base/blank_transparent.png");

    // todo move to textures/gui/base
    UITexture BACKGROUND_PRIMITIVE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/primitive/primitive_background.png")
            .imageSize(176, 166)
            .adaptable(3)
            .name(IDs.PRIMITIVE_BACKGROUND)
            .build();

    // todo clipboard backgrounds, may deserve some redoing

    // DISPLAYS
    UITexture DISPLAY = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display.png")
            .imageSize(182, 117)
            .canApplyTheme()
            .build();

    UITexture DISPLAY_BRONZE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display_bronze.png")
            .imageSize(162, 121)
            .build();

    UITexture DISPLAY_STEEL = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/display_steel.png")
            .imageSize(162, 121)
            .adaptable(1)
            .build();

    // todo primitive display?

    // SLOTS
    UITexture SLOT = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.STANDARD_SLOT)
            .canApplyTheme()
            .build();

    UITexture SLOT_BRONZE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/slot_bronze.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.BRONZE_SLOT)
            .build();

    UITexture SLOT_STEEL = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/slot_steel.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.STEEL_SLOT)
            .build();

    UITexture SLOT_DARK = fullImage("textures/gui/base/slot_dark.png");

    // todo move to textures/gui/base
    UITexture SLOT_PRIMITIVE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/primitive/primitive_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.PRIMITIVE_SLOT)
            .build();

    UITexture FLUID_SLOT = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/base/fluid_slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .name(IDs.STANDARD_FLUID_SLOT)
            .canApplyTheme()
            .build();

    UITexture[] BUTTON_POWER = slice("textures/gui/widget/button_power.png", 16, 32, 16, 16,
            ColorType.DEFAULT);

    UITexture BUTTON_BLACKLIST = fullImage("textures/gui/widget/button_blacklist.png",
            ColorType.DEFAULT);
    UITexture[] BUTTON_IGNORE_DAMAGE = slice("textures/gui/widget/button_filter_damage.png",
            16, 32, 16, 16, ColorType.DEFAULT);
    UITexture BUTTON_IGNORE_NBT = fullImage("textures/gui/widget/button_filter_nbt.png",
            ColorType.DEFAULT);

    UITexture[] BUTTON_CASE_SENSITIVE = slice(
            "textures/gui/widget/ore_filter/button_case_sensitive.png",
            16, 32, 16, 16, ColorType.DEFAULT);

    UITexture[] BUTTON_MATCH_ALL = slice("textures/gui/widget/ore_filter/button_match_all.png",
            16, 32, 16, 16, ColorType.DEFAULT);
    UITexture BUTTON_LOCK = fullImage("textures/gui/widget/button_lock.png");

    UITexture OREDICT_ERROR = fullImage("textures/gui/widget/ore_filter/error.png");
    UITexture OREDICT_INFO = fullImage("textures/gui/widget/ore_filter/info.png");
    UITexture OREDICT_MATCH = fullImage("textures/gui/widget/ore_filter/match.png");
    UITexture OREDICT_NO_MATCH = fullImage("textures/gui/widget/ore_filter/no_match.png");
    UITexture OREDICT_SUCCESS = fullImage("textures/gui/widget/ore_filter/success.png");
    UITexture OREDICT_WAITING = fullImage("textures/gui/widget/ore_filter/waiting.png");
    UITexture OREDICT_WARN = fullImage("textures/gui/widget/ore_filter/warn.png");

    UITexture INFO = fullImage("textures/gui/widget/information.png");

    UITexture[] MANUAL_IO_OVERLAY_IN = { fullImage("textures/gui/icon/manual_io_mode/disabled.png"),
            fullImage("textures/gui/icon/manual_io_mode/filtered.png"),
            fullImage("textures/gui/icon/manual_io_mode/unfiltered.png") };
    UITexture[] MANUAL_IO_OVERLAY_OUT = slice("textures/gui/overlay/manual_io_overlay_out.png",
            18, 18 * 3, 18, 18, ColorType.DEFAULT);
    UITexture[] CONVEYOR_MODE_OVERLAY = slice("textures/gui/overlay/conveyor_mode_overlay.png",
            18, 18 * 2, 18, 18, ColorType.DEFAULT);

    UITexture[] TRANSFER_MODE_OVERLAY = slice("textures/gui/overlay/transfer_mode_overlay.png",
            40, 40 * 3, 40, 40, ColorType.DEFAULT);

    UITexture[] BUTTON_DISTINCT = slice(
            "textures/gui/widget/button_distinct_buses.png",
            16, 32, 16, 16, ColorType.DEFAULT);

    UITexture[] FLUID_TRANSFER_MODE_OVERLAY = slice(
            "textures/gui/overlay/fluid_transfer_mode_overlay.png",
            18, 18 * 3, 18, 18, ColorType.DEFAULT);

    UITexture[] DISTRIBUTION_MODE_OVERLAY = slice(
            "textures/gui/widget/button_distribution_mode.png",
            16, 48, 16, 16, ColorType.DEFAULT);

    UITexture[] VOIDING_MODES = { fullImage("textures/gui/icon/voiding_mode/void_any.png"),
            fullImage("textures/gui/icon/voiding_mode/void_overflow.png") };

    UITexture BUTTON_VOID = fullImage("textures/gui/widget/button_void.png");

    UITexture BUTTON_VOID_PARTIAL = fullImage("textures/gui/widget/button_void_partial.png");

    UITexture[] BUTTON_VOID_MULTIBLOCK = slice("textures/gui/widget/button_void_multiblock.png",
            16, 64, 16, 16, ColorType.DEFAULT);

    UITexture[] FILTER_MODE_OVERLAY = {
            fullImage("textures/gui/icon/filter_mode/filter_insert.png"),
            fullImage("textures/gui/icon/filter_mode/filter_extract.png"),
            fullImage("textures/gui/icon/filter_mode/filter_both.png")
    };

    UITexture[] PRIVATE_MODE_BUTTON = slice(
            "textures/gui/widget/button_public_private.png",
            18, 36, 18, 18, ColorType.DEFAULT);

    UITexture MENU_OVERLAY = fullImage("textures/gui/overlay/menu_overlay.png");

    UITexture RECIPE_LOCK = fullImage("textures/gui/widget/lock.png");

    // todo bronze/steel/primitive fluid slots?

    // SLOT OVERLAYS
    UITexture ATOMIC_OVERLAY_1 = fullImage("textures/gui/overlay/atomic_overlay_1.png",
            ColorType.DEFAULT);
    UITexture ATOMIC_OVERLAY_2 = fullImage("textures/gui/overlay/atomic_overlay_2.png",
            ColorType.DEFAULT);
    UITexture ARROW_INPUT_OVERLAY = fullImage("textures/gui/overlay/arrow_input_overlay.png",
            ColorType.DEFAULT);
    UITexture ARROW_OUTPUT_OVERLAY = fullImage("textures/gui/overlay/arrow_output_overlay.png",
            ColorType.DEFAULT);
    UITexture BATTERY_OVERLAY = fullImage("textures/gui/overlay/battery_overlay.png",
            ColorType.DEFAULT);
    UITexture BEAKER_OVERLAY_1 = fullImage("textures/gui/overlay/beaker_overlay_1.png",
            ColorType.DEFAULT);
    UITexture BEAKER_OVERLAY_2 = fullImage("textures/gui/overlay/beaker_overlay_2.png",
            ColorType.DEFAULT);
    UITexture BEAKER_OVERLAY_3 = fullImage("textures/gui/overlay/beaker_overlay_3.png",
            ColorType.DEFAULT);
    UITexture BEAKER_OVERLAY_4 = fullImage("textures/gui/overlay/beaker_overlay_4.png",
            ColorType.DEFAULT);
    UITexture BENDER_OVERLAY = fullImage("textures/gui/overlay/bender_overlay.png",
            ColorType.DEFAULT);
    UITexture BOX_OVERLAY = fullImage("textures/gui/overlay/box_overlay.png", ColorType.DEFAULT);
    UITexture BOXED_OVERLAY = fullImage("textures/gui/overlay/boxed_overlay.png",
            ColorType.DEFAULT);
    UITexture BREWER_OVERLAY = fullImage("textures/gui/overlay/brewer_overlay.png",
            ColorType.DEFAULT);
    UITexture CANNER_OVERLAY = fullImage("textures/gui/overlay/canner_overlay.png",
            ColorType.DEFAULT);
    UITexture CHARGER_OVERLAY = fullImage("textures/gui/overlay/charger_slot_overlay.png",
            ColorType.DEFAULT);
    UITexture CANISTER_OVERLAY = fullImage("textures/gui/overlay/canister_overlay.png",
            ColorType.DEFAULT);
    UITexture CANISTER_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/canister_overlay_bronze.png");
    UITexture CANISTER_OVERLAY_STEEL = fullImage("textures/gui/overlay/canister_overlay_steel.png");
    UITexture CENTRIFUGE_OVERLAY = fullImage("textures/gui/overlay/centrifuge_overlay.png",
            ColorType.DEFAULT);
    UITexture CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/circuit_overlay.png",
            ColorType.DEFAULT);
    UITexture COAL_OVERLAY_BRONZE = fullImage("textures/gui/overlay/coal_overlay_bronze.png");
    UITexture COAL_OVERLAY_STEEL = fullImage("textures/gui/overlay/coal_overlay_steel.png");
    UITexture COMPRESSOR_OVERLAY = fullImage("textures/gui/overlay/compressor_overlay.png",
            ColorType.DEFAULT);
    UITexture COMPRESSOR_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/compressor_overlay_bronze.png");
    UITexture COMPRESSOR_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/compressor_overlay_steel.png");
    UITexture CRACKING_OVERLAY_1 = fullImage("textures/gui/overlay/cracking_overlay_1.png",
            ColorType.DEFAULT);
    UITexture CRACKING_OVERLAY_2 = fullImage("textures/gui/overlay/cracking_overlay_2.png",
            ColorType.DEFAULT);
    UITexture CRUSHED_ORE_OVERLAY = fullImage("textures/gui/overlay/crushed_ore_overlay.png",
            ColorType.DEFAULT);
    UITexture CRUSHED_ORE_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/crushed_ore_overlay_bronze.png");
    UITexture CRUSHED_ORE_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/crushed_ore_overlay_steel.png");
    UITexture CRYSTAL_OVERLAY = fullImage("textures/gui/overlay/crystal_overlay.png",
            ColorType.DEFAULT);
    UITexture CUTTER_OVERLAY = fullImage("textures/gui/overlay/cutter_overlay.png",
            ColorType.DEFAULT);
    UITexture DARK_CANISTER_OVERLAY = fullImage("textures/gui/overlay/dark_canister_overlay.png",
            ColorType.DEFAULT);
    UITexture DUST_OVERLAY = fullImage("textures/gui/overlay/dust_overlay.png", ColorType.DEFAULT);
    UITexture DUST_OVERLAY_BRONZE = fullImage("textures/gui/overlay/dust_overlay_bronze.png");
    UITexture DUST_OVERLAY_STEEL = fullImage("textures/gui/overlay/dust_overlay_steel.png");
    UITexture PRIMITIVE_DUST_OVERLAY = fullImage(
            "textures/gui/primitive/overlay_primitive_dust.png", ColorType.DEFAULT);
    UITexture EXTRACTOR_OVERLAY = fullImage("textures/gui/overlay/extractor_overlay.png",
            ColorType.DEFAULT);
    UITexture EXTRACTOR_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/extractor_overlay_bronze.png");
    UITexture EXTRACTOR_OVERLAY_STEEL = fullImage(
            "textures/gui/overlay/extractor_overlay_steel.png");
    UITexture FILTER_SLOT_OVERLAY = fullImage("textures/gui/overlay/filter_slot_overlay.png",
            ColorType.DEFAULT);
    UITexture FILTER_SETTINGS_OVERLAY = fullImage(
            "textures/gui/overlay/filter_settings_overlay.png",
            ColorType.DEFAULT);
    UITexture FURNACE_OVERLAY_1 = fullImage("textures/gui/overlay/furnace_overlay_1.png",
            ColorType.DEFAULT);
    UITexture FURNACE_OVERLAY_2 = fullImage("textures/gui/overlay/furnace_overlay_2.png",
            ColorType.DEFAULT);
    UITexture FURNACE_OVERLAY_BRONZE = fullImage("textures/gui/overlay/furnace_overlay_bronze.png");
    UITexture FURNACE_OVERLAY_STEEL = fullImage("textures/gui/overlay/furnace_overlay_steel.png");
    UITexture PRIMITIVE_FURNACE_OVERLAY = fullImage(
            "textures/gui/primitive/overlay_primitive_furnace.png",
            ColorType.DEFAULT);
    UITexture PRIMITIVE_LARGE_FLUID_TANK = fullImage(
            "textures/gui/primitive/primitive_large_fluid_tank.png",
            ColorType.DEFAULT);
    UITexture PRIMITIVE_LARGE_FLUID_TANK_OVERLAY = fullImage(
            "textures/gui/primitive/primitive_large_fluid_tank_overlay.png",
            ColorType.DEFAULT);
    UITexture HAMMER_OVERLAY = fullImage("textures/gui/overlay/hammer_overlay.png",
            ColorType.DEFAULT);
    UITexture HAMMER_OVERLAY_BRONZE = fullImage("textures/gui/overlay/hammer_overlay_bronze.png");
    UITexture HAMMER_OVERLAY_STEEL = fullImage("textures/gui/overlay/hammer_overlay_steel.png");
    UITexture HEATING_OVERLAY_1 = fullImage("textures/gui/overlay/heating_overlay_1.png",
            ColorType.DEFAULT);
    UITexture HEATING_OVERLAY_2 = fullImage("textures/gui/overlay/heating_overlay_2.png",
            ColorType.DEFAULT);
    UITexture IMPLOSION_OVERLAY_1 = fullImage("textures/gui/overlay/implosion_overlay_1.png",
            ColorType.DEFAULT);
    UITexture IMPLOSION_OVERLAY_2 = fullImage("textures/gui/overlay/implosion_overlay_2.png",
            ColorType.DEFAULT);
    UITexture IN_SLOT_OVERLAY = fullImage("textures/gui/overlay/in_slot_overlay.png",
            ColorType.DEFAULT);
    UITexture IN_SLOT_OVERLAY_BRONZE = fullImage("textures/gui/overlay/in_slot_overlay_bronze.png");
    UITexture IN_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/in_slot_overlay_steel.png");
    UITexture INGOT_OVERLAY = fullImage("textures/gui/overlay/ingot_overlay.png",
            ColorType.DEFAULT);
    UITexture PRIMITIVE_INGOT_OVERLAY = fullImage(
            "textures/gui/primitive/overlay_primitive_ingot.png",
            ColorType.DEFAULT);
    UITexture INT_CIRCUIT_OVERLAY = fullImage("textures/gui/overlay/int_circuit_overlay.png",
            ColorType.DEFAULT);
    UITexture LENS_OVERLAY = fullImage("textures/gui/overlay/lens_overlay.png", ColorType.DEFAULT);
    UITexture LIGHTNING_OVERLAY_1 = fullImage("textures/gui/overlay/lightning_overlay_1.png",
            ColorType.DEFAULT);
    UITexture LIGHTNING_OVERLAY_2 = fullImage("textures/gui/overlay/lightning_overlay_2.png",
            ColorType.DEFAULT);
    UITexture MOLD_OVERLAY = fullImage("textures/gui/overlay/mold_overlay.png", ColorType.DEFAULT);
    UITexture MOLECULAR_OVERLAY_1 = fullImage("textures/gui/overlay/molecular_overlay_1.png",
            ColorType.DEFAULT);
    UITexture MOLECULAR_OVERLAY_2 = fullImage("textures/gui/overlay/molecular_overlay_2.png",
            ColorType.DEFAULT);
    UITexture MOLECULAR_OVERLAY_3 = fullImage("textures/gui/overlay/molecular_overlay_3.png",
            ColorType.DEFAULT);
    UITexture MOLECULAR_OVERLAY_4 = fullImage("textures/gui/overlay/molecular_overlay_4.png",
            ColorType.DEFAULT);
    UITexture OUT_SLOT_OVERLAY = fullImage("textures/gui/overlay/out_slot_overlay.png",
            ColorType.DEFAULT);
    UITexture OUT_SLOT_OVERLAY_BRONZE = fullImage(
            "textures/gui/overlay/out_slot_overlay_bronze.png");
    UITexture OUT_SLOT_OVERLAY_STEEL = fullImage("textures/gui/overlay/out_slot_overlay_steel.png");
    UITexture PAPER_OVERLAY = fullImage("textures/gui/overlay/paper_overlay.png",
            ColorType.DEFAULT);
    UITexture PATTERN_OVERLAY = fullImage("textures/gui/widget/pattern_overlay.png",
            ColorType.DEFAULT);
    UITexture PRINTED_PAPER_OVERLAY = fullImage("textures/gui/overlay/printed_paper_overlay.png",
            ColorType.DEFAULT);
    UITexture PIPE_OVERLAY_2 = fullImage("textures/gui/overlay/pipe_overlay_2.png",
            ColorType.DEFAULT);
    UITexture PIPE_OVERLAY_1 = fullImage("textures/gui/overlay/pipe_overlay_1.png",
            ColorType.DEFAULT);
    UITexture PRESS_OVERLAY_1 = fullImage("textures/gui/overlay/press_overlay_1.png",
            ColorType.DEFAULT);
    UITexture PRESS_OVERLAY_2 = fullImage("textures/gui/overlay/press_overlay_2.png",
            ColorType.DEFAULT);
    UITexture PRESS_OVERLAY_3 = fullImage("textures/gui/overlay/press_overlay_3.png",
            ColorType.DEFAULT);
    UITexture PRESS_OVERLAY_4 = fullImage("textures/gui/overlay/press_overlay_4.png",
            ColorType.DEFAULT);
    UITexture REFUND_OVERLAY = fullImage("textures/gui/widget/refund_overlay.png",
            ColorType.DEFAULT);
    UITexture SAWBLADE_OVERLAY = fullImage("textures/gui/overlay/sawblade_overlay.png",
            ColorType.DEFAULT);
    UITexture SOLIDIFIER_OVERLAY = fullImage("textures/gui/overlay/solidifier_overlay.png",
            ColorType.DEFAULT);
    UITexture STRING_SLOT_OVERLAY = fullImage("textures/gui/overlay/string_slot_overlay.png",
            ColorType.DEFAULT);
    UITexture TOOL_SLOT_OVERLAY = fullImage("textures/gui/overlay/tool_slot_overlay.png",
            ColorType.DEFAULT);
    UITexture TURBINE_OVERLAY = fullImage("textures/gui/overlay/turbine_overlay.png",
            ColorType.DEFAULT);
    UITexture VIAL_OVERLAY_1 = fullImage("textures/gui/overlay/vial_overlay_1.png",
            ColorType.DEFAULT);
    UITexture VIAL_OVERLAY_2 = fullImage("textures/gui/overlay/vial_overlay_2.png",
            ColorType.DEFAULT);
    UITexture WIREMILL_OVERLAY = fullImage("textures/gui/overlay/wiremill_overlay.png",
            ColorType.DEFAULT);
    UITexture POSITIVE_MATTER_OVERLAY = fullImage(
            "textures/gui/overlay/positive_matter_overlay.png", ColorType.DEFAULT);
    UITexture NEUTRAL_MATTER_OVERLAY = fullImage("textures/gui/overlay/neutral_matter_overlay.png",
            ColorType.DEFAULT);
    UITexture DATA_ORB_OVERLAY = fullImage("textures/gui/overlay/data_orb_overlay.png",
            ColorType.DEFAULT);
    UITexture SCANNER_OVERLAY = fullImage("textures/gui/overlay/scanner_overlay.png",
            ColorType.DEFAULT);
    UITexture DUCT_TAPE_OVERLAY = fullImage("textures/gui/overlay/duct_tape_overlay.png",
            ColorType.DEFAULT);
    UITexture RESEARCH_STATION_OVERLAY = fullImage(
            "textures/gui/overlay/research_station_overlay.png", ColorType.DEFAULT);
    UITexture OVERLAY_REDSTONE_ON = fullImage("textures/gui/overlay/redstone_on.png");
    UITexture OVERLAY_REDSTONE_OFF = fullImage("textures/gui/overlay/redstone_off.png");

    // BUTTONS

    UITexture BUTTON = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/button.png")
            .imageSize(32, 32)
            .adaptable(2)
            .name(IDs.STANDARD_BUTTON)
            .canApplyTheme()
            .build();

    // BUTTON OVERLAYS

    UITexture BUTTON_ITEM_OUTPUT = fullImage("textures/gui/widget/button_item_output_overlay.png");
    UITexture BUTTON_FLUID_OUTPUT = fullImage(
            "textures/gui/widget/button_fluid_output_overlay.png");
    UITexture BUTTON_AUTO_COLLAPSE = fullImage(
            "textures/gui/widget/button_auto_collapse_overlay.png");
    UITexture BUTTON_X = fullImage("textures/gui/widget/button_x_overlay.png", ColorType.DEFAULT);
    UITexture BUTTON_CLEAR_GRID = fullImage("textures/gui/widget/button_clear_grid.png", null);

    UITexture BUTTON_CROSS = fullImage("textures/gui/widget/button_clear_grid.png");
    UITexture BUTTON_DETECTOR_INVERT = fullImage(
            "textures/gui/widget/button_detector_cover_inverted.png");
    UITexture BUTTON_REDSTONE_ON = fullImage("textures/gui/widget/button_redstone_on.png");
    UITexture BUTTON_REDSTONE_OFF = fullImage("textures/gui/widget/button_redstone_off.png");
    UITexture BUTTON_THROTTLE_PLUS = fullImage("textures/gui/widget/button_throttle_plus.png");
    UITexture BUTTON_THROTTLE_MINUS = fullImage("textures/gui/widget/button_throttle_minus.png");
    UITexture BUTTON_EU = fullImage("textures/gui/overlay/mode_eu.png");
    UITexture BUTTON_PERCENT = fullImage("textures/gui/overlay/mode_percent.png");
    UITexture BUTTON_MAINTENANCE = fullImage("textures/gui/widget/button_maintenance.png");

    UITexture BUTTON_AUTO_PULL = fullImage("textures/gui/widget/button_me_auto_pull.png");

    // PROGRESS BARS

    ProgressBarTextureSet PROGRESS_ARROW = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_arrow.png", ColorType.DEFAULT),
            progressBar("textures/gui/progress_bar/progress_bar_arrow_bronze.png"),
            progressBar("textures/gui/progress_bar/progress_bar_arrow_steel.png"));

    ProgressBarTextureSet PROGRESS_ARROW_MULTIPLE = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_arrow_multiple.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_ASSEMBLER = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_assembler.png", ColorType.DEFAULT));

    UITexture[] PROGRESS_BATH = slice("textures/gui/progress_bar/progress_bar_bath.png",
            20, 40, 20, 20, ColorType.DEFAULT);

    ProgressBarTextureSet PROGRESS_BENDING = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_bending.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_CANNER = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_canner.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_CIRCUIT_ASSEMBLER = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_circuit_assembler.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_COMPRESS = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_compress.png", ColorType.DEFAULT),
            progressBar("textures/gui/progress_bar/progress_bar_compress_bronze.png"),
            progressBar("textures/gui/progress_bar/progress_bar_compress_steel.png"));

    ProgressBarTextureSet PROGRESS_CRACKING = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_cracking.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_CRYSTALLIZATION = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_crystallization.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_EXTRACT = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_extract.png", ColorType.DEFAULT),
            progressBar("textures/gui/progress_bar/progress_bar_extract_bronze.png"),
            progressBar("textures/gui/progress_bar/progress_bar_extract_steel.png"));

    ProgressBarTextureSet PROGRESS_EXTRUDER = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_extruder.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_FUSION = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_fusion.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_GAS_COLLECTOR = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_gas_collector.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_HAMMER = new ProgressBarTextureSet(20, ProgressDrawable.Direction.DOWN,
            progressBar("textures/gui/progress_bar/progress_bar_hammer.png", ColorType.DEFAULT),
            progressBar("textures/gui/progress_bar/progress_bar_hammer_bronze.png"),
            progressBar("textures/gui/progress_bar/progress_bar_hammer_steel.png"));

    SteamTextureSet PROGRESS_HAMMER_BASE = new SteamTextureSet(
            fullImage("textures/gui/progress_bar/progress_bar_hammer_base.png", ColorType.DEFAULT),
            fullImage("textures/gui/progress_bar/progress_bar_hammer_base_bronze.png"),
            fullImage("textures/gui/progress_bar/progress_bar_hammer_base_steel.png"));

    ProgressBarTextureSet PROGRESS_MACERATE = new ProgressBarTextureSet(20, ProgressDrawable.Direction.RIGHT,
            progressBar("textures/gui/progress_bar/progress_bar_macerate.png", ColorType.DEFAULT),
            progressBar("textures/gui/progress_bar/progress_bar_macerate_bronze.png"),
            progressBar("textures/gui/progress_bar/progress_bar_macerate_steel.png"));

    ProgressBarTextureSet PROGRESS_MAGNET = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_magnet.png", ColorType.DEFAULT));

    UITexture[] PROGRESS_MIXER = slice("textures/gui/progress_bar/progress_bar_mixer.png",
            20, 40, 20, 20, ColorType.DEFAULT);

    ProgressBarTextureSet PROGRESS_SIFTER = new ProgressBarTextureSet(20, ProgressDrawable.Direction.DOWN,
            progressBar("textures/gui/progress_bar/progress_bar_sift.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_CUTTER = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_slice.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_PACKER = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_unpacker.png", ColorType.DEFAULT));

    ProgressBarTextureSet PROGRESS_WIREMILL = new ProgressBarTextureSet(
            progressBar("textures/gui/progress_bar/progress_bar_wiremill.png", ColorType.DEFAULT));

    UITexture PROGRESS_BAR_PACKER = progressBar("textures/gui/progress_bar/progress_bar_packer.png",
            ColorType.DEFAULT);

    UITexture PROGRESS_BAR_CIRCUIT = progressBar(
            "textures/gui/progress_bar/progress_bar_circuit.png", ColorType.DEFAULT);

    UITexture PROGRESS_BAR_RECYCLER = progressBar(
            "textures/gui/progress_bar/progress_bar_recycler.png", ColorType.DEFAULT);

    UITexture PROGRESS_BAR_REPLICATOR = progressBar(
            "textures/gui/progress_bar/progress_bar_replicator.png", ColorType.DEFAULT);

    UITexture PROGRESS_BAR_ARC_FURNACE = progressBar(
            "textures/gui/progress_bar/progress_bar_arc_furnace.png", ColorType.DEFAULT);
    UITexture PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR = progressBar(
            "textures/gui/primitive/progress_bar_primitive_blast_furnace.png", ColorType.DEFAULT);

    UITexture PROGRESS_BAR_MASS_FAB = progressBar(
            "textures/gui/progress_bar/progress_bar_mass_fab.png", ColorType.DEFAULT);

    // more custom progress bars
    // todo these boiler empty bars can probably be replaced by using a resized steam slot texture
    UITexture PROGRESS_BAR_BOILER_EMPTY_BRONZE = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/progress_bar/progress_bar_boiler_empty_bronze.png")
            .imageSize(10, 54)
            .adaptable(1)
            .build();

    UITexture PROGRESS_BAR_BOILER_EMPTY_STEEL = new UITexture.Builder()
            .location(GTCEu.MOD_ID, "textures/gui/progress_bar/progress_bar_boiler_empty_steel.png")
            .imageSize(10, 54)
            .adaptable(1)
            .build();

    UITexture PROGRESS_BAR_LATHE = progressBar("textures/gui/progress_bar/progress_bar_lathe.png",
            ColorType.DEFAULT);

    UITexture PROGRESS_BAR_LATHE_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_lathe_base.png", ColorType.DEFAULT);

    UITexture PROGRESS_BAR_BOILER_FUEL_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_boiler_fuel_bronze.png", 18, 36);
    UITexture PROGRESS_BAR_BOILER_FUEL_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_boiler_fuel_steel.png", 18, 36);

    ProgressBarTextureSet PROGRESS_BOILER_FUEL_STEEL = new ProgressBarTextureSet(18, ProgressDrawable.Direction.UP,
            PROGRESS_BAR_BOILER_FUEL_STEEL);

    UITexture PROGRESS_BAR_CRACKING_INPUT = progressBar("textures/gui/progress_bar/progress_bar_cracking_2.png", 21, 38,
            ColorType.DEFAULT);

    UITexture PROGRESS_BAR_BOILER_HEAT = progressBar(
            "textures/gui/progress_bar/progress_bar_boiler_heat.png", ColorType.DEFAULT);
    UITexture PROGRESS_BAR_ASSEMBLY_LINE = progressBar(
            "textures/gui/progress_bar/progress_bar_assembly_line.png", 54, 144, ColorType.DEFAULT);
    UITexture PROGRESS_BAR_ASSEMBLY_LINE_ARROW = progressBar(
            "textures/gui/progress_bar/progress_bar_assembly_line_arrow.png", 10, 36, ColorType.DEFAULT);
    UITexture PROGRESS_BAR_COKE_OVEN = progressBar(
            "textures/gui/progress_bar/progress_bar_coke_oven.png", 36, 36, ColorType.DEFAULT);
    UITexture PROGRESS_BAR_DISTILLATION_TOWER = progressBar(
            "textures/gui/progress_bar/progress_bar_distillation_tower.png", 66, 116, ColorType.DEFAULT);
    UITexture PROGRESS_BAR_SOLAR_BRONZE = progressBar(
            "textures/gui/progress_bar/progress_bar_solar_bronze.png", 10, 20);
    UITexture PROGRESS_BAR_SOLAR_STEEL = progressBar(
            "textures/gui/progress_bar/progress_bar_solar_steel.png", 10, 20);
    UITexture PROGRESS_BAR_RESEARCH_STATION_1 = progressBar(
            "textures/gui/progress_bar/progress_bar_research_station_1.png", 54, 10, ColorType.DEFAULT);
    UITexture PROGRESS_BAR_RESEARCH_STATION_2 = progressBar(
            "textures/gui/progress_bar/progress_bar_research_station_2.png", 10, 36, ColorType.DEFAULT);
    UITexture PROGRESS_BAR_RESEARCH_STATION_BASE = fullImage(
            "textures/gui/progress_bar/progress_bar_research_station_base.png", ColorType.DEFAULT);
    UITexture PROGRESS_BAR_FUSION_ENERGY = progressBar(
            "textures/gui/progress_bar/progress_bar_fusion_energy.png", 94, 14);
    UITexture PROGRESS_BAR_FUSION_HEAT = progressBar(
            "textures/gui/progress_bar/progress_bar_fusion_heat.png", 94, 14);
    UITexture PROGRESS_BAR_MULTI_ENERGY_YELLOW = progressBar(
            "textures/gui/progress_bar/progress_bar_multi_energy_yellow.png", 190, 14);
    UITexture PROGRESS_BAR_HPCA_COMPUTATION = progressBar(
            "textures/gui/progress_bar/progress_bar_hpca_computation.png", 94, 14);
    UITexture PROGRESS_BAR_LCE_FUEL = progressBar(
            "textures/gui/progress_bar/progress_bar_lce_fuel.png", 62, 14);
    UITexture PROGRESS_BAR_LCE_LUBRICANT = progressBar(
            "textures/gui/progress_bar/progress_bar_lce_lubricant.png", 62, 14);
    UITexture PROGRESS_BAR_LCE_OXYGEN = progressBar(
            "textures/gui/progress_bar/progress_bar_lce_oxygen.png", 62, 14);
    UITexture PROGRESS_BAR_TURBINE_ROTOR_SPEED = progressBar(
            "textures/gui/progress_bar/progress_bar_turbine_rotor_speed.png", 62, 14);
    UITexture PROGRESS_BAR_TURBINE_ROTOR_DURABILITY = progressBar(
            "textures/gui/progress_bar/progress_bar_turbine_rotor_durability.png", 62, 14);
    UITexture PROGRESS_BAR_FLUID_RIG_DEPLETION = progressBar(
            "textures/gui/progress_bar/progress_bar_fluid_rig_depletion.png", 190, 14);

    UITexture CYCLE_BUTTON = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/widget/button_distribution_mode.png")
            .imageSize(20, 60)
            .name("cycle")
            .build();
    // 16, 48, 16, 16, true);

    TabTexture TAB_TOP = TabTexture.of(
            fullImage("textures/gui/tab/tabs_top.png", ColorType.DEFAULT), GuiAxis.Y,
            false,
            28, 32, 4);

    UITexture MONITOR = UITexture.fullImage(GTCEu.MOD_ID, "item/computer_monitor_cover");
    UITexture DATA_HATCH = UITexture.fullImage(GTCEu.MOD_ID, "textures/item/data_module.png")
            .getSubArea(0, 0, 1, 1 / 13f);

    UITexture SEPERATOR_SIMPLE = UITexture.builder()
            .location(GTCEu.MOD_ID, "textures/gui/icon/seperator/seperator_simple.png")
            .imageSize(16, 5)
            .adaptable(2)
            .build();

    UITexture CONFIG_ARROW = fullImage("textures/gui/widget/config_arrow.png");
    UITexture CONFIG_ARROW_DARK = fullImage("textures/gui/widget/config_arrow_dark.png");

    // HPCA
    UITexture HPCA_COMPUTATION_COMPONENT = fullImage(
            "textures/gui/widget/hpca/computation_component.png");
    UITexture HPCA_ADVANCED_COMPUTATION_COMPONENT = fullImage(
            "textures/gui/widget/hpca/advanced_computation_component.png");
    UITexture HPCA_DAMAGED_COMPUTATION_COMPONENT = fullImage(
            "textures/gui/widget/hpca/damaged_computation_component.png");
    UITexture HPCA_DAMAGED_ADVANCED_COMPUTATION_COMPONENT = fullImage(
            "textures/gui/widget/hpca/damaged_advanced_computation_component.png");
    UITexture HPCA_ACTIVE_COOLER_COMPONENT = fullImage(
            "textures/gui/widget/hpca/active_cooler_component.png");
    UITexture HPCA_HEAT_SINK_COMPONENT = fullImage(
            "textures/gui/widget/hpca/heat_sink_component.png");
    UITexture HPCA_EMPTY_COMPONENT = fullImage("textures/gui/widget/hpca/empty.png");
    UITexture HPCA_BRIDGE_COMPONENT = fullImage("textures/gui/widget/hpca/bridge_component.png");
    UITexture HPCA_COMPONENT_OUTLINE = fullImage("textures/gui/widget/hpca/component_outline.png");

    // MACHINE GRID OVERLAYS

    UITexture TOOL_FRONT_FACING_ROTATION = fullImage(
            "textures/gui/overlay/tool_front_facing_rotation.png");
    UITexture TOOL_IO_FACING_ROTATION = fullImage(
            "textures/gui/overlay/tool_io_facing_rotation.png");
    UITexture TOOL_PAUSE = fullImage("textures/gui/overlay/tool_pause.png");
    UITexture TOOL_START = fullImage("textures/gui/overlay/tool_start.png");
    UITexture TOOL_COVER_SETTINGS = fullImage(
            "textures/gui/overlay/tool_cover_settings.png");
    UITexture TOOL_MUTE = fullImage("textures/gui/overlay/tool_mute.png");
    UITexture TOOL_SOUND = fullImage("textures/gui/overlay/tool_sound.png");
    UITexture TOOL_ALLOW_INPUT = fullImage(
            "textures/gui/overlay/tool_allow_input.png");
    UITexture TOOL_ATTACH_COVER = fullImage(
            "textures/gui/overlay/tool_attach_cover.png");
    UITexture TOOL_REMOVE_COVER = fullImage(
            "textures/gui/overlay/tool_remove_cover.png");
    UITexture TOOL_PIPE_BLOCK = fullImage(
            "textures/gui/overlay/tool_pipe_block.png");
    UITexture TOOL_PIPE_CONNECT = fullImage(
            "textures/gui/overlay/tool_pipe_connect.png");
    UITexture TOOL_WIRE_BLOCK = fullImage(
            "textures/gui/overlay/tool_wire_block.png");
    UITexture TOOL_WIRE_CONNECT = fullImage(
            "textures/gui/overlay/tool_wire_connect.png");
    UITexture TOOL_AUTO_OUTPUT = fullImage(
            "textures/gui/overlay/tool_auto_output.png");
    UITexture TOOL_DISABLE_AUTO_OUTPUT = fullImage(
            "textures/gui/overlay/tool_disable_auto_output.png");
    UITexture TOOL_SWITCH_CONVERTER_NATIVE = fullImage(
            "textures/gui/overlay/tool_wire_block.png");
    UITexture TOOL_SWITCH_CONVERTER_EU = fullImage(
            "textures/gui/overlay/tool_wire_connect.png");

    // Ore processing

    // ORE PROCESSING
    UITexture OREBY_BASE = fullImage("textures/gui/arrows/oreby-base.png");
    UITexture OREBY_CHEM = fullImage("textures/gui/arrows/oreby-chem.png");
    UITexture OREBY_SEP = fullImage("textures/gui/arrows/oreby-sep.png");
    UITexture OREBY_SIFT = fullImage("textures/gui/arrows/oreby-sift.png");
    UITexture OREBY_SMELT = fullImage("textures/gui/arrows/oreby-smelt.png");

    // Lamp item overlay
    UITexture LAMP_NO_BLOOM = fullImage(
            "textures/gui/item_overlay/lamp_no_bloom.png");
    UITexture LAMP_NO_LIGHT = fullImage(
            "textures/gui/item_overlay/lamp_no_light.png");

    // MISC

    static void init() {/**/}

    private static UITexture fullImage(String path) {
        return fullImage(path, null);
    }

    private static UITexture fullImage(String path, @Nullable ColorType colorType) {
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

    private static UITexture progressBar(String path, @Nullable ColorType colorType) {
        return progressBar(path, 20, 40, colorType);
    }

    private static UITexture progressBar(String path, int width, int height) {
        return progressBar(path, width, height, null);
    }

    private static UITexture progressBar(String path, int width, int height, @Nullable ColorType colorType) {
        UITexture.Builder builder = new UITexture.Builder()
                .location(GTCEu.MOD_ID, path)
                .imageSize(width, height)
                .colorType(colorType);
        return builder.build();
    }

    // todo steam logos? multi indicator blinking logos?
    static UITexture getLogo(@Nullable GTGuiTheme theme) {
        if (theme != null) {
            UITexture logo = theme.getLogo();
            if (logo != null) return logo;
        }
        return GTValues.XMAS.getAsBoolean() ? GREGTECH_LOGO_XMAS : GREGTECH_LOGO;
    }
}
