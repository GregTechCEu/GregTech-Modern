package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Reference2LongLinkedOpenHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemMaterialInfo {

    private final Reference2LongLinkedOpenHashMap<Material> materials = new Reference2LongLinkedOpenHashMap<>();
    private final List<MaterialStack> sortedMaterials = new ArrayList<>();

    public ItemMaterialInfo(MaterialStack... materialStacks) {
        for (var mat : materialStacks) {
            materials.merge(mat.material(), mat.amount(), Long::sum);
        }
        setSortedMaterials();
    }

    public ItemMaterialInfo(List<MaterialStack> materialStacks) {
        for (var mat : materialStacks) {
            materials.merge(mat.material(), mat.amount(), Long::sum);
        }
        setSortedMaterials();
    }

    /**
     * Returns the first MaterialStack in the "materials" list
     */
    public MaterialStack getMaterial() {
        return sortedMaterials.isEmpty() ? null : sortedMaterials.get(0);
    }

    /**
     * Returns all MaterialStacks associated with this Object.
     */
    public ImmutableList<MaterialStack> getMaterials() {
        return ImmutableList.copyOf(sortedMaterials);
    }

    public void addMaterialStacks(List<MaterialStack> stacks) {
        for (var mat : stacks) {
            materials.merge(mat.material(), mat.amount(), Long::sum);
        }
        setSortedMaterials();
    }

    private void setSortedMaterials() {
        sortedMaterials.clear();
        for (var m : materials.keySet()) {
            sortedMaterials.add(new MaterialStack(m, materials.getLong(m)));
        }
        sortedMaterials.sort(Comparator.comparingLong(MaterialStack::amount));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemMaterialInfo that = (ItemMaterialInfo) o;
        return sortedMaterials.equals(that.sortedMaterials);
    }

    @Override
    public int hashCode() {
        return sortedMaterials.hashCode();
    }

    @Override
    public String toString() {
        return sortedMaterials.isEmpty() ? "" : sortedMaterials.get(0).material().toCamelCaseString();
    }
}
