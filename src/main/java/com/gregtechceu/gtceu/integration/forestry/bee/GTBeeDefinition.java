package com.gregtechceu.gtceu.integration.forestry.bee;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.integration.forestry.items.GTApicultureItems;
import com.gregtechceu.gtceu.integration.forestry.items.GTCombType;
import com.gregtechceu.gtceu.integration.forestry.mutation.MaterialMutationCondition;
import forestry.api.apiculture.ForestryBeeSpecies;
import forestry.api.core.HumidityType;
import forestry.api.core.TemperatureType;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import forestry.api.plugin.IApicultureRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;


//todo rename to GTBeeDefinitions
// Make this use a material property instead of hardcoding all of it, please.

//todo if you end up getting rid of the GTBeeSpecies class with all the IDs, this should be renamed GTBeeSpecies and not
// GTBeeDefinition because IBeeDefinition no longer exists in Forestry CE

public class GTBeeDefinition {

    //secret bees are replaced by .setChance(0.001f)

    public static void register(IApicultureRegistration apiculture) {
        registerOrganic(apiculture);
        registerGems(apiculture);
        registerMetals(apiculture);
        registerRareMetals(apiculture);
        registerIndustrial(apiculture);
        registerAlloys(apiculture);
        registerRadioactive(apiculture);
        registerNobleGases(apiculture);
    }

    private static void registerOrganic(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.CLAY, GTTaxa.GENUS_GT_ORGANIC, "lutum",
                        true, TextColor.fromRgb(0xC8C8DA))
                .setBody(TextColor.fromRgb(0x0000FF))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(new ItemStack(Items.CLAY_BALL), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWER);
                    genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_NONE);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_VANILLA);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.INDUSTRIOUS, ForestryBeeSpecies.DILIGENT, 10)
                            .requireResource(Blocks.TERRACOTTA.defaultBlockState());
                });

        apiculture.registerSpecies(GTBeesSpecies.SLIMEBALL, GTTaxa.GENUS_GT_ORGANIC, "bituminipila",
                        true, TextColor.fromRgb(0x4E9E55))
                .setBody(TextColor.fromRgb(0x00FF15))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(new ItemStack(Items.SLIME_BALL), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.STICKY), 0.05f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_MUSHROOMS);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWER);
                    genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
                    genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_BOTH_1);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.MARSHY, GTBeesSpecies.CLAY, 7)
                            .requireResource(Blocks.SLIME_BLOCK.defaultBlockState());
                });

        apiculture.registerSpecies(GTBeesSpecies.PEAT, GTTaxa.GENUS_GT_ORGANIC, "limus",
                        true, TextColor.fromRgb(0x906237))
                .setBody(TextColor.fromRgb(0x58300B))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.COAL), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_WHEAT);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTER);
                    genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_NONE);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.RURAL, GTBeesSpecies.CLAY, 10);
                });

        apiculture.registerSpecies(GTBeesSpecies.STICKYRESIN, GTTaxa.GENUS_GT_ORGANIC, "lenturesinae",
                        true, TextColor.fromRgb(0x2E8F5B))
                .setBody(TextColor.fromRgb(0xDCC289))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STICKY), 0.15f)
                .addSpecialty(GTItems.STICKY_RESIN.asStack(), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWER);
                    genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_NONE);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.SLIMEBALL, GTBeesSpecies.PEAT, 15)
                            .requireResource(getBlockStatesFromTag(BlockTags.create(new ResourceLocation("gtceu", "rubber_logs"))));
                });

        apiculture.registerSpecies(GTBeesSpecies.COAL, GTTaxa.GENUS_GT_ORGANIC, "carbo",
                        true, TextColor.fromRgb(0x666666))
                .setBody(TextColor.fromRgb(0x525252))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.COAL), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.COKE), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_CACTI);
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
                    genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_2);
                    genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_1);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
                    genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_CREEPER);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.INDUSTRIOUS, GTBeesSpecies.PEAT, 9)
                            .addMutationCondition(new MaterialMutationCondition(
                                    getMaterialBlock(TagPrefix.block, GTMaterials.Coal)
                            ));
                });

        apiculture.registerSpecies(GTBeesSpecies.OIL, GTTaxa.GENUS_GT_ORGANIC, "oleum",
                        true, TextColor.fromRgb(0x4C4C4C))
                .setBody(TextColor.fromRgb(0x333333))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.OIL), 0.75f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_SLOWER);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_NORMAL);
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                    genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_NONE);
                    genome.set(BeeChromosomes.HUMIDITY_TOLERANCE, ForestryAlleles.TOLERANCE_NONE);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.COAL, GTBeesSpecies.STICKYRESIN, 4);
                });

        apiculture.registerSpecies(GTBeesSpecies.ASH, GTTaxa.GENUS_GT_ORGANIC, "cinis",
                        true, TextColor.fromRgb(0x1E1A18))
                .setBody(TextColor.fromRgb(0xC6C6C6))
                .setHumidity(HumidityType.ARID)
                .setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.ASH), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL);
                    genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGE);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTER);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_WHEAT);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.COAL, GTBeesSpecies.CLAY, 10)
                            .restrictTemperature(TemperatureType.HELLISH);
                });


        apiculture.registerSpecies(GTBeesSpecies.APATITE, GTTaxa.GENUS_GT_ORGANIC, "stercorat",
                        true, TextColor.fromRgb(0x7FCEF5))
                .setBody(TextColor.fromRgb(0x654525))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.WARM)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.APATITE), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_FASTEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_WHEAT);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.ASH, GTBeesSpecies.COAL, 10)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Apatite)));
                });

        apiculture.registerSpecies(GTBeesSpecies.BIOMASS, GTTaxa.GENUS_GT_ORGANIC, "taeda",
                        true, TextColor.fromRgb(0x21E118))
                .setBody(TextColor.fromRgb(0x17AF0E))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.BIOMASS), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_FASTEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_WHEAT);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTER);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.INDUSTRIOUS, ForestryBeeSpecies.RURAL, 10)
                            .restrictBiomeType(BiomeTags.IS_FOREST);
                });

        apiculture.registerSpecies(GTBeesSpecies.FERTILIZER, GTTaxa.GENUS_GT_ORGANIC, "stercorat_fertilizer",
                        true, TextColor.fromRgb(0x7FCEF5))
                .setBody(TextColor.fromRgb(0x654525))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.WARM)
                .addProduct(ChemicalHelper.get(TagPrefix.dustTiny, GTMaterials.Ash), 0.2f)
                .addProduct(ChemicalHelper.get(TagPrefix.dustTiny, GTMaterials.DarkAsh), 0.2f)
                .addSpecialty(GTItems.FERTILIZER.asStack(), 0.3f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_FASTEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_WHEAT);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_FASTER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.ASH, GTBeesSpecies.APATITE, 8);
                });

        apiculture.registerSpecies(GTBeesSpecies.PHOSPHORUS, GTTaxa.GENUS_GT_ORGANIC, "phosphorus",
                        false, TextColor.fromRgb(0xFFC826))
                .setBody(TextColor.fromRgb(0xC1C1F6))
                .setTemperature(TemperatureType.HOT)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.PHOSPHORUS), 0.35f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.APATITE, GTBeesSpecies.ASH, 12)
                            .restrictTemperature(TemperatureType.HOT)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.TricalciumPhosphate)));
                });
    }

    private static void registerGems(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.REDSTONE, GTTaxa.GENUS_GT_GEM, "rubrumlapis",
                        true, TextColor.fromRgb(0x7D0F0F))
                .setBody(TextColor.fromRgb(0xD11919))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.REDSTONE), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.RAREEARTH), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.INDUSTRIOUS, ForestryBeeSpecies.DEMONIC, 10)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Redstone)));

                });

        apiculture.registerSpecies(GTBeesSpecies.LAPIS, GTTaxa.GENUS_GT_GEM, "lapidi",
                        true, TextColor.fromRgb(0x1947D1))
                .setBody(TextColor.fromRgb(0x476CDA))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.LAPIS), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.DEMONIC, ForestryBeeSpecies.IMPERIAL, 10)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Lapis)));

                });

        apiculture.registerSpecies(GTBeesSpecies.CERTUS, GTTaxa.GENUS_GT_GEM, "quarzeus",
                        true, TextColor.fromRgb(0x57CFFB))
                .setBody(TextColor.fromRgb(0xBBEEFF))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.CERTUS), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.HERMITIC, GTBeesSpecies.LAPIS, 10)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.CertusQuartz)));
                });

        apiculture.registerSpecies(GTBeesSpecies.DIAMOND, GTTaxa.GENUS_GT_GEM, "adamas",
                        false, TextColor.fromRgb(0xCCFFFF))
                .setBody(TextColor.fromRgb(0xA3CCCC))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.DIAMOND), 0.15f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.CERTUS, GTBeesSpecies.COAL, 3)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Diamond)));
                });

        apiculture.registerSpecies(GTBeesSpecies.RUBY, GTTaxa.GENUS_GT_GEM, "rubinus",
                        false, TextColor.fromRgb(0xE6005C))
                .setBody(TextColor.fromRgb(0xCC0052))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.RUBY), 0.15f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.REDSTONE), 0.05f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.REDSTONE, GTBeesSpecies.DIAMOND, 5)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Ruby)));
                });

        apiculture.registerSpecies(GTBeesSpecies.SAPPHIRE, GTTaxa.GENUS_GT_GEM, "sapphirus",
                        true, TextColor.fromRgb(0x0033CC))
                .setBody(TextColor.fromRgb(0x00248F))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.SAPPHIRE), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.CERTUS, GTBeesSpecies.LAPIS, 5)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Sapphire)));
                });

        apiculture.registerSpecies(GTBeesSpecies.OLIVINE, GTTaxa.GENUS_GT_GEM, "olivinum",
                        true, TextColor.fromRgb(0x248F24))
                .setBody(TextColor.fromRgb(0xCCFFCC))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.OLIVINE), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.MAGNESIUM), 0.05f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.CERTUS, ForestryBeeSpecies.ENDED, 5);
                });

        apiculture.registerSpecies(GTBeesSpecies.EMERALD, GTTaxa.GENUS_GT_GEM, "smaragdus",
                        false, TextColor.fromRgb(0x248F24))
                .setBody(TextColor.fromRgb(0x2EB82E))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.COLD)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STONE), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.EMERALD), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.ALUMINIUM), 0.05f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.OLIVINE, GTBeesSpecies.DIAMOND, 4)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Emerald)));
                });
    }

    private static void registerMetals(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.COPPER, GTTaxa.GENUS_GT_METAL, "cuprum",
                        true, TextColor.fromRgb(0xFF6600))
                .setBody(TextColor.fromRgb(0xE65C00))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.COPPER), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.GOLD), 0.05f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.MAJESTIC, GTBeesSpecies.CLAY, 13)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Copper)));
                });

        apiculture.registerSpecies(GTBeesSpecies.TIN, GTTaxa.GENUS_GT_METAL, "stannum",
                        true, TextColor.fromRgb(0xD4D4D4))
                .setBody(TextColor.fromRgb(0xDDDDDD))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.TIN), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.ZINC), 0.05f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.CLAY, ForestryBeeSpecies.DILIGENT, 13)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Tin)));
                });

        apiculture.registerSpecies(GTBeesSpecies.LEAD, GTTaxa.GENUS_GT_METAL, "plumbum",
                        true, TextColor.fromRgb(0x666699))
                .setBody(TextColor.fromRgb(0xA3A3CC))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.WARM)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.LEAD), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.SULFUR), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.COAL, GTBeesSpecies.COPPER, 13)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Lead))));

        apiculture.registerSpecies(GTBeesSpecies.IRON, GTTaxa.GENUS_GT_METAL, "ferrum",
                        true, TextColor.fromRgb(0xDA9147))
                .setBody(TextColor.fromRgb(0xDE9C59))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.IRON), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.TIN), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.TIN, GTBeesSpecies.COPPER, 13)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Iron))));

        apiculture.registerSpecies(GTBeesSpecies.STEEL, GTTaxa.GENUS_GT_METAL, "chalybe",
                        true, TextColor.fromRgb(0x808080))
                .setBody(TextColor.fromRgb(0x999999))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.WARM)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STEEL), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.IRON), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.IRON, GTBeesSpecies.COAL, 10)
                        .restrictTemperature(TemperatureType.HOT)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Steel))));

        apiculture.registerSpecies(GTBeesSpecies.NICKEL, GTTaxa.GENUS_GT_METAL, "nichelium",
                        true, TextColor.fromRgb(0x8585AD))
                .setBody(TextColor.fromRgb(0x8585AD))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.NICKEL), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.PLATINUM), 0.02f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.IRON, GTBeesSpecies.COPPER, 13)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Nickel))));

        apiculture.registerSpecies(GTBeesSpecies.ZINC, GTTaxa.GENUS_GT_METAL, "cadmiae",
                        true, TextColor.fromRgb(0xF0DEF0))
                .setBody(TextColor.fromRgb(0xF2E1F2))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.ZINC), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.GALLIUM), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.IRON, GTBeesSpecies.TIN, 13)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Zinc))));

        apiculture.registerSpecies(GTBeesSpecies.SILVER, GTTaxa.GENUS_GT_METAL, "argenti",
                        true, TextColor.fromRgb(0xC2C2D6))
                .setBody(TextColor.fromRgb(0xCECEDE))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.COLD)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SILVER), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.SULFUR), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.LEAD, GTBeesSpecies.TIN, 10)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Silver))));

        apiculture.registerSpecies(GTBeesSpecies.GOLD, GTTaxa.GENUS_GT_METAL, "aurum",
                        true, TextColor.fromRgb(0xEBC633))
                .setBody(TextColor.fromRgb(0xEDCC47))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.WARM)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.GOLD), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.NICKEL), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.LEAD, GTBeesSpecies.COPPER, 13)
                        .restrictTemperature(TemperatureType.HOT)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Gold))));

        apiculture.registerSpecies(GTBeesSpecies.ARSENIC, GTTaxa.GENUS_GT_METAL, "arsenicum",
                        true, TextColor.fromRgb(0x736C52))
                .setBody(TextColor.fromRgb(0x292412))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.WARM)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.ARSENIC), 0.15f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.ZINC, GTBeesSpecies.SILVER, 10)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Arsenic))));

        apiculture.registerSpecies(GTBeesSpecies.SILICON, GTTaxa.GENUS_GT_ORGANIC, "silex",
                        false, TextColor.fromRgb(0xADA2A7))
                .setBody(TextColor.fromRgb(0x736675))
                .addProduct(ChemicalHelper.get(TagPrefix.dust, GTMaterials.Silicon), 0.30f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOW);
                    genome.set(BeeChromosomes.TERRITORY, ForestryAlleles.TERRITORY_LARGER);
                    genome.set(BeeChromosomes.TOLERATES_RAIN, ForestryAlleles.TRUE);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.IRON, ForestryBeeSpecies.IMPERIAL, 17);
                });
    }

    private static void registerRareMetals(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.ALUMINIUM, GTTaxa.GENUS_GT_RAREMETAL, "alumen",
                        true, TextColor.fromRgb(0xB8B8FF))
                .setBody(TextColor.fromRgb(0xD6D6FF))
                .setHumidity(HumidityType.ARID)
                .setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.ALUMINIUM), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.BAUXITE), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.NICKEL, GTBeesSpecies.ZINC, 9)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Aluminium))));

        apiculture.registerSpecies(GTBeesSpecies.TITANIUM, GTTaxa.GENUS_GT_RAREMETAL, "titanus",
                        true, TextColor.fromRgb(0xCC99FF))
                .setBody(TextColor.fromRgb(0xDBB8FF))
                .setHumidity(HumidityType.ARID)
                .setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.TITANIUM), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.ALMANDINE), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.REDSTONE, GTBeesSpecies.ALUMINIUM, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Titanium))));

        apiculture.registerSpecies(GTBeesSpecies.CHROME, GTTaxa.GENUS_GT_RAREMETAL, "chroma",
                        true, TextColor.fromRgb(0xEBA1EB))
                .setBody(TextColor.fromRgb(0xF2C3F2))
                .setHumidity(HumidityType.ARID).setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.CHROME), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.MAGNESIUM), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.TITANIUM, GTBeesSpecies.RUBY, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Chromium))));

        apiculture.registerSpecies(GTBeesSpecies.MANGANESE, GTTaxa.GENUS_GT_RAREMETAL, "manganum",
                        true, TextColor.fromRgb(0xD5D5D5))
                .setBody(TextColor.fromRgb(0xAAAAAA))
                .setHumidity(HumidityType.ARID).setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.MANGANESE), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.IRON), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.TITANIUM, GTBeesSpecies.ALUMINIUM, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Manganese))));

        apiculture.registerSpecies(GTBeesSpecies.TUNGSTEN, GTTaxa.GENUS_GT_RAREMETAL, "wolframium",
                        false, TextColor.fromRgb(0x5C5C8A))
                .setBody(TextColor.fromRgb(0x7D7DA1))
                .setHumidity(HumidityType.ARID).setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.TUNGSTEN), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.MOLYBDENUM), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(ForestryBeeSpecies.HEROIC, GTBeesSpecies.MANGANESE, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Tungsten))));

        apiculture.registerSpecies(GTBeesSpecies.PLATINUM, GTTaxa.GENUS_GT_RAREMETAL, "platina",
                        false, TextColor.fromRgb(0xE6E6E6))
                .setBody(TextColor.fromRgb(0xFFFFCC))
                .setHumidity(HumidityType.ARID).setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.PLATINUM), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.IRIDIUM), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.DIAMOND, GTBeesSpecies.CHROME, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Platinum))));

        apiculture.registerSpecies(GTBeesSpecies.IRIDIUM, GTTaxa.GENUS_GT_RAREMETAL, "iris",
                        false, TextColor.fromRgb(0xDADADA))
                .setBody(TextColor.fromRgb(0xD1D1E0))
                .setHumidity(HumidityType.ARID).setTemperature(TemperatureType.HELLISH)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.IRIDIUM), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.OSMIUM), 0.05f)
                .setGlint(true)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.TUNGSTEN, GTBeesSpecies.PLATINUM, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Iridium))));

        apiculture.registerSpecies(GTBeesSpecies.OSMIUM, GTTaxa.GENUS_GT_RAREMETAL, "osmia",
                        false, TextColor.fromRgb(0x2B2BDA))
                .setBody(TextColor.fromRgb(0x8B8B8B))
                .setHumidity(HumidityType.ARID).setTemperature(TemperatureType.COLD)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.OSMIUM), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.IRIDIUM), 0.05f)
                .setGlint(true)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.TUNGSTEN, GTBeesSpecies.PLATINUM, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Osmium))));

        apiculture.registerSpecies(GTBeesSpecies.SALTY, GTTaxa.GENUS_GT_RAREMETAL, "sal",
                        true, TextColor.fromRgb(0xF0C8C8))
                .setBody(TextColor.fromRgb(0xFAFAFA))
                .setHumidity(HumidityType.NORMAL).setTemperature(TemperatureType.WARM)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.SALT), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.LITHIUM), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.CLAY, GTBeesSpecies.ALUMINIUM, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Salt))));

        apiculture.registerSpecies(GTBeesSpecies.LITHIUM, GTTaxa.GENUS_GT_RAREMETAL, "lithos",
                        false, TextColor.fromRgb(0xF0328C))
                .setBody(TextColor.fromRgb(0xE1DCFF))
                .setHumidity(HumidityType.NORMAL).setTemperature(TemperatureType.COLD)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.LITHIUM), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.SALT), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.SALTY, GTBeesSpecies.ALUMINIUM, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Lithium))));

        apiculture.registerSpecies(GTBeesSpecies.ELECTROTINE, GTTaxa.GENUS_GT_RAREMETAL, "electrum_electrotine",
                        false, TextColor.fromRgb(0x1E90FF))
                .setBody(TextColor.fromRgb(0x3CB4C8))
                .setHumidity(HumidityType.NORMAL).setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.ELECTROTINE), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.REDSTONE), 0.05f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.REDSTONE, GTBeesSpecies.GOLD, 5)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Electrotine))));

        apiculture.registerSpecies(GTBeesSpecies.SULFUR, GTTaxa.GENUS_GT_RAREMETAL, "sulphur",
                        false, TextColor.fromRgb(0x1E90FF))
                .setBody(TextColor.fromRgb(0x3CB4C8))
                .setHumidity(HumidityType.NORMAL).setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SULFUR), 0.70f)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_NORMAL))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.ASH, GTBeesSpecies.PEAT, 15));

        apiculture.registerSpecies(GTBeesSpecies.INDIUM, GTTaxa.GENUS_GT_RAREMETAL, "indicium",
                        false, TextColor.fromRgb(0xFFA9FF))
                .setBody(TextColor.fromRgb(0x8F5D99))
                .setHumidity(HumidityType.NORMAL).setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.INDIUM), 0.05f)
                .setGlint(true)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST))
                .addMutations(mutations -> mutations.add(GTBeesSpecies.LEAD, GTBeesSpecies.OSMIUM, 1)
                        .restrictBiomeType(BiomeTags.IS_END)
                        .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Indium))));
    }

    private static void registerIndustrial(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.ENERGY, GTTaxa.GENUS_GT_INDUSTRIAL, "industria",
                        false, TextColor.fromRgb(0xC11F1F))
                .setBody(TextColor.fromRgb(0xEBB9B9))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.WARM)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.ENERGY), 0.15f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
                    genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_IGNITION);
                    genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_DOWN_2);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_NETHER);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_AVERAGE);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.DEMONIC, ForestryBeeSpecies.FIENDISH, 10)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Redstone)));
                });

        apiculture.registerSpecies(GTBeesSpecies.LAPOTRON, GTTaxa.GENUS_GT_INDUSTRIAL, "azureus",
                        false, TextColor.fromRgb(0xFFEBC4))
                .setBody(TextColor.fromRgb(0xE36400))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.LAPIS), 0.20f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.ENERGY), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.LAPOTRON), 0.10f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGER);
                    genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_IGNITION);
                    genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_UP_1);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_SNOW);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_AVERAGE);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.LAPIS, GTBeesSpecies.ENERGY, 6)
                            .restrictTemperature(TemperatureType.ICY)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Lapis)));
                });

        apiculture.registerSpecies(GTBeesSpecies.EXPLOSIVE, GTTaxa.GENUS_GT_INDUSTRIAL, "explosionis",
                        false, TextColor.fromRgb(0x7E270F))
                .setBody(TextColor.fromRgb(0x747474))
                .setHumidity(HumidityType.ARID)
                .setTemperature(TemperatureType.HELLISH)
                .addProduct(GTBlocks.INDUSTRIAL_TNT.asStack(), 0.2f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                    genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_SNOWING);
                    genome.set(BeeChromosomes.TEMPERATURE_TOLERANCE, ForestryAlleles.TOLERANCE_NONE);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
                    genome.set(BeeChromosomes.FLOWER_TYPE, ForestryAlleles.FLOWER_TYPE_SNOW);
                    genome.set(BeeChromosomes.POLLINATION, ForestryAlleles.POLLINATION_AVERAGE);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.AUSTERE, GTBeesSpecies.COAL, 4)
                            .requireResource(Blocks.TNT.defaultBlockState());
                });
    }

    private static void registerAlloys(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.REDALLOY, GTTaxa.GENUS_GT_ALLOY, "rubrum",
                        false, TextColor.fromRgb(0xE60000))
                .setBody(TextColor.fromRgb(0xB80000))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.REDALLOY), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWER);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTER);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.COPPER, GTBeesSpecies.REDSTONE, 10)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.RedAlloy)));
                });

        apiculture.registerSpecies(GTBeesSpecies.STAINLESSSTEEL, GTTaxa.GENUS_GT_ALLOY, "nonferrugo",
                        false, TextColor.fromRgb(0xC8C8DC))
                .setBody(TextColor.fromRgb(0x778899))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.HOT)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.STEEL), 0.10f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.STAINLESSSTEEL), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.CHROME), 0.05f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_FAST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.EFFECT, ForestryAlleles.EFFECT_IGNITION);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.CHROME, GTBeesSpecies.STEEL, 9)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.StainlessSteel)));
                });
    }

    private static void registerRadioactive(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.URANIUM, GTTaxa.GENUS_GT_RADIOACTIVE, "ouranos",
                        true, TextColor.fromRgb(0x19AF19))
                .setBody(TextColor.fromRgb(0x169E16))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.COLD)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.URANIUM), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                })
                .addMutations(mutations -> {
                    mutations.add(ForestryBeeSpecies.AVENGING, GTBeesSpecies.PLATINUM, 2)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Uranium238)));
                });

        apiculture.registerSpecies(GTBeesSpecies.PLUTONIUM, GTTaxa.GENUS_GT_RADIOACTIVE, "plutos",
                        true, TextColor.fromRgb(0x570000))
                .setBody(TextColor.fromRgb(0x240000))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.LEAD), 0.15f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.PLUTONIUM), 0.15f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.URANIUM, GTBeesSpecies.EMERALD, 2)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Plutonium239)));
                });

        apiculture.registerSpecies(GTBeesSpecies.NAQUADAH, GTTaxa.GENUS_GT_RADIOACTIVE, "nasquis",
                        false, TextColor.fromRgb(0x003300))
                .setBody(TextColor.fromRgb(0x002400))
                .setHumidity(HumidityType.ARID)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.NAQUADAH), 0.15f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.PLUTONIUM, GTBeesSpecies.IRIDIUM, 1)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Naquadah)));
                });

        apiculture.registerSpecies(GTBeesSpecies.NAQUADRIA, GTTaxa.GENUS_GT_RADIOACTIVE, "nasquidrius",
                        false, TextColor.fromRgb(0x000000))
                .setBody(TextColor.fromRgb(0x002400))
                .setHumidity(HumidityType.ARID)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.SLAG), 0.30f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.NAQUADAH), 0.20f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.NAQUADRIA), 0.15f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.PLUTONIUM, GTBeesSpecies.IRIDIUM, 1)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Naquadria)));
                });

        apiculture.registerSpecies(GTBeesSpecies.TRINIUM, GTTaxa.GENUS_GT_RADIOACTIVE, "trinium",
                        false, TextColor.fromRgb(0xB0E0E6))
                .setBody(TextColor.fromRgb(0xC8C8D2))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.COLD)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.TRINIUM), 0.75f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.NAQUADAH), 0.10f)
                .setGlint(true)
                .setGenome(genome -> genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST))
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.IRIDIUM, GTBeesSpecies.NAQUADAH, 4)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Trinium)));
                });

        apiculture.registerSpecies(GTBeesSpecies.THORIUM, GTTaxa.GENUS_GT_RADIOACTIVE, "thorax",
                        false, TextColor.fromRgb(0x005000))
                .setBody(TextColor.fromRgb(0x001E00))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.COLD)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.THORIUM), 0.75f)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.COAL, GTBeesSpecies.URANIUM, 2)
                            .setChance(0.001f)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Thorium)));
                });

        apiculture.registerSpecies(GTBeesSpecies.LUTETIUM, GTTaxa.GENUS_GT_RADIOACTIVE, "lutetia",
                        false, TextColor.fromRgb(0x00AAFF))
                .setBody(TextColor.fromRgb(0x0059FF))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.LUTETIUM), 0.15f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.THORIUM, ForestryBeeSpecies.IMPERIAL, 1)
                            .setChance(0.001f)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Lutetium)));
                });

        apiculture.registerSpecies(GTBeesSpecies.AMERICIUM, GTTaxa.GENUS_GT_RADIOACTIVE, "libertas",
                        false, TextColor.fromRgb(0x287869))
                .setBody(TextColor.fromRgb(0x0C453A))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.NORMAL)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.AMERICIUM), 0.05f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.LUTETIUM, GTBeesSpecies.CHROME, 1)
                            .setChance(0.001f)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Americium)));
                });

        apiculture.registerSpecies(GTBeesSpecies.NEUTRONIUM, GTTaxa.GENUS_GT_RADIOACTIVE, "media",
                        false, TextColor.fromRgb(0xFFF0F0))
                .setBody(TextColor.fromRgb(0xFAFAFA))
                .setHumidity(HumidityType.DAMP)
                .setTemperature(TemperatureType.HELLISH)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.NEUTRONIUM), 0.0001f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.SPEED, ForestryAlleles.SPEED_SLOWEST);
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_LONGEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_METATURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.NAQUADRIA, GTBeesSpecies.AMERICIUM, 1)
                            .setChance(0.001f)
                            .addMutationCondition(new MaterialMutationCondition(getMaterialBlock(TagPrefix.block, GTMaterials.Neutronium)));
                });
    }

    private static void registerNobleGases(IApicultureRegistration apiculture) {

        apiculture.registerSpecies(GTBeesSpecies.HELIUM, GTTaxa.GENUS_GT_NOBLEGAS, "helium",
                        false, TextColor.fromRgb(0xFFA9FF))
                .setBody(TextColor.fromRgb(0xC8B8B4))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.HELIUM), 0.35f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.STAINLESSSTEEL, ForestryBeeSpecies.INDUSTRIOUS, 10)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.ARGON, GTTaxa.GENUS_GT_NOBLEGAS, "argon",
                        false, TextColor.fromRgb(0x89D9E1))
                .setBody(TextColor.fromRgb(0xBDA5C2))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.ARGON), 0.35f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.HELIUM, ForestryBeeSpecies.IMPERIAL, 8)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.NEON, GTTaxa.GENUS_GT_NOBLEGAS, "novum",
                        false, TextColor.fromRgb(0xFFC826))
                .setBody(TextColor.fromRgb(0xFF7200))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.NEON), 0.35f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.ARGON, GTBeesSpecies.IRON, 6)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.KRYPTON, GTTaxa.GENUS_GT_NOBLEGAS, "kryptos",
                        false, TextColor.fromRgb(0x8A97B0))
                .setBody(TextColor.fromRgb(0x160822))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.KRYPTON), 0.35f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.NEON, ForestryBeeSpecies.AVENGING, 4)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.XENON, GTTaxa.GENUS_GT_NOBLEGAS, "hostis",
                        false, TextColor.fromRgb(0x8A97B0))
                .setBody(TextColor.fromRgb(0x160822))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.XENON), 0.525f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.KRYPTON, ForestryBeeSpecies.EDENIC, 2)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.OXYGEN, GTTaxa.GENUS_GT_NOBLEGAS, "oxygeni",
                        false, TextColor.fromRgb(0xFFFFFF))
                .setBody(TextColor.fromRgb(0x8F8FFF))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.OXYGEN), 0.45f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.HYDROGEN), 0.20f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.HELIUM, ForestryBeeSpecies.ENDED, 15)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.HYDROGEN, GTTaxa.GENUS_GT_NOBLEGAS, "hydrogenium",
                        false, TextColor.fromRgb(0xFFFFFF))
                .setBody(TextColor.fromRgb(0xFF1493))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.HYDROGEN), 0.45f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.NITROGEN), 0.20f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.OXYGEN, ForestryBeeSpecies.INDUSTRIOUS, 15)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.NITROGEN, GTTaxa.GENUS_GT_NOBLEGAS, "nitrogenium",
                        false, TextColor.fromRgb(0xFFC832))
                .setBody(TextColor.fromRgb(0xA52A2A))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.NITROGEN), 0.45f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack(GTCombType.FLUORINE), 0.20f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.OXYGEN, GTBeesSpecies.HYDROGEN, 15)
                            .restrictTemperature(TemperatureType.ICY);
                });

        apiculture.registerSpecies(GTBeesSpecies.FLUORINE, GTTaxa.GENUS_GT_NOBLEGAS, "fluens",
                        false, TextColor.fromRgb(0x86AFF0))
                .setBody(TextColor.fromRgb(0xFF6D00))
                .setHumidity(HumidityType.NORMAL)
                .setTemperature(TemperatureType.ICY)
                .addProduct(GTApicultureItems.BEE_COMBS.stack(GTCombType.FLUORINE), 0.45f)
                .addSpecialty(GTApicultureItems.BEE_COMBS.stack (GTCombType.OXYGEN), 0.20f)
                .setGlint(true)
                .setGenome(genome -> {
                    genome.set(BeeChromosomes.LIFESPAN, ForestryAlleles.LIFESPAN_SHORTEST);
                    genome.set(BeeChromosomes.ACTIVITY, ForestryAlleles.ACTIVITY_NOCTURNAL);
                })
                .addMutations(mutations -> {
                    mutations.add(GTBeesSpecies.NITROGEN, GTBeesSpecies.HYDROGEN, 15)
                            .restrictTemperature(TemperatureType.ICY);
                });
    }

    @Nullable
    private static Supplier<Block> getMaterialBlock(TagPrefix prefix, Material material) {
        return () -> {
            var entry = GTMaterialBlocks.MATERIAL_BLOCKS.get(prefix, material);
            if (entry == null) return null;
            return entry.get();
        };
    }


    private static BlockState[] getBlockStatesFromTag(TagKey<Block> tag) {
        return StreamSupport.stream(BuiltInRegistries.BLOCK.getTagOrEmpty(tag).spliterator(), false)
                .flatMap(holder -> holder.value().getStateDefinition().getPossibleStates().stream())
                .toArray(BlockState[]::new);
    }
}
