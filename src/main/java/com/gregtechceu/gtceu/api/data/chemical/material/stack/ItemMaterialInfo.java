package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemMaterialInfo {

    private final List<MaterialStack> materials = new ArrayList<>();

    public ItemMaterialInfo(MaterialStack... materials) {
        this.materials.addAll(Arrays.asList(materials));
    }

    public ItemMaterialInfo(List<MaterialStack> materials) {
        this.materials.addAll(materials);
    }

    /**
     * Returns the first MaterialStack in the "materials" list
     */
    public MaterialStack getMaterial() {
        return materials.size() == 0 ? null : materials.get(0);
    }

    /**
     * Returns all MaterialStacks associated with this Object.
     */
    public ImmutableList<MaterialStack> getMaterials() {
        return ImmutableList.copyOf(materials);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemMaterialInfo that = (ItemMaterialInfo) o;
        return materials.equals(that.materials);
    }

    @Override
    public int hashCode() {
        return materials.hashCode();
    }

    @Override
    public String toString() {
        return materials.size() == 0 ? "" : materials.get(0).material().toCamelCaseString();
    }

}
