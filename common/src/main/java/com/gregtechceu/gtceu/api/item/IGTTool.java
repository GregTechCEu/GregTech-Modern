package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IGTToolDefinition;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Set;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.*;

public interface IGTTool extends IItemUIFactory {

    boolean isElectric();

    int getElectricTier();

    IGTToolDefinition getToolStats();

    @Nullable
    SoundEntry getSound();

    boolean playSoundOnBlockDestroy();

    @Nullable
    String getOreDictName();

    @Nullable
    Supplier<ItemStack> getMarkerItem();

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
        CompoundTag toolTag = getToolTag(stack);
        String string = toolTag.getString(MATERIAL_KEY);
        Material material = GTRegistries.MATERIALS.get(string);
        if (material == null) {
            toolTag.putString(MATERIAL_KEY, (material = GTMaterials.Iron).toString());
        }
        return material;
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

    default boolean definition$hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
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
                            treeFellingRoutine(playerMP, stack, pos);
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

    default boolean definition$onBlockDestroyed(ItemStack stack, Level worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (!worldIn.isRemote) {
            getToolStats().getBehaviors().forEach(behavior -> behavior.onBlockDestroyed(stack, worldIn, state, pos, entityLiving));

            if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
                damageItem(stack, entityLiving, getToolStats().getToolDamagePerBlockBreak(stack));
            }
            if (entityLiving instanceof Player && playSoundOnBlockDestroy()) {
                // sneaking disables AOE, which means it is okay to play the sound
                // not checking this means the sound will play for every AOE broken block, which is very loud
                if (entityLiving.isSneaking()) {
                    playSound((Player) entityLiving);
                }
            }
        }
        return true;
    }

    default boolean definition$getIsRepairable(ItemStack toRepair, ItemStack repair) {
        // full durability tools in the left slot are not repairable
        // this is needed so enchantment merging works when both tools are full durability
        if (toRepair.getItemDamage() == 0) return false;
        if (repair.getItem() instanceof IGTTool) {
            return getToolMaterial(toRepair) == ((IGTTool) repair.getItem()).getToolMaterial(repair);
        }
        UnificationEntry entry = OreDictUnifier.getUnificationEntry(repair);
        if (entry == null || entry.material == null) return false;
        if (entry.material == getToolMaterial(toRepair)) {
            // special case wood to allow Wood Planks
            if (ModHandler.isMaterialWood(entry.material)) {
                return entry.orePrefix == OrePrefix.plank;
            }
            // Gems can use gem and plate, Ingots can use ingot and plate
            if (entry.orePrefix == OrePrefix.plate) {
                return true;
            }
            if (entry.material.hasProperty(PropertyKey.INGOT)) {
                return entry.orePrefix == OrePrefix.ingot;
            }
            if (entry.material.hasProperty(PropertyKey.GEM)) {
                return entry.orePrefix == OrePrefix.gem;
            }
        }
        return false;
    }

    default Multimap<String, AttributeModifier> definition$getAttributeModifiers(EntityEquipmentSlot equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.create();
        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getTotalAttackDamage(stack), 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", Math.max(-3.9D, getTotalAttackSpeed(stack)), 0));
        }
        return multimap;
    }

    default int definition$getHarvestLevel(ItemStack stack, String toolClass, @Nullable Player player, @Nullable BlockState blockState) {
        return get().getToolClasses(stack).contains(toolClass) ? getTotalHarvestLevel(stack) : -1;
    }

    default boolean definition$canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return getToolStats().getBehaviors().stream().anyMatch(behavior -> behavior.canDisableShield(stack, shield, entity, attacker));
    }

    default boolean definition$doesSneakBypassUse(@Nonnull ItemStack stack, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull Player player) {
        return getToolStats().doesSneakBypassUse();
    }

    default boolean definition$shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() != newStack.getItem() || oldStack.getItemDamage() < newStack.getItemDamage();
    }

    default boolean definition$hasContainerItem(ItemStack stack) {
        return stack.getTag() == null || !stack.getTag().getBoolean(DISALLOW_CONTAINER_ITEM_KEY);
    }

    default ItemStack definition$getContainerItem(ItemStack stack) {
        // Sanity-check, callers should really validate with hasContainerItem themselves...
        if (!definition$hasContainerItem(stack)) {
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

    default boolean definition$canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, Player player) {
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
        if (stack.getMetadata() == GTValues.W) {
            return GTValues.W;
        }

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
        int damage = stack.getItem().getDamage(stack);
        int maxDamage = stack.getItem().getMaxDamage(stack);
        if (damage == 0) return 1.0;
        return (double) (maxDamage - damage) / (double) maxDamage;
    }

    @Nullable
    default ICapabilityProvider definition$initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        List<ICapabilityProvider> providers = new ArrayList<>();
        if (isElectric()) {
            providers.add(ElectricStats.createElectricItem(0L, getElectricTier()).createProvider(stack));
        }
        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            ICapabilityProvider behaviorProvider = behavior.createProvider(stack, nbt);
            if (behaviorProvider != null) {
                providers.add(behaviorProvider);
            }
        }
        if (providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        return new CombinedCapabilityProvider(providers);
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

    default ActionResult<ItemStack> definition$onItemRightClick(World world, Player player, InteractionHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            // TODO: relocate to keybind action when keybind PR happens
            if (player.isSneaking() && getMaxAoEDefinition(stack) != AoESymmetrical.none()) {
                PlayerInventoryHolder.openHandItemUI(player, hand);
                return ActionResult.newResult(InteractionResult.SUCCESS, stack);
            }
        }

        for (IToolBehavior behavior : getToolStats().getBehaviors()) {
            if (behavior.onItemRightClick(world, player, hand).getType() == InteractionResult.SUCCESS) {
                return ActionResult.newResult(InteractionResult.SUCCESS, stack);
            }
        }
        return ActionResult.newResult(InteractionResult.PASS, stack);
    }

    default void definition$getSubItems(@Nonnull NonNullList<ItemStack> items) {
        if (getMarkerItem() != null) {
            items.add(getMarkerItem().get());
        } else if (isElectric()) {
            items.add(get(Materials.Iron, Integer.MAX_VALUE));
        } else {
            items.add(get(Materials.Iron));
        }
    }

    // Client-side methods

    @SideOnly(Side.CLIENT)
    default void definition$addInformation(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<String> tooltip, ITooltipFlag flag) {
        if (!(stack.getItem() instanceof IGTTool)) return;
        IGTTool tool = (IGTTool) stack.getItem();

        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) return;

        IGTToolDefinition toolStats = tool.getToolStats();

        // electric info
        if (this.isElectric()) {
            tooltip.add(I18n.format("metaitem.generic.electric_item.tooltip",
                    getCharge(stack),
                    getMaxCharge(stack),
                    GTValues.VNF[getElectricTier()]));
        }

        // durability info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            // Plus 1 to match vanilla behavior where tools can still be used once at zero durability. We want to not show this
            int damageRemaining = tool.getTotalMaxDurability(stack) - stack.getItemDamage() + 1;
            if (toolStats.isSuitableForCrafting(stack)) {
                tooltip.add(I18n.format("item.gt.tool.tooltip.crafting_uses", TextFormattingUtil.formatNumbers(damageRemaining / Math.max(1, toolStats.getToolDamagePerCraft(stack)))));
            }

            tooltip.add(I18n.format("item.gt.tool.tooltip.general_uses", TextFormattingUtil.formatNumbers(damageRemaining)));
        }

        // attack info
        if (toolStats.isSuitableForAttacking(stack)) {
            tooltip.add(I18n.format("item.gt.tool.tooltip.attack_damage", TextFormattingUtil.formatNumbers(2 + tool.getTotalAttackDamage(stack))));
            tooltip.add(I18n.format("item.gt.tool.tooltip.attack_speed", TextFormattingUtil.formatNumbers(4 + tool.getTotalAttackSpeed(stack))));
        }

        // mining info
        if (toolStats.isSuitableForBlockBreak(stack)) {
            tooltip.add(I18n.format("item.gt.tool.tooltip.mining_speed", TextFormattingUtil.formatNumbers(tool.getTotalToolSpeed(stack))));

            int harvestLevel = tool.getTotalHarvestLevel(stack);
            String harvestName = "item.gt.tool.harvest_level." + harvestLevel;
            if (I18n.contains(harvestName)) { // if there's a defined name for the harvest level, use it
                tooltip.add(I18n.format("item.gt.tool.tooltip.harvest_level_extra", harvestLevel, I18n.format(harvestName)));
            } else {
                tooltip.add(I18n.format("item.gt.tool.tooltip.harvest_level", harvestLevel));
            }
        }

        // behaviors
        boolean addedBehaviorNewLine = false;
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        if (aoeDefinition != AoESymmetrical.none()) {
            addedBehaviorNewLine = tooltip.add("");
            tooltip.add(I18n.format("item.gt.tool.behavior.aoe_mining",
                    aoeDefinition.column * 2 + 1, aoeDefinition.row * 2 + 1, aoeDefinition.layer + 1));
        }

        CompoundTag behaviorsTag = getBehaviorsTag(stack);
        if (behaviorsTag.getBoolean(RELOCATE_MINED_BLOCKS_KEY)) {
            if (!addedBehaviorNewLine) {
                addedBehaviorNewLine = true;
                tooltip.add("");
            }
            tooltip.add(I18n.format("item.gt.tool.behavior.relocate_mining"));
        }

        if (!addedBehaviorNewLine && !toolStats.getBehaviors().isEmpty()) {
            tooltip.add("");
        }
        toolStats.getBehaviors().forEach(behavior -> behavior.addInformation(stack, world, tooltip, flag));

        // unique tooltip
        String uniqueTooltip = "item.gt.tool." + getToolId() + ".tooltip";
        if (I18n.contains(uniqueTooltip)) {
            tooltip.add("");
            tooltip.add(I18n.format(uniqueTooltip));
        }

        tooltip.add("");

        // valid tools
        tooltip.add(I18n.format("item.gt.tool.usable_as",
                stack.getItem().getToolClasses(stack).stream()
                        .map(s -> I18n.format("gt.tool.class." + s))
                        .collect(Collectors.joining(", "))
        ));

        // repair info
        if (!tagCompound.getBoolean(UNBREAKABLE_KEY)) {
            if (TooltipHelper.isShiftDown()) {
                Material material = getToolMaterial(stack);

                Collection<String> repairItems = new ArrayList<>();
                if (!ModHandler.isMaterialWood(material)) {
                    if (material.hasProperty(PropertyKey.INGOT)) {
                        repairItems.add(OrePrefix.ingot.getLocalNameForItem(material));
                    } else if (material.hasProperty(PropertyKey.GEM)) {
                        repairItems.add(OrePrefix.gem.getLocalNameForItem(material));
                    }
                }
                if (!OreDictUnifier.get(OrePrefix.plate, material).isEmpty()) {
                    repairItems.add(OrePrefix.plate.getLocalNameForItem(material));
                }
                if (!repairItems.isEmpty()) {
                    tooltip.add(I18n.format("item.gt.tool.tooltip.repair_material", String.join(", ", repairItems)));
                }
            } else {
                tooltip.add(I18n.format("item.gt.tool.tooltip.repair_info"));
            }
        }
        if (this.isElectric()) {
            tooltip.add(I18n.format("item.gt.tool.replace_tool_head"));
        }
    }

    default boolean definition$canApplyAtEnchantingTable(@Nonnull ItemStack stack, Enchantment enchantment) {
        if (stack.isEmpty()) return false;

        // special case enchants from other mods
        switch (enchantment.getName()) {
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
        if (isElectric() && (enchantment instanceof EnchantmentMending || enchantment instanceof EnchantmentDurability)) {
            return false;
        }

        if (enchantment.type == null) return true;
        // bypass EnumEnchantmentType#canEnchantItem and define custom stack-aware logic.
        // the Minecraft method takes an Item, and does not respect NBT nor meta.
        switch (enchantment.type) {
            case DIGGER: {
                return getToolStats().isSuitableForBlockBreak(stack);
            }
            case WEAPON: {
                return getToolStats().isSuitableForAttacking(stack);
            }
            case BREAKABLE:
                return stack.getTag() != null && !stack.getTag().getBoolean(UNBREAKABLE_KEY);
            case ALL: {
                return true;
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

    @SideOnly(Side.CLIENT)
    default int getColor(ItemStack stack, int tintIndex) {
        return tintIndex % 2 == 1 ? getToolMaterial(stack).getMaterialRGB() : 0xFFFFFF;
    }

    @SideOnly(Side.CLIENT)
    default String getModelPath() {
        return getDomain() + ":" + "tools/" + getToolId();
    }

    @SideOnly(Side.CLIENT)
    default ModelResourceLocation getModelLocation() {
        return new ModelResourceLocation(getModelPath(), "inventory");
    }

    // Sound Playing
    default void playCraftingSound(Player player, ItemStack stack) {
        // player null check for things like auto-crafters
        if (ConfigHolder.client.toolCraftingSounds && getSound() != null && player != null) {
            if (canPlaySound(stack)) {
                setLastCraftingSoundTime(stack);
                player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
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
        if (ConfigHolder.client.toolUseSounds && getSound() != null) {
            player.getEntityWorld().playSound(null, player.posX, player.posY, player.posZ, getSound(), SoundCategory.PLAYERS, 1F, 1F);
        }
    }

    default ModularUI createUI(PlayerInventoryHolder holder, Player entityPlayer) {
        CompoundTag tag = getBehaviorsTag(holder.getCurrentItem());
        AoESymmetrical defaultDefinition = getMaxAoEDefinition(holder.getCurrentItem());
        return ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 120, 80)
                .label(6, 10, "item.gt.tool.aoe.columns")
                .label(49, 10, "item.gt.tool.aoe.rows")
                .label(79, 10, "item.gt.tool.aoe.layers")
                .widget(new ClickButtonWidget(15, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(15, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseColumn(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(50, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseRow(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 24, 20, 20, "+", data -> {
                    AoESymmetrical.increaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new ClickButtonWidget(85, 44, 20, 20, "-", data -> {
                    AoESymmetrical.decreaseLayer(tag, defaultDefinition);
                    holder.markAsDirty();
                }))
                .widget(new DynamicLabelWidget(23, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getColumn(getBehaviorsTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(58, 65, () ->
                        Integer.toString(1 + 2 * AoESymmetrical.getRow(getBehaviorsTag(holder.getCurrentItem()), defaultDefinition))))
                .widget(new DynamicLabelWidget(93, 65, () ->
                        Integer.toString(1 + AoESymmetrical.getLayer(getBehaviorsTag(holder.getCurrentItem()), defaultDefinition))))
                .build(holder, entityPlayer);
    }

    Set<GTToolType> getToolClasses(ItemStack stack);

    // Extended Interfaces

    // IAEWrench

    /**
     * Check if the wrench can be used.
     *
     * @param player wrenching player
     * @param pos    of block.
     * @return true if wrench can be used
     */
    @Override
    default boolean canWrench(ItemStack wrench, Player player, BlockPos pos) {
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
    }

    // IToolWrench

    /*** Called to ensure that the wrench can be used.
     *
     * @param player - The player doing the wrenching
     * @param hand - Which hand was holding the wrench
     * @param wrench - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched
     *
     * @return true if wrenching is allowed, false if not */
    @Override
    default boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        return get().getToolClasses(wrench).contains(ToolClasses.WRENCH);
    }

    /*** Callback after the wrench has been used. This can be used to decrease durability or for other purposes.
     *
     * @param player - The player doing the wrenching
     * @param hand - Which hand was holding the wrench
     * @param wrench - The item stack that holds the wrench
     * @param rayTrace - The object that is being wrenched */
    @Override
    default void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IToolHammer
    @Override
    default boolean isUsable(ItemStack item, LivingEntity user, BlockPos pos) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default boolean isUsable(ItemStack item, LivingEntity user, Entity entity) {
        return get().getToolClasses(item).contains(ToolClasses.WRENCH);
    }

    @Override
    default void toolUsed(ItemStack item, LivingEntity user, BlockPos pos) {
        damageItem(item, user);
        if (user instanceof Player) {
            playSound((Player) user);
        }
    }

    @Override
    default void toolUsed(ItemStack item, LivingEntity user, Entity entity) {
        damageItem(item, user);
    }

    // ITool
    @Override
    default boolean canUse(@Nonnull InteractionHand hand, @Nonnull Player player, @Nonnull BlockPos pos) {
        return get().getToolClasses(player.getHeldItem(hand)).contains(ToolClasses.WRENCH);
    }

    @Override
    default void used(@Nonnull InteractionHand hand, @Nonnull Player player, @Nonnull BlockPos pos) {
        damageItem(player.getHeldItem(hand), player);
        playSound(player);
    }

    // IHideFacades
    @Override
    default boolean shouldHideFacades(@Nonnull ItemStack stack, @Nonnull Player player) {
        return get().getToolClasses(stack).contains(ToolClasses.WRENCH);
    }

    // IToolGrafter

    /**
     * Called by leaves to determine the increase in sapling droprate.
     *
     * @param stack ItemStack containing the grafter.
     * @param world Minecraft world the player and the target block inhabit.
     * @param pos   Coordinate of the broken leaf block.
     * @return Float representing the factor the usual drop chance is to be multiplied by.
     */
    @Override
    default float getSaplingModifier(ItemStack stack, Level world, Player player, BlockPos pos) {
        return getToolClasses(stack).contains(ToolClasses.GRAFTER) ? 100F : 1.0F;
    }

    // IOverlayRenderAware
    @Override
    default void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition) {
        ToolChargeBarRenderer.renderBarsTool(this, stack, xPosition, yPosition);
    }
}
