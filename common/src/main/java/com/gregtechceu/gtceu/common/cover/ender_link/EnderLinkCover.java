package com.gregtechceu.gtceu.common.cover.ender_link;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IEnderLinkCover;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.EnderLinkControllerMachine;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkCardReader;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.stream.Stream;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class EnderLinkCover<T> extends CoverBehavior implements IEnderLinkCover<T>, IUICover, IControllable {
    public final int tier;

    @Nullable
    private EnderLinkControllerMachine controller;

    @Persisted @DescSynced @Getter
    private int channel = 1;

    @Persisted @DescSynced
    private final EnderLinkCardReader cardReader;

    @Persisted @DescSynced @Getter
    protected IO io = IO.OUT;

    @Persisted @Getter @Setter
    protected boolean isWorkingEnabled = true;

    private final ConditionalSubscriptionHandler controllerSearchSubscription;

    public EnderLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide);

        this.tier = tier;
        this.cardReader = new EnderLinkCardReader(this::getGlobalPos, c -> setController(c.orElse(null)));

        this.controllerSearchSubscription = new ConditionalSubscriptionHandler(
                coverHolder, this::searchController, this::isControllerSearchActive
        );
    }

    /////////////////////////////////////
    //***********    API    ***********//
    /////////////////////////////////////

    protected abstract String getUITitle();

    protected abstract Widget createTransferRateUI(int x, int y, int width, int height);

    protected abstract Widget createFilterUI(int x, int y, int width, int height);

    @Override
    public GlobalPos getGlobalPos() {
        return GlobalPos.of(coverHolder.getLevel().dimension(), coverHolder.getPos());
    }

    public void setIo(IO io) {
        if (io == IO.NONE)
            return;

        this.io = io;

        if (controller != null)
            controller.updateCover(this);
    }

    private void setController(@Nullable EnderLinkControllerMachine controller) {
        unlinkFromController();
        this.controller = controller;
        linkToController();

        controllerSearchSubscription.updateSubscription();
    }

    private void searchController() {
        cardReader.searchController();
        controllerSearchSubscription.updateSubscription();
    }

    private boolean isControllerSearchActive() {
        return this.controller == null;
    }

    protected void linkToController() {
        if (this.controller != null)
            this.controller.linkCover(this);
    }

    protected void unlinkFromController() {
        if (this.controller != null)
            this.controller.unlinkCover(this);

        this.controller = null;
    }

    public void setChannel(int channel) {
        int maxChannels = controller != null ? controller.getMaxChannels() : 1;
        this.channel = Mth.clamp(channel, 1, maxChannels);

        if (controller != null) {
            controller.updateCover(this);
        }
    }

    @Override
    public void unlinkController(EnderLinkControllerMachine controller) {
        if (this.controller == controller)
            this.controller = null;
    }


    /////////////////////////////////////
    //*****    COVER OVERRIDES    *****//
    /////////////////////////////////////

    @Override
    public List<ItemStack> getAdditionalDrops() {
        return Stream.of(
                super.getAdditionalDrops(),
                cardReader.getDroppedItems()
        ).flatMap(List::stream).toList();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        controllerSearchSubscription.initialize(coverHolder.getLevel());
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        controllerSearchSubscription.initialize(coverHolder.getLevel());
    }

    @Override
    public void onUnload() {
        super.onUnload();
        unlinkFromController();
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        unlinkFromController();
    }

    /////////////////////////////////////
    //***********    GUI    ***********//
    /////////////////////////////////////


    @Override
    public Widget createUIWidget() {
        final var group = new WidgetGroup(0, 0, 176, 135);
        group.addWidget(new LabelWidget(10, 5, LocalizationUtils.format(getUITitle(), GTValues.VN[tier])));

        group.addWidget(createTransferRateUI(10, 20, 156, 20));

        group.addWidget(new EnumSelectorWidget<>(10, 45, 20, 20, List.of(IO.IN, IO.OUT, IO.BOTH), io, this::setIo));
        group.addWidget(cardReader.createUI(131, 46));

        group.addWidget(createFilterUI(10, 70, 156, 60));

        return group;
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderLinkCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}


