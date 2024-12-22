package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

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
    public final boolean addStoneUnificationInfo;
    public final boolean addPolishedStoneUnificationInfo;
    public final boolean addSmeltStoneUnificationInfo;
    public final boolean addChiselStoneUnificationInfo;
    public final boolean addCrackedStoneUnificationInfo;
    public final boolean addSlabUnificationInfo;
    public final boolean addStairUnificationInfo;
    public final boolean addButtonUnificationInfo;
    public final boolean addWallUnificationInfo;
    public final boolean addPressurePlateUnificationInfo;

    private StoneTypeEntry(@NotNull String modid, @NotNull String stoneName,
                           @Nullable Item stone, @Nullable Item polishedStone,
                           @Nullable Item smeltStone, @Nullable Item chiselStone,
                           @Nullable Item crackedStone, @Nullable Item slab,
                           @Nullable Item stair, @Nullable Item button,
                           @Nullable Item wall, @Nullable Item pressurePlate,
                           @Nullable Material material, long materialAmount,
                           boolean addStoneOreDict, boolean addPolishedStoneOreDict,
                           boolean addSlabOreDict,
                           boolean addStairOreDict, boolean addButtonOreDict,
                           boolean addWallOreDict, boolean addPressurePlateOreDict,
                           boolean addStoneUnificationInfo, boolean addPolishedStoneUnificationInfo,
                           boolean addSmeltStoneUnificationInfo, boolean addChiselStoneUnificationInfo,
                           boolean addCrackedStoneUnificationInfo, boolean addSlabUnificationInfo,
                           boolean addStairUnificationInfo, boolean addButtonUnificationInfo,
                           boolean addWallUnificationInfo, boolean addPressurePlateUnificationInfo) {
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
        this.addStoneUnificationInfo = addStoneUnificationInfo;
        this.addPolishedStoneUnificationInfo = addPolishedStoneUnificationInfo;
        this.addChiselStoneUnificationInfo = addChiselStoneUnificationInfo;
        this.addCrackedStoneUnificationInfo = addCrackedStoneUnificationInfo;
        this.addSmeltStoneUnificationInfo = addSmeltStoneUnificationInfo;
        this.addSlabUnificationInfo = addSlabUnificationInfo;
        this.addStairUnificationInfo = addStairUnificationInfo;
        this.addButtonUnificationInfo = addButtonUnificationInfo;
        this.addWallUnificationInfo = addWallUnificationInfo;
        this.addPressurePlateUnificationInfo = addPressurePlateUnificationInfo;
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
        @Nullable
        private Material material = null;
        private long materialAmount = GTValues.M;
        public boolean addStoneOreDict = false;
        public boolean addPolishedStoneOreDict = false;
        public boolean addSlabOreDict = false;
        public boolean addStairOreDict = false;
        public boolean addButtonOreDict = false;
        public boolean addWallOreDict = false;
        public boolean addPressurePlateOreDict = false;
        public boolean addStoneUnificationInfo = false;
        public boolean addPolishedStoneUnificationInfo = false;
        public boolean addSmeltStoneUnificationInfo = false;
        public boolean addChiselStoneUnificationInfo = false;
        public boolean addCrackedStoneUnificationInfo = false;
        public boolean addSlabUnificationInfo = false;
        public boolean addStairUnificationInfo = false;
        public boolean addButtonUnificationInfo = false;
        public boolean addWallUnificationInfo = false;
        public boolean addPressurePlateUnificationInfo = false;

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

        public Builder registerAllUnificationInfo() {
            return registerUnificationInfo(true, true, true, true, true, true, true, true, true);
        }

        public Builder registerUnificationInfo(boolean stone, boolean polishedStone, boolean smeltStone,
                                               boolean chiselStone, boolean slab, boolean stair, boolean button,
                                               boolean wall, boolean pressurePlate) {
            this.addStoneUnificationInfo = stone;
            this.addPolishedStoneUnificationInfo = polishedStone;
            this.addSmeltStoneUnificationInfo = smeltStone;
            this.addChiselStoneUnificationInfo = chiselStone;
            this.addSlabUnificationInfo = slab;
            this.addStairUnificationInfo = stair;
            this.addButtonUnificationInfo = button;
            this.addWallUnificationInfo = wall;
            this.addPressurePlateUnificationInfo = pressurePlate;
            return this;
        }

        public StoneTypeEntry build() {
            return new StoneTypeEntry(modid, stoneName,
                    stone, polishedStone, smeltStone, chiselStone, crackedStone, slab, stair, button, wall,
                    pressurePlate,
                    material, materialAmount,
                    addStoneOreDict, addPolishedStoneOreDict, addSlabOreDict, addStairOreDict,
                    addButtonOreDict, addWallOreDict, addPressurePlateOreDict,
                    addStoneUnificationInfo, addPolishedStoneUnificationInfo, addSmeltStoneUnificationInfo,
                    addChiselStoneUnificationInfo, addCrackedStoneUnificationInfo, addSlabUnificationInfo,
                    addStairUnificationInfo, addButtonUnificationInfo,
                    addWallUnificationInfo, addPressurePlateUnificationInfo);
        }
    }
}
