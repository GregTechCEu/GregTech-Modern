package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.world.item.Item;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StoneTypeEntry {

    @NotNull
    public final String modid;
    @NotNull
    public final String stoneName;
    @Nullable
    public final Item stone;
    @Nullable
    public final Item polishedStone;
    @Nullable
    public final Item smeltStone;
    @Nullable
    public final Item chiselStone;
    @Nullable
    public final Item crackedStone;
    @Nullable
    public final Item slab;
    @Nullable
    public final Item stair;
    @Nullable
    public final Item button;
    @Nullable
    public final Item pressurePlate;
    @Nullable
    public final Item wall;
    public final Material material;
    public final long materialAmount;
    public final boolean addStoneOreDict;
    public final boolean addPolishedStoneOreDict;
    public final boolean addSlabOreDict;
    public final boolean addStairOreDict;
    public final boolean addButtonOreDict;
    public final boolean addWallOreDict;
    public final boolean addPressurePlateOreDict;
    public final boolean addStoneMaterialInfo;
    public final boolean addPolishedStoneMaterialInfo;
    public final boolean addSmeltStoneMaterialInfo;
    public final boolean addChiselStoneMaterialInfo;
    public final boolean addCrackedStoneMaterialInfo;
    public final boolean addSlabMaterialInfo;
    public final boolean addStairMaterialInfo;
    public final boolean addButtonMaterialInfo;
    public final boolean addWallMaterialInfo;
    public final boolean addPressurePlateMaterialInfo;

    private StoneTypeEntry(@NotNull String modid, @NotNull String stoneName,
                           @Nullable Item stone, @Nullable Item polishedStone,
                           @Nullable Item smeltStone, @Nullable Item chiselStone,
                           @Nullable Item crackedStone, @Nullable Item slab,
                           @Nullable Item stair, @Nullable Item button,
                           @Nullable Item wall, @Nullable Item pressurePlate,
                           @NotNull Material material, long materialAmount,
                           boolean addStoneOreDict, boolean addPolishedStoneOreDict,
                           boolean addSlabOreDict,
                           boolean addStairOreDict, boolean addButtonOreDict,
                           boolean addWallOreDict, boolean addPressurePlateOreDict,
                           boolean addStoneMaterialInfo, boolean addPolishedStoneMaterialInfo,
                           boolean addSmeltStoneMaterialInfo, boolean addChiselStoneMaterialInfo,
                           boolean addCrackedStoneMaterialInfo, boolean addSlabMaterialInfo,
                           boolean addStairMaterialInfo, boolean addButtonMaterialInfo,
                           boolean addWallMaterialInfo, boolean addPressurePlateMaterialInfo) {
        this.modid = modid;
        this.stoneName = stoneName;
        this.stone = stone;
        this.polishedStone = polishedStone;
        this.smeltStone = smeltStone;
        this.chiselStone = chiselStone;
        this.crackedStone = crackedStone;
        this.slab = slab;
        this.stair = stair;
        this.button = button;
        this.wall = wall;
        this.pressurePlate = pressurePlate;
        this.material = material;
        this.materialAmount = materialAmount;
        this.addStoneOreDict = addStoneOreDict;
        this.addPolishedStoneOreDict = addPolishedStoneOreDict;
        this.addSlabOreDict = addSlabOreDict;
        this.addStairOreDict = addStairOreDict;
        this.addButtonOreDict = addButtonOreDict;
        this.addWallOreDict = addWallOreDict;
        this.addPressurePlateOreDict = addPressurePlateOreDict;
        this.addStoneMaterialInfo = addStoneMaterialInfo;
        this.addPolishedStoneMaterialInfo = addPolishedStoneMaterialInfo;
        this.addChiselStoneMaterialInfo = addChiselStoneMaterialInfo;
        this.addCrackedStoneMaterialInfo = addCrackedStoneMaterialInfo;
        this.addSmeltStoneMaterialInfo = addSmeltStoneMaterialInfo;
        this.addSlabMaterialInfo = addSlabMaterialInfo;
        this.addStairMaterialInfo = addStairMaterialInfo;
        this.addButtonMaterialInfo = addButtonMaterialInfo;
        this.addWallMaterialInfo = addWallMaterialInfo;
        this.addPressurePlateMaterialInfo = addPressurePlateMaterialInfo;
    }

    public static class Builder {

        public final String modid;
        public final String stoneName;
        public Item stone = null;
        public Item polishedStone = null;
        public Item smeltStone = null;
        public Item chiselStone = null;
        public Item crackedStone = null;
        public Item slab = null;
        public Item stair = null;
        public Item button = null;
        public Item wall = null;
        public Item pressurePlate = null;
        @NotNull
        private Material material = GTMaterials.NULL;
        private long materialAmount = GTValues.M;
        public boolean addStoneOreDict = false;
        public boolean addPolishedStoneOreDict = false;
        public boolean addSlabOreDict = false;
        public boolean addStairOreDict = false;
        public boolean addButtonOreDict = false;
        public boolean addWallOreDict = false;
        public boolean addPressurePlateOreDict = false;
        public boolean addStoneMaterialInfo = false;
        public boolean addPolishedStoneMaterialInfo = false;
        public boolean addSmeltStoneMaterialInfo = false;
        public boolean addChiselStoneMaterialInfo = false;
        public boolean addCrackedStoneMaterialInfo = false;
        public boolean addSlabMaterialInfo = false;
        public boolean addStairMaterialInfo = false;
        public boolean addButtonMaterialInfo = false;
        public boolean addWallMaterialInfo = false;
        public boolean addPressurePlateMaterialInfo = false;

        public Builder(@NotNull String modid, @NotNull String stoneName) {
            this.modid = modid;
            this.stoneName = stoneName;
        }

        public Builder stone(@NotNull Item stone) {
            this.stone = stone;
            return this;
        }

        public Builder polishedStone(@NotNull Item polishedStone) {
            this.polishedStone = polishedStone;
            return this;
        }

        public Builder smeltStone(@NotNull Item smeltStone) {
            this.smeltStone = smeltStone;
            return this;
        }

        public Builder chiselStone(@NotNull Item chiselStone) {
            this.chiselStone = chiselStone;
            return this;
        }

        public Builder crackedStone(@NotNull Item crackedStone) {
            this.crackedStone = crackedStone;
            return this;
        }

        public Builder slab(@NotNull Item slab) {
            this.slab = slab;
            return this;
        }

        public Builder stair(@NotNull Item stair) {
            this.stair = stair;
            return this;
        }

        public Builder button(@NotNull Item button) {
            this.button = button;
            return this;
        }

        public Builder wall(@NotNull Item wall) {
            this.wall = wall;
            return this;
        }

        public Builder pressurePlate(@NotNull Item pressurePlate) {
            this.pressurePlate = pressurePlate;
            return this;
        }

        public Builder material(@NotNull Material material) {
            return material(material, GTValues.M);
        }

        public Builder material(@NotNull Material material, long materialAmount) {
            this.material = material;
            this.materialAmount = materialAmount;
            return this;
        }

        public Builder registerAllMaterialInfo() {
            return registerMaterialInfo(true, true, true, true, true, true, true, true, true);
        }

        public Builder registerMaterialInfo(boolean stone, boolean polishedStone, boolean smeltStone,
                                            boolean chiselStone, boolean slab, boolean stair, boolean button,
                                            boolean wall, boolean pressurePlate) {
            this.addStoneMaterialInfo = stone;
            this.addPolishedStoneMaterialInfo = polishedStone;
            this.addSmeltStoneMaterialInfo = smeltStone;
            this.addChiselStoneMaterialInfo = chiselStone;
            this.addSlabMaterialInfo = slab;
            this.addStairMaterialInfo = stair;
            this.addButtonMaterialInfo = button;
            this.addWallMaterialInfo = wall;
            this.addPressurePlateMaterialInfo = pressurePlate;
            return this;
        }

        public StoneTypeEntry build() {
            return new StoneTypeEntry(modid, stoneName,
                    stone, polishedStone, smeltStone, chiselStone, crackedStone, slab, stair, button, wall,
                    pressurePlate,
                    material, materialAmount,
                    addStoneOreDict, addPolishedStoneOreDict, addSlabOreDict, addStairOreDict,
                    addButtonOreDict, addWallOreDict, addPressurePlateOreDict,
                    addStoneMaterialInfo, addPolishedStoneMaterialInfo, addSmeltStoneMaterialInfo,
                    addChiselStoneMaterialInfo, addCrackedStoneMaterialInfo, addSlabMaterialInfo,
                    addStairMaterialInfo, addButtonMaterialInfo,
                    addWallMaterialInfo, addPressurePlateMaterialInfo);
        }
    }
}
