package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
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
            return ItemStack.CODEC.parse(context.nbtOps(), compoundTag)
                    .getOrThrow(false, GTCEu.LOGGER::error);
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
            return FluidStack.CODEC.parse(context.nbtOps(), compoundTag)
                    .getOrThrow(false, GTCEu.LOGGER::error);
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
    }

    public static class ComponentTransformer implements ValueTransformer<Component> {

        @Override
        public Tag serializeNBT(Component value, TransformerContext<Component> context) {
            return StringTag.valueOf(Component.Serializer.toJson(value));
        }

        @Override
        public @Nullable Component deserializeNBT(Tag tag, TransformerContext<Component> context) {
            if (tag instanceof StringTag strTag) return Component.Serializer.fromJson(strTag.getAsString());
            return Component.empty();
        }
    }

}
