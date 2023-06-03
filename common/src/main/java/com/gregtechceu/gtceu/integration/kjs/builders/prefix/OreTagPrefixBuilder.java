package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.integration.kjs.built.KJSTagPrefix;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class OreTagPrefixBuilder extends TagPrefixBuilder {
    @Setter
    public transient Supplier<BlockState> stateSupplier;
    @Setter
    public transient Material material = Material.STONE;
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
        return value = base.registerOre(stateSupplier, material, color, sound);
    }
}
