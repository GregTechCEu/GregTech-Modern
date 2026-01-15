package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.ResearchManager;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.UUID;

public class DataItemBehavior implements IInteractionItem, IAddInformation {

    public static final DataItemBehavior INSTANCE = new DataItemBehavior();

    protected DataItemBehavior() {}

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(usedHand);
            // stack.getOrCreateTag().putString("boundPlayerName",
            // Component.Serializer.toJson(player.getDisplayName()));
            int perm = 0;
            while (player.hasPermissions(perm)) perm++;
            // stack.getOrCreateTag().putInt("boundPlayerPermLevel", perm - 1);
            // stack.getOrCreateTag().putString("boundPlayerUUID", player.getStringUUID());
            stack.set(GTDataComponents.DATA_BOUND_PLAYER, new BoundPlayer(player.getUUID(), perm, player.getName()));
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        var boundPlayer = stack.getOrDefault(GTDataComponents.DATA_BOUND_PLAYER, null);
        if (boundPlayer != null) {
            MutableComponent name = MutableComponent.create(boundPlayer.displayName().getContents());
            tooltipComponents.add(Component.translatable("gtceu.tooltip.player_bind", name));
        }
        var target = stack.getOrDefault(GTDataComponents.MONITOR_TARGET, null);
        if (target != null) {
            tooltipComponents.add(Component.translatable(
                    "gtceu.tooltip.wireless_transmitter_bind",
                    Component.literal("" + target.getX()).withStyle(ChatFormatting.GOLD),
                    Component.literal("" + target.getY()).withStyle(ChatFormatting.GOLD),
                    Component.literal("" + target.getZ()).withStyle(ChatFormatting.GOLD),
                    Component.literal(stack.getOrDefault(GTDataComponents.MONITOR_TARGET_FACE, Direction.UP).getName())
                            .withStyle(ChatFormatting.DARK_PURPLE)));
        }
        var conf = stack.getOrDefault(GTDataComponents.MONITOR_COVER_CONFIG, null);
        if (conf != null) {
            tooltipComponents.add(Component.translatable("gtceu.tooltip.computer_monitor_config"));
        }
        var coverData = stack.getOrDefault(GTDataComponents.COMPUTER_MONITOR_DATA, null);
        if (coverData != null) {
            tooltipComponents.add(
                    Component.translatable("gtceu.tooltip.computer_monitor_data",
                            coverData));
        }
        ResearchManager.ResearchItem researchData = stack.get(GTDataComponents.RESEARCH_ITEM);
        if (researchData == null) {
            BlockPos pos = stack.get(GTDataComponents.DATA_COPY_POS);
            if (pos != null) {
                tooltipComponents.add(Component.translatable("gtceu.tooltip.proxy_bind",
                        makePosPart(pos.getX()), makePosPart(pos.getY()), makePosPart(pos.getZ())));
            }
        }
    }

    private static Component makePosPart(int coordinate) {
        return Component.literal(Integer.toString(coordinate)).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        ICoverable coverable = GTCapabilityHelper.getCoverable(context.getLevel(), context.getClickedPos(),
                context.getClickedFace());
        if (coverable != null &&
                coverable.getCoverAtSide(context.getClickedFace()) instanceof IDataStickInteractable interactable) {
            if (context.isSecondaryUseActive()) {
                if (!itemStack.has(GTDataComponents.RESEARCH_ITEM)) {
                    return interactable.onDataStickShiftUse(context.getPlayer(), itemStack);
                }
            } else {
                return interactable.onDataStickUse(context.getPlayer(), itemStack);
            }
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity blockEntity) {
            var machine = blockEntity.getMetaMachine();
            if (!MachineOwner.canOpenOwnerMachine(context.getPlayer(), machine)) {
                return InteractionResult.FAIL;
            }
            if (machine instanceof IDataStickInteractable interactable) {
                if (context.isSecondaryUseActive()) {
                    if (!itemStack.has(GTDataComponents.RESEARCH_ITEM)) {
                        return interactable.onDataStickShiftUse(context.getPlayer(), itemStack);
                    }
                } else {
                    return interactable.onDataStickUse(context.getPlayer(), itemStack);
                }
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    public static record BoundPlayer(UUID uuid, int perm, Component displayName) {

        public static Codec<BoundPlayer> CODEC = RecordCodecBuilder.<BoundPlayer>create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(val -> val.uuid),
                Codec.INT.fieldOf("perm").forGetter(val -> val.perm),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(val -> val.displayName))
                .apply(instance, BoundPlayer::new));
        public static StreamCodec<RegistryFriendlyByteBuf, BoundPlayer> STREAM_CODEC = StreamCodec.composite(
                UUIDUtil.STREAM_CODEC, BoundPlayer::uuid,
                ByteBufCodecs.INT, BoundPlayer::perm,
                ComponentSerialization.STREAM_CODEC, BoundPlayer::displayName,
                BoundPlayer::new);

        public BoundPlayer(UUID uuid, Integer perm, Component name) {
            this(uuid, perm.intValue(), name);
        }
    }
}
