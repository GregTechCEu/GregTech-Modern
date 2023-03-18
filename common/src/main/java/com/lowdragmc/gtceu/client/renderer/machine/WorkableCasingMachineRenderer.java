package com.lowdragmc.gtceu.client.renderer.machine;

import com.lowdragmc.gtceu.GTCEu;
import com.lowdragmc.gtceu.api.capability.IWorkable;
import com.lowdragmc.gtceu.api.machine.MachineDefinition;
import com.lowdragmc.gtceu.api.machine.MetaMachine;
import com.lowdragmc.gtceu.client.model.WorkableOverlayModel;
import com.lowdragmc.gtceu.core.mixins.BlockModelAccessor;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/3/4
 * @implNote WorkableCasingMachineRenderer
 */
public class WorkableCasingMachineRenderer extends MachineRenderer {

    protected final WorkableOverlayModel overlayModel;
    protected final ResourceLocation baseCasing;

    public WorkableCasingMachineRenderer(ResourceLocation baseCasing, ResourceLocation workableModel) {
        this(baseCasing, workableModel, true);
    }

    public WorkableCasingMachineRenderer(ResourceLocation baseCasing, ResourceLocation workableModel, boolean tint) {
        super(tint ? GTCEu.id("block/tinted_cube_all") : GTCEu.id("block/cube_all"));
        this.baseCasing = baseCasing;
        this.overlayModel = new WorkableOverlayModel(workableModel);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected UnbakedModel getModel() {
        var model = super.getModel();
        if (model instanceof BlockModelAccessor blockModelAccessor) {
            blockModelAccessor.getTextureMap().put("all", ModelFactory.parseBlockTextureLocationOrReference(baseCasing.toString()));
        }
        return super.getModel();
    }

    @Override
    public BakedModel getRotatedModel(Direction frontFacing) {
        return blockModels.computeIfAbsent(frontFacing, facing -> getModel().bake(
                ModelFactory.getModeBakery(),
                Material::sprite,
                ModelFactory.getRotation(facing, true), // lock UV here!!!!
                modelLocation));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderMachine(List<BakedQuad> quads, MachineDefinition definition, @Nullable MetaMachine machine, Direction frontFacing, @Nullable Direction side, RandomSource rand, Direction modelFacing, ModelState modelState) {
        super.renderMachine(quads, definition, machine, frontFacing, side, rand, modelFacing, modelState);
        if (machine instanceof IWorkable workable) {
            quads.addAll(overlayModel.bakeQuads(side, frontFacing, workable.isActive(), workable.isWorkingEnabled()));
        } else {
            quads.addAll(overlayModel.bakeQuads(side, frontFacing, false, false));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            register.accept(baseCasing);
            overlayModel.registerTextureAtlas(register);
        }
    }
}
