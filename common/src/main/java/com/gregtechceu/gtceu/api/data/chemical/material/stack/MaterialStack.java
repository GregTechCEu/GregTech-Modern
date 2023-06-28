package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import java.util.Map;
import java.util.WeakHashMap;

public record MaterialStack(Material material, long amount) {
    private static final Map<String, MaterialStack> PARSE_CACHE = new WeakHashMap<>();

    public MaterialStack copy(long amount) {
        return new MaterialStack(material, amount);
    }

    public MaterialStack copy() {
        return new MaterialStack(material, amount);
    }

    public static MaterialStack fromString(CharSequence str) {
        String trimmed = str.toString().trim();
        String copy = trimmed;

        var cached = PARSE_CACHE.get(trimmed);

        if (cached != null) {
            return cached.isEmpty() ? null : cached.copy();
        }

        var count = 1;
        var spaceIndex = copy.indexOf(' ');

        if (spaceIndex >= 2 && copy.indexOf('x') == spaceIndex - 1) {
            count = Integer.parseInt(copy.substring(0, spaceIndex - 1));
            copy = copy.substring(spaceIndex + 1);
        }

        cached = new MaterialStack(GTMaterials.get(copy), count);
        PARSE_CACHE.put(trimmed, cached);
        return cached.copy();
    }

    public boolean isEmpty() {
        return this.material == GTMaterials.Air || this.amount < 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialStack that = (MaterialStack) o;

        if (amount != that.amount) return false;
        return material.equals(that.material);
    }

    @Override
    public int hashCode() {
        return material.hashCode() * 31 + (int) amount * 31;
    }

    @Override
    public String toString() {
        String string = "";
        if (material.getChemicalFormula().isEmpty()) {
            string += "?";
        } else if (material.getMaterialComponents().size() > 1) {
            string += '(' + material.getChemicalFormula() + ')';
        } else {
            string += material.getChemicalFormula();
        }
        if (amount > 1) {
            string += FormattingUtil.toSmallDownNumbers(Long.toString(amount));
        }
        return string;
    }

}
