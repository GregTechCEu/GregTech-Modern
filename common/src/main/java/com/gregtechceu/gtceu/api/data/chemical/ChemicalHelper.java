package com.gregtechceu.gtceu.api.data.chemical;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.ItemMaterialInfo;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.GTValues.M;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ChemicalHelper
 */
public class ChemicalHelper {

    /** Used for custom material data for items that do not fall into the normal "prefix, material" pair */
    public static final Map<ItemLike, ItemMaterialInfo> ITEM_MATERIAL_INFO = new Object2ObjectLinkedOpenHashMap<>();
    /** Mapping of an item to a "prefix, material" pair */
    public static final Map<ItemLike, UnificationEntry> ITEM_UNIFICATION_ENTRY = new HashMap<>();
    /** Mapping of all items that represent a "prefix, material" pair */
    public static final Map<UnificationEntry, ArrayList<ItemLike>> UNIFICATION_ENTRY_ITEM = new Object2ObjectLinkedOpenHashMap<>();
    public static final Map<UnificationEntry, ArrayList<Block>> UNIFICATION_ENTRY_BLOCK = new Object2ObjectLinkedOpenHashMap<>();
    /** Mapping of stone type blockState to "prefix, material" */
    public static final Map<BlockState, TagPrefix> ORES_INVERSE = new HashMap<>();

    public static void registerMaterialInfo(ItemLike item, ItemMaterialInfo materialInfo) {
        ITEM_MATERIAL_INFO.put(item, materialInfo);
    }

    public static ItemMaterialInfo getMaterialInfo(ItemLike item) {
        return ITEM_MATERIAL_INFO.get(item);
    }

    public static void registerUnificationItems(UnificationEntry unificationEntry, ItemLike... items) {
        UNIFICATION_ENTRY_ITEM.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                .addAll(Arrays.stream(items).toList());
        for (ItemLike item : items) {
            ITEM_UNIFICATION_ENTRY.put(item, unificationEntry);
            if (item instanceof Block block) {
                UNIFICATION_ENTRY_BLOCK.computeIfAbsent(unificationEntry, entry -> new ArrayList<>())
                        .add(block);
            }
        }
    }

    public static void registerUnificationItems(TagPrefix tagPrefix, @Nullable Material material, ItemLike... items) {
        registerUnificationItems(new UnificationEntry(tagPrefix, material), items);
        if (TagPrefix.ORES.containsKey(tagPrefix) && !ORES_INVERSE.containsValue(tagPrefix)) {
            ORES_INVERSE.put(TagPrefix.ORES.get(tagPrefix).stoneType().get(), tagPrefix);
        }
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
        var entry = ITEM_UNIFICATION_ENTRY.get(itemLike);
        if (entry != null) {
            Material entryMaterial = entry.material;
            if (entryMaterial != null) {
                return new MaterialStack(entryMaterial, entry.tagPrefix.getMaterialAmount(entryMaterial));
            }
        }
        ItemMaterialInfo info = ITEM_MATERIAL_INFO.get(itemLike);
        return info == null ? null : info.getMaterial().copy();
    }

    @Nullable
    public static TagPrefix getPrefix(ItemLike itemLike) {
        if (itemLike == null) return null;
        UnificationEntry entry = ITEM_UNIFICATION_ENTRY.get(itemLike);
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
        if (materialStack.material().hasProperty(PropertyKey.GEM)
                && !TagPrefix.gem.isIgnored(materialStack.material())
                && materialStack.amount() == TagPrefix.gem.getMaterialAmount(materialStack.material())) {
            return get(TagPrefix.gem, materialStack.material(), (int) (materialStack.amount() / M));
        }
        return getDust(materialStack);
    }

    @Nullable
    public static UnificationEntry getUnificationEntry(ItemLike item) {
        return ITEM_UNIFICATION_ENTRY.get(item);
    }

    public static List<ItemLike> getItems(UnificationEntry unificationEntry) {
        return UNIFICATION_ENTRY_ITEM.computeIfAbsent(unificationEntry, entry -> {
            var items = new ArrayList<ItemLike>();
            for (TagKey<Item> tag : getTags(entry.tagPrefix, entry.material)) {
                for (Holder<Item> itemHolder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    items.add(itemHolder.value());
                }
            }
            TagPrefix prefix = entry.tagPrefix;
            if (items.isEmpty() && prefix.hasItemTable() && prefix.doGenerateItem(entry.material)) {
                return new ArrayList<>(List.of(prefix.getItemFromTable(entry.material).get()));
            }
            return items;
        });
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
            var blocks = new ArrayList<Block>();
            for (TagKey<Block> tag : Arrays.stream(getTags(unificationEntry.tagPrefix, unificationEntry.material)).map(itemTagKey -> TagKey.create(Registries.BLOCK, itemTagKey.location())).toList()) {
                for (Holder<Block> itemHolder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
                    blocks.add(itemHolder.value());
                }
            }
            return blocks;
        });
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
    public static TagKey<Block> getBlockTag(TagPrefix orePrefix, @Nonnull Material material) {
        var tags = orePrefix.getBlockTags(material);
        if (tags.length > 0) {
            return tags[0];
        }
        return null;
    }

    @Nullable
    public static TagKey<Item> getTag(TagPrefix orePrefix, @Nonnull Material material) {
        var tags = orePrefix.getItemTags(material);
        if (tags.length > 0) {
            return tags[0];
        }
        return null;
    }

    public static TagKey<Item>[] getTags(TagPrefix orePrefix, @Nonnull Material material) {
        return orePrefix.getItemTags(material);
    }

    public static List<Map.Entry<ItemStack, ItemMaterialInfo>> getAllItemInfos() {
        return ITEM_MATERIAL_INFO.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(new ItemStack(entry.getKey().asItem()), entry.getValue()))
                .collect(Collectors.toList());
    }
}
