package com.gregtechceu.gtceu.api.machine.property;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.machine.electric.ChargerMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DiodePartMachine;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import lombok.experimental.UtilityClass;

// spotless:off
@UtilityClass
public class GTMachineModelProperties {

    // Generic properties
    public static final BooleanProperty IS_PAINTED = BooleanProperty.create("is_painted");
    public static final BooleanProperty IS_FORMED = BooleanProperty.create("is_formed");
    public static final BooleanProperty IS_TAPED = BooleanProperty.create("taped");

    // Recipe-related properties
    public static final EnumProperty<RecipeLogic.Status> RECIPE_LOGIC_STATUS = EnumProperty.create("recipe_logic_status", RecipeLogic.Status.class);
    public static final BooleanProperty IS_WORKING_ENABLED = BooleanProperty.create("working_enabled");
    public static final BooleanProperty IS_ACTIVE = BooleanProperty.create("active");

    // Machine-specific properties
    public static final BooleanProperty IS_STEEL_MACHINE = BooleanProperty.create("steel");
    public static final EnumProperty<RelativeDirection> VENT_DIRECTION = EnumProperty.create("steam_vent", RelativeDirection.class);

    public static final EnumProperty<ChargerMachine.State> CHARGER_STATE = EnumProperty.create("charger_state", ChargerMachine.State.class);
    public static final BooleanProperty IS_FE_TO_EU = BooleanProperty.create("fe_to_eu");
    public static final BooleanProperty IS_TRANSFORM_UP = BooleanProperty.create("transform_up");
    public static final EnumProperty<DiodePartMachine.AmpMode> DIODE_AMP_MODE = EnumProperty.create("amp_mode", DiodePartMachine.AmpMode.class);

    public static final BooleanProperty IS_HPCA_PART_DAMAGED = BooleanProperty.create("hpca_part_damaged");
    public static final BooleanProperty IS_RANDOM_TICK_MODE = BooleanProperty.create("random_tick_mode");

    public static final BooleanProperty HAS_ROTOR = BooleanProperty.create("has_rotor");
    public static final BooleanProperty IS_ROTOR_SPINNING = BooleanProperty.create("rotor_spinning");
    public static final BooleanProperty IS_EMISSIVE_ROTOR = BooleanProperty.create("emissive_rotor");

}
// spotless:on
