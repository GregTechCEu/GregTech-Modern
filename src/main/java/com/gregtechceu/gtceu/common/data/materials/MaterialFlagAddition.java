package com.gregtechceu.gtceu.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;

import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class MaterialFlagAddition {

    public static void register() {
        OreProperty oreProp = Aluminium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Bauxite, Bauxite, Ilmenite, Rutile);
        oreProp.setWashedIn(SodiumPersulfate);

        oreProp = Beryllium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Emerald, Emerald, Thorium);

        oreProp = Cobalt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(CobaltOxide, Cobaltite);
        oreProp.setWashedIn(SodiumPersulfate);

        oreProp = Copper.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobalt, Gold, Nickel, Gold);
        oreProp.setWashedIn(Mercury);

        oreProp = Gold.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Copper, Nickel, Silver);
        oreProp.setWashedIn(Mercury);

        oreProp = Iron.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Tin, Tin, Gold);
        oreProp.setWashedIn(SodiumPersulfate);

        oreProp = Lead.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Silver, Sulfur);

        oreProp = Lithium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lithium);

        oreProp = Molybdenum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Molybdenum);

        // oreProp = Magnesium.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Olivine);

        // oreProp = Manganese.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Chrome, Iron);
        // oreProp.setSeparatedInto(Iron);

        oreProp = Neodymium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(RareEarth);

        oreProp = Nickel.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cobalt, Iron, Platinum);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(Mercury);

        oreProp = Platinum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Nickel, Cobalt, Palladium);
        oreProp.setWashedIn(Mercury);

        oreProp = Plutonium239.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uraninite, Lead, Uraninite);

        // oreProp = Silicon.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(SiliconDioxide);

        oreProp = Silver.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lead, Sulfur, Sulfur, Gold);
        oreProp.setWashedIn(Mercury);

        oreProp = Sulfur.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur);

        oreProp = Thorium.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uraninite, Lead);

        oreProp = Tin.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Zinc);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(SodiumPersulfate);

        // oreProp = Titanium.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Almandine);

        // oreProp = Tungsten.getProperty(PropertyKey.ORE);
        // oreProp.setOreByProducts(Manganese, Molybdenum);

        oreProp = Naquadah.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Barite, NaquadahEnriched);
        oreProp.setSeparatedInto(NaquadahEnriched);

        oreProp = CertusQuartz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(NetherQuartz, Barite);

        oreProp = Almandine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Aluminium);

        oreProp = Asbestos.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Diatomite, Silicon, Magnesium);

        oreProp = BlueTopaz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Topaz);

        oreProp = Goethite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Malachite, Limonite);
        oreProp.setSeparatedInto(Iron);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Calcite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Calcium, Calcium, Sodalite);

        oreProp = Cassiterite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Tin, Bismuth);
        oreProp.setDirectSmeltResult(Tin);

        oreProp = CassiteriteSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Tin);
        oreProp.setDirectSmeltResult(Tin);

        oreProp = Chalcopyrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Chromite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Magnesium, Chromium);
        oreProp.setSeparatedInto(Iron);

        oreProp = Cinnabar.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Redstone, Sulfur, Glowstone);

        oreProp = Coal.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Coal, Coal, Thorium);

        oreProp = Cobaltite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Cobalt);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Cobalt);

        oreProp = Cooperite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Nickel, Cobalt, Palladium);
        oreProp.setWashedIn(Mercury);

        oreProp = Diamond.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Graphite);

        oreProp = Emerald.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Beryllium, Aluminium);

        oreProp = Galena.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Silver);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Lead);

        oreProp = Garnierite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Nickel);
        oreProp.setDirectSmeltResult(Nickel);

        oreProp = GreenSapphire.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Sapphire);

        oreProp = Grossular.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetYellow, Calcium);

        oreProp = Ilmenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Rutile, Rutile, IlmeniteSlag);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(SodiumPersulfate);

        oreProp = Bauxite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Grossular, Rutile, Gallium);
        oreProp.setWashedIn(SodiumPersulfate);

        oreProp = Lazurite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodalite, Lapis);

        oreProp = Magnesite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Magnesium, Magnesium, Cobaltite);
        oreProp.setDirectSmeltResult(Magnesium);

        oreProp = Magnetite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Gold);
        oreProp.setSeparatedInto(Gold);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Molybdenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Molybdenum, Sulfur, Quartzite);
        oreProp.setDirectSmeltResult(Molybdenum);

        oreProp = Pyrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, TricalciumPhosphate, Iron);
        oreProp.setSeparatedInto(Iron);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Pyrolusite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Tantalite, Niobium);
        oreProp.setDirectSmeltResult(Manganese);

        oreProp = Pyrope.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Magnesium);

        oreProp = Realgar.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Antimony, Barite);

        oreProp = RockSalt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Salt, Borax);

        oreProp = Ruby.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Chromium, GarnetRed, Chromium);

        oreProp = Salt.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(RockSalt, Borax);

        oreProp = Saltpeter.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Saltpeter, Potassium, Salt);

        oreProp = Sapphire.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, GreenSapphire);

        oreProp = Scheelite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Molybdenum, Calcium);

        oreProp = Sodalite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lazurite, Lapis);

        oreProp = Tantalite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Niobium, Tantalum);

        oreProp = Spessartine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, Manganese);

        oreProp = Sphalerite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetYellow, Gallium, Cadmium, Zinc);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Zinc);

        oreProp = Stibnite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(AntimonyTrioxide, Antimony, Cinnabar);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Antimony);

        oreProp = Tetrahedrite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Antimony, Zinc, Cadmium);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Topaz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(BlueTopaz);

        oreProp = Tungstate.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Manganese, Silver, Lithium);
        oreProp.setWashedIn(Mercury);

        oreProp = Uraninite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Uraninite, Thorium, Silver);

        oreProp = Limonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Nickel, Goethite, CobaltOxide);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = NetherQuartz.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Quartzite);

        oreProp = Quartzite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(CertusQuartz, Barite);

        oreProp = Graphite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Carbon);

        oreProp = Bornite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        oreProp.setWashedIn(Mercury);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Chalcocite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Massicot, Silver);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Bastnasite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Neodymium, RareEarth);
        oreProp.setSeparatedInto(Neodymium);

        oreProp = Pentlandite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Sulfur, Cobalt);
        oreProp.setSeparatedInto(Iron);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Nickel);

        oreProp = Spodumene.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Lithium);

        oreProp = Lepidolite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lithium, Caesium, Boron);

        oreProp = GlauconiteSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodium, Aluminium, Iron);
        oreProp.setSeparatedInto(Iron);

        oreProp = Malachite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Goethite, Calcite, Zincite);
        oreProp.setWashedIn(SodiumPersulfate);
        oreProp.setDirectSmeltResult(Copper);

        oreProp = Olivine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Pyrope, Magnesium, Manganese);

        oreProp = Opal.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Opal);

        oreProp = Amethyst.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Amethyst);

        oreProp = Lapis.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Lazurite, Sodalite, Pyrite);

        oreProp = Apatite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(TricalciumPhosphate, Phosphate, Pyrochlore);

        oreProp = TricalciumPhosphate.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Apatite, Phosphate, Pyrochlore);

        oreProp = GarnetRed.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Spessartine, Pyrope, Almandine);

        oreProp = GarnetYellow.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Andradite, Grossular, Uvarovite);

        oreProp = VanadiumMagnetite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Magnetite, Magnetite, Vanadium);
        oreProp.setSeparatedInto(Gold);

        oreProp = Pollucite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Caesium, Aluminium, Potassium);

        oreProp = Bentonite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Calcium, Magnesium);

        oreProp = FullersEarth.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Aluminium, Silicon, Magnesium);

        oreProp = Pitchblende.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Thorium, Uraninite, Lead);

        oreProp = Monazite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Thorium, Neodymium, RareEarth);
        oreProp.setSeparatedInto(Neodymium);

        oreProp = Redstone.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Cinnabar, RareEarth, Glowstone);

        oreProp = Diatomite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Hematite, Sapphire);

        oreProp = GraniticMineralSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Deepslate, Magnetite);
        oreProp.setSeparatedInto(Gold);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = GarnetSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(GarnetRed, GarnetYellow);

        oreProp = BasalticMineralSand.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Basalt, Magnetite);
        oreProp.setSeparatedInto(Gold);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Hematite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Magnetite, Calcium, Magnesium);
        oreProp.setSeparatedInto(Iron);
        oreProp.setDirectSmeltResult(Iron);

        oreProp = Wulfenite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Manganese, Manganese, Lead);

        oreProp = Soapstone.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(SiliconDioxide, Magnesium, Calcite, Talc);

        oreProp = Kyanite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Talc, Aluminium, Silicon);

        oreProp = Gypsum.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sulfur, Calcium, Salt);

        oreProp = Talc.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Clay, Carbon, Clay);

        oreProp = Powellite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Iron, Potassium, Molybdenite);

        oreProp = Trona.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Sodium, SodaAsh, SodaAsh);

        oreProp = Mica.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Potassium, Aluminium);

        oreProp = Zeolite.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Calcium, Silicon, Aluminium);

        oreProp = Electrotine.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Redstone, Electrum, Diamond);

        oreProp = Pyrochlore.getProperty(PropertyKey.ORE);
        oreProp.setOreByProducts(Apatite, Calcium, Niobium);
    }
}
