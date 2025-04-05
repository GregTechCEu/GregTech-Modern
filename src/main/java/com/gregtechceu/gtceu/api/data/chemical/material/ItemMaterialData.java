package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;
import com.gregtechceu.gtceu.data.tags.TagsHandler;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.gregtechceu.gtceu.utils.memoization.MemoizedBlockSupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.datafixers.util.Pair;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemMaterialData {

    /** Used for custom material data for items that do not fall into the normal "prefix, material" pair */
    public static final Map<ItemLike, ItemMaterialInfo> ITEM_MATERIAL_INFO = new Object2ObjectOpenHashMap<>();
    /** Mapping of an item to a "prefix, material" pair */
    public static final List<Pair<Supplier<? extends ItemLike>, MaterialEntry>> ITEM_MATERIAL_ENTRY = new ArrayList<>();
    public static final Map<ItemLike, MaterialEntry> ITEM_MATERIAL_ENTRY_COLLECTED = new Object2ObjectOpenHashMap<>();
    /** Mapping of a tag to a "prefix, material" pair */
    public static final Map<TagKey<Item>, MaterialEntry> TAG_MATERIAL_ENTRY = new Object2ObjectLinkedOpenHashMap<>();
    /** Mapping of a fluid to a material */
    public static final Map<Fluid, Material> FLUID_MATERIAL = new Object2ObjectOpenHashMap<>();
    /** Mapping of all items that represent a "prefix, material" pair */
    public static final Map<MaterialEntry, List<Supplier<? extends ItemLike>>> MATERIAL_ENTRY_ITEM_MAP = new Object2ObjectOpenHashMap<>();
    public static final Map<MaterialEntry, List<Supplier<? extends Block>>> MATERIAL_ENTRY_BLOCK_MAP = new Object2ObjectOpenHashMap<>();
    /** Mapping of stone type blockState to "prefix, material" */
    public static final Map<Supplier<BlockState>, TagPrefix> ORES_INVERSE = new Object2ReferenceOpenHashMap<>();

    public static final Map<ItemStack, List<ItemStack>> UNRESOLVED_ITEM_MATERIAL_INFO = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    public static void registerMaterialInfo(ItemLike item, ItemMaterialInfo materialInfo) {
        if (item instanceof Block block) {
            ITEM_MATERIAL_INFO.put(block, materialInfo);
        } else if (item instanceof BlockItem blockItem) {
            ITEM_MATERIAL_INFO.put(blockItem.getBlock(), materialInfo);
        } else if (item instanceof ItemEntry<?> entry) {
            ITEM_MATERIAL_INFO.put(entry.asItem(), materialInfo);
        } else {
            ITEM_MATERIAL_INFO.put(item, materialInfo);
        }
    }

    public static ItemMaterialInfo getMaterialInfo(ItemLike item) {
        if (item instanceof Block block) {
            return ITEM_MATERIAL_INFO.get(block);
        } else if (item instanceof BlockItem blockItem) {
            var info = ITEM_MATERIAL_INFO.get(blockItem.getBlock());
            if (info != null) return info;
            return ITEM_MATERIAL_INFO.get(item);
        } else if (item instanceof ItemEntry<?> entry) {
            return ITEM_MATERIAL_INFO.get(entry.asItem());
        }
        return ITEM_MATERIAL_INFO.get(item);
    }

    public static void clearMaterialInfo(ItemLike item) {
        if (item instanceof Block block) {
            ITEM_MATERIAL_INFO.remove(block);
        } else if (item instanceof BlockItem blockItem) {
            var info = ITEM_MATERIAL_INFO.get(blockItem.getBlock());
            if (info != null) {
                ITEM_MATERIAL_INFO.remove(blockItem.getBlock());
            } else {
                ITEM_MATERIAL_INFO.remove(item);
            }
        } else if (item instanceof ItemEntry<?> entry) {
            ITEM_MATERIAL_INFO.remove(entry.asItem());
        } else {
            ITEM_MATERIAL_INFO.remove(item);
        }
    }

    @SafeVarargs
    public static void registerMaterialInfoItems(MaterialEntry materialEntry,
                                                 Supplier<? extends ItemLike>... items) {
        var entryList = MATERIAL_ENTRY_ITEM_MAP.computeIfAbsent(materialEntry, entry -> new ArrayList<>());
        for (Supplier<? extends ItemLike> item : items) {
            ITEM_MATERIAL_ENTRY.add(Pair.of(item, materialEntry));
            entryList.add(item);
            if (item instanceof Block block) {
                MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, entry -> new ArrayList<>())
                        .add(() -> block);
            } else if (item instanceof BlockEntry<?> blockEntry) {
                MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, entry -> new ArrayList<>())
                        .add(blockEntry);
            } else if (item instanceof RegistryObject<?> registryObject) {
                if (registryObject.getKey().isFor(Registries.BLOCK)) {
                    MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, entry -> new ArrayList<>())
                            .add((RegistryObject<Block>) registryObject);
                }
            } else if (item instanceof MemoizedBlockSupplier<? extends Block> supplier) {
                MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, entry -> new ArrayList<>())
                        .add(supplier);
            }
        }
        if (TagPrefix.ORES.containsKey(materialEntry.tagPrefix()) &&
                !ORES_INVERSE.containsValue(materialEntry.tagPrefix())) {
            ORES_INVERSE.put(TagPrefix.ORES.get(materialEntry.tagPrefix()).stoneType(), materialEntry.tagPrefix());
        }
        if (!materialEntry.isEmpty()) {
            for (TagKey<Item> tag : materialEntry.tagPrefix().getAllItemTags(materialEntry.material())) {
                TAG_MATERIAL_ENTRY.putIfAbsent(tag, materialEntry);
            }
        }
    }

    @SafeVarargs
    public static void registerMaterialInfoItems(TagPrefix tagPrefix, Material material,
                                                 Supplier<? extends ItemLike>... items) {
        registerMaterialInfoItems(new MaterialEntry(tagPrefix, material), items);
    }

    public static void registerMaterialInfoItems(TagPrefix tagPrefix, Material material, ItemLike... items) {
        registerMaterialInfoItems(new MaterialEntry(tagPrefix, material),
                Arrays.stream(items).map(item -> (Supplier<ItemLike>) () -> item).toArray(Supplier[]::new));
        for (ItemLike item : items) {
            ITEM_MATERIAL_ENTRY_COLLECTED.put(item, new MaterialEntry(tagPrefix, material));
        }
    }

    public static void reinitializeMaterialData() {
        // Clear old data
        MATERIAL_ENTRY_ITEM_MAP.clear();
        MATERIAL_ENTRY_BLOCK_MAP.clear();
        ITEM_MATERIAL_ENTRY.clear();
        FLUID_MATERIAL.clear();

        // Load new data
        TagsHandler.initExtraUnificationEntries();
        for (TagPrefix prefix : TagPrefix.values()) {
            prefix.getIgnored().forEach((mat, items) -> {
                if (items.length > 0) {
                    registerMaterialInfoItems(prefix, mat, items);
                }
            });
        }
        GTMaterialItems.toUnify.forEach(ItemMaterialData::registerMaterialInfoItems);
        WoodMachineRecipes.registerMaterialInfo();
    }

    @ApiStatus.Internal
    public static void resolveItemMaterialInfos(Consumer<FinishedRecipe> provider) {
        for (var entry : UNRESOLVED_ITEM_MATERIAL_INFO.entrySet()) {
            List<MaterialStack> stacks = new ArrayList<>();
            var stack = entry.getKey();
            var count = stack.getCount();
            for (var input : entry.getValue()) {
                var matStack = getMaterialInfo(input.getItem());
                if (matStack != null) {
                    matStack.getMaterials()
                            .forEach(ms -> stacks.add(new MaterialStack(ms.material(), ms.amount() / count)));
                }
            }
            if (stacks.isEmpty()) continue;
            var matInfo = ITEM_MATERIAL_INFO.get(stack.getItem());
            if (matInfo == null) {
                matInfo = new ItemMaterialInfo(stacks);
                ITEM_MATERIAL_INFO.put(stack.getItem(), matInfo);
            } else {
                matInfo.addMaterialStacks(stacks);
            }
            RecyclingRecipes.registerRecyclingRecipes(provider, entry.getKey().copyWithCount(1),
                    matInfo.getMaterials(), false, null);
        }
        UNRESOLVED_ITEM_MATERIAL_INFO.clear();
    }
}
