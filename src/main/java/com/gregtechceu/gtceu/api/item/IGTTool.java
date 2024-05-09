package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.material.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.datacomponents.GTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;
import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.CommonHooks;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.*;
import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_UUID;
import static net.minecraft.world.item.Item.BASE_ATTACK_SPEED_UUID;

@SuppressWarnings("DataFlowIssue")
public interface IGTTool extends IItemUIFactory, ItemLike {

    GTToolType getToolType();
    Material getMaterial();

    boolean isElectric();

    int getElectricTier();

    Tier getTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEntry getSound();

    boolean playSoundOnBlockDestroy();

    @NotNull
    default Item asItem() {
        return (Item) this;
    }

    default ItemStack getRaw() {
        ItemStack stack = new ItemStack(asItem());
        getBehaviorsComponent(stack);
        return stack;
    }

    default ItemStack get() {
        ItemStack stack = new ItemStack(asItem());

        // do NOT set this, to have tools have crafting durability.
        // stack.set(GTDataComponents.DISALLOW_CONTAINER_ITEM, Unit.INSTANCE);

        IGTToolDefinition toolStats = getToolStats();

        // don't show the normal vanilla damage and attack speed tooltips,
        // we handle those ourselves
        stack.update(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(Collections.emptyList(), true), val -> val.withTooltip(false));

        // Grab the definition here because we cannot use getMaxAoEDefinition as it is not initialized yet
        AoESymmetrical aoeDefinition = getToolStats().getAoEDefinition(stack);

        // Set other tool stats (durability)
        ToolProperty toolProperty = this.getMaterial().getProperty(PropertyKey.TOOL);

        // Durability formula we are working with:
        // Final Durability = (material durability * material durability multiplier) + (tool definition durability *
        // definition durability multiplier) - 1
        // Subtracts 1 internally since Minecraft treats "0" as a valid durability, but we don't want to display this.

        int durability = toolProperty.getDurability() * toolProperty.getDurabilityMultiplier();

        // Most Tool Definitions do not set a base durability, which will lead to ignoring the multiplier if present. So
        // apply the multiplier to the material durability if that would happen
        if (toolStats.getBaseDurability(stack) == 0) {
            durability *= (int) toolStats.getDurabilityMultiplier(stack);
        } else {
            durability += (int) (toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack));
        }

        stack.set(DataComponents.MAX_DAMAGE, durability - 1);
        stack.set(DataComponents.DAMAGE, 0);
        if (toolProperty.isUnbreakable()) {
            stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        }

        // Set tool and material enchantments
        Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>(toolProperty.getEnchantments());
        enchantments.putAll(toolStats.getDefaultEnchantments(stack));
        enchantments.forEach((enchantment, level) -> {
            if (enchantment.canEnchant(stack)) {
                stack.enchant(enchantment, level);
            }
        });

        // Set behaviours
        if (toolProperty.isMagnetic()) {
            stack.set(GTDataComponents.RELOCATE_MINED_BLOCKS, Unit.INSTANCE);
        }

        return stack;
    }

    default ItemStack get(long defaultCharge, long defaultMaxCharge) {
        ItemStack stack = get();
        if (isElectric()) {
            ElectricItem electricItem = (ElectricItem) GTCapabilityHelper.getElectricItem(stack);
            if (electricItem != null) {
                electricItem.setMaxChargeOverride(defaultMaxCharge);
                electricItem.setCharge(defaultCharge);
            }
        }
        return stack;
    }

    default ItemStack get(long defaultMaxCharge) {
        return get(defaultMaxCharge, defaultMaxCharge);
    }

    default Material getToolMaterial(ItemStack stack) {
        if (stack.getItem() instanceof IGTTool tool) {
            return tool.getMaterial();
        }

        return GTMaterials.Iron;
    }

    @Nullable
    default ToolProperty getToolProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.TOOL);
    }

    @Nullable
    default DustProperty getDustProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.DUST);
    }

    default float getMaterialToolSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getHarvestSpeed();
    }

    default float getMaterialAttackDamage(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getAttackDamage();
    }

    default float getMaterialAttackSpeed(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0F : toolProperty.getAttackSpeed();
    }

    default int getMaterialDurability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getDurability() * toolProperty.getDurabilityMultiplier();
    }

    default int getMaterialEnchantability(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getEnchantability();
    }

    default int getMaterialHarvestLevel(ItemStack stack) {
        ToolProperty toolProperty = getToolProperty(stack);
        return toolProperty == null ? 0 : toolProperty.getHarvestLevel();
    }

    default long getMaxCharge(ItemStack stack) {
        if (isElectric()) {
            if (stack.has(GTDataComponents.ENERGY_CONTENT)) {
                return stack.get(GTDataComponents.ENERGY_CONTENT).maxCharge();
            }
        }
        return -1L;
    }

    default long getCharge(ItemStack stack) {
        if (isElectric()) {
            if (stack.has(GTDataComponents.ENERGY_CONTENT)) {
                return stack.get(GTDataComponents.ENERGY_CONTENT).charge();
            }
        }
        return -1L;
    }

    default float getTotalToolSpeed(ItemStack stack) {
        if (stack.has(GTDataComponents.GT_TOOL) && stack.get(GTDataComponents.GT_TOOL).toolSpeed().isPresent()) {
            return stack.get(GTDataComponents.GT_TOOL).toolSpeed().get();
        }
        float toolSpeed = getToolStats().getEfficiencyMultiplier(stack) * getMaterialToolSpeed(stack) + getToolStats().getBaseEfficiency(stack);
        stack.update(GTDataComponents.GT_TOOL, new GTTool(), tool -> tool.setToolSpeed(toolSpeed));
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        if (stack.has(GTDataComponents.GT_TOOL) && stack.get(GTDataComponents.GT_TOOL).attackDamage().isPresent()) {
            return stack.get(GTDataComponents.GT_TOOL).attackDamage().get();
        }
        float baseDamage = getToolStats().getBaseDamage(stack);
        final float attackDamage;
        // represents a tool that should always have an attack damage value of 0
        // formatted like this to have attackDamage be final for the lambda.
        if (baseDamage != Float.MIN_VALUE) {
            attackDamage = getMaterialAttackDamage(stack) + baseDamage;
        } else {
            attackDamage = 0;
        }
        stack.update(GTDataComponents.GT_TOOL, new GTTool(), tool -> tool.setAttackDamage(attackDamage));
        return attackDamage;
    }

    default float getTotalAttackSpeed(ItemStack stack) {
        if (stack.has(GTDataComponents.GT_TOOL) && stack.get(GTDataComponents.GT_TOOL).toolSpeed().isPresent()) {
            return stack.get(GTDataComponents.GT_TOOL).toolSpeed().get();
        }
        float attackSpeed = getMaterialAttackSpeed(stack) + getToolStats().getTool().defaultMiningSpeed();
        stack.update(GTDataComponents.GT_TOOL, new GTTool(), tool -> tool.setToolSpeed(attackSpeed));
        return attackSpeed;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        if (stack.has(DataComponents.MAX_DAMAGE)) {
            return stack.get(DataComponents.MAX_DAMAGE);
        }

        IGTToolDefinition toolStats = getToolStats();
        int maxDurability = getMaterialDurability(stack);
        int builderDurability = (int) (toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack));

        // If there is no durability set in the tool builder, multiply the builder AOE multiplier to the material durability
        maxDurability = builderDurability == 0 ? (int) (maxDurability * toolStats.getDurabilityMultiplier(stack)) : maxDurability + builderDurability;
        stack.set(DataComponents.MAX_DAMAGE, maxDurability);
        return maxDurability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        if (stack.has(GTDataComponents.GT_TOOL) && stack.get(GTDataComponents.GT_TOOL).enchantability().isPresent()) {
            return stack.get(GTDataComponents.GT_TOOL).enchantability().get();
        }
        int enchantability = getMaterialEnchantability(stack);
        stack.update(GTDataComponents.GT_TOOL, new GTTool(), tool -> tool.setEnchantability(enchantability));
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        if (stack.has(GTDataComponents.GT_TOOL) && stack.get(GTDataComponents.GT_TOOL).harvestLevel().isPresent()) {
            return stack.get(GTDataComponents.GT_TOOL).harvestLevel().get();
        }
        int harvestLevel = getMaterialHarvestLevel(stack) + getToolStats().getBaseQuality(stack);
        stack.update(GTDataComponents.GT_TOOL, new GTTool(), tool -> tool.setHarvestLevel(harvestLevel));
        return harvestLevel;
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, BlockState state) {
        // special case check (mostly for the sword)
        float specialValue = getDestroySpeed(state, getToolClasses(stack));
        if (specialValue != -1) return specialValue;

        if (isToolEffective(stack, state, getToolClasses(stack), getTotalHarvestLevel(stack))) {
            return getTotalToolSpeed(stack);
        }

        return getToolStats().isToolEffective(state) ? getTotalToolSpeed(stack) : 1.0F;
    }

    default boolean definition$hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        getToolStats().getBehaviors().forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide) return false;
        getBehaviorsComponent(stack).behaviors().forEach(behavior -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isCrouching()) {
            ServerPlayer playerMP = (ServerPlayer) player;
            int result = -1;
            if (isTool(stack, GTToolType.SHEARS)) {
                result = shearBlockRoutine(playerMP, stack, pos);
            }
            if (result != 0) {
                // prevent exploits with instantly breakable blocks
                BlockState state = player.level().getBlockState(pos);
                boolean effective = stack.get(DataComponents.TOOL).isCorrectForDrops(state);

                effective |= isToolEffective(stack, state, getToolClasses(stack), getTotalHarvestLevel(stack));

                if (effective) {
                    if (areaOfEffectBlockBreakRoutine(stack, playerMP)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            if (stack.get(GTDataComponents.TOOL_BEHAVIOURS).hasBehavior(GTToolBehaviors.TREE_FELLING) && state.is(BlockTags.LOGS)) {
                                new TreeFellingHelper().fellTree(stack, player.level(), state, pos, player);
                            }
                            if (playSoundOnBlockDestroy()) playSound(player);
                        } else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    default boolean definition$mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos,
                                         LivingEntity entityLiving) {
        if (!worldIn.isClientSide) {
            getToolStats().getBehaviors()
                    .forEach(behavior -> behavior.onBlockDestroyed(stack, worldIn, state, pos, entityLiving));

            if ((double) state.getDestroySpeed(worldIn, pos) != 0.0D) {
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
            }
            if (entityLiving instanceof Player && playSoundOnBlockDestroy()) {
                // sneaking disables AOE, which means it is okay to play the sound
                // not checking this means the sound will play for every AOE broken block, which is very loud
                if (entityLiving.isCrouching()) {
                    playSound((Player) entityLiving);
                }
            }
        }
        return true;
    }

    default boolean definition$isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        // full durability tools in the left slot are not repairable
        // this is needed so enchantment merging works when both tools are full durability
        if (toRepair.getDamageValue() == 0) return false;
        if (repair.getItem() instanceof IGTTool gtTool) {
            return getToolMaterial(toRepair) == gtTool.getToolMaterial(repair);
        }
        UnificationEntry entry = ChemicalHelper.getUnificationEntry(repair.getItem());
        if (entry == null || entry.material == null) return false;
        if (entry.material == getToolMaterial(toRepair)) {
            // special case wood to allow Wood Planks
            /* TODO Add plank prefix
            if (ModHandler.isMaterialWood(entry.material)) {
                return entry.tagPrefix == TagPrefix.planks;
            }
            */
            // Gems can use gem and plate, Ingots can use ingot and plate
            if (entry.tagPrefix == TagPrefix.plate) {
                return true;
            }
            if (entry.material.hasProperty(PropertyKey.INGOT)) {
                return entry.tagPrefix == TagPrefix.ingot;
            }
            if (entry.material.hasProperty(PropertyKey.GEM)) {
                return entry.tagPrefix == TagPrefix.gem;
            }
        }
        return false;
    }

    default ItemAttributeModifiers definition$getDefaultAttributeModifiers(ItemStack stack) {
        return ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getTotalAttackDamage(stack), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", Math.max(-3.9D, getTotalAttackSpeed(stack)), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    }

    default int definition$getHarvestLevel(ItemStack stack, GTToolType toolClass, @Nullable Player player,
                                           @Nullable BlockState blockState) {
        return getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity,
                                                LivingEntity attacker) {
        return getToolStats().getBehaviors().stream()
                .anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
    }

    default boolean definition$doesSneakBypassUse(@NotNull ItemStack stack, @NotNull BlockGetter world,
                                                  @NotNull BlockPos pos, @NotNull Player player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem() || oldStack.getDamageValue() < newStack.getDamageValue();
    }

    default boolean definition$hasCraftingRemainingItem(ItemStack stack) {
        return !stack.has(GTDataComponents.DISALLOW_CONTAINER_ITEM);
    }

    default ItemStack definition$getCraftingRemainingItem(ItemStack stack) {
        // Sanity-check, callers should really validate with hasContainerItem themselves...
        if (!definition$hasCraftingRemainingItem(stack)) {
            return ItemStack.EMPTY;
        }
        stack = stack.copy();
        Player player = CommonHooks.getCraftingPlayer();
        damageItemWhenCrafting(stack, player);
        playCraftingSound(player, stack);
        // We cannot simply return the copied stack here because Forge's bug
        // Introduced here: https://github.com/MinecraftForge/MinecraftForge/pull/3388
        // Causing PlayerDestroyItemEvent to never be fired under correct circumstances.
        // While preliminarily fixing ItemStack being null in ForgeHooks#getContainerItem in the PR
        // The semantics was misunderstood, any stack that are "broken" (damaged beyond maxDamage)
        // Will be "empty" ItemStacks (while not == ItemStack.EMPTY, but isEmpty() == true)
        // PlayerDestroyItemEvent will not be fired correctly because of this oversight.
        if (stack.isEmpty()) { // Equal to listening to PlayerDestroyItemEvent
            return getToolStats().getBrokenStack();
        }
        return stack;
    }

    default boolean definition$shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
                                                           boolean slotChanged) {
        if (getCharge(oldStack) != getCharge(newStack)) {
            return slotChanged;
        }
        return !oldStack.equals(newStack);
    }

    default boolean definition$onEntitySwing(LivingEntity entityLiving, ItemStack stack) {
        getToolStats().getBehaviors().forEach(behavior -> behavior.onEntitySwing(entityLiving, stack));
        return false;
    }

    default boolean definition$canDestroyBlockInCreative(Level world, BlockPos pos, ItemStack stack, Player player) {
        return true;
    }

    default double definition$getDurabilityForDisplay(ItemStack stack) {
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        if (damage == 0) return 1.0;
        return (double) (maxDamage - damage) / (double) maxDamage;
    }

    default void definition$init() {
        getToolStats().getBehaviors().forEach(behavior -> behavior.init(this));
    }

    default InteractionResult definition$onItemUseFirst(ItemStack stack, UseOnContext context) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUseFirst(stack, context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    default InteractionResult definition$onItemUse(UseOnContext context) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUse(context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> definition$use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // TODO: relocate to keybind action when keybind PR happens
        if (player.isShiftKeyDown() && !getAoEDefinition(stack).isNone()) {
            ItemStack heldItem = player.getItemInHand(hand);
            if (player instanceof ServerPlayer serverPlayer) {
                HeldItemUIFactory.INSTANCE.openUI(serverPlayer, hand);
            }
            return InteractionResultHolder.success(heldItem);
        }


        for (IToolBehavior<?> behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemRightClick(world, player, hand).getResult() == InteractionResult.SUCCESS) {
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    default boolean definition$shouldOpenUIAfterUse(UseOnContext context) {
        for (IToolBehavior<?> behavior : getToolStats().getBehaviors()) {
            if (!behavior.shouldOpenUIAfterUse(context))  {
                return false;
            }
        }

        return true;
    }

    default void definition$fillItemCategory(CreativeModeTab category, @NotNull NonNullList<ItemStack> items) {
        if (isElectric()) {
            items.add(get(Integer.MAX_VALUE));
        } else {
            items.add(get());
        }
    }

    // Client-side methods

    @OnlyIn(Dist.CLIENT)
    default void definition$appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip, TooltipFlag flag) {
        if (!(stack.getItem() instanceof IGTTool tool)) return;

        IGTToolDefinition toolStats = tool.getToolStats();

        // electric info
        if (this.isElectric()) {
            tooltip.add(Component.translatable("metaitem.generic.electric_item.tooltip",
                    getCharge(stack),
                    getMaxCharge(stack),
                    GTValues.VNF[getElectricTier()]));
        }

        // durability info
        if (!stack.has(DataComponents.UNBREAKABLE)) {
            // Plus 1 to match vanilla behavior where tools can still be used once at zero durability. We want to not show this
            int damageRemaining = tool.getTotalMaxDurability(stack) - stack.getDamageValue() + 1;
            if (toolStats.isSuitableForCrafting(stack)) {
                tooltip.add(Component.translatable("item.gtceu.tool.tooltip.crafting_uses", FormattingUtil
                        .formatNumbers(damageRemaining / Math.max(1, toolStats.getToolDamagePerCraft(stack)))));
            }

            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.general_uses",
                    FormattingUtil.formatNumbers(damageRemaining)));
        }

        // attack info
        if (toolStats.isSuitableForAttacking(stack)) {
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.attack_damage",
                    FormattingUtil.formatNumbers(2 + tool.getTotalAttackDamage(stack))));
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.attack_speed",
                    FormattingUtil.formatNumbers(4 + tool.getTotalAttackSpeed(stack))));
        }

        // mining info
        if (toolStats.isSuitableForBlockBreak(stack)) {
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.mining_speed",
                    FormattingUtil.formatNumbers(tool.getTotalToolSpeed(stack))));

            int harvestLevel = tool.getTotalHarvestLevel(stack);
            String harvestName = "item.gtceu.tool.harvest_level." + harvestLevel;
            if (I18n.exists(harvestName)) { // if there's a defined name for the harvest level, use it
                tooltip.add(Component.translatable("item.gtceu.tool.tooltip.harvest_level_extra", harvestLevel,
                        Component.translatable(harvestName)));
            } else {
                tooltip.add(Component.translatable("item.gtceu.tool.tooltip.harvest_level", harvestLevel));
            }
        }

        // behaviors
        boolean addedBehaviorNewLine = false;
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        if (aoeDefinition != AoESymmetrical.none()) {
            addedBehaviorNewLine = tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        if (stack.has(GTDataComponents.RELOCATE_MINED_BLOCKS)) {
            if (!addedBehaviorNewLine) {
                addedBehaviorNewLine = true;
                tooltip.add(Component.literal(""));
            }
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.relocate_mining"));
        }

        if (!addedBehaviorNewLine && !toolStats.getBehaviors().isEmpty()) {
            tooltip.add(Component.literal(""));
        }
        toolStats.getBehaviors().forEach(behavior -> behavior.addInformation(stack, context, tooltip, flag));

        // unique tooltip
        String uniqueTooltip = "item.gtceu.tool." + BuiltInRegistries.ITEM.getKey(this.asItem()).getPath() + ".tooltip";
        if (I18n.exists(uniqueTooltip)) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable(uniqueTooltip));
        }

        tooltip.add(Component.literal(""));

        // valid tools
        tooltip.add(Component.translatable("item.gtceu.tool.usable_as",
                getToolClasses(stack).stream()
                        .map(s -> Component.translatable("gtceu.tool.class." + s.name))
                        .collect(Component::empty, FormattingUtil::combineComponents, FormattingUtil::combineComponents)
        ));

        // repair info
        if (!stack.has(DataComponents.UNBREAKABLE)) {
            if (GTUtil.isShiftDown()) {
                Material material = getToolMaterial(stack);

                Collection<Component> repairItems = new ArrayList<>();
                if (!VanillaRecipeHelper.isMaterialWood(material)) {
                    if (material.hasProperty(PropertyKey.INGOT)) {
                        repairItems.add(TagPrefix.ingot.getLocalizedName(material));
                    } else if (material.hasProperty(PropertyKey.GEM)) {
                        repairItems.add(TagPrefix.gem.getLocalizedName(material));
                    }
                }
                if (!ChemicalHelper.get(TagPrefix.plate, material).isEmpty()) {
                    repairItems.add(TagPrefix.plate.getLocalizedName(material));
                }
                if (!repairItems.isEmpty()) {
                    tooltip.add(Component.translatable("item.gtceu.tool.tooltip.repair_material", repairItems.stream()
                            .collect(Component::empty, FormattingUtil::combineComponents,
                                    FormattingUtil::combineComponents)));
                }
            } else {
                tooltip.add(Component.translatable("item.gtceu.tool.tooltip.repair_info"));
            }
        }
        if (this.isElectric()) {
            tooltip.add(Component.translatable("item.gtceu.tool.replace_tool_head"));
        }
    }

    default boolean definition$canApplyAtEnchantingTable(@NotNull ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) return false;

        // special case enchants from other mods
        switch (enchantment.getDescriptionId()) {
            case "enchantment.cofhcore.smashing":
                // block cofhcore smashing enchant from all tools
                return false;
            case "enchantment.autosmelt": // endercore
            case "enchantment.cofhcore.smelting": // cofhcore
            case "enchantment.as.smelting": // astral sorcery
                // block autosmelt enchants from AoE and Tree-Felling tools
                return getToolStats().getAoEDefinition(stack) == AoESymmetrical.none() && !stack.get(GTDataComponents.TOOL_BEHAVIOURS).hasBehavior(GTToolBehaviors.TREE_FELLING);
        }

        // Block Mending and Unbreaking on Electric tools
        if (isElectric() &&
                (enchantment instanceof MendingEnchantment || enchantment instanceof DigDurabilityEnchantment)) {
            return false;
        }

        // bypass EnumEnchantmentType#canEnchantItem and define custom stack-aware logic.
        // the Minecraft method takes an Item, and does not respect NBT nor meta.
        if (enchantment.getSupportedItems() == ItemTags.MINING_ENCHANTABLE) {
            return getToolStats().isSuitableForBlockBreak(stack);
        } else if (enchantment.getSupportedItems() == ItemTags.WEAPON_ENCHANTABLE) {
            return getToolStats().isSuitableForAttacking(stack);
        } else if (enchantment.getSupportedItems() == ItemTags.DURABILITY_ENCHANTABLE) {
            return !stack.has(DataComponents.UNBREAKABLE);
        }

        ToolProperty property = getToolProperty(stack);
        if (property == null) return false;

        // Check for any special enchantments specified by the material of this Tool
        if (!property.getEnchantments().isEmpty() && property.getEnchantments().containsKey(enchantment)) {
            return true;
        }

        // Check for any additional Enchantment Types added in the builder
        return getToolStats().isEnchantable(stack) && getToolStats().canApplyEnchantment(stack, enchantment);
    }

    @OnlyIn(Dist.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

    // Sound Playing
    default void playCraftingSound(Player player, ItemStack stack) {
        // player null check for things like auto-crafters
        if (ConfigHolder.INSTANCE.client.toolCraftingSounds && getSound() != null && player != null) {
            if (canPlaySound(stack)) {
                setLastCraftingSoundTime(stack);
                playSound(player);
            }
        }
    }

    default void setLastCraftingSoundTime(ItemStack stack) {
        stack.update(GTDataComponents.GT_TOOL, new GTTool(), tool -> tool.setLastCraftingUse((int) System.currentTimeMillis()));
    }

    default boolean canPlaySound(ItemStack stack) {
        return Math.abs((int) System.currentTimeMillis() - stack.get(GTDataComponents.GT_TOOL).lastCraftingUse().orElse(0)) > 1000;
    }

    default void playSound(Player player) {
        if (ConfigHolder.INSTANCE.client.toolUseSounds && getSound() != null) {
            player.level().playSound(null, player.position().x, player.position().y, player.position().z,
                    getSound().getMainEvent(), SoundSource.PLAYERS, 1F, 1F);
        }
    }

    default ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        ItemStack held = holder.getHeld();
        AoESymmetrical definition = getAoEDefinition(held);
        return new ModularUI(120, 80, holder, entityPlayer).background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(6, 10, "item.gtceu.tool.aoe.columns"))
                .widget(new LabelWidget(49, 10, "item.gtceu.tool.aoe.rows"))
                .widget(new LabelWidget(79, 10, "item.gtceu.tool.aoe.layers"))
                .widget(new ButtonWidget(15, 24, 20, 20, new TextTexture("+"), (data) -> {
                    held.set(GTDataComponents.AOE, AoESymmetrical.increaseColumn(definition));
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(15, 44, 20, 20, new TextTexture("-"), (data) -> {
                    held.set(GTDataComponents.AOE, AoESymmetrical.decreaseColumn(definition));
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(50, 24, 20, 20, new TextTexture("+"), (data) -> {
                    held.set(GTDataComponents.AOE, AoESymmetrical.increaseRow(definition));
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(50, 44, 20, 20, new TextTexture("-"), (data) -> {
                    held.set(GTDataComponents.AOE, AoESymmetrical.decreaseRow(definition));
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(85, 24, 20, 20, new TextTexture("+"), (data) -> {
                    held.set(GTDataComponents.AOE, AoESymmetrical.increaseLayer(definition));
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(85, 44, 20, 20, new TextTexture("-"), (data) -> {
                    held.set(GTDataComponents.AOE, AoESymmetrical.decreaseLayer(definition));
                    holder.markAsDirty();
                }))
                .widget(new LabelWidget(23, 65, () ->
                        Integer.toString(1 + 2 * held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.none()).getColumn())))
                .widget(new LabelWidget(58, 65, () ->
                        Integer.toString(1 + 2 * held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.none()).getRow())))
                .widget(new LabelWidget(93, 65, () ->
                        Integer.toString(1 + held.getOrDefault(GTDataComponents.AOE, AoESymmetrical.none()).getLayer())));
    }

    default Set<GTToolType> getToolClasses(ItemStack stack) {
        return new HashSet<>(getToolType().toolClasses);
    }

    default void attachCapabilities(RegisterCapabilitiesEvent event) {
        for (IToolBehavior<?> behavior : getToolStats().getBehaviors()) {
            if (behavior instanceof IComponentCapability componentCapability) {
               componentCapability.attachCaps(event, this.asItem());
            }
        }
        if (this.isElectric()) {
            ElectricStats item = ElectricStats.createElectricItem(0L, getElectricTier());
            item.attachCaps(event, this.asItem());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static ItemColor tintColor() {
        return (itemStack, index) -> {
            if (itemStack.getItem() instanceof IGTTool item) {
                Material material = item.getMaterial();
                // TODO switch around main and secondary color once new textures are added
                return switch (index) {
                    case 0, -101 -> {
                        if (item.getToolClasses(itemStack).contains(GTToolType.CROWBAR)) {
                            if (itemStack.has(DataComponents.DYED_COLOR)) {
                                yield itemStack.get(DataComponents.DYED_COLOR).rgb();
                            }
                        }
                        yield -1;
                    }
                    case 1, -111 -> material.getMaterialARGB();
                    case 2, -121 -> {
                        if (material.getMaterialSecondaryARGB() != -1) {
                            yield material.getMaterialSecondaryARGB();
                        } else {
                            yield material.getMaterialARGB();
                        }
                    }
                    default -> -1;
                };
            }
            return -1;
        };
    }
}
