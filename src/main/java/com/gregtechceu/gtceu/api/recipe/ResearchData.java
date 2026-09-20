package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ResearchData implements Iterable<ResearchData.ResearchEntry> {

    public static final Codec<ResearchData> CODEC = ResearchEntry.CODEC.listOf().xmap(ResearchData::new,
            data -> data.entries);
    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchData> STREAM_CODEC = ResearchEntry.STREAM_CODEC
            .apply(ByteBufCodecs.list()).map(ResearchData::new, data -> data.entries);

    private final List<ResearchEntry> entries;

    public ResearchData() {
        entries = new ArrayList<>();
    }

    public ResearchData(List<ResearchEntry> entries) {
        this.entries = new ArrayList<>(entries);
    }

    /**
     * @param entry the entry to add
     */
    public void add(@NotNull ResearchEntry entry) {
        this.entries.add(entry);
    }

    @NotNull
    @Override
    public Iterator<ResearchEntry> iterator() {
        return this.entries.iterator();
    }

    public static ResearchData fromJson(JsonArray array, HolderLookup.Provider registries) {
        return CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries), array).getOrThrow();
    }

    public JsonArray toJson(HolderLookup.Provider registries) {
        return CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), this).getOrThrow().getAsJsonArray();
    }

    public static ResearchData fromNetwork(RegistryFriendlyByteBuf buf) {
        return STREAM_CODEC.decode(buf);
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        STREAM_CODEC.encode(buf, this);
    }

    /**
     * An entry containing information about a researchable recipe.
     * <p>
     * Used for internal research storage and JEI integration.
     */
    public static final class ResearchEntry {

        public static final Codec<ResearchEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("researchId").forGetter(val -> val.researchId),
                ItemStack.CODEC.fieldOf("dataItem").forGetter(val -> val.dataItem))
                .apply(instance, ResearchEntry::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, ResearchEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ResearchEntry::getResearchId,
                ItemStack.STREAM_CODEC, ResearchEntry::getDataItem,
                ResearchEntry::new);

        @NotNull
        private final String researchId;
        @NotNull
        private final ItemStack dataItem;

        /**
         * @param researchId the id of the research
         * @param dataItem   the item allowed to contain the research
         */
        public ResearchEntry(@NotNull String researchId, @NotNull ItemStack dataItem) {
            this.researchId = researchId;
            this.dataItem = dataItem;
        }

        public String getResearchId() {
            return researchId;
        }

        public ItemStack getDataItem() {
            return dataItem;
        }

        public static ResearchEntry fromJson(JsonObject tag, HolderLookup.Provider registries) {
            return CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, registries), tag).getOrThrow();
        }

        public JsonObject toJson(HolderLookup.Provider registries) {
            return CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), this).getOrThrow().getAsJsonObject();
        }

        public static ResearchEntry fromNetwork(RegistryFriendlyByteBuf buf) {
            return STREAM_CODEC.decode(buf);
        }

        public void toNetwork(RegistryFriendlyByteBuf buf) {
            STREAM_CODEC.encode(buf, this);
        }
    }
}
