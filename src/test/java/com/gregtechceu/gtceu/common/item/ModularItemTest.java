package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ItemModule;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItemModules;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.electric.BatteryBufferMachine;
import com.gregtechceu.gtceu.gametest.util.TestUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(GTCEu.MOD_ID)
public class ModularItemTest {

    /**
     * @return a modular item that has 2 LuV slots and an MV slot
     */
    private ItemStack makeModularItem(GameTestHelper helper) {
        ItemStack armor = GTItems.NANO_CHESTPLATE_ADVANCED.asStack();
        IModularItem modularArmor = getModularItem(helper, armor);
        modularArmor.setSlots(List.of(GTItemModules.TIERED_SLOTS[GTValues.LuV],
                GTItemModules.TIERED_SLOTS[GTValues.LuV], GTItemModules.TIERED_SLOTS[GTValues.MV]));
        return armor;
    }

    private @NotNull IModularItem getModularItem(GameTestHelper helper, ItemStack stack) {
        IModularItem modular = GTCapabilityHelper.getModularItem(stack);
        TestUtils.assertNotNull(helper, modular, stack.getDisplayName().getString() + " was not modular!");
        return modular;
    }

    private void checkModule(GameTestHelper helper, IModularItem modular, int slot, ItemModule module, ItemLike item) {
        AppliedItemModule appliedModule = modular.getModuleInSlot(slot);
        TestUtils.assertNotNull(helper, appliedModule, "module in slot %d was null".formatted(slot));
        TestUtils.assertEqual(helper, appliedModule.getModule(), module, "incorrect module in slot %d".formatted(slot));
        TestUtils.assertEqual(helper, appliedModule.getModuleItem(), new ItemStack(item),
                "incorrect module item in slot %d".formatted(slot));
    }

    private void attachFullBattery(ItemStack stack) {
        IModularItem modular = GTCapabilityHelper.getModularItem(stack);
        assert modular != null;
        modular.attach(GTItemModules.BATTERY, false).setModuleItem(chargeToMax(GTItems.ULTIMATE_BATTERY.asStack()));
    }

    private ItemStack chargeToMax(ItemStack stack) {
        IElectricItem electric = GTCapabilityHelper.getElectricItem(stack);
        assert electric != null;
        electric.charge(electric.getMaxCharge(), electric.getTier(), true, false);
        return stack;
    }

    @GameTest(template = "empty_5x5", batch = "modularItemTests")
    public void testSlotAssignment(GameTestHelper helper) {
        ItemStack stack = makeModularItem(helper);
        IModularItem modular = getModularItem(helper, stack);
        TestUtils.assertEqual(helper, modular.getSlots().size(), 3, "Unexpected module slot amount");
        helper.assertTrue(GTItemModules.TIERED_SLOTS[GTValues.LuV] == modular.getSlots().get(1),
                "Expected 2nd slot to be LuV, got " + modular.getSlots().get(1).getDisplayName().getString());
        helper.succeed();
    }

    @GameTest(template = "empty_5x5", batch = "modularItemTests")
    public void testEquipmentFoundry(GameTestHelper helper) {
        helper.setBlock(0, 0, 0, GTBlocks.EQUIPMENT_FOUNDRY.get());
        ItemStack armor = makeModularItem(helper);

        IItemHandler top = TestUtils.getItemHandler(helper, BlockPos.ZERO, Direction.UP);
        IItemHandler side = TestUtils.getItemHandler(helper, BlockPos.ZERO, Direction.NORTH);
        IItemHandler bottom = TestUtils.getItemHandler(helper, BlockPos.ZERO, Direction.DOWN);

        // try inserting various modules
        TestUtils.insertItem(helper, top, armor, true, "failed to insert modular item into equipment foundry");
        TestUtils.insertItem(helper, side, GTItems.ELECTRIC_MOTOR_ZPM, false,
                "successfully inserted ZPM module even though max slot tier is LuV");
        TestUtils.insertItem(helper, side, GTItems.ELECTRIC_MOTOR_LuV, true,
                "unable to insert LuV module even though 2 LuV slots should be empty");
        TestUtils.insertItem(helper, side, GTItems.ELECTRIC_MOTOR_IV, false, "successfully inserted duplicate module");
        TestUtils.insertItem(helper, side, GTItems.ELECTRIC_PISTON_LuV, true,
                "unable to insert LuV module even though a LuV slot should be empty");
        TestUtils.insertItem(helper, side, GTItems.ROBOT_ARM_LuV, false,
                "successfully inserted 3rd LuV module when there should be only 2 LuV slots");
        TestUtils.insertItem(helper, side, GTItems.ROBOT_ARM_LV, true,
                "unable to insert LV module even though MV slot should be available");

        // check output
        ItemStack output = bottom.extractItem(0, 1, false);
        TestUtils.assertEqual(helper, output, makeModularItem(helper));
        IModularItem modular = getModularItem(helper, output);

        checkModule(helper, modular, 0, GTItemModules.ATTACK_SPEED[GTValues.LuV], GTItems.ELECTRIC_MOTOR_LuV);
        checkModule(helper, modular, 1, GTItemModules.ATTACK_DAMAGE[GTValues.LuV], GTItems.ELECTRIC_PISTON_LuV);
        checkModule(helper, modular, 2, GTItemModules.BLOCK_REACH[GTValues.LV], GTItems.ROBOT_ARM_LV);

        helper.succeed();
    }

    @GameTest(template = "empty_5x5", batch = "modularItemTests")
    public void testAttributeModule(GameTestHelper helper) {
        ItemStack armor = makeModularItem(helper);
        IModularItem modular = getModularItem(helper, armor);

        AppliedItemModule module = modular.attach(GTItemModules.ATTACK_DAMAGE[GTValues.IV], false);
        helper.assertTrue(armor.getAttributeModifiers(EquipmentSlot.CHEST).containsKey(Attributes.ATTACK_DAMAGE),
                "modular item did not have damage attribute");

        module.setEnabled(false);
        helper.assertFalse(armor.getAttributeModifiers(EquipmentSlot.CHEST).containsKey(Attributes.ATTACK_DAMAGE),
                "modular item had damage attribute with disabled module");

        module.setEnabled(true);
        helper.assertTrue(armor.getAttributeModifiers(EquipmentSlot.CHEST).containsKey(Attributes.ATTACK_DAMAGE),
                "modular item did not have damage attribute with re-enabled module");

        module.detach();
        helper.assertFalse(armor.getAttributeModifiers(EquipmentSlot.CHEST).containsKey(Attributes.ATTACK_DAMAGE),
                "modular item had damage attribute with detached module");
        TestUtils.assertEqual(helper, modular.getModuleInSlot(0), null,
                "modular item retained module even though it was detached");

        helper.succeed();
    }

    @GameTest(template = "empty_5x5", batch = "modularItemTests")
    public void testFlightModule(GameTestHelper helper) {
        ItemStack armor = makeModularItem(helper);
        IModularItem modular = getModularItem(helper, armor);
        modular.attach(GTItemModules.CREATIVE_FLIGHT, false);
        attachFullBattery(armor);
        Player player = helper.makeMockSurvivalPlayer();
        helper.assertFalse(player.getAbilities().mayfly, "default survival player had ability to fly");
        player.getInventory().setItem(0, armor);
        player.tick();
        helper.assertTrue(player.getAbilities().mayfly, "player could not fly with creative flight module");
        helper.succeed();
    }

    @GameTest(template = "empty_5x5", batch = "modularItemTests")
    public void testEnergyShieldModule(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        ItemStack armor = makeModularItem(helper);
        IModularItem modular = getModularItem(helper, armor);

        modular.attach(GTItemModules.DAMAGE_BLOCK[GTValues.LuV], false);
        attachFullBattery(armor);

        helper.setBlock(0, 0, 0, Blocks.SMOOTH_QUARTZ);
        helper.setBlock(0, 3, 0, Blocks.SMOOTH_QUARTZ);
        helper.setBlock(2, 0, 0, Blocks.SMOOTH_QUARTZ);

        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 0, 1, 0);
        // noinspection DataFlowIssue
        entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addPermanentModifier(
                new AttributeModifier("no_knockback", 1000, AttributeModifier.Operation.ADDITION));
        helper.spawn(EntityType.IRON_GOLEM, 2, 1, 0);
        entity.equipItemIfPossible(armor);

        TestUtils.assertEqual(helper, entity.getHealth(), entity.getMaxHealth(),
                "entity health wasn't max when just spawned");
        helper.runAtTickTime(80, () -> TestUtils.assertEqual(helper, entity.getHealth(), entity.getMaxHealth(),
                "entity was damaged even though with energy shield"));
    }

    @GameTest(template = "empty_5x5", batch = "modularItemTests")
    public void testWirelessCharging(GameTestHelper helper) {
        TestUtils.succeedAfterTest(helper);
        ItemStack armor = makeModularItem(helper);
        IModularItem modular = getModularItem(helper, armor);

        Player player = helper.makeMockSurvivalPlayer();
        player.moveTo(helper.absoluteVec(Vec3.atBottomCenterOf(Vec3i.ZERO)));
        helper.getLevel().addFreshEntity(player);

        BatteryBufferMachine buffer = (BatteryBufferMachine) TestUtils.setMachine(helper, BlockPos.ZERO,
                GTMachines.BATTERY_BUFFER_4[GTValues.MV]);
        ItemStack bufferBattery = chargeToMax(GTItems.BATTERY_MV_LITHIUM.asStack());
        buffer.getBatteryInventory().setStackInSlot(0, bufferBattery);
        buffer.setOwnerUUID(player.getUUID());
        IElectricItem bufferBatteryElectricItem = GTCapabilityHelper.getElectricItem(bufferBattery);
        assert bufferBatteryElectricItem != null;

        ItemStack sensor = GTItems.SENSOR_LuV.asStack();
        sensor.onItemUseFirst(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, sensor,
                new BlockHitResult(Vec3.ZERO, Direction.UP, buffer.getBlockPos(), false)));
        modular.attach(GTItemModules.WIRELESS_CHARGER[GTValues.LuV], false).setModuleItem(sensor);
        modular.attach(GTItemModules.BATTERY, false).setModuleItem(GTItems.BATTERY_MV_LITHIUM.asStack());

        player.setItemSlot(EquipmentSlot.CHEST, armor);

        helper.runAtTickTime(20, () -> {
            IElectricItem electric = GTCapabilityHelper.getElectricItem(armor);
            TestUtils.assertNotNull(helper, electric, "expected armor with battery to be electric");
            helper.assertTrue(electric.getCharge() > 0, "Armor didn't charge");
            TestUtils.assertEqual(helper, electric.getCharge() + bufferBatteryElectricItem.getCharge(),
                    electric.getMaxCharge(), "Sum of energy was not equal to initial energy");
        });
    }
}
