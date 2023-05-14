package com.gregtechceu.gtceu.client.renderer.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.model.WorkableOverlayModel;
import com.gregtechceu.gtceu.core.mixins.BlockModelAccessor;
import com.gregtechceu.gtlib.client.model.ModelFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class WorkableSidedCasingMachineRenderer extends MachineRenderer {

    protected final ResourceLocation top, bottom, side;
    protected final WorkableOverlayModel overlayModel;

    public WorkableSidedCasingMachineRenderer(String basePath, ResourceLocation workableModel, boolean tint) {
        super(tint ? GTCEu.id("block/cube_bottom_top_tintindex") : new ResourceLocation("block/cube_bottom_top"));
        this.top = GTCEu.id(basePath + "/top");
        this.bottom = GTCEu.id(basePath + "/bottom");
        this.side = GTCEu.id(basePath + "/side");
        this.overlayModel = new WorkableOverlayModel(workableModel);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected UnbakedModel getModel() {
        var model = super.getModel();
        if (model instanceof BlockModelAccessor blockModelAccessor) {
            blockModelAccessor.getTextureMap().put("bottom", ModelFactory.parseBlockTextureLocationOrReference(bottom.toString()));
            blockModelAccessor.getTextureMap().put("top", ModelFactory.parseBlockTextureLocationOrReference(top.toString()));
            blockModelAccessor.getTextureMap().put("side", ModelFactory.parseBlockTextureLocationOrReference(side.toString()));
        }
        return model;
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
            register.accept(top);
            register.accept(side);
            register.accept(bottom);
            overlayModel.registerTextureAtlas(register);
        }
    }

    @NotNull
    @Override
    @Environment(EnvType.CLIENT)
    public TextureAtlasSprite getParticleTexture() {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(side);
    }
}
