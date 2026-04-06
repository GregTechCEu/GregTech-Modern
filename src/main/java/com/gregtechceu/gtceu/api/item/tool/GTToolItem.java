package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GTToolItem extends TieredItem implements IGTTool {

    @Getter
    protected final GTToolType toolType;
    @Getter
    protected final int electricTier;
    @Getter
    protected final Material material;
    @Getter
    private final IGTToolDefinition toolStats;

    public GTToolItem(GTToolType toolType, MaterialToolTier tier, Material material, IGTToolDefinition definition,
                      Properties properties) {
        super(tier, material.hasFlag(MaterialFlags.FIRE_RESISTANT) ? properties.fireResistant() : properties);
        this.toolType = toolType;
        this.material = material;
        this.electricTier = toolType.electricTier;
        this.toolStats = definition;
        if (GTCEu.isClientSide()) {
            ToolItemRenderer.create(this, toolType);
        }
        definition$init();
    }

    public static GTToolItem create(GTToolType toolType, MaterialToolTier tier, Material material,
                                    IGTToolDefinition definition, Properties properties) {
        return new GTToolItem(toolType, tier, material, definition, properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return get();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return definition$canPerformAction(stack, action);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        return definition$onItemUseFirst(itemStack, context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return definition$onItemUse(context);
    }

    @Override
    public String getDescriptionId() {
        return toolType.getUnlocalizedName();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(toolType.getUnlocalizedName(), material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return definition$mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public boolean isElectric() {
        return electricTier > -1;
    }

    @Nullable
    @Override
    public SoundEntry getSound() {
        return toolType.soundEntry;
    }

    @Override
    public boolean playSoundOnBlockDestroy() {
        return toolType.playSoundOnBlockDestroy;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return definition$hurtEnemy(stack, target, attacker);
    }

    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        return definition$onBlockStartBreak(stack, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        definition$appendHoverText(stack, context, tooltipComponents, isAdvanced);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return definition$isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        return definition$getAllEnchantments(stack, lookup);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return definition$getEnchantmentLevel(stack, enchantment);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return definition$canDisableShield(shield, shield, entity, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return definition$doesSneakBypassUse(stack, level, pos, player);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return definition$shouldCauseBlockBreakReset(oldStack, newStack);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return definition$hasCraftingRemainingItem(stack);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return definition$getCraftingRemainingItem(itemStack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return definition$shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return definition$use(level, player, usedHand);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return this.definition$isCorrectToolForDrops(stack, state);
    }
}
