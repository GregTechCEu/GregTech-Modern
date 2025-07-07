package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowerlessJetpack implements IArmorLogic, IJetpack, IItemHUDProvider {

    // Map of FluidIngredient -> burn time
    public static final AbstractObject2IntMap<FluidIngredient> FUELS = new Object2IntOpenHashMap<>();
    public static final int tankCapacity = 16000;

    private FluidIngredient currentFuel = FluidIngredient.EMPTY;
    private FluidIngredient previousFuel = FluidIngredient.EMPTY;
    private int burnTimer = 0;

    @OnlyIn(Dist.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    public PowerlessJetpack() {
        if (GTCEu.isClientSide())
            HUD = new ArmorUtils.ModularHUD();
    }

    @Override
    public void onArmorTick(Level world, Player player, @NotNull ItemStack stack) {
        if (!FluidUtil.getFluidHandler(stack).isPresent()) return;

        CompoundTag data = stack.getOrCreateTag();

        if (data.contains("burnTimer")) burnTimer = data.getShort("burnTimer");
        if (!data.contains("enabled")) {
            data.putBoolean("enabled", true);
            data.putBoolean("hover", false);
            data.putByte("toggleTimer", (byte) 0);
        }

        boolean jetpackEnabled = data.getBoolean("enabled");
        boolean hoverMode = data.getBoolean("hover");
        byte toggleTimer = data.getByte("toggleTimer");

        String messageKey = null;
        if (toggleTimer == 0) {
            if (KeyBind.JETPACK_ENABLE.isKeyDown(player)) {
                jetpackEnabled = !jetpackEnabled;
                messageKey = "metaarmor.jetpack.flight." + (jetpackEnabled ? "enable" : "disable");
                data.putBoolean("enabled", jetpackEnabled);
            } else if (KeyBind.ARMOR_HOVER.isKeyDown(player)) {
                hoverMode = !hoverMode;
                messageKey = "metaarmor.jetpack.hover." + (hoverMode ? "enable" : "disable");
                data.putBoolean("hover", hoverMode);
            }

            if (messageKey != null) {
                toggleTimer = 5;
                if (!world.isClientSide) player.displayClientMessage(Component.translatable(messageKey), true);
            }
        }

        if (toggleTimer > 0) toggleTimer--;
        data.putByte("toggleTimer", toggleTimer);

        if (currentFuel.isEmpty())
            findNewRecipe(stack);

        performFlying(player, jetpackEnabled, hoverMode, stack);
        data.putShort("burnTimer", (short) burnTimer);
    }

    @Override
    public ArmorItem.Type getArmorType() {
        return ArmorItem.Type.CHESTPLATE;
    }

    @Override
    public int getArmorDisplay(Player player, @NotNull ItemStack armor, EquipmentSlot slot) {
        return 0;
    }

    @Override
    public void addToolComponents(@NotNull ArmorComponentItem item) {
        item.attachComponents(new Behaviour(tankCapacity));
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/liquid_fuel_jetpack.png");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(@NotNull ItemStack item, GuiGraphics guiGraphics) {
        IFluidHandler tank = FluidUtil.getFluidHandler(item).resolve().orElse(null);
        if (tank != null) {
            if (tank.getFluidInTank(0).getAmount() == 0) return;
            String formated = String.format("%.1f",
                    (tank.getFluidInTank(0).getAmount() * 100.0F / tank.getTankCapacity(0)));
            this.HUD.newString(Component.translatable("metaarmor.hud.fuel_lvl", formated + "%"));
            CompoundTag data = item.getTag();

            if (data != null) {
                if (data.contains("enabled")) {
                    Component status = (data.getBoolean("enabled") ?
                            Component.translatable("metaarmor.hud.status.enabled") :
                            Component.translatable("metaarmor.hud.status.disabled"));
                    Component result = Component.translatable("metaarmor.hud.engine_enabled", status);
                    this.HUD.newString(result);
                }
                if (data.contains("hover")) {
                    Component status = (data.getBoolean("hover") ?
                            Component.translatable("metaarmor.hud.status.enabled") :
                            Component.translatable("metaarmor.hud.status.disabled"));
                    Component result = Component.translatable("metaarmor.hud.hover_mode", status);
                    this.HUD.newString(result);
                }
            }
        }
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @Override
    public int getEnergyPerUse() {
        return 1;
    }

    @Override
    public boolean canUseEnergy(ItemStack stack, int amount) {
        if (currentFuel.isEmpty()) return false;
        if (burnTimer > 0) return true;
        var ret = FluidUtil.getFluidHandler(stack)
                .map(h -> h.drain(Integer.MAX_VALUE, FluidAction.SIMULATE))
                .map(drained -> drained.getAmount() >= currentFuel.getAmount())
                .orElse(Boolean.FALSE);
        if (!ret) currentFuel = FluidIngredient.EMPTY;
        return ret;
    }

    @Override
    public void drainEnergy(ItemStack stack, int amount) {
        if (burnTimer == 0) {
            FluidUtil.getFluidHandler(stack)
                    .ifPresent(h -> h.drain(currentFuel.getAmount(), FluidAction.EXECUTE));
            burnTimer = FUELS.getInt(currentFuel);
        }
        burnTimer -= amount;
    }

    @Override
    public boolean hasEnergy(ItemStack stack) {
        return burnTimer > 0 || !currentFuel.isEmpty();
    }

    public void findNewRecipe(@NotNull ItemStack stack) {
        FluidUtil.getFluidContained(stack).ifPresentOrElse(fluid -> {
            if (!previousFuel.isEmpty() && previousFuel.test(fluid) &&
                    fluid.getAmount() >= previousFuel.getAmount()) {
                currentFuel = previousFuel;
                return;
            }

            for (var fuel : FUELS.keySet()) {
                if (fuel.test(fluid) && fluid.getAmount() >= fuel.getAmount()) {
                    previousFuel = currentFuel = fuel;
                }
            }
        }, () -> currentFuel = FluidIngredient.EMPTY);
    }

    /*
     * @Override
     * public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor,
     * 
     * @NotNull DamageSource source, double damage,
     * EntityEquipmentSlot equipmentSlot) {
     * int damageLimit = (int) Math.min(Integer.MAX_VALUE, burnTimer * 1.0 / 32 * 25.0);
     * if (source.isUnblockable()) return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
     * return new ISpecialArmor.ArmorProperties(0, 0, damageLimit);
     * }
     */

    public static class Behaviour implements IDurabilityBar, IItemComponent, ISubItemHandler, IAddInformation,
                                  IInteractionItem, IComponentCapability {

        public final int maxCapacity;
        private final IntIntPair durabilityBarColors;

        public Behaviour(int internalCapacity) {
            this.maxCapacity = internalCapacity;
            this.durabilityBarColors = GradientUtil.getGradient(0xB7AF08, 10);
        }

        @Override
        public float getDurabilityForDisplay(@NotNull ItemStack itemStack) {
            return FluidUtil.getFluidContained(itemStack)
                    .map(stack -> (float) stack.getAmount() / maxCapacity)
                    .orElse(0f);
        }

        @Nullable
        @Override
        public IntIntPair getDurabilityColorsForDisplay(ItemStack itemStack) {
            return durabilityBarColors;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap,
                    LazyOptional.of(() -> new FluidHandlerItemStack(itemStack, maxCapacity) {

                        @Override
                        public boolean canFillFluidType(FluidStack fluid) {
                            for (var ingredient : FUELS.keySet()) {
                                if (ingredient.test(fluid)) return true;
                            }
                            return false;
                        }
                    }));
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                    TooltipFlag isAdvanced) {
            CompoundTag data = stack.getOrCreateTag();
            Component state;
            boolean enabled = !data.contains("enabled") || data.getBoolean("enabled");
            state = enabled ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.engine_enabled", state));

            boolean hover = data.contains("hover") && data.getBoolean("hover");
            state = hover ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.hover_mode", state));
        }

        @Override
        public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
            ItemStack copy = item.getDefaultInstance();
            IFluidHandler fluidHandlerItem = FluidUtil.getFluidHandler(copy).resolve().orElse(null);
            if (fluidHandlerItem != null) {
                fluidHandlerItem.fill(GTMaterials.Diesel.getFluid(tankCapacity), IFluidHandler.FluidAction.SIMULATE);
                items.add(copy);
            } else {
                items.add(copy);
            }
        }
    }
}
