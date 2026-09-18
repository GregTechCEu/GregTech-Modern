package com.gregtechceu.gtceu.api.data.worldgen;

import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

import com.mojang.serialization.JsonOps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class SimpleWorldGenLayer implements IWorldGenLayer {

    private final Identifier id;
    private final IWorldGenLayer.RuleTestSupplier target;
    @Getter
    private final Set<Identifier> levels;

    public SimpleWorldGenLayer(Identifier id, IWorldGenLayer.RuleTestSupplier target,
                               Set<Identifier> levels) {
        this.id = id;
        this.target = target;
        this.levels = levels;

        GTRegistries.WORLD_GEN_LAYERS.register(id, this);
    }

    @Override
    public @NotNull String getSerializedName() {
        return id.toString();
    }

    @Override
    public String toString() {
        return getSerializedName() + "[" +
                RuleTest.CODEC.encodeStart(JsonOps.INSTANCE, target.get()).result().orElse(null) + "]" +
                ",dimensions=" + levels.toString();
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

    public RuleTest getTarget() {
        return target.get();
    }

    @Override
    public boolean isApplicableForLevel(Identifier level) {
        return levels.contains(level);
    }
}
