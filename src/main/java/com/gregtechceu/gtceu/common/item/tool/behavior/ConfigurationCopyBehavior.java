package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

public class ConfigurationCopyBehavior implements IToolBehavior, IInteractionItem {

    public static final ConfigurationCopyBehavior INSTANCE = new ConfigurationCopyBehavior();

    public ConfigurationCopyBehavior() {}

    @Override
    public void addBehaviorNBT(@NotNull ItemStack stack, @NotNull CompoundTag tag) {
        tag.putString("Configuration", "");
        IToolBehavior.super.addBehaviorNBT(stack, tag);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        CompoundTag tags = getBehaviorsTag(stack);
        if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
            if (!context.getLevel().getBlockState(context.getHitResult().getBlockPos()).getBlock().kjs$getMod()
                    .equals("gtceu")) {
                context.getPlayer()
                        .displayClientMessage(Component.translatable("This only works with GregTech machines"), true);

                return InteractionResult.PASS;
            }
            BlockEntity be = context.getLevel().getBlockEntity(context.getHitResult().getBlockPos());
            if (!(be instanceof MetaMachineBlockEntity mmbe)) {
                context.getPlayer()
                        .displayClientMessage(
                                Component.translatable("This only works with GregTech electrical machines"), true);
                return InteractionResult.PASS;
            }
            if (!(mmbe.metaMachine instanceof SimpleTieredMachine stm)) {
                context.getPlayer()
                        .displayClientMessage(Component.translatable("This only works with simple GregTech machines"),
                                true);
                return InteractionResult.PASS;

            }
            setCfg(tags, stm);
            context.getPlayer()
                    .displayClientMessage(Component.translatable("Copied source block configuration into tool."), true);

            tags.putString("Configuration", "isSet");

        } else {
            String cfg = tags.getString("Configuration");
            if (cfg.isEmpty()) {
                context.getPlayer()
                        .displayClientMessage(Component.translatable(
                                "Shift-click on a source machine to copy configuration into tool", cfg), true);

                return InteractionResult.PASS;
            }
            if (!context.getLevel().getBlockState(context.getHitResult().getBlockPos()).getBlock().kjs$getMod()
                    .equals("gtceu")) {
                context.getPlayer()
                        .displayClientMessage(Component.translatable("This only works with GregTech machines"), true);

                return InteractionResult.PASS;
            }
            BlockEntity be = context.getLevel().getBlockEntity(context.getHitResult().getBlockPos());
            if (!(be instanceof MetaMachineBlockEntity mmbe)) {
                context.getPlayer()
                        .displayClientMessage(
                                Component.translatable("This only works with GregTech electrical machines"), true);
                return InteractionResult.PASS;
            }
            if (!(mmbe.metaMachine instanceof SimpleTieredMachine stm)) {
                context.getPlayer()
                        .displayClientMessage(Component.translatable("This only works with simple GregTech machines"),
                                true);
                return InteractionResult.PASS;

            }

            setCfgOnTarget(tags, stm);
            context.getPlayer()
                    .displayClientMessage(Component.translatable("Pasted block configuration onto machine."), true);

        }
        return InteractionResult.CONSUME;
    }

    private void setCfg(CompoundTag tags, SimpleTieredMachine metaMachine) {
        tags.putString("outputFacingItems", metaMachine.getOutputFacingItems().toString());
        tags.putBoolean("autoOutputItems", metaMachine.hasAutoOutputItem());
        tags.putBoolean("allowInputFromOutputSideItems", metaMachine.isAllowInputFromOutputSideItems());
        tags.putString("outputFacingFluids", metaMachine.getOutputFacingFluids().toString());
        tags.putBoolean("autoOutputFluids", metaMachine.hasAutoOutputFluid());
        tags.putBoolean("allowInputFromOutputSideFluids", metaMachine.isAllowInputFromOutputSideFluids());
    }

    private void setCfgOnTarget(CompoundTag tags, SimpleTieredMachine m) {
        m.setOutputFacingItems(Direction.byName(tags.getString("outputFacingItems")));
        m.setAutoOutputItems(tags.getBoolean("autoOutputItems"));
        m.setAllowInputFromOutputSideItems(tags.getBoolean("allowInputFromOutputSideItems"));
        m.setOutputFacingFluids(Direction.byName(tags.getString("outputFacingFluids")));
        m.setAutoOutputFluids(tags.getBoolean("autoOutputFluids"));
        m.setAllowInputFromOutputSideFluids(tags.getBoolean("allowInputFromOutputSideFluids"));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> onItemRightClick(@NotNull Level world, @NotNull Player player,
                                                                        @NotNull InteractionHand hand) {
        return IToolBehavior.super.onItemRightClick(world, player, hand);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        var tagCompound = getBehaviorsTag(stack);
        tooltip.add(Component.translatable("metaitem.config_copy.display", tagCompound.getString("Configuration")));
    }
}
