package com.gregtechceu.gtceu.api.data.worldgen;

import com.mojang.serialization.JsonOps;
import lombok.Getter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class SimpleWorldGenLayer implements IWorldGenLayer {
    private final String name;
    @Getter
    private final RuleTest target;

    public SimpleWorldGenLayer(String name, RuleTest target) {
        this.name = name;
        this.target = target;
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return getSerializedName() + "[" + RuleTest.CODEC.encodeStart(JsonOps.INSTANCE, target).result().orElse(null) + "]";
    }

    @Override
    public int hashCode() {
        return getSerializedName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IWorldGenLayer that)) return false;

        return getSerializedName().equals(that.getSerializedName());
    }
}
