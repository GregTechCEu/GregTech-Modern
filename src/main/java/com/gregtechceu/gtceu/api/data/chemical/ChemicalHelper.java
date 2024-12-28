package com.gregtechceu.gtceu.api.data.chemical;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.GTValues.M;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ChemicalHelper
 */
public class ChemicalHelper {

    @Nullable
    public static MaterialStack getMaterialStack(Object object) {
        if (object instanceof MaterialStack materialStack) {
            return materialStack;
        } else if (object instanceof MaterialEntry entry) {
            return getMaterialStack(entry);
        } else if (object instanceof ItemStack itemStack) {
            return getMaterialStack(itemStack);
        } else if (object instanceof ItemLike item) {
            return getMaterialStack(item);
        } else if (object instanceof SizedIngredient sized) {
            return getMaterialStack(sized.getItems()[0]);
        } else if (object instanceof Ingredient ing) {
            for(var stack : ing.getItems()) {
                var ms = getMaterialStack(stack);
                if(ms != null) return ms;
            }
        }
        return null;
    }

    @Nullable
    public static MaterialStack getMaterialStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) return null;
        return getMaterialStack(itemStack.getItem());
    }

    @Nullable
    public static MaterialStack getMaterialStack(MaterialEntry entry) {
        if (entry != null) {
            Material entryMaterial = entry.material();
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.tagPrefix().getMaterialAmount(entryMaterial));
            }
        }
        return null;
    }

    @Nullable
    public static MaterialStack getMaterialStack(ItemLike itemLike) {
        var entry = getMaterialEntry(itemLike);
        if (entry != null) {
            Material entryMaterial = entry.material();
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.tagPrefix().getMaterialAmount(entryMaterial));
            }
        }
        ItemMaterialInfo info = ItemMaterialData.ITEM_MATERIAL_INFO.get(itemLike);
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
        if (ItemMaterialData.FLUID_MATERIAL.isEmpty()) {
            Set<TagKey<Fluid>> allFluidTags = BuiltInRegistries.FLUID.getTagNames().collect(Collectors.toSet());
            for (final Material material : GTCEuAPI.materialManager.getRegisteredMaterials()) {
                if (material.hasProperty(PropertyKey.FLUID)) {
                    FluidProperty property = material.getProperty(PropertyKey.FLUID);
                    FluidStorageKey.allKeys().stream()
                            .map(property::get)
                            .filter(Objects::nonNull)
                            .map(f -> Pair.of(f, TagUtil.createFluidTag(BuiltInRegistries.FLUID.getKey(f).getPath())))
                            .filter(pair -> allFluidTags.contains(pair.getSecond()))
                            .forEach(pair -> {
                                allFluidTags.remove(pair.getSecond());
                                ItemMaterialData.FLUID_MATERIAL.put(pair.getFirst(), material);
                            });
                }
            }
        }
        return ItemMaterialData.FLUID_MATERIAL.get(fluid);
    }

    @Nullable
    public static TagPrefix getPrefix(ItemLike itemLike) {
        if (itemLike == null) return null;
        MaterialEntry entry = getMaterialEntry(itemLike);
        if (entry != null) return entry.tagPrefix();
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
    public static MaterialEntry getMaterialEntry(ItemLike itemLike) {
        // asItem is a bit slow, avoid calling it multiple times
        var itemKey = itemLike.asItem();
        var unifyingEntry = ItemMaterialData.ITEM_MATERIAL_ENTRY_COLLECTED.get(itemKey);

        if (unifyingEntry == null) {
            // Resolve all the lazy suppliers once, rather than on each request. This avoids O(n) lookup performance
            // for unification entries.
            ItemMaterialData.ITEM_MATERIAL_ENTRY.removeIf(entry -> {
                ItemMaterialData.ITEM_MATERIAL_ENTRY_COLLECTED.put(entry.getKey().get().asItem(), entry.getValue());
                return true;
            });

            // guess an entry based on the item's tags if none are pre-registered.
            unifyingEntry = ItemMaterialData.ITEM_MATERIAL_ENTRY_COLLECTED.computeIfAbsent(itemKey, item -> {
                for (TagKey<Item> itemTag : item.asItem().builtInRegistryHolder().tags().toList()) {
                    MaterialEntry materialEntry = getMaterialEntry(itemTag);
                    // check that it's not the empty marker and that it's not a parent tag
                    if (materialEntry != null &&
                            Arrays.stream(materialEntry.tagPrefix().getItemParentTags()).noneMatch(itemTag::equals)) {
                        return materialEntry;
                    }
                }
                return null;
            });
        }
        return unifyingEntry;
    }

    public static MaterialEntry getMaterialEntry(TagKey<Item> tag) {
        if (ItemMaterialData.TAG_MATERIAL_ENTRY.isEmpty()) {
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
                                ItemMaterialData.TAG_MATERIAL_ENTRY.put(tagKey, new MaterialEntry(prefix, material));
                            });
                }
            }
        }
        return ItemMaterialData.TAG_MATERIAL_ENTRY.get(tag);
    }

    public static List<ItemLike> getItems(MaterialEntry materialEntry) {
        return ItemMaterialData.MATERIAL_ENTRY_ITEM_MAP.computeIfAbsent(materialEntry, entry -> {
            var items = new ArrayList<Supplier<? extends ItemLike>>();
            for (TagKey<Item> tag : getTags(entry.tagPrefix(), entry.material())) {
                for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    items.add(itemHolder::value);
                }
            }
            TagPrefix prefix = entry.tagPrefix();
            if (items.isEmpty() && prefix.hasItemTable() && prefix.doGenerateItem(entry.material())) {
                return new ArrayList<>(List.of(prefix.getItemFromTable(entry.material())));
            }
            return items;
        }).stream().map(Supplier::get).collect(Collectors.toList());
    }

    public static ItemStack get(MaterialEntry materialEntry, int size) {
        var list = getItems(materialEntry);
        if (list.isEmpty()) return ItemStack.EMPTY;
        var stack = list.get(0).asItem().getDefaultInstance();
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
        return ItemMaterialData.MATERIAL_ENTRY_BLOCK_MAP.computeIfAbsent(materialEntry, entry -> {
            var blocks = new ArrayList<Supplier<? extends Block>>();
            for (TagKey<Block> tag : Arrays.stream(getTags(materialEntry.tagPrefix(), materialEntry.material()))
                    .map(itemTagKey -> TagKey.create(Registries.BLOCK, itemTagKey.location())).toList()) {
                for (Holder<Block> itemHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                    blocks.add(itemHolder::value);
                }
            }
            return blocks;
        }).stream().map(Supplier::get).collect(Collectors.toList());
    }

    public static Block getBlock(MaterialEntry materialEntry) {
        var list = getBlocks(materialEntry);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    public static Block getBlock(TagPrefix orePrefix, Material material) {
        return getBlock(new MaterialEntry(orePrefix, material));
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
        return ItemMaterialData.ITEM_MATERIAL_INFO.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(new ItemStack(entry.getKey().asItem()), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static Optional<TagPrefix> getOrePrefix(BlockState state) {
        return ItemMaterialData.ORES_INVERSE.entrySet().stream().filter(entry -> entry.getKey().get().equals(state))
                .map(Map.Entry::getValue).findFirst();
    }
}
