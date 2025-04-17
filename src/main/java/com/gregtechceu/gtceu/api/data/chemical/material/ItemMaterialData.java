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
import org.jetbrains.annotations.NotNull;

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

    /**
     * Register Material Entry for an item
     *
     * @param supplier      a supplier to the item
     * @param materialEntry the entry to register
     */
    public static void registerMaterialEntry(@NotNull Supplier<? extends ItemLike> supplier,
                                             @NotNull MaterialEntry materialEntry) {
        registerItemEntry(supplier, materialEntry);
        ITEM_MATERIAL_ENTRY.add(Pair.of(supplier, materialEntry));
        if (supplier instanceof RegistryObject<? extends ItemLike> registryObject) {
            registerRegistryObjectEntry(registryObject, materialEntry);
        } else if (supplier instanceof BlockEntry<?> entry) {
            registerBlockEntry(entry, materialEntry);
        } else if (supplier instanceof MemoizedBlockSupplier<?> blockSupplier) {
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
                .add(supplier);
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

    @SuppressWarnings("unchecked")
    private static void registerRegistryObjectEntry(@NotNull RegistryObject<? extends ItemLike> registryObject,
                                                    @NotNull MaterialEntry materialEntry) {
        var key = registryObject.getKey();
        if (key != null && key.isFor(Registries.BLOCK)) {
            registerBlockEntry((Supplier<? extends Block>) registryObject, materialEntry);
        }
    }

    private static void registerBlockEntry(@NotNull Supplier<? extends Block> supplier,
                                           @NotNull MaterialEntry materialEntry) {
        MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, k -> new ArrayList<>())
                .add(supplier);
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
