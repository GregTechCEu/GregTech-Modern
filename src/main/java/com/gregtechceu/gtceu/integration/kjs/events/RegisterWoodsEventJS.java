package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;

import net.minecraft.world.item.Item;

import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterWoodsEventJS implements KubeEvent {

    public RegisterWoodsEventJS() {
        this.woods = new ArrayList<>();
        this.builders = new ArrayList<>();
    }

    @HideFromJS
    public ArrayList<WoodTypeEntry> woods;
    @HideFromJS
    public ArrayList<Builder> builders;

    @Accessors(fluent = true, chain = true)
    public class Builder {

        private final String modId;
        private final String woodName;

        @HideFromJS
        private Builder(String modId, String woodName) {
            this.modId = modId;
            this.woodName = woodName;
        }

        @Setter
        private transient String recipeId;
        @Setter
        private transient Item plank;
        @Setter
        private transient Item strippedLog;
        @Setter
        private transient Item strippedWood;
        @Setter
        private transient Item wood;
        @Setter
        private transient Item log;
        @Setter
        private transient Item door;
        @Setter
        private transient Item trapdoor;
        @Setter
        private transient Item slab;
        @Setter
        private transient Item fence;
        @Setter
        private transient Item fenceGate;
        @Setter
        private transient Item stairs;
        @Setter
        private transient Item boat;
        @Setter
        private transient Item chestBoat;
        @Setter
        private transient Item sign;
        @Setter
        private transient Item hangingSign;
        @Setter
        private transient Item button;
        @Setter
        private transient Item pressurePlate;

        @HideFromJS
        public WoodTypeEntry toEntry() throws IllegalArgumentException {
            WoodTypeEntry.Builder builder = new WoodTypeEntry.Builder(this.modId, this.woodName);

            if (this.plank != null)
                builder.planks(this.plank, this.recipeId == null ? null : this.recipeId + "_planks");
            if (this.log != null) builder.strippedLog(this.log).removeCharcoalRecipe();
            if (this.strippedLog != null) builder.strippedLog(this.strippedLog);
            if (this.wood != null) builder.strippedLog(this.wood);
            if (this.strippedWood != null) builder.strippedWood(this.strippedWood);
            if (this.door != null) builder.door(this.door, this.recipeId == null ? null : this.recipeId + "_door");
            if (this.trapdoor != null)
                builder.trapdoor(this.trapdoor, this.recipeId == null ? null : this.recipeId + "_trapdoor");
            if (this.slab != null) builder.slab(this.slab, this.recipeId == null ? null : this.recipeId + "_slab");
            if (this.fence != null) builder.fence(this.fence, this.recipeId == null ? null : this.recipeId + "_fence");
            if (this.fenceGate != null)
                builder.fenceGate(this.fenceGate, this.recipeId == null ? null : this.recipeId + "_fence_gate");
            if (this.stairs != null)
                builder.stairs(this.stairs, this.recipeId == null ? null : this.recipeId + "_stairs");
            if (this.boat != null) builder.boat(this.boat, this.recipeId == null ? null : this.recipeId + "_boat");
            if (this.chestBoat != null)
                builder.chestBoat(this.chestBoat, this.recipeId == null ? null : this.recipeId + "_chest_boat");
            if (this.sign != null) builder.sign(this.sign, this.recipeId == null ? null : this.recipeId + "_sign");
            if (this.hangingSign != null)
                builder.hangingSign(this.hangingSign, this.recipeId == null ? null : this.recipeId + "_hanging_sign");
            if (this.button != null)
                builder.button(this.button, this.recipeId == null ? null : this.recipeId + "_button");
            if (this.pressurePlate != null) builder.pressurePlate(this.pressurePlate,
                    this.recipeId == null ? null : this.recipeId + "_pressure_plate");

            return builder.registerMaterialInfo(this.plank != null, this.door != null, this.slab != null,
                    this.fence != null, this.fenceGate != null, this.stairs != null, this.boat != null,
                    this.chestBoat != null, this.button != null, this.pressurePlate != null)
                    .build();
        }

        boolean wasRegistered = false;

        @HideFromJS
        public void register() {
            if (!this.wasRegistered) {
                this.wasRegistered = true;
                try {
                    woods.add(this.toEntry());
                } catch (Exception e) {
                    GTCEu.LOGGER.error(e);

                }
            } else
                GTCEu.LOGGER.warn("Tried registering a wood type twice!");
        }
    }

    public Builder register(@NotNull String modId, @NotNull String woodName) {
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(woodName, "woodName");

        Builder value = new Builder(modId, woodName);

        this.builders.add(value);
        return value;
    }

    @Override
    public void afterPosted(EventResult result) {
        for (Builder builder : this.builders) {
            builder.register();
        }
        KubeEvent.super.afterPosted(result);
    }
}
