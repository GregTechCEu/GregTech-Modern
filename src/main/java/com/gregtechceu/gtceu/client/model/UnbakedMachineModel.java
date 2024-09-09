package com.gregtechceu.gtceu.client.model;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class UnbakedMachineModel implements IUnbakedGeometry<UnbakedMachineModel> {

    private final MachineDefinition definition;
    private final boolean isItem;

    public UnbakedMachineModel(ResourceLocation machineId, boolean isItem) {
        this.definition = GTRegistries.MACHINES.get(machineId);
        this.isItem = isItem;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState state,
                           ItemOverrides overrides, ResourceLocation modelLocation) {
        return new BakedMachineModel();
    }

    public static final class Loader implements IGeometryLoader<UnbakedMachineModel> {

        public static final Loader INSTANCE = new Loader();

        private Loader() {}

        @Override
        public UnbakedMachineModel read(JsonObject jsonObject,
                                        JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            ResourceLocation machineId = new ResourceLocation(GsonHelper.getAsString(jsonObject, "machine"));
            return new UnbakedMachineModel(machineId, GsonHelper.getAsBoolean(jsonObject, "is_item"));
        }
    }

    public final class BakedMachineModel implements IDynamicBakedModel {

        @Override
        public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                                 @NotNull RandomSource rand,
                                                 @NotNull ModelData modelData, @Nullable RenderType renderType) {
            if (isItem) {
                List<BakedQuad> quads = new ArrayList<>();
                definition.getRenderer().renderMachine(quads, definition, null, Direction.NORTH,
                        side, rand, side, BlockModelRotation.X0_Y0, modelData, renderType);
                return quads;
            } else {
                return definition.getRenderer().getQuads(state, side, rand, modelData, renderType);
            }
        }

        @Override
        public boolean useAmbientOcclusion() {
            return false;
        }

        public boolean isGui3d() {
            return true;
        }

        public boolean usesBlockLight() {
            return true;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return null;
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }

        @Override
        public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                               @NotNull BlockState state, @NotNull ModelData modelData) {
            return IDynamicBakedModel.super.getModelData(level, pos, state,
                    definition.getRenderer().getModelData(level, pos, state, modelData));
        }
    }
}
