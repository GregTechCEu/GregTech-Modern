package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CoverBehaviorTransformer implements IValueTransformer<CoverBehavior> {

    @Override
    public boolean mustProvideObject() {
        return true;
    }

    @Override
    public Tag serializeClientSyncNBT(CoverBehavior value, ISyncManaged holder) {
        if (holder instanceof ICoverable coverable) return serialize(value, coverable, true);
        return new CompoundTag();
    }

    @Override
    public Tag serializeNBT(CoverBehavior value, ISyncManaged holder) {
        if (holder instanceof ICoverable coverable) return serialize(value, coverable, false);
        return new CompoundTag();
    }

    @Override
    public CoverBehavior deserializeClientNBT(Tag tag, ISyncManaged holder, @Nullable CoverBehavior currentVal) {
        if (tag instanceof CompoundTag compoundTag && holder instanceof ICoverable coverable)
            return deserialize(compoundTag, coverable, currentVal, true);
        return null;
    }

    @Override
    public CoverBehavior deserializeNBT(Tag tag, ISyncManaged holder, @Nullable CoverBehavior currentVal) {
        if (tag instanceof CompoundTag compoundTag && holder instanceof ICoverable coverable)
            return deserialize(compoundTag, coverable, currentVal, false);
        return null;
    }

    private CompoundTag serialize(CoverBehavior cover, ICoverable holder, boolean isSync) {
        var compound = new CompoundTag();
        if (cover == null) return compound;

        compound.putInt("side", cover.attachedSide.ordinal());
        compound.putString("coverType", cover.coverDefinition.getId().toString());
        CompoundTag serialisedCover = cover.getSyncDataHolder().serializeNBT(isSync);
        compound.put("data", serialisedCover);

        return compound;
    }

    public CoverBehavior deserialize(CompoundTag tag, ICoverable holder, @Nullable CoverBehavior cover,
                                     boolean isSync) {
        /// Ldlib backwards compat
        if (tag.contains("payload") && tag.contains("uid")) {
            tag.putInt("side", tag.getCompound("uid").getInt("side"));
            tag.putString("coverType", tag.getCompound("uid").getString("id"));
            tag.put("data", tag.getCompound("payload").getCompound("d"));
        }

        Direction side = Direction.values()[tag.getInt("side")];

        if (tag.isEmpty() || tag.getString("coverType").isEmpty()) {
            holder.setCoverAtSide(null, side);
            return null;
        }
        ResourceLocation coverType = ResourceLocation.tryParse(tag.getString("coverType"));
        if (cover == null || cover.coverDefinition.getId() != coverType) {
            var coverReg = GTRegistries.COVERS.get(coverType);
            if (coverReg == null) {
                GTCEu.LOGGER.error("Error during NBT load: unknown cover type {} ({})", coverType,
                        tag.getString("coverType"));
                return null;
            }
            holder.setCoverAtSide(coverReg.createCoverBehavior(holder, side), side);
        }

        Objects.requireNonNull(holder.getCoverAtSide(side)).getSyncDataHolder().deserializeNBT(tag.getCompound("data"),
                isSync);

        if (!isSync && holder.getCoverAtSide(side).getAttachItem() == ItemStack.EMPTY) {
            GTCEu.LOGGER.error("Invalid cover save state, this should never happen unless loading corrupted data.");
            holder.setCoverAtSide(null, side);
        }

        return holder.getCoverAtSide(side);
    }
}
