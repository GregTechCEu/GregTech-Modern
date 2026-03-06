package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.ArrayList;

public interface IMaintenanceMachine extends IMultiPart {

    BooleanProperty MAINTENANCE_TAPED_PROPERTY = GTMachineModelProperties.IS_TAPED;
    byte ALL_PROBLEMS = 0;
    byte NO_PROBLEMS = 0b111111;

    /**
     * @return true if this is a Full-Auto Maintenance Hatch, false otherwise.
     */
    boolean isFullAuto();

    /**
     * Sets this Maintenance Hatch as being duct taped
     * 
     * @param isTaped is the state of the hatch being taped or not
     */
    void setTaped(boolean isTaped);

    boolean isTaped();

    /**
     * Initial problems of the maintenance.
     */
    byte startProblems();

    /**
     * This value stores whether each of the 5 maintenance problems have been fixed.
     * A value of 0 means the problem is not fixed, else it is fixed
     * Value positions correspond to the following from left to right: 0=Wrench, 1=Screwdriver, 2=Soft Mallet, 3=Hard
     * Hammer, 4=Wire Cutter, 5=Crowbar
     */
    byte getMaintenanceProblems();

    void setMaintenanceProblems(byte problems);

    /**
     * Time is how long it is active and is used to determine how often problems occur.
     * See {@link IMaintenanceMachine#calculateTime(int)} for the trigger point.
     */
    int getTimeActive();

    void setTimeActive(int time);

    /**
     * Duration modifier for recipe. {@link IMaintenanceMachine#modifyRecipe(GTRecipe)}
     * It's configurable in the Configurable Maintenance Part.
     */
    default float getDurationMultiplier() {
        return 1;
    }

    /**
     * Higher {@link IMaintenanceMachine#getDurationMultiplier()} refers to a lower time multiplier.
     * The lower time multiplier means more likely causing problems.
     */
    default float getTimeMultiplier() {
        return 1;
    }

    @Override
    default boolean canShared() {
        return false;
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     *
     * @param duration in ticks to add to the counter of active time
     */
    default void calculateMaintenance(IMaintenanceMachine maintenanceMachine, int duration) {
        if (!ConfigHolder.INSTANCE.machines.enableMaintenance || maintenanceMachine.isFullAuto()) {
            return;
        }

        setTimeActive(getTimeActive() + duration);
        float rate = ConfigHolder.INSTANCE.machines.maintenanceCheckRate / maintenanceMachine.getTimeMultiplier();
        if (getTimeActive() >= rate) {
            setTimeActive(0);
            if (GTValues.RNG.nextInt(6000) == 0) {
                causeRandomMaintenanceProblems();
                maintenanceMachine.setTaped(false);
            }
        }
    }

    /**
     * Used to calculate whether a maintenance problem should happen based on machine time active
     */
    default void calculateMaintenance(IMaintenanceMachine maintenanceMachine) {
        calculateMaintenance(maintenanceMachine, 1);
    }

    default int getNumMaintenanceProblems() {
        return ConfigHolder.INSTANCE.machines.enableMaintenance ? 6 - Integer.bitCount(getMaintenanceProblems()) : 0;
    }

    default boolean hasMaintenanceProblems() {
        return ConfigHolder.INSTANCE.machines.enableMaintenance && this.getMaintenanceProblems() < 63;
    }

    default void setMaintenanceFixed(int index) {
        setMaintenanceProblems((byte) (getMaintenanceProblems() | (byte) (1 << index)));
    }

    default void causeRandomMaintenanceProblems() {
        setMaintenanceProblems(
                (byte) (getMaintenanceProblems() & (byte) ~(1 << GTValues.RNG.nextInt(6))));
    }

    @Override
    default boolean onWorking(IWorkableMultiController controller) {
        calculateMaintenance(this);
        if (hasMaintenanceProblems()) {
            controller.getRecipeLogic().markLastRecipeDirty();
        }
        return true;
    }

    @Override
    default GTRecipe modifyRecipe(GTRecipe recipe) {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            if (hasMaintenanceProblems()) {
                return null;
            }
            var durationMultiplier = getDurationMultiplier();
            if (durationMultiplier != 1) {
                recipe = recipe.copy();
                recipe.duration = (int) (recipe.duration * durationMultiplier);
            }
        }
        return recipe;
    }

    //////////////////////////////////////
    // ******* FANCY GUI ********//
    //////////////////////////////////////

    @Override
    default void attachFancyTooltipsToController(IMultiController controller, TooltipsPanel tooltipsPanel) {
        attachTooltips(tooltipsPanel);
    }

    @Override
    default void attachTooltips(TooltipsPanel tooltipsPanel) {
        if (ConfigHolder.INSTANCE.machines.enableMaintenance) {
            tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(() -> GuiTextures.MAINTENANCE_ICON, () -> {
                var tooltips = new ArrayList<Component>();
                tooltips.add(Component.translatable("gtceu.multiblock.universal.has_problems_header")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

                if ((getMaintenanceProblems() & 1) == 0)
                    tooltips.add(Component.translatable("gtceu.multiblock.universal.problem.wrench"));

                if (((getMaintenanceProblems() >> 1) & 1) == 0)
                    tooltips.add(Component.translatable("gtceu.multiblock.universal.problem.screwdriver"));

                if (((getMaintenanceProblems() >> 2) & 1) == 0)
                    tooltips.add(Component.translatable("gtceu.multiblock.universal.problem.soft_mallet"));

                if (((getMaintenanceProblems() >> 3) & 1) == 0)
                    tooltips.add(Component.translatable("gtceu.multiblock.universal.problem.hard_hammer"));

                if (((getMaintenanceProblems() >> 4) & 1) == 0)
                    tooltips.add(Component.translatable("gtceu.multiblock.universal.problem.wire_cutter"));

                if (((getMaintenanceProblems() >> 5) & 1) == 0)
                    tooltips.add(Component.translatable("gtceu.multiblock.universal.problem.crowbar"));

                return tooltips;
            }, this::hasMaintenanceProblems, () -> null));
        }
    }
}
