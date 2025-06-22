package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LargeBoilerRenderer extends WorkableCasingMachineRenderer implements IControllerRenderer {

    public static final ResourceLocation BLOOM_OVERLAY = GTCEu.id("block/casings/firebox/machine_casing_firebox_bloom");
    public final BoilerFireboxType firebox;

    public LargeBoilerRenderer(ResourceLocation texture, BoilerFireboxType firebox, ResourceLocation workableModel) {
        super(texture, workableModel);
        this.firebox = firebox;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part, Direction frontFacing,
                                @Nullable Direction side, RandomSource rand, Direction modelFacing,
                                ModelState modelState) {
        if (side == null) {
            return;
        }

        var multiFront = MetaMachine.getFrontFacing(machine.self());
        var multiUpward = MetaMachine.getUpwardFacing(machine.self());
        var multiState = GTMatrixUtils.createRotationState(multiFront, Direction.NORTH);
        var multiFacing = ModelFactory.modelFacing(side, multiFront);
        var flipped = machine.self().isFlipped();
        var relativeDown = RelativeDirection.DOWN.getRelative(multiFront, multiUpward, flipped);
        // the rest of the owl
        if (machine.self().getPos().relative(relativeDown).get(relativeDown.getAxis()) !=
                part.self().getPos().get(relativeDown.getAxis())) {
            quads.add(StaticFaceBakery.bakeFace(multiFacing, ModelFactory.getBlockSprite(baseCasing), multiState));
            return;
        }
        // firebox
        if (side == relativeDown) {
            quads.add(
                    StaticFaceBakery.bakeFace(multiFacing, ModelFactory.getBlockSprite(firebox.bottom()), multiState));
        } else if (side == RelativeDirection.UP.getRelative(multiFacing, multiUpward, flipped)) {
            quads.add(StaticFaceBakery.bakeFace(multiFacing, ModelFactory.getBlockSprite(firebox.top()), multiState));
        } else {
            quads.add(StaticFaceBakery.bakeFace(multiFacing, ModelFactory.getBlockSprite(firebox.side()), multiState));
            if (machine instanceof IRecipeLogicMachine recipeLogicMachine &&
                    recipeLogicMachine.getRecipeLogic().isWorking()) {
                quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.SLIGHTLY_OVER_BLOCK, multiFacing,
                        ModelFactory.getBlockSprite(BLOOM_OVERLAY), multiState, -101, 15, true, false));
            }
        }
    }
}
