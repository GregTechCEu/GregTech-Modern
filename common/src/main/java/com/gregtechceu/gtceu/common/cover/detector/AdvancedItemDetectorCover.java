package com.gregtechceu.gtceu.common.cover.detector;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.RedstoneUtil;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextBoxWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AdvancedItemDetectorCover extends ItemDetectorCover implements IUICover {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AdvancedItemDetectorCover.class, DetectorCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
    

    private static final int DEFAULT_MIN = 64;
    private static final int DEFAULT_MAX = 512;

    @Persisted @Getter
    private int minValue, maxValue;

    @Persisted @DescSynced @Getter
    protected final FilterHandler<ItemStack, ItemFilter> filterHandler;

    public AdvancedItemDetectorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

        this.minValue = DEFAULT_MIN;
        this.maxValue = DEFAULT_MAX;

        filterHandler = FilterHandlers.item(this);
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    @Override
    protected void update() {
        if (this.coverHolder.getOffsetTimer() % 20 != 0)
            return;

        ItemFilter filter = filterHandler.getFilter();
        IItemTransfer itemTransfer = getItemTransfer();
        if (itemTransfer == null)
            return;

        int storedItems = 0;

        for (int i = 0; i < itemTransfer.getSlots(); i++) {
            if (filter.test(itemTransfer.getStackInSlot(i)))
                storedItems += itemTransfer.getStackInSlot(i).getCount();
        }

        setRedstoneSignalOutput(RedstoneUtil.computeRedstoneBetweenValues(storedItems, maxValue, minValue, isInverted()));
    }

    public void setMinValue(int minValue) {
        this.minValue = Mth.clamp(minValue, 0, maxValue - 1);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = Math.max(maxValue, 0);
    }


    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 176, 170);
        group.addWidget(new LabelWidget(10, 5, "cover.advanced_item_detector.label"));

        group.addWidget(new TextBoxWidget(10, 55, 65,
                List.of(LocalizationUtils.format("cover.advanced_item_detector.min"))));

        group.addWidget(new TextBoxWidget(10, 80, 65,
                List.of(LocalizationUtils.format("cover.advanced_item_detector.max"))));

        group.addWidget(new IntInputWidget(80, 50, 176 - 80 - 10, 20, this::getMinValue, this::setMinValue));
        group.addWidget(new IntInputWidget(80, 75, 176 - 80 - 10, 20, this::getMaxValue, this::setMaxValue));


        // Invert Redstone Output Toggle:
        group.addWidget(new ToggleButtonWidget(
                9, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted
        ) {
            @Override
            public void updateScreen() {
                super.updateScreen();
                setHoverTooltips(List.copyOf(LangHandler.getMultiLang(
                        "cover.advanced_item_detector.invert." + (isPressed ? "enabled" : "disabled")
                )));
            }
        });


        // Item Filter UI:
        group.addWidget(filterHandler.createFilterSlotUI(148, 100));
        group.addWidget(filterHandler.createFilterConfigUI(10, 100, 156, 60));

        return group;
    }
}
