package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.capability.ElectricItem;
import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.datacomponents.GTTool;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviorsComponent;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.material.ChemicalHelper;
import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.api.material.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.material.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.tag.TagPrefix;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.item.GTItems;
import com.gregtechceu.gtceu.data.machine.GTMachines;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.DummyMachineBlockEntity;
import com.gregtechceu.gtceu.utils.InfiniteEnergyContainer;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.IShearable;
import net.neoforged.neoforge.event.EventHooks;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ToolHelper
 */
public class ToolHelper {

    // Crafting Symbols
    private static final BiMap<Character, GTToolType> symbols = HashBiMap.create();

    private ToolHelper() {/**/}

    /**
     * @return finds the registered crafting symbol with the tool
     */
    public static Character getSymbolFromTool(GTToolType tool) {
        return symbols.inverse().get(tool);
    }

    /**
     * @return finds the registered tool with the crafting symbol
     */
    public static GTToolType getToolFromSymbol(Character symbol) {
        return symbols.get(symbol);
    }

    public static Set<Character> getToolSymbols() {
        return symbols.keySet();
    }

    /**
     * Registers the tool against a crafting symbol, this is used in
     * {@link com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper}
     */
    public static void registerToolSymbol(Character symbol, GTToolType tool) {
        symbols.put(symbol, tool);
    }

    public static ToolBehaviorsComponent getBehaviorsComponent(ItemStack stack) {
        return stack.getOrDefault(GTDataComponents.TOOL_BEHAVIORS, ToolBehaviorsComponent.EMPTY);
    }

    public static boolean hasBehaviorsComponent(ItemStack stack) {
        return stack.has(GTDataComponents.TOOL_BEHAVIORS);
    }

    public static ItemStack get(GTToolType toolType, Material material) {
        if (material.hasProperty(PropertyKey.TOOL)) {
            var entry = GTItems.TOOL_ITEMS.get(material, toolType);
            if (entry != null) {
                return entry.get().get();
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean is(ItemStack stack, GTToolType toolType) {
        return getToolTypes(stack).contains(toolType);
    }

    public static boolean canUse(ItemStack stack) {
        return stack.getDamageValue() <= stack.getMaxDamage();
    }

    public static void damageItem(@NotNull ItemStack stack, @Nullable LivingEntity user, int damage) {
        if (!(stack.getItem() instanceof IGTTool tool)) {
            if (user != null) stack.hurtAndBreak(damage, user, EquipmentSlot.MAINHAND);
        } else {
            if (stack.has(DataComponents.UNBREAKABLE)) {
                return;
            }
            if (!(user instanceof Player player) || !player.isCreative()) {
                RandomSource random = user == null ? GTValues.RNG : user.getRandom();
                if (tool.isElectric()) {
                    int electricDamage = damage * ConfigHolder.INSTANCE.machines.energyUsageMultiplier;
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
                    if (electricItem != null) {
                        electricItem.discharge(electricDamage, tool.getElectricTier(), true, false, false);
                        if (electricItem.getCharge() > 0 &&
                                random.nextInt(100) >= ConfigHolder.INSTANCE.tools.rngDamageElectricTools) {
                            return;
                        }
                    } else {
                        throw new IllegalStateException(
                                "Electric tool does not have an attached electric item capability.");
                    }
                }
                int negated = EnchantmentHelper.processDurabilityChange((ServerLevel) user.level(), stack, damage);
                damage -= negated;
                if (damage <= 0) {
                    return;
                }
                int newDurability = stack.getDamageValue() + damage;
                if (user instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, stack, newDurability);
                }
                stack.setDamageValue(newDurability);
                if (newDurability > stack.getMaxDamage()) {
                    if (user instanceof Player player) {
                        Stat<?> stat = Stats.ITEM_BROKEN.get(stack.getItem());
                        player.awardStat(stat);
                    }
                    if (user != null) {
                        user.breakItem(stack);
                    }
                    stack.shrink(1);
                }
            }
        }
    }

    public static void playToolSound(GTToolType toolType, ServerPlayer player) {
        if (toolType.soundEntry != null) {
            toolType.soundEntry.playOnServer(player.level(), player.blockPosition());
        }
    }

    public static ItemStack getAndSetToolData(GTToolType toolType, Material material, int maxDurability,
                                              int harvestLevel,
                                              float toolSpeed, float attackDamage) {
        var entry = GTItems.TOOL_ITEMS.get(material, toolType);
        if (entry == null) return ItemStack.EMPTY;
        ItemStack stack = entry.get().getRaw();
        stack.update(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(Collections.emptyList(), true),
                val -> val.withTooltip(false));
        stack.set(DataComponents.MAX_DAMAGE, maxDurability);
        GTTool toolComponent = new GTTool(Optional.of(toolSpeed), Optional.of(attackDamage), Optional.empty(),
                Optional.of(harvestLevel), Optional.empty());
        stack.set(GTDataComponents.GT_TOOL, toolComponent);
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
        if (toolProperty != null) {
            toolProperty.getEnchantments().forEach((enchantment, level) -> {
                if (entry.get().definition$canApplyAtEnchantingTable(stack, enchantment)) {
                    Registry<Enchantment> registry = GTRegistries.builtinRegistry().registryOrThrow(Registries.ENCHANTMENT);
                    stack.enchant(registry.getHolderOrThrow(enchantment), level);
                }
            });
        }
        return stack;
    }

    /**
     * Attempts to get an electric item variant with override of max charge
     *
     * @param maxCharge new max charge of this electric item
     * @return item stack with given max charge
     * @throws IllegalStateException if this item is not electric item or uses custom implementation
     */
    public static ItemStack getMaxChargeOverrideStack(Item item, long maxCharge) {
        ItemStack itemStack = item.getDefaultInstance();
        IElectricItem iElectricItem = GTCapabilityHelper.getElectricItem(itemStack);
        if (!(iElectricItem instanceof ElectricItem electricItem)) {
            throw new IllegalStateException("Not an electric item.");
        }
        electricItem.setMaxChargeOverride(maxCharge);
        return itemStack;
    }

    /**
     * AoE Block Breaking Routine.
     */
    public static boolean areaOfEffectBlockBreakRoutine(ItemStack stack, ServerPlayer player) {
        int currentDurability = stack.getDamageValue();
        int maximumDurability = stack.getMaxDamage();
        int remainingUses = maximumDurability - currentDurability;
        Set<BlockPos> harvestableBlocks = getHarvestableBlocks(stack, player);
        if (!harvestableBlocks.isEmpty()) {
            int blocksBroken = 0;
            for (BlockPos pos : harvestableBlocks) {
                if (!breakBlockRoutine(player, stack, pos, blocksBroken++ == 0)) {
                    return true;
                }

                remainingUses--;
                if (stack.getItem() instanceof IGTTool gtTool && !gtTool.isElectric() && remainingUses == 0) {
                    return true;
                }
                // If the tool is an electric tool, catch the tool breaking and cancel the remaining AOE
                else if (!ItemStack.isSameItem(player.getMainHandItem(), stack)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface AOEFunction {

        boolean apply(ItemStack stack, Level level, Player player, BlockPos start, UseOnContext context);
    }

    public static AoESymmetrical getAoEDefinition(ItemStack stack) {
        return stack.getOrDefault(GTDataComponents.AOE, AoESymmetrical.none());
    }

    public static Set<BlockPos> iterateAoE(ItemStack stack, AoESymmetrical aoeDefinition, Level world,
                                           Player player, HitResult rayTraceResult,
                                           AOEFunction function) {
        if (aoeDefinition != null && aoeDefinition != AoESymmetrical.none() &&
                rayTraceResult instanceof BlockHitResult blockHit && blockHit.getDirection() != null) {
            int column = aoeDefinition.column;
            int row = aoeDefinition.row;
            int layer = aoeDefinition.layer;
            Direction playerFacing = player.getDirection();
            Direction.Axis playerAxis = playerFacing.getAxis();
            Direction.Axis sideHitAxis = blockHit.getDirection().getAxis();
            Direction.AxisDirection sideHitAxisDir = blockHit.getDirection().getAxisDirection();
            Set<BlockPos> validPositions = new ObjectOpenHashSet<>();
            if (sideHitAxis.isVertical()) {
                boolean isX = playerAxis == Direction.Axis.X;
                boolean isDown = sideHitAxisDir == Direction.AxisDirection.NEGATIVE;
                for (int y = 0; y <= layer; y++) {
                    for (int x = isX ? -row : -column; x <= (isX ? row : column); x++) {
                        for (int z = isX ? -column : -row; z <= (isX ? column : row); z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = blockHit.getBlockPos().offset(x, isDown ? y : -y, z);
                                if (player.mayUseItemAt(pos.relative(blockHit.getDirection()), blockHit.getDirection(),
                                        stack)) {
                                    if (function.apply(stack, world, player, pos, new UseOnContext(player.level(),
                                            player, player.getUsedItemHand(), stack, blockHit))) {
                                        validPositions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                boolean isX = sideHitAxis == Direction.Axis.X;
                boolean isNegative = sideHitAxisDir == Direction.AxisDirection.NEGATIVE;
                for (int x = 0; x <= layer; x++) {
                    // Special case for any additional column > 1: https://i.imgur.com/Dvcx7Vg.png
                    // Same behaviour as the Flux Bore
                    for (int y = (row == 0 ? 0 : -1); y <= (row == 0 ? 0 : row * 2 - 1); y++) {
                        for (int z = -column; z <= column; z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = blockHit.getBlockPos().offset(
                                        isX ? (isNegative ? x : -x) : (isNegative ? z : -z), y,
                                        isX ? (isNegative ? z : -z) : (isNegative ? x : -x));
                                if (function.apply(stack, world, player, pos, new UseOnContext(player.level(), player,
                                        player.getUsedItemHand(), stack, blockHit))) {
                                    validPositions.add(pos);
                                }
                            }
                        }
                    }
                }
            }
            return validPositions;
        }
        return Collections.emptySet();
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, Level world,
                                                     Player player, HitResult rayTraceResult) {
        return iterateAoE(stack, aoeDefinition, world, player, rayTraceResult, ToolHelper::isBlockAoEHarvestable);
    }

    private static boolean isBlockAoEHarvestable(ItemStack stack, Level world, Player player, BlockPos pos,
                                                 UseOnContext context) {
        if (world.getBlockState(pos).isAir()) return false;

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof LiquidBlock) return false;

        BlockPos hitBlockPos = context.getClickedPos();
        BlockState hitBlockState = world.getBlockState(hitBlockPos);
        if (state.getDestroySpeed(world, pos) < 0 ||
                state.getDestroySpeed(world, pos) - hitBlockState.getDestroySpeed(world, hitBlockPos) > 8) {
            // If mining a block takes significantly longer than the center block, do not mine it.
            // Originally this was just a check for if it is at all harder of a block, however that
            // would cause some annoyances, like Grass Block not being broken if a Dirt Block was the
            // hit block for AoE. This value is somewhat arbitrary, but should cause things to feel
            // natural to mine, but avoid exploits like mining Obsidian quickly by instead targeting Stone.
            return false;
        }
        return stack.getItem().isCorrectToolForDrops(stack, state);
    }

    /**
     * Applies Forge Hammer recipes to block broken, used for hammers or tools with hard hammer enchant applied.
     */
    public static void applyHammerDropConversion(ServerLevel world, BlockPos pos, ItemStack tool, BlockState state,
                                                 List<ItemStack> drops, int fortune, float dropChance,
                                                 RandomSource random) {
        if (is(tool, GTToolType.HARD_HAMMER) || /*
                                                 * EnchantmentHelper.getEnchantmentLevel(EnchantmentHardHammer.INSTANCE,
                                                 * tool)
                                                 */ -1 > 0) {
            List<ItemStack> silktouchDrops = getSilkTouchDrop(world, pos, state);
            for (ItemStack silktouchDrop : silktouchDrops) {
                if (silktouchDrop.isEmpty()) continue;
                // Stack lists can be immutable going into Recipe#matches barring no rewrites
                // Search for forge hammer recipes from all drops individually (only LV or under)

                Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> caps = Tables
                        .newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
                DummyMachineBlockEntity be = new DummyMachineBlockEntity(GTValues.LV,
                        GTRecipeTypes.FORGE_HAMMER_RECIPES, GTMachines.defaultTankSizeFunction, caps);
                caps.put(IO.IN, EURecipeCapability.CAP, List.of(new InfiniteEnergyContainer(be.getMetaMachine(),
                        GTValues.V[GTValues.LV], GTValues.V[GTValues.LV], 1, GTValues.V[GTValues.LV], 1)));
                caps.put(IO.IN, ItemRecipeCapability.CAP, List.of(new NotifiableItemStackHandler(be.getMetaMachine(), 1,
                        IO.IN, IO.IN, (slots) -> new CustomItemStackHandler(silktouchDrop))));
                caps.put(IO.OUT, ItemRecipeCapability.CAP,
                        List.of(new NotifiableItemStackHandler(be.getMetaMachine(), 2, IO.OUT)));
                be.getMetaMachine().reinitializeCapabilities(caps);

                Iterator<GTRecipe> hammerRecipes = GTRecipeTypes.FORGE_HAMMER_RECIPES.searchRecipe(be.metaMachine);
                GTRecipe hammerRecipe = hammerRecipes == null || !hammerRecipes.hasNext() ? null : hammerRecipes.next();
                if (hammerRecipe != null && hammerRecipe.handleRecipeIO(IO.IN, be.metaMachine)) {
                    drops.clear();
                    TagPrefix prefix = ChemicalHelper.getPrefix(silktouchDrop.getItem());
                    if (prefix == null) {
                        for (Content output : hammerRecipe.getOutputContents(ItemRecipeCapability.CAP)) {
                            if (dropChance >= 1.0F || random.nextFloat() <= dropChance) {
                                drops.add(ItemRecipeCapability.CAP
                                        .copyInner(ItemRecipeCapability.CAP.of(output.content)).getItems()[0]);
                            }
                        }
                    } else if (TagPrefix.ORES.containsKey(prefix)) {
                        for (Content content : hammerRecipe.getOutputContents(ItemRecipeCapability.CAP)) {
                            if (dropChance >= 1.0F || random.nextFloat() <= dropChance) {
                                ItemStack output = ItemRecipeCapability.CAP.of(content.content).getItems()[0];
                                // Only apply fortune on ore -> crushed forge hammer recipes
                                if (ChemicalHelper.getPrefix(output.getItem()) == TagPrefix.crushed) {
                                    output = output.copy();
                                    if (fortune > 0) output.grow(random.nextInt(fortune));
                                    drops.add(output);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static final ThreadLocal<Boolean> isAoeBreakingBlocks = ThreadLocal.withInitial(() -> false);

    public static boolean breakBlockRoutine(ServerPlayer player, ItemStack tool, BlockPos pos, boolean playSound) {
        // This is *not* a vanilla/forge convention, Forge never added "shears" to ItemShear's tool classes.
        if (isTool(tool, GTToolType.SHEARS) && shearBlockRoutine(player, tool, pos) == 0) {
            return false;
        }
        Level world = player.level();

        isAoeBreakingBlocks.set(true);
        boolean canBreak = onBlockBreakEvent(world, player.gameMode.getGameModeForPlayer(), player, pos);
        if (!canBreak) {
            isAoeBreakingBlocks.set(false);
            return false;
        } else {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            BlockEntity tile = world.getBlockEntity(pos);
            if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
                world.sendBlockUpdated(pos, state, state, 3);
                return false;
            } else if (player.blockActionRestricted(world, pos, player.gameMode.getGameModeForPlayer())) {
                return false;
            } else if (player.isCreative()) {
                return removeBlockRoutine(state, world, player, pos, playSound);
            } else {
                world.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                boolean successful = removeBlockRoutine(state, world, player, pos, playSound);

                ItemStack copiedTool = tool.copy();
                boolean canHarvest = player.hasCorrectToolForDrops(state);
                tool.mineBlock(world, state, pos, player);
                if (tool.isEmpty() && !copiedTool.isEmpty()) {
                    onPlayerDestroyItem(player, copiedTool, InteractionHand.MAIN_HAND);
                }

                if (successful && canHarvest) {
                    block.playerDestroy(world, player, pos, state, tile, copiedTool);
                }

                isAoeBreakingBlocks.set(false);
                return successful;
            }
        }
    }

    public static boolean onBlockBreakEvent(Level level, GameType gameType, ServerPlayer player, BlockPos pos) {
        return !CommonHooks.fireBlockBreak(level, gameType, player, pos, level.getBlockState(pos)).isCanceled();
    }

    public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {
        EventHooks.onPlayerDestroyItem(player, stack, hand);
    }

    public static double getPlayerBlockReach(@NotNull Player player) {
        return player.blockInteractionRange();
    }

    public static boolean removeBlockRoutine(@Nullable BlockState state, Level world, ServerPlayer player, BlockPos pos,
                                             boolean playSound) {
        state = state == null ? world.getBlockState(pos) : state;
        state.getBlock().playerWillDestroy(world, pos, state, player);

        boolean successful = world.removeBlock(pos, false);

        if (playSound)
            world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));

        if (successful) {
            state.getBlock().destroy(world, pos, state);
        }
        return successful;
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, Level world, Player player,
                                                     HitResult rayTraceResult) {
        return getHarvestableBlocks(stack, getAoEDefinition(stack), world, player, rayTraceResult);
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, Player player) {
        if (!hasBehaviorsComponent(stack)) return Collections.emptySet();

        AoESymmetrical aoeDefiniton = getAoEDefinition(stack);
        if (aoeDefiniton == AoESymmetrical.none()) {
            return Collections.emptySet();
        }

        HitResult rayTraceResult = getPlayerDefaultRaytrace(player);
        return getHarvestableBlocks(stack, aoeDefiniton, player.level(), player, rayTraceResult);
    }

    public static HitResult getPlayerDefaultRaytrace(@NotNull Player player) {
        return player.pick(getPlayerBlockReach(player), 1.0f, false);
    }

    /**
     * Can be called to do a default set of "successful use" actions.
     * Damages the item, plays the tool sound (if available), and swings the player's arm.
     *
     * @param player the player clicking the item
     * @param world  the world in which the click happened
     * @param hand   the hand holding the item
     */
    public static void onActionDone(@NotNull Player player, @NotNull Level world, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IGTTool tool = (IGTTool) stack.getItem();
        ToolHelper.damageItem(stack, player);
        if (tool.getSound() != null) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), tool.getSound().getMainEvent(),
                    SoundSource.PLAYERS, 1.0F,
                    1.0F);
        }
        player.swing(hand);
    }

    public static Set<GTToolType> getToolTypes(ItemStack tool) {
        Set<GTToolType> types = new HashSet<>();
        if (tool.getItem() instanceof IGTTool gtTool) {
            return gtTool.getToolClasses(tool);
        }
        for (GTToolType toolType : GTToolType.getTypes().values()) {
            if (toolType.itemTags.stream().anyMatch(tool::is)) types.add(toolType);
        }
        return types;
    }

    /**
     * @return if any of the specified tool classes exists in the tool
     */
    public static boolean isTool(ItemStack tool, GTToolType... toolClasses) {
        for (GTToolType toolType : toolClasses) {
            if (toolType.itemTags.stream().anyMatch(tool::is)) return true;
        }

        if (tool.getItem() instanceof IGTTool igtTool) {
            if (toolClasses.length == 1) {
                return igtTool.getToolClasses(tool).contains(toolClasses[0]);
            }
            for (GTToolType toolClass : igtTool.getToolClasses(tool)) {
                for (GTToolType specified : toolClasses) {
                    if (toolClass.equals(specified)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // encompasses all vanilla special case tool checks for harvesting
    public static boolean isToolEffective(ItemStack stack, BlockState state, Set<GTToolType> toolClasses,
                                          int harvestLevel) {
        Block block = state.getBlock();

        if (toolClasses.contains(GTToolType.PICKAXE)) {
            if (Blocks.OBSIDIAN == block && harvestLevel >= 3) return true;
            if (state.is(BlockTags.NEEDS_IRON_TOOL) && harvestLevel >= 2) return true;
            if (state.is(BlockTags.NEEDS_STONE_TOOL) && harvestLevel >= 1) return true;
            if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return true;
        }
        if (toolClasses.contains(GTToolType.SHOVEL)) {
            if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return true;
            if (block == Blocks.SNOW_BLOCK || block == Blocks.SNOW) return true;
        }
        if (toolClasses.contains(GTToolType.AXE)) {
            if (state.is(BlockTags.MINEABLE_WITH_AXE)) return true;
        }
        if (toolClasses.contains(GTToolType.SWORD)) {
            if (block instanceof WebBlock) return true;
        }
        if (toolClasses.contains(GTToolType.SCYTHE)) {}
        if (toolClasses.contains(GTToolType.FILE)) {
            if (block instanceof IronBarsBlock) {
                return true;
            }
        }
        if (toolClasses.contains(GTToolType.CROWBAR)) {
            return block instanceof BaseRailBlock;
        }
        return stack.get(DataComponents.TOOL).isCorrectForDrops(state);
    }

    /**
     * Damages tools in a context where the tool had been used to craft something.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     */
    public static void damageItemWhenCrafting(@NotNull ItemStack stack, @Nullable LivingEntity entity) {
        int damage = 2;
        if (stack.getItem() instanceof IGTTool) {
            damage = ((IGTTool) stack.getItem()).getToolStats().getToolDamagePerCraft(stack);
        } else {
            if (stack.getTags().anyMatch(s -> s.location().getPath().startsWith("tool") ||
                    s.location().getPath().startsWith("crafting_tool"))) {
                damage = 1;
            }
        }
        damageItem(stack, entity, damage);
    }

    /**
     * Damages tools appropriately.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool.
     * <p>
     * This method only takes 1 durability off, it ignores the tool's effectiveness because of the lack of context.
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     */
    public static void damageItem(@NotNull ItemStack stack, @Nullable LivingEntity entity) {
        damageItem(stack, entity, 1);
    }

    /**
     * Special cases for vanilla destroy speed changes.
     * If return -1, no special case was found, and some other method
     * should be used to determine the destroy speed.
     */
    public static float getDestroySpeed(BlockState state, Set<GTToolType> toolClasses) {
        if (toolClasses.contains(GTToolType.SWORD)) {
            Block block = state.getBlock();
            if (block instanceof WebBlock) {
                return 15.0F;
            }
        }
        return -1;
    }

    /**
     * Shearing a Block.
     *
     * @return -1 if not shearable, otherwise return 0 or 1, 0 if tool is now broken.
     */
    public static int shearBlockRoutine(ServerPlayer player, ItemStack tool, BlockPos pos) {
        if (!player.isCreative()) {
            Level world = player.serverLevel();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof IShearable shearable) {
                if (shearable.isShearable(player, tool, world, pos)) {
                    List<ItemStack> shearedDrops = shearable.onSheared(player, tool, world, pos);
                    boolean relocateMinedBlocks = tool.has(GTDataComponents.RELOCATE_MINED_BLOCKS);
                    Iterator<ItemStack> iter = shearedDrops.iterator();
                    while (iter.hasNext()) {
                        ItemStack stack = iter.next();
                        if (relocateMinedBlocks && player.addItem(stack)) {
                            iter.remove();
                        } else {
                            float f = 0.7F;
                            double xo = world.random.nextFloat() * f + 0.15D;
                            double yo = world.random.nextFloat() * f + 0.15D;
                            double zo = world.random.nextFloat() * f + 0.15D;
                            ItemEntity entityItem = new ItemEntity(world, pos.getX() + xo, pos.getY() + yo,
                                    pos.getZ() + zo, stack);
                            entityItem.setDefaultPickUpDelay();
                            world.addFreshEntity(entityItem);
                        }
                    }
                    ToolHelper.damageItem(tool, player, 1);
                    player.awardStat(Stats.BLOCK_MINED.get((Block) shearable));
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                    return tool.isEmpty() ? 0 : 1;
                }
            }
        }
        return -1;
    }

    // Suppliers for broken tool stacks
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_LV = () -> GTItems.POWER_UNIT_LV.get()
            .getDefaultInstance();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_MV = () -> GTItems.POWER_UNIT_MV.get()
            .getDefaultInstance();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_HV = () -> GTItems.POWER_UNIT_HV.get()
            .getDefaultInstance();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_EV = () -> GTItems.POWER_UNIT_EV.get()
            .getDefaultInstance();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_IV = () -> GTItems.POWER_UNIT_IV.get()
            .getDefaultInstance();

    /**
     * @param state the BlockState of the block
     * @return the silk touch drop
     */
    @NotNull
    public static List<ItemStack> getSilkTouchDrop(ServerLevel world, BlockPos origin, @NotNull BlockState state) {
        ItemStack tool = GTItems.TOOL_ITEMS.get(GTMaterials.Neutronium, GTToolType.PICKAXE).get().get();
        tool.enchant(
                world.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH),
                1);

        return state.getDrops(new LootParams.Builder(world).withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(origin))
                .withParameter(LootContextParams.TOOL, tool));
    }
}
