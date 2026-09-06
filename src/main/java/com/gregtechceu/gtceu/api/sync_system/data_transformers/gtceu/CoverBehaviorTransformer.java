package com.gregtechceu.gtceu.api.sync_system.data_transformers.gtceu;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class CoverBehaviorTransformer implements ValueTransformer<CoverBehavior> {

    @Override
    public Tag serializeNBT(@Nullable CoverBehavior value,
                            CoverBehaviorTransformer.TransformerContext<CoverBehavior> context) {
        if (value == null) {
            var nullTag = new CompoundTag();
            nullTag.putBoolean("null", true);
            return nullTag;
        }

        var compound = new CompoundTag();
        compound.putString("side", value.attachedSide.getSerializedName());
        compound.putString("coverType", value.coverDefinition.getId().toString());
        CompoundTag serializedCover = value.getSyncDataHolder().serializeNBT(context.lookup());
        compound.put("data", serializedCover);
        return compound;
    }

    @Override
    public @Nullable CoverBehavior deserializeNBT(Tag t,
                                                  CoverBehaviorTransformer.TransformerContext<CoverBehavior> context) {
        CompoundTag tag = ValueTransformer.assertTagType(CompoundTag.class, t, context);
        if (tag.getBoolean("null")) {
            return null;
        }

        if (!(context.holder() instanceof ICoverable holder)) {
            GTCEu.LOGGER.error("Sync: Object attempting to load cover does not implement ICoverable {}", context);
            return null;

        }

        CoverBehavior cover = context.currentValue();

        /// Ldlib backwards compat
        if (tag.contains("payload") && tag.contains("uid")) {
            tag.putInt("side", tag.getCompound("uid").getInt("side"));
            tag.putString("coverType", tag.getCompound("uid").getString("id"));
            tag.put("data", tag.getCompound("payload").getCompound("d"));
        }

        Direction side;
        if (tag.contains("side", Tag.TAG_STRING)) {
            side = Direction.CODEC.byName(tag.getString("side"));
        } else if (tag.contains("side", Tag.TAG_ANY_NUMERIC)) {
            // backwards compat
            side = Direction.values()[tag.getInt("side")];
        } else {
            GTCEu.LOGGER.error("Error during NBT load: invalid side {}", tag.get("side"));
            return null;
        }

        if (tag.isEmpty() || tag.getString("coverType").isEmpty()) {
            holder.setCoverAtSide(null, side);
            return null;
        }
        ResourceLocation coverType = ResourceLocation.tryParse(tag.getString("coverType"));
        if (cover == null || !cover.coverDefinition.getId().equals(coverType)) {
            var coverReg = GTRegistries.COVERS.get(coverType);
            if (coverReg == null) {
                GTCEu.LOGGER.error("Error during NBT load: unknown cover type {} ({})", coverType,
                        tag.getString("coverType"));
                return null;
            }
            holder.setCoverAtSide(coverReg.createCoverBehavior(holder, side), side);
        }

        CoverBehavior newCover = holder.getCoverAtSide(side);
        if (newCover == null) return null;

        newCover.getSyncDataHolder().deserializeNBT(context.lookup(), tag.getCompound("data"));

        if (newCover.getAttachItem() == ItemStack.EMPTY) {
            GTCEu.LOGGER.error("Invalid cover save state, this should never happen unless loading corrupted data.");
            holder.setCoverAtSide(null, side);
        }

        return newCover;
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, @Nullable CoverBehavior value,
                              TransformerContext<CoverBehavior> context) {
        buf.writeBoolean(value != null);
        if (value == null) return;
        buf.writeEnum(value.attachedSide);
        buf.writeResourceLocation(value.coverDefinition.getId());
        if (context.isClientFullSyncUpdate()) value.getSyncDataHolder().resyncAllFields();
        value.getSyncDataHolder().writeClientPacket(context.lookup(), buf);
    }

    @Override
    public @Nullable CoverBehavior readFromPacket(FriendlyByteBuf buf, TransformerContext<CoverBehavior> context) {
        if (!(context.holder() instanceof ICoverable holder)) {
            GTCEu.LOGGER.error("Sync: Object attempting to sync cover does not implement ICoverable {}", context);
            return null;
        }

        if (!buf.readBoolean()) {
            return null;
        }

        Direction side = buf.readEnum(Direction.class);
        ResourceLocation coverId = buf.readResourceLocation();
        CoverBehavior cover = context.currentValue();

        if (cover == null || !cover.coverDefinition.getId().equals(coverId)) {
            var coverReg = GTRegistries.COVERS.get(coverId);
            if (coverReg == null) {
                GTCEu.LOGGER.error("Error during packet read: unknown cover type {}", coverId);
                return null;
            }
            holder.setCoverAtSide(coverReg.createCoverBehavior(holder, side), side);
        }

        CoverBehavior newCover = holder.getCoverAtSide(side);
        if (newCover == null) return null;

        newCover.getSyncDataHolder().readClientPacket(context.lookup(), buf);

        return newCover;
    }
}
