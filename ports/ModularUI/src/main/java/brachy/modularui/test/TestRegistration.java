package brachy.modularui.test;

import brachy.modularui.ModularUI;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

import java.util.function.Function;
import java.util.function.Supplier;

public class TestRegistration {

    // @formatter:off
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ModularUI.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModularUI.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModularUI.MOD_ID);


    private static Function<Item.Properties, TestItem> testItemFactory() {
        if (ModularUI.Mods.CURIOS.isLoaded()) {
            return TestCurioItem::new;
        }
        return TestItem::new;
    }

    public static final DeferredItem<TestItem> TEST_ITEM = ITEMS.registerItem("test_item", testItemFactory());

    public static final DeferredBlock<TestBlock> TEST_BLOCK = BLOCKS.register("test_block", () -> new TestBlock(TestBlockEntity::new));
    public static final DeferredItem<BlockItem> TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TEST_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("test_block", () -> BlockEntityType.Builder.of(TestBlockEntity::new, TEST_BLOCK.get()).build(null));

    public static final DeferredBlock<TestBlock> TEST_MACHINE_BLOCK = BLOCKS.register("machine_block", () -> new TestBlock(TestMachine.BE::new));
    public static final DeferredItem<BlockItem> TEST_MACHINE_BLOCK_ITEM = ITEMS.register("machine_block", () -> new BlockItem(TEST_MACHINE_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestMachine.BE>> TEST_MACHINE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("machine_block", () -> BlockEntityType.Builder.of(TestMachine.BE::new, TEST_MACHINE_BLOCK.get()).build(null));
    // @formatter:on

    @SubscribeEvent
    public static void modifyCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(TEST_ITEM.toStack());
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> {
            return new ItemStackHandler(4);
        }, TestRegistration.TEST_ITEM.get());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TestRegistration.TEST_MACHINE_BLOCK_ENTITY.get(), (machine, dir) -> {
            return machine.getInventory();
        });
    }

    public static void register(IEventBus modBus) {
        modBus.register(TestRegistration.class);

        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
    }
}
