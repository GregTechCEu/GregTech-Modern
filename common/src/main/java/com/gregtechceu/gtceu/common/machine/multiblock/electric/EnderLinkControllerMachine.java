package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.cover.IEnderLinkCover;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkCardWriter;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkControllerRegistry;
import com.gregtechceu.gtceu.common.pipelike.enderlink.EnderLinkNetwork;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.gregtechceu.gtceu.api.GTValues.*;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EnderLinkControllerMachine extends MultiblockControllerMachine implements IFancyUIMachine, IMachineModifyDrops, IControllable {
    @Persisted @Getter
    private boolean workingEnabled;

    @Persisted @DescSynced
    private UUID uuid;

    private final int tier;
    private final Set<IEnderLinkCover<?>> loadedLinkedCovers = new ObjectArraySet<>();

    @Getter
    private final EnderLinkNetwork network;

    @Persisted
    private final EnderLinkCardWriter cardWriter;

    private final ConditionalSubscriptionHandler subscriptionHandler;

    public EnderLinkControllerMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder);

        this.tier = tier;
        this.cardWriter = new EnderLinkCardWriter(this);

        //noinspection DataFlowIssue
        this.network = new EnderLinkNetwork(() -> getLevel().getServer().getTickCount(), getMaxChannels());

        // The machine on the remote side needs to know its UUID as well, so that it can be registered there.
        // This field may not yet be synced to the remote instance in onLoad(), so registration happens on sync instead.
        addSyncUpdateListener("uuid", (changedField, newValue, oldValue) -> {
            EnderLinkControllerRegistry.unregisterController((UUID) oldValue);
            EnderLinkControllerRegistry.registerController(this);
        });

        this.subscriptionHandler = new ConditionalSubscriptionHandler(this, this::update, this::isSubscriptionActive);
    }

    public UUID getUuid() {
        if (uuid == null && !LDLib.isRemote()) {
            this.uuid = UUID.randomUUID();
        }

        return uuid;
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        drops.addAll(cardWriter.getDroppedItems());
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (!LDLib.isRemote())
            EnderLinkControllerRegistry.registerController(this);
    }

    @Override
    public void onUnload() {
        super.onUnload();

        loadedLinkedCovers.forEach(cover -> cover.unlinkController(this));
        EnderLinkControllerRegistry.unregisterController(this);
    }

    //////////////////////////////////////
    //**********   BEHAVIOR   **********//
    //////////////////////////////////////

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;

        subscriptionHandler.updateSubscription();
    }

    public int getMaxChannels() {
        return 8; // TODO adapt to tier, as well as linked controllers
    }

    public int getMaxRange() {
        return 32; // TODO adapt to tier
    }

    public GlobalPos getGlobalPosition() {
        return GlobalPos.of(getLevel().dimension(), this.getPos());
    }

    private void update() {
        if (getOffsetTimer() % 20 == 0) {
            network.transferAll();
            subscriptionHandler.updateSubscription();
        }
    }

    private boolean isSubscriptionActive() {
        return isWorkingEnabled();
    }

    ///////////////////////////////////////
    //******   COVER INTERACTION   ******//
    ///////////////////////////////////////

    public <T> void linkCover(IEnderLinkCover<T> cover) {
        loadedLinkedCovers.add(cover);
        network.registerCover(cover);
    }

    public <T> void unlinkCover(IEnderLinkCover<T> cover) {
        loadedLinkedCovers.remove(cover);
        network.unregisterCover(cover);
    }

    public <T> void updateCover(IEnderLinkCover<T> cover) {
        network.unregisterCover(cover);
        network.registerCover(cover);
    }

    //////////////////////////////////////
    //********     Structure    ********//
    //////////////////////////////////////
    public static Block getCasingState(int tier) {
        return switch (tier) {
            case MV -> GTBlocks.CASING_STEEL_SOLID.get();
            case HV -> GTBlocks.CASING_STAINLESS_CLEAN.get();
            case EV -> GTBlocks.CASING_TITANIUM_STABLE.get();
            case IV -> GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get();
            case LuV -> GTBlocks.CASING_HSSE_STURDY.get();
            default -> throw new IllegalStateException("Unexpected value: " + tier);
        };
    }

    public static ResourceLocation getBaseTexture(int tier) {
        return switch (tier) {
            case MV -> GTCEu.id("block/casings/solid/machine_casing_solid_steel");
            case HV -> GTCEu.id("block/casings/solid/machine_casing_clean_stainless_steel");
            case EV -> GTCEu.id("block/casings/solid/machine_casing_stable_titanium");
            case IV -> GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel");
            case LuV -> GTCEu.id("block/casings/solid/machine_casing_study_hsse");
            default -> throw new NotImplementedException("Not yet implemented"); // TODO
        };
    }

    /////////////////////////////////////
    //************   GUI   ************//
    /////////////////////////////////////

    @Override
    public Widget createUIWidget() {
        WidgetGroup widgetGroup = new WidgetGroup(0, 0, 200, 150);

        widgetGroup.addWidget(cardWriter.createUI(10, 10));

        return widgetGroup;
    }


    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderLinkControllerMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
