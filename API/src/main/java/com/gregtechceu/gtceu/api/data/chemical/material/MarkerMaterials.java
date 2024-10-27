package com.gregtechceu.gtceu.api.data.chemical.material;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.world.item.DyeColor;

import com.google.common.collect.HashBiMap;

public class MarkerMaterials {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        Color.Colorless.toString();
        Empty.toString();
    }

    /**
     * Marker materials without category
     */
    public static final MarkerMaterial Empty = new MarkerMaterial(GTCEu.id("empty"));

    /**
     * Color materials
     */
    public static class Color {

        /**
         * Can be used only by direct specifying
         * Means absence of color on TagPrefix
         * Often a default value for color prefixes
         */
        public static final MarkerMaterial Colorless = new MarkerMaterial(GTCEu.id("colorless"));

        public static final MarkerMaterial White = new MarkerMaterial(GTCEu.id("white"));
        public static final MarkerMaterial Orange = new MarkerMaterial(GTCEu.id("orange"));
        public static final MarkerMaterial Magenta = new MarkerMaterial(GTCEu.id("magenta"));
        public static final MarkerMaterial LightBlue = new MarkerMaterial(GTCEu.id("light_blue"));
        public static final MarkerMaterial Yellow = new MarkerMaterial(GTCEu.id("yellow"));
        public static final MarkerMaterial Lime = new MarkerMaterial(GTCEu.id("lime"));
        public static final MarkerMaterial Pink = new MarkerMaterial(GTCEu.id("pink"));
        public static final MarkerMaterial Gray = new MarkerMaterial(GTCEu.id("gray"));
        public static final MarkerMaterial LightGray = new MarkerMaterial(GTCEu.id("light_gray"));
        public static final MarkerMaterial Cyan = new MarkerMaterial(GTCEu.id("cyan"));
        public static final MarkerMaterial Purple = new MarkerMaterial(GTCEu.id("purple"));
        public static final MarkerMaterial Blue = new MarkerMaterial(GTCEu.id("blue"));
        public static final MarkerMaterial Brown = new MarkerMaterial(GTCEu.id("brown"));
        public static final MarkerMaterial Green = new MarkerMaterial(GTCEu.id("green"));
        public static final MarkerMaterial Red = new MarkerMaterial(GTCEu.id("red"));
        public static final MarkerMaterial Black = new MarkerMaterial(GTCEu.id("black"));

        /**
         * Arrays containing all possible color values (without Colorless!)
         */
        public static final MarkerMaterial[] VALUES = new MarkerMaterial[] {
                White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, LightGray, Cyan, Purple, Blue, Brown,
                Green, Red, Black
        };

        /**
         * Gets color by it's name
         * Name format is equal to DyeColor
         */
        public static MarkerMaterial valueOf(String string) {
            for (MarkerMaterial color : VALUES) {
                if (color.getName().equals(string)) {
                    return color;
                }
            }
            return null;
        }

        /**
         * Contains associations between MC DyeColor and Color MarkerMaterial
         */
        public static final HashBiMap<DyeColor, MarkerMaterial> COLORS = HashBiMap.create();

        static {
            for (var color : DyeColor.values()) {
                COLORS.put(color, Color.valueOf(color.getName()));
            }
        }
    }
}
