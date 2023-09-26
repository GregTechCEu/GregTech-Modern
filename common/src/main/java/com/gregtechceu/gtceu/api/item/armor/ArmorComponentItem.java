package com.gregtechceu.gtceu.api.item.armor;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.IItemUseFirst;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import lombok.Getter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ArmorComponentItem extends ArmorItem implements HeldItemUIFactory.IHeldItemUIHolder, IItemRendererProvider, IItemUseFirst, IComponentItem {
    @Getter
    private IArmorLogic armorLogic = new DummyArmorLogic();
    @Getter
    protected List<IItemComponent> components;


    public ArmorComponentItem(ArmorMaterial material, EquipmentSlot slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        return null;
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
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return armorLogic.getAttributeModifiers(slot);
    }

    @Override
    public EquipmentSlot getSlot() {
        return armorLogic.getEquipmentSlot();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (this.getSlot().getFilterFlag() + 100 == slotId) {
            this.armorLogic.onArmorTick(level, entity, stack);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return super.isValidRepairItem(stack, repairCandidate);
    }

    @Nullable
    @Override
    public IRenderer getRenderer(ItemStack stack) {
        return null;
    }

    @Override
    public ModularUI createUI(Player entityPlayer, HeldItemUIFactory.HeldItemHolder holder) {
        return null;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 50;
    }
}
