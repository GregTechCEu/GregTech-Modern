package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviors;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AllArgsConstructor
public class TorchPlaceBehavior implements IToolBehavior<TorchPlaceBehavior> {

    public static final TorchPlaceBehavior INSTANCE = new TorchPlaceBehavior();
    public static final Codec<TorchPlaceBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("cache_slot_key", false).forGetter(val -> val.cacheSlotKey),
            Codec.INT.optionalFieldOf("cached_torch_slot", 0).forGetter(val -> val.cachedTorchSlot))
            .apply(instance, TorchPlaceBehavior::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TorchPlaceBehavior> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, val -> val.cacheSlotKey,
            ByteBufCodecs.VAR_INT, val -> val.cachedTorchSlot,
            TorchPlaceBehavior::new);

    private final boolean cacheSlotKey;
    private final int cachedTorchSlot;

    protected TorchPlaceBehavior() {
        cacheSlotKey = false;
        cachedTorchSlot = 0;
    }

    @Override
    public @NotNull InteractionResult onItemUse(UseOnContext context) {
        Player player = context.getPlayer();

        ItemStack slotStack;
        if (this.cacheSlotKey) {
            if (cachedTorchSlot < 0) {
                slotStack = player.getInventory().offhand.get(0);
            } else {
                slotStack = player.getInventory().items.get(cachedTorchSlot);
            }
            if (checkAndPlaceTorch(context, slotStack)) {
                return InteractionResult.SUCCESS;
            }
        }
        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            slotStack = player.getInventory().offhand.get(i);
            if (checkAndPlaceTorch(context, slotStack)) {
                final int finalI = i;
                slotStack.update(GTDataComponents.TOOL_BEHAVIORS, ToolBehaviors.EMPTY,
                        val -> val.withBehavior(new TorchPlaceBehavior(this.cacheSlotKey, -(finalI + 1))));
                return InteractionResult.SUCCESS;
            }
        }
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            slotStack = player.getInventory().items.get(i);
            if (checkAndPlaceTorch(context, slotStack)) {
                final int finalI = i;
                slotStack.update(GTDataComponents.TOOL_BEHAVIORS, ToolBehaviors.EMPTY,
                        val -> val.withBehavior(new TorchPlaceBehavior(this.cacheSlotKey, finalI)));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean checkAndPlaceTorch(UseOnContext context, ItemStack slotStack) {
        if (slotStack.isEmpty())
            return false;

        Item slotItem = slotStack.getItem();

        if (slotItem != Items.TORCH && !slotStack.is(TagUtil.createItemTag("torches")))
            return false;

        if (context.getPlayer() == null)
            return false;

        if (!(slotItem instanceof BlockItem slotItemBlock)) {
            return false;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = context.getLevel().getBlockState(pos);

        if (!state.canBeReplaced()) {
            pos = pos.relative(context.getClickedFace());
        }

        if (context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), slotStack)) {
            var torchContext = new UseOnContext(context.getLevel(), context.getPlayer(), context.getHand(), slotStack,
                    context.getHitResult());
            var blockPlaceContext = new BlockPlaceContext(torchContext);
            InteractionResult placed = slotItemBlock.place(blockPlaceContext);
            boolean wasPlaced = placed.consumesAction();
            if (wasPlaced) {
                SoundType sound = slotItemBlock.getBlock().getSoundType(slotItemBlock.getBlock().defaultBlockState(), context.getLevel(), pos, context.getPlayer());
                context.getLevel().playSound(context.getPlayer(), pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                        (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            }
            return wasPlaced;
        }
        return false;
    }

    @Override
    public ToolBehaviorType<TorchPlaceBehavior> getType() {
        return GTToolBehaviors.TORCH_PLACE;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.torch_place"));
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TorchPlaceBehavior that))
            return false;

        return cacheSlotKey == that.cacheSlotKey && cachedTorchSlot == that.cachedTorchSlot;
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(cacheSlotKey);
        result = 31 * result + cachedTorchSlot;
        return result;
    }
}
