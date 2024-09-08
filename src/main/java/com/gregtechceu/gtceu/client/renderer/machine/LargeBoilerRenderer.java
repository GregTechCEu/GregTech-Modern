package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.gregtechceu.gtceu.common.block.BoilerFireboxType;

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

/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote LargeBoilerRenderer
 */
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
        // We have to render it ourselves to avoid uv issues
        if (machine.self().getPos().below().getY() == part.self().getPos().getY()) {
            // firebox
            if (side != null && modelFacing != null) {
                if (side == Direction.UP) {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(firebox.top()), modelState));
                } else if (side == Direction.DOWN) {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(firebox.bottom()), modelState));
                } else {
                    quads.add(StaticFaceBakery.bakeFace(modelFacing,
                            ModelFactory.getBlockSprite(firebox.side()), modelState));
                    if (machine instanceof IRecipeLogicMachine recipeLogicMachine &&
                            recipeLogicMachine.getRecipeLogic().isWorking()) {
                        quads.add(StaticFaceBakery.bakeFace(StaticFaceBakery.SLIGHTLY_OVER_BLOCK,
                                modelFacing, ModelFactory.getBlockSprite(BLOOM_OVERLAY), modelState,
                                -101, 15, true, false));
                    }
                }
            }
        } else {
            if (side != null && modelFacing != null) {
                quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(baseCasing), modelState));
            }
        }
    }
}
