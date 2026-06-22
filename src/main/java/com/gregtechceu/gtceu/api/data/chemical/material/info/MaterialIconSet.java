package com.gregtechceu.gtceu.api.data.chemical.material.info;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaterialIconSet {

    static {
        GTRegistries.MATERIAL_ICON_SETS.unfreeze();
    }

    public static final MaterialIconSet DULL = new MaterialIconSet(GTCEu.id("dull"), null, true);
    public static final MaterialIconSet METALLIC = new MaterialIconSet(GTCEu.id("metallic"));
    public static final MaterialIconSet MAGNETIC = new MaterialIconSet(GTCEu.id("magnetic"), METALLIC);
    public static final MaterialIconSet SHINY = new MaterialIconSet(GTCEu.id("shiny"), METALLIC);
    public static final MaterialIconSet BRIGHT = new MaterialIconSet(GTCEu.id("bright"), SHINY);
    public static final MaterialIconSet DIAMOND = new MaterialIconSet(GTCEu.id("diamond"), SHINY);
    public static final MaterialIconSet EMERALD = new MaterialIconSet(GTCEu.id("emerald"), DIAMOND);
    public static final MaterialIconSet GEM_HORIZONTAL = new MaterialIconSet(GTCEu.id("gem_horizontal"), EMERALD);
    public static final MaterialIconSet GEM_VERTICAL = new MaterialIconSet(GTCEu.id("gem_vertical"), EMERALD);
    public static final MaterialIconSet RUBY = new MaterialIconSet(GTCEu.id("ruby"), EMERALD);
    public static final MaterialIconSet OPAL = new MaterialIconSet(GTCEu.id("opal"), RUBY);
    public static final MaterialIconSet GLASS = new MaterialIconSet(GTCEu.id("glass"), RUBY);
    public static final MaterialIconSet NETHERSTAR = new MaterialIconSet(GTCEu.id("netherstar"), GLASS);
    public static final MaterialIconSet FINE = new MaterialIconSet(GTCEu.id("fine"));
    public static final MaterialIconSet SAND = new MaterialIconSet(GTCEu.id("sand"), FINE);
    public static final MaterialIconSet WOOD = new MaterialIconSet(GTCEu.id("wood"), FINE);
    public static final MaterialIconSet ROUGH = new MaterialIconSet(GTCEu.id("rough"), FINE);
    public static final MaterialIconSet FLINT = new MaterialIconSet(GTCEu.id("flint"), ROUGH);
    public static final MaterialIconSet LIGNITE = new MaterialIconSet(GTCEu.id("lignite"), ROUGH);
    public static final MaterialIconSet QUARTZ = new MaterialIconSet(GTCEu.id("quartz"), ROUGH);
    public static final MaterialIconSet CERTUS = new MaterialIconSet(GTCEu.id("certus"), QUARTZ);
    public static final MaterialIconSet LAPIS = new MaterialIconSet(GTCEu.id("lapis"), QUARTZ);
    public static final MaterialIconSet FLUID = new MaterialIconSet(GTCEu.id("fluid"));
    public static final MaterialIconSet RADIOACTIVE = new MaterialIconSet(GTCEu.id("radioactive"), METALLIC);

    // Implementation -----------------------------------------------------------------------------------------------

    public final ResourceLocation id;
    public final String name;
    public final boolean isRootIconset;

    /**
     * This can be null if {@link MaterialIconSet#isRootIconset} is true,
     * otherwise it will be Nonnull
     */
    @Nullable
    public final MaterialIconSet parentIconset;

    /**
     * Create a new MaterialIconSet whose parent is {@link MaterialIconSet#DULL}
     * 
     * @deprecated Use {@link MaterialIconSet#MaterialIconSet(ResourceLocation)} instead
     * @param name the name of the iconset
     */
    @Deprecated(since = "8.0.0")
    public MaterialIconSet(@NotNull String name) {
        this(name, MaterialIconSet.DULL);
    }

    /**
     * Create a new MaterialIconSet whose parent is one of your choosing
     * 
     * @deprecated Use {@link MaterialIconSet#MaterialIconSet(ResourceLocation, MaterialIconSet)} instead
     * @param name          the name of the iconset
     * @param parentIconset the parent iconset
     */
    @Deprecated(since = "8.0.0")
    public MaterialIconSet(@NotNull String name, @NotNull MaterialIconSet parentIconset) {
        this(name, parentIconset, false);
    }

    /**
     * Create a new MaterialIconSet which is a root
     * 
     * @deprecated Use {@link MaterialIconSet#MaterialIconSet(ResourceLocation, MaterialIconSet, boolean)} instead
     * @param name          the name of the iconset
     * @param parentIconset the parent iconset, should be null if this should be a root iconset
     * @param isRootIconset true if this should be a root iconset, otherwise false
     */
    @Deprecated(since = "8.0.0")
    public MaterialIconSet(@NotNull String name, @Nullable MaterialIconSet parentIconset, boolean isRootIconset) {
        this(GTCEu.id(name), parentIconset, isRootIconset);
    }

    /**
     * Create a new MaterialIconSet whose parent is {@link MaterialIconSet#DULL}
     *
     * @param id the id of the iconset
     */
    public MaterialIconSet(@NotNull ResourceLocation id) {
        this(id, MaterialIconSet.DULL);
    }

    /**
     * Create a new MaterialIconSet whose parent is one of your choosing
     *
     * @param id            the id of the iconset
     * @param parentIconset the parent iconset
     */
    public MaterialIconSet(@NotNull ResourceLocation id, @NotNull MaterialIconSet parentIconset) {
        this(id, parentIconset, false);
    }

    /**
     * Create a new MaterialIconSet which is a root
     *
     * @param id            the id of the iconset
     * @param parentIconset the parent iconset, should be null if this should be a root iconset
     * @param isRootIconset true if this should be a root iconset, otherwise false
     */
    public MaterialIconSet(@NotNull ResourceLocation id, @Nullable MaterialIconSet parentIconset,
                           boolean isRootIconset) {
        this.id = id;

        if (id.getPath().contains("/"))
            throw new IllegalArgumentException("MaterialIconSet id cannot have '/' %s".formatted(id));

        this.name = id.getPath();
        this.isRootIconset = isRootIconset;
        this.parentIconset = parentIconset;

        GTRegistries.MATERIAL_ICON_SETS.register(this.id, this);
    }

    /**
     * @deprecated Use {@code GTRegistries.MATERIAL_ICON_SETS.get()}
     */
    @Deprecated(since = "8.0.0")
    public static MaterialIconSet getByName(@NotNull String name) {
        return GTRegistries.MATERIAL_ICON_SETS.get(GTCEu.id(name));
    }

    public String getName() {
        return id.getPath();
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static void init() {
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.MATERIAL_ICON_SETS, MaterialIconSet.class));
        if (GTCEu.Mods.isKubeJSLoaded()) {
            GTRegistryInfo.registerFor(GTRegistries.MATERIAL_ICON_SETS.getRegistryName());
        }
        GTRegistries.MATERIAL_ICON_SETS.freeze();
    }
}
