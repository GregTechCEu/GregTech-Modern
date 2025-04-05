package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Entry for a wood type and all of its associated items
 */
public final class WoodTypeEntry {

    @NotNull
    public final String modid;
    @NotNull
    public final String woodName;
    @NotNull
    public final TagKey<Item> logTag;
    // 4 sided bark
    @Nullable
    public final Item log;
    @Nullable
    public final Item strippedLog;
    // 6 sided bark
    @Nullable
    public final Item wood;
    @Nullable
    public final Item strippedWood;
    /**
     * if log -> charcoal recipes should be removed
     */
    public final boolean removeCharcoalRecipe;
    /**
     * if log -> charcoal recipes should be added
     */
    public final boolean addCharcoalRecipe;
    @NotNull
    public final Item planks;
    @Nullable
    public final String planksRecipeName;
    @Nullable
    public final Item door;
    @Nullable
    public final String doorRecipeName;
    @Nullable
    public final Item trapdoor;
    @Nullable
    public final String trapdoorRecipeName;
    @Nullable
    public final Item slab;
    @Nullable
    public final String slabRecipeName;
    public final boolean addSlabCraftingRecipe;
    public final Item fence;
    @Nullable
    public final String fenceRecipeName;
    @Nullable
    public final Item fenceGate;
    @Nullable
    public final String fenceGateRecipeName;
    @Nullable
    public final Item stairs;
    @Nullable
    public final String stairsRecipeName;
    public final boolean addStairsCraftingRecipe;
    @Nullable
    public final Item boat;
    @Nullable
    public final String boatRecipeName;
    @Nullable
    public final Item chestBoat;
    @Nullable
    public final String chestBoatRecipeName;
    @Nullable
    public final Item sign;
    @Nullable
    public final String signRecipeName;
    @Nullable
    public final Item hangingSign;
    @Nullable
    public final String hangingSignRecipeName;
    @Nullable
    public final Item button;
    @Nullable
    public final String buttonRecipeName;
    @Nullable
    public final Item pressurePlate;
    @Nullable
    public final String pressurePlateRecipeName;
    public final Material material;

    public final boolean addLogOreDict;
    public final boolean addPlanksOreDict;
    public final boolean addDoorsOreDict;
    public final boolean addSlabsOreDict;
    public final boolean addFencesOreDict;
    public final boolean addFenceGatesOreDict;
    public final boolean addStairsOreDict;
    public final boolean addBoatsOreDict;
    public final boolean addChestBoatsOreDict;
    public final boolean addButtonsOreDict;
    public final boolean addPressurePlatesOreDict;

    public final boolean addPlanksMaterialInfo;
    public final boolean addDoorsMaterialInfo;
    public final boolean addSlabsMaterialInfo;
    public final boolean addFencesMaterialInfo;
    public final boolean addFenceGatesMaterialInfo;
    public final boolean addStairsMaterialInfo;
    public final boolean addBoatsMaterialInfo;
    public final boolean addChestBoatsMaterialInfo;
    public final boolean addButtonsMaterialInfo;
    public final boolean addPressurePlatesMaterialInfo;
    public final boolean generateLogToPlankRecipe;

    /**
     * @see Builder
     */
    private WoodTypeEntry(@NotNull String modid, @NotNull String woodName, @NotNull TagKey<Item> logTag,
                          @Nullable Item log, @Nullable Item strippedLog,
                          @Nullable Item wood, @Nullable Item strippedWood,
                          boolean removeCharcoalRecipe, boolean addCharcoalRecipe,
                          @NotNull Item planks, @Nullable String planksRecipeName,
                          @Nullable Item door, @Nullable String doorRecipeName,
                          @Nullable Item trapdoor, @Nullable String trapdoorRecipeName,
                          @Nullable Item slab, @Nullable String slabRecipeName, boolean addSlabCraftingRecipe,
                          @Nullable Item fence, @Nullable String fenceRecipeName,
                          @Nullable Item fenceGate, @Nullable String fenceGateRecipeName,
                          @Nullable Item stairs, @Nullable String stairsRecipeName, boolean addStairsCraftingRecipe,
                          @Nullable Item boat, @Nullable String boatRecipeName,
                          @Nullable Item chestBoat, @Nullable String chestBoatRecipeName,
                          @Nullable Item sign, @Nullable String signRecipeName,
                          @Nullable Item hangingSign, @Nullable String hangingSignRecipeName,
                          @Nullable Item button, @Nullable String buttonRecipeName,
                          @Nullable Item pressurePlate, @Nullable String pressurePlateRecipeName,
                          @NotNull Material material,
                          boolean addLogOreDict, boolean addPlanksOreDict, boolean addDoorsOreDict,
                          boolean addSlabsOreDict,
                          boolean addFencesOreDict, boolean addFenceGatesOreDict, boolean addStairsOreDict,
                          boolean addBoatsOreDict, boolean addChestBoatsOreDict,
                          boolean addButtonsOreDict, boolean addPressurePlatesOreDict,
                          boolean addPlanksMaterialInfo, boolean addDoorsMaterialInfo,
                          boolean addSlabsMaterialInfo, boolean addFencesMaterialInfo,
                          boolean addFenceGatesMaterialInfo, boolean addStairsMaterialInfo,
                          boolean addBoatsMaterialInfo, boolean addChestBoatsMaterialInfo,
                          boolean addPressurePlatesMaterialInfo, boolean addButtonsMaterialInfo,
                          boolean generateLogToPlankRecipe) {
        this.modid = modid;
        this.woodName = woodName;
        this.logTag = logTag;
        this.log = log;
        this.strippedLog = strippedLog;
        this.wood = wood;
        this.strippedWood = strippedWood;
        this.removeCharcoalRecipe = removeCharcoalRecipe;
        this.addCharcoalRecipe = addCharcoalRecipe;
        this.planks = planks;
        this.planksRecipeName = planksRecipeName;
        this.door = door;
        this.doorRecipeName = doorRecipeName;
        this.trapdoor = trapdoor;
        this.trapdoorRecipeName = trapdoorRecipeName;
        this.slab = slab;
        this.slabRecipeName = slabRecipeName;
        this.addSlabCraftingRecipe = addSlabCraftingRecipe;
        this.fence = fence;
        this.fenceRecipeName = fenceRecipeName;
        this.fenceGate = fenceGate;
        this.fenceGateRecipeName = fenceGateRecipeName;
        this.stairs = stairs;
        this.stairsRecipeName = stairsRecipeName;
        this.addStairsCraftingRecipe = addStairsCraftingRecipe;
        this.boat = boat;
        this.boatRecipeName = boatRecipeName;
        this.chestBoat = chestBoat;
        this.chestBoatRecipeName = chestBoatRecipeName;
        this.sign = sign;
        this.signRecipeName = signRecipeName;
        this.hangingSign = hangingSign;
        this.hangingSignRecipeName = hangingSignRecipeName;
        this.button = button;
        this.buttonRecipeName = buttonRecipeName;
        this.pressurePlate = pressurePlate;
        this.pressurePlateRecipeName = pressurePlateRecipeName;
        this.material = !material.isNull() ? material : GTMaterials.Wood;

        this.addLogOreDict = addLogOreDict;
        this.addPlanksOreDict = addPlanksOreDict;
        this.addDoorsOreDict = addDoorsOreDict;
        this.addSlabsOreDict = addSlabsOreDict;
        this.addFencesOreDict = addFencesOreDict;
        this.addFenceGatesOreDict = addFenceGatesOreDict;
        this.addStairsOreDict = addStairsOreDict;
        this.addBoatsOreDict = addBoatsOreDict;
        this.addChestBoatsOreDict = addChestBoatsOreDict;
        this.addButtonsOreDict = addButtonsOreDict;
        this.addPressurePlatesOreDict = addPressurePlatesOreDict;
        this.addPlanksMaterialInfo = addPlanksMaterialInfo;
        this.addDoorsMaterialInfo = addDoorsMaterialInfo;
        this.addSlabsMaterialInfo = addSlabsMaterialInfo;
        this.addFencesMaterialInfo = addFencesMaterialInfo;
        this.addFenceGatesMaterialInfo = addFenceGatesMaterialInfo;
        this.addStairsMaterialInfo = addStairsMaterialInfo;
        this.addBoatsMaterialInfo = addBoatsMaterialInfo;
        this.addChestBoatsMaterialInfo = addChestBoatsMaterialInfo;
        this.addButtonsMaterialInfo = addButtonsMaterialInfo;
        this.addPressurePlatesMaterialInfo = addPressurePlatesMaterialInfo;
        this.generateLogToPlankRecipe = generateLogToPlankRecipe;
    }

    @NotNull
    public TagKey<Item> getStick() {
        if (this.material == GTMaterials.Wood) {
            return Tags.Items.RODS_WOODEN;
        } else {
            // noinspection DataFlowIssue is valid.
            return ChemicalHelper.getTag(TagPrefix.rod, this.material);
        }
    }

    public Item[] getLogs() {
        return new Item[] { this.log, this.wood, this.strippedWood, this.strippedLog };
    }

    public static class Builder {

        private final String modid;
        private final String woodName;

        private TagKey<Item> logTag = null;
        private Item log = null;
        private Item strippedLog = null;
        private Item wood = null;
        private Item strippedWood = null;
        private boolean removeCharcoalRecipe;
        private boolean addCharcoalRecipe;
        private Item planks = null;
        private String planksRecipeName;
        private Item door = null;
        private String doorRecipeName;
        private Item trapdoor = null;
        private String trapdoorRecipeName;
        private Item slab = null;
        private String slabRecipeName;
        private boolean addSlabsCraftingRecipe;
        private Item fence = null;
        private String fenceRecipeName;
        private Item fenceGate = null;
        private String fenceGateRecipeName;
        private Item stairs = null;
        private String stairsRecipeName;
        private boolean addStairsCraftingRecipe;
        private Item boat = null;
        private String boatRecipeName;
        private Item chestBoat = null;
        private String chestBoatRecipeName;
        private Item sign = null;
        private String signRecipeName;
        private Item hangingSign = null;
        private String hangingSignRecipeName;
        private Item button = null;
        private String buttonRecipeName;
        private Item pressurePlate = null;
        private String pressurePlateRecipeName;
        @NotNull
        private Material material = GTMaterials.NULL;

        private boolean addLogOreDict;
        private boolean addPlanksOreDict;
        private boolean addDoorsOreDict;
        private boolean addSlabsOreDict;
        private boolean addFencesOreDict;
        private boolean addFenceGatesOreDict;
        private boolean addStairsOreDict;
        private boolean addBoatsOreDict;
        private boolean addChestBoatsOreDict;
        private boolean addButtonOreDict;
        private boolean addPressurePlateOreDict;

        private boolean addPlanksMaterialInfo;
        private boolean addDoorsMaterialInfo;
        private boolean addSlabsMaterialInfo;
        private boolean addFencesMaterialInfo;
        private boolean addFenceGatesMaterialInfo;
        private boolean addStairsMaterialInfo;
        private boolean addBoatsMaterialInfo;
        private boolean addChestBoatsMaterialInfo;
        private boolean addButtonMaterialInfo;
        private boolean addPressurePlateMaterialInfo;
        private boolean generateLogToPlankRecipe = true;

        /**
         * @param modid    the modid adding recipes for the wood
         * @param woodName the name of the wood
         */
        public Builder(@NotNull String modid, @NotNull String woodName) {
            Preconditions.checkArgument(!modid.isEmpty(), "Modid cannot be empty.");
            Preconditions.checkArgument(!woodName.isEmpty(), "Wood name cannot be empty.");
            this.modid = modid;
            this.woodName = woodName;
        }

        /**
         * Add an entry for TagKey of logs
         *
         * @param logTag the TagKey to add
         * @return this
         */
        public Builder logTag(@NotNull TagKey<Item> logTag) {
            this.logTag = logTag;
            return this;
        }

        /**
         * Add an entry for logs
         *
         * @param log the log to add
         * @return this
         */
        public Builder log(@NotNull Item log) {
            this.log = log;
            return this;
        }

        /**
         * Add an entry for stripped logs
         *
         * @param strippedLog the stripped log to add
         * @return this
         */
        public Builder strippedLog(@NotNull Item strippedLog) {
            this.strippedLog = strippedLog;
            return this;
        }

        /**
         * Add an entry for wood (the 6-sided log block)
         *
         * @param wood the wood to add
         * @return this
         */
        public Builder wood(@NotNull Item wood) {
            this.wood = wood;
            return this;
        }

        /**
         * Add an entry for logs
         *
         * @param strippedWood the stripped wood to add
         * @return this
         */
        public Builder strippedWood(@NotNull Item strippedWood) {
            this.strippedWood = strippedWood;
            return this;
        }

        /**
         * Remove log -> charcoal recipe if the config is enabled
         *
         * @return this
         */
        public Builder removeCharcoalRecipe() {
            this.removeCharcoalRecipe = true;
            return this;
        }

        /**
         * Add log -> charcoal recipe if the config is disabled
         *
         * @return this
         */
        public Builder addCharcoalRecipe() {
            this.addCharcoalRecipe = true;
            return this;
        }

        /**
         * Add an entry for planks
         *
         * @param planks           the planks to add
         * @param planksRecipeName the recipe for crafting the planks
         * @return this
         */
        public Builder planks(@NotNull Item planks, @Nullable String planksRecipeName) {
            this.planks = planks;
            this.planksRecipeName = planksRecipeName;
            return this;
        }

        /**
         * Add an entry for a door
         *
         * @param door           the door to add
         * @param doorRecipeName the recipe name for crafting the door
         * @return this
         */
        public Builder door(@NotNull Item door, @Nullable String doorRecipeName) {
            this.door = door;
            this.doorRecipeName = doorRecipeName;
            return this;
        }

        /**
         * Add an entry for a trapdoor
         *
         * @param trapdoor           the trapdoor to add
         * @param trapdoorRecipeName the recipe name for crafting the trapdoor
         * @return this
         */
        public Builder trapdoor(@NotNull Item trapdoor, @Nullable String trapdoorRecipeName) {
            this.trapdoor = trapdoor;
            this.trapdoorRecipeName = trapdoorRecipeName;
            return this;
        }

        /**
         * Add an entry for a slab
         *
         * @param slab the slab to add
         * @return this
         */
        public Builder slab(@NotNull Item slab, @Nullable String slabRecipeName) {
            this.slab = slab;
            this.slabRecipeName = slabRecipeName;
            return this;
        }

        /**
         * Add crafting recipe for slab
         *
         * @return this
         */
        public Builder addSlabRecipe() {
            this.addSlabsCraftingRecipe = true;
            return this;
        }

        /**
         * Add an entry for a fence
         *
         * @param fence           the fence to add
         * @param fenceRecipeName the recipe name for crafting the fence
         * @return this
         */
        public Builder fence(@NotNull Item fence, @Nullable String fenceRecipeName) {
            this.fence = fence;
            this.fenceRecipeName = fenceRecipeName;
            return this;
        }

        /**
         * Add an entry for a fence gate
         *
         * @param fenceGate           the fence gate to add
         * @param fenceGateRecipeName the recipe name for crafting the fence gate
         * @return this
         */
        public Builder fenceGate(@NotNull Item fenceGate, @Nullable String fenceGateRecipeName) {
            this.fenceGate = fenceGate;
            this.fenceGateRecipeName = fenceGateRecipeName;
            return this;
        }

        /**
         * Add an entry for stairs
         *
         * @param stairs           the stairs to add
         * @param stairsRecipeName the recipe name for crafting the stairs
         * @return this
         */
        public Builder stairs(@NotNull Item stairs, @Nullable String stairsRecipeName) {
            this.stairs = stairs;
            this.stairsRecipeName = stairsRecipeName;
            return this;
        }

        /**
         * Add crafting recipe for stairs
         *
         * @return this
         */
        public Builder addStairsRecipe() {
            this.addStairsCraftingRecipe = true;
            return this;
        }

        /**
         * Add an entry for a boat
         *
         * @param boat           the boat to add
         * @param boatRecipeName the recipe name for crafting the boat
         * @return this
         */
        public Builder boat(@NotNull Item boat, @Nullable String boatRecipeName) {
            this.boat = boat;
            this.boatRecipeName = boatRecipeName;
            return this;
        }

        /**
         * Add an entry for a boat with chest
         *
         * @param chestBoat           the boat to add
         * @param chestBoatRecipeName the recipe name for crafting the boat
         * @return this
         */
        public Builder chestBoat(@NotNull Item chestBoat, @Nullable String chestBoatRecipeName) {
            this.chestBoat = chestBoat;
            this.chestBoatRecipeName = chestBoatRecipeName;
            return this;
        }

        /**
         * Add an entry for a sign
         *
         * @param sign           the sign to add
         * @param signRecipeName the recipe name for crafting the sign
         * @return this
         */
        public Builder sign(@NotNull Item sign, @Nullable String signRecipeName) {
            this.sign = sign;
            this.signRecipeName = signRecipeName;
            return this;
        }

        /**
         * Add an entry for a sign
         *
         * @param hangingSign           the hanging sign to add
         * @param hangingSignRecipeName the recipe name for crafting the hanging sign
         * @return this
         */
        public Builder hangingSign(@NotNull Item hangingSign, @Nullable String hangingSignRecipeName) {
            this.hangingSign = hangingSign;
            this.hangingSignRecipeName = hangingSignRecipeName;
            return this;
        }

        /**
         * Add an entry for a sign
         *
         * @param button           the hanging sign to add
         * @param buttonRecipeName the recipe name for crafting the hanging sign
         * @return this
         */
        public Builder button(@NotNull Item button, @Nullable String buttonRecipeName) {
            this.button = button;
            this.buttonRecipeName = buttonRecipeName;
            return this;
        }

        /**
         * Add an entry for a sign
         *
         * @param pressurePlate           the hanging sign to add
         * @param pressurePlateRecipeName the recipe name for crafting the hanging sign
         * @return this
         */
        public Builder pressurePlate(@NotNull Item pressurePlate, @Nullable String pressurePlateRecipeName) {
            this.pressurePlate = pressurePlate;
            this.pressurePlateRecipeName = pressurePlateRecipeName;
            return this;
        }

        /**
         * Specify material for wood entry. If not provided, {@link GTMaterials#Wood} will be used
         *
         * @param material material for wood entry
         * @return this
         */
        public Builder material(@NotNull Material material) {
            this.material = material;
            return this;
        }

        /**
         * Register all possible tags for wood entry.
         *
         * @return this
         */
        public Builder registerAllTags() {
            return registerTag(true, true, true, true, true, true, true, true, true, true, true);
        }

        /**
         * Register all possible unification info for wood entry.
         *
         * @return this
         */
        public Builder registerAllMaterialInfo() {
            return registerMaterialInfo(true, true, true, true, true, true, true, true, true, true);
        }

        /**
         * Register tags for wood entry.
         *
         * @param log           whether to add tags for logs
         * @param planks        whether to add tags for planks
         * @param door          whether to add tags for doors
         * @param slab          whether to add tags for slab
         * @param fence         whether to add tags for fences
         * @param fenceGate     whether to add tags for fence gates
         * @param stairs        whether to add tags for stairs
         * @param boat          whether to add unification info for boats
         * @param chestBoat     whether to add unification info for chest boats
         * @param button        whether to add unification info for buttons
         * @param pressurePlate whether to add unification info for pressure plates
         * @return this
         */
        public Builder registerTag(boolean log, boolean planks, boolean door, boolean slab, boolean fence,
                                   boolean fenceGate, boolean stairs, boolean boat, boolean chestBoat, boolean button,
                                   boolean pressurePlate) {
            this.addLogOreDict = log;
            this.addPlanksOreDict = planks;
            this.addDoorsOreDict = door;
            this.addSlabsOreDict = slab;
            this.addFencesOreDict = fence;
            this.addFenceGatesOreDict = fenceGate;
            this.addStairsOreDict = stairs;
            this.addBoatsOreDict = boat;
            this.addChestBoatsOreDict = chestBoat;
            this.addButtonOreDict = button;
            this.addPressurePlateOreDict = pressurePlate;
            return this;
        }

        /**
         * Register unification info for wood entry.
         *
         * @param planks        whether to add unification info for planks
         * @param door          whether to add unification info for doors
         * @param slab          whether to add unification info for slab
         * @param fence         whether to add unification info for fences
         * @param fenceGate     whether to add unification info for fence gates
         * @param stairs        whether to add unification info for stairs
         * @param boat          whether to add unification info for boats
         * @param chestBoat     whether to add unification info for chest boats
         * @param button        whether to add unification info for buttons
         * @param pressurePlate whether to add unification info for pressure plates
         * @return this
         */
        public Builder registerMaterialInfo(boolean planks, boolean door, boolean slab, boolean fence,
                                            boolean fenceGate, boolean stairs, boolean boat, boolean chestBoat,
                                            boolean button, boolean pressurePlate) {
            this.addPlanksMaterialInfo = planks;
            this.addDoorsMaterialInfo = door;
            this.addSlabsMaterialInfo = slab;
            this.addFencesMaterialInfo = fence;
            this.addFenceGatesMaterialInfo = fenceGate;
            this.addStairsMaterialInfo = stairs;
            this.addBoatsMaterialInfo = boat;
            this.addChestBoatsMaterialInfo = chestBoat;
            this.addButtonMaterialInfo = button;
            this.addPressurePlateMaterialInfo = pressurePlate;
            return this;
        }

        /**
         * Specify if log -> planks recipe should be generated
         *
         * @param enabled if log -> planks recipe should be enabled
         * @return this
         */
        public Builder generateLogToPlankRecipe(boolean enabled) {
            this.generateLogToPlankRecipe = enabled;
            return this;
        }

        /**
         * @return a new wood type entry, if valid
         */
        @NotNull
        public WoodTypeEntry build() {
            Preconditions.checkArgument(planks != null, "Planks cannot be empty.");

            // add default tag if logTag is null
            if (logTag == null)
                logTag = TagUtil.optionalTag(BuiltInRegistries.ITEM,
                        new ResourceLocation(modid, woodName + "_logs"));

            return new WoodTypeEntry(modid, woodName, logTag, log, strippedLog, wood, strippedWood,
                    removeCharcoalRecipe, addCharcoalRecipe,
                    planks, planksRecipeName,
                    door, doorRecipeName,
                    trapdoor, trapdoorRecipeName,
                    slab, slabRecipeName, addSlabsCraftingRecipe,
                    fence, fenceRecipeName, fenceGate, fenceGateRecipeName,
                    stairs, stairsRecipeName, addStairsCraftingRecipe,
                    boat, boatRecipeName, chestBoat, chestBoatRecipeName,
                    sign, signRecipeName, hangingSign, hangingSignRecipeName,
                    button, buttonRecipeName, pressurePlate, pressurePlateRecipeName,
                    material,
                    addLogOreDict, addPlanksOreDict, addDoorsOreDict, addSlabsOreDict,
                    addFencesOreDict, addFenceGatesOreDict, addStairsOreDict, addBoatsOreDict, addChestBoatsOreDict,
                    addButtonOreDict, addPressurePlateOreDict,
                    addPlanksMaterialInfo, addDoorsMaterialInfo, addSlabsMaterialInfo,
                    addFencesMaterialInfo,
                    addFenceGatesMaterialInfo, addStairsMaterialInfo, addBoatsMaterialInfo,
                    addChestBoatsMaterialInfo, addButtonMaterialInfo, addPressurePlateMaterialInfo,
                    generateLogToPlankRecipe);
        }
    }
}
