package com.gregtechceu.gtceu.api.material;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.fluid.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.material.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.data.material.GTMaterials;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.GTValues.M;
import static com.gregtechceu.gtceu.api.material.material.ItemMaterialData.*;

public class ChemicalHelper {

    public static @Nullable ItemMaterialInfo getMaterialInfo(@Nullable Object object) {
        if (object instanceof ItemMaterialInfo materialInfo) {
            return materialInfo;
        } else if (object instanceof MaterialStack materialStack) {
            return new ItemMaterialInfo(materialStack);
        } else if (object instanceof ItemStack itemStack) {
            return ItemMaterialData.getMaterialInfo(itemStack.getItem());
        } else if (object instanceof ItemLike item) {
            return ItemMaterialData.getMaterialInfo(item);
        } else if (object instanceof MaterialEntry entry) {
            var items = getItems(entry);
            if (!items.isEmpty()) {
                return ItemMaterialData.getMaterialInfo(items.get(0));
            }
        } else if (object instanceof Ingredient ing) {
            if (!ing.isCustom()) {
                for (Ingredient.Value value : ing.getValues()) {
                    if (value instanceof Ingredient.TagValue) {
                        return null;
                    }
                }
            }
            for (var stack : ing.getItems()) {
                var ms = ItemMaterialData.getMaterialInfo(stack.getItem());
                if (ms != null) return ms;
            }
        } else if (object instanceof SizedIngredient ing) {
            return getMaterialInfo(ing.ingredient());
        }
        return null;
    }

    public static MaterialStack getMaterialStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) return MaterialStack.EMPTY;
        return getMaterialStack(itemStack.getItem());
    }

    public static MaterialStack getMaterialStack(@NotNull MaterialEntry entry) {
        Material entryMaterial = entry.material();
        if (!entryMaterial.isNull()) {
            return new MaterialStack(entryMaterial, entry.tagPrefix().getMaterialAmount(entryMaterial));
        }
        return MaterialStack.EMPTY;
    }

    public static MaterialStack getMaterialStack(ItemLike itemLike) {
        var entry = getMaterialEntry(itemLike);
        if (!entry.isEmpty()) {
            Material entryMaterial = entry.material();
            return new MaterialStack(entryMaterial, entry.tagPrefix().getMaterialAmount(entryMaterial));
        }
        ItemMaterialInfo info = ITEM_MATERIAL_INFO.get(itemLike.asItem());
        if (info == null) return MaterialStack.EMPTY;
        if (info.getMaterial().isEmpty()) {
            GTCEu.LOGGER.error("ItemMaterialInfo for {} is empty!", itemLike);
            return MaterialStack.EMPTY;
        }
        return info.getMaterial();
    }

    public static Material getMaterial(Fluid fluid) {
        if (FLUID_MATERIAL.isEmpty()) {
            Set<TagKey<Fluid>> allFluidTags = BuiltInRegistries.FLUID.getTagNames().collect(Collectors.toSet());
            for (final Material material : GTCEuAPI.materialManager) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    FluidStorageKey.allKeys().stream()
                            .map(property::get)
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
        return FLUID_MATERIAL.getOrDefault(fluid, GTMaterials.NULL);
    }

    public static TagPrefix getPrefix(ItemLike itemLike) {
        MaterialEntry entry = getMaterialEntry(itemLike);
        if (!entry.isEmpty()) return entry.tagPrefix();
        return TagPrefix.NULL_PREFIX;
    }

    public static ItemStack getDust(Material material, long materialAmount) {
        if (!material.hasProperty(PropertyKey.DUST) || materialAmount <= 0) {
            return ItemStack.EMPTY;
        }
        if (materialAmount % M == 0 || materialAmount >= M * 16) {
            return get(TagPrefix.dust, material, (int) (materialAmount / M));
        } else if ((materialAmount * 4) % M == 0 || materialAmount >= M * 8) {
            return get(TagPrefix.dustSmall, material, (int) ((materialAmount * 4) / M));
        } else if ((materialAmount * 9) >= M) {
            return get(TagPrefix.dustTiny, material, (int) ((materialAmount * 9) / M));
        }
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

    public static MaterialEntry getMaterialEntry(ItemLike itemLike) {
        // asItem is a bit slow, avoid calling it multiple times
        var itemKey = itemLike.asItem();
        var materialEntry = ITEM_MATERIAL_ENTRY_COLLECTED.get(itemKey);

        if (materialEntry == null) {
            // Resolve all the lazy suppliers once, rather than on each request. This avoids O(n) lookup performance
            // for unification entries.
            for (var entry : ITEM_MATERIAL_ENTRY) {
                ITEM_MATERIAL_ENTRY_COLLECTED.put(entry.getFirst().get().asItem(), entry.getSecond());
            }
            ITEM_MATERIAL_ENTRY.clear();

            // guess an entry based on the item's tags if none are pre-registered.
            materialEntry = ITEM_MATERIAL_ENTRY_COLLECTED.computeIfAbsent(itemKey, item -> {
                for (TagKey<Item> itemTag : item.asItem().builtInRegistryHolder().tags().toList()) {
                    MaterialEntry materialEntry1 = getMaterialEntry(itemTag);
                    // check that it's not the empty marker and that it's not a parent tag
                    if (!materialEntry1.isEmpty() &&
                            materialEntry1.tagPrefix().getItemParentTags().stream().noneMatch(itemTag::equals)) {
                        return materialEntry1;
                    }
                }
                return MaterialEntry.NULL_ENTRY;
            });
        }
        return materialEntry;
    }

    public static MaterialEntry getMaterialEntry(TagKey<Item> tag) {
        if (TAG_MATERIAL_ENTRY.isEmpty()) {
            // If the map is empty, resolve all possible tags to their values in an attempt to save time on later
            // lookups.
            Set<TagKey<Item>> allItemTags = BuiltInRegistries.ITEM.getTagNames().collect(Collectors.toSet());
            for (TagPrefix prefix : GTRegistries.TAG_PREFIXES) {
                for (Material material : GTCEuAPI.materialManager) {
                    prefix.getItemTags(material).stream()
                            .filter(allItemTags::contains)
                            .forEach(tagKey -> {
                                // remove the tag so that the next iteration is faster.
                                allItemTags.remove(tagKey);
                                TAG_MATERIAL_ENTRY.put(tagKey, new MaterialEntry(prefix, material));
                            });
                }
            }
        }
        return TAG_MATERIAL_ENTRY.getOrDefault(tag, MaterialEntry.NULL_ENTRY);
    }

    public static List<ItemLike> getItems(MaterialEntry materialEntry) {
        if (materialEntry.material().isNull()) return new ArrayList<>();
        return MATERIAL_ENTRY_ITEM_MAP.computeIfAbsent(materialEntry, entry -> {
            TagPrefix prefix = entry.tagPrefix();
            var items = new ArrayList<Supplier<? extends Item>>();
            for (TagKey<Item> tag : prefix.getItemTags(entry.material())) {
                for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    items.add(itemHolder::value);
                }
            }
            if (items.isEmpty() && prefix.hasItemTable() && prefix.doGenerateItem(entry.material())) {
                return List.of(() -> prefix.getItemFromTable(entry.material()).get().asItem());
            }
            return items;
        }).stream().map(Supplier::get).collect(Collectors.toList());
    }

    public static ItemStack get(MaterialEntry materialEntry, int size) {
        var list = getItems(materialEntry);
        if (list.isEmpty()) return ItemStack.EMPTY;
        var stack = list.getFirst().asItem().getDefaultInstance();
        stack.setCount(size);
        return stack;
    }

    public static ItemStack get(TagPrefix orePrefix, Material material, int stackSize) {
        return get(new MaterialEntry(orePrefix, material), stackSize);
    }

    public static ItemStack get(TagPrefix orePrefix, Material material) {
        return get(orePrefix, material, 1);
    }

    public static List<Block> getBlocks(MaterialEntry materialEntry) {
        if (materialEntry.isEmpty()) return Collections.emptyList();
        return MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, entry -> {
            TagPrefix prefix = entry.tagPrefix();
            var blocks = new ArrayList<Supplier<? extends Block>>();
            for (TagKey<Block> tag : prefix.getBlockTags(entry.material())) {
                for (Holder<Block> itemHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                    blocks.add(itemHolder::value);
                }
            }
            if (blocks.isEmpty() && prefix.hasItemTable() && prefix.doGenerateBlock(entry.material())) {
                var blockSupplier = ItemMaterialData.convertToBlock(prefix.getItemFromTable(entry.material()));
                if (blockSupplier != null) {
                    return Collections.singletonList(blockSupplier);
                }
            }
            return blocks;
        }).stream().map(Supplier::get).collect(Collectors.toList());
    }

    @Nullable
    public static Block getBlock(MaterialEntry materialEntry) {
        var list = getBlocks(materialEntry);
        if (list.isEmpty()) return null;
        return list.getFirst();
    }

    @Nullable
    public static Block getBlock(TagPrefix orePrefix, Material material) {
        return getBlock(new MaterialEntry(orePrefix, material));
    }

    @Nullable
    public static TagKey<Block> getBlockTag(TagPrefix orePrefix, @NotNull Material material) {
        var tags = orePrefix.getBlockTags(material);
        if (!tags.isEmpty()) {
            return tags.getFirst();
        }
        return null;
    }

    @Nullable
    public static TagKey<Item> getTag(TagPrefix orePrefix, @NotNull Material material) {
        var tags = orePrefix.getItemTags(material);
        if (!tags.isEmpty()) {
            return tags.getFirst();
        }
        return null;
    }

    public static List<TagKey<Item>> getTags(TagPrefix orePrefix, @NotNull Material material) {
        return orePrefix.getItemTags(material);
    }

    public static List<Pair<ItemStack, ItemMaterialInfo>> getAllItemInfos() {
        List<Pair<ItemStack, ItemMaterialInfo>> f = new ArrayList<>();
        for (var entry : ITEM_MATERIAL_INFO.entrySet()) {
            f.add(Pair.of(new ItemStack(entry.getKey()), entry.getValue()));
        }
        return f;
    }

    public static Optional<TagPrefix> getOrePrefix(BlockState state) {
        return ORES_INVERSE.entrySet().stream()
                .filter(entry -> entry.getKey().get().equals(state))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
