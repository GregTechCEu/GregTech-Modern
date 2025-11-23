package com.gregtechceu.gtceu.api.machine.feature.multiblock;

public interface IDistinctPart extends IMultiPart {

    boolean isDistinct();

    void setDistinct(boolean isDistinct);

    /*
     * @Override
     * default void attachConfigurators(ConfiguratorPanel configuratorPanel) {
     * superAttachConfigurators(configuratorPanel);
     * configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
     * GuiTextures.BUTTON_DISTINCT_BUSES.getSubTexture(0, 0.5, 1, 0.5),
     * GuiTextures.BUTTON_DISTINCT_BUSES.getSubTexture(0, 0, 1, 0.5),
     * this::isDistinct, (clickData, pressed) -> setDistinct(pressed))
     * .setTooltipsSupplier(pressed -> List.of(
     * Component.translatable("gtceu.multiblock.universal.distinct")
     * .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))
     * .append(Component.translatable(pressed ? "gtceu.multiblock.universal.distinct.yes" :
     * "gtceu.multiblock.universal.distinct.no")))));
     * }
     * 
     * default void superAttachConfigurators(ConfiguratorPanel configuratorPanel) {
     * IMultiPart.super.attachConfigurators(configuratorPanel);
     * }
     */
}
