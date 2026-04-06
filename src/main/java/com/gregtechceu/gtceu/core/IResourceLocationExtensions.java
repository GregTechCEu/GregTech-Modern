package com.gregtechceu.gtceu.core;

import net.minecraft.resources.ResourceLocation;

public interface IResourceLocationExtensions {

    public boolean gtm$getImplicit();

    public void gtm$setImplicit(boolean implicit);

    public ResourceLocation gtm$asNonImplicit();
}
