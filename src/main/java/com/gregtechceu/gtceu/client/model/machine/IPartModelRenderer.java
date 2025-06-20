package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IPartModelRenderer {

    /**
     * Render part according to its controllers.
     * 
     * @return whether its model has been replaced with controller's model
     */
    @OnlyIn(Dist.CLIENT)
    default boolean renderReplacedPartMachine(List<BakedQuad> quads, IMultiPart part, Direction frontFacing,
                                              @Nullable Direction elementSide, @Nullable Direction modelFront,
                                              RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        var controllers = part.getControllers();
        for (IMultiController controller : controllers) {
            var state = controller.self().getBlockState();
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

            if (model instanceof IControllerModelRenderer controllerRenderer) {
                controllerRenderer.renderPartModel(quads, controller, part, frontFacing, modelFront,
                        rand, elementSide, modelData, renderType);
                return true;
            } else if (model instanceof MachineModel machineModel) {
                for (var render : machineModel.getDynamicRenders()) {
                    if (render instanceof IControllerModelRenderer controllerRenderer) {
                        controllerRenderer.renderPartModel(quads, controller, part, frontFacing, modelFront,
                                rand, elementSide, modelData, renderType);
                        return true;
                    }
                }
                machineModel.renderBaseModel(quads, controller.self(), elementSide, rand, modelData, renderType);
                return true;
            }
        }
        return false;
    }
}
