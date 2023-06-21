package com.gregtechceu.gtceu.api.gui.editor;

import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.editor.data.resource.Resource;
import net.minecraft.nbt.CompoundTag;

/**
 * @author KilaBash
 * @date 2022/12/11
 * @implNote MBDResources
 */
public class RecipeTypeResources extends Resources {
    protected RecipeTypeResources() {
        super();
        resources.put(RecipeTypeResource.RESOURCE_NAME, new RecipeTypeResource());
    }

    public static RecipeTypeResources fromNBT(CompoundTag tag) {
        var resource = new RecipeTypeResources();
        resource.deserializeNBT(tag);
        return resource;
    }

    public static RecipeTypeResources defaultResource() { // default
        RecipeTypeResources resources = new RecipeTypeResources();
        resources.resources.values().forEach(Resource::buildDefault);
        return resources;
    }

}
