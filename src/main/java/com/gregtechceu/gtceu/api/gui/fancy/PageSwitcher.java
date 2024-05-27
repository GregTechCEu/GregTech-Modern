package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PageSwitcher implements IFancyUIProvider {

    private final Consumer<IFancyUIProvider> onPageSwitched;

    private List<IFancyUIProvider> pages = List.of();
    private IFancyUIProvider currentPage = null;

    public PageSwitcher(Consumer<IFancyUIProvider> onPageSwitched) {
        this.onPageSwitched = onPageSwitched;
    }

    public void setPageList(List<IFancyUIProvider> allPages, IFancyUIProvider currentPage) {
        this.pages = allPages;
        this.currentPage = currentPage;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var container = new WidgetGroup(0, 0, 176, 166);

        var scrollableGroup = new DraggableScrollableWidgetGroup(10, 10, 156, 146);
        scrollableGroup.setYScrollBarWidth(8);
        scrollableGroup.setYBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL, GuiTextures.BUTTON);
        container.addWidget(scrollableGroup);

        var groupedPages = pages.stream().collect(Collectors.groupingBy(
                page -> Objects.requireNonNullElse(page.getPageGroupingData(), new PageGroupingData(null, -1))));

        final MutableInt currentY = new MutableInt(0);
        groupedPages.keySet().stream()
                .sorted(Comparator.comparingInt(PageGroupingData::groupPositionWeight))
                .forEachOrdered(group -> {
                    if (group.groupKey() != null) {
                        scrollableGroup.addWidget(
                                new LabelWidget(0, currentY.getAndAdd(12), group.groupKey()).setDropShadow(false));
                    }

                    final var currentPage = new MutableInt(0);
                    currentY.subtract(30); // To account for adding it back on the first page inside this group

                    groupedPages.get(group).forEach(page -> {
                        var index = currentPage.getAndIncrement();
                        var y = currentY.addAndGet(index % 5 == 0 ? 30 : 0); // Jump to the next row every 5 parts

                        var pageWidget = new WidgetGroup((index % 5) * 30, y, 25, 25);
                        pageWidget.addWidget(new ButtonWidget(0, 0, 25, 25, GuiTextures.BACKGROUND,
                                clickData -> onPageSwitched.accept(page)));
                        pageWidget.addWidget(new ImageWidget(4, 4, 17, 17, page.getTabIcon()));
                        // For some reason, this doesn't work in any other way:
                        pageWidget.widgets.get(0).setHoverTooltips(page.getTitle().getString());
                        scrollableGroup.addWidget(pageWidget);
                    });

                    if (!groupedPages.get(group).isEmpty()) {
                        currentY.add(30);
                    }
                });

        return container;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new TextTexture("+").setDropShadow(false).setColor(ChatFormatting.BLACK.getColor());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.title_bar.page_switcher");
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }
}
