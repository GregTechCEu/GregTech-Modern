package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.datacomponents.GTTool;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolUIBehavior;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.material.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbility;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.*;

public interface IGTTool extends IUIHolder.Item, ItemLike {

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
        return new ItemStack(asItem());
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
    default ToolProperty getToolProperty() {
        return getMaterial().getProperty(PropertyKey.TOOL);
    }

    @Nullable
    default DustProperty getDustProperty(ItemStack stack) {
        return getToolMaterial(stack).getProperty(PropertyKey.DUST);
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

    @SuppressWarnings("DataFlowIssue")
    default long getMaxCharge(ItemStack stack) {
        if (isElectric()) {
            if (stack.has(GTDataComponents.ENERGY_CONTENT)) {
                return stack.get(GTDataComponents.ENERGY_CONTENT).maxCharge();
            }
        }
        return -1L;
    }

    @SuppressWarnings("DataFlowIssue")
    default long getCharge(ItemStack stack) {
        if (isElectric()) {
            if (stack.has(GTDataComponents.ENERGY_CONTENT)) {
                return stack.get(GTDataComponents.ENERGY_CONTENT).charge();
            }
        }
        return -1L;
    }

    default float getTotalToolSpeed() {
        return getToolStats().getEfficiencyMultiplier() * getMaterialToolSpeed() +
                getToolStats().getBaseEfficiency();
    }

    default float getTotalAttackDamage() {
        float baseDamage = getToolStats().getBaseDamage();
        final float attackDamage;
        // represents a tool that should always have an attack damage value of 0
        // formatted like this to have attackDamage be final for the lambda.
        if (baseDamage != Float.MIN_VALUE) {
            attackDamage = getMaterialAttackDamage() + baseDamage;
        } else {
            attackDamage = 0;
        }
        return attackDamage;
    }

    default float getTotalAttackSpeed() {
        return getMaterialAttackSpeed() + getToolStats().getTool().defaultMiningSpeed();
    }

    @SuppressWarnings("DataFlowIssue")
    default int getTotalMaxDurability(ItemStack stack) {
        if (stack.has(DataComponents.MAX_DAMAGE)) {
            return stack.get(DataComponents.MAX_DAMAGE);
        }

        IGTToolDefinition toolStats = getToolStats();
        int maxDurability = getMaterialDurability();
        int builderDurability = (int) (toolStats.getBaseDurability() * toolStats.getDurabilityMultiplier());

        // If there is no durability set in the tool builder, multiply the builder AOE multiplier to the material
        // durability
        maxDurability = builderDurability == 0 ? (int) (maxDurability * toolStats.getDurabilityMultiplier()) :
                maxDurability + builderDurability;
        stack.set(DataComponents.MAX_DAMAGE, maxDurability);
        return maxDurability;
    }

    @SuppressWarnings("DataFlowIssue")
    default int getTotalEnchantability(ItemStack stack) {
        if (stack.has(GTDataComponents.GT_TOOL) && stack.get(GTDataComponents.GT_TOOL).enchantability().isPresent()) {
            return stack.get(GTDataComponents.GT_TOOL).enchantability().get();
        }
        int enchantability = getMaterialEnchantability();
        stack.update(GTDataComponents.GT_TOOL, GTTool.EMPTY, tool -> tool.setEnchantability(enchantability));
        return enchantability;
    }

    default int getTotalHarvestLevel() {
        return getMaterialHarvestLevel() + getToolStats().getBaseQuality();
    }

    // Item.class methods
    default float definition$getDestroySpeed(ItemStack stack, BlockState state) {
        if (isToolEffective(stack, state, getToolClasses(stack), getTotalHarvestLevel())) {
            return getTotalToolSpeed();
        }

        return getToolStats().isToolEffective(state) ? getToolStats().getTool().getMiningSpeed(state) : 1.0F;
    }

    default boolean definition$hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        getBehaviorsComponent(stack).behaviors()
                .forEach((key, behavior) -> behavior.hitEntity(stack, target, attacker));
        damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide) return false;
        getBehaviorsComponent(stack).behaviors()
                .forEach((type, behavior) -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isShiftKeyDown()) {
            ServerPlayer playerMP = (ServerPlayer) player;
            int result = -1;
            if (isTool(stack, GTToolType.SHEARS)) {
                result = shearBlockRoutine(playerMP, stack, pos);
            }
            if (result != 0) {
                // prevent exploits with instantly breakable blocks
                BlockState state = player.level().getBlockState(pos);
                boolean effective = isToolEffective(stack, state, getToolClasses(stack), getTotalHarvestLevel());

                if (effective) {
                    if (areaOfEffectBlockBreakRoutine(stack, playerMP)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            var behavior = getBehaviorsComponent(stack).getBehavior(GTToolBehaviors.TREE_FELLING);
                            if (behavior != null && behavior.isEnabled() && state.is(BlockTags.LOGS)) {
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
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
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
            return getToolMaterial(toRepair) == gtTool.getToolMaterial(repair);
        }
        MaterialEntry entry = ChemicalHelper.getMaterialEntry(repair.getItem());
        if (entry.isEmpty()) return false;
        if (entry.material() == getToolMaterial(toRepair)) {
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

    default ItemEnchantments definition$getAllEnchantments(ItemStack stack,
                                                           HolderLookup.RegistryLookup<Enchantment> lookup) {
        var existing = stack.get(GTDataComponents.INNATE_ENCHANTMENTS);
        if (existing != null) {
            return existing;
        }

        ItemEnchantments original = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        var enchantments = new ItemEnchantments.Mutable(original);

        ToolProperty toolProperty = this.getMaterial().getProperty(PropertyKey.TOOL);

        // Set tool and material enchantments
        Object2IntMap<ResourceKey<Enchantment>> innateEnchantments = new Object2IntOpenHashMap<>();
        innateEnchantments.putAll(getToolStats().getDefaultEnchantments());
        innateEnchantments.putAll(toolProperty.getEnchantments());

        innateEnchantments.forEach((enchantKey, level) -> {
            lookup.get(enchantKey).ifPresent(enchant -> enchantments.upgrade(enchant, level));
        });
        // noinspection DataFlowIssue
        return stack.set(GTDataComponents.INNATE_ENCHANTMENTS, enchantments.toImmutable());
    }

    default int definition$getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        HolderLookup.RegistryLookup<Enchantment> lookup = enchantment.unwrapLookup();
        if (lookup == null) {
            return stack.getTagEnchantments().getLevel(enchantment);
        }
        return definition$getAllEnchantments(stack, lookup).getLevel(enchantment);
    }

    default int definition$getHarvestLevel(ItemStack stack, GTToolType toolClass, @Nullable Player player,
                                           @Nullable BlockState blockState) {
        return getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel() : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity,
                                                LivingEntity attacker) {
        return getBehaviorsComponent(stack).behaviors().values().stream()
                .anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
    }

    default boolean definition$doesSneakBypassUse(@NotNull ItemStack stack, @NotNull BlockGetter world,
                                                  @NotNull BlockPos pos, @NotNull Player player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (!newStack.is(oldStack.getItem()))
            return true;

        if (!newStack.isDamageableItem() || !oldStack.isDamageableItem())
            return !ItemStack.isSameItemSameComponents(newStack, oldStack);

        DataComponentMap newComponents = newStack.getComponents();
        DataComponentMap oldComponents = oldStack.getComponents();

        if (newComponents.isEmpty() || oldComponents.isEmpty())
            return !(newComponents.isEmpty() && oldComponents.isEmpty());

        Set<DataComponentType<?>> newKeys = new HashSet<>(newComponents.keySet());
        Set<DataComponentType<?>> oldKeys = new HashSet<>(oldComponents.keySet());

        newKeys.remove(DataComponents.DAMAGE);
        oldKeys.remove(DataComponents.DAMAGE);
        newKeys.remove(GTDataComponents.ENERGY_CONTENT.get());
        oldKeys.remove(GTDataComponents.ENERGY_CONTENT.get());

        if (!newKeys.equals(oldKeys))
            return true;

        return !newKeys.stream().allMatch(key -> Objects.equals(newComponents.get(key), oldComponents.get(key)));
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
        // While preliminarily fixing ItemStack being null in CommonHooks#getContainerItem in the PR
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

    default boolean definition$canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility action) {
        if (getToolType().defaultAbilities.contains(action)) {
            return true;
        }
        for (IToolBehavior<?> behavior : getBehaviorsComponent(stack).behaviors().values()) {
            if (behavior.canPerformAction(stack, action)) {
                return true;
            }
        }

        return false;
    }

    default InteractionResult definition$onItemUseFirst(ItemStack stack, UseOnContext context) {
        for (IToolBehavior<?> behavior : getBehaviorsComponent(stack).behaviors().values()) {
            if (behavior.onItemUseFirst(stack, context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    default InteractionResult definition$onItemUse(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        for (IToolBehavior<?> behavior : getBehaviorsComponent(stack).behaviors().values()) {
            if (behavior.onItemUse(context) == InteractionResult.SUCCESS) {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> definition$use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // TODO: relocate to keybind action when keybind PR happens
        for (IToolBehavior<?> behavior : getBehaviorsComponent(stack).behaviors().values()) {
            if (behavior.onItemRightClick(world, player, hand).getResult() == InteractionResult.SUCCESS) {
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    default boolean definition$shouldOpenUIAfterUse(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        for (IToolBehavior<?> behavior : getBehaviorsComponent(stack).behaviors().values()) {
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

    // Client-side methods

    @OnlyIn(Dist.CLIENT)
    default void definition$appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context,
                                            @NotNull List<Component> tooltip, TooltipFlag flag) {
        if (!(stack.getItem() instanceof IGTTool tool)) return;

        IGTToolDefinition toolStats = tool.getToolStats();

        // electric info
        if (this.isElectric()) {
            tooltip.add(Component.translatable("metaitem.generic.electric_item.tooltip",
                    FormattingUtil.formatNumbers(getCharge(stack)),
                    FormattingUtil.formatNumbers(getMaxCharge(stack)),
                    GTValues.VNF[getElectricTier()]));
        }

        // durability info
        if (!stack.has(DataComponents.UNBREAKABLE)) {
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
                    FormattingUtil.formatNumbers(2 + tool.getTotalAttackDamage())));
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.attack_speed",
                    FormattingUtil.formatNumbers(4 + tool.getTotalAttackSpeed())));
        }

        // mining info
        if (toolStats.isSuitableForBlockBreak(stack)) {
            tooltip.add(Component.translatable("item.gtceu.tool.tooltip.mining_speed",
                    FormattingUtil.formatNumbers(tool.getTotalToolSpeed())));

            int harvestLevel = tool.getTotalHarvestLevel();
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
        AoESymmetrical aoeDefinition = getAoEDefinition(stack);

        if (!aoeDefinition.isNone()) {
            addedBehaviorNewLine = tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.gtceu.tool.behavior.aoe_mining",
                    aoeDefinition.column() * 2 + 1, aoeDefinition.row() * 2 + 1, aoeDefinition.layer() + 1));
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
                getToolClassNames(stack).stream()
                        .filter(s -> I18n.exists("gtceu.tool.class." + s))
                        .map(s -> Component.translatable("gtceu.tool.class." + s))
                        .collect(Component::empty, FormattingUtil::combineComponents,
                                FormattingUtil::combineComponents)));

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
        stack.update(GTDataComponents.GT_TOOL, GTTool.EMPTY,
                tool -> tool.setLastCraftingUse((int) System.currentTimeMillis()));
    }

    default boolean canPlaySound(ItemStack stack) {
        return Math.abs(
                (int) System.currentTimeMillis() - stack.getOrDefault(GTDataComponents.GT_TOOL, GTTool.EMPTY)
                        .lastCraftingUse().orElse(0)) >
                1000;
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
            if (!(behavior instanceof IToolUIBehavior<?> uiBehavior) || !uiBehavior.openUI(player, holder.getHand())) {
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

    default void attachCapabilities(RegisterCapabilitiesEvent event) {
        for (IToolBehavior<?> behavior : getToolStats().getBehaviors()) {
            if (behavior instanceof IComponentCapability componentCapability) {
                componentCapability.attachCapabilities(event, this.asItem());
            }
        }
        if (this.isElectric()) {
            ElectricStats item = ElectricStats.createElectricItem(0L, getElectricTier());
            item.attachCapabilities(event, this.asItem());
        }
    }

    default boolean definition$isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (stack.getItem() instanceof IGTTool gtTool) {
            return isToolEffective(stack, state, gtTool.getToolClasses(stack), gtTool.getTotalHarvestLevel());
        }
        return stack.isCorrectToolForDrops(state);
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
                                // noinspection DataFlowIssue
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
