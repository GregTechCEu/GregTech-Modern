package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.model.SpriteOverrider;
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
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/4/1
 * @implNote ElectricGearBoxRenderer
 */
public class SplitShaftTieredHullMachineRenderer extends TieredHullMachineRenderer implements ISplitShaftRenderer {

    public SplitShaftTieredHullMachineRenderer(int tier, ResourceLocation modelLocation) {
        super(tier, modelLocation);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean renderReplacedPartMachine(List<BakedQuad> quads, IMultiPart part, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        var controllers = part.getControllers();
        for (IMultiController controller : controllers) {
            var state = controller.self().getBlockState();
            if (state.getBlock() instanceof MetaMachineBlock block) {
                var renderer = block.definition.getRenderer();
                if (renderer instanceof WorkableCasingMachineRenderer workableCasingMachineRenderer) {
                    var baseTexture = workableCasingMachineRenderer.baseCasing;
                    var bakeModel =  ModelFactory.getUnBakedModel(modelLocation).bake(
                            ModelFactory.getModeBaker(),
                            new SpriteOverrider(Map.of("bottom", baseTexture, "top", baseTexture, "side", baseTexture)),
                            ModelFactory.getRotation(frontFacing),
                            modelLocation);
                    if (bakeModel != null) {
                        quads.addAll(bakeModel.getQuads(part.self().getDefinition().defaultBlockState(), side, rand));
                        return true;
                    }
                } else if (renderer instanceof IControllerRenderer controllerRenderer) {
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
