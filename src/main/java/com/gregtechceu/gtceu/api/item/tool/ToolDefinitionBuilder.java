package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("unused")
@Accessors(fluent = true, chain = true)
public class ToolDefinitionBuilder {

    private final List<IToolBehavior> behaviours = new ArrayList<>();
    @Setter
    private int damagePerAction = 1;
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
    private boolean isEnchantable;
    private BiPredicate<ItemStack, Enchantment> canApplyEnchantment;
    @Setter
    private float attackSpeed = 0F;
    private boolean sneakBypassUse = false;
    @Setter
    private Supplier<ItemStack> brokenStack = () -> ItemStack.EMPTY;
    @Setter
    private AoESymmetrical aoe = AoESymmetrical.ZERO;
    private final Set<Block> effectiveBlocks = new ObjectOpenHashSet<>();
    private Predicate<BlockState> effectiveStates;
    private final Object2IntMap<Enchantment> defaultEnchantments = new Object2IntArrayMap<>();

    public ToolDefinitionBuilder behaviors(IToolBehavior... behaviours) {
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

    public ToolDefinitionBuilder canApplyEnchantment(BiPredicate<ItemStack, Enchantment> canApplyEnchantment) {
        this.isEnchantable = true;
        this.canApplyEnchantment = canApplyEnchantment;
        return this;
    }

    public ToolDefinitionBuilder canApplyEnchantment(EnchantmentCategory... enchantmentTypes) {
        this.isEnchantable = true;
        this.canApplyEnchantment = (stack, enchantment) -> {
            for (EnchantmentCategory type : enchantmentTypes) {
                if (enchantment.category == type) {
                    return true;
                }
            }
            return false;
        };
        return this;
    }

    public ToolDefinitionBuilder sneakBypassUse() {
        this.sneakBypassUse = true;
        return this;
    }

    public ToolDefinitionBuilder aoe(int additionalColumns, int additionalRows, int additionalDepth) {
        return aoe(AoESymmetrical.of(additionalColumns, additionalRows, additionalDepth));
    }

    public ToolDefinitionBuilder effectiveBlocks(Block... blocks) {
        Collections.addAll(this.effectiveBlocks, blocks);
        return this;
    }

    public ToolDefinitionBuilder effectiveStates(Predicate<BlockState> effectiveStates) {
        this.effectiveStates = effectiveStates;
        return this;
    }

    public ToolDefinitionBuilder defaultEnchantment(Enchantment enchantment, int level) {
        if (ConfigHolder.INSTANCE.recipes.enchantedTools) {
            this.defaultEnchantments.put(enchantment, level);
        }

        return this;
    }

    public IGTToolDefinition build() {
        return new IGTToolDefinition() {

            private final List<IToolBehavior> behaviors = ImmutableList.copyOf(ToolDefinitionBuilder.this.behaviours);
            private final int damagePerAction = ToolDefinitionBuilder.this.damagePerAction;
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
            private final boolean isEnchantable = ToolDefinitionBuilder.this.isEnchantable;
            private final BiPredicate<ItemStack, Enchantment> canApplyEnchantment = ToolDefinitionBuilder.this.canApplyEnchantment;
            private final float attackSpeed = ToolDefinitionBuilder.this.attackSpeed;
            private final boolean sneakBypassUse = ToolDefinitionBuilder.this.sneakBypassUse;
            private final Supplier<ItemStack> brokenStack = ToolDefinitionBuilder.this.brokenStack;
            private final AoESymmetrical aoeSymmetrical = ToolDefinitionBuilder.this.aoe;
            private final Predicate<BlockState> effectiveStatePredicate;
            private final Object2IntMap<Enchantment> defaultEnchantments = ToolDefinitionBuilder.this.defaultEnchantments;

            {
                Set<Block> effectiveBlocks = ToolDefinitionBuilder.this.effectiveBlocks;
                Predicate<BlockState> effectiveStates = ToolDefinitionBuilder.this.effectiveStates;
                Predicate<BlockState> effectiveStatePredicate = null;
                if (!effectiveBlocks.isEmpty()) {
                    effectiveStatePredicate = state -> effectiveBlocks.contains(state.getBlock());
                }
                if (effectiveStates != null) {
                    effectiveStatePredicate = effectiveStatePredicate == null ? effectiveStates :
                            effectiveStatePredicate.or(effectiveStates);
                }
                this.effectiveStatePredicate = effectiveStatePredicate == null ? state -> false :
                        effectiveStatePredicate;
            }

            @Override
            public List<IToolBehavior> getBehaviors() {
                return behaviors;
            }

            @Override
            public boolean isToolEffective(BlockState state) {
                return effectiveStatePredicate.test(state);
            }

            @Override
            public int getDamagePerCraftingAction(ItemStack stack) {
                return damagePerCraftingAction;
            }

            @Override
            public int getDamagePerAction(ItemStack stack) {
                return damagePerAction;
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
            public int getBaseDurability(ItemStack stack) {
                return baseDurability;
            }

            @Override
            public float getDurabilityMultiplier(ItemStack stack) {
                return durabilityMultiplier;
            }

            @Override
            public int getBaseQuality(ItemStack stack) {
                return baseQuality;
            }

            @Override
            public float getBaseDamage(ItemStack stack) {
                return attackDamage;
            }

            @Override
            public float getBaseEfficiency(ItemStack stack) {
                return baseEfficiency;
            }

            @Override
            public float getEfficiencyMultiplier(ItemStack stack) {
                return efficiencyMultiplier;
            }

            @Override
            public boolean isEnchantable(ItemStack stack) {
                return isEnchantable;
            }

            @Override
            public boolean canApplyEnchantment(ItemStack stack, Enchantment enchantment) {
                return canApplyEnchantment.test(stack, enchantment);
            }

            @Override
            public Object2IntMap<Enchantment> getDefaultEnchantments(ItemStack stack) {
                return Object2IntMaps.unmodifiable(this.defaultEnchantments);
            }

            @Override
            public float getAttackSpeed(ItemStack stack) {
                return attackSpeed;
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
            public AoESymmetrical getAoEDefinition(ItemStack stack) {
                return aoeSymmetrical;
            }
        };
    }
}
