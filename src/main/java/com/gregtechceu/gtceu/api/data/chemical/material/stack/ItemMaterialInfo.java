package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemMaterialInfo {

    private final List<MaterialStack> sortedMaterials = new ArrayList<>();

    public ItemMaterialInfo(MaterialStack... materialStacks) {
        var materials = new Object2LongOpenHashMap<Material>();
        for (var mat : materialStacks) {
            materials.addTo(mat.material(), mat.amount());
        }
        setSortedMaterials(materials);
    }

    public ItemMaterialInfo(List<MaterialStack> materialStacks) {
        var materials = new Object2LongOpenHashMap<Material>();
        materialStacks.forEach(stack -> materials.addTo(stack.material(), stack.amount()));
        setSortedMaterials(materials);
    }

    /**
     * Returns the first MaterialStack in the "materials" list
     */
    public MaterialStack getMaterial() {
        return sortedMaterials.isEmpty() ? MaterialStack.EMPTY : sortedMaterials.get(0);
    }

    /**
     * Returns all MaterialStacks associated with this Object.
     */
    @Unmodifiable
    public List<MaterialStack> getMaterials() {
        return Collections.unmodifiableList(sortedMaterials);
    }

    public void addMaterialStacks(List<MaterialStack> stacks) {
        var materials = new Object2LongOpenHashMap<Material>();
        sortedMaterials.forEach(stack -> materials.addTo(stack.material(), stack.amount()));
        stacks.forEach(stack -> materials.addTo(stack.material(), stack.amount()));
        setSortedMaterials(materials);
    }

    private void setSortedMaterials(Object2LongMap<Material> materials) {
        sortedMaterials.clear();
        materials.object2LongEntrySet().stream()
                .sorted(Comparator.comparingLong(Object2LongMap.Entry::getLongValue))
                .forEach(entry -> sortedMaterials.add(new MaterialStack(entry.getKey(), entry.getLongValue())));
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
