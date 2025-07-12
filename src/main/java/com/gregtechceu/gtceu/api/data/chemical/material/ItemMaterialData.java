package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.misc.RecyclingRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.StoneMachineRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;
import com.gregtechceu.gtceu.data.tags.TagsHandler;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.gregtechceu.gtceu.utils.memoization.MemoizedBlockSupplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.RegistryObject;

import com.mojang.datafixers.util.Pair;
import com.tterrag.registrate.util.entry.RegistryEntry;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemMaterialData {

    /** Used for custom material data for items that do not fall into the normal "prefix, material" pair */
    public static final Map<Item, ItemMaterialInfo> ITEM_MATERIAL_INFO = new Object2ObjectOpenHashMap<>();
    /** Mapping of an item to a "prefix, material" pair */
    public static final List<Pair<Supplier<? extends Item>, MaterialEntry>> ITEM_MATERIAL_ENTRY = new ArrayList<>();
    public static final Map<Item, MaterialEntry> ITEM_MATERIAL_ENTRY_COLLECTED = new Object2ObjectOpenHashMap<>();
    /** Mapping of a tag to a "prefix, material" pair */
    public static final Map<TagKey<Item>, MaterialEntry> TAG_MATERIAL_ENTRY = new Object2ObjectLinkedOpenHashMap<>();
    /** Mapping of a fluid to a material */
    public static final Map<Fluid, Material> FLUID_MATERIAL = new Object2ObjectOpenHashMap<>();
    /** Mapping of all items that represent a "prefix, material" pair */
    public static final Map<MaterialEntry, List<Supplier<? extends Item>>> MATERIAL_ENTRY_ITEM_MAP = new Object2ObjectOpenHashMap<>();
    public static final Map<MaterialEntry, List<Supplier<? extends Block>>> MATERIAL_ENTRY_BLOCK_MAP = new Object2ObjectOpenHashMap<>();
    /** Mapping of stone type blockState to "prefix, material" */
    public static final Map<Supplier<BlockState>, TagPrefix> ORES_INVERSE = new Object2ReferenceOpenHashMap<>();

    public static final Map<ItemStack, List<ItemStack>> UNRESOLVED_ITEM_MATERIAL_INFO = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    public static void registerMaterialInfo(ItemLike item, ItemMaterialInfo materialInfo) {
        ITEM_MATERIAL_INFO.put(item.asItem(), materialInfo);
    }

    public static ItemMaterialInfo getMaterialInfo(ItemLike item) {
        return ITEM_MATERIAL_INFO.get(item.asItem());
    }

    public static void clearMaterialInfo(ItemLike item) {
        ITEM_MATERIAL_INFO.remove(item.asItem());
    }

    /**
     * Register Material Entry for an item
     *
     * @param supplier      a supplier to the item
     * @param materialEntry the entry to register
     */
    public static void registerMaterialEntry(@NotNull Supplier<? extends ItemLike> supplier,
                                             @NotNull MaterialEntry materialEntry) {
        registerItemEntry(supplier, materialEntry);
        ITEM_MATERIAL_ENTRY.add(Pair.of(() -> supplier.get().asItem(), materialEntry));
        var blockSupplier = convertToBlock(supplier);
        if (blockSupplier != null) {
            registerBlockEntry(blockSupplier, materialEntry);
        }
    }

    /**
     * @see #registerMaterialEntry(Supplier, MaterialEntry)
     */
    public static void registerMaterialEntries(@NotNull Collection<Supplier<? extends ItemLike>> items,
                                               @NotNull TagPrefix tagPrefix, @NotNull Material material) {
        if (!items.isEmpty()) {
            MaterialEntry entry = new MaterialEntry(tagPrefix, material);
            for (var supplier : items) {
                registerMaterialEntry(supplier, entry);
            }
        }
    }

    /**
     * @see #registerMaterialEntry(Supplier, MaterialEntry)
     */
    public static void registerMaterialEntry(@NotNull Supplier<? extends ItemLike> item,
                                             @NotNull TagPrefix tagPrefix, @NotNull Material material) {
        registerMaterialEntry(item, new MaterialEntry(tagPrefix, material));
    }

    /**
     * @see #registerMaterialEntry(Supplier, MaterialEntry)
     */
    public static void registerMaterialEntry(@NotNull ItemLike item,
                                             @NotNull TagPrefix tagPrefix, @NotNull Material material) {
        registerMaterialEntry(() -> item, new MaterialEntry(tagPrefix, material));
    }

    private static void registerItemEntry(@NotNull Supplier<? extends ItemLike> supplier,
                                          @NotNull MaterialEntry materialEntry) {
        MATERIAL_ENTRY_ITEM_MAP.computeIfAbsent(materialEntry, k -> new ArrayList<>())
                .add(() -> supplier.get().asItem());
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

    private static void registerBlockEntry(@NotNull Supplier<? extends Block> supplier,
                                           @NotNull MaterialEntry materialEntry) {
        MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, k -> new ArrayList<>())
                .add(supplier);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable Supplier<? extends Block> convertToBlock(@NotNull Supplier<? extends ItemLike> supplier) {
        if (supplier instanceof RegistryObject<? extends ItemLike> registryObject) {
            var key = registryObject.getKey();
            if (key != null && key.isFor(Registries.BLOCK)) {
                return (Supplier<? extends Block>) registryObject;
            }
        } else if (supplier instanceof RegistryEntry<? extends ItemLike> entry) {
            var key = entry.getKey();
            if (key.isFor(Registries.BLOCK)) {
                return (Supplier<? extends Block>) entry;
            }
        } else if (supplier instanceof MemoizedBlockSupplier<?> blockSupplier) {
            return blockSupplier;
        }
        return null;
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
            prefix.getIgnored().forEach((mat, items) -> registerMaterialEntries(Arrays.asList(items), prefix, mat));
        }
        GTMaterialItems.toUnify
                .forEach((materialEntry, supplier) -> registerMaterialEntry(supplier, materialEntry));
        WoodMachineRecipes.registerMaterialInfo();
        StoneMachineRecipes.registerMaterialInfo();
    }

    @ApiStatus.Internal
    public static void resolveItemMaterialInfos(Consumer<FinishedRecipe> provider) {
        for (var iter = UNRESOLVED_ITEM_MATERIAL_INFO.entrySet().iterator(); iter.hasNext();) {
            var entry = iter.next();
            var stack = entry.getKey();
            var existingMaterialInfo = recurseFindMaterialInfo(ITEM_MATERIAL_INFO.get(stack.getItem()), stack);
            if (existingMaterialInfo != null) {
                RecyclingRecipes.registerRecyclingRecipes(provider, stack.copyWithCount(1),
                        existingMaterialInfo.getMaterials(), false, null);
            }
            iter.remove();
        }
    }

    private static ItemMaterialInfo recurseFindMaterialInfo(ItemMaterialInfo info, ItemStack stack) {
        // grab material info from each input
        for (var input : UNRESOLVED_ITEM_MATERIAL_INFO.get(stack)) {
            // recurse if its nested inputs, not yet resolved
            if (UNRESOLVED_ITEM_MATERIAL_INFO.containsKey(input)) {
                info = recurseFindMaterialInfo(info, input);
            } else {
                // add the info from an item that is resolved (or not in the map to begin with)
                var singularMatInfo = getMaterialInfo(input.getItem());
                int inputCount = input.getCount();
                int outputCount = stack.getCount();
                if (singularMatInfo != null) { // if that material info exists
                    List<MaterialStack> stackList = new ArrayList<>();
                    for (var matStack : singularMatInfo.getMaterials()) {
                        stackList.add(matStack.multiply(inputCount).divide(outputCount));
                    }
                    if (info == null) { // if the info isn't set initialize it
                        info = new ItemMaterialInfo(stackList);
                        ITEM_MATERIAL_INFO.put(stack.getItem(), info);
                    } else { // otherwise, add to it
                        info.addMaterialStacks(stackList);
                    }
                }
            }
        }
        return info;
    }
}
