package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.steam.SteamEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.data.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTGuis;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamParallelMultiblockMachine extends WorkableMultiblockMachine implements IMuiMachine {

    @Getter
    @Setter
    private int maxParallels;

    @Nullable
    @SyncToClient
    private SteamEnergyRecipeHandler steamEnergy = null;

    // if in millibuckets, this is 2.0, Meaning 2mb of steam -> 1 EU
    public static final double CONVERSION_RATE = 2.0;

    public SteamParallelMultiblockMachine(BlockEntityCreationInfo info, int maxParallels) {
        super(info);
        this.maxParallels = maxParallels;
    }

    public SteamParallelMultiblockMachine(BlockEntityCreationInfo info) {
        this(info, ConfigHolder.INSTANCE.machines.steamMultiParallelAmount);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (var part : getParts()) {
            if (!PartAbility.STEAM.isApplicable(part.self().getDefinition().getBlock())) continue;
            var handlers = part.getRecipeHandlers();
            for (var hl : handlers) {
                if (!hl.isValid(IO.IN)) continue;
                for (var fluidHandler : hl.getCapability(FluidRecipeCapability.CAP)) {
                    if (!(fluidHandler instanceof NotifiableFluidTank nft)) continue;
                    if (nft.isFluidValid(0, GTMaterials.Steam.getFluid(1))) {
                        steamEnergy = new SteamEnergyRecipeHandler(nft, getConversionRate());
                        syncDataHolder.markClientSyncFieldDirty("steamEnergy");
                        addHandlerList(RecipeHandlerList.of(IO.IN, steamEnergy));
                        return;
                    }
                }
            }
        }
        if (steamEnergy == null) { // No steam hatch found
            onStructureInvalid();
        }
    }

    public double getConversionRate() {
        return CONVERSION_RATE;
    }

    /**
     * Recipe Modifier for <b>Steam Multiblock Machines</b> - can be used as a valid {@link RecipeModifier}
     * <p>
     * Recipe is rejected if tier is greater than LV
     * </p>
     * <p>
     * Recipe is parallelized up to the Multiblock's parallel limit.
     * Then, duration is multiplied by {@code 1.5×} and EUt is multiplied by {@code (8/9) × parallels}, up to a cap of
     * 32 EUt
     * </p>
     *
     * @param machine a {@link SteamParallelMultiblockMachine}
     * @param recipe  recipe
     * @return A {@link ModifierFunction} for the given Steam Multiblock Machine and recipe
     */
    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof SteamParallelMultiblockMachine steamMachine)) {
            return RecipeModifier.nullWrongType(SteamParallelMultiblockMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > GTValues.LV) return ModifierFunction.NULL;

        // Duration = 1.5x base duration
        // EUt (not steam) = (4/3) * (2/3) * parallels * base EUt, up to a max of 32 EUt
        long eut = recipe.getInputEUt().getTotalEU();
        int parallelAmount = ParallelLogic.getParallelAmount(machine, recipe, steamMachine.maxParallels);
        double eutMultiplier = (eut * 0.8888 * parallelAmount <= 32) ? (0.8888 * parallelAmount) : (32.0 / eut);
        return ModifierFunction.builder()
                .inputModifier(ContentModifier.multiplier(parallelAmount))
                .outputModifier(ContentModifier.multiplier(parallelAmount))
                .durationMultiplier(1.5)
                .eutMultiplier(eutMultiplier)
                .parallels(parallelAmount)
                .build();
    }

    /*
     * @Override
     * public void addDisplayText(List<Component> textList) {
     * IDisplayUIMachine.super.addDisplayText(textList);
     * if (isFormed()) {
     * if (steamEnergy != null && steamEnergy.getCapacity() > 0) {
     * long steamStored = steamEnergy.getStored();
     * textList.add(Component.translatable("gtceu.multiblock.steam.steam_stored", steamStored,
     * steamEnergy.getCapacity()));
     * }
     * 
     * if (!isWorkingEnabled()) {
     * textList.add(Component.translatable("gtceu.multiblock.work_paused"));
     * 
     * } else if (isActive()) {
     * textList.add(Component.translatable("gtceu.multiblock.running"));
     * if (maxParallels > 1) textList.add(Component.translatable("gtceu.multiblock.parallel", maxParallels));
     * int currentProgress = (int) (recipeLogic.getProgressPercent() * 100);
     * double maxInSec = (float) recipeLogic.getDuration() / 20.0f;
     * double currentInSec = (float) recipeLogic.getProgress() / 20.0f;
     * textList.add(
     * Component.translatable("gtceu.multiblock.progress", String.format("%.2f", (float) currentInSec),
     * String.format("%.2f", (float) maxInSec), currentProgress));
     * } else {
     * textList.add(Component.translatable("gtceu.multiblock.idling"));
     * }
     * 
     * if (recipeLogic.isWaiting()) {
     * textList.add(Component.translatable("gtceu.multiblock.steam.low_steam")
     * .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
     * }
     * }
     * }
     */

    /*
     * @Override
     * public IGuiTexture getScreenTexture() {
     * return GuiTextures.DISPLAY_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks);
     * }
     */

    /*
     * @Override
     * public ModularUI createUI(Player entityPlayer) {
     * var screen = new DraggableScrollableWidgetGroup(7, 4, 162, 121).setBackground(getScreenTexture());
     * screen.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));
     * screen.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
     * .setMaxWidthLimit(150)
     * .clickHandler(this::handleDisplayClick));
     * return new ModularUI(176, 216, this, entityPlayer)
     * .background(GuiTextures.BACKGROUND_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks))
     * .widget(screen)
     * .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(),
     * GuiTextures.SLOT_STEAM.get(ConfigHolder.INSTANCE.machines.steelSteamMultiblocks), 7, 134,
     * true));
     * }
     */

    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var panel = GTGuis.createPanel(this, 176, 164);

        var parentWidget = new ParentWidget<>();
        var listWidget = new ListWidget<>()
                .width(170 - 6)
                .height(70 - 6)
                .childSeparator(Icon.EMPTY_2PX)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .alignX(Alignment.CenterLeft);
        parentWidget.size(170, 70)
                .background(GTGuiTextures.MUI_DISPLAY);

        listWidget.child(GTMultiblockTextUtil.addSteamUsageLine(this.steamEnergy, syncManager))
                .child(GTMultiblockTextUtil.addProgressLine(this, syncManager))
                .child(GTMultiblockTextUtil.addParallelLine(this, syncManager))
                .child(GTMultiblockTextUtil.addOutputLines(this, syncManager));

        parentWidget.child(listWidget.left(3).top(3));

        var theme = this.getDefinition().getThemeId();
        var backgroundTexture = (UITexture) ThemeAPI.INSTANCE.getTheme(theme).getPanelTheme().getTheme()
                .getBackground();
        if (backgroundTexture == null) {
            backgroundTexture = GTGuiTextures.BACKGROUND;
        }

        panel.child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(0.95f)
                        .heightRel(.45f)
                        .margin(4, 0)
                        .left(3).top(5)
                        .child(parentWidget))
                .child(Flow.col()
                        .coverChildren()
                        .leftRel(1.0f)
                        .reverseLayout(true)
                        .bottom(16)
                        .padding(3, 5, 4, 4)
                        .childPadding(2)
                        .background(backgroundTexture.getSubArea(0.25f, 0f, 1.0f, 1.0f))
                        .child(GTMuiWidgets.createPowerButton(this, syncManager)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));

        return panel;
    }
}
