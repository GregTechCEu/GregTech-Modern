package com.gregtechceu.gtceu.client.model.machine.impl;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPartRenderer {

    /**
     * Render part according to its controllers.
     * 
     * @return whether its model has been replaced with controller's model
     */
    @OnlyIn(Dist.CLIENT)
    default boolean renderReplacedPartMachine(List<BakedQuad> quads, IMultiPart part, Direction frontFacing,
                                              @Nullable Direction side, RandomSource rand, Direction modelFacing,
                                              ModelState modelState, @NotNull ModelData modelData,
                                              RenderType renderType) {
        var controllers = part.getControllers();
        for (IMultiController controller : controllers) {
            var state = controller.self().getBlockState();
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

            if (model instanceof IControllerRenderer controllerRenderer) {
                controllerRenderer.renderPartModel(quads, controller, part, frontFacing, side, rand, modelFacing,
                        modelState);
                return true;
            } else if (model instanceof MachineModel machineModel) {
                machineModel.renderBaseModel(quads, controller.self().getDefinition(), controller.self(),
                        modelState, side, rand, modelData, renderType);
                return true;
            }
        }
        return false;
    }
}
