package com.lowdragmc.gtceu.api.data.chemical.material;

import com.google.common.collect.HashBiMap;
import com.lowdragmc.gtceu.api.GTValues;
import net.minecraft.world.item.DyeColor;

public class MarkerMaterials {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        Color.Colorless.toString();
        Tier.ULV.toString();
        Empty.toString();
    }

    /**
     * Marker materials without category
     */
    public static final MarkerMaterial Empty = new MarkerMaterial("empty");

    /**
     * Color materials
     */
    public static class Color {

        /**
         * Can be used only by direct specifying
         * Means absence of color on OrePrefix
         * Often a default value for color prefixes
         */
        public static final MarkerMaterial Colorless = new MarkerMaterial("colorless");

        public static final MarkerMaterial White = new MarkerMaterial("white");
        public static final MarkerMaterial Orange = new MarkerMaterial("orange");
        public static final MarkerMaterial Magenta = new MarkerMaterial("magenta");
        public static final MarkerMaterial LightBlue = new MarkerMaterial("light_blue");
        public static final MarkerMaterial Yellow = new MarkerMaterial("yellow");
        public static final MarkerMaterial Lime = new MarkerMaterial("lime");
        public static final MarkerMaterial Pink = new MarkerMaterial("pink");
        public static final MarkerMaterial Gray = new MarkerMaterial("gray");
        public static final MarkerMaterial LightGray = new MarkerMaterial("light_gray");
        public static final MarkerMaterial Cyan = new MarkerMaterial("cyan");
        public static final MarkerMaterial Purple = new MarkerMaterial("purple");
        public static final MarkerMaterial Blue = new MarkerMaterial("blue");
        public static final MarkerMaterial Brown = new MarkerMaterial("brown");
        public static final MarkerMaterial Green = new MarkerMaterial("green");
        public static final MarkerMaterial Red = new MarkerMaterial("red");
        public static final MarkerMaterial Black = new MarkerMaterial("black");

        /**
         * Arrays containing all possible color values (without Colorless!)
         */
        public static final MarkerMaterial[] VALUES = new MarkerMaterial[]{
                White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, LightGray, Cyan, Purple, Blue, Brown, Green, Red, Black
        };

        /**
         * Gets color by it's name
         * Name format is equal to EnumDyeColor
         */
        public static MarkerMaterial valueOf(String string) {
            for (MarkerMaterial color : VALUES) {
                if (color.toString().equals(string)) {
                    return color;
                }
            }
            return null;
        }

        /**
         * Contains associations between MC EnumDyeColor and Color MarkerMaterial
         */
        public static final HashBiMap<DyeColor, MarkerMaterial> COLORS = HashBiMap.create();

        static {
            for (var color : DyeColor.values()) {
                COLORS.put(color, Color.valueOf(color.getName()));
            }
        }

    }

    /**
     * Circuitry, batteries and other technical things
     */
    public static class Tier {
        public static final Material ULV = new MarkerMaterial(GTValues.VN[GTValues.ULV].toLowerCase());
        public static final Material LV = new MarkerMaterial(GTValues.VN[GTValues.LV].toLowerCase());
        public static final Material MV = new MarkerMaterial(GTValues.VN[GTValues.MV].toLowerCase());
        public static final Material HV = new MarkerMaterial(GTValues.VN[GTValues.HV].toLowerCase());
        public static final Material EV = new MarkerMaterial(GTValues.VN[GTValues.EV].toLowerCase());
        public static final Material IV = new MarkerMaterial(GTValues.VN[GTValues.IV].toLowerCase());
        public static final Material LuV = new MarkerMaterial(GTValues.VN[GTValues.LuV].toLowerCase());
        public static final Material ZPM = new MarkerMaterial(GTValues.VN[GTValues.ZPM].toLowerCase());
        public static final Material UV = new MarkerMaterial(GTValues.VN[GTValues.UV].toLowerCase());
        // TODO do we really need UHV+?
//        public static final Material UHV = new MarkerMaterial(GTValues.VN[GTValues.UHV].toLowerCase());
//        public static final Material UEV = new MarkerMaterial(GTValues.VN[GTValues.UEV].toLowerCase());
//        public static final Material UIV = new MarkerMaterial(GTValues.VN[GTValues.UIV].toLowerCase());
//        public static final Material UXV = new MarkerMaterial(GTValues.VN[GTValues.UXV].toLowerCase());
//        public static final Material OpV = new MarkerMaterial(GTValues.VN[GTValues.OpV].toLowerCase());
//        public static final Material MAX = new MarkerMaterial(GTValues.VN[GTValues.MAX].toLowerCase());
    }

    public static class Component {
        public static final Material Resistor = new MarkerMaterial("resistor");
        public static final Material Transistor = new MarkerMaterial("transistor");
        public static final Material Capacitor = new MarkerMaterial("capacitor");
        public static final Material Diode = new MarkerMaterial("diode");
        public static final Material Inductor = new MarkerMaterial("inductor");
    }

}
