package com.gregtechceu.gtceu.api.item.module;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandler;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SliderWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public abstract class ItemModule {

    private static final Map<ResourceLocation, ItemModule> MODULES = new HashMap<>();

    @Getter
    private final ResourceLocation id;

    public static @Nullable ItemModule getModuleById(ResourceLocation id) {
        return MODULES.get(id);
    }

    public ItemModule(ResourceLocation id) {
        this.id = id;
        if (MODULES.containsKey(id)) {
            GTCEu.LOGGER.warn("Attempted to create 2 modules with the same id: {}", id);
        } else MODULES.put(id, this);
    }

    public final CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        return tag;
    }

    public static ItemModule fromNBT(CompoundTag tag) {
        return getModuleById(ResourceLocation.tryParse(tag.getString("id")));
    }

    public abstract Component getInfo();

    public void onAttach(AppliedItemModule module) {}

    public void onRemove(AppliedItemModule module) {}

    public void onEquip(LivingEntity entity, AppliedItemModule module) {}

    public void onArmorTick(LivingEntity entity, AppliedItemModule module) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getModuleItem());
        long energy = energyUsagePerTick(entity, module);
        if (electricItem != null) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    public void onUnequip(LivingEntity entity, AppliedItemModule module) {}

    /**
     * Called each tick this item is in a player's inventory or equipment slots
     */
    public void onInventoryTick(Player player, AppliedItemModule module) {
        if (module.getModuleItem() == null) return;
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(module.getModuleItem());
        long energy = energyUsagePerTick(player, module);
        if (electricItem != null && useEnergyInInventory(player, module)) {
            electricItem.discharge(energy, electricItem.getTier(), true, false, false);
        }
    }

    /**
     * @return name displayed in the modules UI
     */
    public Component getDisplayName(AppliedItemModule module) {
        List<Component> list = new ArrayList<>();
        appendHoverText(null, TooltipFlag.NORMAL, list, module);
        return list.isEmpty() ? Component.empty() : list.get(0);
    }

    public void appendHoverText(Level level, TooltipFlag isAdvanced, List<Component> tooltips,
                                AppliedItemModule module) {}

    public boolean useEnergyInInventory(LivingEntity entity, AppliedItemModule module) {
        return true;
    }

    public long energyUsagePerTick(LivingEntity entity, AppliedItemModule module) {
        return 0;
    }

    public float changeDamage(LivingEntity entity, AppliedItemModule modifier, float damage, DamageSource source) {
        return damage;
    }

    public boolean canRemove(AppliedItemModule module) {
        return true;
    }

    public boolean isPPE(AppliedItemModule module) {
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean canApplyTo(ItemStack stack) {
        IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
        return modularItem != null && modularItem.getModule(this) == null;
    }

    public boolean isEnabled(AppliedItemModule module) {
        if (!module.getTag().contains("enabled")) {
            setEnabled(module, true);
        }
        return module.getTag().getBoolean("enabled");
    }

    public void setEnabled(AppliedItemModule module, boolean enabled) {
        module.getTag().putBoolean("enabled", enabled);
    }

    /**
     * Called when the item this module is attached to is ticked,
     * ignores {@link #isEnabled(AppliedItemModule)}.
     *
     * @param entity the entity in which the item is, {@code null} if the item is not in one
     * @param pos    the position of the block in which the item is, {@code null} if the item is not in one
     */
    public void onTickRaw(AppliedItemModule module, @Nullable Entity entity, @NotNull Level level,
                          @Nullable BlockPos pos) {}

    public InteractionResultHolder<ItemStack> use(AppliedItemModule module, Level level, Player player,
                                                  InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public InteractionResult useOn(AppliedItemModule module, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult onItemUseFirst(AppliedItemModule module, UseOnContext context) {
        return InteractionResult.PASS;
    }

    public InteractionResult interactLivingEntity(AppliedItemModule module, Player player,
                                                  LivingEntity interactionTarget, InteractionHand usedHand) {
        return InteractionResult.PASS;
    }

    public Settings getSettings(AppliedItemModule module, PanelSyncManager psm, int id) {
        psm.syncValue("module_enabled", id,
                SyncHandlers.intNumber(() -> isEnabled(module) ? 0 : 1, x -> setEnabled(module, x == 0)));
        Settings settings = new Settings(psm, id);
        return settings
                .bool(Text.lang("gtceu.module.gui.enabled"), () -> isEnabled(module), b -> setEnabled(module, b));
    }

    public static final class Settings extends ArrayList<IWidget> {

        private final PanelSyncManager psm;
        private final int id;
        private int current = 0;

        private Settings(PanelSyncManager psm, int id) {
            this.psm = psm;
            this.id = id;
        }

        private String registerSyncValue(SyncHandler<?> syncHandler) {
            String key = "module_setting" + current;
            current++;
            psm.syncValue(key, id, syncHandler);
            return key;
        }

        private Settings pop() {
            remove(size() - 1);
            return this;
        }

        private <W extends IWidget> W last() {
            // noinspection unchecked
            return (W) get(size() - 1);
        }

        private static Flow wrap(Text label, IWidget widget) {
            return Flow.row()
                    .coverChildren()
                    .childPadding(5)
                    .child(new TextWidget<>(label.get()))
                    .child(widget);
        }

        public Settings custom(Text label, SyncHandler<?> syncHandler, Widget<?> widget) {
            String key = registerSyncValue(syncHandler);
            add(wrap(label, widget
                    .syncHandler(key, id)));
            return this;
        }

        public Settings bool(Text label, BooleanSupplier getter, Consumer<Boolean> setter) {
            return custom(label,
                    SyncHandlers.intNumber(() -> getter.getAsBoolean() ? 0 : 1, x -> setter.accept(x == 0))
                            .allowC2S(),
                    new ToggleButton()
                            .invertSelected(true)
                            .overlay(false, GuiTextures.CHECKMARK));
        }

        public Settings num(Text label, IntSupplier getter, IntConsumer setter, int min, int max) {
            return num(label, getter, setter, min, max, "%d");
        }

        public Settings num(Text label, IntSupplier getter, IntConsumer setter, int min, int max, String sliderLabel) {
            custom(label,
                    SyncHandlers.intNumber(getter, setter)
                            .allowC2S(),
                    new SliderWidget()
                            .bounds(min, max)
                            .width(50)
                            .stopper(1))
                    .<Flow>last()
                    .child(new TextWidget<>(
                            Text.dynamic(() -> Component.literal(sliderLabel.formatted(getter.getAsInt())))));
            return this;
        }

        public Settings num(Text label, DoubleSupplier getter, DoubleConsumer setter, double min, double max) {
            return num(label, getter, setter, min, max, "%.2f"::formatted);
        }

        public Settings num(Text label, DoubleSupplier getter, DoubleConsumer setter, double min, double max,
                            DoubleFunction<String> sliderLabel) {
            custom(label,
                    SyncHandlers.doubleNumber(getter, setter)
                            .allowC2S(),
                    new SliderWidget()
                            .bounds(min, max)
                            .width(50)
                            .stopper((max - min) / 10))
                    .<Flow>last()
                    .child(new TextWidget<>(
                            Text.dynamic(() -> Component.literal(sliderLabel.apply(getter.getAsDouble())))));
            return this;
        }

        public Settings str(Text label, Supplier<String> getter, Consumer<String> setter) {
            return custom(label,
                    SyncHandlers.string(getter, setter)
                            .allowC2S(),
                    new TextFieldWidget());
        }

        public Settings progress(Text label, DoubleSupplier getter, DoubleFunction<String> rightLabel) {
            DoubleSyncValue syncValue = SyncHandlers.doubleNumber(getter, null);
            custom(label, syncValue, new ProgressWidget()
                    .texture(new Rectangle().hollow(1).color(0xFF555555),
                            new DrawableStack(
                                    new Rectangle().color(0xFFEEE600),
                                    new Rectangle().hollow(1).color(0xFF555555)),
                            ProgressDrawable.Direction.RIGHT)
                    .width(50))
                    .<Flow>last()
                    .child(new TextWidget<>(
                            Text.dynamic(() -> Component.literal(rightLabel.apply(syncValue.getValue())))));
            return this;
        }
    }
}
