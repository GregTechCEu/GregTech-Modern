package com.gregtechceu.gtceu.api.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CombinedCapabilityProvider implements ICapabilityProvider {

    private final ICapabilityProvider[] providers;

    public CombinedCapabilityProvider(ICapabilityProvider... providers) {
        this.providers = providers;
    }

    public CombinedCapabilityProvider(List<ICapabilityProvider> providers) {
        this.providers = providers.toArray(new ICapabilityProvider[0]);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        for (ICapabilityProvider provider : providers) {
            LazyOptional<T> cap = provider.getCapability(capability, facing);
            if (cap.isPresent()) {
                return cap;
            }
        }
        return LazyOptional.empty();
    }
}
