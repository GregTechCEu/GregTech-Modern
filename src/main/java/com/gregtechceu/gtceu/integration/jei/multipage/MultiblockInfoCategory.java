package com.gregtechceu.gtceu.integration.jei.multipage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.ISlottedRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MultiblockInfoCategory extends ModularUIRecipeCategory<MultiblockInfoWrapper> {

    public final static RecipeType<MultiblockInfoWrapper> RECIPE_TYPE = new RecipeType<>(GTCEu.id("multiblock_info"),
            MultiblockInfoWrapper.class);
    private final IDrawable background;
    private final IDrawable icon;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.background = guiHelper.createBlankDrawable(160, 160);
        this.icon = helpers.getGuiHelper().createDrawableItemStack(GTMultiMachines.ELECTRIC_BLAST_FURNACE.asStack());
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoWrapper::new)
                .toList());
    }

    @Override
    public void createRecipeExtras(@NotNull IRecipeExtrasBuilder builder, @NotNull MultiblockInfoWrapper recipe,
                                   @NotNull IFocusGroup focuses) {
        super.createRecipeExtras(builder, recipe, focuses);
        List<IRecipeSlotDrawable> slots = new ArrayList<>(builder.getRecipeSlots().getSlots());
        class ProxyRecipeWidget implements ISlottedRecipeWidget {

            private final ScreenPosition position = new ScreenPosition(0, 0);

            @Override
            public Optional<RecipeSlotUnderMouse> getSlotUnderMouse(double mouseX, double mouseY) {
                var panel = recipe.getWidget();
                var pos = panel.getSelfPosition();
                var size = panel.getSize();
                boolean inParent = Widget.isMouseOver(pos.x, pos.y, size.width, size.height, mouseX, mouseY);
                if (!inParent) return Optional.empty();
                List<Widget> widgets = recipe.modularUI.getFlatWidgetCollection();
                return slots.stream()
                        .filter(slot -> {
                            Optional<String> slotName = slot.getSlotName();
                            if (slotName.isEmpty()) return false;
                            String name = slotName.get();
                            int index = Integer.parseInt(name.substring(5));
                            Widget widget = widgets.get(index);
                            slot.setPosition(widget.getPositionX(), widget.getPositionY());
                            return slot.isMouseOver(mouseX, mouseY);
                        })
                        .findFirst()
                        .map(slot -> new RecipeSlotUnderMouse(slot, 0, 0));
            }

            @Override
            public ScreenPosition getPosition() {
                return position;
            }
        }

        builder.addSlottedWidget(new ProxyRecipeWidget(), slots);
    }

    @Override
    public @Nullable ResourceLocation getRegistryName(@NotNull MultiblockInfoWrapper recipe) {
        return recipe.definition.getId();
    }

    @Override
    @NotNull
    public RecipeType<MultiblockInfoWrapper> getRecipeType() {
        return RECIPE_TYPE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.jei.multiblock_info");
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }
}
