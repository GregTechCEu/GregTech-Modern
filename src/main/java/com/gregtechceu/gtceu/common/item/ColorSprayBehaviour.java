package com.gregtechceu.gtceu.common.item;

import appeng.api.util.AEColor;
import appeng.blockentity.networking.CableBusBlockEntity;
import com.google.common.collect.ImmutableMap;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.lowdragmc.lowdraglib.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ColorSprayBehaviour
 */
public class ColorSprayBehaviour implements IDurabilityBar, IInteractionItem, IAddInformation {
    // vanilla
    private static final ImmutableMap<DyeColor, Block> GLASS_MAP;
    private static final ImmutableMap<DyeColor, Block> GLASS_PANE_MAP;
    private static final ImmutableMap<DyeColor, Block> TERRACOTTA_MAP;
    private static final ImmutableMap<DyeColor, Block> WOOL_MAP;
    private static final ImmutableMap<DyeColor, Block> CARPET_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_MAP;
    private static final ImmutableMap<DyeColor, Block> CONCRETE_POWDER_MAP;
    private static final ImmutableMap<DyeColor, Block> SHULKER_BOX_MAP;
    private static final ImmutableMap<DyeColor, Block> CANDLE_MAP;

    // mod support
    private static final ImmutableMap<DyeColor, Block> SEAT_MAP;

    private static ResourceLocation getId(String modid, DyeColor color, String postfix) {
        return new ResourceLocation(modid, "%s_%s".formatted(color.getSerializedName(), postfix));
    }

    static {
        ImmutableMap.Builder<DyeColor, Block> glassBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> glassPaneBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> terracottaBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> woolBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> carpetBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concreteBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> concretePowderBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> shulkerBoxBuilder = ImmutableMap.builder();
        ImmutableMap.Builder<DyeColor, Block> candleBuilder = ImmutableMap.builder();

        ImmutableMap.Builder<DyeColor, Block> seatBuilder = ImmutableMap.builder();

        for (DyeColor color : DyeColor.values()) {
            // if there are > 16 colors (vanilla end) & tinted is loaded, use tinted blocks
            if (color.ordinal() > 15 && Platform.isModLoaded(GTValues.MODID_TINTED)) {
                glassBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "stained_glass")));
                glassPaneBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "stained_glass_pane")));
                terracottaBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "terracotta")));
                woolBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "wool")));
                carpetBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "carpet")));
                concreteBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "concrete")));
                concretePowderBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "concrete_powder")));
                shulkerBoxBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "shulker_box")));
                candleBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_TINTED, color, "candle")));
            } else {
                glassBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "stained_glass")));
                glassPaneBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "stained_glass_pane")));
                terracottaBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "terracotta")));
                woolBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "wool")));
                carpetBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "carpet")));
                concreteBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "concrete")));
                concretePowderBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "concrete_powder")));
                shulkerBoxBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "shulker_box")));
                candleBuilder.put(color, BuiltInRegistries.BLOCK.get(getId("minecraft", color, "candle")));

                /* somehow didn't want to work, it seems registry isn't fully loaded yet (forge) so `BuiltInRegistries.BLOCK.getId` returns air for modded blocks
                if (GTCEu.isCreateLoaded()) {
                    seatBuilder.put(color, BuiltInRegistries.BLOCK.get(getId(GTValues.MODID_CREATE, color, "seat")));
                }
                 */
            }
        }
        GLASS_MAP = glassBuilder.build();
        GLASS_PANE_MAP = glassPaneBuilder.build();
        TERRACOTTA_MAP = terracottaBuilder.build();
        WOOL_MAP = woolBuilder.build();
        CARPET_MAP = carpetBuilder.build();
        CONCRETE_MAP = concreteBuilder.build();
        CONCRETE_POWDER_MAP = concretePowderBuilder.build();
        SHULKER_BOX_MAP = shulkerBoxBuilder.build();
        CANDLE_MAP = candleBuilder.build();

        SEAT_MAP = seatBuilder.build();
    }


    private final Supplier<ItemStack> empty;
    private final DyeColor color;
    public final int totalUses;
    private final Tuple<float[], float[]> durabilityBarColors;


    public ColorSprayBehaviour(Supplier<ItemStack> empty, int totalUses, int color) {
        this.empty = empty;
        DyeColor[] colors = DyeColor.values();
        this.color = color >= colors.length || color < 0 ? null : colors[color];
        // default to a gray color if this.color is null (like for solvent spray)
        int colorValue = this.color == null ? 0x969696 : this.color.getTextColor();
        this.totalUses = totalUses;
        this.durabilityBarColors = GradientUtil.getGradient(colorValue, 10);
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        return (float) getUsesLeft(stack) / totalUses;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, getDurabilityForDisplay(stack));
        return mixColors(f, durabilityBarColors.getA(), durabilityBarColors.getB());
    }

    private static int mixColors(float ratio, float[]... colors) {
        float r = 0, g = 0, b = 0;
        ratio = ratio * (1.0f / colors.length);
        for (float[] color : colors) {
            r += color[0] * ratio;
            g += color[1] * ratio;
            b += color[2] * ratio;
        }
        //noinspection PointlessBitwiseExpression
        return ((int)(r * 255) & 0xFF) << 16 |
                ((int)(g * 255) & 0xFF) << 8 |
                ((int)(b * 255) & 0xFF) << 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        int remainingUses = getUsesLeft(stack);
        if (color != null) {
            tooltipComponents.add(Component.translatable("behaviour.paintspray." + this.color.getSerializedName() + ".tooltip"));
        } else {
            tooltipComponents.add(Component.translatable("behaviour.paintspray.solvent.tooltip"));
        }
        tooltipComponents.add(Component.translatable("behaviour.paintspray.uses", remainingUses));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var facing = context.getClickedFace();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();
        if (player != null) {
            if (!tryPaintBlock(player, level, pos, facing)) {
                return InteractionResult.PASS;
            }
            useItemDurability(player, context.getHand(), stack, empty.get());
            GTSoundEntries.SPRAY_CAN_TOOL.play(level, null, player.position(), 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean tryPaintBlock(Player player, Level world, BlockPos pos, Direction side) {
        var blockState = world.getBlockState(pos);
        var block = blockState.getBlock();
        if (color == null) {
            return tryStripBlockColor(player, world, pos, block, side);
        }
        return recolorBlockState(world, pos, side, this.color) || tryPaintSpecialBlock(player, world, pos, block);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean recolorBlockState(Level level, BlockPos pos, Direction side, DyeColor color) {
        BlockState state = level.getBlockState(pos);
        for (Property property : state.getProperties()) {
            if (property.getValueClass() == DyeColor.class) {
                state.setValue(property, color);
                return true;
            }
        }
        return false;
    }

    private boolean tryPaintSpecialBlock(Player player, Level world, BlockPos pos, Block block) {
        if (block.defaultBlockState().is(CustomTags.GLASS_BLOCKS_BLOCK)) {
            if (recolorBlockNoState(GLASS_MAP, this.color, world, pos, Blocks.GLASS)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(CustomTags.GLASS_PANES_BLOCK)) {
            if (recolorBlockNoState(GLASS_PANE_MAP, this.color, world, pos, Blocks.GLASS_PANE)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.TERRACOTTA)) {
            if (recolorBlockNoState(TERRACOTTA_MAP, this.color, world, pos, Blocks.TERRACOTTA)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.WOOL)) {
            if (recolorBlockNoState(WOOL_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.WOOL_CARPETS)) {
            if (recolorBlockNoState(CARPET_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_BLOCK)) {
            if (recolorBlockNoState(CONCRETE_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_POWDER_BLOCK)) {
            if (recolorBlockNoState(CONCRETE_POWDER_MAP, this.color, world, pos)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.SHULKER_BOXES)) {
            if (recolorBlockNoState(SHULKER_BOX_MAP, this.color, world, pos, Blocks.SHULKER_BOX)) {
                return true;
            }
        }
        if (block.defaultBlockState().is(BlockTags.CANDLES)) {
            if (recolorBlockNoState(CANDLE_MAP, this.color, world, pos)) {
                return true;
            }
        }

        /* somehow didn't want to work
        if (GTCEu.isCreateLoaded() && block.defaultBlockState().is(CustomTags.CREATE_SEATS)) {
            if (recolorBlockNoState(SEAT_MAP, this.color, world, pos)) {
                return true;
            }
        }
         */

        // MTE special case
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof IMachineBlockEntity machineBe) {
            MetaMachine mte = machineBe.getMetaMachine();
            if (mte != null) {
                if (mte.getPaintingColor() != this.color.getTextColor()) {
                    mte.setPaintingColor(this.color.getTextColor());
                    return true;
                } else return false;
            }
        }

        // PipeBlockEntity special case
        if (be instanceof PipeBlockEntity<?, ?> pipe) {
            if (pipe.getPaintingColor() != this.color.getTextColor()) {
                pipe.setPaintingColor(this.color.getTextColor());
                return true;
            } else return false;
        }

        if (GTCEu.isAE2Loaded()) {
            if (be instanceof CableBusBlockEntity cable) {
                // do not try to recolor if it already is this color
                if (cable.getColor().ordinal() != color.ordinal()) {
                    cable.recolourBlock(null, AEColor.values()[color.ordinal()], player);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, DyeColor color, Level world, BlockPos pos) {
        return recolorBlockNoState(map, color, world, pos, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean recolorBlockNoState(Map<DyeColor, Block> map, DyeColor color, Level world, BlockPos pos, Block _default) {
        Block newBlock = map.getOrDefault(color, _default);
        BlockState old = world.getBlockState(pos);
        if (newBlock == Blocks.AIR) newBlock = _default;
        if (newBlock != null && newBlock != old.getBlock()) {
            BlockState state = newBlock.defaultBlockState();
            for (Property property : old.getProperties()) {
                state.setValue(property, old.getValue(property));
            }
            world.setBlock(pos, state, 3);
            return true;
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean tryStripBlockColor(Player player, Level world, BlockPos pos, Block block, Direction side) {
        // MC special cases
        if (block instanceof StainedGlassBlock) {
            world.setBlock(pos, Blocks.GLASS.defaultBlockState(), 3);
            return true;
        }
        if (block instanceof StainedGlassPaneBlock) {
            world.setBlock(pos, Blocks.GLASS_PANE.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.TERRACOTTA) && block != Blocks.TERRACOTTA) {
            world.setBlock(pos, Blocks.TERRACOTTA.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.WOOL) && block != Blocks.WHITE_WOOL) {
            world.setBlock(pos, Blocks.WHITE_WOOL.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.WOOL_CARPETS) && block != Blocks.WHITE_CARPET) {
            world.setBlock(pos, Blocks.WHITE_CARPET.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_BLOCK) && block != Blocks.WHITE_CONCRETE) {
            world.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(CustomTags.CONCRETE_POWDER_BLOCK) && block != Blocks.WHITE_CONCRETE_POWDER) {
            world.setBlock(pos, Blocks.WHITE_CONCRETE_POWDER.defaultBlockState(), 3);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.SHULKER_BOXES) && block != Blocks.SHULKER_BOX) {
            recolorBlockNoState(SHULKER_BOX_MAP, null, world, pos, Blocks.SHULKER_BOX);
            return true;
        }
        if (block.defaultBlockState().is(BlockTags.CANDLES) && block != Blocks.WHITE_CANDLE) {
            recolorBlockNoState(CANDLE_MAP, DyeColor.WHITE, world, pos);
            return true;
        }

        /* somehow didn't want to work
        if (GTCEu.isCreateLoaded() && block.defaultBlockState().is(CustomTags.CREATE_SEATS)) {
            if (recolorBlockNoState(SEAT_MAP, DyeColor.WHITE, world, pos, AllBlocks.SEATS.get(DyeColor.WHITE).get())) {
                return true;
            }
        }
         */

        // MTE special case
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof IMachineBlockEntity machineBe) {
            MetaMachine mte = machineBe.getMetaMachine();
            if (mte != null) {
                if (mte.isPainted()) {
                    mte.setPaintingColor(mte.getDefaultPaintingColor());
                    return true;
                } else return false;
            }
        }

        // PipeBlockEntity special case
        if (be instanceof PipeBlockEntity<?, ?> pipe) {
            if (pipe.isPainted()) {
                pipe.setPaintingColor(pipe.getDefaultPaintingColor());
                return true;
            } else return false;
        }

        // AE2 cable special case
        if (GTCEu.isAE2Loaded()) {
            if (be instanceof CableBusBlockEntity cable) {
                // do not try to strip color if it is already colorless
                if (cable.getColor() != AEColor.TRANSPARENT) {
                    cable.recolourBlock(null, AEColor.TRANSPARENT, player);
                    return true;
                } else return false;
            }
        }

        // General case
        BlockState state = world.getBlockState(pos);
        for (Property prop : state.getProperties()) {
            if (prop.getValueClass() == DyeColor.class) {
                BlockState defaultState = block.defaultBlockState();
                DyeColor defaultColor = DyeColor.WHITE;
                try {
                    // try to read the default color value from the default state instead of just
                    // blindly setting it to default state, and potentially resetting other values
                    defaultColor = (DyeColor) defaultState.getValue(prop);
                } catch (IllegalArgumentException ignored) {
                    // no default color, we may have to fallback to WHITE here
                    // other mods that have custom behavior can be done as
                    // special cases above on a case-by-case basis
                }
                recolorBlockState(world, pos, side, defaultColor);
                return true;
            }
        }

        return false;
    }

    public boolean useItemDurability(Player player, InteractionHand hand, ItemStack stack, ItemStack replacementStack) {
        int usesLeft = getUsesLeft(stack);
        if (!player.isCreative()) {
            if (--usesLeft <= 0) {
                if (replacementStack.isEmpty()) {
                    //if replacement stack is empty, just shrink resulting stack
                    stack.shrink(1);
                } else {
                    //otherwise, update held item to replacement stack
                    player.setItemInHand(hand, replacementStack);
                }
                return true;
            }
            setUsesLeft(stack, usesLeft);
        }
        return true;
    }

    public final int getUsesLeft(ItemStack stack) {
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains("UsesLeft", Tag.TAG_INT))
            return totalUses;
        return tagCompound.getInt("UsesLeft");
    }

    public static void setUsesLeft(ItemStack itemStack, int usesLeft) {
        CompoundTag tagCompound = itemStack.getOrCreateTag();
        tagCompound.putInt("UsesLeft", usesLeft);
    }
}
