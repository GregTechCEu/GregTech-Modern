package com.gregtechceu.gtceu.integration.kjs.builders.block;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.SimpleCoilType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.block.CoilBlock;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import dev.latvian.mods.kubejs.block.BlockBuilder;
import dev.latvian.mods.kubejs.client.VariantBlockStateGenerator;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Accessors(chain = true, fluent = true)
public class CoilBlockBuilder extends BlockBuilder {

    @Setter
    public transient int temperature = 0, level = 0, energyDiscount = 1, tier = 0;
    @NotNull
    public transient Supplier<Material> material = () -> GTMaterials.NULL;
    @Setter
    public transient String texture = "minecraft:missingno";

    public CoilBlockBuilder(ResourceLocation i) {
        super(i);
        property(GTBlockStateProperties.ACTIVE);
        renderType("cutout_mipped");
        noValidSpawns(true);
    }

    @Override
    protected void generateBlockStateJson(VariantBlockStateGenerator bs) {
        bs.simpleVariant("active=false", newID("block/", "").toString());
        bs.simpleVariant("active=true", newID("block/", "_active").toString());
    }

    @Override
    protected void generateBlockModelJsons(AssetJsonGenerator generator) {
        generator.blockModel(id, m -> {
            m.parent("minecraft:block/cube_all");
            m.texture("all", texture);
        });
        generator.blockModel(id.withSuffix("_active"), m -> {
            m.parent("gtceu:block/cube_2_layer/all");
            m.texture("bot_all", texture);
            m.texture("top_all", texture + "_bloom");
        });
    }

    public CoilBlockBuilder coilMaterial(@NotNull Supplier<Material> material) {
        this.material = material;
        return this;
    }

    @Override
    public Block createObject() {
        SimpleCoilType coilType = new SimpleCoilType(this.id.getPath(), temperature, level, energyDiscount, tier,
                material, new ResourceLocation(texture));
        CoilBlock result = new CoilBlock(this.createProperties(), coilType);
        GTCEuAPI.HEATING_COILS.put(coilType, () -> result);
        return result;
    }
}
