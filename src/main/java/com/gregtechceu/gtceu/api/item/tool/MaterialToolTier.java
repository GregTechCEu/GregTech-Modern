package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote MaterialTier
 */
public class MaterialToolTier implements Tier {
    public final Material material;

    public final ToolProperty property;

    public MaterialToolTier(Material material) {
        this.material = material;
        if (!material.hasProperty(PropertyKey.TOOL)) {
            throw  new IllegalArgumentException("material %s hasn't got Tool Property".formatted(material));
        }
        this.property = material.getProperty(PropertyKey.TOOL);
    }

    @Override
    public int getUses() {
        return property.getDurability() * property.getDurabilityMultiplier();
    }

    @Override
    public float getSpeed() {
        return property.getHarvestSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return property.getAttackDamage();
    }

    @Override
    public int getLevel() {
        return property.getHarvestLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return property.getEnchantability();
    }

    @Override
    @Nonnull
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
