package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.data.recipe.WoodTypeEntry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.rhino.util.HideFromJS;

import java.util.ArrayList;

public class RegisterWoodsEventJS implements KubeEvent {

    public RegisterWoodsEventJS() {
        this.woods = new ArrayList<>();
        this.wrapped = new ArrayList<>();
    }

    @HideFromJS
    public ArrayList<WoodTypeEntry> woods;
    @HideFromJS
    public ArrayList<Wrapped> wrapped;

    public class Wrapped {

        RegisterWoodsEventJS evt;
        String modId;
        String woodName;

        @HideFromJS
        Wrapped(String modId, String woodName) {
            this.modId = modId;
            this.woodName = woodName;
        }

        private String _recipeId;
        private Item _plank;
        private Item _strippedLog;
        private Item _strippedWood;
        private Item _wood;
        private Item _log;
        private Item _door;
        private Item _trapdoor;
        private Item _slab;
        private Item _fence;
        private Item _fenceGate;
        private Item _stairs;
        private Item _boat;
        private Item _chestBoat;
        private Item _sign;
        private Item _hangingSign;
        private Item _button;
        private Item _pressurePlate;

        public Wrapped recipeId(String recipeId) {
            this._recipeId = recipeId;
            return this;
        }

        public Wrapped plank(Item plank) {
            this._plank = plank;
            return this;
        }

        public Wrapped strippedLog(Item strippedLog) {
            this._strippedLog = strippedLog;
            return this;
        }

        public Wrapped strippedWood(Item strippedWood) {
            this._strippedWood = strippedWood;
            return this;
        }

        public Wrapped wood(Item wood) {
            this._wood = wood;
            return this;
        }

        public Wrapped log(Item log) {
            this._log = log;
            return this;
        }

        public Wrapped door(Item door) {
            this._door = door;
            return this;
        }

        public Wrapped trapdoor(Item trapdoor) {
            this._trapdoor = trapdoor;
            return this;
        }

        public Wrapped slab(Item slab) {
            this._slab = slab;
            return this;
        }

        public Wrapped fence(Item fence) {
            this._fence = fence;
            return this;
        }

        public Wrapped fenceGate(Item fenceGate) {
            this._fenceGate = fenceGate;
            return this;
        }

        public Wrapped stairs(Item stairs) {
            this._stairs = stairs;
            return this;
        }

        public Wrapped boat(Item boat) {
            this._boat = boat;
            return this;
        }

        public Wrapped chestBoat(Item chestBoat) {
            this._chestBoat = chestBoat;
            return this;
        }

        public Wrapped sign(Item sign) {
            this._sign = sign;
            return this;
        }

        public Wrapped hangingSign(Item hangingSign) {
            this._hangingSign = hangingSign;
            return this;
        }

        public Wrapped button(Item button) {
            this._button = button;
            return this;
        }

        public Wrapped pressurePlate(Item pressurePlate) {
            this._pressurePlate = pressurePlate;
            return this;
        }

        @HideFromJS
        public WoodTypeEntry toEntry() throws IllegalArgumentException {
            if (this.modId == null) {
                throw new IllegalArgumentException("Need a modId");
            }
            if (this.woodName == null) {
                throw new IllegalArgumentException("Need a woodName");
            }
            if (this._recipeId == null) {
                throw new IllegalArgumentException("Need a recipeId");
            }

            if (this._plank == null) {
                throw new IllegalArgumentException("Need a plank");
            }

            if (this._strippedLog == null) {
                throw new IllegalArgumentException("Need a strippedLog");
            }

            if (this._strippedWood == null) {
                throw new IllegalArgumentException("Need a strippedWood");
            }

            if (this._wood == null) {
                throw new IllegalArgumentException("Need a wood");
            }

            if (this._log == null) {
                throw new IllegalArgumentException("Need a log");
            }

            if (this._door == null) {
                throw new IllegalArgumentException("Need a door");
            }

            if (this._trapdoor == null) {
                throw new IllegalArgumentException("Need a trapdoor");
            }

            if (this._slab == null) {
                throw new IllegalArgumentException("Need a slab");
            }

            if (this._fence == null) {
                throw new IllegalArgumentException("Need a fence");
            }

            if (this._fenceGate == null) {
                throw new IllegalArgumentException("Need a fenceGate");
            }

            if (this._stairs == null) {
                throw new IllegalArgumentException("Need a stairs");
            }

            if (this._boat == null) {
                throw new IllegalArgumentException("Need a boat");
            }

            if (this._chestBoat == null) {
                throw new IllegalArgumentException("Need a chestBoat");
            }

            if (this._sign == null) {
                throw new IllegalArgumentException("Need a sign");
            }

            if (this._hangingSign == null) {
                throw new IllegalArgumentException("Need a hangingSign");
            }

            if (this._button == null) {
                throw new IllegalArgumentException("Need a button");
            }

            if (this._pressurePlate == null) {
                throw new IllegalArgumentException("Need a pressurePlate");
            }

            return new WoodTypeEntry.Builder(this.modId, this.woodName)
                    .planks(this._plank, this._recipeId + "_planks")
                    .log(Items.OAK_LOG).removeCharcoalRecipe()
                    .strippedLog(this._strippedLog)
                    .wood(this._wood)
                    .strippedWood(this._strippedWood)
                    .door(this._door, this._recipeId + "_door")
                    .trapdoor(this._trapdoor, this._recipeId + "_trapdoor")
                    .slab(this._slab, this._recipeId + "_slab")
                    .fence(this._fence, this._recipeId + "_fence")
                    .fenceGate(this._fenceGate, this._recipeId + "_fence_gate")
                    .stairs(this._stairs, this._recipeId + "_stairs")
                    .boat(this._boat, this._recipeId + "_boat")
                    .chestBoat(this._chestBoat, this._recipeId + "_chest_boat")
                    .sign(this._sign, this._recipeId + "_sign")
                    .hangingSign(this._hangingSign, this._recipeId + "_hanging_sign")
                    .button(this._button, this._recipeId + "_button")
                    .pressurePlate(this._pressurePlate, this._recipeId + "_pressure_plate")
                    .registerAllMaterialInfo()
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

    public Wrapped register(String modId, String woodName) {
        var wrap = new Wrapped(modId, woodName);
        this.wrapped.add(wrap);
        return wrap;
    }

    @Override
    public void afterPosted(EventResult result) {
        for (var wrapped : this.wrapped) {
            wrapped.register();
        }
        KubeEvent.super.afterPosted(result);
    }
}
