package com.gregtechceu.gtceu.api.chemical.material.properties;

import com.gregtechceu.gtceu.api.fluids.store.FluidStorage;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.jetbrains.annotations.Nullable;

@NoArgsConstructor
public class FluidProperty implements IMaterialProperty<FluidProperty> {

    @Getter
    private final FluidStorage storage = new FluidStorage();
    @Getter @Setter
    private @Nullable FluidStorageKey primaryKey = null;

    @Override
    public void verifyProperty(MaterialProperties properties) {

    }

}
