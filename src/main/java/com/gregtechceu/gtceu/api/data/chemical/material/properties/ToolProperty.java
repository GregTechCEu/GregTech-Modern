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
    private final Object2IntMap<Enchantment> enchantments = new Object2IntArrayMap<>();

    public ToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types) {
        this.harvestSpeed = harvestSpeed;
        this.attackDamage = attackDamage;
        this.durability = durability;
        this.harvestLevel = harvestLevel;
        this.types = types;
    }

    public ToolProperty() {
        this(1.0F, 1.0F, 100, 2, GTToolType.getTypes().values().toArray(GTToolType[]::new));
    }

    public Object2IntMap<Enchantment> getEnchantments() {
        return enchantments;
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

    public static class Builder {

        private final ToolProperty toolProperty;

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

        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel,
                                 GTToolType... types) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel, types);
        }

        private Builder(float harvestSpeed, float attackDamage, int durability, int harvestLevel, GTToolType[] types) {
            toolProperty = new ToolProperty(harvestSpeed, attackDamage, durability, harvestLevel, types);
        }

        public Builder enchantability(int enchantability) {
            toolProperty.enchantability = enchantability;
            return this;
        }

        public Builder attackSpeed(float attackSpeed) {
            toolProperty.attackSpeed = attackSpeed;
            return this;
        }

        public Builder ignoreCraftingTools() {
            toolProperty.ignoreCraftingTools = true;
            return this;
        }

        public Builder unbreakable() {
            toolProperty.isUnbreakable = true;
            return this;
        }

        public Builder types(GTToolType... types) {
            toolProperty.types = types;
            return this;
        }

        public Builder addTypes(GTToolType... types) {
            toolProperty.types = ArrayUtils.addAll(toolProperty.types, types);
            return this;
        }

        public Builder enchantment(Enchantment enchantment, int level) {
            toolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        public Builder magnetic() {
            toolProperty.isMagnetic = true;
            return this;
        }

        public Builder durabilityMultiplier(int multiplier) {
            toolProperty.durabilityMultiplier = multiplier;
            return this;
        }

        public ToolProperty build() {
            return toolProperty;
        }
    }
}
