package com.gregtechceu.gtceu.api.item.armor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import lombok.Getter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ArmorComponentItem extends ArmorItem implements IComponentItem {
    @Getter
    private IArmorLogic armorLogic = new DummyArmorLogic();
    @Getter
    protected List<IItemComponent> components;

    public ArmorComponentItem(ArmorMaterial material, ArmorItem.Type type, Properties properties) {
        super(material, type, properties);
        components = new ArrayList<>();
    }

    public ArmorComponentItem setArmorLogic(IArmorLogic armorLogic) {
        Preconditions.checkNotNull(armorLogic, "Cannot set ArmorLogic to null");
        this.armorLogic = armorLogic;
        this.armorLogic.addToolComponents(this);
        return this;
    }

    public void attachComponents(IItemComponent... components) {
        this.components.addAll(Arrays.asList(components));
        for (IItemComponent component : components) {
            component.onAttached(this);
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        IArmorLogic armorLogic = getArmorLogic();
        multimap.putAll(armorLogic.getAttributeModifiers(slot, stack));
        return multimap;
    }

    @Override
    public ArmorItem.Type getType() {
        return armorLogic.getArmorType();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (this.getType().getSlot().getFilterFlag() + 100 == slotId && entity instanceof Player player) {
            this.armorLogic.onArmorTick(level, player, stack);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 50;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return armorLogic.getArmorModel(livingEntity, itemStack, equipmentSlot, original);
            }
        });
    }

    @Nullable
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return armorLogic.getArmorTexture(stack, entity, slot, type).toString();
    }
}
