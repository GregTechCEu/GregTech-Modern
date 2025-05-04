package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.api.recipe.content.SerializerFluidIngredient;
import com.gregtechceu.gtceu.data.item.GTDataComponents;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public class PowerlessJetpack implements IArmorLogic, IJetpack, IItemHUDProvider {

    // Map of FluidIngredient -> burn time
    public static final AbstractObject2IntMap<SizedFluidIngredient> FUELS = new Object2IntOpenHashMap<>();
    public static final int tankCapacity = 16000;

    private SizedFluidIngredient currentFuel = SerializerFluidIngredient.EMPTY;
    private SizedFluidIngredient previousFuel = SerializerFluidIngredient.EMPTY;
    private int burnTimer = 0;

    @OnlyIn(Dist.CLIENT)
    private ArmorUtils.@UnknownNullability ModularHUD HUD;

    public PowerlessJetpack() {
        if (GTCEu.isClientSide())
            HUD = new ArmorUtils.ModularHUD();
    }

    @Override
    public void onArmorTick(Level world, Player player, @NotNull ItemStack stack) {
        if (FluidUtil.getFluidHandler(stack).isEmpty()) return;

        GTArmor.Mutable data = stack.getOrDefault(GTDataComponents.ARMOR_DATA, GTArmor.EMPTY).toMutable();

        boolean jetpackEnabled = data.enabled();
        boolean hoverMode = data.hover();
        byte toggleTimer = data.toggleTimer();

        String messageKey = null;
        if (toggleTimer == 0) {
            if (KeyBind.JETPACK_ENABLE.isKeyDown(player)) {
                jetpackEnabled = !jetpackEnabled;
                messageKey = "metaarmor.jetpack.flight." + (jetpackEnabled ? "enable" : "disable");
                data.enabled(jetpackEnabled);
            } else if (KeyBind.ARMOR_HOVER.isKeyDown(player)) {
                hoverMode = !hoverMode;
                messageKey = "metaarmor.jetpack.hover." + (hoverMode ? "enable" : "disable");
                data.hover(hoverMode);
            }

            if (messageKey != null) {
                toggleTimer = 5;
                if (!world.isClientSide) player.displayClientMessage(Component.translatable(messageKey), true);
            }
        }

        if (toggleTimer > 0) toggleTimer--;
        data.toggleTimer(toggleTimer);

        if (currentFuel.ingredient().hasNoFluids())
            findNewRecipe(stack);

        performFlying(player, jetpackEnabled, hoverMode, stack);
        data.burnTimer((short) burnTimer);
        stack.set(GTDataComponents.ARMOR_DATA, data.toImmutable());
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
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
        return GTCEu.id("textures/armor/liquid_fuel_jetpack.png");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(@NotNull ItemStack item, GuiGraphics guiGraphics) {
        IFluidHandler tank = FluidUtil.getFluidHandler(item).orElse(null);
        if (tank != null) {
            if (tank.getFluidInTank(0).getAmount() == 0) return;
            String formated = String.format("%.1f",
                    (tank.getFluidInTank(0).getAmount() * 100.0F / tank.getTankCapacity(0)));
            this.HUD.newString(Component.translatable("metaarmor.hud.fuel_lvl", formated + "%"));
            GTArmor data = item.get(GTDataComponents.ARMOR_DATA);

            if (data != null) {
                Component status = data.enabled() ?
                        Component.translatable("metaarmor.hud.status.enabled") :
                        Component.translatable("metaarmor.hud.status.disabled");
                Component result = Component.translatable("metaarmor.hud.engine_enabled", status);
                this.HUD.newString(result);

                status = data.hover() ?
                        Component.translatable("metaarmor.hud.status.enabled") :
                        Component.translatable("metaarmor.hud.status.disabled");
                result = Component.translatable("metaarmor.hud.hover_mode", status);
                this.HUD.newString(result);
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
        if (currentFuel.ingredient().hasNoFluids()) return false;
        if (burnTimer > 0) return true;
        var ret = FluidUtil.getFluidHandler(stack)
                .map(h -> h.drain(Integer.MAX_VALUE, FluidAction.SIMULATE))
                .map(drained -> drained.getAmount() >= currentFuel.amount())
                .orElse(Boolean.FALSE);
        if (!ret) currentFuel = SerializerFluidIngredient.EMPTY;
        return ret;
    }

    @Override
    public void drainEnergy(ItemStack stack, int amount) {
        if (burnTimer == 0) {
            FluidUtil.getFluidHandler(stack)
                    .ifPresent(h -> h.drain(currentFuel.amount(), FluidAction.EXECUTE));
            burnTimer = FUELS.getInt(currentFuel);
        }
        burnTimer -= amount;
    }

    @Override
    public boolean hasEnergy(ItemStack stack) {
        return burnTimer > 0 || !currentFuel.ingredient().hasNoFluids();
    }

    public void findNewRecipe(@NotNull ItemStack stack) {
        FluidUtil.getFluidContained(stack).ifPresentOrElse(fluid -> {
            if (!previousFuel.ingredient().hasNoFluids() && previousFuel.test(fluid) &&
                    fluid.getAmount() >= previousFuel.amount()) {
                currentFuel = previousFuel;
                return;
            }

            for (var fuel : FUELS.keySet()) {
                if (fuel.test(fluid) && fluid.getAmount() >= fuel.amount()) {
                    previousFuel = currentFuel = fuel;
                }
            }
        }, () -> currentFuel = SerializerFluidIngredient.EMPTY);
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
        private final Pair<Integer, Integer> durabilityBarColors;

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
        public Pair<Integer, Integer> getDurabilityColorsForDisplay(ItemStack itemStack) {
            return durabilityBarColors;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                    TooltipFlag isAdvanced) {
            GTArmor data = stack.getOrDefault(GTDataComponents.ARMOR_DATA, GTArmor.EMPTY);

            Component state = data.enabled() ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.engine_enabled", state));

            state = data.hover() ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.hover_mode", state));
        }

        @Override
        public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, unused) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack, maxCapacity) {

                        private SizedFluidIngredient currentFuel = SerializerFluidIngredient.EMPTY;

                        @Override
                        public boolean canFillFluidType(@NotNull FluidStack fluid) {
                            if (!currentFuel.ingredient().hasNoFluids() && currentFuel.test(fluid) &&
                                    fluid.getAmount() >= currentFuel.amount()) {
                                return true;
                            }

                            boolean found = false;
                            for (var fuel : FUELS.keySet()) {
                                if (fuel.test(fluid) && fluid.getAmount() >= fuel.amount()) {
                                    currentFuel = fuel;
                                    found = true;
                                }
                            }
                            return found;
                        }
                    }, item);
        }

        @Override
        public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
            ItemStack copy = item.getDefaultInstance();
            IFluidHandler fluidHandlerItem = FluidUtil.getFluidHandler(copy).orElse(null);
            if (fluidHandlerItem != null) {
                fluidHandlerItem.fill(GTMaterials.Diesel.getFluid(tankCapacity), IFluidHandler.FluidAction.SIMULATE);
                items.add(copy);
            } else {
                items.add(copy);
            }
        }
    }
}
