package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.OreProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.PhantomSlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TankWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.FluidStorage;
import com.lowdragmc.lowdraglib.utils.CycleItemStackHandler;
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
        OreProperty prop = material.getProperty(PropertyKey.ORE);
        //Items
        addItemSlots(mainproducts, byproducts, material, prop);
        //Machines
        addMachineSlots(machines);
        //GUI
        setupGui(mainproducts, byproducts, machines, material, prop);
    }

    private void setupGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines, Material material, OreProperty prop) {
        setupBaseGui(mainproducts, byproducts, machines);
        if(!material.hasProperty(PropertyKey.BLAST)){
            setupSmeltGui(mainproducts, machines);
        }
        if(prop.getWashedIn().getLeft() != null){
            setupChemGui(mainproducts,byproducts,machines,prop);
        }
        if(prop.getSeparatedInto() != null && !prop.getSeparatedInto().isEmpty()) {
            setupSepGui(mainproducts, byproducts,machines);
        }
        if(material.hasProperty(PropertyKey.GEM)){
            setupSiftGui(mainproducts, byproducts,machines);
        }
    }

    //Base

    private void setupBaseGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_BASE));
        setupBaseGuiItems(mainproducts, byproducts);
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

    private void setupBaseGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts) {
        //Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),0,3,3));
        //Crushing Ore
        PhantomSlotWidget crushedSlot = new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),2,3,47);
        crushedSlot.setOnAddedTooltips((w, tooltips) -> tooltips.add(Component.translatable("gtceu.gui.content.chance_1", String.format("%.1f", 69f) + "%")));
        addWidget(crushedSlot);
        //Crushing Ore BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),0,3,65));
        //Washing Crushed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),3,64,25));
        //Washing Crushed Ore BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),1,82,25));
        //Crushing Crushed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),5,23,92));
        //Crushing Crushed Ore BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),0,23,110));
        //Centrifuging Impure Dust
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,51,101));
        //Centrifuging Impure Dust BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),5,51,119));
        //Crushing Washed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),6,137,47));
        //Crushing Washed Ore BP
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),3,155,47));
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
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),4,70,119));
        //Simple Washing Crushed Ore
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),2,3,105));
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

    private void setupChemGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines, OreProperty prop) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_CHEM));
        setupChemGuiItems(mainproducts,byproducts);
        setupChemGuiMachines(machines,prop);
    }

    private void setupChemGuiMachines(List<List<ItemStack>> machines, OreProperty prop) {
        Pair<Material,Integer> reagent = prop.getWashedIn();
        PhantomSlotWidget chembathSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),3,25,48);
        chembathSlot.setBackgroundTexture(null);
        addWidget(chembathSlot);
        TankWidget washingReagentSlot = new TankWidget(new FluidStorage(reagent.getLeft().getFluid(reagent.getRight())),42,48,false,false);
        addWidget(washingReagentSlot);
    }

    private void setupChemGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts) {
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),3,64,48));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),8,82,48));
    }

    //Sep

    private void setupSepGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_SEP));
        setupSepGuiItems(mainproducts,byproducts);
        setupSepGuiMachines(machines);
    }

    private void setupSepGuiMachines(List<List<ItemStack>> machines) {
        PhantomSlotWidget separatorSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),7,155,69);
        separatorSlot.setBackgroundTexture(null);
        addWidget(separatorSlot);
    }

    private void setupSepGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts) {
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),7,155,92));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),9,155,110));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),10,155,128));
    }

    //Sift

    private void setupSiftGui(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, List<List<ItemStack>> machines) {
        addWidget(new ImageWidget(0,0,186,174,OREBY_SIFT));
        setupSiftGuiItems(mainproducts,byproducts);
        setupSiftGuiMachines(machines);
    }

    private void setupSiftGuiMachines(List<List<ItemStack>> machines) {
        PhantomSlotWidget sifterSlot = new PhantomSlotWidget(new CycleItemStackHandler(machines),6,101,24);
        sifterSlot.setBackgroundTexture(null);
        addWidget(sifterSlot);
    }

    private void setupSiftGuiItems(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts) {
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(mainproducts),6,119,21));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),11,119,3));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),12,137,3));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),13,155,3));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),14,137,21));
        addWidget(new PhantomSlotWidget(new CycleItemStackHandler(byproducts),15,155,21));
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

    private void addItemSlots(List<List<ItemStack>> mainproducts, List<List<ItemStack>> byproducts, Material material, OreProperty prop) {
        setupMainProducts(mainproducts, material, prop);
        setupByproducts(byproducts, material, prop);
    }

    private void setupMainProducts(List<List<ItemStack>> mainproducts, Material material, OreProperty prop) {
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
        List<ItemStack> crushedSlot = Collections.singletonList(ChemicalHelper.get(crushed, material, 2 * prop.getOreMultiplier()));
        mainproducts.add(crushedSlot);
        //Washed Ore
        List<ItemStack> crushedPurifiedSlot = Collections.singletonList(ChemicalHelper.get(crushedPurified, material));
        mainproducts.add(crushedPurifiedSlot);
        //TC'ed Ore
        List<ItemStack> crushedRefinedSlot = Collections.singletonList(ChemicalHelper.get(crushedRefined, material));
        mainproducts.add(crushedRefinedSlot);
        //Impure Dust
        List<ItemStack> dustImpureSlot = Collections.singletonList(ChemicalHelper.get(dustImpure, material));
        mainproducts.add(dustImpureSlot);
        //Pure Dust
        List<ItemStack> dustPureSlot = Collections.singletonList(ChemicalHelper.get(dustPure, material));
        mainproducts.add(dustPureSlot);
        //Dust
        List<ItemStack> dustSlot = Collections.singletonList(ChemicalHelper.get(dust, material));
        mainproducts.add(dustSlot);
        //Gem
        if(material.hasProperty(PropertyKey.GEM)){
            List<ItemStack> gemSlot = Collections.singletonList(ChemicalHelper.get(gem, material));
            mainproducts.add(gemSlot);
        }
    }

    private void setupByproducts(List<List<ItemStack>> byproducts, Material material, OreProperty prop) {
        Material byproductMaterial1 = GTUtil.selectItemInList(0, material, prop.getOreByProducts(), Material.class);
        Material byproductMaterial2 = GTUtil.selectItemInList(1, material, prop.getOreByProducts(), Material.class);
        Material byproductMaterial3 = GTUtil.selectItemInList(3, material, prop.getOreByProducts(), Material.class);
        List<Material> separatedMaterial = prop.getSeparatedInto();
        //Crushing Ore BP
        ItemStack crushingOreByproductStack = ChemicalHelper.get(gem, byproductMaterial1);
        if (crushingOreByproductStack.isEmpty()) crushingOreByproductStack = ChemicalHelper.get(dust, byproductMaterial1);
        List<ItemStack> maceratorBPSlot1 = Collections.singletonList(crushingOreByproductStack);
        byproducts.add(maceratorBPSlot1);
        //Washing Crushed Ore BP
        ItemStack washingByproductStack = ChemicalHelper.get(dustTiny, byproductMaterial1, 3);
        List<ItemStack> washerBPSlot = Collections.singletonList(washingByproductStack);
        byproducts.add(washerBPSlot);
        //Crushing Crushed Ore BP
        ItemStack crushingCrushedByproductStack = ChemicalHelper.get(dust, byproductMaterial1);
        List<ItemStack> maceratorBPSlot2 = Collections.singletonList(crushingCrushedByproductStack);
        byproducts.add(maceratorBPSlot2);
        //Crushing Washed Ore BP
        ItemStack crushingWashededByproductStack = ChemicalHelper.get(dust, byproductMaterial2);
        List<ItemStack> maceratorBPSlot3 = Collections.singletonList(crushingWashededByproductStack);
        byproducts.add(maceratorBPSlot3);
        //Crushing TC'ed Ore BP
        ItemStack crushingTCedByproductStack = ChemicalHelper.get(dust, byproductMaterial2);
        List<ItemStack> maceratorBPSlot4 = Collections.singletonList(crushingTCedByproductStack);
        byproducts.add(maceratorBPSlot4);
        //Centrifuging Impure Dust BP
        ItemStack centrifugingImpureDustByproductStack = ChemicalHelper.get(dustTiny, byproductMaterial1);
        List<ItemStack> centrifugeBPSlot = Collections.singletonList(centrifugingImpureDustByproductStack);
        byproducts.add(centrifugeBPSlot);
        //Centrifuging Pure Dust BP
        ItemStack centrifugingPureDustByproductStack = ChemicalHelper.get(dustTiny, byproductMaterial2);
        List<ItemStack> centrifugeBPSlot2 = Collections.singletonList(centrifugingPureDustByproductStack);
        byproducts.add(centrifugeBPSlot2);
        //TC'ing Crushed/Washed Ore BP
        ItemStack tcCrushedWashedByproductStack = ChemicalHelper.get(dustTiny, byproductMaterial2, 3);
        List<ItemStack> tcBPSlot = Collections.singletonList(tcCrushedWashedByproductStack);
        byproducts.add(tcBPSlot);
        //Bathing Crushed Ore BP
        ItemStack bathingCrushedByproductStack = ChemicalHelper.get(dust, byproductMaterial3);
        List<ItemStack> bathBPSlot = Collections.singletonList(bathingCrushedByproductStack);
        byproducts.add(bathBPSlot);
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
        List<ItemStack> sepBPSlot2 = Collections.singletonList(separatedStack2);
        byproducts.add(sepBPSlot2);
        //Sifting Washed Ore BP
        ItemStack exquisiteStack = new ItemStack(Items.AIR);
        ItemStack flawlessStack = new ItemStack(Items.AIR);
        ItemStack gemStack =  new ItemStack(Items.AIR);
        ItemStack flawedStack = new ItemStack(Items.AIR);
        ItemStack chippedStack = new ItemStack(Items.AIR);
        if(material.hasProperty(PropertyKey.GEM)){
            exquisiteStack = ChemicalHelper.get(gemExquisite, material);
            flawlessStack = ChemicalHelper.get(gemFlawless, material);
            gemStack = ChemicalHelper.get(gem, material);
            flawedStack = ChemicalHelper.get(gemFlawed, material);
            chippedStack = ChemicalHelper.get(gemChipped, material);
        }
        List<ItemStack> siftBPSlot1 = Collections.singletonList(exquisiteStack);
        List<ItemStack> siftBPSlot2 = Collections.singletonList(flawlessStack);
        List<ItemStack> siftBPSlot3 = Collections.singletonList(gemStack);
        List<ItemStack> siftBPSlot4 = Collections.singletonList(flawedStack);
        List<ItemStack> siftBPSlot5 = Collections.singletonList(chippedStack);
        byproducts.add(siftBPSlot1);
        byproducts.add(siftBPSlot2);
        byproducts.add(siftBPSlot3);
        byproducts.add(siftBPSlot4);
        byproducts.add(siftBPSlot5);
    }
}
