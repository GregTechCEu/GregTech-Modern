package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/7/10
 * @implNote IRotorHolderMachine
 */
public interface IRotorHolderMachine extends IMultiPart {

    int SPEED_INCREMENT = 1;
    int SPEED_DECREMENT = 3;

    /**
     * @return the base efficiency of the rotor holder in %
     */
    static int getBaseEfficiency() {
        return 100;
    }

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
        var facing = self().getFrontFacing();
        boolean permuteXZ = facing.getAxis() == Direction.Axis.Z;
        var centerPos = self().getPos().relative(facing);
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                var blockPos = centerPos.offset(permuteXZ ? x : 0, y, permuteXZ ? 0 : x);
                var blockState = self().getLevel().getBlockState(blockPos);
                if (!blockState.isAir()) {
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
