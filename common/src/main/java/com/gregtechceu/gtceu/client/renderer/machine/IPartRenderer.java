package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/16
 * @implNote IPartRenderer
 */
public interface IPartRenderer {
    /**
     * Render part according to its controllers.
     * @return whether its model has been replaced with controller's model
     */
    @Environment(EnvType.CLIENT)
    default boolean renderReplacedPartMachine(List<BakedQuad> quads, IMultiPart part, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        var controllers = part.getControllers();
        for (IMultiController controller : controllers) {
            var state = controller.self().getBlockState();
            if (state.getBlock() instanceof MetaMachineBlock block) {
                var renderer = block.definition.getRenderer();
                if (renderer instanceof IControllerRenderer controllerRenderer) {
                    controllerRenderer.renderPartModel(quads, controller, part, frontFacing, side, rand, modelFacing, modelState);
                    return true;
                } else if (renderer instanceof MachineRenderer machineRenderer) {
                    machineRenderer.renderBaseModel(quads, block.definition, controller.self(), frontFacing, side, rand);
                    return true;
                }
            }
        }
        return false;
    }

}
