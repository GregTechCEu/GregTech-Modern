package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTClientFluidTypeExtensions;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.core.mixins.BlockBehaviourAccessor;
import com.gregtechceu.gtceu.integration.kjs.GTCEuServerEvents;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockFluidVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTBedrockOreVeinEventJS;
import com.gregtechceu.gtceu.integration.kjs.events.GTOreVeinEventJS;

import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

import com.tterrag.registrate.util.entry.BlockEntry;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings("deprecation")
@ApiStatus.Internal
public class MixinHelpers {

    public static void generateGTDynamicLoot(TriConsumer<ResourceLocation, LootTable, RegistryAccess.Frozen> lootTables,
                                             final RegistryAccess.Frozen access) {
        final VanillaBlockLoot blockLoot = new VanillaBlockLoot(access);

        Holder<Enchantment> fortune = access.registryOrThrow(Registries.ENCHANTMENT)
                .getHolderOrThrow(Enchantments.FORTUNE);
        GTMaterialBlocks.MATERIAL_BLOCKS.rowMap().forEach((prefix, map) -> {
            if (TagPrefix.ORES.containsKey(prefix)) {
                final TagPrefix.OreType type = TagPrefix.ORES.get(prefix);
                map.forEach((material, blockEntry) -> {
                    ResourceLocation lootTableId = blockEntry.getId().withPrefix("blocks/");
                    Block block = blockEntry.get();

                    ItemStack dropItem = ChemicalHelper.get(TagPrefix.rawOre, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.gem, material);
                    if (dropItem.isEmpty()) dropItem = ChemicalHelper.get(TagPrefix.dust, material);
                    int oreMultiplier = type.isDoubleDrops() ? 2 : 1;

                    LootTable.Builder builder = blockLoot.createSilkTouchDispatchTable(block,
                            blockLoot.applyExplosionDecay(block,
                                    LootItem.lootTableItem(dropItem.getItem())
                                            .apply(SetItemCountFunction
                                                    .setCount(ConstantValue.exactly(oreMultiplier)))));
                    // disable fortune for balance reasons. (for now, until we can think of a better solution.)
                    // .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));

                    LootPool.Builder pool = LootPool.lootPool();
                    boolean isEmpty = true;
                    for (MaterialStack secondaryMaterial : prefix.secondaryMaterials()) {
                        if (secondaryMaterial.material().hasProperty(PropertyKey.DUST)) {
                            ItemStack dustStack = ChemicalHelper.getGem(secondaryMaterial);
                            pool.add(LootItem.lootTableItem(dustStack.getItem())
                                    .when(blockLoot.doesNotHaveSilkTouch())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                                    // .apply(ApplyBonusCount.addUniformBonusCount(fortune))
                                    .apply(LimitCount.limitCount(IntRange.range(0, 2)))
                                    .apply(ApplyExplosionDecay.explosionDecay()));
                            isEmpty = false;
                        }
                    }
                    if (!isEmpty) {
                        builder.withPool(pool);
                    }
                    lootTables.accept(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build(), access);
                    ((BlockBehaviourAccessor) blockEntry.get())
                            .setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
                });
            } else {
                MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
            }
        });
        GTMaterialBlocks.CABLE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTMaterialBlocks.FLUID_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTMaterialBlocks.ITEM_PIPE_BLOCKS.rowMap().forEach((prefix, map) -> {
            MixinHelpers.addMaterialBlockLootTables(lootTables, prefix, map, blockLoot, access);
        });
        GTMaterialBlocks.SURFACE_ROCK_BLOCKS.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(blockEntry.getId().getNamespace(),
                    "blocks/" + blockEntry.getId().getPath());
            LootTable.Builder builder = blockLoot
                    .createSingleItemTable(ChemicalHelper.get(TagPrefix.dustTiny, material).getItem(),
                            UniformGenerator.between(3, 5))
                    .apply(ApplyBonusCount.addUniformBonusCount(fortune));
            lootTables.accept(lootTableId, builder.setParamSet(LootContextParamSets.BLOCK).build(), access);
            ((BlockBehaviourAccessor) blockEntry.get())
                    .setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
        });
        GTRegistries.MACHINES.forEach(machine -> {
            Block block = machine.getBlock();
            ResourceLocation id = machine.getId();
            ResourceLocation lootTableId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                    "blocks/" + id.getPath());
            ((BlockBehaviourAccessor) block).setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
            lootTables.accept(lootTableId,
                    blockLoot.createSingleItemTable(block).setParamSet(LootContextParamSets.BLOCK).build(), access);
        });
    }

    public static void addMaterialBlockLootTables(TriConsumer<ResourceLocation, LootTable, RegistryAccess.Frozen> lootTables,
                                                  TagPrefix prefix,
                                                  Map<Material, ? extends BlockEntry<? extends Block>> map,
                                                  VanillaBlockLoot blockLoot, RegistryAccess.Frozen access) {
        map.forEach((material, blockEntry) -> {
            ResourceLocation lootTableId = blockEntry.getId().withPrefix("blocks/");
            ((BlockBehaviourAccessor) blockEntry.get())
                    .setDrops(ResourceKey.create(Registries.LOOT_TABLE, lootTableId));
            lootTables.accept(lootTableId,
                    blockLoot.createSingleItemTable(blockEntry.get()).setParamSet(LootContextParamSets.BLOCK).build(),
                    access);
        });
    }

    public static void postKJSVeinEvents(RegistryAccess.Frozen registries) {
        if (!GTCEu.Mods.isKubeJSLoaded()) {
            return;
        }
        KJSCallWrapper.updateRegistryAccessContainer(registries);

        KJSCallWrapper.postEventWithRegistry(KJSCallWrapper::postOreVeinEvent,
                registries.registryOrThrow(GTRegistries.Keys.ORE_VEIN));

        KJSCallWrapper.postEventWithRegistry(KJSCallWrapper::postBedrockFluidEvent,
                registries.registryOrThrow(GTRegistries.Keys.BEDROCK_FLUID));

        KJSCallWrapper.postEventWithRegistry(KJSCallWrapper::postBedrockOreEvent,
                registries.registryOrThrow(GTRegistries.Keys.BEDROCK_ORE));
    }

    public static void addFluidTexture(Material material, FluidStorage.FluidEntry value) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(value.getFluid().get());
        if (extensions instanceof GTClientFluidTypeExtensions gtExtensions && value.getBuilder() != null) {
            value.getBuilder().determineTextures(material, value.getKey());

            gtExtensions.setFlowingTexture(value.getBuilder().flowing());
            gtExtensions.setStillTexture(value.getBuilder().still());
        }
    }

    private static final class KJSCallWrapper {

        private static <T> void postEventWithRegistry(Consumer<WritableRegistry<T>> eventProvider,
                                                      Registry<T> registry) {
            if (registry instanceof MappedRegistry<T> writable) {
                // unfreeze the registry, register to it, refreeze it.
                writable.unfreeze();
                eventProvider.accept(writable);
                writable.freeze();
            }
        }

        private static void postOreVeinEvent(WritableRegistry<GTOreDefinition> registry) {
            GTCEuServerEvents.ORE_VEIN_MODIFICATION.post(new GTOreVeinEventJS(registry));
        }

        private static void postBedrockFluidEvent(WritableRegistry<BedrockFluidDefinition> registry) {
            GTCEuServerEvents.FLUID_VEIN_MODIFICATION.post(new GTBedrockFluidVeinEventJS(registry));
        }

        private static void postBedrockOreEvent(WritableRegistry<BedrockOreDefinition> registry) {
            GTCEuServerEvents.BEDROCK_ORE_VEIN_MODIFICATION.post(new GTBedrockOreVeinEventJS(registry));
        }

        private static void updateRegistryAccessContainer(RegistryAccess.Frozen registriesWithEverything) {
            if (RegistryAccessContainer.current.access().registries().count() <
                    registriesWithEverything.registries().count()) {
                RegistryAccessContainer.current = new RegistryAccessContainer(registriesWithEverything);
            }
        }
    }

    public static final class ClientCallWrapper {

        public static Level getClientLevel() {
            return Minecraft.getInstance().level;
        }
    }
}
