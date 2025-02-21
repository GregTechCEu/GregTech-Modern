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

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
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

        float xPos = facing.getStepX() * 0.76F + pos.getX() + 0.25F;
        float yPos = facing.getStepY() * 0.76F + pos.getY() + 0.25F;
        float zPos = facing.getStepZ() * 0.76F + pos.getZ() + 0.25F;

        float ySpd = facing.getStepY() * 0.1F + 0.2F + 0.1F * GTValues.RNG.nextFloat();
        float xSpd;
        float zSpd;

        if (facing.getStepY() == -1) {
            float temp = GTValues.RNG.nextFloat() * 2 * (float) Math.PI;
            xSpd = (float) Math.sin(temp) * 0.1F;
            zSpd = (float) Math.cos(temp) * 0.1F;
        } else {
            xSpd = facing.getStepX() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
            zSpd = facing.getStepZ() * (0.1F + 0.2F * GTValues.RNG.nextFloat());
        }
        self().getLevel().addParticle(ParticleTypes.LARGE_SMOKE,
                xPos + GTValues.RNG.nextFloat() * 0.5F,
                yPos + GTValues.RNG.nextFloat() * 0.5F,
                zPos + GTValues.RNG.nextFloat() * 0.5F,
                xSpd, ySpd, zSpd);
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
