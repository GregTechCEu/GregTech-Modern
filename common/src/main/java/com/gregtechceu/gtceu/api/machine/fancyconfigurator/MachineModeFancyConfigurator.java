package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Rundas
 * @implNote MachineModeFancyConfigurator
 */
public class MachineModeFancyConfigurator implements IFancyConfigurator {
    protected WorkableTieredMachine machine;
    protected GTRecipeType[] recipeTypes;
    protected GTRecipeType activeRecipeType;

    public MachineModeFancyConfigurator(WorkableTieredMachine machine) {
        this.machine = machine;
    }

    @Override
    public String getTitle() {
        return "gtceu.gui.machinemode.title";
    }

    @Override
    public IGuiTexture getIcon() {
        return GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5);
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        this.activeRecipeType = machine.getActiveRecipeType();
        buffer.writeVarInt(Arrays.asList(this.recipeTypes).indexOf(this.activeRecipeType));
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        this.activeRecipeType = this.recipeTypes[buffer.readVarInt()];
    }

    @Override
    public void detectAndSendChange(BiConsumer<Integer, Consumer<FriendlyByteBuf>> sender) {
        var newActiveRecipeType = machine.getActiveRecipeType();
        if (newActiveRecipeType != activeRecipeType) {
            this.activeRecipeType = newActiveRecipeType;
            sender.accept(0, buf -> buf.writeVarInt(Arrays.asList(this.recipeTypes).indexOf(this.activeRecipeType)));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            this.activeRecipeType = this.recipeTypes[buffer.readVarInt()];
        }
    }

    @Override
    public Widget createConfigurator() {
        final List<String> recipeTypeNames = new ArrayList<>();
        for(GTRecipeType type : this.recipeTypes){
            recipeTypeNames.add(type.toString());
        }
        return new WidgetGroup(0, 0, 120, 40) {
            final Map<Integer, WidgetGroup> lightGroups = new HashMap<>();
            @Override
            public void initWidget() {
                super.initWidget();
                setBackground(GuiTextures.BACKGROUND_INVERSE);
                addWidget(new ImageWidget(20, 5, 120 - 5 - 10 - 5 - 20, 20, () -> new GuiTextureGroup(GuiTextures.DISPLAY_FRAME, new TextTexture(activeRecipeType.toString()))));
                addWidget(new SelectorWidget(20, 15, 120, 20, recipeTypeNames, -1).setOnChanged(
                        rt -> activeRecipeType = recipeTypes[recipeTypeNames.indexOf(rt)]
                ));
            }

            @Override
            public void writeInitialData(FriendlyByteBuf buffer) {
                buffer.writeVarInt(Arrays.asList(machine.getRecipeType()).indexOf(machine.getActiveRecipeType()));
                super.writeInitialData(buffer);
            }

            @Override
            public void readInitialData(FriendlyByteBuf buffer) {
                activeRecipeType = recipeTypes[buffer.readVarInt()];
                super.readInitialData(buffer);
            }

            @Override
            public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
                if (id == 0) {
                    int min = buffer.readVarInt();
                    int max = buffer.readVarInt();
                } else {
                    super.readUpdateInfo(id, buffer);
                }
            }
        };
    }

    @Override
    public List<Component> getTooltips() {
        List<Component> types = new ArrayList<>();
        for (GTRecipeType type : recipeTypes){
            types.add(Component.literal(type.toString()));
        }
        return types;
    }
}
