package com.gregtechceu.gtceu.integration.kjs.builders.prefix;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.integration.kjs.built.KJSTagPrefix;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

@Accessors(fluent = true, chain = true)
public class OreTagPrefixBuilder extends TagPrefixBuilder {
    @Setter
    public transient Supplier<BlockState> stateSupplier;
    @Setter
    public transient boolean isNether = false;
    @Setter
    public transient boolean isSand = false;
    @Setter
    public transient MapColor color = MapColor.STONE;
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
        return value = base.registerOre(stateSupplier, isNether, color, sound, isSand);
    }
}
