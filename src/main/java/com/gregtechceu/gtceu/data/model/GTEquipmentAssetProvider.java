package com.gregtechceu.gtceu.data.model;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ArmorProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Emits {@code assets/<ns>/equipment/<material>.json} for every material with an
 * {@link ArmorProperty}. 1.21.5+ requires these per-material equipment definitions to render
 * humanoid armor layers; without them, gtceu armor renders invisible.
 *
 * <p>
 * The set of unique {@link ArmorProperty#getAssetId() assetIds} is collected first so that
 * materials that share a texture key (e.g. all metal armors keyed at {@code gtceu:metal})
 * emit a single shared JSON, matching the runtime {@code material.assetId()} lookup that
 * vanilla {@link net.minecraft.world.item.equipment.Equippable} uses.
 */
public class GTEquipmentAssetProvider extends EquipmentAssetProvider {

    public GTEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        Map<ResourceKey<EquipmentAsset>, ArmorProperty> uniqueAssets = new HashMap<>();
        GTCEuAPI.materialManager.stream()
                .map(mat -> mat.getProperty(PropertyKey.ARMOR))
                .filter(prop -> prop != null && prop.getAssetId() != null)
                .forEach(prop -> uniqueAssets.putIfAbsent(prop.getAssetId(), prop));

        uniqueAssets.forEach((assetId, prop) -> {
            Identifier texture = assetId.identifier();
            EquipmentClientInfo info = EquipmentClientInfo.builder()
                    .addHumanoidLayers(texture, prop.isDyeable())
                    .build();
            output.accept(assetId, info);
        });
    }
}
