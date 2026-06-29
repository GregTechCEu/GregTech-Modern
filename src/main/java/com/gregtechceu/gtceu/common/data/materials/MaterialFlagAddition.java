package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class MaterialFlagAddition {

    // TODO move all of these onto the respective materials' builders
    public static void register() {
        Aluminium.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Bauxite, Bauxite, Ilmenite, Rutile);
            oreProp.setWashedIn(SodiumPersulfate);
        });

        Beryllium.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Emerald, Emerald, Thorium);
        });

        Cobalt.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(CobaltOxide, Cobaltite);
            oreProp.setWashedIn(SodiumPersulfate);
        });

        Copper.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Cobalt, Gold, Nickel, Gold);
            oreProp.setWashedIn(Mercury);
        });

        Gold.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Copper, Nickel, Silver);
            oreProp.setWashedIn(Mercury);
        });

        Iron.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Nickel, Tin, Tin, Gold);
            oreProp.setWashedIn(SodiumPersulfate);
        });

        Lead.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Silver, Sulfur);
        });

        Lithium.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Lithium);
        });

        Molybdenum.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Molybdenum);
        });

        // OreProperty oreProp = Magnesium.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Olivine);

        // OreProperty oreProp = Manganese.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Chrome, Iron);
        // oreProp.setSeparatedInto(Iron);

        Neodymium.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(RareEarth);
        });

        Nickel.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Cobalt, Iron, Platinum);
            oreProp.setSeparatedInto(Iron);
            oreProp.setWashedIn(Mercury);
        });

        Platinum.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Nickel, Nickel, Cobalt, Palladium);
            oreProp.setWashedIn(Mercury);
        });

        Plutonium239.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Uraninite, Lead, Uraninite);
        });

        // OreProperty oreProp = Silicon.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(SiliconDioxide);

        Silver.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Lead, Sulfur, Sulfur, Gold);
            oreProp.setWashedIn(Mercury);
        });

        Sulfur.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur);
        });

        Thorium.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Uraninite, Lead);
        });

        Tin.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Zinc);
            oreProp.setSeparatedInto(Iron);
            oreProp.setWashedIn(SodiumPersulfate);
        });

        // OreProperty oreProp = Titanium.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Almandine);

        // OreProperty oreProp = Tungsten.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Manganese, Molybdenum);

        Naquadah.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur, Barite, NaquadahEnriched);
            oreProp.setSeparatedInto(NaquadahEnriched);

        });
        CertusQuartz.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(NetherQuartz, Barite);
        });

        Almandine.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(GarnetRed, Aluminium);
        });

        Asbestos.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Diatomite, Silicon, Magnesium);
        });

        BlueTopaz.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Topaz);
        });

        Goethite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Malachite, Limonite);
            oreProp.setSeparatedInto(Iron);
            oreProp.setDirectSmeltResult(Iron);
        });

        Calcite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Calcium, Calcium, Sodalite);
        });

        Cassiterite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Tin, Bismuth);
            oreProp.setDirectSmeltResult(Tin);
        });

        CassiteriteSand.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Tin);
            oreProp.setDirectSmeltResult(Tin);
        });

        Chalcopyrite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
            oreProp.setWashedIn(Mercury);
            oreProp.setDirectSmeltResult(Copper);
        });

        Chromite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Magnesium, Chromium);
            oreProp.setSeparatedInto(Iron);

        });
        Cinnabar.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Redstone, Sulfur, Glowstone);
        });

        Coal.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Coal, Coal, Thorium);
        });

        Cobaltite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur, Cobalt);
            oreProp.setWashedIn(SodiumPersulfate);
            oreProp.setDirectSmeltResult(Cobalt);
        });

        Cooperite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Nickel, Nickel, Cobalt, Palladium);
            oreProp.setWashedIn(Mercury);
        });

        Diamond.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Graphite);
        });

        Emerald.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Beryllium, Aluminium);
        });

        Galena.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur, Silver);
            oreProp.setWashedIn(Mercury);
            oreProp.setDirectSmeltResult(Lead);
        });

        Garnierite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Nickel);
            oreProp.setDirectSmeltResult(Nickel);
        });

        GreenSapphire.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Aluminium, Sapphire);
        });

        Grossular.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(GarnetYellow, Calcium);
        });

        Ilmenite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Rutile, Rutile, IlmeniteSlag);
            oreProp.setSeparatedInto(Iron);
            oreProp.setWashedIn(SodiumPersulfate);
        });

        Bauxite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Grossular, Rutile, Gallium);
            oreProp.setWashedIn(SodiumPersulfate);
        });

        Lazurite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sodalite, Lapis);
        });

        Magnesite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Magnesium, Magnesium, Cobaltite);
            oreProp.setDirectSmeltResult(Magnesium);
        });

        Magnetite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Gold);
            oreProp.setSeparatedInto(Gold);
            oreProp.setWashedIn(Mercury);
            oreProp.setDirectSmeltResult(Iron);
        });

        Molybdenite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Molybdenum, Sulfur, Quartzite);
            oreProp.setDirectSmeltResult(Molybdenum);
        });

        Pyrite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur, TricalciumPhosphate, Iron);
            oreProp.setSeparatedInto(Iron);
            oreProp.setDirectSmeltResult(Iron);
        });

        Pyrolusite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Manganese, Tantalite, Niobium);
            oreProp.setDirectSmeltResult(Manganese);
        });

        Pyrope.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(GarnetRed, Magnesium);
        });

        Realgar.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur, Antimony, Barite);
        });

        RockSalt.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Salt, Borax);
        });

        Ruby.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Chromium, GarnetRed, Chromium);
        });

        Salt.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(RockSalt, Borax);
        });

        Saltpeter.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Saltpeter, Potassium, Salt);
        });

        Sapphire.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Aluminium, GreenSapphire);
        });

        Scheelite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Manganese, Molybdenum, Calcium);
        });

        Sodalite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Lazurite, Lapis);
        });

        Tantalite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Manganese, Niobium, Tantalum);
        });

        Spessartine.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(GarnetRed, Manganese);
        });

        Sphalerite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(GarnetYellow, Gallium, Cadmium, Zinc);
            oreProp.setWashedIn(SodiumPersulfate);
            oreProp.setDirectSmeltResult(Zinc);
        });

        Stibnite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(AntimonyTrioxide, Antimony, Cinnabar);
            oreProp.setWashedIn(SodiumPersulfate);
            oreProp.setDirectSmeltResult(Antimony);
        });

        Tetrahedrite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Antimony, Zinc, Cadmium);
            oreProp.setWashedIn(SodiumPersulfate);
            oreProp.setDirectSmeltResult(Copper);
        });

        Topaz.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(BlueTopaz);
        });

        Tungstate.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Manganese, Silver, Lithium);
            oreProp.setWashedIn(Mercury);
        });

        Uraninite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Uraninite, Thorium, Silver);
        });

        Limonite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Nickel, Goethite, CobaltOxide);
            oreProp.setSeparatedInto(Iron);
            oreProp.setWashedIn(SodiumPersulfate);
            oreProp.setDirectSmeltResult(Iron);
        });

        NetherQuartz.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Quartzite);
        });

        Quartzite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(CertusQuartz, Barite);
        });

        Graphite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Carbon);
        });

        Bornite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
            oreProp.setWashedIn(Mercury);
            oreProp.setDirectSmeltResult(Copper);
        });

        Chalcocite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur, Massicot, Silver);
            oreProp.setDirectSmeltResult(Copper);
        });

        Bastnasite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Neodymium, RareEarth);
            oreProp.setSeparatedInto(Neodymium);
        });

        Pentlandite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Sulfur, Cobalt);
            oreProp.setSeparatedInto(Iron);
            oreProp.setWashedIn(SodiumPersulfate);
            oreProp.setDirectSmeltResult(Nickel);
        });

        Spodumene.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Aluminium, Lithium);
        });

        Lepidolite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Lithium, Caesium, Boron);
        });

        GlauconiteSand.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sodium, Aluminium, Iron);
            oreProp.setSeparatedInto(Iron);
        });

        Malachite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Goethite, Calcite, Zincite);
            oreProp.setWashedIn(SodiumPersulfate);
            oreProp.setDirectSmeltResult(Copper);
        });

        Olivine.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Pyrope, Magnesium, Manganese);
        });

        Opal.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Opal);
        });

        Amethyst.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Amethyst);
        });

        Lapis.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Lazurite, Sodalite, Pyrite);
        });

        Apatite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(TricalciumPhosphate, Phosphate, Pyrochlore);
        });

        TricalciumPhosphate.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Apatite, Phosphate, Pyrochlore);
        });

        GarnetRed.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Spessartine, Pyrope, Almandine);
        });

        GarnetYellow.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Andradite, Grossular, Uvarovite);
        });

        VanadiumMagnetite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Magnetite, Magnetite, Vanadium);
            oreProp.setSeparatedInto(Gold);
        });

        Pollucite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Caesium, Aluminium, Potassium);
        });

        Bentonite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Aluminium, Calcium, Magnesium);
        });

        FullersEarth.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Aluminium, Silicon, Magnesium);
        });

        Pitchblende.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Thorium, Uraninite, Lead);
        });

        Monazite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Thorium, Neodymium, RareEarth);
            oreProp.setSeparatedInto(Neodymium);
        });

        Redstone.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Cinnabar, RareEarth, Glowstone);
        });

        Diatomite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Hematite, Sapphire);
        });

        GraniticMineralSand.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Deepslate, Magnetite);
            oreProp.setSeparatedInto(Gold);
            oreProp.setDirectSmeltResult(Iron);
        });

        GarnetSand.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(GarnetRed, GarnetYellow);
        });

        BasalticMineralSand.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Basalt, Magnetite);
            oreProp.setSeparatedInto(Gold);
            oreProp.setDirectSmeltResult(Iron);
        });

        Hematite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Magnetite, Calcium, Magnesium);
            oreProp.setSeparatedInto(Iron);
            oreProp.setDirectSmeltResult(Iron);
        });

        Wulfenite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Manganese, Manganese, Lead);
        });

        Soapstone.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(SiliconDioxide, Magnesium, Calcite, Talc);
        });

        Kyanite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Talc, Aluminium, Silicon);
        });

        Gypsum.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sulfur, Calcium, Salt);
        });

        Talc.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Clay, Carbon, Clay);
        });

        Powellite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Iron, Potassium, Molybdenite);
        });

        Trona.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Sodium, SodaAsh, SodaAsh);
        });

        Mica.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Potassium, Aluminium);
        });

        Zeolite.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Calcium, Silicon, Aluminium);
        });

        Electrotine.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Redstone, Electrum, Diamond);
        });

        Pyrochlore.onRegister(mat -> {
            OreProperty oreProp = mat.getProperty(PropertyKey.ORE);
            oreProp.setOreByProducts(Apatite, Calcium, Niobium);
        });
    }
}
