package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public class PatternStateTransformer implements ValueTransformer<PatternState> {
    @Override
    public Tag serializeNBT(PatternState value, TransformerContext<PatternState> context) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("formed", value.isFormed());
        tag.putBoolean("flipped", value.isFlipped());
        tag.putInt("checkState", value.getState().ordinal());
        // tag.put("error", ValueTransformers.get(value.getError().getClass()).serializeNBT(value.getError(), context));
        return tag;
    }

    @Override
    public @Nullable PatternState deserializeNBT(Tag tag, TransformerContext<PatternState> context) {
        CompoundTag compoundTag = (CompoundTag) tag;
        boolean formed = compoundTag.getBoolean("formed");
        boolean flipped = compoundTag.getBoolean("flipped");
        PatternState.CheckState state = PatternState.CheckState.values()[compoundTag.getInt("checkState")];
        //PatternError error = c
        PatternState pState = new PatternState();
        pState.setFormed(formed);
        pState.setFlipped(flipped);
        pState.setState(state);
        //pState.setError(error);
        return pState;
    }
}
