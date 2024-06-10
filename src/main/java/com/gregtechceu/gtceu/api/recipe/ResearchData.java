package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
public final class ResearchData implements Iterable<ResearchData.ResearchEntry> {

    public static final Codec<ResearchData> CODEC = ResearchEntry.CODEC.listOf().xmap(ResearchData::new,
            data -> data.entries);

    private final List<ResearchEntry> entries;

    public ResearchData() {
        entries = new ArrayList<>();
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

    public static ResearchData fromNetwork(RegistryFriendlyByteBuf buf) {
        List<ResearchEntry> entries = new ArrayList<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; ++i) {
            entries.add(ResearchEntry.fromNetwork(buf));
        }
        return new ResearchData(entries);
    }

    public void toNetwork(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.entries.size());
        this.entries.forEach(entry -> entry.toNetwork(buf));
    }

    /**
     * An entry containing information about a researchable recipe.
     * <p>
     * Used for internal research storage and JEI integration.
     */
    public static final class ResearchEntry {

        public static final Codec<ResearchEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("research_id").forGetter(val -> val.researchId),
                ItemStack.CODEC.fieldOf("data_item").forGetter(val -> val.dataItem))
                .apply(instance, ResearchEntry::new));

        @NotNull
        @Getter
        private final String researchId;
        @NotNull
        @Getter
        private final ItemStack dataItem;

        /**
         * @param researchId the id of the research
         * @param dataItem   the item allowed to contain the research
         */
        public ResearchEntry(@NotNull String researchId, @NotNull ItemStack dataItem) {
            this.researchId = researchId;
            this.dataItem = dataItem;
        }

        public static ResearchEntry fromNetwork(RegistryFriendlyByteBuf buf) {
            String researchId = buf.readUtf();
            ItemStack dataItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
            return new ResearchEntry(researchId, dataItem);
        }

        public void toNetwork(RegistryFriendlyByteBuf buf) {
            buf.writeUtf(this.researchId);
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.dataItem);
        }
    }
}
