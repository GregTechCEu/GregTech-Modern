package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PatternErrorTransformer implements ValueTransformer<PatternError> {
    @Override
    public Tag serializeNBT(PatternError value, TransformerContext<PatternError> context) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("pos", NbtUtils.writeBlockPos(value.getPos()));
        ListTag outerListTag = new ListTag();
        for (List<ItemStack> stackList : value.getCandidates()) {
            ListTag innerListTag = new ListTag();
            for (ItemStack stack : stackList) {
                innerListTag.add(stack.serializeNBT());
            }
            outerListTag.add(innerListTag);
        }
        compoundTag.put("candidates", outerListTag);
        return compoundTag;
    }

    @Override
    public @Nullable PatternError deserializeNBT(Tag tag, TransformerContext<PatternError> context) {
        CompoundTag compoundTag = (CompoundTag) tag;
        BlockPos pos = NbtUtils.readBlockPos(compoundTag.getCompound("pos"));
        List<List<ItemStack>> stacks = new ArrayList<>();
        ListTag outer = compoundTag.getList("candidates", Tag.TAG_LIST);
        for (int i = 0; i < outer.size(); i++) {
            List<ItemStack> innerStacks = new ArrayList<>();
            ListTag inner = outer.getList(i);
            for (int j = 0; j < inner.size(); j++) {
                innerStacks.add(ItemStack.of(inner.getCompound(j)));
            }
            stacks.add(innerStacks);
        }
        return new PatternError(pos, stacks);
    }
}
