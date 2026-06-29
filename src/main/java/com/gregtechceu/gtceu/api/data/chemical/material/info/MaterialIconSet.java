package com.gregtechceu.gtceu.api.data.chemical.material.info;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class MaterialIconSet {

    public static final Holder<MaterialIconSet> DULL = REGISTRATE.materialIconSet("dull", null);
    public static final Holder<MaterialIconSet> METALLIC = REGISTRATE.materialIconSet("metallic");
    public static final Holder<MaterialIconSet> MAGNETIC = REGISTRATE.materialIconSet("magnetic", METALLIC);
    public static final Holder<MaterialIconSet> SHINY = REGISTRATE.materialIconSet("shiny", METALLIC);
    public static final Holder<MaterialIconSet> BRIGHT = REGISTRATE.materialIconSet("bright", SHINY);
    public static final Holder<MaterialIconSet> DIAMOND = REGISTRATE.materialIconSet("diamond", SHINY);
    public static final Holder<MaterialIconSet> EMERALD = REGISTRATE.materialIconSet("emerald", DIAMOND);
    public static final Holder<MaterialIconSet> GEM_HORIZONTAL = REGISTRATE.materialIconSet("gem_horizontal", EMERALD);
    public static final Holder<MaterialIconSet> GEM_VERTICAL = REGISTRATE.materialIconSet("gem_vertical", EMERALD);
    public static final Holder<MaterialIconSet> RUBY = REGISTRATE.materialIconSet("ruby", EMERALD);
    public static final Holder<MaterialIconSet> OPAL = REGISTRATE.materialIconSet("opal", RUBY);
    public static final Holder<MaterialIconSet> GLASS = REGISTRATE.materialIconSet("glass", RUBY);
    public static final Holder<MaterialIconSet> NETHERSTAR = REGISTRATE.materialIconSet("netherstar", GLASS);
    public static final Holder<MaterialIconSet> FINE = REGISTRATE.materialIconSet("fine");
    public static final Holder<MaterialIconSet> SAND = REGISTRATE.materialIconSet("sand", FINE);
    public static final Holder<MaterialIconSet> WOOD = REGISTRATE.materialIconSet("wood", FINE);
    public static final Holder<MaterialIconSet> ROUGH = REGISTRATE.materialIconSet("rough", FINE);
    public static final Holder<MaterialIconSet> FLINT = REGISTRATE.materialIconSet("flint", ROUGH);
    public static final Holder<MaterialIconSet> LIGNITE = REGISTRATE.materialIconSet("lignite", ROUGH);
    public static final Holder<MaterialIconSet> QUARTZ = REGISTRATE.materialIconSet("quartz", ROUGH);
    public static final Holder<MaterialIconSet> CERTUS = REGISTRATE.materialIconSet("certus", QUARTZ);
    public static final Holder<MaterialIconSet> LAPIS = REGISTRATE.materialIconSet("lapis", QUARTZ);
    public static final Holder<MaterialIconSet> FLUID = REGISTRATE.materialIconSet("fluid");
    public static final Holder<MaterialIconSet> RADIOACTIVE = REGISTRATE.materialIconSet("radioactive", METALLIC);

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

    public MaterialIconSet(@NotNull ResourceLocation id, @Nullable MaterialIconSet parentIconset) {
        this(id, parentIconset, parentIconset == null);
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

    public static void init() {}
}
