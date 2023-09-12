package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Rundas/Screret
 * @implNote MachineModeFancyConfigurator
 */
public class MachineModeFancyConfigurator implements IFancyConfigurator {
    protected IRecipeLogicMachine machine;

    public MachineModeFancyConfigurator(IRecipeLogicMachine machine) {
        this.machine = machine;
    }

    @Override
    public String getTitle() {
        return "gtceu.gui.machinemode.title";
    }

    @Override
    public IGuiTexture getIcon() {
        return new ResourceTexture("gtceu:textures/item/lv_robot_arm.png");
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(Arrays.asList(machine.getRecipeType()).indexOf(machine.getActiveRecipeType()));
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        machine.setActiveRecipeType(buffer.readVarInt());
    }

    @Override
    public void detectAndSendChange(BiConsumer<Integer, Consumer<FriendlyByteBuf>> sender) {
        sender.accept(0, buf -> buf.writeVarInt(Arrays.asList(machine.getRecipeType()).indexOf(machine.getActiveRecipeType())));
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            machine.setActiveRecipeType(buffer.readVarInt());
        }
    }

    @Override
    public Widget createConfigurator() {
        List<String> recipeTypeNames = Arrays.stream(machine.getRecipeType()).map(rt -> FormattingUtil.toEnglishName(rt.registryName.getPath())).toList();
        return new WidgetGroup(0, 0, 140, 40) {
            @Override
            public void initWidget() {
                super.initWidget();
                setBackground(GuiTextures.BACKGROUND_INVERSE);
                addWidget(new SelectorWidget(0, 0, 140, 20, recipeTypeNames, -1).setOnChanged(
                        rt -> {
                            machine.setActiveRecipeType(recipeTypeNames.indexOf(rt));
                            machine.getRecipeLogic().resetRecipeLogic();
                        }).setSupplier(() -> recipeTypeNames.get(recipeTypeNames.indexOf(FormattingUtil.toEnglishName(machine.getRecipeType()[machine.getActiveRecipeType()].registryName.getPath()))))
                );
            }

            @Override
            public void writeInitialData(FriendlyByteBuf buffer) {
                buffer.writeVarInt(Arrays.asList(machine.getRecipeType()).indexOf(machine.getActiveRecipeType()));
                super.writeInitialData(buffer);
            }

            @Override
            public void readInitialData(FriendlyByteBuf buffer) {
                machine.setActiveRecipeType(buffer.readVarInt());
                super.readInitialData(buffer);
            }

            @Override
            public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
                super.readUpdateInfo(id, buffer);
            }
        };
    }

    @Override
    public List<Component> getTooltips() {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal("Change active Machine Mode"));
        return tooltip;
    }
}
