package com.gregtechceu.gtceu.data.datamap;

import com.gregtechceu.gtceu.api.item.spoilage.SpoilAction;
import com.gregtechceu.gtceu.api.item.spoilage.ItemSpoilBehaviour;
import com.gregtechceu.gtceu.common.condition.IsDevEnvironmentCondition;
import com.gregtechceu.gtceu.common.data.GTDataMaps;
import com.gregtechceu.gtceu.common.item.spoil_actions.SpawnEntitySpoilAction;
import com.gregtechceu.gtceu.common.item.spoil_actions.TransformItemSpoilAction;
import com.gregtechceu.gtceu.data.recipe.misc.ComposterRecipes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import com.tterrag.registrate.providers.RegistrateDataMapProvider;

public class DataMapsHandler {

    public static void init(RegistrateDataMapProvider provider) {
        final var compostables = provider.builder(NeoForgeDataMaps.COMPOSTABLES);
        ComposterRecipes.addComposterRecipes((item, chance) -> compostables.add(item.builtInRegistryHolder(),
                new Compostable(chance), false));

        attachDevSpoilables(provider);
    }

    // Attach spoilage behaviour that is exclusive to dev environments
    public static void attachDevSpoilables(RegistrateDataMapProvider provider) {
        var datamap = provider.builder(GTDataMaps.SPOILABLE_DATA);

        registerDevSpoilable(datamap, Items.JIGSAW, 10, new TransformItemSpoilAction(Items.DIRT));
        registerDevSpoilable(datamap, Items.APPLE, 10, new TransformItemSpoilAction(Items.STRUCTURE_BLOCK));
        registerDevSpoilable(datamap, Items.STRUCTURE_BLOCK, 40, new TransformItemSpoilAction(Items.STRUCTURE_VOID));
        registerDevSpoilable(datamap, Items.STRUCTURE_VOID, 10, new TransformItemSpoilAction(Items.JIGSAW));
        registerDevSpoilable(datamap, Items.EGG, 20, new TransformItemSpoilAction(Items.DRAGON_EGG), new SpawnEntitySpoilAction(EntityType.PIG, 3));
    }

    private static void registerDevSpoilable(DataMapProvider.Builder<ItemSpoilBehaviour, Item> datamapBuilder, Item item, long ticks, SpoilAction... spoilActions) {
        datamapBuilder.add(item.builtInRegistryHolder(), new ItemSpoilBehaviour(ticks, spoilActions), false, new IsDevEnvironmentCondition());
    }
}
