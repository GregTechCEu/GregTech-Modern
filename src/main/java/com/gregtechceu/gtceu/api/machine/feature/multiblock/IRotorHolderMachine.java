package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IRotorHolderMachine extends IMultiPart {

    int SPEED_INCREMENT = 1;
    int SPEED_DECREMENT = 3;

    BooleanProperty HAS_ROTOR_PROPERTY = GTMachineModelProperties.HAS_ROTOR;
    BooleanProperty ROTOR_SPINNING_PROPERTY = GTMachineModelProperties.IS_ROTOR_SPINNING;
    BooleanProperty EMISSIVE_ROTOR_PROPERTY = GTMachineModelProperties.IS_EMISSIVE_ROTOR;

    /**
     * @return the base efficiency of the rotor holder in %
     */
    static int getBaseEfficiency() {
        return 100;
    }

    @NotNull
    Material getRotorMaterial();

    ItemStack getRotorStack();

    void setRotorStack(ItemStack rotorStack);

    /**
     * @return the current speed of the holder
     */
    int getRotorSpeed();

    /**
     * @return the current speed of the holder
     */
    void setRotorSpeed(int speed);

    /**
     *
     * @return the maximum speed the holder can have
     */
    int getMaxRotorHolderSpeed();

    /**
     * Tier difference between the rotor holder and it's controller.
     */
    int getTierDifference();

    /**
     * @return the efficiency provided by the rotor holder in %
     */
    default int getHolderEfficiency() {
        int tierDifference = getTierDifference();
        if (tierDifference == -1)
            return -1;

        return 100 + 10 * tierDifference;
    }

    /**
     * @return the power multiplier provided by the rotor holder
     */
    default int getHolderPowerMultiplier() {
        int tierDifference = getTierDifference();
        if (tierDifference == -1) return -1;

        return (int) Math.pow(2, getTierDifference());
    }

    /**
     * returns true on both the Client and Server
     *
     * @return whether there is a rotor in the holder
     */
    default boolean hasRotor() {
        return TurbineRotorBehaviour.getBehaviour(getRotorStack()) != null;
    }

    /**
     * @return the rotor's efficiency in %
     */
    default int getRotorEfficiency() {
        var stack = getRotorStack();
        var behavior = TurbineRotorBehaviour.getBehaviour(stack);
        if (behavior != null) {
            return behavior.getRotorEfficiency(stack);
        }
        return -1;
    }

    /**
     * @return the rotor's power in %
     */
    default int getRotorPower() {
        var stack = getRotorStack();
        var behavior = TurbineRotorBehaviour.getBehaviour(stack);
        if (behavior != null) {
            return behavior.getRotorPower(stack);
        }
        return -1;
    }

    /**
     * @return the rotor's durability as %
     */
    default int getRotorDurabilityPercent() {
        var stack = getRotorStack();
        var behavior = TurbineRotorBehaviour.getBehaviour(stack);
        if (behavior != null) {
            return behavior.getRotorDurabilityPercent(stack);
        }
        return -1;
    }

    /**
     * damages the rotor
     *
     * @param damageAmount to damage
     */
    default void damageRotor(int damageAmount) {
        var stack = getRotorStack();
        var behavior = TurbineRotorBehaviour.getBehaviour(stack);
        if (behavior != null) {
            behavior.applyRotorDamage(stack, damageAmount);
            setRotorStack(stack);
        }
    }

    /**
     * @return true if the front face is unobstructed
     */
    default boolean isFrontFaceFree() {
        final var facing = self().getFrontFacing();
        final var up = facing.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
        final var pos = self().getPos();
        final var level = self().getLevel();
        for (int dLeft = -1; dLeft < 2; dLeft++) {
            for (int dUp = -1; dUp < 2; dUp++) {
                final var checkPos = RelativeDirection.offsetPos(pos, facing, up, false, dUp, dLeft, 1);
                if (!level.getBlockState(checkPos).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return the total efficiency the rotor holder and rotor provide in %
     */
    default int getTotalEfficiency() {
        int rotorEfficiency = getRotorEfficiency();
        if (rotorEfficiency == -1)
            return -1;

        int holderEfficiency = getHolderEfficiency();
        if (holderEfficiency == -1)
            return -1;

        return Math.max(getBaseEfficiency(), rotorEfficiency * holderEfficiency / 100);
    }

    /**
     *
     * @return the total power boost to output and consumption the rotor holder and rotor provide in %
     */
    default int getTotalPower() {
        return getHolderPowerMultiplier() * getRotorPower();
    }

    default boolean isRotorSpinning() {
        return getRotorSpeed() > 0;
    }

    //////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    //////////////////////////////////////
    @Override
    default GTRecipe modifyRecipe(GTRecipe recipe) {
        if (!isFrontFaceFree() || !hasRotor()) {
            return null;
        }
        return IMultiPart.super.modifyRecipe(recipe);
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
        tooltipsPanel.attachTooltips(new IFancyTooltip.Basic(
                () -> GuiTextures.INDICATOR_NO_STEAM.get(false),
                () -> List.of(Component.translatable("gtceu.multiblock.universal.rotor_obstructed")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                () -> !isFrontFaceFree(),
                () -> null));
    }
}
