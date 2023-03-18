package com.lowdragmc.gtceu.client.renderer.machine;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.lowdragmc.gtceu.api.machine.feature.multiblock.IMultiController;
import com.lowdragmc.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.lowdragmc.gtceu.common.block.variant.BoilerFireBoxCasingBlock;
import com.lowdragmc.gtceu.common.block.variant.CasingBlock;
import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote LargeBoilerRenderer
 */
public class LargeBoilerRenderer extends WorkableCasingMachineRenderer implements IControllerRenderer{
    public static final ResourceLocation BLOOM_OVERLAY = GTCEu.id("block/casings/firebox/machine_casing_firebox_bloom");
    public final CasingBlock.CasingType casingType;
    public final BoilerFireBoxCasingBlock.CasingType firebox;

    public LargeBoilerRenderer(CasingBlock.CasingType casingType, BoilerFireBoxCasingBlock.CasingType firebox, ResourceLocation workableModel) {
        super(casingType.getTexture(), workableModel, false);
        this.casingType = casingType;
        this.firebox = firebox;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, IMultiController machine, IMultiPart part, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        // We have to render it ourselves to avoid uv issues
        if (machine.self().getPos().below().getY() == part.self().getPos().getY()) {
            // firebox
            if (side != null && modelFacing != null) {
                if (side == Direction.UP) {
                    quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(firebox.getTop()), modelState));
                } else if (side == Direction.DOWN) {
                    quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(firebox.getBottom()), modelState));
                } else {
                    quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(firebox.getSide()), modelState));
                    if (machine instanceof IRecipeLogicMachine recipeLogicMachine && recipeLogicMachine.getRecipeLogic().isWorking()) {
                        quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(BLOOM_OVERLAY), modelState, -1, 15, true, false));
                    }
                }
            }
        } else {
            if (side != null && modelFacing != null) {
                quads.add(FaceQuad.bakeFace(modelFacing, ModelFactory.getBlockSprite(casingType.getTexture()), modelState));
            }
        }
    }
}
