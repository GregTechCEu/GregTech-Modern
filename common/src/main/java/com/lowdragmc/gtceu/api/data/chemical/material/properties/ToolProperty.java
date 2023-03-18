package com.lowdragmc.gtceu.api.data.chemical.material.properties;

import com.lowdragmc.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.gtceu.api.item.tool.MaterialToolTier;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public class ToolProperty implements IMaterialProperty<ToolProperty> {

    /**
     * Harvest speed of tools made from this Material.
     * <p>
     * Default: 1.0F
     */
    @Getter
    private float harvestSpeed;

    /**
     * Attack damage of tools made from this Material
     * <p>
     * Default: 1.0F
     */
    @Getter
    private float attackDamage;

    /**
     * Attack speed of tools made from this Material
     * <p>
     * Default: 0.0F
     */
    @Getter
    private float attackSpeed;

    /**
     * Durability of tools made from this Material.
     * <p>
     * Default: 100
     */
    @Getter
    private int durability;

    /**
     * Harvest level of tools made of this Material.
     * <p>
     * Default: 2 (Iron).
     */
    @Getter
    private int harvestLevel;

    /**
     * Enchantability of tools made from this Material.
     * <p>
     * Default: 10
     */
    @Getter
    private int enchantability = 10;

    /**
     * If crafting tools should not be made from this material
     */
    @Getter
    private boolean ignoreCraftingTools;

    /**
     * If tools made of this material should be unbreakable and ignore durability checks.
     */
    @Getter
    private boolean isUnbreakable;

    /**
     * If tools made of this material should be "magnetic," meaning items go
     * directly into the player's inventory instead of dropping on the ground.
     */
    @Getter
    private boolean isMagnetic;

    /**
     * A multiplier to the base durability for this material
     * Mostly for modpack makers
     */
    @Getter
    private int durabilityMultiplier = 1;

    private MaterialToolTier toolTier;

    /**
     * Enchantment to be applied to tools made from this Material.
     */
    private final Object2IntMap<Enchantment> enchantments = new Object2IntArrayMap<>();

    public ToolProperty(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
        this.harvestSpeed = harvestSpeed;
        this.attackDamage = attackDamage;
        this.durability = durability;
        this.harvestLevel = harvestLevel;
    }

    public ToolProperty() {
        this(1.0F, 1.0F, 100, 2);
    }

    public Object2IntMap<Enchantment> getEnchantments() {
        return enchantments;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.GEM)) properties.ensureSet(PropertyKey.INGOT, true);
    }

    public void addEnchantmentForTools(Enchantment enchantment, int level) {
        enchantments.put(enchantment, level);
    }

    public MaterialToolTier getTier(Material material) {
        if (toolTier == null) {
            toolTier = new MaterialToolTier(material);
        }
        return toolTier;
    }

    public static class Builder {

        private final ToolProperty toolProperty;

        public static Builder of(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            return new Builder(harvestSpeed, attackDamage, durability, harvestLevel);
        }

        private Builder(float harvestSpeed, float attackDamage, int durability, int harvestLevel) {
            toolProperty = new ToolProperty(harvestSpeed, attackDamage, durability, harvestLevel);
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

        public Builder enchantment(Enchantment enchantment, int level) {
            toolProperty.addEnchantmentForTools(enchantment, level);
            return this;
        }

        public Builder magnetic() {
            toolProperty.isMagnetic = true;
            return this;
        }

        public Builder durabilityMultiplier(int multiplier) {
            toolProperty.durability = multiplier;
            return this;
        }

        public ToolProperty build() {
            return toolProperty;
        }
    }
}
