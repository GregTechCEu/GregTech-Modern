package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.machine.MachineModel;

import com.lowdragmc.lowdraglib.client.model.custommodel.CustomBakedModel;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelUtils {

    private ModelUtils() {}

    private static final List<AssetEventListener<?>> EVENT_LISTENERS = new ArrayList<>();

    public static List<BakedQuad> getBakedModelQuads(BakedModel model, BlockAndTintGetter level, BlockPos pos,
                                                     BlockState state, Direction side, RandomSource rand) {
        return model.getQuads(state, side, rand, model.getModelData(level, pos, state, ModelData.EMPTY), null);
    }

    public static String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> entry) {
        Property<?> property = entry.getKey();
        Comparable<?> value = entry.getValue();

        String valueString = Util.getPropertyName(property, value);
        if (Boolean.TRUE.equals(value)) {
            valueString = ChatFormatting.GREEN + valueString;
        } else if (Boolean.FALSE.equals(value)) {
            valueString = ChatFormatting.RED + valueString;
        }

        return property.getName() + ": " + valueString;
    }

    public static void registerAtlasStitchedEventListener(AssetEventListener.AtlasStitched listener) {
        EVENT_LISTENERS.add(listener);
    }

    public static void registerAtlasStitchedEventListener(final ResourceLocation atlasLocation,
                                                          final AssetEventListener.AtlasStitched listener) {
        EVENT_LISTENERS.add((AssetEventListener.AtlasStitched) event -> {
            if (event.getAtlas().location().equals(atlasLocation)) {
                listener.accept(event);
            }
        });
    }

    public static void registerBakeEventListener(AssetEventListener.ModifyBakingResult listener) {
        EVENT_LISTENERS.add(listener);
    }

    public static void registerAddModelsEventListener(AssetEventListener.RegisterAdditional listener) {
        EVENT_LISTENERS.add(listener);
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAtlasStitched(TextureStitchEvent.Post event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<TextureStitchEvent.Post>) listener).accept(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        // Unwrap all machine models from the LDLib CTM models so we don't need to be as aggressive with mixins.
        // Also, the caching they have stops our models from updating properly.
        for (var entry : event.getModels().entrySet()) {
            BakedModel model = entry.getValue();
            if (!(model instanceof CustomBakedModel ctmModel)) {
                continue;
            }
            if (ctmModel.getParent() instanceof MachineModel machine) {
                entry.setValue(machine);
            }
        }

        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.ModifyBakingResult>) listener).accept(event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (var listener : EVENT_LISTENERS) {
            Class<?> eventClass = listener.eventClass();
            if (eventClass != null && eventClass.isInstance(event)) {
                ((AssetEventListener<ModelEvent.RegisterAdditional>) listener).accept(event);
            }
        }
    }
}
