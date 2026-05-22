package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.TieredItemModule;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.recipe.type.EquipmentFoundryRecipe;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class GTModuleEMIRecipe extends ModularEmiRecipe<WidgetGroup> implements EmiRecipe {

    private static final ItemStack NO_ITEM = Items.BARRIER.getDefaultInstance()
            .setHoverName(Component.translatable("gtceu.equipment_foundry.gui.tier_too_high"));

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            GTCEu.id("equipment_foundry"),
            EmiIngredient.of(Ingredient.of(GTBlocks.EQUIPMENT_FOUNDRY)));

    private final int unique = GTValues.RNG.nextInt();
    private final EquipmentFoundryRecipe recipe;
    private int selectedTier = -1;

    public GTModuleEMIRecipe(EquipmentFoundryRecipe recipe) {
        super(WidgetGroup::new);
        this.recipe = recipe;
        this.widget = this::createUIWidget;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return Stream
                .concat(Arrays.stream(recipe.getEquipment().getItems()),
                        Arrays.stream(recipe.getIngredient().getItems()))
                .filter(this::isModular)
                .map(this::fromStack)
                .toList();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(EmiIngredient.of(Ingredient.of(GTBlocks.EQUIPMENT_FOUNDRY)));
    }

    @Override
    public int getDisplayWidth() {
        return 200;
    }

    @Override
    public int getDisplayHeight() {
        int height = 57;
        height += Minecraft.getInstance().font.wordWrapHeight(recipe.getModules()[0].getInfo(), getDisplayWidth() - 8);
        return height;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);
        widgets.addTexture(EmiTexture.PLUS, 35, 11);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 83, 9);
        ItemStack[] stacks = new ItemStack[] { ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY };
        int[] indexes = new int[] { 0, 0 };
        widgets.addGeneratedSlot(random -> updateStacks(stacks, indexes), unique, 8, 8);
        widgets.addGeneratedSlot(random -> fromStack(stacks[1]), unique, 56, 8);
        widgets.addGeneratedSlot(random -> fromStack(stacks[2]), unique, 115, 8);
    }

    private boolean isModular(ItemStack stack) {
        return stack.getCapability(GTCapability.CAPABILITY_MODULAR_ITEM).isPresent();
    }

    private EmiIngredient fromStack(ItemStack stack) {
        return EmiIngredient.of(Ingredient.of(stack));
    }

    private EmiIngredient updateStacks(ItemStack[] stacks, int[] indexes) {
        if (stacks == null || indexes == null) return null;
        stacks[1] = getModuleItem(indexes, selectedTier);
        stacks[0] = getEquipment(indexes, stacks[1]);
        stacks[2] = getResult(stacks[0], stacks[1]);
        return fromStack(stacks[0]);
    }

    private ItemStack getModuleItem(int[] indexes, int tier) {
        ItemStack[] stacks = recipe.getIngredient().getItems();
        if (tier != -1) {
            stacks = Arrays.stream(stacks)
                    .filter(stack -> GTUtil.getTier(stack.getItem()) == tier)
                    .toArray(ItemStack[]::new);
        }
        return stacks.length == 0 ? NO_ITEM : stacks[indexes[1]++ % stacks.length];
    }

    private ItemStack getEquipment(int[] indexes, ItemStack module) {
        ItemStack[] stacks;
        if (module == NO_ITEM) stacks = Arrays.stream(recipe.getEquipment().getItems())
                .filter(stack -> stack.getCapability(GTCapability.CAPABILITY_MODULAR_ITEM).map(
                        modularItem -> modularItem.attach(recipe.getModule(selectedTier), true) != null).orElse(false))
                .toArray(ItemStack[]::new);
        else stacks = Arrays.stream(recipe.getEquipment().getItems())
                .filter(this::isModular)
                .filter(stack -> recipe.matches(new RecipeWrapper(new CombinedInvWrapper(
                        new CustomItemStackHandler(stack),
                        new CustomItemStackHandler(module))), 0))
                .toArray(ItemStack[]::new);
        return stacks[indexes[0]++ % stacks.length];
    }

    private ItemStack getResult(ItemStack equipment, ItemStack module) {
        ItemStack copy = equipment.copy();
        if (module == NO_ITEM) {
            IModularItem modularItem = GTCapabilityHelper.getModularItem(copy);
            if (modularItem != null) modularItem.attach(recipe.getModule(selectedTier), false);
            return copy;
        }
        RecipeWrapper wrapper = new RecipeWrapper(new CombinedInvWrapper(
                new CustomItemStackHandler(copy),
                new CustomItemStackHandler(module)));
        return recipe.assemble(wrapper, 0);
    }

    public WidgetGroup createUIWidget() {
        Font font = Minecraft.getInstance().font;
        WidgetGroup widgets = new WidgetGroup(0, 0, 200, 200);
        int y = 30;
        List<LabelWidget> desc = new ArrayList<>();
        if (recipe.getModules()[0] instanceof TieredItemModule) {
            TieredItemModule[] tieredModules = Arrays.stream(recipe.getModules())
                    .map(module -> (TieredItemModule) module).toArray(TieredItemModule[]::new);
            int minTier = tieredModules[0].getTier();
            int maxTier = tieredModules[tieredModules.length - 1].getTier();
            for (int i = minTier; i < maxTier; i++) {
                if (getModuleItem(new int[] { 0, 0 }, i) == NO_ITEM) minTier++;
                else break;
            }
            for (int i = maxTier; i > minTier; i--) {
                if (getModuleItem(new int[] { 0, 0 }, i) == NO_ITEM) maxTier--;
                else break;
            }
            if (minTier != maxTier) {
                widgets.addWidget(new LabelWidget(2, y, Component.translatable(
                        "gtceu.equipment_foundry.gui.supports_tiers",
                        GTValues.VNF[minTier],
                        GTValues.VNF[maxTier])));
                y += font.lineHeight;
            }
            selectedTier = tieredModules[0].getTier();
            LabelWidget tierLabel = new LabelWidget(2, y, Component.translatable(
                    "gtceu.equipment_foundry.gui.tier",
                    GTValues.VNF[selectedTier]));
            widgets.addWidget(tierLabel);
            int finalY = y;
            int finalMinTier = minTier;
            int finalMaxTier = maxTier;
            ButtonWidget button = new ButtonWidget(
                    2 + font.width(Component.translatable("gtceu.equipment_foundry.gui.tier", "")),
                    tierLabel.getPositionY(),
                    font.width(GTValues.VNF[GTValues.MAX]), tierLabel.getSizeHeight(),
                    click -> {
                        if (click.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) selectedTier++;
                        if (click.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) selectedTier--;
                        selectedTier = Mth.clamp(selectedTier, finalMinTier, finalMaxTier);
                        tierLabel.setComponent(Component.translatable("gtceu.equipment_foundry.gui.tier",
                                GTValues.VNF[selectedTier]));
                        if (!desc.isEmpty()) {
                            desc.forEach(widget -> widget.setComponent(Component.literal("")));
                            int i = 0;
                            for (FormattedCharSequence line : font
                                    .split(tieredModules[selectedTier - finalMinTier].getInfo(), 196)) {
                                Component component = GTStringUtils.toComponent(line);
                                if (desc.size() <= i) {
                                    desc.add(new LabelWidget(2, finalY + (i + 2) * font.lineHeight, component));
                                    widgets.addWidget(desc.get(i));
                                }
                                desc.get(i).setComponent(component);
                                i++;
                            }
                        }
                    });
            button.setHoverTooltips(Component.translatable("gtceu.equipment_foundry.gui.tooltip.tier_switch"));
            widgets.addWidget(button);
            y += 2 * font.lineHeight;
        }
        for (FormattedCharSequence line : font.split(recipe.getModules()[0].getInfo(), 196)) {
            desc.add(new LabelWidget(2, y, GTStringUtils.toComponent(line)));
            widgets.addWidget(desc.get(desc.size() - 1));
            y += font.lineHeight;
        }
        return widgets;
    }

    public static void addRecipes(EmiRegistry registry) {
        registry.getRecipeManager()
                .getAllRecipesFor(GTRecipeTypes.EQUIPMENT_FOUNDRY_RECIPES.get())
                .stream()
                .map(GTModuleEMIRecipe::new)
                .forEach(registry::addRecipe);
    }
}
