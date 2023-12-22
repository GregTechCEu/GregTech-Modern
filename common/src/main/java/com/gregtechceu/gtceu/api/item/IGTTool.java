package com.gregtechceu.gtceu.api.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.item.gui.PlayerInventoryHolder;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.TreeFellingHelper;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.ICraftRemainder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ModHandler;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import dev.architectury.injectables.annotations.ExpectPlatform;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.MendingEnchantment;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.*;
import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_UUID;
import static net.minecraft.world.item.Item.BASE_ATTACK_SPEED_UUID;

public interface IGTTool extends IItemUIFactory {

    Material getMaterial();
    
    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEntry getSound();

    boolean playSoundOnBlockDestroy();

    default Item get() {
        return (Item) this;
    }

    default ItemStack getRaw() {
        ItemStack stack = new ItemStack(get());
        getBehaviorsTag(stack);
        return stack;
    }

    default ItemStack get(Material material) {
        ItemStack stack = new ItemStack(get());

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
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);

        // Durability formula we are working with:
        // Final Durability = (material durability * material durability multiplier) + (tool definition durability * definition durability multiplier) - 1
        // Subtracts 1 internally since Minecraft treats "0" as a valid durability, but we don't want to display this.

        int durability = toolProperty.getDurability() * toolProperty.getDurabilityMultiplier();

        // Most Tool Definitions do not set a base durability, which will lead to ignoring the multiplier if present. So apply the multiplier to the material durability if that would happen
        if (toolStats.getBaseDurability(stack) == 0) {
            durability *= toolStats.getDurabilityMultiplier(stack);
        } else {
            durability += toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack);
        }

        toolTag.putInt(MAX_DURABILITY_KEY, durability - 1);
        toolTag.putInt(DURABILITY_KEY, 0);
        if (toolProperty.isUnbreakable()) {
            stackCompound.putBoolean(UNBREAKABLE_KEY, true);
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
        CompoundTag behaviourTag = getBehaviorsTag(stack);
        getToolStats().getBehaviors().forEach(behavior -> behavior.addBehaviorNBT(stack, behaviourTag));


        if (aoeDefinition != AoESymmetrical.none()) {
            behaviourTag.putInt(MAX_AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.putInt(MAX_AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.putInt(MAX_AOE_LAYER_KEY, aoeDefinition.layer);
            behaviourTag.putInt(AOE_COLUMN_KEY, aoeDefinition.column);
            behaviourTag.putInt(AOE_ROW_KEY, aoeDefinition.row);
            behaviourTag.putInt(AOE_LAYER_KEY, aoeDefinition.layer);
        }

        if (toolProperty.isMagnetic()) {
            behaviourTag.putBoolean(RELOCATE_MINED_BLOCKS_KEY, true);
        }

        return stack;
    }

    default ItemStack get(Material material, long defaultCharge, long defaultMaxCharge) {
        ItemStack stack = get(material);
        if (isElectric()) {
            ElectricItem electricItem = (ElectricItem) GTCapabilityHelper.getElectricItem(stack);
            if (electricItem != null) {
                electricItem.setMaxChargeOverride(defaultMaxCharge);
                electricItem.setCharge(defaultCharge);
            }
        }
        return stack;
    }

    default ItemStack get(Material material, long defaultMaxCharge) {
        return get(material, defaultMaxCharge, defaultMaxCharge);
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
        float toolSpeed = getToolStats().getEfficiencyMultiplier(stack) * getMaterialToolSpeed(stack) + getToolStats().getBaseEfficiency(stack);
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
            attackDamage = getMaterialAttackDamage(stack) + baseDamage;
        }
        toolTag.putFloat(ATTACK_DAMAGE_KEY, attackDamage);
        return attackDamage;
    }

    default float getTotalAttackSpeed(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ATTACK_SPEED_KEY, Tag.TAG_FLOAT)) {
            return toolTag.getFloat(ATTACK_SPEED_KEY);
        }
        float attackSpeed = getMaterialAttackSpeed(stack) + getToolStats().getAttackSpeed(stack);
        toolTag.putFloat(ATTACK_SPEED_KEY, attackSpeed);
        return attackSpeed;
    }

    default int getTotalMaxDurability(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(MAX_DURABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(MAX_DURABILITY_KEY);
        }

        IGTToolDefinition toolStats = getToolStats();
        int maxDurability = getMaterialDurability(stack);
        int builderDurability = (int) (toolStats.getBaseDurability(stack) * toolStats.getDurabilityMultiplier(stack));

        // If there is no durability set in the tool builder, multiply the builder AOE multiplier to the material durability
        maxDurability = builderDurability == 0 ? (int) (maxDurability * toolStats.getDurabilityMultiplier(stack)) : maxDurability + builderDurability;

        toolTag.putInt(MAX_DURABILITY_KEY, maxDurability);
        return maxDurability;
    }

    default int getTotalEnchantability(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(ENCHANTABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(ENCHANTABILITY_KEY);
        }
        int enchantability = getMaterialEnchantability(stack);
        toolTag.putInt(ENCHANTABILITY_KEY, enchantability);
        return enchantability;
    }

    default int getTotalHarvestLevel(ItemStack stack) {
        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(HARVEST_LEVEL_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(HARVEST_LEVEL_KEY);
        }
        int harvestLevel = getMaterialHarvestLevel(stack) + getToolStats().getBaseQuality(stack);
        toolTag.putInt(HARVEST_LEVEL_KEY, harvestLevel);
        return harvestLevel;
    }

    default AoESymmetrical getMaxAoEDefinition(ItemStack stack) {
        return AoESymmetrical.readMax(getBehaviorsTag(stack));
    }

    default AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.read(getToolTag(stack), getMaxAoEDefinition(stack));
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
        damageItem(stack, attacker, getToolStats().getToolDamagePerAttack(stack));
        return true;
    }

    default boolean definition$onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (player.level().isClientSide) return false;
        getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockStartBreak(stack, pos, player));

        if (!player.isCrouching()) {
            ServerPlayer playerMP = (ServerPlayer) player;
            int result = -1;
            if (isTool(stack, GTToolType.SHEARS)) {
                result = shearBlockRoutine(playerMP, stack, pos);
            }
            if (result != 0) {
                // prevent exploits with instantly breakable blocks
                BlockState state = player.level().getBlockState(pos);
                boolean effective = false;
                for (GTToolType type : getToolClasses(stack)) {
                    if (state.is(type.harvestTag)) {
                        effective = true;
                        break;
                    }
                }

                effective |= isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));

                if (effective) {
                    if (areaOfEffectBlockBreakRoutine(stack, playerMP)) {
                        if (playSoundOnBlockDestroy()) playSound(player);
                    } else {
                        if (result == -1) {
                            new TreeFellingHelper().fellTree(stack, player.level(), state, pos, player);
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

    default boolean definition$mineBlock(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!worldIn.isClientSide) {
            getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockDestroyed(stack, worldIn, state, pos, entityLiving));

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
        if (repair.getItem() instanceof IGTTool) {
            return getToolMaterial(toRepair) == ((IGTTool) repair.getItem()).getToolMaterial(repair);
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

    default Multimap<Attribute, AttributeModifier> definition$getDefaultAttributeModifiers(EquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getTotalAttackDamage(stack), AttributeModifier.Operation.ADDITION));
            multimap.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", Math.max(-3.9D, getTotalAttackSpeed(stack)), AttributeModifier.Operation.ADDITION));
        }
        return multimap;
    }

    default int definition$getHarvestLevel(ItemStack stack, GTToolType toolClass, @Nullable Player player, @Nullable BlockState blockState) {
        return getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return getToolStats().getBehaviors().stream().anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
    }

    default boolean definition$doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull BlockGetter world, @Nonnull BlockPos pos, @Nonnull Player player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem() || oldStack.getDamageValue() < newStack.getDamageValue();
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
        Player player = ICraftRemainder.craftingPlayer.get();
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

    default boolean definition$shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
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

    default boolean definition$isDamaged(ItemStack stack) {
        return definition$getDamage(stack) > 0;
    }

    default int definition$getDamage(ItemStack stack) {
        // bypass the Forge OreDictionary using ItemStack#getItemDamage instead of ItemStack#getMetadata
        // this will allow tools to retain their oredicts when durability changes.
        // No normal tool ItemStack a player has should ever have a metadata value other than 0
        // so this should not cause unexpected behavior for them

        CompoundTag toolTag = getToolTag(stack);
        if (toolTag.contains(DURABILITY_KEY, Tag.TAG_INT)) {
            return toolTag.getInt(DURABILITY_KEY);
        }
        toolTag.putInt(DURABILITY_KEY, 0);
        return 0;
    }

    default int definition$getMaxDamage(ItemStack stack) {
        return getTotalMaxDurability(stack);
    }

    default void definition$setDamage(ItemStack stack, int durability) {
        CompoundTag toolTag = getToolTag(stack);
        toolTag.putInt(DURABILITY_KEY, durability);
    }

    default double definition$getDurabilityForDisplay(ItemStack stack) {
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        if (damage == 0) return 1.0;
        return (double) (maxDamage - damage) / (double) maxDamage;
    }

    @ExpectPlatform
    public static void init() {

    }

    default InteractionResult definition$onItemUseFirst(@Nonnull Player player, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Direction facing, float hitX, float hitY, float hitZ, @Nonnull InteractionHand hand) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUseFirst(player, world, pos, facing, hitX, hitY, hitZ, hand) == InteractionResult.SUCCESS)  {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    default InteractionResult definition$onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS)  {
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    default InteractionResultHolder<ItemStack> definition$use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            // TODO: relocate to keybind action when keybind PR happens
            if (player.isCrouching() && getMaxAoEDefinition(stack) != AoESymmetrical.none()) {
                PlayerInventoryHolder.openHandItemUI(player, hand);
                return InteractionResultHolder.success(stack);
            }
        }

        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemRightClick(world, player, hand).getResult() == InteractionResult.SUCCESS) {
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    default void definition$getSubItems(@Nonnull NonNullList<ItemStack> items) {
        if (isElectric()) {
            items.add(get(GTMaterials.Iron, Integer.MAX_VALUE));
        } else {
            items.add(get(GTMaterials.Iron));
        }
    }

    // Client-side methods

    @Environment(EnvType.CLIENT)
    default void definition$addInformation(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, TooltipFlag flag) {
        if (!(stack.getItem() instanceof IGTTool tool)) return;

        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) return;

        IGTToolDefinition toolStats = tool.getToolStats();

        // electric info
        if (this.isElectric()) {
            tooltip.add(Component.translatable("metaitem.generic.electric_item.tooltip",
                    getCharge(stack),
                    getMaxCharge(stack),
                    GTValues.VNF[getElectricTier()]));
        }

        // durability info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            // Plus 1 to match vanilla behavior where tools can still be used once at zero durability. We want to not show this
            int damageRemaining = tool.getTotalMaxDurability(stack) - stack.getDamageValue() + 1;
            if (toolStats.isSuitableForCrafting(stack)) {
                tooltip.add(Component.translatable("item.gt.tool.tooltip.crafting_uses", FormattingUtil.formatNumbers(damageRemaining / Math.max(1, toolStats.getToolDamagePerCraft(stack)))));
            }

            tooltip.add(Component.translatable("item.gt.tool.tooltip.general_uses", FormattingUtil.formatNumbers(damageRemaining)));
        }

        // attack info
        if (toolStats.isSuitableForAttacking(stack)) {
            tooltip.add(Component.translatable("item.gt.tool.tooltip.attack_damage", FormattingUtil.formatNumbers(2 + tool.getTotalAttackDamage(stack))));
            tooltip.add(Component.translatable("item.gt.tool.tooltip.attack_speed", FormattingUtil.formatNumbers(4 + tool.getTotalAttackSpeed(stack))));
        }

        // mining info
        if (toolStats.isSuitableForBlockBreak(stack)) {
            tooltip.add(Component.translatable("item.gt.tool.tooltip.mining_speed", FormattingUtil.formatNumbers(tool.getTotalToolSpeed(stack))));

            int harvestLevel = tool.getTotalHarvestLevel(stack);
            String harvestName = "item.gt.tool.harvest_level." + harvestLevel;
            if (I18n.exists(harvestName)) { // if there's a defined name for the harvest level, use it
                tooltip.add(Component.translatable("item.gt.tool.tooltip.harvest_level_extra", harvestLevel, Component.translatable(harvestName)));
            } else {
                tooltip.add(Component.translatable("item.gt.tool.tooltip.harvest_level", harvestLevel));
            }
        }

        // behaviors
        boolean addedBehaviorNewLine = false;
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        if (aoeDefinition != AoESymmetrical.none()) {
            addedBehaviorNewLine = tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.gt.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        CompoundTag behaviorsTag = getBehaviorsTag(stack);
        if (behaviorsTag.getBoolean(RELOCATE_MINED_BLOCKS_KEY)) {
            if (!addedBehaviorNewLine) {
                addedBehaviorNewLine = true;
                tooltip.add(Component.literal(""));
            }
            tooltip.add(Component.translatable("item.gt.tool.behavior.relocate_mining"));
        }

        if (!addedBehaviorNewLine && !toolStats.getBehaviors().isEmpty()) {
            tooltip.add(Component.literal(""));
        }
        toolStats.getBehaviors().forEach(behavior -> behavior.addInformation(stack, world, tooltip, flag));

        // unique tooltip
        String uniqueTooltip = "item.gt.tool." + BuiltInRegistries.ITEM.getKey(this.get()).getPath() + ".tooltip";
        if (I18n.exists(uniqueTooltip)) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable(uniqueTooltip));
        }

        tooltip.add(Component.literal(""));

        // valid tools
        tooltip.add(Component.translatable("item.gt.tool.usable_as",
                getToolClasses(stack).stream()
                        .map(s -> Component.translatable("gt.tool.class." + s))
                        .collect(Component::empty, (c1, c2) -> c1.append(", ").append(c2), (c1, c2) -> c1.append(", ").append(c2))
        ));

        // repair info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            if (GTUtil.isShiftDown()) {
                Material material = getToolMaterial(stack);

                Collection<Component> repairItems = new ArrayList<>();
                if (!ModHandler.isMaterialWood(material)) {
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
                    tooltip.add(Component.translatable("item.gt.tool.tooltip.repair_material", repairItems.stream().collect(Component::empty, (c1, c2) -> c1.append(", ").append(c2), (c1, c2) -> c1.append(", ").append(c2))));
                }
            } else {
                tooltip.add(Component.translatable("item.gt.tool.tooltip.repair_info"));
            }
        }
        if (this.isElectric()) {
            tooltip.add(Component.translatable("item.gt.tool.replace_tool_head"));
        }
    }

    default boolean definition$canApplyAtEnchantingTable(@Nonnull ItemStack stack, Enchantment enchantment) {
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
                return getToolStats().getAoEDefinition(stack) == AoESymmetrical.none() && !getBehaviorsTag(stack).contains(TREE_FELLING_KEY);
        }

        // Block Mending and Unbreaking on Electric tools
        if (isElectric() && (enchantment instanceof MendingEnchantment || enchantment instanceof DigDurabilityEnchantment)) {
            return false;
        }

        if (enchantment.category == null) return true;
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

    @Environment(EnvType.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

    // Sound Playing
    default void playCraftingSound(Player player, ItemStack stack) {
        // player null check for things like auto-crafters
        if (ConfigHolder.INSTANCE.client.toolCraftingSounds && getSound() != null && player != null) {
            if (canPlaySound(stack)) {
                setLastCraftingSoundTime(stack);
                player.level().playSound(null, player.position().x, player.position().y, player.position().z, getSound().getMainEvent(), SoundSource.PLAYERS, 1F, 1F);
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
            player.level().playSound(null, player.position().x, player.position().y, player.position().z, getSound().getMainEvent(), SoundSource.PLAYERS, 1F, 1F);
        }
    }

    default ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        CompoundTag tag = getBehaviorsTag(holder.getHeld());
        AoESymmetrical defaultDefinition = getMaxAoEDefinition(holder.getHeld());
        return new ModularUI(120, 80, holder, entityPlayer).background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(6, 10, "item.gt.tool.aoe.columns"))
                .widget(new LabelWidget(49, 10, "item.gt.tool.aoe.rows"))
                .widget(new LabelWidget(79, 10, "item.gt.tool.aoe.layers"))
                .widget(new ButtonWidget(15, 24, 20, 20, new TextTexture("+"), (data) -> {
                    AoESymmetrical.increaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(15, 44, 20, 20, new TextTexture("-"), (data) -> {
                    AoESymmetrical.decreaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(50, 24, 20, 20, new TextTexture("+"), (data) -> {
                    AoESymmetrical.increaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(50, 44, 20, 20, new TextTexture("-"), (data) -> {
                    AoESymmetrical.decreaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(85, 24, 20, 20, new TextTexture("+"), (data) -> {
                    AoESymmetrical.increaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ButtonWidget(85, 44, 20, 20, new TextTexture("-"), (data) -> {
                    AoESymmetrical.decreaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new LabelWidget(23, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getColumn(getBehaviorsTag(holder.getHeld()), defaultDefinition))))
                .widget(new LabelWidget(58, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getRow(getBehaviorsTag(holder.getHeld()), defaultDefinition))))
                .widget(new LabelWidget(93, 65, () ->
                        Integer.toString(1 + AoESymmetrical.getLayer(getBehaviorsTag(holder.getHeld()), defaultDefinition))));
    }

    Set<GTToolType> getToolClasses(ItemStack stack);
}
