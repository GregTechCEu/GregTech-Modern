package com.gregtechceu.gtceu.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.GTCEu;
import com.mojang.serialization.JsonOps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

    public static ResearchData fromJson(JsonArray array) {
        List<ResearchEntry> entries = new ArrayList<>();
        for (int i = 0; i < array.size(); ++i) {
            entries.add(ResearchEntry.fromJson(array.get(i).getAsJsonObject()));
        }
        return new ResearchData(entries);
    }

    public JsonArray toJson() {
        JsonArray json = new JsonArray();
        this.entries.forEach(entry -> json.add(entry.toJson()));
        return json;
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

        public static ResearchEntry fromJson(JsonObject tag) {
            return new ResearchEntry(tag.get("researchId").getAsString(), ItemStack.CODEC.parse(JsonOps.INSTANCE, tag.get("dataItem")).getOrThrow(false, GTCEu.LOGGER::error));
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("researchId", researchId);
            json.addProperty("dataItem", dataItem.save(new CompoundTag()).toString());
            return json;
        }
    }
}