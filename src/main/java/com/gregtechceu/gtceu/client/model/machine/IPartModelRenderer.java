package com.gregtechceu.gtceu.client.model.machine;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.CTMBakedModel;

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

import java.util.ArrayList;
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

            // spotless:off
            if (model instanceof IControllerModelRenderer controllerRenderer) {
                controllerRenderer.renderPartModel(quads, controller, part, frontFacing, modelFront,
                        rand, elementSide, modelData, renderType);
                return true;
            } else if (model instanceof MachineModel machineModel) {
                renderMachineModel(machineModel, controller, quads, part, frontFacing, modelFront,
                        elementSide, rand, modelData, renderType);
                return true;
            } else if (model instanceof CTMBakedModel<?> ctmModel &&
                    ctmModel.getParent() instanceof MachineModel machineModel) {
                List<BakedQuad> toConnect = new ArrayList<>();
                renderMachineModel(machineModel, controller, toConnect, part, frontFacing, modelFront,
                        elementSide, rand, modelData, renderType);
                quads.addAll(CTMBakedModel.reBakeCustomQuads(toConnect,
                        controller.self().getLevel(), controller.self().getPos(), state, elementSide));
                return true;
            }
            // spotless:on
        }
        return false;
    }

    private void renderMachineModel(MachineModel model, IMultiController controller,
                                    List<BakedQuad> quads, IMultiPart part, Direction frontFacing,
                                    @Nullable Direction modelFront, @Nullable Direction elementSide,
                                    RandomSource rand, ModelData modelData, @Nullable RenderType renderType) {
        for (var render : model.getDynamicRenders()) {
            if (render instanceof IControllerModelRenderer controllerRenderer) {
                controllerRenderer.renderPartModel(quads, controller, part, frontFacing, modelFront,
                        rand, elementSide, modelData, renderType);
                return;
            }
        }
        model.renderBaseModel(quads, controller.self(), elementSide, rand, modelData, renderType);
    }
}
