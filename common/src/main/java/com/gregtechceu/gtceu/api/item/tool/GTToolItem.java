package com.gregtechceu.gtceu.api.item.tool;

import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.IGTTool;
import com.gregtechceu.gtceu.api.item.IItemUseFirst;
import com.gregtechceu.gtceu.api.sound.SoundEntry;
import com.gregtechceu.gtceu.client.renderer.item.ToolItemRenderer;
import com.lowdragmc.lowdraglib.Platform;
import com.mojang.datafixers.util.Pair;
import dev.architectury.injectables.annotations.ExpectPlatform;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author KilaBash
 * @date 2023/2/23
 * @implNote GTToolItem
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GTToolItem extends DiggerItem implements IItemUseFirst, IGTTool {

    @Getter
    protected final GTToolType toolType;
    @Getter
    protected final int electricTier;
    @Getter
    protected final Material material;
    @Getter
    private IGTToolDefinition toolStats;

    @ExpectPlatform
    public static GTToolItem create(GTToolType toolType, MaterialToolTier tier, Material material, int electricTier, IGTToolDefinition definition, Properties properties) {
        throw new AssertionError();
    }

    protected GTToolItem(GTToolType toolType, MaterialToolTier tier, Material material, int electricTier, IGTToolDefinition definition, Properties properties) {
        super(0, 0, tier, toolType.harvestTag, properties);
        this.toolType = toolType;
        this.material = material;
        this.electricTier = electricTier;
        this.toolStats = definition;
        if (Platform.isClient()) {
            ToolItemRenderer.create(this, toolType);
        }
    }

    @Override
    public MaterialToolTier getTier() {
        return (MaterialToolTier) super.getTier();
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return super.hasCraftingRemainingItem();
    }

    @Environment(EnvType.CLIENT)
    public static ItemColor tintColor() {
        return (itemStack, index) ->{
            if (itemStack.getItem() instanceof GTToolItem item) {
                return switch (index) {
                    case 0 -> {
                        if (item.toolType == GTToolType.CROWBAR) {
                            if (itemStack.hasTag() && itemStack.getTag().contains("tint_color", Tag.TAG_INT)) {
                                yield itemStack.getTag().getInt("tint_color");
                            }
                        }
                        yield -1;
                    }
                    case 1 -> item.getTier().material.getMaterialARGB();
                    default -> -1;
                };
            }
            return -1;
        };
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        var toolable = GTCapabilityHelper.getToolable(context.getLevel(), context.getClickedPos(), context.getClickedFace());
        if (toolable != null && ToolHelper.canUse(itemStack)) {
            var result = toolable.onToolClick(getToolType(), itemStack, context);
            if (result == InteractionResult.CONSUME && context.getPlayer() instanceof ServerPlayer serverPlayer) {
                ToolHelper.playToolSound(toolType, serverPlayer);

                if (!serverPlayer.isCreative()) {
                    ToolHelper.damageItem(itemStack, serverPlayer, 1);
                }
            }
            return result;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (this.toolType == GTToolType.SHOVEL) {
            return useShovelOn(context);
        } else if (this.toolType == GTToolType.AXE) {
            return useAxeOn(context);
        } else if (this.toolType == GTToolType.HOE) {
            return useHoeOn(context);
        }
        return InteractionResult.PASS;
    }

    public InteractionResult useShovelOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        } else {
            Player player = context.getPlayer();
            BlockState blockState2 = ShovelItem.FLATTENABLES.get(blockState.getBlock());
            BlockState blockState3 = null;
            if (blockState2 != null && level.getBlockState(blockPos.above()).isAir()) {
                level.playSound(player, blockPos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                blockState3 = blockState2;
            } else if (blockState.getBlock() instanceof CampfireBlock && blockState.getValue(CampfireBlock.LIT)) {
                if (!level.isClientSide()) {
                    level.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, blockPos, 0);
                }

                CampfireBlock.dowse(context.getPlayer(), level, blockPos, blockState);
                blockState3 = blockState.setValue(CampfireBlock.LIT, false);
            }

            if (blockState3 != null) {
                if (!level.isClientSide) {
                    level.setBlock(blockPos, blockState3, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState3));
                    if (player != null) {
                        context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    private Optional<BlockState> getStripped(BlockState unstrippedState) {
        return Optional.ofNullable(AxeItem.STRIPPABLES.get(unstrippedState.getBlock()))
                .map(block -> block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, unstrippedState.getValue(RotatedPillarBlock.AXIS)));
    }

    public InteractionResult useAxeOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState blockState = level.getBlockState(blockPos);
        Optional<BlockState> strippable = getStripped(blockState);
        Optional<BlockState> cleanable = WeatheringCopper.getPrevious(blockState);
        Optional<BlockState> waxable = Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(blockState.getBlock()))
                .map(block -> block.withPropertiesOf(blockState));
        ItemStack itemStack = context.getItemInHand();
        Optional<BlockState> result = Optional.empty();
        if (strippable.isPresent()) {
            level.playSound(player, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            result = strippable;
        } else if (cleanable.isPresent()) {
            level.playSound(player, blockPos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, blockPos, 0);
            result = cleanable;
        } else if (waxable.isPresent()) {
            level.playSound(player, blockPos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, blockPos, 0);
            result = waxable;
        }

        if (result.isPresent()) {
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockPos, itemStack);
            }

            level.setBlock(blockPos, result.get(), 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, result.get()));
            if (player != null) {
                itemStack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }


    public InteractionResult useHoeOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = HoeItem.TILLABLES.get(level.getBlockState(blockPos).getBlock());
        if (pair == null) {
            return InteractionResult.PASS;
        } else {
            Predicate<UseOnContext> predicate = pair.getFirst();
            Consumer<UseOnContext> consumer = pair.getSecond();
            if (predicate.test(context)) {
                Player player = context.getPlayer();
                level.playSound(player, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!level.isClientSide) {
                    consumer.accept(context);
                    if (player != null) {
                        context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    @Override
    public String getDescriptionId() {
        return toolType.getUnlocalizedName();
    }

    @Override
    public Component getDescription() {
        return Component.translatable(toolType.getUnlocalizedName(), getTier().material.getLocalizedName());
    }

    @Override
    public Component getName(ItemStack stack) {
        return this.getDescription();
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return definition$mineBlock(stack, level, state, pos, miningEntity);
        /*
        if (stack.is(CustomTags.TREE_FELLING_TOOLS) && state.is(BlockTags.LOGS)) {
            new TreeFellingHelper().fellTree(stack, level, state, pos, miningEntity);
        }
        return super.mineBlock(stack, level, state, pos, miningEntity);
         */
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
    public Set<GTToolType> getToolClasses(ItemStack stack) {
        return Set.of(this.toolType);
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


    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return definition$canApplyAtEnchantingTable(stack, enchantment);
    }

    public int getEnchantmentValue(ItemStack stack) {
        return getTotalEnchantability(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return definition$isValidRepairItem(stack, repairCandidate);
    }

    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return definition$getDefaultAttributeModifiers(slot, stack);
    }

    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return definition$canDisableShield(shield, shield, entity, attacker);
    }

    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return definition$doesSneakBypassUse(stack, level, pos, player);
    }

    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return definition$shouldCauseBlockBreakReset(oldStack, newStack);
    }

    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return definition$hasCraftingRemainingItem(stack);
    }

    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return definition$getCraftingRemainingItem(itemStack);
    }

    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return definition$shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    public boolean isDamaged(ItemStack stack) {
        return definition$isDamaged(stack);
    }

    public int getDamage(ItemStack stack) {
        return definition$getDamage(stack);
    }

    public int getMaxDamage(ItemStack stack) {
        return definition$getMaxDamage(stack);
    }

    public void setDamage(ItemStack stack, int damage) {
        definition$setDamage(stack, damage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return definition$use(level, player, usedHand);
    }


}
