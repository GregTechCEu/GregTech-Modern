package com.gregtechceu.gtceu.integration.kjs.built;

import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.generator.WorldGeneratorUtils;
import lombok.Getter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class KJSWorldGenLayer implements IWorldGenLayer {
    private final String name;
    @Getter
    private final RuleTest target;

    public KJSWorldGenLayer(String name, RuleTest target) {
        this.name = name;
        this.target = target;
        WorldGeneratorUtils.WORLD_GEN_LAYERS.put(name, this);
    }

    @Override
    public String getSerializedName() {
        return name;
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
