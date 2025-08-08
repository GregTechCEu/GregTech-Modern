package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorLogicSuite;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.core.IFireImmuneEntity;
import com.gregtechceu.gtceu.utils.input.KeyBind;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMappings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class QuarkTechSuite extends ArmorLogicSuite implements IStepAssist {

    public static final Reference2IntMap<MobEffect> potionRemovalCost = new Reference2IntOpenHashMap<>();
    private float charge = 0.0F;
    private static final byte RUNNING_TIMER = 10; // .5 seconds
    private static final byte JUMPING_TIMER = 10; // .5 seconds
    private static final double LEGGING_ACCEL = 0.085D;

    @OnlyIn(Dist.CLIENT)
    protected ArmorUtils.ModularHUD HUD;

    public QuarkTechSuite(ArmorItem.Type slot, int energyPerUse, long capacity, int tier) {
        super(energyPerUse, capacity, tier, slot);
        potionRemovalCost.put(MobEffects.POISON, 10000);
        potionRemovalCost.put(MobEffects.WITHER, 25000);
        potionRemovalCost.put(MobEffects.CONFUSION, 8000);
        potionRemovalCost.put(MobEffects.DIG_SLOWDOWN, 12500);
        // potionRemovalCost.put(MobEffects.BAD_OMEN, 30000);
        potionRemovalCost.put(MobEffects.MOVEMENT_SLOWDOWN, 9000);
        potionRemovalCost.put(MobEffects.UNLUCK, 5000);
        if (GTCEu.isClientSide() && this.shouldDrawHUD()) {
            HUD = new ArmorUtils.ModularHUD();
        }
    }

    @Override
    public void onArmorTick(Level world, Player player, ItemStack itemStack) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item == null)
            return;

        CompoundTag data = itemStack.getOrCreateTag();
        byte toggleTimer = data.contains("toggleTimer") ? data.getByte("toggleTimer") : 0;
        int nightVisionTimer = data.contains("nightVisionTimer") ? data.getInt("nightVisionTimer") :
                ArmorUtils.NIGHTVISION_DURATION;
        byte runningTimer = data.contains("runningTimer") ? data.getByte("runningTimer") : RUNNING_TIMER;
        byte boostedJumpTimer = data.contains("boostedJumpTimer") ? data.getByte("boostedJumpTimer") : JUMPING_TIMER;

        if (!player.getItemBySlot(EquipmentSlot.CHEST).is(GTItems.QUANTUM_CHESTPLATE.get()) &&
                !player.getItemBySlot(EquipmentSlot.CHEST).is(GTItems.QUANTUM_CHESTPLATE_ADVANCED.get())) {
            if (!world.isClientSide) ((IFireImmuneEntity) player).gtceu$setFireImmune(false);
        }

        boolean ret = false;
        if (type == ArmorItem.Type.HELMET) {
            ret = supplyAir(item, player) || supplyFood(item, player);

            removeNegativeEffects(item, player);

            boolean nightVision = data.contains("nightVision") && data.getBoolean("nightVision");
            if (toggleTimer == 0 && KeyBind.ARMOR_MODE_SWITCH.isKeyDown(player)) {
                nightVision = !nightVision;
                toggleTimer = 5;
                if (item.getCharge() < ArmorUtils.MIN_NIGHTVISION_CHARGE) {
                    nightVision = false;
                    player.displayClientMessage(Component.translatable("metaarmor.nms.nightvision.error"), true);
                } else {
                    player.displayClientMessage(Component
                            .translatable("metaarmor.nms.nightvision." + (nightVision ? "enabled" : "disabled")), true);
                }
            }

            if (nightVision) {
                player.removeEffect(MobEffects.BLINDNESS);
                if (nightVisionTimer <= ArmorUtils.NIGHT_VISION_RESET) {
                    nightVisionTimer = ArmorUtils.NIGHTVISION_DURATION;
                    player.addEffect(
                            new MobEffectInstance(MobEffects.NIGHT_VISION, ArmorUtils.NIGHTVISION_DURATION, 0, true,
                                    false));
                    item.discharge((4), this.tier, true, false, false);
                }
            } else {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            data.putBoolean("nightVision", nightVision);

            if (nightVisionTimer > 0) nightVisionTimer--;
            if (toggleTimer > 0) toggleTimer--;

            data.putInt("nightVisionTimer", nightVisionTimer);
            data.putByte("toggleTimer", toggleTimer);
        } else if (type == ArmorItem.Type.CHESTPLATE && !player.fireImmune()) {
            ((IFireImmuneEntity) player).gtceu$setFireImmune(true);
            if (player.isOnFire()) player.extinguishFire();
        } else if (type == ArmorItem.Type.LEGGINGS) {
            boolean canUseEnergy = item.canUse(energyPerUse / 100);
            boolean sprinting = SyncedKeyMappings.VANILLA_FORWARD.isKeyDown(player) && player.isSprinting();
            boolean jumping = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
            boolean sneaking = SyncedKeyMappings.VANILLA_SNEAK.isKeyDown(player);

            if (canUseEnergy && sprinting) {
                if (runningTimer == 0) {
                    runningTimer = RUNNING_TIMER;
                    item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
                }
            }
            if (canUseEnergy && (player.onGround() || player.isInWater()) && sprinting) {
                float speed = 0.25F;
                if (player.isInWater()) {
                    speed = 0.1F;
                    if (jumping) {
                        player.push(0.0, 0.1, 0.0);
                        player.hurtMarked = true;
                    }
                }
                player.moveRelative(speed, new Vec3(0, 0, 1));
            } else if (canUseEnergy && player.isInWater() && (sneaking || jumping)) {
                if (sneaking)
                    player.push(0.0, -LEGGING_ACCEL, 0.0);
                if (jumping)
                    player.push(0.0, LEGGING_ACCEL, 0.0);
            }

            if (runningTimer > 0) runningTimer--;
            data.putByte("runningTimer", runningTimer);
        } else if (type == ArmorItem.Type.BOOTS) {
            boolean canUseEnergy = item.canUse(energyPerUse / 100);
            boolean jumping = SyncedKeyMappings.VANILLA_JUMP.isKeyDown(player);
            boolean boostedJump = data.contains("boostedJump") && data.getBoolean("boostedJump");
            if (boostedJumpTimer == 0 && KeyBind.BOOTS_ENABLE.isKeyDown(player)) {
                boostedJump = !boostedJump;
                boostedJumpTimer = JUMPING_TIMER;
                player.displayClientMessage(Component
                        .translatable("metaarmor.nms.boosted_jump." + (boostedJump ? "enabled" : "disabled")), true);
            }
            if (boostedJump) {
                if (!world.isClientSide) {
                    boolean onGround = !data.contains("onGround") || data.getBoolean("onGround");
                    if (onGround && !player.onGround() && jumping) {
                        item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
                        ret = true;
                    }

                    if (player.onGround() != onGround) {
                        data.putBoolean("onGround", player.onGround());
                    }
                } else {
                    if (canUseEnergy && player.onGround()) {
                        this.charge = 1.0F;
                    }

                    Vec3 delta = player.getDeltaMovement();
                    if (delta.y >= 0.0D && this.charge > 0.0F && !player.isInWater()) {
                        if (jumping) {
                            if (this.charge == 1.0F) {
                                player.setDeltaMovement(delta.x * 3.6D, delta.y, delta.z * 3.6D);
                            }
                            // gives an arc path for movement force
                            player.addDeltaMovement(new Vec3(0.0, this.charge * 0.32, 0.0));
                            this.charge = (float) (this.charge * 0.7D);
                        } else if (this.charge < 1.0F) {
                            this.charge = 0.0F;
                        }
                    }
                }
            }
            data.putBoolean("boostedJump", boostedJump);

            if (boostedJumpTimer > 0) boostedJumpTimer--;

            data.putInt("boostedJumpTimer", boostedJumpTimer);
        }

        if (ret) {
            player.inventoryMenu.sendAllDataToRemote();
        }
    }

    public boolean supplyAir(@NotNull IElectricItem item, Player player) {
        int air = player.getAirSupply();
        if (item.canUse(energyPerUse / 100) && air < 100) {
            player.setAirSupply(air + 200);
            item.discharge(energyPerUse / 100, item.getTier(), true, false, false);
            return true;
        }
        return false;
    }

    public boolean supplyFood(@NotNull IElectricItem item, Player player) {
        if (item.canUse(energyPerUse / 10) && player.getFoodData().needsFood()) {
            int slotId = -1;
            IItemHandler playerInv = player.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().orElse(null);
            if (playerInv instanceof IItemHandlerModifiable items) {
                for (int i = 0; i < items.getSlots(); i++) {
                    ItemStack current = items.getStackInSlot(i);
                    if (current.getFoodProperties(player) != null) {
                        slotId = i;
                        break;
                    }
                }

                if (slotId > -1) {
                    ItemStack stack = items.getStackInSlot(slotId);
                    InteractionResultHolder<ItemStack> result = ArmorUtils.eat(player, stack);
                    stack = result.getObject();
                    if (stack.isEmpty())
                        items.setStackInSlot(slotId, ItemStack.EMPTY);

                    if (result.getResult() == InteractionResult.SUCCESS)
                        item.discharge(energyPerUse / 10, item.getTier(), true, false, false);

                    return true;
                }
            }
        }
        return false;
    }

    public static void removeNegativeEffects(@NotNull IElectricItem item, Player player) {
        for (MobEffectInstance effect : new LinkedList<>(player.getActiveEffects())) {
            MobEffect potion = effect.getEffect();
            int cost = potionRemovalCost.getOrDefault(potion, -1);
            if (cost != -1) {
                cost = cost * (effect.getAmplifier() + 1);
                if (item.canUse(cost)) {
                    item.discharge(cost, item.getTier(), true, false, false);
                    player.removeEffect(potion);
                }
            }
        }
    }

    /*
     * @Override
     * public ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor, DamageSource source,
     * double damage, EntityEquipmentSlot equipmentSlot) {
     * int damageLimit = Integer.MAX_VALUE;
     * IElectricItem item = armor.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
     * if (item == null) {
     * return new ArmorProperties(0, 0, damageLimit);
     * }
     * if (energyPerUse > 0) {
     * damageLimit = (int) Math.min(damageLimit, 25.0D * item.getCharge() / (energyPerUse * 100.0D));
     * }
     *
     * if (source == DamageSource.FALL) {
     * if (SLOT == EntityEquipmentSlot.FEET) {
     * return new ArmorProperties(10, 1.0D, damageLimit);
     * }
     *
     * if (SLOT == EntityEquipmentSlot.LEGS) {
     * return new ArmorProperties(9, 0.8D, damageLimit);
     * }
     * }
     * return new ArmorProperties(8, getDamageAbsorption() * getAbsorption(armor), damageLimit);
     * }
     *
     * @Override
     * public boolean handleUnblockableDamage(EntityLivingBase entity, @NotNull ItemStack armor, DamageSource source,
     * double damage, EntityEquipmentSlot equipmentSlot) {
     * return source != DamageSource.FALL && source != DamageSource.DROWN && source != DamageSource.STARVE &&
     * source != DamageSource.OUT_OF_WORLD;
     * }
     */

    @Override
    public int damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage,
                           EquipmentSlot equipmentSlot) {
        IElectricItem item = GTCapabilityHelper.getElectricItem(itemStack);
        if (item != null) {
            item.discharge(energyPerUse / 100L * damage, item.getTier(), true, false, false);
        }
        return super.damageArmor(entity, itemStack, source, damage, equipmentSlot);
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        ItemStack currentChest = Minecraft.getInstance().player.getInventory().armor
                .get(EquipmentSlot.CHEST.getIndex());
        String armorTexture = "quark_tech_suite";
        if (currentChest.is(GTItems.QUANTUM_CHESTPLATE_ADVANCED.get())) armorTexture = "advanced_quark_tech_suite";
        return slot != EquipmentSlot.LEGS ?
                GTCEu.id(String.format("textures/armor/%s_1.png", armorTexture)) :
                GTCEu.id(String.format("textures/armor/%s_2.png", armorTexture));
    }

    @Override
    public double getDamageAbsorption() {
        return type == ArmorItem.Type.CHESTPLATE ? 1.2D : 1.0D;
    }

    @Override
    public float getHeatResistance() {
        return 0.5f;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(ItemStack item, GuiGraphics guiGraphics) {
        addCapacityHUD(item, this.HUD);
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @Override
    public void addInfo(ItemStack itemStack, List<Component> lines) {
        super.addInfo(itemStack, lines);
        if (type == ArmorItem.Type.HELMET) {
            CompoundTag nbtData = itemStack.getOrCreateTag();
            boolean nv = nbtData.getBoolean("nightVision");
            if (nv) {
                lines.add(Component.translatable("metaarmor.message.nightvision.enabled"));
            } else {
                lines.add(Component.translatable("metaarmor.message.nightvision.disabled"));
            }
            lines.add(Component.translatable("metaarmor.tooltip.potions"));
            lines.add(Component.translatable("metaarmor.tooltip.breath"));
            lines.add(Component.translatable("metaarmor.tooltip.autoeat"));
        } else if (type == ArmorItem.Type.CHESTPLATE) {
            lines.add(Component.translatable("metaarmor.tooltip.burning"));
            lines.add(Component.translatable("metaarmor.tooltip.freezing"));
        } else if (type == ArmorItem.Type.LEGGINGS) {
            lines.add(Component.translatable("metaarmor.tooltip.speed"));
        } else if (type == ArmorItem.Type.BOOTS) {
            lines.add(Component.translatable("metaarmor.tooltip.stepassist"));
            lines.add(Component.translatable("metaarmor.tooltip.falldamage"));
            lines.add(Component.translatable("metaarmor.tooltip.jump"));
        }
    }

    @Override
    public boolean isPPE() {
        return true;
    }
}
