package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.integration.kjs.built.KJSTagPrefix;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;

import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class OreTagPrefixBuilder extends TagPrefixBuilder {
    @Setter
    public transient Supplier<BlockState> stateSupplier;
    @Setter
    public transient Supplier<Material> materialSupplier;
    @Setter
    public transient ResourceLocation baseModelLocation;
    @Setter
    public transient BlockBehaviour.Properties templateProperties;
    @Setter
    public transient boolean isNether = false;
    @Setter
    public transient boolean isSand = false;
    @Setter
    public transient net.minecraft.world.level.material.Material material = net.minecraft.world.level.material.Material.STONE;
    @Setter
    public transient MaterialColor color = MaterialColor.STONE;
    @Setter
    public transient SoundType sound = SoundType.STONE;

    public OreTagPrefixBuilder(ResourceLocation id, Object... args) {
        super(id, args);
    }

    @Override
    public KJSTagPrefix create(String id) {
        return KJSTagPrefix.oreTagPrefix(id);
    }
    
    @Override
    public TagPrefix register() {
        return value = base.registerOre(stateSupplier, materialSupplier, templateProperties, baseModelLocation, isNether, isSand);
    }
}
