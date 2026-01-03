package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.MaterialToolTier;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.item.enchantment.Enchantment;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import static com.gregtechceu.gtceu.api.item.tool.GTToolType.*;

public class ToolProperty implements IMaterialProperty {

    /**
     * Harvest speed of tools made from this Material.
     * <p>
     * Default: 1.0F
     */
    @Getter
    @Setter
    private float harvestSpeed;

    /**
     * Attack damage of tools made from this Material
     * <p>
     * Default: 1.0F
     */
    @Getter
    @Setter
    private float attackDamage;

    /**
     * Attack speed of tools made from this Material
     * <p>
     * Default: 0.0F
     */
    @Getter
    @Setter
    private float attackSpeed;

    /**
     * Durability of tools made from this Material.
     * <p>
     * Default: 100
     */
    @Getter
    @Setter
    private int durability;

    /**
     * Harvest level of tools made of this Material.
     * <p>
     * Default: 2 (Iron).
     */
    @Getter
    @Setter
    private int harvestLevel;

    @Getter
    @Setter
    private int prospectingDepth;

    /**
     * Enchantability of tools made from this Material.
     * <p>
     * Default: 10
     */
    @Getter
    @Setter
    private int enchantability = 10;

    /**
     * If crafting tools should not be made from this material
     */
    @Getter
    @Setter
    private boolean ignoreCraftingTools;

    /**
     * If tools made of this material should be unbreakable and ignore durability checks.
     */
    @Getter
    @Setter
    private boolean isUnbreakable;

    /**
     * If tools made of this material should be "magnetic," meaning items go
     * directly into the player's inventory instead of dropping on the ground.
     */
    @Getter
    @Setter
    private boolean isMagnetic;

    /**
     * A multiplier to the base durability for this material
     * Mostly for modpack makers
     */
    @Getter
    @Setter
    private int durabilityMultiplier = 1;

    private MaterialToolTier toolTier;

    /**
     * Gen for given type
     */
    @Getter
    @Setter
    private GTToolType[] types;

    /**
     * Enchantment to be applied to tools made from this Material.
     */
    @Getter
    private final Object2IntMap<Enchantment> enchantments = new Object2IntArrayMap<>();

    public ToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types) {
        this.harvestSpeed = harvestSpeed;
        this.attackDamage = attackDamage;
        this.durability = durability;
        this.harvestLevel = harvestLevel;
        this.types = types;
        this.prospectingDepth = this.harvestLevel * 2 + 1;
    }

    public ToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel, int prospectingDepth,
                        GTToolType[] types) {
        this(harvestSpeed, attackDamage, durability, harvestLevel, types);
        this.prospectingDepth = prospectingDepth;
    }

    public ToolProperty() {
        this(1.0F, 1.0F, 100, 2, GTToolType.getTypes().values().toArray(GTToolType[]::new));
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            if (!properties.hasProperty(PropertyKey.GEM)) properties.ensureSet(PropertyKey.INGOT, true);
        }
    }

    public void addEnchantmentForTools(Enchantment enchantment, int level) {
        if (ConfigHolder.INSTANCE.recipes.enchantedTools) {
            enchantments.put(enchantment, level);
        }
    }

    public MaterialToolTier getTier(Material material) {
        if (toolTier == null) {
            toolTier = new MaterialToolTier(material);
        }
        return toolTier;
    }

    public boolean hasType(GTToolType toolType) {
        return ArrayUtils.contains(types, toolType);
    }

    public ToolProperty addTypes(GTToolType... types) {
        this.types = ArrayUtils.addAll(this.types, types);
        return this;
    }

    public ToolProperty removeTypes(GTToolType... types) {
        this.types = Arrays.stream(this.types).filter(type -> !ArrayUtils.contains(types, type))
                .toArray(GTToolType[]::new);
        return this;
    }

    @SuppressWarnings("unused") // API, need to treat all of these as used
    public static class Builder {

        private final ToolProperty toolProperty;

        /**
         * Create Tools for this Material.
         *
         * @param harvestSpeed The mining speed of a tool made from this Material.
         * @param attackDamage The attack damage of a tool made from this Material.
         * @param durability   The durability of a tool made from this Material.
         * @param harvestLevel The harvest level that tools of this Material can mine.
         */
        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel, new GTToolType[] {
                    SWORD,
                    PICKAXE,
                    SHOVEL,
                    AXE,
                    HOE,
                    MINING_HAMMER,
                    SPADE,
                    SAW,
                    HARD_HAMMER,
                    // SOFT_MALLET,
                    WRENCH,
                    FILE,
                    CROWBAR,
                    SCREWDRIVER,
                    // MORTAR,
                    WIRE_CUTTER,
                    SCYTHE,
                    KNIFE,
                    BUTCHERY_KNIFE,
                    // PLUNGER,
                    DRILL_LV,
                    DRILL_MV,
                    DRILL_HV,
                    DRILL_EV,
                    DRILL_IV,
                    CHAINSAW_LV,
                    WRENCH_LV,
                    WRENCH_HV,
                    WRENCH_IV,
                    BUZZSAW,
                    SCREWDRIVER_LV,
                    WIRE_CUTTER_LV,
                    WIRE_CUTTER_HV,
                    WIRE_CUTTER_IV,
            });
        }

        /**
         * Create Tools for this Material.
         *
         * @param harvestSpeed The mining speed of a tool made from this Material.
         * @param attackDamage The attack damage of a tool made from this Material.
         * @param durability   The durability of a tool made from this Material.
         * @param harvestLevel The harvest level that tools of this Material can mine.
         * @param types        The types of tools that can be made of this Material.
         */
        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                                 GTToolType... types) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel, types);
        }

        private Builder(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types) {
            toolProperty = new ToolProperty(harvestSpeed, attackDamage, durability, harvestLevel, types);
        }

        /**
         * Set the base enchantability of a tool made from this Material. Iron is 14, Diamond is 10, Stone is 5.
         */
        public Builder enchantability(int enchantability) {
            toolProperty.enchantability = enchantability;
            return this;
        }

        /**
         * Set the attack speed of a tool made from this Material (animation time).
         */
        public Builder attackSpeed(float attackSpeed) {
            toolProperty.attackSpeed = attackSpeed;
            return this;
        }

        /**
         * Disable crafting tools being made from this Material.
         */
        public Builder ignoreCraftingTools() {
            toolProperty.ignoreCraftingTools = true;
            return this;
        }

        /**
         * Set tools made from this Material as unbreakable, bypassing all durability.
         */
        public Builder unbreakable() {
            toolProperty.isUnbreakable = true;
            return this;
        }

        /**
         * Set the types of tools that can be made of this Material.
         */
        public Builder types(GTToolType... types) {
            toolProperty.types = types;
            return this;
        }

        /**
         * Add additional types of tools that can be made of this Material.
         */
        public Builder addTypes(GTToolType... types) {
            toolProperty.types = ArrayUtils.addAll(toolProperty.types, types);
            return this;
        }

        /**
         * Add a default enchantment to tools made of this Material.
         * 
         * @param enchantment The default enchantment, applied on crafting the tool.
         * @param level       The level of the enchantment.
         */
        public Builder enchantment(Enchantment enchantment, int level) {
            toolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        /**
         * Set tools made from this Material as magnetic, pulling mined blocks into your inventory.
         */
        public Builder magnetic() {
            toolProperty.isMagnetic = true;
            return this;
        }

        /**
         * Set a multiplier to the base durability of tools made from this Material.
         */
        public Builder durabilityMultiplier(int multiplier) {
            toolProperty.durabilityMultiplier = multiplier;
            return this;
        }

        public ToolProperty build() {
            return toolProperty;
        }
    }
}
