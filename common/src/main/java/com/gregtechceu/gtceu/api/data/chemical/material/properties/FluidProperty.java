package com.gregtechceu.gtceu.api.data.chemical.material.properties;

import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

@NoArgsConstructor
public class FluidProperty implements IMaterialProperty<FluidProperty> {

    @Getter
    private final FluidStorage storage = new FluidStorage();
    @Getter @Setter
    private @Nullable FluidStorageKey primaryKey = null;
    @Getter @Setter
    private ResourceLocation stillTexture, flowTexture;

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

}
