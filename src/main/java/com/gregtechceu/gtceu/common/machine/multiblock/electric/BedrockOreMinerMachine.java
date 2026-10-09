package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.Icon;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.trait.BedrockOreMinerLogic;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BedrockOreMinerMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    @Getter
    private final int tier;

    public BedrockOreMinerMachine(BlockEntityCreationInfo info, int tier) {
        super(info, new BedrockOreMinerLogic());
        this.tier = tier;
    }

    @Override
    public BedrockOreMinerLogic getRecipeLogic() {
        return (BedrockOreMinerLogic) super.getRecipeLogic();
    }

    public int getEnergyTier() {
        var energyContainer = this.getCapabilitiesFlat(IO.IN, EURecipeCapability.CAP);
        var energyCont = new EnergyContainerList(energyContainer.stream().filter(IEnergyContainer.class::isInstance)
                .map(IEnergyContainer.class::cast).toList());
        return Math.min(this.tier + 1, Math.max(this.tier, GTUtil.getFloorTierByVoltage(energyCont.getInputVoltage())));
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();

        GenericListSyncHandler<Component> veinMaterialList = new GenericListSyncHandler.Builder<Component>()
                .adapter(ByteBufAdapters.COMPONENT)
                .getter(() -> {
                    var materials = getRecipeLogic().getVeinMaterials();
                    if (materials == null) return List.of();
                    return materials.stream().map(m -> (Component)m.material().getLocalizedName().withStyle(ChatFormatting.GREEN)).toList();
                })
                .build();


        DynamicLinkedSyncHandler<GenericListSyncHandler<Component>> materialListWidgetHandler = new DynamicLinkedSyncHandler<>(veinMaterialList)
                .widgetProvider((psm, list) -> {
                  var listWidget = new ListWidget<>()
                          .widthRel(1)
                          .childSeparator(Icon.EMPTY_2PX);
                  var values = list.getValue();
                  if (values.isEmpty()) {
                      listWidget.child(Text.lang("gtceu.multiblock.ore_rig.drilled_ore_entry", Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                              .withStyle(ChatFormatting.RED)).asWidget());
                  }
                  for (var value: values) {
                      listWidget.child(Text.lang("gtceu.multiblock.ore_rig.drilled_ore_entry", value).asWidget());
                  }
                  return listWidget;
                });

        IntSyncValue oreAmount = new IntSyncValue(() -> getRecipeLogic().getOreToProduce());

        syncManager.syncValue("veinMaterials", veinMaterialList);
        syncManager.syncValue("oreAmount", oreAmount);

        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addEnergyTierLine(this, syncManager));

        widgets.add(Text.dynamic(() -> Component.translatable("gtceu.multiblock.ore_rig.ore_amount",
                        Component.literal(FormattingUtil.formatNumbers(
                        getRecipeLogic().getOreToProduce() * 20L / BedrockOreMinerLogic.MAX_PROGRESS) + "/s").withStyle(ChatFormatting.BLUE)))
                .asWidget()
        );

        widgets.add(Text.lang("gtceu.multiblock.ore_rig.drilled_ores_list").asWidget());

        widgets.add(new DynamicWidget<>().syncHandler(materialListWidgetHandler));

        return widgets;
    }

    public static int getDepletionChance(int tier) {
        if (tier == GTValues.MV)
            return 1;
        if (tier == GTValues.HV)
            return 2;
        if (tier == GTValues.EV)
            return 8;
        return 1;
    }

    public static int getRigMultiplier(int tier) {
        if (tier == GTValues.MV)
            return 1;
        if (tier == GTValues.HV)
            return 4;
        if (tier == GTValues.EV)
            return 16;
        return 1;
    }

    public static Block getCasingState(int tier) {
        if (tier == GTValues.MV)
            return GTBlocks.CASING_STEEL_SOLID.get();
        if (tier == GTValues.HV)
            return GTBlocks.CASING_TITANIUM_STABLE.get();
        if (tier == GTValues.EV)
            return GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
        return GTBlocks.CASING_STEEL_SOLID.get();
    }

    public static Material getFrameMaterial(int tier) {
        return switch (tier) {
            case GTValues.MV -> GTMaterials.Steel;
            case GTValues.HV -> GTMaterials.Titanium;
            case GTValues.EV -> GTMaterials.TungstenSteel;
            default -> GTMaterials.Steel;
        };
    }

    public static ResourceLocation getBaseTexture(int tier) {
        if (tier == GTValues.MV)
            return GTCEu.id("block/casings/solid/machine_casing_solid_steel");
        if (tier == GTValues.HV)
            return GTCEu.id("block/casings/solid/machine_casing_stable_titanium");
        if (tier == GTValues.EV)
            return GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel");
        return GTCEu.id("block/casings/solid/machine_casing_solid_steel");
    }
}
