package com.gregtechceu.gtceu.api.data.chemical.material.info;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MaterialIconSet {

    public static final Map<String, MaterialIconSet> ICON_SETS = new HashMap<>();
    public static final MaterialIconSet DULL = new MaterialIconSet("dull", null, true);
    public static final MaterialIconSet METALLIC = new MaterialIconSet("metallic");
    public static final MaterialIconSet MAGNETIC = new MaterialIconSet("magnetic", METALLIC);
    public static final MaterialIconSet SHINY = new MaterialIconSet("shiny", METALLIC);
    public static final MaterialIconSet BRIGHT = new MaterialIconSet("bright", SHINY);
    public static final MaterialIconSet DIAMOND = new MaterialIconSet("diamond", SHINY);
    public static final MaterialIconSet EMERALD = new MaterialIconSet("emerald", DIAMOND);
    public static final MaterialIconSet GEM_HORIZONTAL = new MaterialIconSet("gem_horizontal", EMERALD);
    public static final MaterialIconSet GEM_VERTICAL = new MaterialIconSet("gem_vertical", EMERALD);
    public static final MaterialIconSet RUBY = new MaterialIconSet("ruby", EMERALD);
    public static final MaterialIconSet OPAL = new MaterialIconSet("opal", RUBY);
    public static final MaterialIconSet GLASS = new MaterialIconSet("glass", RUBY);
    public static final MaterialIconSet NETHERSTAR = new MaterialIconSet("netherstar", GLASS);
    public static final MaterialIconSet FINE = new MaterialIconSet("fine");
    public static final MaterialIconSet SAND = new MaterialIconSet("sand", FINE);
    public static final MaterialIconSet WOOD = new MaterialIconSet("wood", FINE);
    public static final MaterialIconSet ROUGH = new MaterialIconSet("rough", FINE);
    public static final MaterialIconSet FLINT = new MaterialIconSet("flint", ROUGH);
    public static final MaterialIconSet LIGNITE = new MaterialIconSet("lignite", ROUGH);
    public static final MaterialIconSet QUARTZ = new MaterialIconSet("quartz", ROUGH);
    public static final MaterialIconSet CERTUS = new MaterialIconSet("certus", QUARTZ);
    public static final MaterialIconSet LAPIS = new MaterialIconSet("lapis", QUARTZ);
    public static final MaterialIconSet FLUID = new MaterialIconSet("fluid");
    public static final MaterialIconSet RADIOACTIVE = new MaterialIconSet("radioactive", METALLIC);

    // Implementation -----------------------------------------------------------------------------------------------

    private static int idCounter = 0;
    public final String name;
    public final int id;
    public final boolean isRootIconset;

    /**
     * This can be null if {@link MaterialIconSet#isRootIconset} is true,
     * otherwise it will be Nonnull
     */
    public final MaterialIconSet parentIconset;

    /**
     * Create a new MaterialIconSet whose parent is {@link MaterialIconSet#DULL}
     *
     * @param name the name of the iconset
     */
    public MaterialIconSet(@NotNull String name) {
        this(name, MaterialIconSet.DULL);
    }

    /**
     * Create a new MaterialIconSet whose parent is one of your choosing
     *
     * @param name          the name of the iconset
     * @param parentIconset the parent iconset
     */
    public MaterialIconSet(@NotNull String name, @NotNull MaterialIconSet parentIconset) {
        this(name, parentIconset, false);
    }

    /**
     * Create a new MaterialIconSet which is a root
     * 
     * @param name          the name of the iconset
     * @param parentIconset the parent iconset, should be null if this should be a root iconset
     * @param isRootIconset true if this should be a root iconset, otherwise false
     */
    public MaterialIconSet(@NotNull String name, @Nullable MaterialIconSet parentIconset, boolean isRootIconset) {
        this.name = name.toLowerCase(Locale.ENGLISH);
        Preconditions.checkArgument(!ICON_SETS.containsKey(this.name),
                "MaterialIconSet " + this.name + " already registered!");
        this.id = idCounter++;
        this.isRootIconset = isRootIconset;
        this.parentIconset = parentIconset;
        ICON_SETS.put(this.name, this);
    }

    public static MaterialIconSet getByName(@NotNull String name) {
        return ICON_SETS.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return name;
    }

    public static void init() {
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistryInfo.MATERIAL_ICON_SET.registryKey);
        }
    }
}
