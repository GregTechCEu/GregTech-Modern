package com.gregtechceu.gtceu.api.data.chemical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.data.recipe.misc.StoneMachineRecipes;
import com.gregtechceu.gtceu.data.recipe.misc.WoodMachineRecipes;
import com.gregtechceu.gtceu.data.tags.TagsHandler;
import com.gregtechceu.gtceu.utils.SupplierMemoizer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.GTValues.M;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ChemicalHelper
 */
public class ChemicalHelper {

    /** Used for custom material data for items that do not fall into the normal "prefix, material" pair */
    public static final Map<ItemLike, ItemMaterialInfo> ITEM_MATERIAL_INFO = new ConcurrentHashMap<>();
    /** Mapping of an item to a "prefix, material" pair */
    public static final Set<Map.Entry<Supplier<? extends ItemLike>, UnificationEntry>> ITEM_UNIFICATION_ENTRY = ConcurrentHashMap
            .newKeySet();
    public static final Map<ItemLike, UnificationEntry> ITEM_UNIFICATION_ENTRY_COLLECTED = new ConcurrentHashMap<>();
    /** Mapping of a tag to a "prefix, material" pair */
    public static final Map<TagKey<Item>, UnificationEntry> TAG_UNIFICATION_ENTRY = new Object2ObjectLinkedOpenHashMap<>();
    /** Mapping of a fluid to a material */
    public static final Map<Fluid, Material> FLUID_MATERIAL = new ConcurrentHashMap<>();
    /** Mapping of all items that represent a "prefix, material" pair */
    public static final Map<UnificationEntry, ArrayList<Supplier<? extends ItemLike>>> UNIFICATION_ENTRY_ITEM = new ConcurrentHashMap<>();
    public static final Map<UnificationEntry, ArrayList<Supplier<? extends Block>>> UNIFICATION_ENTRY_BLOCK = new ConcurrentHashMap<>();
    /** Mapping of stone type blockState to "prefix, material" */
    public static final Map<Supplier<BlockState>, TagPrefix> ORES_INVERSE = new ConcurrentHashMap<>();

    public static void registerMaterialInfo(ItemLike item, ItemMaterialInfo materialInfo) {
        ITEM_MATERIAL_INFO.put(item, materialInfo);
    }

    public static ItemMaterialInfo getMaterialInfo(ItemLike item) {
        if (item instanceof Block block) {
            return ITEM_MATERIAL_INFO.get(block);
        } else if (item instanceof BlockItem blockItem) {
            return ITEM_MATERIAL_INFO.get(blockItem.getBlock());
        } else if (item instanceof ItemEntry<?> entry) {
            return ITEM_MATERIAL_INFO.get(entry.asItem());
        }
        return ITEM_MATERIAL_INFO.get(item);
    }

    @SafeVarargs
    public static void registerUnificationItems(UnificationEntry unificationEntry,
                                                Supplier<? extends ItemLike>... items) {
        UNIFICATION_ENTRY_ITEM.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                .addAll(Arrays.asList(items));
        for (Supplier<? extends ItemLike> item : items) {
            ITEM_UNIFICATION_ENTRY.add(Map.entry(item, unificationEntry));
            if (item instanceof Block block) {
                UNIFICATION_ENTRY_BLOCK.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                        .add(() -> block);
            } else if (item instanceof BlockEntry<?> blockEntry) {
                UNIFICATION_ENTRY_BLOCK.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                        .add(blockEntry);
            } else if (item instanceof RegistryObject<?> registryObject) {
                if (registryObject.getKey().isFor(Registries.BLOCK)) {
                    UNIFICATION_ENTRY_BLOCK.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                            .add((RegistryObject<Block>) registryObject);
                }
            } else if (item instanceof SupplierMemoizer.MemoizedBlockSupplier<? extends Block> supplier) {
                UNIFICATION_ENTRY_BLOCK.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                        .add(supplier);
            }
        }
        if (TagPrefix.ORES.containsKey(unificationEntry.tagPrefix) &&
                !ORES_INVERSE.containsValue(unificationEntry.tagPrefix)) {
            ORES_INVERSE.put(TagPrefix.ORES.get(unificationEntry.tagPrefix).stoneType(), unificationEntry.tagPrefix);
        }
        for (TagKey<Item> tag : unificationEntry.tagPrefix.getAllItemTags(unificationEntry.material)) {
            TAG_UNIFICATION_ENTRY.putIfAbsent(tag, unificationEntry);
        }
    }

    @SafeVarargs
    public static void registerUnificationItems(TagPrefix tagPrefix, @Nullable Material material,
                                                Supplier<? extends ItemLike>... items) {
        registerUnificationItems(new UnificationEntry(tagPrefix, material), items);
    }

    public static void registerUnificationItems(TagPrefix tagPrefix, @Nullable Material material, ItemLike... items) {
        registerUnificationItems(new UnificationEntry(tagPrefix, material),
                Arrays.stream(items).map(item -> (Supplier<ItemLike>) () -> item).toArray(Supplier[]::new));
        for (ItemLike item : items) {
            ITEM_UNIFICATION_ENTRY_COLLECTED.put(item, new UnificationEntry(tagPrefix, material));
        }
    }

    @Nullable
    public static MaterialStack getMaterial(Object object) {
        if (object instanceof MaterialStack materialStack) {
            return materialStack;
        } else if (object instanceof UnificationEntry entry) {
            return getMaterial(entry);
        } else if (object instanceof ItemStack itemStack) {
            return getMaterial(itemStack);
        } else if (object instanceof ItemLike item) {
            return getMaterial(item);
        }
        return null;
    }

    @Nullable
    public static MaterialStack getMaterial(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        return getMaterial(itemStack.getItem());
    }

    @Nullable
    public static MaterialStack getMaterial(UnificationEntry entry) {
        if (entry != null) {
            Material entryMaterial = entry.material;
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.tagPrefix.getMaterialAmount(entryMaterial));
            }
        }
        return null;
    }

    @Nullable
    public static MaterialStack getMaterial(ItemLike itemLike) {
        var entry = getUnificationEntry(itemLike);
        if (entry != null) {
            Material entryMaterial = entry.material;
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.tagPrefix.getMaterialAmount(entryMaterial));
            }
        }
        ItemMaterialInfo info = ITEM_MATERIAL_INFO.get(itemLike);
        if (info == null)
            return null;
        if (info.getMaterial() == null) {
            GTCEu.LOGGER.error("ItemMaterialInfo for {} is empty!", itemLike);
            return null;
        }
        return info.getMaterial().copy();
    }

    @Nullable
    public static Material getMaterial(Fluid fluid) {
        if (FLUID_MATERIAL.isEmpty()) {
            Set<TagKey<Fluid>> allFluidTags = BuiltInRegistries.FLUID.getTagNames().collect(Collectors.toSet());
            for (final Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    FluidStorageKey.allKeys().stream()
                            .map(key -> property.get(key))
                            .filter(Objects::nonNull)
                            .map(f -> Pair.of(f, TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(f).getPath())))
                            .filter(pair -> allFluidTags.contains(pair.getSecond()))
                            .forEach(pair -> {
                                allFluidTags.remove(pair.getSecond());
                                FLUID_MATERIAL.put(pair.getFirst(), material);
                            });
                }
            }
        }
        return FLUID_MATERIAL.get(fluid);
    }

    @Nullable
    public static TagPrefix getPrefix(ItemLike itemLike) {
        if (itemLike == null) return null;
        UnificationEntry entry = getUnificationEntry(itemLike);
        if (entry != null) return entry.tagPrefix;
        return null;
    }

    public static ItemStack getDust(Material material, long materialAmount) {
        if (!material.hasProperty(PropertyKey.DUST) || materialAmount <= 0)
            return ItemStack.EMPTY;
        if (materialAmount % M == 0 || materialAmount >= M * 16)
            return get(TagPrefix.dust, material, (int) (materialAmount / M));
        else if ((materialAmount * 4) % M == 0 || materialAmount >= M * 8)
            return get(TagPrefix.dustSmall, material, (int) ((materialAmount * 4) / M));
        else if ((materialAmount * 9) >= M)
            return get(TagPrefix.dustTiny, material, (int) ((materialAmount * 9) / M));
        return ItemStack.EMPTY;
    }

    public static ItemStack getDust(MaterialStack materialStack) {
        return getDust(materialStack.material(), materialStack.amount());
    }

    public static ItemStack getIngot(Material material, long materialAmount) {
        if (!material.hasProperty(PropertyKey.INGOT) || materialAmount <= 0)
            return ItemStack.EMPTY;
        if (materialAmount % (M * 9) == 0)
            return get(TagPrefix.block, material, (int) (materialAmount / (M * 9)));
        if (materialAmount % M == 0 || materialAmount >= M * 16)
            return get(TagPrefix.ingot, material, (int) (materialAmount / M));
        else if ((materialAmount * 9) >= M)
            return get(TagPrefix.nugget, material, (int) ((materialAmount * 9) / M));
        return ItemStack.EMPTY;
    }

    /**
     * Returns an Ingot of the material if it exists. Otherwise it returns a Dust.
     * Returns ItemStack.EMPTY if neither exist.
     */
    public static ItemStack getIngotOrDust(Material material, long materialAmount) {
        ItemStack ingotStack = getIngot(material, materialAmount);
        if (ingotStack != ItemStack.EMPTY) return ingotStack;
        return getDust(material, materialAmount);
    }

    public static ItemStack getIngotOrDust(MaterialStack materialStack) {
        return getIngotOrDust(materialStack.material(), materialStack.amount());
    }

    public static ItemStack getGem(MaterialStack materialStack) {
        if (materialStack.material().hasProperty(PropertyKey.GEM) &&
                !TagPrefix.gem.isIgnored(materialStack.material()) &&
                materialStack.amount() == TagPrefix.gem.getMaterialAmount(materialStack.material())) {
            return get(TagPrefix.gem, materialStack.material(), (int) (materialStack.amount() / M));
        }
        return getDust(materialStack);
    }

    @Nullable
    public static UnificationEntry getUnificationEntry(ItemLike itemLike) {
        // asItem is a bit slow, avoid calling it multiple times
        var itemKey = itemLike.asItem();
        var unifyingEntry = ITEM_UNIFICATION_ENTRY_COLLECTED.get(itemKey);

        if (unifyingEntry == null) {
            // Resolve all the lazy suppliers once, rather than on each request. This avoids O(n) lookup performance
            // for unification entries.
            ITEM_UNIFICATION_ENTRY.removeIf(entry -> {
                ITEM_UNIFICATION_ENTRY_COLLECTED.put(entry.getKey().get().asItem(), entry.getValue());
                return true;
            });

            // guess an entry based on the item's tags if none are pre-registered.
            unifyingEntry = ITEM_UNIFICATION_ENTRY_COLLECTED.computeIfAbsent(itemKey, item -> {
                for (TagKey<Item> itemTag : item.asItem().builtInRegistryHolder().tags().toList()) {
                    UnificationEntry unificationEntry = getUnificationEntry(itemTag);
                    // check that it's not the empty marker and that it's not a parent tag
                    if (unificationEntry != null &&
                            Arrays.stream(unificationEntry.tagPrefix.getItemParentTags()).noneMatch(itemTag::equals)) {
                        return unificationEntry;
                    }
                }
                return UnificationEntry.EmptyMapMarkerEntry;
            });
        }
        return unifyingEntry != UnificationEntry.EmptyMapMarkerEntry ? unifyingEntry : null;
    }

    public static UnificationEntry getUnificationEntry(TagKey<Item> tag) {
        if (TAG_UNIFICATION_ENTRY.isEmpty()) {
            // If the map is empty, resolve all possible tags to their values in an attempt to save time on later
            // lookups.
            Set<TagKey<Item>> allItemTags = BuiltInRegistries.ITEM.getTagNames().collect(Collectors.toSet());
            for (TagPrefix prefix : TagPrefix.values()) {
                for (Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
                    Arrays.stream(prefix.getItemTags(material))
                            .filter(allItemTags::contains)
                            .forEach(tagKey -> {
                                // remove the tag so that the next iteration is faster.
                                allItemTags.remove(tagKey);
                                TAG_UNIFICATION_ENTRY.put(tagKey, new UnificationEntry(prefix, material));
                            });
                }
            }
        }
        return TAG_UNIFICATION_ENTRY.get(tag);
    }

    public static List<ItemLike> getItems(UnificationEntry unificationEntry) {
        return UNIFICATION_ENTRY_ITEM.computeIfAbsent(unificationEntry, entry -> {
            var items = new ArrayList<Supplier<? extends ItemLike>>();
            for (TagKey<Item> tag : getTags(entry.tagPrefix, entry.material)) {
                for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    items.add(itemHolder::value);
                }
            }
            TagPrefix prefix = entry.tagPrefix;
            if (items.isEmpty() && prefix.hasItemTable() && prefix.doGenerateItem(entry.material)) {
                return new ArrayList<>(List.of(prefix.getItemFromTable(entry.material)));
            }
            return items;
        }).stream().map(Supplier::get).collect(Collectors.toList());
    }

    public static ItemStack get(UnificationEntry unificationEntry, int size) {
        var list = getItems(unificationEntry);
        if (list.isEmpty()) return ItemStack.EMPTY;
        var stack = list.get(0).asItem().getDefaultInstance();
        stack.setCount(size);
        return stack;
    }

    public static ItemStack get(TagPrefix orePrefix, Material material, int stackSize) {
        return get(new UnificationEntry(orePrefix, material), stackSize);
    }

    public static ItemStack get(TagPrefix orePrefix, Material material) {
        return get(orePrefix, material, 1);
    }

    public static List<Block> getBlocks(UnificationEntry unificationEntry) {
        return UNIFICATION_ENTRY_BLOCK.computeIfAbsent(unificationEntry, entry -> {
            var blocks = new ArrayList<Supplier<? extends Block>>();
            for (TagKey<Block> tag : Arrays.stream(getTags(unificationEntry.tagPrefix, unificationEntry.material))
                    .map(itemTagKey -> TagKey.create(Registries.BLOCK, itemTagKey.location())).toList()) {
                for (Holder<Block> itemHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                    blocks.add(itemHolder::value);
                }
            }
            return blocks;
        }).stream().map(Supplier::get).collect(Collectors.toList());
    }

    public static Block getBlock(UnificationEntry unificationEntry) {
        var list = getBlocks(unificationEntry);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static Block getBlock(TagPrefix orePrefix, Material material) {
        return getBlock(new UnificationEntry(orePrefix, material));
    }

    @Nullable
    public static TagKey<Block> getBlockTag(TagPrefix orePrefix, @NotNull Material material) {
        var tags = orePrefix.getBlockTags(material);
        if (tags.length > 0) {
            return tags[0];
        }
        return null;
    }

    @Nullable
    public static TagKey<Item> getTag(TagPrefix orePrefix, @NotNull Material material) {
        var tags = orePrefix.getItemTags(material);
        if (tags.length > 0) {
            return tags[0];
        }
        return null;
    }

    public static TagKey<Item>[] getTags(TagPrefix orePrefix, @NotNull Material material) {
        return orePrefix.getItemTags(material);
    }

    public static List<Map.Entry<ItemStack, ItemMaterialInfo>> getAllItemInfos() {
        return ITEM_MATERIAL_INFO.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(new ItemStack(entry.getKey().asItem()), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static Optional<TagPrefix> getOrePrefix(BlockState state) {
        return ORES_INVERSE.entrySet().stream().filter(entry -> entry.getKey().get().equals(state))
                .map(Map.Entry::getValue).findFirst();
    }

    public static void reinitializeUnification() {
        // Clear old data
        ChemicalHelper.UNIFICATION_ENTRY_ITEM.clear();
        ChemicalHelper.UNIFICATION_ENTRY_BLOCK.clear();
        ChemicalHelper.ITEM_UNIFICATION_ENTRY.clear();
        ChemicalHelper.FLUID_MATERIAL.clear();

        // Load new data
        TagsHandler.initExtraUnificationEntries();
        for (TagPrefix prefix : TagPrefix.values()) {
            prefix.getIgnored().forEach((mat, items) -> {
                if (items.length > 0) {
                    ChemicalHelper.registerUnificationItems(prefix, mat, items);
                }
            });
        }
        GTMaterialItems.toUnify.forEach(ChemicalHelper::registerUnificationItems);
        WoodMachineRecipes.registerUnificationInfo();
        StoneMachineRecipes.registerUnificationInfo();
    }
}
