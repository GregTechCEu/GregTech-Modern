package com.gregtechceu.gtceu.api.item.tool;

import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.item.GTToolItem;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.utils.RayTraceHelper;
import com.simibubi.create.content.decoration.palettes.GlassPaneBlock;
import dev.architectury.injectables.annotations.ExpectPlatform;
import io.github.fabricators_of_create.porting_lib.extensions.extensions.IShearable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote ToolHelper
 */
public class ToolHelper {

    public static final String TOOL_TAG_KEY = "gtceu:tool";
    public static final String BEHAVIOURS_TAG_KEY = "gtceu:behaviours";

    // Base item keys

    // Electric item keys
    public static final String MAX_CHARGE_KEY = "MaxCharge";
    public static final String CHARGE_KEY = "Charge";

    // Vanilla keys
    public static final String UNBREAKABLE_KEY = "Unbreakable";
    public static final String HIDE_FLAGS = "HideFlags";

    // Misc keys
    public static final String DISALLOW_CONTAINER_ITEM_KEY = "DisallowContainerItem";

    // Keys that resides in tool tag
    public static final String MATERIAL_KEY = "Material";
    public static final String DURABILITY_KEY = "Durability";
    public static final String MAX_DURABILITY_KEY = "MaxDurability";
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


    public static CompoundTag getToolTag(ItemStack stack) {
        return stack.getOrCreateTagElement(TOOL_TAG_KEY);
    }

    public static CompoundTag getBehaviorsTag(ItemStack stack) {
        return stack.getOrCreateTagElement(BEHAVIOURS_TAG_KEY);
    }

    public static ItemStack get(GTToolType toolType, Material material) {
        if (material.hasProperty(PropertyKey.TOOL)) {
            var entry = GTItems.TOOL_ITEMS.get(material.getToolTier(), toolType);
            if (entry != null) {
                return entry.asStack();
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

    public static void damageItem(@Nonnull ItemStack stack, RandomSource random, @Nullable ServerPlayer user) {
        if (canUse(stack)) {
            stack.hurt(1, random, user);
        } else {
            stack.shrink(1);
        }
    }

    public static void playToolSound(GTToolType toolType, ServerPlayer player) {
        if (toolType.soundEntry != null) {
            toolType.soundEntry.playOnServer(player.level(), player.blockPosition());
        }
    }

    public static List<BlockPos> getAOEPositions(LivingEntity miner, ItemStack stack, BlockPos pos, int radius) {

        Level level = miner.level();

        ArrayList<BlockPos> aoePositions = new ArrayList<>();
        ArrayList<BlockPos> potentialPositions = new ArrayList<>();


        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z = -radius; z <= radius; z++) {
                    potentialPositions.add(new BlockPos(x, y, z));
                }
            }
        }

        Vec3 cameraPos = miner.getEyePosition(1);
        Vec3 rotation = miner.getViewVector(1);

        Vec3 combined = cameraPos.add(rotation.x * 4.5F, rotation.y * 4.5F, rotation.z * 4.5F);

        BlockHitResult result = level.clip(new ClipContext(cameraPos, combined, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, miner));


        for (BlockPos blockPos : potentialPositions) {

            switch (result.getDirection().getAxis()) {
                case X -> {
                    if(blockPos.getX() == 0) {
                        aoePositions.add(pos.offset(blockPos));
                    }
                }
                case Y -> {
                    if(blockPos.getY() == 0) {
                        aoePositions.add(pos.offset(blockPos));
                    }
                }
                case Z -> {
                    if(blockPos.getZ() == 0) {
                        aoePositions.add(pos.offset(blockPos));
                    }
                }
            }
        }

        return aoePositions;
    }

    public static boolean aoeCanBreak(ItemStack stack, Level level, BlockPos origin, BlockPos pos) {
        if (origin.equals(pos)) return false;
        if (!stack.isCorrectToolForDrops(level.getBlockState(pos))) return false;

        BlockState state = level.getBlockState(pos);
        BlockState originState = level.getBlockState(origin);

        //Adapted from GTCEU 1.12.2, used to stop mining blocks like obsidian faster by targeting neighbouring stone. The value 8 is an arbitrary and does not represent anything.
        if (state.getDestroySpeed(level, pos) < 0 || state.getDestroySpeed(level, pos) - originState.getDestroySpeed(level, pos) > 8) return false;

        return true;
    }

    /**
     * @return if any of the specified tool classes exists in the tool
     */
    public static boolean isTool(ItemStack tool, GTToolType... toolClasses) {
        if (!(tool.getItem() instanceof IGTTool igtTool)) return false;
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
        return false;
    }

    // encompasses all vanilla special case tool checks for harvesting
    public static boolean isToolEffective(BlockState state, Set<GTToolType> toolClasses, int harvestLevel) {
        Block block = state.getBlock();
        if (toolClasses.stream().anyMatch(type -> state.is(type.harvestTag))) {
            return getHarvestLevel(state) <= harvestLevel;
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
            return block instanceof RailBlock;
        }
        return false;
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
                    ToolHelper.damageItem(tool, world.random, player);
                    player.awardStat(Stats.BLOCK_MINED.get((Block) shearable));
                    world.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                    return tool.isEmpty() ? 0 : 1;
                }
            }
        }
        return -1;
    }

    public static int getHarvestLevel(BlockState state) {
        for (int i = 0; i < CustomTags.TOOL_TIERS.length; ++i) {
            TagKey<Block> tag = CustomTags.TOOL_TIERS[i];
            if (state.is(tag)) {
                return i + 1;
            }
        }
        return 0;
    }
}
