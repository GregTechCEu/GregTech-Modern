package com.gregtechceu.gtceu.api.data.chemical.material.info;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import com.lowdragmc.lowdraglib.utils.ResourceHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record MaterialIconType(String name) {

    public static final Map<String, MaterialIconType> ICON_TYPES = new HashMap<>();

    public static final MaterialIconType dustTiny = new MaterialIconType("dustTiny");
    public static final MaterialIconType dustSmall = new MaterialIconType("dustSmall");
    public static final MaterialIconType dust = new MaterialIconType("dust");
    public static final MaterialIconType dustImpure = new MaterialIconType("dustImpure");
    public static final MaterialIconType dustPure = new MaterialIconType("dustPure");

    public static final MaterialIconType rawOre = new MaterialIconType("rawOre");
    public static final MaterialIconType rawOreBlock = new MaterialIconType("rawOreBlock");
    public static final MaterialIconType crushed = new MaterialIconType("crushed");
    public static final MaterialIconType crushedPurified = new MaterialIconType("crushedPurified");
    public static final MaterialIconType crushedRefined = new MaterialIconType("crushedRefined");

    public static final MaterialIconType gem = new MaterialIconType("gem");
    public static final MaterialIconType gemChipped = new MaterialIconType("gemChipped");
    public static final MaterialIconType gemFlawed = new MaterialIconType("gemFlawed");
    public static final MaterialIconType gemFlawless = new MaterialIconType("gemFlawless");
    public static final MaterialIconType gemExquisite = new MaterialIconType("gemExquisite");

    public static final MaterialIconType nugget = new MaterialIconType("nugget");

    public static final MaterialIconType ingot = new MaterialIconType("ingot");
    public static final MaterialIconType ingotHot = new MaterialIconType("ingotHot");
    public static final MaterialIconType ingotDouble = new MaterialIconType("ingotDouble");
    public static final MaterialIconType ingotTriple = new MaterialIconType("ingotTriple");
    public static final MaterialIconType ingotQuadruple = new MaterialIconType("ingotQuadruple");
    public static final MaterialIconType ingotQuintuple = new MaterialIconType("ingotQuintuple");

    public static final MaterialIconType plate = new MaterialIconType("plate");
    public static final MaterialIconType plateDouble = new MaterialIconType("plateDouble");
    public static final MaterialIconType plateTriple = new MaterialIconType("plateTriple");
    public static final MaterialIconType plateQuadruple = new MaterialIconType("plateQuadruple");
    public static final MaterialIconType plateQuintuple = new MaterialIconType("plateQuintuple");
    public static final MaterialIconType plateDense = new MaterialIconType("plateDense");

    public static final MaterialIconType rod = new MaterialIconType("rod");
    public static final MaterialIconType lens = new MaterialIconType("lens");
    public static final MaterialIconType round = new MaterialIconType("round");
    public static final MaterialIconType bolt = new MaterialIconType("bolt");
    public static final MaterialIconType screw = new MaterialIconType("screw");
    public static final MaterialIconType ring = new MaterialIconType("ring");
    public static final MaterialIconType wireFine = new MaterialIconType("wireFine");
    public static final MaterialIconType gearSmall = new MaterialIconType("gearSmall");
    public static final MaterialIconType rotor = new MaterialIconType("rotor");
    public static final MaterialIconType rodLong = new MaterialIconType("rodLong");
    public static final MaterialIconType springSmall = new MaterialIconType("springSmall");
    public static final MaterialIconType spring = new MaterialIconType("spring");
    public static final MaterialIconType gear = new MaterialIconType("gear");
    public static final MaterialIconType foil = new MaterialIconType("foil");

    public static final MaterialIconType toolHeadSword = new MaterialIconType("toolHeadSword");
    public static final MaterialIconType toolHeadPickaxe = new MaterialIconType("toolHeadPickaxe");
    public static final MaterialIconType toolHeadShovel = new MaterialIconType("toolHeadShovel");
    public static final MaterialIconType toolHeadAxe = new MaterialIconType("toolHeadAxe");
    public static final MaterialIconType toolHeadHoe = new MaterialIconType("toolHeadHoe");
    public static final MaterialIconType toolHeadHammer = new MaterialIconType("toolHeadHammer");
    public static final MaterialIconType toolHeadFile = new MaterialIconType("toolHeadFile");
    public static final MaterialIconType toolHeadSaw = new MaterialIconType("toolHeadSaw");
    public static final MaterialIconType toolHeadBuzzSaw = new MaterialIconType("toolHeadBuzzSaw");
    public static final MaterialIconType toolHeadDrill = new MaterialIconType("toolHeadDrill");
    public static final MaterialIconType toolHeadChainsaw = new MaterialIconType("toolHeadChainsaw");
    public static final MaterialIconType toolHeadScythe = new MaterialIconType("toolHeadScythe");
    public static final MaterialIconType toolHeadScrewdriver = new MaterialIconType("toolHeadScrewdriver");
    public static final MaterialIconType toolHeadWrench = new MaterialIconType("toolHeadWrench");
    public static final MaterialIconType toolHeadWireCutter = new MaterialIconType("toolHeadWireCutter");

    public static final MaterialIconType turbineBlade = new MaterialIconType("turbineBlade");

    // BLOCK TEXTURES
    public static final MaterialIconType liquid = new MaterialIconType("liquid");
    public static final MaterialIconType gas = new MaterialIconType("gas");
    public static final MaterialIconType plasma = new MaterialIconType("plasma");
    public static final MaterialIconType molten = new MaterialIconType("molten");
    public static final MaterialIconType block = new MaterialIconType("block");
    public static final MaterialIconType ore = new MaterialIconType("ore");
    public static final MaterialIconType oreSmall = new MaterialIconType("oreSmall");
    public static final MaterialIconType frameGt = new MaterialIconType("frameGt");
    public static final MaterialIconType wire = new MaterialIconType("wire");

    // USED FOR GREGIFICATION ADDON
    public static final MaterialIconType seed = new MaterialIconType("seed");
    public static final MaterialIconType crop = new MaterialIconType("crop");
    public static final MaterialIconType essence = new MaterialIconType("essence");

    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> ITEM_MODEL_CACHE = HashBasedTable
            .create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> ITEM_TEXTURE_CACHE = HashBasedTable
            .create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> ITEM_TEXTURE_CACHE_SECONDARY = HashBasedTable
            .create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCK_MODEL_CACHE = HashBasedTable
            .create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCK_TEXTURE_CACHE = HashBasedTable
            .create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCK_TEXTURE_CACHE_SECONDARY = HashBasedTable
            .create();

    public MaterialIconType(String name) {
        this.name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        Preconditions.checkArgument(!ICON_TYPES.containsKey(this.name),
                "MaterialIconType " + this.name + " already registered!");
        ICON_TYPES.put(this.name, this);
    }

    public static void init() {
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistryInfo.MATERIAL_ICON_TYPE.registryKey);
        }
    }

    public static MaterialIconType getByName(String name) {
        return ICON_TYPES.get(name);
    }

    public ResourceLocation getBlockTexturePath(@NotNull MaterialIconSet materialIconSet, boolean doReadCache) {
        return getBlockTexturePath(materialIconSet, null, doReadCache);
    }

    public ResourceLocation getBlockTexturePath(@NotNull MaterialIconSet materialIconSet, @Nullable String suffix,
                                                boolean doReadCache) {
        if (doReadCache) {
            if (suffix == null || suffix.isBlank()) {
                if (BLOCK_TEXTURE_CACHE.contains(this, materialIconSet))
                    return BLOCK_TEXTURE_CACHE.get(this, materialIconSet);
            } else {
                if (BLOCK_TEXTURE_CACHE_SECONDARY.contains(this, materialIconSet))
                    return BLOCK_TEXTURE_CACHE_SECONDARY.get(this, materialIconSet);
            }
        }

        suffix = suffix == null || suffix.isBlank() ? "" : "_" + suffix;

        MaterialIconSet iconSet = materialIconSet;
        // noinspection ConstantConditions
        if (!GTCEu.isClientSide() || Minecraft.getInstance() == null ||
                Minecraft.getInstance().getResourceManager() == null)
            return GTModels.BLANK_TEXTURE; // check minecraft for null for CI environments
        if (!iconSet.isRootIconset) {
            while (!iconSet.isRootIconset) {
                ResourceLocation location = GTCEu
                        .id(String.format("textures/block/material_sets/%s/%s%s.png", iconSet.name, this.name, suffix));
                if (ResourceHelper.isResourceExist(location) || ResourceHelper.isResourceExistRaw(location))
                    break;
                iconSet = iconSet.parentIconset;
            }
        }

        ResourceLocation location = GTCEu
                .id(String.format("textures/block/material_sets/%s/%s%s.png", iconSet.name, this.name, suffix));
        if (!suffix.isEmpty() && !ResourceHelper.isResourceExist(location) &&
                !ResourceHelper.isResourceExistRaw(location)) {
            return GTModels.BLANK_TEXTURE;
        }
        location = GTCEu.id(String.format("block/material_sets/%s/%s%s", iconSet.name, this.name, suffix));
        if (suffix.isEmpty()) {
            BLOCK_TEXTURE_CACHE.put(this, materialIconSet, location);
        } else {
            BLOCK_TEXTURE_CACHE_SECONDARY.put(this, materialIconSet, location);
        }

        return location;
    }

    @NotNull
    public ResourceLocation getBlockModelPath(@NotNull MaterialIconSet materialIconSet, boolean doReadCache) {
        if (doReadCache) {
            if (BLOCK_MODEL_CACHE.contains(this, materialIconSet)) {
                return BLOCK_MODEL_CACHE.get(this, materialIconSet);
            }
        }

        MaterialIconSet iconSet = materialIconSet;
        // noinspection ConstantConditions
        if (!iconSet.isRootIconset && GTCEu.isClientSide() && Minecraft.getInstance() != null &&
                Minecraft.getInstance().getResourceManager() != null) { // check minecraft for null for CI environments
            while (!iconSet.isRootIconset) {
                ResourceLocation location = GTCEu
                        .id(String.format("models/block/material_sets/%s/%s.json", iconSet.name, this.name));
                if (ResourceHelper.isResourceExist(location) || ResourceHelper.isResourceExistRaw(location))
                    break;
                iconSet = iconSet.parentIconset;
            }
        }

        ResourceLocation location = GTCEu.id(String.format("block/material_sets/%s/%s", iconSet.name, this.name));
        ITEM_MODEL_CACHE.put(this, materialIconSet, location);

        return location;
    }

    @NotNull
    public ResourceLocation getItemModelPath(@NotNull MaterialIconSet materialIconSet, boolean doReadCache) {
        if (doReadCache) {
            if (ITEM_MODEL_CACHE.contains(this, materialIconSet)) {
                return ITEM_MODEL_CACHE.get(this, materialIconSet);
            }
        }

        MaterialIconSet iconSet = materialIconSet;
        // noinspection ConstantConditions
        if (!iconSet.isRootIconset && GTCEu.isClientSide() && Minecraft.getInstance() != null &&
                Minecraft.getInstance().getResourceManager() != null) { // check minecraft for null for CI environments
            while (!iconSet.isRootIconset) {
                ResourceLocation location = GTCEu
                        .id(String.format("models/item/material_sets/%s/%s.json", iconSet.name, this.name));
                if (ResourceHelper.isResourceExist(location) || ResourceHelper.isResourceExistRaw(location))
                    break;
                iconSet = iconSet.parentIconset;
            }
        }

        ResourceLocation location = GTCEu.id(String.format("item/material_sets/%s/%s", iconSet.name, this.name));
        ITEM_MODEL_CACHE.put(this, materialIconSet, location);

        return location;
    }

    @Nullable
    public ResourceLocation getItemTexturePath(@NotNull MaterialIconSet materialIconSet, boolean doReadCache) {
        return getItemTexturePath(materialIconSet, null, doReadCache);
    }

    @Nullable
    public ResourceLocation getItemTexturePath(@NotNull MaterialIconSet materialIconSet, String suffix,
                                               boolean doReadCache) {
        if (doReadCache) {
            if (suffix == null || suffix.isBlank()) {
                if (ITEM_TEXTURE_CACHE.contains(this, materialIconSet))
                    return ITEM_TEXTURE_CACHE.get(this, materialIconSet);
            } else {
                if (ITEM_TEXTURE_CACHE_SECONDARY.contains(this, materialIconSet))
                    return ITEM_TEXTURE_CACHE_SECONDARY.get(this, materialIconSet);
            }
        }

        suffix = suffix == null || suffix.isBlank() ? "" : "_" + suffix;

        MaterialIconSet iconSet = materialIconSet;
        // noinspection ConstantConditions
        if (!iconSet.isRootIconset && GTCEu.isClientSide() && Minecraft.getInstance() != null &&
                Minecraft.getInstance().getResourceManager() != null) { // check minecraft for null for CI environments
            while (!iconSet.isRootIconset) {
                ResourceLocation location = GTCEu
                        .id(String.format("textures/item/material_sets/%s/%s%s.png", iconSet.name, this.name, suffix));
                if (ResourceHelper.isResourceExist(location) || ResourceHelper.isResourceExistRaw(location))
                    break;
                iconSet = iconSet.parentIconset;
            }
        }

        ResourceLocation location = GTCEu
                .id(String.format("textures/item/material_sets/%s/%s%s.png", iconSet.name, this.name, suffix));
        if (!suffix.isEmpty() && !ResourceHelper.isResourceExist(location) &&
                !ResourceHelper.isResourceExistRaw(location)) {
            return null;
        }
        location = GTCEu.id(String.format("item/material_sets/%s/%s%s", iconSet.name, this.name, suffix));
        if (suffix.isEmpty()) {
            ITEM_TEXTURE_CACHE.put(this, materialIconSet, location);
        } else {
            ITEM_TEXTURE_CACHE_SECONDARY.put(this, materialIconSet, location);
        }

        return location;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
