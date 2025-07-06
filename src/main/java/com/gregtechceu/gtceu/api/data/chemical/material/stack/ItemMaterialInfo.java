package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemMaterialInfo {

    private final List<MaterialStack> sortedMaterials = new ArrayList<>();
    private int sortedHash = 0;
    private String toStringValue;

    public ItemMaterialInfo(MaterialStack... materialStacks) {
        var materials = new Reference2LongOpenHashMap<Material>();
        for (var mat : materialStacks) {
            materials.addTo(mat.material(), mat.amount());
        }
        setSortedMaterials(materials);
    }

    public ItemMaterialInfo(List<MaterialStack> materialStacks) {
        var materials = new Reference2LongOpenHashMap<Material>();
        for (var stack : materialStacks) {
            materials.addTo(stack.material(), stack.amount());
        }
        setSortedMaterials(materials);
    }

    public ItemMaterialInfo(Reference2LongMap<Material> materialList) {
        setSortedMaterials(materialList);
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
    @UnmodifiableView
    public List<MaterialStack> getMaterials() {
        return Collections.unmodifiableList(sortedMaterials);
    }

    public void addMaterialStacks(List<MaterialStack> stacks) {
        var materials = new Reference2LongOpenHashMap<Material>();
        sortedMaterials.forEach(stack -> materials.addTo(stack.material(), stack.amount()));
        stacks.forEach(stack -> materials.addTo(stack.material(), stack.amount()));
        setSortedMaterials(materials);
    }

    private void setSortedMaterials(Reference2LongMap<Material> matStacks) {
        sortedMaterials.clear();

        for (var entry : matStacks.reference2LongEntrySet()) {
            sortedMaterials.add(new MaterialStack(entry.getKey(), entry.getLongValue()));
        }
        sortedMaterials.sort(Comparator.comparingLong(MaterialStack::amount));

        sortedHash = sortedMaterials.hashCode();

        StringBuilder ret = new StringBuilder("[ ");
        for (var matStack : sortedMaterials) {
            ret.append(matStack.amount() / (float) GTValues.M).append("x ")
                    .append(matStack.material().getResourceLocation()).append(" ");
        }
        ret.append("]");
        toStringValue = ret.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemMaterialInfo that = (ItemMaterialInfo) o;
        return this.hashCode() == o.hashCode() && sortedMaterials.equals(that.sortedMaterials);
    }

    @Override
    public int hashCode() {
        return sortedHash;
    }

    @Override
    public String toString() {
        return toStringValue;
    }
}
