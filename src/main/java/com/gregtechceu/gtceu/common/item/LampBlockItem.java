package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.block.LampBlock;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LampBlockItem extends BlockItem {

    public LampBlockItem(LampBlock block, Properties properties) {
        super(block, properties);

        if (GTCEu.isClientSide()) {
            ClientCallWrapper.registerEventListener(this);
        }
    }

    @NotNull
    @Override
    public LampBlock getBlock() {
        return (LampBlock) super.getBlock();
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState returnValue = super.getPlacementState(context);
        ItemStack handItem = context.getItemInHand();
        if (returnValue != null) {
            LampData data = handItem.getOrDefault(GTDataComponents.LAMP_DATA, LampData.EMPTY);
            returnValue = returnValue
                    .setValue(LampBlock.INVERTED, data.inverted())
                    .setValue(LampBlock.BLOOM, data.bloom())
                    .setValue(LampBlock.LIGHT, data.lit());
        }
        return returnValue;
    }

    public void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {
        for (int i = 0; i < 8; ++i) {
            items.add(this.getBlock().getStackFromIndex(i));
        }
    }

    public void initializeClient(Consumer<IClientItemExtensions> consumer) {}

    private static class ClientCallWrapper {

        private static void registerEventListener(LampBlockItem item) {}
    }

    public record LampData(boolean inverted, boolean bloom, boolean lit) {

        public static final LampData EMPTY = new LampData(false, false, false);
        public static final Codec<LampData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("inverted").forGetter(LampData::inverted),
                Codec.BOOL.fieldOf("bloom").forGetter(LampData::bloom),
                Codec.BOOL.fieldOf("lit").forGetter(LampData::lit)).apply(instance, LampData::new));
        public static final StreamCodec<ByteBuf, LampData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, LampData::inverted,
                ByteBufCodecs.BOOL, LampData::bloom,
                ByteBufCodecs.BOOL, LampData::lit,
                LampData::new);
    }
}
