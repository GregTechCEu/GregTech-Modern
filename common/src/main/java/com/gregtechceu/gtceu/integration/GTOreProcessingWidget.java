package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.gui.GuiTextures.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.Water;

/**
 * @author Rundas
 * @implNote GTOreProcessingWidget
 */
public class GTOreProcessingWidget extends WidgetGroup {
    public GTOreProcessingWidget(Material material) {
        super(0, 0, 186, 174);
        setClientSideWidget();
        //Handlers Setup
        List<List<ItemStack>> mainproducts = new ArrayList<>();
        List<List<ItemStack>> byproducts = new ArrayList<>();
        List<List<ItemStack>> machines = new ArrayList<>();
        List<Content> chanceContent = new ArrayList<>();
        OreProperty prop = material.getProperty(PropertyKey.ORE);
        //Items
        addItemSlots(mainproducts, byproducts, material, prop, chanceContent);
        //Machines
        addMachineSlots(machines);
        //GUI
        setupGui(mainproducts, byproducts, machines, material, prop, chanceContent);
    }

    private void setupGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines, Material material, OreProperty prop, List<Content> chanceContent) {
        setupBaseGui(mainproducts, byproducts, machines, chanceContent);
        if(!material.hasProperty(PropertyKey.BLAST)){
            setupSmeltGui(mainproducts, machines);
        }
        if(prop.getWashedIn().getLeft() != null){
            setupChemGui(mainproducts,byproducts,machines,prop,chanceContent);
        }
        if(prop.getSeparatedInto() != null && !prop.getSeparatedInto().isEmpty()) {
            setupSepGui(mainproducts,byproducts,machines,chanceContent);
        }
        if(material.hasProperty(PropertyKey.GEM)){
            setupSiftGui(mainproducts, byproducts,machines,chanceContent);
        }
    }

    //Base

    private void setupBaseGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines, List<Content> chanceContent) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_BASE));
        setupBaseGuiItems(mainproducts, byproducts, chanceContent);
        setupBaseGuiMachines(machines);
    }

    private void setupBaseGuiMachines(List<List<ItemStack>> machines) {
        //Ore -> Crushed Ore
        PhantomSlotWidget maceratorSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),1,3,25);
        maceratorSlot.setBackgroundTexture(null);
        addWidget(maceratorSlot);
        //Crushed Ore -> Impure Dust
        PhantomSlotWidget maceratorSlot2 = new PhantomSlotWidget(new CycleItemStackHandler(machines),1,23,70);
        maceratorSlot2.setBackgroundTexture(null);
        addWidget(maceratorSlot2);
        //Washed Ore -> Pure Dust
        PhantomSlotWidget maceratorSlot3 = new PhantomSlotWidget(new CycleItemStackHandler(machines),1,114,47);
        maceratorSlot3.setBackgroundTexture(null);
        addWidget(maceratorSlot3);
        //TC'ed Ore -> Dust
        PhantomSlotWidget maceratorSlot4 = new PhantomSlotWidget(new CycleItemStackHandler(machines),1,70,80);
        maceratorSlot4.setBackgroundTexture(null);
        addWidget(maceratorSlot4);
        //Crushed Ore -> Washed Ore
        PhantomSlotWidget washerSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),2,25,25);
        washerSlot.setBackgroundTexture(null);
        addWidget(washerSlot);
        TankWidget waterSlot = new TankWidget(new FluidStorage(Water.getFluid(1000)),42,25,false,false);
        waterSlot.initTemplate();
        addWidget(waterSlot);
        //Impure Dust -> Dust
        PhantomSlotWidget centrifugeSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),4,51,80);
        centrifugeSlot.setBackgroundTexture(null);
        addWidget(centrifugeSlot);
        //Pure Dust -> Dust
        PhantomSlotWidget centrifugeSlot2 = new PhantomSlotWidget(new CycleItemStackHandler(machines),4,133,70);
        centrifugeSlot2.setBackgroundTexture(null);
        addWidget(centrifugeSlot2);
        //Crushed Ore/Washed Ore -> TC'ed Ore
        PhantomSlotWidget thermalCentrifugeSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),5,97,70);
        thermalCentrifugeSlot.setBackgroundTexture(null);
        addWidget(thermalCentrifugeSlot);
        //Crushed Ore -> Washed Ore
        PhantomSlotWidget cauldronWasherSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),8,4,124);
        cauldronWasherSlot.setBackgroundTexture(null);
        addWidget(cauldronWasherSlot);
        //Impure Dust -> Dust
        PhantomSlotWidget cauldronWasherSlot2 = new PhantomSlotWidget(new CycleItemStackHandler(machines),8,42,144);
        cauldronWasherSlot2.setBackgroundTexture(null);
        addWidget(cauldronWasherSlot2);
        //Pure Dust -> Dust
        PhantomSlotWidget cauldronWasherSlot3 = new PhantomSlotWidget(new CycleItemStackHandler(machines),8,103,144);
        cauldronWasherSlot3.setBackgroundTexture(null);
        addWidget(cauldronWasherSlot3);
    }

    private void setupBaseGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<Content> chanceContent) {
        //Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),0,3,3));
        //Crushing Ore
        PhantomSlotWidget crushedSlot = new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),2,3,47);
        addWidget(crushedSlot);
        //Crushing Ore BP
        PhantomSlotWidget crushedOreBPSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),0,3,65);
        crushedOreBPSlot.setOverlay(chanceContent.get(1).createOverlay(false));
        crushedOreBPSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 14f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 8.5) + "%"));
        });
        addWidget(crushedOreBPSlot);
        //Washing Crushed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),3,64,25));
        //Washing Crushed Ore BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),1,82,25));
        //Crushing Crushed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),5,23,92));
        //Crushing Crushed Ore BP
        PhantomSlotWidget crushingCrushedOreBPSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),0,23,110);
        crushingCrushedOreBPSlot.setOverlay(chanceContent.get(2).createOverlay(false));
        crushingCrushedOreBPSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 14f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 8.5) + "%"));
        });
        addWidget(crushingCrushedOreBPSlot);
        //Centrifuging Impure Dust
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,51,101));
        //Centrifuging Impure Dust BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),5,51,119));
        //Crushing Washed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),6,137,47));
        //Crushing Washed Ore BP
        PhantomSlotWidget crushingWashedOreBPSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),3,155,47);
        crushingWashedOreBPSlot.setOverlay(chanceContent.get(3).createOverlay(false));
        crushingWashedOreBPSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 14f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 8.5) + "%"));
        });
        addWidget(crushingWashedOreBPSlot);
        //Centrifuging Pure Dust
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,133,92));
        //Centrifuging Pure Dust BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),6,133,110));
        //Centrifuging Impure Dust
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,51,101));
        //Centrifuging Impure Dust BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),5,51,119));
        //TC'ing Crushed/Washed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),4,97,92));
        //TC'ing Crushed/Washed Ore BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),7,97,110));
        //Crushing TC'ed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,70,101));
        //Crushing TC'ed Ore BP
        PhantomSlotWidget crushingTCedOreBPSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),4,70,119);
        crushingTCedOreBPSlot.setOverlay(chanceContent.get(4).createOverlay(false));
        crushingTCedOreBPSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 14f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 8.5) + "%"));
        });
        addWidget(crushingTCedOreBPSlot);
        //Simple Washing Crushed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),9,3,105));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),3,3,145));
        //Simple Washing Impure Dust
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),5,23,145));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,63,145));
        //Simple Washing Pure Dust
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),6,84,145));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,124,145));
    }

    //Smelt

    private void setupSmeltGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> machines) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_SMELT));
        setupSmeltGuiItems(mainproducts);
        setupSmeltGuiMachines(machines);
    }

    private void setupSmeltGuiMachines(List<List<ItemStack>> machines) {
        PhantomSlotWidget furnaceSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),0,23,3);
        furnaceSlot.setBackgroundTexture(null);
        addWidget(furnaceSlot);
    }

    private void setupSmeltGuiItems(List<List<ItemStack>> mainproducts) {
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),1,46,3));
    }

    //Bath

    private void setupChemGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines, OreProperty prop, List<Content> chanceContent) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_CHEM));
        setupChemGuiItems(mainproducts,byproducts,chanceContent);
        setupChemGuiMachines(machines,prop);
    }

    private void setupChemGuiMachines(List<List<ItemStack>> machines, OreProperty prop) {
        Pair<Material,Integer> reagent = prop.getWashedIn();
        PhantomSlotWidget chembathSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),3,25,48);
        chembathSlot.setBackgroundTexture(null);
        addWidget(chembathSlot);
        TankWidget washingReagentSlot = new TankWidget(new FluidStorage(reagent.getLeft().getFluid(reagent.getRight())),42,48,false,false);
        washingReagentSlot.initTemplate();
        addWidget(washingReagentSlot);
    }

    private void setupChemGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<Content> chanceContent) {
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),3,64,48));
        PhantomSlotWidget bathingCrushedOreBPSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),8,82,48);
        bathingCrushedOreBPSlot.setOverlay(chanceContent.get(5).createOverlay(false));
        bathingCrushedOreBPSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 70f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 5.8) + "%"));
        });
        addWidget(bathingCrushedOreBPSlot);
    }

    //Sep

    private void setupSepGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines, List<Content> chanceContent) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_SEP));
        setupSepGuiItems(mainproducts,byproducts,chanceContent);
        setupSepGuiMachines(machines);
    }

    private void setupSepGuiMachines(List<List<ItemStack>> machines) {
        PhantomSlotWidget separatorSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),7,155,69);
        separatorSlot.setBackgroundTexture(null);
        addWidget(separatorSlot);
    }

    private void setupSepGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<Content> chanceContent) {
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,155,92));
        PhantomSlotWidget separatorBPSlot1 = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),9,155,110);
        separatorBPSlot1.setOverlay(chanceContent.get(6).createOverlay(false));
        separatorBPSlot1.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 40f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 8.5) + "%"));
        });
        addWidget(separatorBPSlot1);
        PhantomSlotWidget separatorBPSlot2 = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),10,155,128);
        separatorBPSlot2.setOverlay(chanceContent.get(7).createOverlay(false));
        separatorBPSlot2.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 20f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 6f) + "%"));
        });
        addWidget(separatorBPSlot2);
    }

    //Sift

    private void setupSiftGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines, List<Content> chanceContent) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_SIFT));
        setupSiftGuiItems(mainproducts,byproducts,chanceContent);
        setupSiftGuiMachines(machines);
    }

    private void setupSiftGuiMachines(List<List<ItemStack>> machines) {
        PhantomSlotWidget sifterSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),6,101,24);
        sifterSlot.setBackgroundTexture(null);
        addWidget(sifterSlot);
    }

    private void setupSiftGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<Content> chanceContent) {
        PhantomSlotWidget exquisiteSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),11,119,3);
        exquisiteSlot.setOverlay(chanceContent.get(8).createOverlay(false));
        exquisiteSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 3f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 1f) + "%"));
        });
        addWidget(exquisiteSlot);
        PhantomSlotWidget flawlessSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),12,137,3);
        flawlessSlot.setOverlay(chanceContent.get(9).createOverlay(false));
        flawlessSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 3f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 1f) + "%"));
        });
        addWidget(flawlessSlot);
        PhantomSlotWidget gemSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),13,155,3);
        gemSlot.setOverlay(chanceContent.get(10).createOverlay(false));
        gemSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 3f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 1f) + "%"));
        });
        addWidget(gemSlot);
        PhantomSlotWidget dustSlot = new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),6,119,21);
        dustSlot.setOverlay(chanceContent.get(0).createOverlay(false));
        dustSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 3f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 1f) + "%"));
        });
        addWidget(dustSlot);
        PhantomSlotWidget flawedSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),14,137,21);
        flawlessSlot.setOverlay(chanceContent.get(11).createOverlay(false));
        flawedSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 3f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 1f) + "%"));
        });
        addWidget(flawedSlot);
        PhantomSlotWidget chippedSlot = new PhantomSlotWidget(new CycleItemStackHandler(byproducts),15,155,21);
        chippedSlot.setOverlay(chanceContent.get(12).createOverlay(false));
        chippedSlot.setOnAddedTooltips((w, tooltips) -> {
            tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 3f) + "%"));
            tooltips.add(Component.translatable("gtceu.gui.content.tier_boost", String.format("%.1f", 1f) + "%"));
        });
        addWidget(chippedSlot);
    }

    //Slots

    private void addMachineSlots(List<List<ItemStack>> machines) {
        List<ItemStack> furnaceSlot = Collections.singletonList(new ItemStack(Blocks.FURNACE.asItem()));
        machines.add(furnaceSlot);
        List<ItemStack> maceratorSlot = Collections.singletonList(MACERATOR[GTValues.LV].asStack());
        machines.add(maceratorSlot);
        List<ItemStack> washerSlot = Collections.singletonList(ORE_WASHER[GTValues.LV].asStack());
        machines.add(washerSlot);
        List<ItemStack> bathSlot = Collections.singletonList(CHEMICAL_BATH[GTValues.LV].asStack());
        machines.add(bathSlot);
        List<ItemStack> centrifugeSlot = Collections.singletonList(CENTRIFUGE[GTValues.LV].asStack());
        machines.add(centrifugeSlot);
        List<ItemStack> thermalCentrifugeSlot = Collections.singletonList(THERMAL_CENTRIFUGE[GTValues.LV].asStack());
        machines.add(thermalCentrifugeSlot);
        List<ItemStack> sifterSlot = Collections.singletonList(SIFTER[GTValues.LV].asStack());
        machines.add(sifterSlot);
        List<ItemStack> separatorSlot = Collections.singletonList(ELECTROMAGNETIC_SEPARATOR[GTValues.LV].asStack());
        machines.add(separatorSlot);
        List<ItemStack> cauldronWasherSlot = new ArrayList<>();
        cauldronWasherSlot.add(new ItemStack(Items.CAULDRON));
        cauldronWasherSlot.add(ORE_WASHER[GTValues.LV].asStack());
        machines.add(cauldronWasherSlot);
    }

    private void addItemSlots(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, Material material, OreProperty prop, List<Content> chanceContent) {
        setupMainProducts(mainproducts, material, prop, chanceContent);
        setupByproducts(byproducts, material, prop, chanceContent);
    }

    private void setupMainProducts(List<List<ItemStack>> mainproducts, Material material, OreProperty prop, List<Content> chanceContent) {
        //Ore
        List<ItemStack> oreSlot = Collections.singletonList(ChemicalHelper.get(ore, material));
        mainproducts.add(oreSlot);
        //Direct Smelt Result
        Material smeltingResult = prop.getDirectSmeltResult() != null ? prop.getDirectSmeltResult() : material;
        List<ItemStack> smeltSlot = new ArrayList<>();
        if(smeltingResult.hasProperty(PropertyKey.INGOT)){
            smeltSlot.add(ChemicalHelper.get(ingot,smeltingResult));
        }else if(smeltingResult.hasProperty(PropertyKey.GEM)) {
            smeltSlot.add(ChemicalHelper.get(gem,smeltingResult));
        }else if(smeltingResult.hasProperty(PropertyKey.DUST)){
            smeltSlot.add(ChemicalHelper.get(dust,smeltingResult));
        }else{
            smeltSlot.add(new ItemStack(Items.AIR));
        }
        mainproducts.add(smeltSlot);
        //Crushed Ore
        List<ItemStack> crushedSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(TagPrefix.crushed, material)).stream().map(Holder::value).map(item -> new ItemStack(item, 2 * prop.getOreMultiplier())).toList();
        mainproducts.add(crushedSlot);
        //Washed Ore
        List<ItemStack> crushedPurifiedSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(crushedPurified, material)).stream().map(Holder::value).map(ItemStack::new).toList();
        mainproducts.add(crushedPurifiedSlot);
        //TC'ed Ore
        List<ItemStack> crushedRefinedSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(crushedRefined, material)).stream().map(Holder::value).map(ItemStack::new).toList();
        mainproducts.add(crushedRefinedSlot);
        //Impure Dust
        List<ItemStack> dustImpureSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dustImpure, material)).stream().map(Holder::value).map(ItemStack::new).toList();
        mainproducts.add(dustImpureSlot);
        //Pure Dust
        List<ItemStack> dustPureSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dustPure, material)).stream().map(Holder::value).map(ItemStack::new).toList();
        mainproducts.add(dustPureSlot);
        //Dust
        List<ItemStack> dustSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dust, material)).stream().map(Holder::value).map(ItemStack::new).toList();
        mainproducts.add(dustSlot);
        //Gem
        List<ItemStack> gemSlot = new ArrayList<>();
        if(material.hasProperty(PropertyKey.GEM)){
            gemSlot.add(ChemicalHelper.get(gem, material));
        }
        mainproducts.add(gemSlot);
        chanceContent.add(new Content(gemSlot,0.35f,0.05f,null,null));
        List<ItemStack> simpleCrushedSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(crushed, material)).stream().map(Holder::value).map(ItemStack::new).toList();
        mainproducts.add(simpleCrushedSlot);
    }

    private void setupByproducts(List<List<ItemStack>> byproducts, Material material, OreProperty prop, List<Content> chanceContent) {
        Material byproductMaterial1 = GTUtil.selectItemInList(0, material, prop.getOreByProducts(), Material.class);
        Material byproductMaterial2 = GTUtil.selectItemInList(1, material, prop.getOreByProducts(), Material.class);
        Material byproductMaterial3 = GTUtil.selectItemInList(3, material, prop.getOreByProducts(), Material.class);
        List<Material> separatedMaterial = prop.getSeparatedInto();
        //Crushing Ore BP
        ItemStack crushingOreByproductStack = ChemicalHelper.get(gem, byproductMaterial1);
        if (crushingOreByproductStack.isEmpty()) crushingOreByproductStack = ChemicalHelper.get(dust, byproductMaterial1);
        List<ItemStack> maceratorBPSlot1 = Collections.singletonList(crushingOreByproductStack);
        byproducts.add(maceratorBPSlot1);
        chanceContent.add(new Content(crushingOreByproductStack,0.14f,0.07f,null,null));
        //Washing Crushed Ore BP
        List<ItemStack> washerBPSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dustTiny, byproductMaterial1)).stream().map(Holder::value).map(item -> new ItemStack(item,3)).toList();
        byproducts.add(washerBPSlot);
        //Crushing Crushed Ore BP
        List<ItemStack> maceratorBPSlot2 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dust, byproductMaterial1)).stream().map(Holder::value).map(ItemStack::new).toList();;
        byproducts.add(maceratorBPSlot2);
        chanceContent.add(new Content(maceratorBPSlot2,0.14f,0.085f,null,null));
        //Crushing Washed Ore BP
        List<ItemStack> maceratorBPSlot3 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dust, byproductMaterial2)).stream().map(Holder::value).map(ItemStack::new).toList();
        byproducts.add(maceratorBPSlot3);
        chanceContent.add(new Content(maceratorBPSlot3,0.14f,0.085f,null,null));
        //Crushing TC'ed Ore BP
        List<ItemStack> maceratorBPSlot4 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dust, byproductMaterial2)).stream().map(Holder::value).map(ItemStack::new).toList();
        byproducts.add(maceratorBPSlot4);
        chanceContent.add(new Content(maceratorBPSlot4,0.14f,0.085f,null,null));
        //Centrifuging Impure Dust BP
        List<ItemStack> centrifugeBPSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dustTiny, byproductMaterial1)).stream().map(Holder::value).map(ItemStack::new).toList();
        byproducts.add(centrifugeBPSlot);
        //Centrifuging Pure Dust BP
        List<ItemStack> centrifugeBPSlot2 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dustTiny, byproductMaterial2)).stream().map(Holder::value).map(ItemStack::new).toList();
        byproducts.add(centrifugeBPSlot2);
        //TC'ing Crushed/Washed Ore BP
        List<ItemStack> tcBPSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dustTiny, byproductMaterial2)).stream().map(Holder::value).map(item -> new ItemStack(item,3)).toList();
        byproducts.add(tcBPSlot);
        //Bathing Crushed Ore BP
        List<ItemStack> bathBPSlot = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(dust, byproductMaterial3)).stream().map(Holder::value).map(ItemStack::new).toList();
        byproducts.add(bathBPSlot);
        chanceContent.add(new Content(bathBPSlot,0.7f,0.58f,null,null));
        //Separating Pure Dust BP
        ItemStack separatedStack1 = new ItemStack(Items.AIR);
        ItemStack separatedStack2 = new ItemStack(Items.AIR);
        TagPrefix prefix;
        if(prop.getSeparatedInto() != null && !prop.getSeparatedInto().isEmpty()) {
            separatedStack1 = ChemicalHelper.get(dustSmall, separatedMaterial.get(0));
            prefix = (separatedMaterial.get(separatedMaterial.size() - 1).getBlastTemperature() == 0
                    && separatedMaterial.get(separatedMaterial.size() - 1).hasProperty(PropertyKey.INGOT)) ? nugget : dustSmall;
            separatedStack2 = ChemicalHelper.get(prefix, separatedMaterial.get(separatedMaterial.size() - 1), prefix == nugget ? 2 : 1);
        }
        List<ItemStack> sepBPSlot1 = Collections.singletonList(separatedStack1);
        byproducts.add(sepBPSlot1);
        chanceContent.add(new Content(sepBPSlot1,0.4f,0.85f,null,null));
        List<ItemStack> sepBPSlot2 = Collections.singletonList(separatedStack2);
        byproducts.add(sepBPSlot2);
        chanceContent.add(new Content(sepBPSlot2,0.2f,0.6f,null,null));
        //Sifting Washed Ore BP
        if(material.hasProperty(PropertyKey.GEM)){
            List<ItemStack> siftBPSlot1 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(gemExquisite, material)).stream().map(Holder::value).map(ItemStack::new).toList();
            List<ItemStack> siftBPSlot2 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(gemFlawless, material)).stream().map(Holder::value).map(ItemStack::new).toList();
            List<ItemStack> siftBPSlot3 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(gem, material)).stream().map(Holder::value).map(ItemStack::new).toList();
            List<ItemStack> siftBPSlot4 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(gemFlawed, material)).stream().map(Holder::value).map(ItemStack::new).toList();
            List<ItemStack> siftBPSlot5 = BuiltInRegistries.ITEM.getOrCreateTag(ChemicalHelper.getTag(gemChipped, material)).stream().map(Holder::value).map(ItemStack::new).toList();
            byproducts.add(siftBPSlot1);
            chanceContent.add(new Content(siftBPSlot1,0.03f,0.01f,null,null));
            byproducts.add(siftBPSlot2);
            chanceContent.add(new Content(siftBPSlot2,0.1f,0.015f,null,null));
            byproducts.add(siftBPSlot3);
            chanceContent.add(new Content(siftBPSlot3,0.5f,0.075f,null,null));
            byproducts.add(siftBPSlot4);
            chanceContent.add(new Content(siftBPSlot4,0.25f,0.03f,null,null));
            byproducts.add(siftBPSlot5);
            chanceContent.add(new Content(siftBPSlot5,0.35f,0.04f,null,null));
        }
    }
}
