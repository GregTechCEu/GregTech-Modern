package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.function.*;

@SuppressWarnings("unused")
@Accessors(fluent = true, chain = true)
public class ToolDefinitionBuilder {

    private final List<IToolBehavior<?>> behaviours = new ArrayList<>();
    @Setter
    private Tool tool = new Tool(Collections.emptyList(), 1.0F, 1, true);
    @Setter
    private int damagePerCraftingAction = 1;
    private boolean suitableForBlockBreaking = false;
    private boolean suitableForAttacking = false;
    private boolean suitableForCrafting = false;
    @Setter
    private int baseDurability = 0;
    @Setter
    private float durabilityMultiplier = 1.0f;
    @Setter
    private int baseQuality = 0;
    @Setter
    private float attackDamage = 0F;
    @Setter
    private float baseEfficiency = 4F;
    @Setter
    private float efficiencyMultiplier = 1.0F;
    @Setter
    private float attackSpeed = 0.0F;
    private boolean isEnchantable;
    private List<TagKey<Item>> validEnchantmentTags = Collections.emptyList();
    private boolean sneakBypassUse = false;
    @Setter
    private Supplier<ItemStack> brokenStack = () -> ItemStack.EMPTY;
    @Setter
    private AoESymmetrical aoe = AoESymmetrical.ZERO;
    private final Object2IntMap<ResourceKey<Enchantment>> defaultEnchantments = new Object2IntArrayMap<>();

    public ToolDefinitionBuilder behaviors(IToolBehavior<?>... behaviours) {
        Collections.addAll(this.behaviours, behaviours);
        return this;
    }

    public ToolDefinitionBuilder blockBreaking() {
        this.suitableForBlockBreaking = true;
        return this;
    }

    public ToolDefinitionBuilder attacking() {
        this.suitableForAttacking = true;
        return this;
    }

    public ToolDefinitionBuilder crafting() {
        this.suitableForCrafting = true;
        return this;
    }

    public ToolDefinitionBuilder baseQuality() {
        return baseQuality(0);
    }

    /**
     * Sets the attack to the lowest possible value.
     * Attack in-game will always result in 0 no matter the
     * material stats, which MC will not see as a valid weapon.
     */
    public ToolDefinitionBuilder cannotAttack() {
        this.attackDamage = Float.MIN_VALUE;
        return this;
    }

    public ToolDefinitionBuilder noEnchant() {
        this.isEnchantable = false;
        return this;
    }

    @SafeVarargs
    public final ToolDefinitionBuilder validEnchantmentTags(TagKey<Item>... enchantmentTypes) {
        this.isEnchantable = true;
        this.validEnchantmentTags = Arrays.asList(enchantmentTypes);
        return this;
    }

    public ToolDefinitionBuilder sneakBypassUse() {
        this.sneakBypassUse = true;
        return this;
    }

    public ToolDefinitionBuilder aoe(int additionalColumns, int additionalRows, int additionalDepth) {
        return aoe(AoESymmetrical.of(additionalColumns, additionalRows, additionalDepth));
    }

    public ToolDefinitionBuilder defaultEnchantment(ResourceKey<Enchantment> enchantment, int level) {
        if (ConfigHolder.INSTANCE.recipes.enchantedTools) {
            this.defaultEnchantments.put(enchantment, level);
        }
        return this;
    }

    public IGTToolDefinition build() {
        return new IGTToolDefinition() {

            private final List<IToolBehavior<?>> behaviors = ImmutableList
                    .copyOf(ToolDefinitionBuilder.this.behaviours);
            private final Tool tool = ToolDefinitionBuilder.this.tool;
            private final int damagePerCraftingAction = ToolDefinitionBuilder.this.damagePerCraftingAction;
            private final boolean suitableForBlockBreaking = ToolDefinitionBuilder.this.suitableForBlockBreaking;
            private final boolean suitableForAttacking = ToolDefinitionBuilder.this.suitableForAttacking;
            private final boolean suitableForCrafting = ToolDefinitionBuilder.this.suitableForCrafting;
            private final int baseDurability = ToolDefinitionBuilder.this.baseDurability;
            private final float durabilityMultiplier = ToolDefinitionBuilder.this.durabilityMultiplier;
            private final int baseQuality = ToolDefinitionBuilder.this.baseQuality;
            private final float attackDamage = ToolDefinitionBuilder.this.attackDamage;
            private final float baseEfficiency = ToolDefinitionBuilder.this.baseEfficiency;
            private final float efficiencyMultiplier = ToolDefinitionBuilder.this.efficiencyMultiplier;
            private final float attackSpeed = ToolDefinitionBuilder.this.attackSpeed;
            private final boolean isEnchantable = ToolDefinitionBuilder.this.isEnchantable;
            private final List<TagKey<Item>> validEnchantmentTags = ToolDefinitionBuilder.this.validEnchantmentTags;
            private final boolean sneakBypassUse = ToolDefinitionBuilder.this.sneakBypassUse;
            private final Supplier<ItemStack> brokenStack = ToolDefinitionBuilder.this.brokenStack;
            private final AoESymmetrical aoeSymmetrical = ToolDefinitionBuilder.this.aoe;
            private final Object2IntMap<ResourceKey<Enchantment>> defaultEnchantments = ToolDefinitionBuilder.this.defaultEnchantments;

            @Override
            public List<IToolBehavior<?>> getBehaviors() {
                return behaviors;
            }

            @Override
            public Tool getTool() {
                return tool;
            }

            @Override
            public boolean isToolEffective(BlockState state) {
                return tool.isCorrectForDrops(state);
            }

            @Override
            public int getDamagePerCraftingAction(ItemStack stack) {
                return damagePerCraftingAction;
            }

            @Override
            public int getDamagePerAction(ItemStack stack) {
                return tool.damagePerBlock();
            }

            @Override
            public boolean isSuitableForBlockBreak(ItemStack stack) {
                return suitableForBlockBreaking;
            }

            @Override
            public boolean isSuitableForAttacking(ItemStack stack) {
                return suitableForAttacking;
            }

            @Override
            public boolean isSuitableForCrafting(ItemStack stack) {
                return suitableForCrafting;
            }

            @Override
            public int getBaseDurability() {
                return baseDurability;
            }

            @Override
            public float getDurabilityMultiplier() {
                return durabilityMultiplier;
            }

            @Override
            public int getBaseQuality() {
                return baseQuality;
            }

            @Override
            public float getBaseDamage() {
                return attackDamage;
            }

            @Override
            public float getBaseEfficiency() {
                return baseEfficiency;
            }

            @Override
            public float getEfficiencyMultiplier() {
                return efficiencyMultiplier;
            }

            @Override
            public float getAttackSpeed() {
                return attackSpeed;
            }

            @Override
            public boolean isEnchantable(ItemStack stack) {
                return isEnchantable;
            }

            @Override
            public List<TagKey<Item>> getValidEnchantmentTags() {
                return validEnchantmentTags;
            }

            @Override
            public Object2IntMap<ResourceKey<Enchantment>> getDefaultEnchantments() {
                return Object2IntMaps.unmodifiable(this.defaultEnchantments);
            }

            @Override
            public boolean doesSneakBypassUse() {
                return sneakBypassUse;
            }

            @Override
            public ItemStack getBrokenStack() {
                return brokenStack.get();
            }

            @Override
            public AoESymmetrical getAoEDefinition() {
                return aoeSymmetrical;
            }
        };
    }
}
