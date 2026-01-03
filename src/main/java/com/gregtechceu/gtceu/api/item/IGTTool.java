package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.CombinedCapabilityProvider;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolUIBehavior;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.Util;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.common.util.LazyOptional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.*;
import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_UUID;
import static net.minecraft.world.item.Item.BASE_ATTACK_SPEED_UUID;

public interface IGTTool extends HeldItemUIFactory.IHeldItemUIHolder, ItemLike, IForgeItem {

    GTToolType getToolType();

    Material getMaterial();

    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEntry getSound();

    boolean playSoundOnBlockDestroy();

    default Item asItem() {
        return (Item) this;
    }

    default ItemStack getRaw() {
        ItemStack stack = new ItemStack(asItem());
        getBehaviorsTag(stack);
        return stack;
    }

    default ItemStack get() {
        ItemStack stack = new ItemStack(asItem());

        CompoundTag stackCompound = stack.getOrCreateTag();
        stackCompound.putBoolean(DISALLOW_CONTAINER_ITEM_KEY, false);

        CompoundTag toolTag = getToolTag(stack);
        IGTToolDefinition toolStats = getToolStats();

        // don't show the normal vanilla damage and attack speed tooltips,
        // we handle those ourselves
        stackCompound.putInt(HIDE_FLAGS, 2);

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
            durability *= toolStats.getDurabilityMultiplier(stack);
        } else {
            durability += toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack);
        }

        toolTag.putInt(MAX_DURABILITY_KEY, durability - 1);
        if (toolProperty.isUnbreakable()) {
            stackCompound.putBoolean(UNBREAKABLE_KEY, true);
        }

        // Set behaviours
        CompoundTag behaviourTag = getBehaviorsTag(stack);
        getToolStats().getBehaviors().forEach(behavior -> behavior.addBehaviorNBT(stack, behaviourTag));

        if (!aoeDefinition.isZero()) {
            behaviourTag.putInt(MAX_AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.putInt(MAX_AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.putInt(MAX_AOE_LAYER_KEY, aoeDefinition.layer);
            behaviourTag.putInt(AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.putInt(AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.putInt(AOE_LAYER_KEY, aoeDefinition.layer);
        }

        if (toolProperty.isMagnetic()) {
            behaviourTag.putBoolean(RELOCATE_MINED_BLOCKS_KEY, true);
            behaviourTag.putBoolean(RELOCATE_MOB_DROPS_KEY, true);
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

    default @Nullable ToolProperty getToolProperty() {
        return getMaterial().getProperty(PropertyKey.TOOL);
    }

    default float getMaterialToolSpeed() {
        ToolProperty toolProperty = getToolProperty();
        return toolProperty == null ? 0F : toolProperty.getHarvestSpeed();
    }

    default float getMaterialAttackDamage() {
        ToolProperty toolProperty = getToolProperty();
        return toolProperty == null ? 0F : toolProperty.getAttackDamage();
    }

    default float getMaterialAttackSpeed() {
        ToolProperty toolProperty = getToolProperty();
        return toolProperty == null ? 0F : toolProperty.getAttackSpeed();
    }

    default int getMaterialDurability() {
        ToolProperty toolProperty = getToolProperty();
        return toolProperty == null ? 0 : toolProperty.getDurability() * toolProperty.getDurabilityMultiplier();
    }

    default int getMaterialEnchantability() {
        ToolProperty toolProperty = getToolProperty();
        return toolProperty == null ? 0 : toolProperty.getEnchantability();
    }

    default int getMaterialHarvestLevel() {
        ToolProperty toolProperty = getToolProperty();
        return toolProperty == null ? 0 : toolProperty.getHarvestLevel();
    }

    default int getProspectingDepth() {
        ToolProperty toolProperty = getToolProperty();
        return toolProperty == null ? 1 : toolProperty.getProspectingDepth();
    }

    default long getMaxCharge(ItemStack stack) {
        if (isElectric()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(MAX_CHARGE_KEY, Tag.TAG_LONG)) {
                return tag.getLong(MAX_CHARGE_KEY);
            }
        }
        return -1L;
    }

    default long getCharge(ItemStack stack) {
        if (isElectric()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(CHARGE_KEY, Tag.TAG_LONG)) {
                return tag.getLong(CHARGE_KEY);
            }
        }
        return -1L;
    }

    default float getTotalToolSpeed(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(TOOL_SPEED_KEY, Tag.TAG_FLOAT)) {
            return toolTag.getFloat(TOOL_SPEED_KEY);
        }
        float toolSpeed = getToolStats().getEfficiencyMultiplier(stack) * getMaterialToolSpeed() +
                getToolStats().getBaseEfficiency(stack);
        toolTag.putFloat(TOOL_SPEED_KEY, toolSpeed);
        return toolSpeed;
    }

    default float getTotalAttackDamage(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ATTACK_DAMAGE_KEY, Tag.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_DAMAGE_KEY);
        }
        float baseDamage = getToolStats().getBaseDamage(stack);
        float attackDamage = 0;
        // represents a tool that should always have an attack damage value of 0
        if (baseDamage != Float.MIN_VALUE) {
            attackDamage = getMaterialAttackDamage() + baseDamage;
        }
        toolTag.putFloat(ATTACK_DAMAGE_KEY, attackDamage);
        return attackDamage;
    }

    default float getTotalAttackSpeed(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ATTACK_SPEED_KEY, Tag.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_SPEED_KEY);
        }
        float attackSpeed = getMaterialAttackSpeed() + getToolStats().getAttackSpeed(stack);
        toolTag.putFloat(ATTACK_SPEED_KEY, attackSpeed);
        return attackSpeed;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(MAX_DURABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(MAX_DURABILITY_KEY);
        }

        IGTToolDefinition toolStats = getToolStats();
        int maxDurability = getMaterialDurability();
        int builderDurability = (int) (toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack));

        // If there is no durability set in the tool builder, multiply the builder AOE multiplier to the material
        // durability
        maxDurability = builderDurability == 0 ? (int) (maxDurability * toolStats.getDurabilityMultiplier(stack)) :
                maxDurability + builderDurability;

        toolTag.putInt(MAX_DURABILITY_KEY, maxDurability);
        return maxDurability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ENCHANTABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(ENCHANTABILITY_KEY);
        }
        int enchantability = getMaterialEnchantability();
        toolTag.putInt(ENCHANTABILITY_KEY, enchantability);
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(HARVEST_LEVEL_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(HARVEST_LEVEL_KEY);
        }
        int harvestLevel = getMaterialHarvestLevel() + getToolStats().getBaseQuality(stack);
        toolTag.putInt(HARVEST_LEVEL_KEY, harvestLevel);
        return harvestLevel;
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, BlockState state) {
        // special case check (mostly for the sword)
        float specialValue = getDestroySpeed(state, getToolClasses(stack));
        if (specialValue != -1) return specialValue;

        if (isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack))) {
            return getTotalToolSpeed(stack);
        }

        return getToolStats().isToolEffective(state) ? getTotalToolSpeed(stack) : 1.0F;
    }

    default boolean definition$hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        getToolStats().getBehaviors().forEach(behavior -> behavior.hitEntity(stack, target, attacker));
        ToolHelper.damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide) return false;
        getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isShiftKeyDown()) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            int result = -1;
            if (isTool(stack, GTToolType.SHEARS)) {
                result = shearBlockRoutine(serverPlayer, stack, pos);
            }
            if (result != 0) {
                // prevent exploits with instantly breakable blocks
                BlockState state = player.level().getBlockState(pos);
                boolean effective = false;
                for (GTToolType type : getToolClasses(stack)) {
                    if (type.harvestTags.stream().anyMatch(state::is)) {
                        effective = true;
                        break;
                    }
                }

                effective |= isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));

                if (effective) {
                    if (areaOfEffectBlockBreakRoutine(stack, serverPlayer, pos)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            var tag = getBehaviorsTag(stack);
                            if (tag.getBoolean(TREE_FELLING_KEY) &&
                                    !tag.getBoolean(DISABLE_TREE_FELLING_KEY) &&
                                    state.is(BlockTags.LOGS)) {
                                TreeFellingHelper.fellTree(stack, player.level(), state, pos, player);
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
                ToolHelper.damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
            }
            if (entityLiving instanceof Player && playSoundOnBlockDestroy()) {
                // sneaking disables AOE, which means it is okay to play the sound
                // not checking this means the sound will play for every AOE broken block, which is very loud
                if (entityLiving.isShiftKeyDown()) {
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
            return this.getMaterial() == gtTool.getMaterial();
        }

        MaterialEntry entry = ChemicalHelper.getMaterialEntry(repair.getItem());
        if (entry.isEmpty()) return false;

        if (entry.material() == this.getMaterial()) {
            // special case wood to allow Wood Planks
            if (VanillaRecipeHelper.isMaterialWood(entry.material())) {
                return entry.tagPrefix() == TagPrefix.planks;
            }
            // Gems can use gem and plate, Ingots can use ingot and plate
            if (entry.tagPrefix() == TagPrefix.plate) {
                return true;
            }
            if (entry.material().hasProperty(PropertyKey.INGOT)) {
                return entry.tagPrefix() == TagPrefix.ingot;
            }
            if (entry.material().hasProperty(PropertyKey.GEM)) {
                return entry.tagPrefix() == TagPrefix.gem;
            }
        }
        return false;
    }

    default Map<Enchantment, Integer> definition$getAllEnchantments(ItemStack stack) {
        var defaultEnchantments = getDefaultEnchantments(stack);

        if (defaultEnchantments.isEmpty()) {
            return EnchantmentHelper.getEnchantments(stack);
        }
        return joinEnchantments(stack, defaultEnchantments);
    }

    default Map<Enchantment, Integer> getDefaultEnchantments(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);

        if (toolTag.contains(DEFAULT_ENCHANTMENTS_KEY, Tag.TAG_LIST)) {
            ListTag defaultsTag = toolTag.getList(DEFAULT_ENCHANTMENTS_KEY, Tag.TAG_COMPOUND);
            return EnchantmentHelper.deserializeEnchantments(defaultsTag);
        }

        // Get tool and material enchantments
        Object2IntMap<Enchantment> defaultEnchantments = new Object2IntLinkedOpenHashMap<>();
        defaultEnchantments.putAll(getToolStats().getDefaultEnchantments(stack));
        defaultEnchantments.putAll(this.getMaterial().getProperty(PropertyKey.TOOL).getEnchantments());

        // save them to the tool NBT tag
        ListTag enchantList = new ListTag();
        for (var entry : defaultEnchantments.object2IntEntrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment == null || !this.definition$canApplyAtEnchantingTable(stack, enchantment)) {
                continue;
            }
            int level = entry.getIntValue();
            enchantList.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), level));
        }
        toolTag.put(DEFAULT_ENCHANTMENTS_KEY, enchantList);

        return defaultEnchantments;
    }

    default int definition$getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        return definition$getAllEnchantments(stack).getOrDefault(enchantment, 0);
    }

    default boolean definition$isFoil(ItemStack stack) {
        return !getAllEnchantments(stack).isEmpty();
    }

    default Multimap<Attribute, AttributeModifier> definition$getDefaultAttributeModifiers(EquipmentSlot equipmentSlot,
                                                                                           ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                    getTotalAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                    Math.max(-3.9D, getTotalAttackSpeed(stack)), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
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
        if (!oldStack.is(newStack.getItem())) {
            return true;
        }
        if (newStack.isDamageableItem() && oldStack.isDamageableItem()) {
            CompoundTag newTag = newStack.getTag();
            CompoundTag oldTag = oldStack.getTag();
            if (newTag != null && oldTag != null) {
                Set<String> newKeys = new HashSet<>(newTag.getAllKeys());
                Set<String> oldKeys = new HashSet<>(oldTag.getAllKeys());
                newKeys.remove(ItemStack.TAG_DAMAGE);
                oldKeys.remove(ItemStack.TAG_DAMAGE);
                newKeys.remove(CHARGE_KEY);
                oldKeys.remove(CHARGE_KEY);
                if (!newKeys.equals(oldKeys)) {
                    return true;
                }
                return !newKeys.stream().allMatch((key) -> Objects.equals(newTag.get(key), oldTag.get(key)));
            }
            return newTag != null || oldTag != null;
        }
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    default boolean definition$hasCraftingRemainingItem(ItemStack stack) {
        return stack.getTag() == null || !stack.getTag().getBoolean(DISALLOW_CONTAINER_ITEM_KEY);
    }

    default ItemStack definition$getCraftingRemainingItem(ItemStack stack) {
        // Sanity-check, callers should really validate with hasContainerItem themselves...
        if (!definition$hasCraftingRemainingItem(stack)) {
            return ItemStack.EMPTY;
        }
        stack = stack.copy();
        Player player = ForgeHooks.getCraftingPlayer();
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

    default int definition$getDamage(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        // this only exists to support old tools now.
        if (toolTag.contains(DURABILITY_KEY, Tag.TAG_INT)) {
            int damage = toolTag.getInt(DURABILITY_KEY);
            // remove the old durability nbt tag
            toolTag.remove(DURABILITY_KEY);
            return damage;
        }
        return IForgeItem.super.getDamage(stack);
    }

    default int definition$getMaxDamage(ItemStack stack) {
        return getTotalMaxDurability(stack);
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

    default boolean definition$canPerformAction(@NotNull ItemStack stack, @NotNull ToolAction action) {
        if (getToolType().defaultAbilities.contains(action)) {
            return true;
        }
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.canPerformAction(stack, action)) {
                return true;
            }
        }

        return false;
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
            InteractionResult result = behavior.onItemUse(context);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> definition$use(Level world, Player player, InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);
        // TODO: relocate to keybind action when keybind PR happens
        for (var behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemRightClick(world, player, hand).getResult() == InteractionResult.SUCCESS) {
                return InteractionResultHolder.success(heldItem);
            }
        }
        return InteractionResultHolder.pass(heldItem);
    }

    default boolean definition$shouldOpenUIAfterUse(UseOnContext context) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (!behavior.shouldOpenUIAfterUse(context)) {
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

    default void definition$appendHoverText(@NotNull ItemStack stack, @Nullable Level world,
                                            @NotNull List<Component> tooltip, TooltipFlag flag) {
        if (!(stack.getItem() instanceof IGTTool tool)) return;

        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) return;

        IGTToolDefinition toolStats = tool.getToolStats();

        // electric info
        if (this.isElectric()) {
            ElectricStats.addCurrentChargeTooltip(tooltip, getCharge(stack), getMaxCharge(stack), getElectricTier(),
                    false);
        }

        // durability info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            // Plus 1 to match vanilla behavior where tools can still be used once at zero durability. We want to not
            // show this
            int damageRemaining = tool.getTotalMaxDurability(stack) - stack.getDamageValue() + 1;
            if (toolStats.isSuitableForCrafting(stack)) {
                tooltip.add(Component.translatable("item.gtceu.tool.tooltip.crafting_uses", FormattingUtil
                        .formatNumbers(damageRemaining / Math.max(1, toolStats.getToolDamagePerCraft(stack)))));
            }
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.max_uses",
                    FormattingUtil.formatNumbers(tool.getTotalMaxDurability(stack))));
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
            if (Language.getInstance().has(harvestName)) { // if there's a defined name for the harvest level, use it
                tooltip.add(Component.translatable("item.gtceu.tool.tooltip.harvest_level_extra", harvestLevel,
                        Component.translatable(harvestName)));
            } else {
                tooltip.add(Component.translatable("item.gtceu.tool.tooltip.harvest_level", harvestLevel));
            }
        }

        // behaviors
        boolean addedBehaviorNewLine = false;
        AoESymmetrical aoeDefinition = getAoEDefinition(stack);

        if (!aoeDefinition.isZero()) {
            addedBehaviorNewLine = tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        CompoundTag behaviorsTag = getBehaviorsTag(stack);
        if (behaviorsTag.getBoolean(RELOCATE_MINED_BLOCKS_KEY)) {
            if (!addedBehaviorNewLine) {
                addedBehaviorNewLine = true;
                tooltip.add(CommonComponents.EMPTY);
            }
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.relocate_mining"));
        }

        if (!addedBehaviorNewLine && !toolStats.getBehaviors().isEmpty()) {
            tooltip.add(CommonComponents.EMPTY);
        }
        toolStats.getBehaviors().forEach(behavior -> behavior.addInformation(stack, world, tooltip, flag));

        // unique tooltip
        String uniqueTooltip = this.getToolType().getUnlocalizedName() + ".tooltip";
        if (Language.getInstance().has(uniqueTooltip)) {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable(uniqueTooltip));
        }

        tooltip.add(CommonComponents.EMPTY);

        var defaultEnchants = getDefaultEnchantments(stack);
        if (!defaultEnchants.isEmpty()) {
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.default_enchantments"));
            for (var entry : defaultEnchants.entrySet()) {
                Enchantment enchant = entry.getKey();
                if (enchant == null) continue;

                tooltip.add(enchant.getFullname(entry.getValue()));
            }
        }

        tooltip.add(CommonComponents.EMPTY);

        // valid tools
        tooltip.add(Component.translatable("item.gtceu.tool.usable_as",
                getToolClassNames(stack).stream()
                        .filter(s -> Language.getInstance().has("gtceu.tool.class." + s))
                        .map(s -> Component.translatable("gtceu.tool.class." + s))
                        .collect(Component::empty, FormattingUtil::combineComponents,
                                FormattingUtil::combineComponents)));

        // repair info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            if (GTUtil.isShiftDown()) {
                Material material = getMaterial();

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

    ResourceLocation COFH_SMASHING_ENCHANT_ID = new ResourceLocation(GTValues.MODID_ENSORCELLATION, "smashing");
    Set<ResourceLocation> AUTOSMELT_ENCHANT_IDS = Util.make(new HashSet<>(), set -> {
        set.add(new ResourceLocation(GTValues.MODID_ENDERIO, "auto_smelt")); // EnderIO
        set.add(new ResourceLocation(GTValues.MODID_ENSORCELLATION, "smelting")); // CoFH
    });

    default boolean definition$canApplyAtEnchantingTable(@NotNull ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) return false;

        ResourceLocation enchantmentId = EnchantmentHelper.getEnchantmentId(enchantment);
        if (COFH_SMASHING_ENCHANT_ID.equals(enchantmentId)) {
            // block CoFH smashing enchant from all tools
            return false;
        } else if (AUTOSMELT_ENCHANT_IDS.contains(enchantmentId)) {
            // block autosmelt enchants from AoE and Tree-Felling tools
            if (!getToolStats().getAoEDefinition(stack).isZero() ||
                    getBehaviorsTag(stack).contains(TREE_FELLING_KEY)) {
                return false;
            }
        }

        // bypass EnumEnchantmentType#canEnchantItem and define custom stack-aware logic.
        // the Minecraft method takes an Item, and does not respect NBT nor meta.
        switch (enchantment.category) {
            case DIGGER -> {
                return getToolStats().isSuitableForBlockBreak(stack);
            }
            case WEAPON -> {
                return getToolStats().isSuitableForAttacking(stack);
            }
            case BREAKABLE -> {
                return stack.getTag() != null && !stack.getTag().getBoolean(UNBREAKABLE_KEY);
            }
            case VANISHABLE -> {
                return true;
            }
        }

        ToolProperty property = this.getToolProperty();
        if (property == null) return false;

        // Check for any special enchantments specified by the material of this Tool
        if (!property.getEnchantments().isEmpty() && property.getEnchantments().containsKey(enchantment)) {
            return true;
        }

        // Check for any additional Enchantment Types added in the builder
        return getToolStats().isEnchantable(stack) && getToolStats().canApplyEnchantment(stack, enchantment);
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
        getToolTag(stack).putInt(LAST_CRAFTING_USE_KEY, (int) System.currentTimeMillis());
    }

    default boolean canPlaySound(ItemStack stack) {
        return Math.abs((int) System.currentTimeMillis() - getToolTag(stack).getInt(LAST_CRAFTING_USE_KEY)) > 1000;
    }

    default void playSound(Player player) {
        if (ConfigHolder.INSTANCE.client.toolUseSounds && getSound() != null) {
            player.level().playSound(null, player.position().x, player.position().y, player.position().z,
                    getSound().getMainEvent(), SoundSource.PLAYERS, 1F, 1F);
        }
    }

    @Override
    default ModularUI createUI(Player player, HeldItemUIFactory.HeldItemHolder holder) {
        for (var behavior : getToolStats().getBehaviors()) {
            if (!(behavior instanceof IToolUIBehavior uiBehavior) || !uiBehavior.openUI(player, holder.getHand())) {
                continue;
            }
            return uiBehavior.createUI(player, holder);
        }
        return new ModularUI(holder, player);
    }

    default Set<GTToolType> getToolClasses(ItemStack stack) {
        return new HashSet<>(getToolType().toolClasses);
    }

    default Set<String> getToolClassNames(ItemStack stack) {
        return getToolClasses(stack).stream().flatMap(type -> type.toolClassNames.stream()).collect(Collectors.toSet());
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        if (isElectric()) {
            ElectricStats item = ElectricStats.createElectricItem(0L, getElectricTier());
            providers.add(new ICapabilityProvider() {

                @Override
                public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                                  @Nullable Direction arg) {
                    return item.getCapability(stack, capability);
                }
            });
        }
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior instanceof IComponentCapability componentCapability) {
                providers.add(new ICapabilityProvider() {

                    @Override
                    public @NotNull <
                            T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                                                             @Nullable Direction arg) {
                        return componentCapability.getCapability(stack, capability);
                    }
                });
            }
        }
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
    }

    default boolean definition$isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (stack.getItem() instanceof IGTTool gtTool) {
            return isToolEffective(state, gtTool.getToolClasses(stack), gtTool.getTotalHarvestLevel(stack));
        }
        return stack.getItem().isCorrectToolForDrops(state);
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
                            if (itemStack.hasTag() && getToolTag(itemStack).contains(TINT_COLOR_KEY, Tag.TAG_INT)) {
                                yield getToolTag(itemStack).getInt(TINT_COLOR_KEY);
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
