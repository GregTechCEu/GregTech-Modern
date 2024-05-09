package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.datacomponents.ToolBehaviorsComponent;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.data.tools.GTToolBehaviors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import java.util.List;

// TODO this currently voids the used tool as well as a torch. how fix?
@AllArgsConstructor
public class TorchPlaceBehavior implements IToolBehavior<TorchPlaceBehavior> {
    public static final TorchPlaceBehavior INSTANCE = new TorchPlaceBehavior();
    public static final MapCodec<TorchPlaceBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("cache_slot_key", false).forGetter(val -> val.cacheSlotKey),
        Codec.INT.optionalFieldOf("cached_torch_slot", 0).forGetter(val -> val.cachedTorchSlot)
    ).apply(instance, TorchPlaceBehavior::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TorchPlaceBehavior> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, val -> val.cacheSlotKey,
        ByteBufCodecs.VAR_INT, val -> val.cachedTorchSlot,
        TorchPlaceBehavior::new
    );

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
                slotStack.update(GTDataComponents.TOOL_BEHAVIOURS, new ToolBehaviorsComponent(List.of()), val -> val.withBehavior(new TorchPlaceBehavior(this.cacheSlotKey, -(finalI + 1))));
                return InteractionResult.SUCCESS;
            }
        }
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            slotStack = player.getInventory().items.get(i);
            if (checkAndPlaceTorch(context, slotStack)) {
                final int finalI = i;
                slotStack.update(GTDataComponents.TOOL_BEHAVIOURS, new ToolBehaviorsComponent(List.of()), val -> val.withBehavior(new TorchPlaceBehavior(this.cacheSlotKey, finalI)));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean checkAndPlaceTorch(UseOnContext context, ItemStack slotStack) {
        if (!slotStack.isEmpty()) {
            Item slotItem = slotStack.getItem();
            if (slotItem instanceof BlockItem slotItemBlock) {
                Block slotBlock = slotItemBlock.getBlock();
                if (slotBlock == Blocks.TORCH ||
                        slotStack.is(TagUtil.createItemTag("torches"))) {
                    BlockPos pos = context.getClickedPos();
                    BlockState state = context.getLevel().getBlockState(pos);
                    if (!state.canBeReplaced()) {
                        pos = pos.relative(context.getClickedFace());
                    }
                    if (context.getPlayer().mayUseItemAt(pos, context.getClickedFace(), slotStack)) {
                        var blockPlaceContext = new BlockPlaceContext(context);
                        if (slotItemBlock.place(blockPlaceContext).consumesAction()) {
                            BlockState slotState = context.getLevel().getBlockState(pos);
                            SoundType soundtype = slotState.getSoundType(context.getLevel(), pos, context.getPlayer());
                            context.getLevel().playSound(context.getPlayer(), pos, soundtype.getPlaceSound(), SoundSource.BLOCKS,
                                    (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            if (!context.getPlayer().isCreative()) slotStack.shrink(1);
                            return true;
                        }
                    }
                }
            }
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