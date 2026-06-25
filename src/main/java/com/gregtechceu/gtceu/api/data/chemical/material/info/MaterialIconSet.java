package com.gregtechceu.gtceu.api.data.chemical.material.info;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class MaterialIconSet {

    static {
        GTRegistries.MATERIAL_ICON_SETS.unfreeze();
    }

    public static final MaterialIconSet DULL = REGISTRATE.materialIconSet("dull", null, true);
    public static final MaterialIconSet METALLIC = REGISTRATE.materialIconSet("metallic");
    public static final MaterialIconSet MAGNETIC = REGISTRATE.materialIconSet("magnetic", METALLIC);
    public static final MaterialIconSet SHINY = REGISTRATE.materialIconSet("shiny", METALLIC);
    public static final MaterialIconSet BRIGHT = REGISTRATE.materialIconSet("bright", SHINY);
    public static final MaterialIconSet DIAMOND = REGISTRATE.materialIconSet("diamond", SHINY);
    public static final MaterialIconSet EMERALD = REGISTRATE.materialIconSet("emerald", DIAMOND);
    public static final MaterialIconSet GEM_HORIZONTAL = REGISTRATE.materialIconSet("gem_horizontal", EMERALD);
    public static final MaterialIconSet GEM_VERTICAL = REGISTRATE.materialIconSet("gem_vertical", EMERALD);
    public static final MaterialIconSet RUBY = REGISTRATE.materialIconSet("ruby", EMERALD);
    public static final MaterialIconSet OPAL = REGISTRATE.materialIconSet("opal", RUBY);
    public static final MaterialIconSet GLASS = REGISTRATE.materialIconSet("glass", RUBY);
    public static final MaterialIconSet NETHERSTAR = REGISTRATE.materialIconSet("netherstar", GLASS);
    public static final MaterialIconSet FINE = REGISTRATE.materialIconSet("fine");
    public static final MaterialIconSet SAND = REGISTRATE.materialIconSet("sand", FINE);
    public static final MaterialIconSet WOOD = REGISTRATE.materialIconSet("wood", FINE);
    public static final MaterialIconSet ROUGH = REGISTRATE.materialIconSet("rough", FINE);
    public static final MaterialIconSet FLINT = REGISTRATE.materialIconSet("flint", ROUGH);
    public static final MaterialIconSet LIGNITE = REGISTRATE.materialIconSet("lignite", ROUGH);
    public static final MaterialIconSet QUARTZ = REGISTRATE.materialIconSet("quartz", ROUGH);
    public static final MaterialIconSet CERTUS = REGISTRATE.materialIconSet("certus", QUARTZ);
    public static final MaterialIconSet LAPIS = REGISTRATE.materialIconSet("lapis", QUARTZ);
    public static final MaterialIconSet FLUID = REGISTRATE.materialIconSet("fluid");
    public static final MaterialIconSet RADIOACTIVE = REGISTRATE.materialIconSet("radioactive", METALLIC);

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
     * Create a new MaterialIconSet which is a root
     *
     * @param id            the id of the iconset
     * @param parentIconset the parent iconset, should be null if this should be a root iconset
     * @param isRootIconset true if this should be a root iconset, otherwise false
     */
    @ApiStatus.Internal
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
