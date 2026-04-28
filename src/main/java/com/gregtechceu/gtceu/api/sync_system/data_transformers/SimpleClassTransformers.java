package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jspecify.annotations.Nullable;

public class SimpleClassTransformers {

    public static class ItemStackTransformer implements ValueTransformer<ItemStack> {

        @Override
        public Tag serializeNBT(ItemStack value, TransformerContext<ItemStack> context) {
            if (value.isEmpty()) return new CompoundTag();
            return ItemStack.CODEC.encodeStart(context.lookup().createSerializationContext(NbtOps.INSTANCE), value)
                    .getOrThrow();
        }

        @Override
        public @Nullable ItemStack deserializeNBT(Tag tag, TransformerContext<ItemStack> context) {
            if (!(tag instanceof CompoundTag compoundTag) || compoundTag.isEmpty()) return ItemStack.EMPTY;
            return ItemStack.CODEC.parse(context.lookup().createSerializationContext(NbtOps.INSTANCE), compoundTag)
                    .getOrThrow();
        }
    }

    public static class FluidStackTransformer implements ValueTransformer<FluidStack> {

        @Override
        public Tag serializeNBT(FluidStack value, TransformerContext<FluidStack> context) {
            if (value.isEmpty()) return new CompoundTag();
            return FluidStack.CODEC.encodeStart(context.lookup().createSerializationContext(NbtOps.INSTANCE), value)
                    .getOrThrow();
        }

        @Override
        public @Nullable FluidStack deserializeNBT(Tag tag, TransformerContext<FluidStack> context) {
            if (!(tag instanceof CompoundTag compoundTag) || compoundTag.isEmpty()) return FluidStack.EMPTY;
            return FluidStack.CODEC.parse(context.lookup().createSerializationContext(NbtOps.INSTANCE), compoundTag)
                    .getOrThrow();
        }
    }

    public static class BlockPosTransformer implements ValueTransformer<BlockPos> {

        @Override
        public Tag serializeNBT(BlockPos value, TransformerContext<BlockPos> context) {
            return new IntArrayTag(new int[] { value.getX(), value.getY(), value.getZ() });
        }

        @Override
        public @Nullable BlockPos deserializeNBT(Tag tag, TransformerContext<BlockPos> context) {
            if (tag instanceof IntArrayTag inta && inta.size() == 3) {
                var arr = inta.getAsIntArray();
                return new BlockPos(arr[0], arr[1], arr[2]);
            }
            return BlockPos.ZERO;
        }
    }

    public static class ComponentTransformer implements ValueTransformer<Component> {

        @Override
        public Tag serializeNBT(Component value, TransformerContext<Component> context) {
            return ComponentSerialization.CODEC
                    .encodeStart(context.lookup().createSerializationContext(NbtOps.INSTANCE), value)
                    .getOrThrow();
        }

        @Override
        public @Nullable Component deserializeNBT(Tag tag, TransformerContext<Component> context) {
            return ComponentSerialization.CODEC
                    .parse(context.lookup().createSerializationContext(NbtOps.INSTANCE), tag)
                    .result().orElse(Component.empty());
        }
    }
}
