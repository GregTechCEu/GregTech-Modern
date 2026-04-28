package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.compat.BakedModel;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverableRenderer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
public class ModelUtils {

    private ModelUtils() {}

    private static final List<EventListenerHolder> EVENT_LISTENERS = new ArrayList<>();

    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        return model.getQuads(state, side, rand, model.getModelData(level, pos, state, ModelData.EMPTY), null);
    }

    public static BakedModel getModelForState(BlockState state) {
        return new BlockStateBakedModel(state);
    }

    public static String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
        Property<?> property = entry.getKey();
        Comparable<?> value = entry.getValue();
        return getPropertyValueString(property, value);
    }

    public static String getPropertyValueString(Property.Value<?> entry) {
        return getPropertyValueString(entry.property(), entry.value());
    }

    private static String getPropertyValueString(Property<?> property, Comparable<?> value) {
        String valueString = Util.getPropertyName(property, value);
        if (Boolean.TRUE.equals(value)) {
            valueString = ChatFormatting.GREEN + valueString;
        } else if (Boolean.FALSE.equals(value)) {
            valueString = ChatFormatting.RED + valueString;
        }

        return property.getName() + ": " + valueString;
    }

    public static void registerAtlasStitchedEventListener(boolean removeOnReload,
                                                          AssetEventListener.AtlasStitched listener) {
        EVENT_LISTENERS.add(new EventListenerHolder(listener, removeOnReload));
    }

    public static void registerAtlasStitchedEventListener(boolean removeOnReload, final Identifier atlasLocation,
                                                          final AssetEventListener.AtlasStitched listener) {
        registerAtlasStitchedEventListener(removeOnReload, event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                listener.accept(event);
            }
        });
    }

    public static void registerBakeEventListener(boolean removeOnReload,
                                                 AssetEventListener.ModifyBakingResult listener) {
        EVENT_LISTENERS.add(new EventListenerHolder(listener, removeOnReload));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerReloadListener(AddClientReloadListenersEvent event) {
        event.addListener(GTCEu.id("model_utils_reload"),
                (ResourceManagerReloadListener) resourceManager -> EVENT_LISTENERS
                        .removeIf(EventListenerHolder::removeOnReload));
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAtlasStitched(TextureAtlasStitchedEvent event) {
        TextureAtlas atlas = event.getAtlas();
        if (atlas.location() == TextureAtlas.LOCATION_BLOCKS) {
            MachineModel.initSprites(atlas);
            ICoverableRenderer.initSprites(atlas);
        }

        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<TextureAtlasStitchedEvent>) listener.listener).accept(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.ModifyBakingResult>) listener.listener).accept(event);
            }
        }

        // LDLib2's 26.x model path no longer exposes the old CustomBakedModel CTM wrapper here.
    }

    private record EventListenerHolder(AssetEventListener<?> listener, boolean removeOnReload) {}

    private record BlockStateBakedModel(BlockState blockState) implements BakedModel {

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand,
                                        ModelData modelData, RenderType renderType) {
            var parts = new ArrayList<BlockStateModelPart>();
            Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState).collectParts(rand, parts);

            List<BakedQuad> quads = new ArrayList<>();
            for (BlockStateModelPart part : parts) {
                for (BakedQuad quad : part.getQuads(side)) {
                    if (renderType == null || quad.materialInfo().itemRenderType() == renderType ||
                            quad.materialInfo().layer().pipeline() == renderType.pipeline()) {
                        quads.add(quad);
                    }
                }
            }
            return quads;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState)
                    .particleMaterial().sprite();
        }
    }
}
