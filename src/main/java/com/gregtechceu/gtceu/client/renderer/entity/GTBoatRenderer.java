package com.gregtechceu.gtceu.client.renderer.entity;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.entity.GTBoat;
import com.gregtechceu.gtceu.common.entity.GTChestBoat;

import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;

import java.util.Map;
import java.util.stream.Stream;

public class GTBoatRenderer extends BoatRenderer {

    private final Map<GTBoat.BoatType, Pair<ResourceLocation, ListModel<Boat>>> boats;

    public GTBoatRenderer(EntityRendererProvider.Context context, boolean chestBoat) {
        super(context, chestBoat);
        boats = Stream.of(GTBoat.BoatType.values()).collect(ImmutableMap.toImmutableMap(k -> k,
                (m) -> Pair.of(new ResourceLocation(GTCEu.MOD_ID,
                        getTextureLocation(m, chestBoat)), createBoatModel(context, m, chestBoat))));
    }

    @Override
    public Pair<ResourceLocation, ListModel<Boat>> getModelWithLocation(Boat boat) {
        if (boat instanceof GTChestBoat gtcb) {
            return this.boats.get(gtcb.getBoatType());
        } else
            return this.boats.get(((GTBoat) boat).getBoatType());
    }

    private static String getTextureLocation(GTBoat.BoatType type, boolean chest) {
        return chest ? "textures/entity/boat/" + type.getName() + "_chest_boat.png" :
                "textures/entity/boat/" + type.getName() + "_boat.png";
    }

    private BoatModel createBoatModel(EntityRendererProvider.Context context, GTBoat.BoatType type, boolean chest) {
        ModelLayerLocation modelLoc = chest ? getChestBoatModelName(type) : getBoatModelName(type);
        ModelPart part = context.bakeLayer(modelLoc);
        return chest ? new ChestBoatModel(part) : new BoatModel(part);
    }

    public static ModelLayerLocation getChestBoatModelName(GTBoat.BoatType type) {
        return new ModelLayerLocation(new ResourceLocation(GTCEu.MOD_ID, "chest_boat/" + type.getName()), "main");
    }

    public static ModelLayerLocation getBoatModelName(GTBoat.BoatType type) {
        return new ModelLayerLocation(new ResourceLocation(GTCEu.MOD_ID, "boat/" + type.getName()), "main");
    }
}
