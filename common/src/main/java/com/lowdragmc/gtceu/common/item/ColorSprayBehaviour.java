package com.lowdragmc.gtceu.common.item;

import com.lowdragmc.gtceu.api.item.component.IAddInformation;
import com.lowdragmc.gtceu.api.item.component.IDurabilityBar;
import com.lowdragmc.gtceu.api.item.component.IInteractionItem;
import com.lowdragmc.gtceu.utils.GradientUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote ColorSprayBehaviour
 */
public class ColorSprayBehaviour implements IDurabilityBar, IInteractionItem, IAddInformation {
    private final Supplier<ItemStack> empty;
    private final DyeColor color;
    private final int solventColor;

    public ColorSprayBehaviour(Supplier<ItemStack> empty, int totalUses, int color) {
        this.empty = empty;
        DyeColor[] colors = DyeColor.values();
        this.color = color >= colors.length || color < 0 ? null : colors[color];
        // default to a gray color if this.color is null (like for solvent spray)
        int colorValue = this.color == null ? 0x969696 : this.color.getTextColor();
        this.solventColor = color;
//        this.durabilityBarColors = GradientUtil.getGradient(colorValue, 10);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return IDurabilityBar.super.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return color == null ? solventColor  : color.getTextColor();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        int remainingUses = stack.getMaxDamage() - stack.getDamageValue();
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
        // TODO SPRAY
        if (player != null && stack.getDamageValue() < stack.getMaxDamage()) {
//            if (!tryPaintBlock(player, level, pos, facing)) {
//                return InteractionResult.PASS;
//            }
            stack.setDamageValue(stack.getDamageValue() + 1);
            if (stack.getDamageValue() == stack.getMaxDamage()) {
                player.setItemInHand(context.getHand(), empty.get());
            }
//            level.playSound(null, player.position().x, player.position().y, player.position().z, GTSoundEvents.SPRAY_CAN_TOOL, SoundCategory.PLAYERS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

//    private boolean tryPaintBlock(Player player, Level world, BlockPos pos, Direction side) {
//        var blockState = world.getBlockState(pos);
//        var block = blockState.getBlock();
//        if (color == null) {
//            return tryStripBlockColor(player, world, pos, block, side);
//        }
//        return block.recolorBlock(world, pos, side, this.color) || tryPaintSpecialBlock(player, world, pos, block);
//    }
//
//    private static boolean tryStripBlockColor(Player player, Level world, BlockPos pos, Block block, Direction side) {
//        // MC special cases
//        if (block instanceof StainedGlassBlock) {
//            world.setBlock(pos, Blocks.TINTED_GLASS);
//            return true;
//        }
//        if (block instanceof StainedGlassPaneBlock) {
//            world.setBlock(pos, Blocks.GLASS_PANE.getDefaultState());
//            return true;
//        }
//        if (block == Blocks.STAINED_HARDENED_CLAY) {
//            world.setBlockState(pos, Blocks.HARDENED_CLAY.getDefaultState());
//            return true;
//        }
//
//        // MTE special case
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof IGregTechTileEntity) {
//            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
//            if (mte != null) {
//                if (mte.isPainted()) {
//                    mte.setPaintingColor(-1);
//                    return true;
//                } else return false;
//            }
//        }
//
//        // TileEntityPipeBase special case
//        if (te instanceof IPipeTile) {
//            IPipeTile<?, ?> pipe = (IPipeTile<?, ?>) te;
//            if (pipe.isPainted()) {
//                pipe.setPaintingColor(-1);
//                return true;
//            } else return false;
//        }
//
//        // AE2 cable special case
//        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
//            if (te instanceof TileCableBus) {
//                TileCableBus cable = (TileCableBus) te;
//                // do not try to strip color if it is already colorless
//                if (cable.getColor() != AEColor.TRANSPARENT) {
//                    cable.recolourBlock(null, AEColor.TRANSPARENT, player);
//                    return true;
//                } else return false;
//            }
//        }
//
//        // General case
//        IBlockState state = world.getBlockState(pos);
//        for (IProperty prop : state.getProperties().keySet()) {
//            if (prop.getName().equals("color") && prop.getValueClass() == EnumDyeColor.class) {
//                IBlockState defaultState = block.getDefaultState();
//                EnumDyeColor defaultColor = EnumDyeColor.WHITE;
//                try {
//                    // try to read the default color value from the default state instead of just
//                    // blindly setting it to default state, and potentially resetting other values
//                    defaultColor = (EnumDyeColor) defaultState.getValue(prop);
//                } catch (IllegalArgumentException ignored) {
//                    // no default color, we may have to fallback to WHITE here
//                    // other mods that have custom behavior can be done as
//                    // special cases above on a case-by-case basis
//                }
//                block.recolorBlock(world, pos, side, defaultColor);
//                return true;
//            }
//        }
//
//        return false;
//    }
}
