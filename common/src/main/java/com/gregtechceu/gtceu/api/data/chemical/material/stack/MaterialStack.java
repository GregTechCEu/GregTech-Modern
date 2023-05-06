package com.gregtechceu.gtceu.api.data.chemical.material.stack;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public record MaterialStack(Material material, long amount) {
    static Map<String, MaterialStack> PARSE_CACHE = new HashMap<>();
    static MaterialStack EMPTY = new MaterialStack(GTMaterials.Air, 0);

    public MaterialStack copy(long amount) {
        return new MaterialStack(material, amount);
    }

    public MaterialStack copy() {
        return new MaterialStack(material, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialStack that = (MaterialStack) o;

        if (amount != that.amount) return false;
        return material.equals(that.material);
    }

    public static MaterialStack fromString(CharSequence str) {
        var os = str.toString().trim();
        var s = os;

        var cached = PARSE_CACHE.get(os);

        if (cached != null) {
            return cached.isEmpty() ? MaterialStack.EMPTY : cached.copy();
        }

        var count = 1;
        var spaceIndex = s.indexOf(' ');

        if (spaceIndex >= 2 && s.indexOf('x') == spaceIndex - 1) {
            count = Integer.parseInt(s.substring(0, spaceIndex - 1));
            s = s.substring(spaceIndex + 1);
        }

        cached = new MaterialStack(GTMaterials.get(s), count);
        PARSE_CACHE.put(os, cached);
        return cached.copy();
    }

    public boolean isEmpty() {
        return this.material == GTMaterials.Air || this.amount < 1;
    }

    @Override
    public int hashCode() {
        return material.hashCode();
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
