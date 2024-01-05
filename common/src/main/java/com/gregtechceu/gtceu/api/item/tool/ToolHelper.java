package com.gregtechceu.gtceu.api.item.tool;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ToolProperty;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.tool.aoe.AoESymmetrical;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.simibubi.create.content.decoration.palettes.GlassPaneBlock;
import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.fabricators_of_create.porting_lib.extensions.extensions.IShearable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ToolHelper
 */
public class ToolHelper {

    public static final String TOOL_TAG_KEY = "GT.Tool";
    public static final String BEHAVIOURS_TAG_KEY = "GT.Behaviours";

    // Base item keys

    // Electric item keys
    public static final String MAX_CHARGE_KEY = "MaxCharge";
    public static final String CHARGE_KEY = "Charge";

    // Vanilla keys
    public static final String UNBREAKABLE_KEY = "Unbreakable";
    public static final String HIDE_FLAGS = "HideFlags";

    // Misc keys
    public static final String DISALLOW_CONTAINER_ITEM_KEY = "DisallowContainerItem";
    public static final String TINT_COLOR_KEY = "TintColor";

    // Keys that resides in tool tag
    public static final String DURABILITY_KEY = ItemStack.TAG_DAMAGE;
    public static final String MAX_DURABILITY_KEY = "MaxDamage";
    public static final String TOOL_SPEED_KEY = "ToolSpeed";
    public static final String ATTACK_DAMAGE_KEY = "AttackDamage";
    public static final String ATTACK_SPEED_KEY = "AttackSpeed";
    public static final String ENCHANTABILITY_KEY = "Enchantability";
    public static final String HARVEST_LEVEL_KEY = "HarvestLevel";
    public static final String LAST_CRAFTING_USE_KEY = "LastCraftingUse";

    // Keys that resides in behaviours tag

    // AoE
    public static final String MAX_AOE_COLUMN_KEY = "MaxAoEColumn";
    public static final String MAX_AOE_ROW_KEY = "MaxAoERow";
    public static final String MAX_AOE_LAYER_KEY = "MaxAoELayer";
    public static final String AOE_COLUMN_KEY = "AoEColumn";
    public static final String AOE_ROW_KEY = "AoERow";
    public static final String AOE_LAYER_KEY = "AoELayer";

    // Others
    public static final String HARVEST_ICE_KEY = "HarvestIce";
    public static final String TORCH_PLACING_KEY = "TorchPlacing";
    public static final String TORCH_PLACING_CACHE_SLOT_KEY = "TorchPlacing$Slot";
    public static final String TREE_FELLING_KEY = "TreeFelling";
    public static final String DISABLE_SHIELDS_KEY = "DisableShields";
    public static final String RELOCATE_MINED_BLOCKS_KEY = "RelocateMinedBlocks";


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
     * Registers the tool against a crafting symbol, this is used in {@link com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper}
     */
    public static void registerToolSymbol(Character symbol, GTToolType tool) {
        symbols.put(symbol, tool);
    }

    public static CompoundTag getToolTag(ItemStack stack) {
        return stack.getOrCreateTagElement(TOOL_TAG_KEY);
    }

    public static CompoundTag getBehaviorsTag(ItemStack stack) {
        return stack.getOrCreateTagElement(BEHAVIOURS_TAG_KEY);
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
        if (stack.getItem() instanceof GTToolItem item) {
            return item.getToolType() == toolType;
        }
        return false;
    }

    public static boolean canUse(ItemStack stack) {
        return stack.getDamageValue() <= stack.getMaxDamage();
    }

    public static void damageItem(@Nonnull ItemStack stack, @Nullable LivingEntity user, int damage) {
        if (!(stack.getItem() instanceof IGTTool tool)) {
            if (user != null) stack.hurtAndBreak(damage, user, p -> {});
        } else {
            if (stack.getTag() != null && stack.getTag().getBoolean(UNBREAKABLE_KEY)) {
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
                                random.nextInt(100) > ConfigHolder.INSTANCE.tools.rngDamageElectricTools) {
                            return;
                        }
                    } else {
                        throw new IllegalStateException("Electric tool does not have an attached electric item capability.");
                    }
                }
                int unbreakingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, stack);
                int negated = 0;
                for (int k = 0; unbreakingLevel > 0 && k < damage; k++) {
                    if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(stack, unbreakingLevel, random)) {
                        negated++;
                    }
                }
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

    public static ItemStack getAndSetToolData(GTToolType toolType, Material material, int maxDurability, int harvestLevel,
                                              float toolSpeed, float attackDamage) {
        var entry = GTItems.TOOL_ITEMS.get(material, toolType);
        if (entry == null) return ItemStack.EMPTY;
        ItemStack stack = entry.get().getRaw();
        stack.getOrCreateTag().putInt(HIDE_FLAGS, 2);
        CompoundTag toolTag = getToolTag(stack);
        toolTag.putInt(MAX_DURABILITY_KEY, maxDurability);
        toolTag.putInt(HARVEST_LEVEL_KEY, harvestLevel);
        toolTag.putFloat(TOOL_SPEED_KEY, toolSpeed);
        toolTag.putFloat(ATTACK_DAMAGE_KEY, attackDamage);
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
        if (toolProperty != null) {
            toolProperty.getEnchantments().forEach((enchantment, level) -> {
                if (entry.get().definition$canApplyAtEnchantingTable(stack, enchantment)) {
                    stack.enchant(enchantment, level);
                }
            });
        }
        return stack;
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
            for (BlockPos pos : harvestableBlocks) {
                if (!breakBlockRoutine(player, stack, pos)) {
                    return true;
                }

                remainingUses--;
                if (stack.getItem() instanceof IGTTool && !((IGTTool) stack.getItem()).isElectric() && remainingUses == 0) {
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

    public static AoESymmetrical getMaxAoEDefinition(ItemStack stack) {
        return AoESymmetrical.readMax(getBehaviorsTag(stack));
    }

    public static AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.read(getBehaviorsTag(stack), getMaxAoEDefinition(stack));
    }

    public static Set<BlockPos> iterateAoE(ItemStack stack, AoESymmetrical aoeDefinition, Level world,
                                           Player player, HitResult rayTraceResult,
                                           AOEFunction function) {
        if (aoeDefinition != AoESymmetrical.none() && rayTraceResult instanceof BlockHitResult blockHit && blockHit.getDirection() != null) {
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
                                if (player.mayUseItemAt(pos.relative(blockHit.getDirection()), blockHit.getDirection(), stack)) {
                                    if (function.apply(stack, world, player, pos, new UseOnContext(player.level(), player, player.getUsedItemHand(), stack, blockHit))) {
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
                                if (function.apply(stack, world, player, pos, new UseOnContext(player.level(), player, player.getUsedItemHand(), stack, blockHit))) {
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

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, Level world, Player player, HitResult rayTraceResult) {
        return iterateAoE(stack, aoeDefinition, world, player, rayTraceResult, ToolHelper::isBlockAoEHarvestable);
    }

    private static boolean isBlockAoEHarvestable(ItemStack stack, Level world, Player player, BlockPos pos, UseOnContext context) {
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
        return stack.getItem().isCorrectToolForDrops(state);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean breakBlockRoutine(ServerPlayer player, ItemStack tool, BlockPos pos) {
        // This is *not* a vanilla/forge convention, Forge never added "shears" to ItemShear's tool classes.
        if (isTool(tool, GTToolType.SHEARS) && shearBlockRoutine(player, tool, pos) == 0) {
            return false;
        }
        Level world = player.level();
        boolean canBreak = onBlockBreakEvent(world, player.gameMode.getGameModeForPlayer(), player, pos);
        if (!canBreak) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        BlockEntity tile = world.getBlockEntity(pos);
        if (block instanceof GameMasterBlock && !player.canUseGameMasterBlocks()) {
            world.sendBlockUpdated(pos, state, state, 3);
            return false;
        } else {
            world.levelEvent(player, 2001, pos, Block.getId(state));
            boolean successful;
            if (player.isCreative()) {
                successful = removeBlockRoutine(state, world, player, pos, false);
            } else {
                ItemStack copiedTool = tool.isEmpty() ? ItemStack.EMPTY : tool.copy();
                boolean canHarvest = player.hasCorrectToolForDrops(state);
                if (!tool.isEmpty()) {
                    tool.mineBlock(world, state, pos, player);
                    if (tool.isEmpty()) {
                        onPlayerDestroyItem(player, copiedTool, InteractionHand.MAIN_HAND);
                    }
                }

                successful = removeBlockRoutine(null, world, player, pos, canHarvest);
                if (successful && canHarvest) {
                    block.playerDestroy(world, player, pos, state, tile, copiedTool);
                }
            }
            return successful;
        }
    }

    @ExpectPlatform
    public static boolean onBlockBreakEvent(Level level, GameType gameType, ServerPlayer player, BlockPos pos) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void onPlayerDestroyItem(Player player, ItemStack stack, InteractionHand hand) {
        throw new AssertionError();
    }

    public static boolean removeBlockRoutine(@Nullable BlockState state, Level world, ServerPlayer player,
                                             BlockPos pos, boolean canHarvest) {
        state = state == null ? world.getBlockState(pos) : state;
        state.getBlock().playerWillDestroy(world, pos, state, player);

        boolean successful = world.destroyBlock(pos, canHarvest, player);
        if (successful) {
            player.getMainHandItem().mineBlock(world, state, pos, player);
            state.getBlock().destroy(world, pos, state);
        }
        return successful;
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, Level world, Player player,
                                                     HitResult rayTraceResult) {
        return getHarvestableBlocks(stack, getAoEDefinition(stack), world, player, rayTraceResult);
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, Player player) {
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

    @ExpectPlatform
    public static double getPlayerBlockReach(@NotNull Player player) {
        throw new AssertionError();
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
            world.playSound(null, player.getX(), player.getY(), player.getZ(), tool.getSound().getMainEvent(), SoundSource.PLAYERS, 1.0F,
                    1.0F);
        }
        player.swing(hand);
    }

    public static Set<GTToolType> getToolTypes(ItemStack tool) {
        Set<GTToolType> types = new HashSet<>();
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
    public static boolean isToolEffective(BlockState state, Set<GTToolType> toolClasses, int harvestLevel) {
        Block block = state.getBlock();
        if (toolClasses.stream().anyMatch(type -> type.harvestTags.stream().anyMatch(state::is))) {
            return isCorrectTierForDrops(state, harvestLevel);
        }

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
        if (toolClasses.contains(GTToolType.SCYTHE)) {
        }
        if (toolClasses.contains(GTToolType.FILE)) {
            if (block instanceof GlassPaneBlock) {
                return true;
            }
        }
        if (toolClasses.contains(GTToolType.CROWBAR)) {
            return block instanceof BaseRailBlock;
        }
        return false;
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
            if (stack.getTags().anyMatch(s -> s.location().getPath().startsWith("tool") || s.location().getPath().startsWith("crafting_tool"))) {
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
            /*
            if (state.getBlock() instanceof IShearable shearable) {
                if (shearable.isShearable(tool, world, pos)) {
                    List<ItemStack> shearedDrops = shearable.onSheared(player, tool, world, pos, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool));
                    boolean relocateMinedBlocks = getBehaviorsTag(tool).getBoolean(RELOCATE_MINED_BLOCKS_KEY);
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
                            ItemEntity entityItem = new ItemEntity(world, pos.getX() + xo, pos.getY() + yo, pos.getZ() + zo, stack);
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
            */
        }
        return -1;
    }

    @ExpectPlatform
    private static boolean isCorrectTierForDrops(BlockState state, int tier) {
        throw new AssertionError();
    }
}
