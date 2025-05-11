package com.gregtechceu.gtceu.api.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IHazardParticleContainer;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyTooltip;
import com.gregtechceu.gtceu.api.gui.fancy.TooltipsPanel;
import com.gregtechceu.gtceu.api.machine.feature.IEnvironmentalHazardEmitter;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTParticleTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMufflerMachine extends IMultiPart, IEnvironmentalHazardEmitter {

    void recoverItemsTable(ItemStack... recoveryItems);

    /**
     * @return true if front face is free and contains only air blocks in 1x1 area OR has a duct block on it.
     */
    default boolean isFrontFaceFree() {
        var frontPos = self().getPos().relative(self().getFrontFacing());
        return self().getLevel().getBlockState(frontPos).isAir() ||
                GTCapabilityHelper.getHazardContainer(self().getLevel(),
                        frontPos, self().getFrontFacing().getOpposite()) != null;
    }

    default void emitPollutionParticles() {
        var pos = self().getPos();
        var facing = self().getFrontFacing();

        IHazardParticleContainer container = GTCapabilityHelper.getHazardContainer(self().getLevel(),
                pos.relative(facing), facing.getOpposite());
        if (container != null) {
            // do not emit particles if front face has a duct on it.
            return;
        }

        var center = pos.getCenter();
        var offset = .75f;
        var xPos = (float) (center.x + facing.getStepX() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);
        var yPos = (float) (center.y + facing.getStepY() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);
        var zPos = (float) (center.z + facing.getStepZ() * offset + (GTValues.RNG.nextFloat() - .5f) * .35f);

        var ySpd = facing.getStepY() + (GTValues.RNG.nextFloat() - .15f) * .5f;
        var xSpd = facing.getStepX() + (GTValues.RNG.nextFloat() - .5f) * .5f;
        var zSpd = facing.getStepZ() + (GTValues.RNG.nextFloat() - .5f) * .5f;

        self().getLevel().addParticle(GTParticleTypes.MUFFLER_PARTICLE.get(),
                xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }

    @Override
    default GTRecipe modifyRecipe(GTRecipe recipe) {
        if (!isFrontFaceFree()) {
            return null;
        }
        return IMultiPart.super.modifyRecipe(recipe);
    }

    @Override
    default float getHazardStrengthPerOperation() {
        float outputAmount = 2.5f;
        return this instanceof TieredPartMachine tiered ? outputAmount / Math.max(tiered.getTier(), 1) : outputAmount;
    }

    @Override
    default boolean afterWorking(IWorkableMultiController controller) {
        spreadEnvironmentalHazard();
        var supplier = controller.self().getDefinition().getRecoveryItems();
        if (supplier != null) {
            recoverItemsTable(supplier.get());
        }
        return IMultiPart.super.afterWorking(controller);
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
                () -> List.of(Component.translatable("gtceu.multiblock.universal.muffler_obstructed")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))),
                () -> !isFrontFaceFree(),
                () -> null));
    }
}
