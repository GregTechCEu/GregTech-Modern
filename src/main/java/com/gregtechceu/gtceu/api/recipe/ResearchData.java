package com.gregtechceu.gtceu.api.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import java.util.*;

@AllArgsConstructor
public final class ResearchData implements Iterable<ResearchData.ResearchEntry> {

    private final Collection<ResearchEntry> entries;

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

    public static ResearchData fromNBT(ListTag tag) {
        List<ResearchEntry> entries = new ArrayList<>();
        for (int i = 0; i < tag.size(); ++i) {
            entries.add(ResearchEntry.fromNBT(tag.getCompound(i)));
        }
        return new ResearchData(entries);
    }

    public ListTag toNBT() {
        ListTag tag = new ListTag();
        this.entries.forEach(entry -> tag.add(entry.toNBT()));
        return tag;
    }

    /**
     * An entry containing information about a researchable recipe.
     * <p>
     * Used for internal research storage and JEI integration.
     */
    public static final class ResearchEntry {

        @NotNull
        @Getter
        private final String researchId;
        @NotNull
        @Getter
        private final ItemStack dataItem;

        /**
         * @param researchId the id of the research
         * @param dataItem the item allowed to contain the research
         */
        public ResearchEntry(@NotNull String researchId, @NotNull ItemStack dataItem) {
            this.researchId = researchId;
            this.dataItem = dataItem;
        }

        public static ResearchEntry fromNBT(CompoundTag tag) {
            return new ResearchEntry(tag.getString("researchId"), ItemStack.of(tag.getCompound("dataItem")));
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("researchId", researchId);
            tag.put("dataItem", dataItem.save(new CompoundTag()));
            return tag;
        }
    }
}