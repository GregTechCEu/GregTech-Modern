package com.gregtechceu.gtceu.api.gui;

import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

@SuppressWarnings("unused")
public class GuiTextures {

    // GREGTECH
    public static final ResourceTexture GREGTECH_LOGO = new ResourceTexture(
            "gtceu:textures/gui/icon/gregtech_logo.png");
    public static final ResourceTexture GREGTECH_LOGO_XMAS = new ResourceTexture(
            "gtceu:textures/gui/icon/gregtech_logo_xmas.png");

    // HUD
    public static final ResourceTexture TOOL_FRONT_FACING_ROTATION = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_front_facing_rotation.png");
    public static final ResourceTexture TOOL_IO_FACING_ROTATION = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_io_facing_rotation.png");
    public static final ResourceTexture TOOL_PAUSE = new ResourceTexture("gtceu:textures/gui/overlay/tool_pause.png");
    public static final ResourceTexture TOOL_START = new ResourceTexture("gtceu:textures/gui/overlay/tool_start.png");
    public static final ResourceTexture TOOL_COVER_SETTINGS = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_cover_settings.png");
    public static final ResourceTexture TOOL_MUTE = new ResourceTexture("gtceu:textures/gui/overlay/tool_mute.png");
    public static final ResourceTexture TOOL_SOUND = new ResourceTexture("gtceu:textures/gui/overlay/tool_sound.png");
    public static final ResourceTexture TOOL_ALLOW_INPUT = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_allow_input.png");
    public static final ResourceTexture TOOL_ATTACH_COVER = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_attach_cover.png");
    public static final ResourceTexture TOOL_REMOVE_COVER = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_remove_cover.png");
    public static final ResourceTexture TOOL_PIPE_BLOCK = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_pipe_block.png");
    public static final ResourceTexture TOOL_PIPE_CONNECT = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_pipe_connect.png");
    public static final ResourceTexture TOOL_WIRE_BLOCK = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_wire_block.png");
    public static final ResourceTexture TOOL_WIRE_CONNECT = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_wire_connect.png");
    public static final ResourceTexture TOOL_AUTO_OUTPUT = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_auto_output.png");
    public static final ResourceTexture TOOL_DISABLE_AUTO_OUTPUT = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_disable_auto_output.png");
    public static final ResourceTexture TOOL_SWITCH_CONVERTER_NATIVE = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_wire_block.png"); // todo switch to tool_switch_converter_native once that
                                                               // gets made
    public static final ResourceTexture TOOL_SWITCH_CONVERTER_EU = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_wire_connect.png"); // todo switch to tool_switch_converter_eu once that
                                                                 // gets made

    // BASE TEXTURES
    public static final ResourceBorderTexture BACKGROUND = new ResourceBorderTexture(
            "gtceu:textures/gui/base/background.png", 16, 16, 4, 4);
    public static final ResourceBorderTexture BACKGROUND_INVERSE = new ResourceBorderTexture(
            "gtceu:textures/gui/base/background_inverse.png", 16, 16, 4, 4);
    public static final SteamTexture BACKGROUND_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/base/background_%s.png");
    public static final ResourceTexture CLIPBOARD_BACKGROUND = new ResourceTexture(
            "gtceu:textures/gui/base/clipboard_background.png");
    public static final ResourceTexture CLIPBOARD_PAPER_BACKGROUND = new ResourceTexture(
            "gtceu:textures/gui/base/clipboard_paper_background.png");
    public static final ResourceBorderTexture TITLE_BAR_BACKGROUND = new ResourceBorderTexture(
            "gtceu:textures/gui/base/title_bar_background.png", 16, 16, 4, 4);

    public static final ResourceTexture DISPLAY = new ResourceTexture("gtceu:textures/gui/base/display.png");
    public static final SteamTexture DISPLAY_STEAM = SteamTexture.fullImage("gtceu:textures/gui/base/display_%s.png");
    public static final ResourceTexture BLANK = new ResourceBorderTexture("gtceu:textures/gui/base/blank.png", 1, 1, 0,
            0);
    public static final ResourceTexture BLANK_TRANSPARENT = new ResourceBorderTexture(
            "gtceu:textures/gui/base/blank_transparent.png", 1, 1, 0, 0);
    public static final ResourceBorderTexture FLUID_SLOT = new ResourceBorderTexture(
            "gtceu:textures/gui/base/fluid_slot.png", 18, 18, 1, 1);
    public static final ResourceTexture FLUID_TANK_BACKGROUND = new ResourceTexture(
            "gtceu:textures/gui/base/fluid_tank_background.png");
    public static final ResourceTexture FLUID_TANK_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/base/fluid_tank_overlay.png");
    public static final ResourceBorderTexture SLOT = new ResourceBorderTexture("gtceu:textures/gui/base/slot.png", 18,
            18, 1, 1);
    public static final ResourceBorderTexture SLOT_DARK = new ResourceBorderTexture(
            "gtceu:textures/gui/base/slot_dark.png", 18,
            18, 1, 1);

    public static final ResourceTexture SLOT_DARKENED = new ResourceTexture(
            "gtceu:textures/gui/base/darkened_slot.png");
    public static final SteamTexture SLOT_STEAM = SteamTexture.fullImage("gtceu:textures/gui/base/slot_%s.png");
    public static final ResourceTexture TOGGLE_BUTTON_BACK = new ResourceTexture(
            "gtceu:textures/gui/widget/toggle_button_background.png");

    public static final ResourceTexture CLOSE_ICON = new ResourceTexture("gtceu:textures/gui/icon/close.png");

    // FLUID & ITEM OUTPUT BUTTONS
    public static final ResourceTexture BLOCKS_INPUT = new ResourceTexture(
            "gtceu:textures/gui/widget/button_blocks_input.png");
    public static final ResourceBorderTexture BUTTON = new ResourceBorderTexture("gtceu:textures/gui/widget/button.png",
            32, 32, 2, 2);
    public static final ResourceTexture BUTTON_ALLOW_IMPORT_EXPORT = new ResourceTexture(
            "gtceu:textures/gui/widget/button_allow_import_export.png");
    public static final ResourceTexture BUTTON_BLACKLIST = new ResourceTexture(
            "gtceu:textures/gui/widget/button_blacklist.png");
    public static final ResourceTexture BUTTON_CHUNK_MODE = new ResourceTexture(
            "gtceu:textures/gui/widget/button_chunk_mode.png");
    public static final ResourceTexture BUTTON_CLEAR_GRID = new ResourceTexture(
            "gtceu:textures/gui/widget/button_clear_grid.png");
    public static final ResourceTexture BUTTON_FILTER_DAMAGE = new ResourceTexture(
            "gtceu:textures/gui/widget/button_filter_damage.png");
    public static final ResourceTexture BUTTON_DISTINCT_BUSES = new ResourceTexture(
            "gtceu:textures/gui/widget/button_distinct_buses.png");
    public static final ResourceTexture BUTTON_POWER = new ResourceTexture(
            "gtceu:textures/gui/widget/button_power.png");
    public static final ResourceTexture BUTTON_BATCH = new ResourceTexture(
            "gtceu:textures/gui/widget/button_batch.png");
    public static final ResourceTexture BUTTON_FILTER_NBT = new ResourceTexture(
            "gtceu:textures/gui/widget/button_filter_nbt.png");
    public static final ResourceTexture BUTTON_FLUID_OUTPUT = new ResourceTexture(
            "gtceu:textures/gui/widget/button_fluid_output_overlay.png");
    public static final ResourceTexture BUTTON_ITEM_OUTPUT = new ResourceTexture(
            "gtceu:textures/gui/widget/button_item_output_overlay.png");
    public static final ResourceTexture BUTTON_LOCK = new ResourceTexture("gtceu:textures/gui/widget/button_lock.png");
    public static final ResourceTexture BUTTON_VOID = new ResourceTexture("gtceu:textures/gui/widget/button_void.png");
    public static final ResourceTexture BUTTON_VOID_PARTIAL = new ResourceTexture(
            "gtceu:textures/gui/widget/button_void_partial.png");
    public static final ResourceTexture BUTTON_VOID_MULTIBLOCK = new ResourceTexture(
            "gtceu:textures/gui/widget/button_void_multiblock.png");
    public static final ResourceTexture BUTTON_LEFT = new ResourceTexture("gtceu:textures/gui/widget/left.png");
    public static final ResourceTexture BUTTON_PUBLIC_PRIVATE = new ResourceTexture(
            "gtceu:textures/gui/widget/button_public_private.png");
    public static final ResourceTexture BUTTON_CHECK = new ResourceTexture(
            "gtceu:textures/gui/widget/button_check.png");
    public static final ResourceTexture BUTTON_LIST = new ResourceTexture(
            "gtceu:textures/gui/widget/button_list.png");
    public static final ResourceTexture BUTTON_RIGHT = new ResourceTexture("gtceu:textures/gui/widget/right.png");
    public static final ResourceTexture BUTTON_SILK_TOUCH_MODE = new ResourceTexture(
            "gtceu:textures/gui/widget/button_silk_touch_mode.png");
    public static final ResourceTexture BUTTON_SWITCH_VIEW = new ResourceTexture(
            "gtceu:textures/gui/widget/button_switch_view.png");
    public static final ResourceTexture BUTTON_WORKING_ENABLE = new ResourceTexture(
            "gtceu:textures/gui/widget/button_working_enable.png");
    public static final ResourceTexture BUTTON_INT_CIRCUIT_PLUS = new ResourceTexture(
            "gtceu:textures/gui/widget/button_circuit_plus.png");
    public static final ResourceTexture BUTTON_INT_CIRCUIT_MINUS = new ResourceTexture(
            "gtceu:textures/gui/widget/button_circuit_minus.png");
    public static final ResourceTexture CLIPBOARD_BUTTON = new ResourceTexture(
            "gtceu:textures/gui/widget/clipboard_button.png");
    public static final ResourceBorderTexture CLIPBOARD_TEXT_BOX = new ResourceBorderTexture(
            "gtceu:textures/gui/widget/clipboard_text_box.png", 9, 18, 1, 1);
    public static final ResourceTexture DISTRIBUTION_MODE = new ResourceTexture(
            "gtceu:textures/gui/widget/button_distribution_mode.png");
    public static final ResourceTexture BUTTON_AUTO_PULL = new ResourceTexture(
            "gtceu:textures/gui/widget/button_me_auto_pull.png");
    public static final ResourceTexture LOCK = new ResourceTexture("gtceu:textures/gui/widget/lock.png");
    public static final ResourceTexture LOCK_WHITE = new ResourceTexture("gtceu:textures/gui/widget/lock_white.png");
    public static final ResourceTexture SWITCH = new ResourceTexture("gtceu:textures/gui/widget/switch.png");
    public static final ResourceTexture SWITCH_HORIZONTAL = new ResourceTexture(
            "gtceu:textures/gui/widget/switch_horizontal.png");
    public static final ResourceTexture VANILLA_BUTTON = new ResourceBorderTexture(
            "ldlib:textures/gui/button_common.png", 198, 18, 1, 1);

    public static final ResourceTexture ENERGY_DETECTOR_COVER_MODE_BUTTON = new ResourceTexture(
            "gtceu:textures/gui/widget/button_detector_cover_energy_mode.png");
    public static final ResourceTexture INVERT_REDSTONE_BUTTON = new ResourceTexture(
            "gtceu:textures/gui/widget/button_detector_cover_inverted.png");

    public static final ResourceTexture IO_CONFIG_FLUID_MODES_BUTTON = new ResourceTexture(
            "gtceu:textures/gui/icon/io_config/output_config_fluid_modes.png");
    public static final ResourceTexture IO_CONFIG_ITEM_MODES_BUTTON = new ResourceTexture(
            "gtceu:textures/gui/icon/io_config/output_config_item_modes.png");
    public static final ResourceTexture IO_CONFIG_COVER_SLOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/icon/io_config/cover_slot_overlay.png");
    public static final ResourceTexture IO_CONFIG_COVER_SETTINGS = new ResourceTexture(
            "gtceu:textures/gui/icon/io_config/cover_settings.png");

    public static final ResourceTexture PATTERN_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/widget/pattern_overlay.png");
    public static final ResourceTexture REFUND_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/widget/refund_overlay.png");
    // INDICATORS & ICONS
    public static final ResourceTexture INDICATOR_NO_ENERGY = new ResourceTexture(
            "gtceu:textures/gui/base/indicator_no_energy.png");
    public static final SteamTexture INDICATOR_NO_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/base/indicator_no_steam_%s.png");
    public static final ResourceTexture TANK_ICON = new ResourceTexture("gtceu:textures/gui/base/tank_icon.png");

    // WIDGET UI RELATED
    public static final ResourceTexture SLIDER_BACKGROUND = new ResourceTexture(
            "gtceu:textures/gui/widget/slider_background.png");
    public static final ResourceTexture SLIDER_BACKGROUND_VERTICAL = new ResourceTexture(
            "gtceu:textures/gui/widget/slider_background_vertical.png");
    public static final ResourceTexture SLIDER_ICON = new ResourceTexture("gtceu:textures/gui/widget/slider.png");
    public static final ResourceTexture MAINTENANCE_BUTTON = new ResourceTexture(
            "gtceu:textures/gui/widget/button_maintenance.png");
    public static final ResourceTexture MAINTENANCE_ICON = new ResourceTexture(
            "gtceu:textures/block/overlay/machine/overlay_maintenance.png");
    public static final ResourceTexture STORAGE_ICON = new ResourceTexture(
            "gtceu:textures/item/storage_cover.png");
    public static final ResourceTexture BUTTON_MINER_MODES = new ResourceTexture(
            "gtceu:textures/gui/widget/button_miner_modes.png");

    // ORE PROCESSING
    public static final ResourceTexture OREBY_BASE = new ResourceTexture("gtceu:textures/gui/arrows/oreby-base.png");
    public static final ResourceTexture OREBY_CHEM = new ResourceTexture("gtceu:textures/gui/arrows/oreby-chem.png");
    public static final ResourceTexture OREBY_SEP = new ResourceTexture("gtceu:textures/gui/arrows/oreby-sep.png");
    public static final ResourceTexture OREBY_SIFT = new ResourceTexture("gtceu:textures/gui/arrows/oreby-sift.png");
    public static final ResourceTexture OREBY_SMELT = new ResourceTexture("gtceu:textures/gui/arrows/oreby-smelt.png");

    // PRIMITIVE
    public static final ResourceBorderTexture PRIMITIVE_BACKGROUND = new ResourceBorderTexture(
            "gtceu:textures/gui/primitive/primitive_background.png", 176, 166, 3, 3);
    public static final ResourceBorderTexture PRIMITIVE_SLOT = new ResourceBorderTexture(
            "gtceu:textures/gui/primitive/primitive_slot.png", 18, 18, 1, 1);
    public static final ResourceTexture PRIMITIVE_FURNACE_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/primitive/overlay_primitive_furnace.png");
    public static final ResourceTexture PRIMITIVE_DUST_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/primitive/overlay_primitive_dust.png");
    public static final ResourceTexture PRIMITIVE_INGOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/primitive/overlay_primitive_ingot.png");
    public static final ResourceTexture PRIMITIVE_LARGE_FLUID_TANK = new ResourceTexture(
            "gtceu:textures/gui/primitive/primitive_large_fluid_tank.png");
    public static final ResourceTexture PRIMITIVE_LARGE_FLUID_TANK_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/primitive/primitive_large_fluid_tank_overlay.png");
    public static final ResourceTexture PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR = new ResourceTexture(
            "gtceu:textures/gui/primitive/progress_bar_primitive_blast_furnace.png");

    // SLOT OVERLAYS
    public static final ResourceTexture ATOMIC_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/atomic_overlay_1.png");
    public static final ResourceTexture ATOMIC_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/atomic_overlay_2.png");
    public static final ResourceTexture ARROW_INPUT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/arrow_input_overlay.png");
    public static final ResourceTexture ARROW_OUTPUT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/arrow_output_overlay.png");
    public static final ResourceTexture BATTERY_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/battery_overlay.png");
    public static final ResourceTexture BEAKER_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/beaker_overlay_1.png");
    public static final ResourceTexture BEAKER_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/beaker_overlay_2.png");
    public static final ResourceTexture BEAKER_OVERLAY_3 = new ResourceTexture(
            "gtceu:textures/gui/overlay/beaker_overlay_3.png");
    public static final ResourceTexture BEAKER_OVERLAY_4 = new ResourceTexture(
            "gtceu:textures/gui/overlay/beaker_overlay_4.png");
    public static final ResourceTexture BENDER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/bender_overlay.png");
    public static final ResourceTexture BOX_OVERLAY = new ResourceTexture("gtceu:textures/gui/overlay/box_overlay.png");
    public static final ResourceTexture BOXED_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/boxed_overlay.png");
    public static final ResourceTexture BREWER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/brewer_overlay.png");
    public static final ResourceTexture CANNER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/canner_overlay.png");
    public static final ResourceTexture CHARGER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/charger_slot_overlay.png");
    public static final ResourceTexture CANISTER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/canister_overlay.png");
    public static final SteamTexture CANISTER_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/canister_overlay_%s.png");
    public static final ResourceTexture CENTRIFUGE_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/centrifuge_overlay.png");
    public static final ResourceTexture CIRCUIT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/circuit_overlay.png");
    public static final SteamTexture COAL_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/coal_overlay_%s.png");
    public static final ResourceTexture COMPRESSOR_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/compressor_overlay.png");
    public static final SteamTexture COMPRESSOR_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/compressor_overlay_%s.png");
    public static final ResourceTexture CRACKING_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/cracking_overlay_1.png");
    public static final ResourceTexture CRACKING_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/cracking_overlay_2.png");
    public static final ResourceTexture CRUSHED_ORE_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/crushed_ore_overlay.png");
    public static final SteamTexture CRUSHED_ORE_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/crushed_ore_overlay_%s.png");
    public static final ResourceTexture CRYSTAL_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/crystal_overlay.png");
    public static final ResourceTexture CUTTER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/cutter_overlay.png");
    public static final ResourceTexture DARK_CANISTER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/dark_canister_overlay.png");
    public static final ResourceTexture DUST_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/dust_overlay.png");
    public static final SteamTexture DUST_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/dust_overlay_%s.png");
    public static final ResourceTexture EXTRACTOR_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/extractor_overlay.png");
    public static final SteamTexture EXTRACTOR_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/extractor_overlay_%s.png");
    public static final ResourceTexture FILTER_SLOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/filter_slot_overlay.png");
    public static final ResourceTexture FURNACE_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/furnace_overlay_1.png");
    public static final ResourceTexture FURNACE_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/furnace_overlay_2.png");
    public static final SteamTexture FURNACE_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/furnace_overlay_%s.png");
    public static final ResourceTexture HAMMER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/hammer_overlay.png");
    public static final SteamTexture HAMMER_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/hammer_overlay_%s.png");
    public static final ResourceTexture HEATING_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/heating_overlay_1.png");
    public static final ResourceTexture HEATING_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/heating_overlay_2.png");
    public static final ResourceTexture IMPLOSION_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/implosion_overlay_1.png");
    public static final ResourceTexture IMPLOSION_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/implosion_overlay_2.png");
    public static final ResourceTexture IN_SLOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/in_slot_overlay.png");
    public static final SteamTexture IN_SLOT_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/in_slot_overlay_%s.png");
    public static final ResourceTexture INGOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/ingot_overlay.png");
    public static final ResourceTexture INT_CIRCUIT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/int_circuit_overlay.png");
    public static final ResourceTexture LENS_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/lens_overlay.png");
    public static final ResourceTexture LIGHTNING_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/lightning_overlay_1.png");
    public static final ResourceTexture LIGHTNING_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/lightning_overlay_2.png");
    public static final ResourceTexture MOLD_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/mold_overlay.png");
    public static final ResourceTexture MOLECULAR_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/molecular_overlay_1.png");
    public static final ResourceTexture MOLECULAR_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/molecular_overlay_2.png");
    public static final ResourceTexture MOLECULAR_OVERLAY_3 = new ResourceTexture(
            "gtceu:textures/gui/overlay/molecular_overlay_3.png");
    public static final ResourceTexture MOLECULAR_OVERLAY_4 = new ResourceTexture(
            "gtceu:textures/gui/overlay/molecular_overlay_4.png");
    public static final ResourceTexture OUT_SLOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/out_slot_overlay.png");
    public static final SteamTexture OUT_SLOT_OVERLAY_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/overlay/out_slot_overlay_%s.png");
    public static final ResourceTexture PAPER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/paper_overlay.png");
    public static final ResourceTexture PRINTED_PAPER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/printed_paper_overlay.png");
    public static final ResourceTexture PIPE_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/pipe_overlay_2.png");
    public static final ResourceTexture PIPE_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/pipe_overlay_1.png");
    public static final ResourceTexture PRESS_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/press_overlay_1.png");
    public static final ResourceTexture PRESS_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/press_overlay_2.png");
    public static final ResourceTexture PRESS_OVERLAY_3 = new ResourceTexture(
            "gtceu:textures/gui/overlay/press_overlay_3.png");
    public static final ResourceTexture PRESS_OVERLAY_4 = new ResourceTexture(
            "gtceu:textures/gui/overlay/press_overlay_4.png");
    public static final ResourceTexture SAWBLADE_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/sawblade_overlay.png");
    public static final ResourceTexture SOLIDIFIER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/solidifier_overlay.png");
    public static final ResourceTexture STRING_SLOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/string_slot_overlay.png");
    public static final ResourceTexture TOOL_SLOT_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/tool_slot_overlay.png");
    public static final ResourceTexture TURBINE_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/turbine_overlay.png");
    public static final ResourceTexture VIAL_OVERLAY_1 = new ResourceTexture(
            "gtceu:textures/gui/overlay/vial_overlay_1.png");
    public static final ResourceTexture VIAL_OVERLAY_2 = new ResourceTexture(
            "gtceu:textures/gui/overlay/vial_overlay_2.png");
    public static final ResourceTexture WIREMILL_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/wiremill_overlay.png");
    public static final ResourceTexture POSITIVE_MATTER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/positive_matter_overlay.png");
    public static final ResourceTexture NEUTRAL_MATTER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/neutral_matter_overlay.png");
    public static final ResourceTexture DATA_ORB_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/data_orb_overlay.png");
    public static final ResourceTexture SCANNER_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/scanner_overlay.png");
    public static final ResourceTexture DUCT_TAPE_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/duct_tape_overlay.png");
    public static final ResourceTexture RESEARCH_STATION_OVERLAY = new ResourceTexture(
            "gtceu:textures/gui/overlay/research_station_overlay.png");

    // PROGRESS BARS
    public static final ResourceTexture PROGRESS_BAR_ARC_FURNACE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_arc_furnace.png");
    public static final ResourceTexture PROGRESS_BAR_ARROW = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_arrow.png");
    public static final SteamTexture PROGRESS_BAR_ARROW_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_arrow_%s.png");
    public static final ResourceTexture PROGRESS_BAR_ARROW_MULTIPLE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_arrow_multiple.png");
    public static final ResourceTexture PROGRESS_BAR_ASSEMBLER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_assembler.png");

    public static final ResourceTexture PROGRESS_BAR_ASSEMBLY_LINE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_assembly_line.png");
    public static final ResourceTexture PROGRESS_BAR_ASSEMBLY_LINE_ARROW = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_assembly_line_arrow.png");
    public static final ResourceTexture PROGRESS_BAR_BATH = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_bath.png");
    public static final ResourceTexture PROGRESS_BAR_BENDING = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_bending.png");
    public static final SteamTexture PROGRESS_BAR_BOILER_EMPTY = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_boiler_empty_%s.png");
    public static final SteamTexture PROGRESS_BAR_BOILER_FUEL = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_boiler_fuel_%s.png");
    public static final ResourceTexture PROGRESS_BAR_BOILER_HEAT = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_boiler_heat.png");
    public static final ResourceTexture PROGRESS_BAR_CANNER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_canner.png");
    public static final ResourceTexture PROGRESS_BAR_CIRCUIT = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_circuit_assembler.png");
    public static final ResourceTexture PROGRESS_BAR_CIRCUIT_ASSEMBLER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_circuit_assembler.png");
    public static final ResourceTexture PROGRESS_BAR_COKE_OVEN = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_coke_oven.png");
    public static final ResourceTexture PROGRESS_BAR_COMPRESS = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_compress.png");
    public static final SteamTexture PROGRESS_BAR_COMPRESS_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_compress_%s.png");
    public static final ResourceTexture PROGRESS_BAR_CRACKING = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_cracking.png");
    public static final ResourceTexture PROGRESS_BAR_CRACKING_INPUT = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_cracking_2.png");
    public static final ResourceTexture PROGRESS_BAR_CRYSTALLIZATION = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_crystallization.png");
    public static final ResourceTexture PROGRESS_BAR_DISTILLATION_TOWER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_distillation_tower.png");
    public static final ResourceTexture PROGRESS_BAR_EXTRACT = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_extract.png");
    public static final SteamTexture PROGRESS_BAR_EXTRACT_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_extract_%s.png");
    public static final ResourceTexture PROGRESS_BAR_EXTRUDER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_extruder.png");
    public static final ResourceTexture PROGRESS_BAR_FUSION = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_fusion.png");
    public static final ResourceTexture PROGRESS_BAR_GAS_COLLECTOR = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_gas_collector.png");
    public static final ResourceTexture PROGRESS_BAR_HAMMER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_hammer.png");
    public static final SteamTexture PROGRESS_BAR_HAMMER_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_hammer_%s.png");
    public static final ResourceTexture PROGRESS_BAR_HAMMER_BASE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_hammer_base.png");
    public static final SteamTexture PROGRESS_BAR_HAMMER_BASE_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_hammer_base_%s.png");
    public static final ResourceTexture PROGRESS_BAR_LATHE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_lathe.png");
    public static final ResourceTexture PROGRESS_BAR_LATHE_BASE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_lathe_base.png");
    public static final ResourceTexture PROGRESS_BAR_MACERATE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_macerate.png");
    public static final SteamTexture PROGRESS_BAR_MACERATE_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_macerate_%s.png");
    public static final ResourceTexture PROGRESS_BAR_MAGNET = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_magnet.png");
    public static final ResourceTexture PROGRESS_BAR_MASS_FAB = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_mass_fab.png");
    public static final ResourceTexture PROGRESS_BAR_MIXER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_mixer.png");
    public static final ResourceTexture PROGRESS_BAR_PACKER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_packer.png");
    public static final ResourceTexture PROGRESS_BAR_RECYCLER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_recycler.png");
    public static final ResourceTexture PROGRESS_BAR_REPLICATOR = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_replicator.png");
    public static final ResourceTexture PROGRESS_BAR_SIFT = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_sift.png");
    public static final ResourceTexture PROGRESS_BAR_SLICE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_slice.png");
    public static final SteamTexture PROGRESS_BAR_SOLAR_STEAM = SteamTexture
            .fullImage("gtceu:textures/gui/progress_bar/progress_bar_solar_%s.png");
    public static final ResourceTexture PROGRESS_BAR_UNLOCK = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_unlock.png");
    public static final ResourceTexture PROGRESS_BAR_UNPACKER = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_unpacker.png");
    public static final ResourceTexture PROGRESS_BAR_WIREMILL = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_wiremill.png");
    public static final ResourceTexture PROGRESS_BAR_RESEARCH_STATION_1 = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_research_station_1.png");
    public static final ResourceTexture PROGRESS_BAR_RESEARCH_STATION_2 = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_research_station_2.png");
    public static final ResourceTexture PROGRESS_BAR_RESEARCH_STATION_BASE = new ResourceTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_research_station_base.png");

    // JEI
    public static final ResourceTexture INFO_ICON = new ResourceTexture("gtceu:textures/gui/widget/information.png");
    public static final ResourceTexture MULTIBLOCK_CATEGORY = new ResourceTexture(
            "gtceu:textures/gui/icon/coke_oven.png");

    public static final ResourceTexture ARC_FURNACE_RECYCLING_CATEGORY = new ResourceTexture(
            "gtceu:textures/gui/icon/category/arc_furnace_recycling.png");
    public static final ResourceTexture MACERATOR_RECYCLING_CATEGORY = new ResourceTexture(
            "gtceu:textures/gui/icon/category/macerator_recycling.png");
    public static final ResourceTexture EXTRACTOR_RECYCLING_CATEGORY = new ResourceTexture(
            "gtceu:textures/gui/icon/category/extractor_recycling.png");

    // Covers
    public static final ResourceTexture COVER_MACHINE_CONTROLLER = new ResourceTexture(
            "gtceu:textures/items/machine_controller_cover.png");

    // Terminal
    public static final ResourceTexture ICON_REMOVE = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/remove_hover.png");
    public static final ResourceTexture ICON_UP = new ResourceTexture("gtceu:textures/gui/terminal/icon/up_hover.png");
    public static final ResourceTexture ICON_DOWN = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/down_hover.png");
    public static final ResourceTexture ICON_RIGHT = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/right_hover.png");
    public static final ResourceTexture ICON_LEFT = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/left_hover.png");
    public static final ResourceTexture ICON_ADD = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/add_hover.png");

    public static final ResourceTexture ICON_NEW_PAGE = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/system/memory_card_hover.png");
    public static final ResourceTexture ICON_LOAD = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/folder_hover.png");
    public static final ResourceTexture ICON_SAVE = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/system/save_hover.png");
    public static final ResourceTexture ICON_LOCATION = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/guide_hover.png");
    public static final ResourceTexture ICON_VISIBLE = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/appearance_hover.png");
    public static final ResourceTexture ICON_CALCULATOR = new ResourceTexture(
            "gtceu:textures/gui/terminal/icon/calculator_hover.png");
    public static final ResourceTexture UI_FRAME_SIDE_UP = new ResourceTexture(
            "gtceu:textures/gui/terminal/frame_side_up.png");
    public static final ResourceTexture UI_FRAME_SIDE_DOWN = new ResourceTexture(
            "gtceu:textures/gui/terminal/frame_side_down.png");

    // Texture Areas
    public static final ResourceTexture BUTTON_FLUID = new ResourceTexture(
            "gtceu:textures/block/cover/cover_interface_fluid_button.png");
    public static final ResourceTexture BUTTON_ITEM = new ResourceTexture(
            "gtceu:textures/block/cover/cover_interface_item_button.png");
    public static final ResourceTexture BUTTON_ENERGY = new ResourceTexture(
            "gtceu:textures/block/cover/cover_interface_energy_button.png");
    public static final ResourceTexture BUTTON_MACHINE = new ResourceTexture(
            "gtceu:textures/block/cover/cover_interface_machine_button.png");
    public static final ResourceTexture BUTTON_INTERFACE = new ResourceTexture(
            "gtceu:textures/block/cover/cover_interface_computer_button.png");
    public static final ResourceTexture COVER_INTERFACE_MACHINE_ON_PROXY = new ResourceTexture(
            "gtceu:textures/block/cover/cover_interface_machine_on_proxy.png");
    public static final ResourceTexture COVER_INTERFACE_MACHINE_OFF_PROXY = new ResourceTexture(
            "gtceu:textures/blocks/cover/cover_interface_machine_off_proxy.png");
    public static final ResourceTexture SCENE = new ResourceTexture("gtceu:textures/gui/widget/scene.png");
    public static final ResourceBorderTexture DISPLAY_FRAME = new ResourceBorderTexture(
            "gtceu:textures/gui/base/display_frame.png", 16, 16, 4, 4);
    public static final ResourceTexture INSUFFICIENT_INPUT = new ResourceTexture(
            "gtceu:textures/gui/base/indicator_no_energy.png");
    public static final ResourceBorderTexture ENERGY_BAR_BACKGROUND = new ResourceBorderTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_boiler_empty_steel.png", 10, 54, 1, 1);
    public static final ResourceBorderTexture ENERGY_BAR_BASE = new ResourceBorderTexture(
            "gtceu:textures/gui/progress_bar/progress_bar_boiler_heat.png", 10, 54, 1, 1);
    public static final ResourceTexture LIGHT_ON = new ResourceTexture("gtceu:textures/gui/widget/light_on.png");
    public static final ResourceTexture LIGHT_OFF = new ResourceTexture("gtceu:textures/gui/widget/light_off.png");
    public static final ResourceTexture UP = new ResourceTexture("gtceu:textures/gui/base/up.png");
    public static final ResourceTexture[] TIER = new ResourceTexture[9];
    static {
        var offset = 1f / TIER.length;
        for (int i = 0; i < TIER.length; i++) {
            TIER[i] = new ResourceTexture("gtceu:textures/gui/overlay/tier.png").getSubTexture(0, i * offset, 1,
                    offset);
        }
    }

    // Lamp item overlay
    public static final ResourceTexture LAMP_NO_BLOOM = new ResourceTexture(
            "gtceu:textures/gui/item_overlay/lamp_no_bloom.png");
    public static final ResourceTexture LAMP_NO_LIGHT = new ResourceTexture(
            "gtceu:textures/gui/item_overlay/lamp_no_light.png");

    // ME hatch/bus
    public static final ResourceTexture NUMBER_BACKGROUND = new ResourceTexture(
            "gtceu:textures/gui/widget/number_background.png");
    public static final ResourceTexture CONFIG_ARROW = new ResourceTexture(
            "gtceu:textures/gui/widget/config_arrow.png");
    public static final ResourceTexture CONFIG_ARROW_DARK = new ResourceTexture(
            "gtceu:textures/gui/widget/config_arrow_dark.png");
    public static final ResourceTexture SELECT_BOX = new ResourceTexture("gtceu:textures/gui/widget/select_box.png");

    // HPCA Component icons
    public static final ResourceTexture HPCA_COMPONENT_OUTLINE = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/component_outline.png");
    public static final ResourceTexture HPCA_ICON_EMPTY_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/empty_component.png");
    public static final ResourceTexture HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/advanced_computation_component.png");
    public static final ResourceTexture HPCA_ICON_BRIDGE_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/bridge_component.png");
    public static final ResourceTexture HPCA_ICON_COMPUTATION_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/computation_component.png");
    public static final ResourceTexture HPCA_ICON_ACTIVE_COOLER_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/active_cooler_component.png");
    public static final ResourceTexture HPCA_ICON_HEAT_SINK_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/heat_sink_component.png");
    public static final ResourceTexture HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/damaged_advanced_computation_component.png");
    public static final ResourceTexture HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT = new ResourceTexture(
            "gtceu:textures/gui/widget/hpca/damaged_computation_component.png");
}
