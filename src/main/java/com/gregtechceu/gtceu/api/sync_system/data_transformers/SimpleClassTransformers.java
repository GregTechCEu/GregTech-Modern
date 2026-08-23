package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

public class SimpleClassTransformers {

    public static class ItemStackTransformer implements ValueTransformer<ItemStack> {

        @Override
        public Tag serializeNBT(ItemStack value, TransformerContext<ItemStack> context) {
            if (value.isEmpty()) return new CompoundTag();
            return value.save(new CompoundTag());
        }

        @Override
        public @Nullable ItemStack deserializeNBT(Tag tag, TransformerContext<ItemStack> context) {
            if (!(tag instanceof CompoundTag compoundTag) || compoundTag.isEmpty()) return ItemStack.EMPTY;
            return ItemStack.of(compoundTag);
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf, ItemStack value, TransformerContext<ItemStack> context) {
            buf.writeItem(value);
        }

        @Override
        public @Nullable ItemStack readFromPacket(FriendlyByteBuf buf, TransformerContext<ItemStack> context) {
            return buf.readItem();
        }
    }

    public static class FluidStackTransformer implements ValueTransformer<FluidStack> {

        @Override
        public Tag serializeNBT(FluidStack value, TransformerContext<FluidStack> context) {
            if (value.isEmpty()) return new CompoundTag();
            return value.writeToNBT(new CompoundTag());
        }

        @Override
        public @Nullable FluidStack deserializeNBT(Tag tag, TransformerContext<FluidStack> context) {
            if (!(tag instanceof CompoundTag compoundTag) || compoundTag.isEmpty()) return FluidStack.EMPTY;
            return FluidStack.loadFluidStackFromNBT(compoundTag);
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf, FluidStack value, TransformerContext<FluidStack> context) {
            value.writeToPacket(buf);
        }

        @Override
        public @Nullable FluidStack readFromPacket(FriendlyByteBuf buf, TransformerContext<FluidStack> context) {
            return FluidStack.readFromPacket(buf);
        }
    }

    public static class BlockPosTransformer implements ValueTransformer<BlockPos> {

        @Override
        public Tag serializeNBT(BlockPos value, TransformerContext<BlockPos> context) {
            return NbtUtils.writeBlockPos(value);
        }

        @Override
        public @Nullable BlockPos deserializeNBT(Tag tag, TransformerContext<BlockPos> context) {
            return NbtUtils.readBlockPos(ValueTransformer.assertTagType(CompoundTag.class, tag, context));
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf, BlockPos value, TransformerContext<BlockPos> context) {
            buf.writeBlockPos(value);
        }

        @Override
        public @Nullable BlockPos readFromPacket(FriendlyByteBuf buf, TransformerContext<BlockPos> context) {
            return buf.readBlockPos();
        }
    }

    public static class ComponentTransformer implements ValueTransformer<Component> {

        @Override
        public Tag serializeNBT(Component value, TransformerContext<Component> context) {
            return StringTag.valueOf(Component.Serializer.toJson(value));
        }

        @Override
        public @Nullable Component deserializeNBT(Tag tag, TransformerContext<Component> context) {
            return ExtraCodecs.COMPONENT.parse(context.ops(), tag)
                    .getOrThrow(true, GTCEu.LOGGER::error);
        }

        @Override
        public void writeToPacket(FriendlyByteBuf buf, Component value, TransformerContext<Component> context) {
            buf.writeComponent(value);
        }

        @Override
        public @Nullable Component readFromPacket(FriendlyByteBuf buf, TransformerContext<Component> context) {
            return buf.readComponent();
        }
    }
}
