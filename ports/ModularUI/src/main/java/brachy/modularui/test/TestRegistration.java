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
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.Set;

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

    public static final DeferredItem<TestItem> TEST_ITEM = ITEMS.registerItem("test_item", testItemFactory(), () -> new Item.Properties().stacksTo(1));

    public static final DeferredBlock<TestBlock> TEST_BLOCK = BLOCKS.registerBlock("test_block", properties -> new TestBlock(properties, TestBlockEntity::new));
    public static final DeferredItem<BlockItem> TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TEST_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("test_block", () -> new BlockEntityType<>(TestBlockEntity::new, Set.of(TEST_BLOCK.get())));

    public static final DeferredBlock<TestBlock> TEST_MACHINE_BLOCK = BLOCKS.registerBlock("machine_block", properties -> new TestBlock(properties, TestMachine.BE::new));
    public static final DeferredItem<BlockItem> TEST_MACHINE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TEST_MACHINE_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestMachine.BE>> TEST_MACHINE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("machine_block", () -> new BlockEntityType<>(TestMachine.BE::new, Set.of(TEST_MACHINE_BLOCK.get())));
    // @formatter:on

    @SubscribeEvent
    public static void modifyCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(TEST_ITEM.toStack());
        }
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Item.ITEM, (stack, access) ->
                new ItemAccessItemHandler(access, DataComponents.CONTAINER, 4), TestRegistration.TEST_ITEM.get());
        event.registerBlockEntity(Capabilities.Item.BLOCK, TestRegistration.TEST_MACHINE_BLOCK_ENTITY.get(), (machine, dir) -> {
            return machine.getResourceInventory();
        });
    }

    public static void register(IEventBus modBus) {
        modBus.register(TestRegistration.class);

        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
    }
}
