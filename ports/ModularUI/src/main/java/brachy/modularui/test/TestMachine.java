package brachy.modularui.test;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IDoubleValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Color;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.SingleChildWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.menu.DropdownWidget;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ApiStatus.Experimental
public class TestMachine {

    private static final IItemHandler EMPTY_INFINITE_ITEM_HANDLER = new EmptyItemHandler() {
        @Override
        public int getSlots() {
            return Integer.MAX_VALUE; // pls don't iterate UwU
        }
    };

    public static class BE extends AbstractBlockEntity implements IUIHolder<PosGuiData> {

        private final ItemStackHandler input = new ItemStackHandler(4);
        private final ItemStackHandler output = new ItemStackHandler(4);
        private final IItemHandlerModifiable inv = new CombinedInvWrapper(input, output);

        private int ticks = 0;
        private boolean running = false, paused = false;
        private int recipeProgress = 0;
        @Nullable private Recipe lastRecipe = null;

        public BE(BlockPos pos, BlockState blockState) {
            super(TestRegistration.TEST_MACHINE_BLOCK_ENTITY.get(), pos, blockState);
        }

        public @NotNull IItemHandler getInventory() {
            return inv;
        }

        @Override
        public ModularScreen createScreen(PosGuiData data, ModularPanel<?> mainPanel) {
            return new ModularScreen(ModularUI.MOD_ID, mainPanel);
        }

        @Override
        public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
            var panel = new ModularPanel<>("machine");
            return panel
                    .coverChildren()
                    .invisible()
                    .child(Flow.col()
                            .coverChildren()
                            .childPadding(2)
                            .child(new ParentWidget<>()
                                    .coverChildren(176, 30)
                                    .padding(7)
                                    .widgetTheme(IThemeApi.PANEL)
                                    .child(Recipes.buildMachineUI(panel, this.input, this.output, new DoubleSyncValue(this::getProgress).allowC2S()))
                                    .child(new ParentWidget<>()
                                            .coverChildren()
                                            .decoration()
                                            .padding(4)
                                            .background(GuiTextures.MC_BACKGROUND.getSubArea(0, 0, 1, 0.5f))
                                            .horizontalCenter()
                                            .anchorTop(1)
                                            .child(Text.str("Machine Name").asWidget())
                                            .name("title"))
                                    .child(new ParentWidget<>()
                                            .coverChildren()
                                            .decoration()
                                            .padding(4)
                                            .paddingLeft(1)
                                            .background(GuiTextures.MC_BACKGROUND.getSubArea(0.5f, 0, 1, 1f))
                                            .bottom(7)
                                            .rightRelAnchor(0, 1f)
                                            .child(new ToggleButton()
                                                    .value(new BooleanSyncValue(() -> this.paused, v -> this.paused = v).allowC2S())
                                                    .overlay(true, GuiTextures.PLAY)
                                                    .overlay(false, GuiTextures.PAUSE))
                                            .name("side_options"))
                            )
                            .child(new SingleChildWidget<>()
                                    .coverChildren()
                                    .padding(7)
                                    .widgetTheme(IThemeApi.PANEL)
                                    .child(SlotGroupWidget.playerInventory(false))));
        }

        @Override
        public void update() {
            if (hasLevel() && !getLevel().isClientSide) {
                if (this.running && this.lastRecipe != null && !this.paused) {
                    if (++this.recipeProgress == this.lastRecipe.ticks) {
                        this.lastRecipe.finishRecipe(this.output);
                        this.recipeProgress = 0;
                        if (this.lastRecipe.startRecipe(this.input, true)) {
                            this.lastRecipe.startRecipe(this.input, false);
                        } else {
                            this.running = false;
                        }
                    }
                } else if (this.ticks % 20 == 0) {
                    Recipe recipe = Recipes.findRecipe(this.input, this.lastRecipe);
                    if (recipe != null) {
                        this.lastRecipe = recipe;
                        this.recipeProgress = 0;
                        this.lastRecipe.startRecipe(this.input, false);
                        this.running = true;
                    }
                }
            }
            this.ticks++;
        }

        public double getProgress() {
            if (this.lastRecipe == null || !this.running) return 0;
            return (double) this.recipeProgress / this.lastRecipe.ticks;
        }
    }

    public static class Recipe {

        private final Identifier resloc;
        private final List<ItemStack> in = new ArrayList<>(), out = new ArrayList<>();
        private int ticks = 80;

        public Recipe(String resloc) {
            this.resloc = ModularUI.id(resloc);
        }

        public Recipe in(ItemStack stack) {
            this.in.add(stack);
            return this;
        }

        public Recipe in(Item item, int count) {
            return in(new ItemStack(item, count));
        }

        public Recipe in(Item item) {
            return in(item, 1);
        }

        public Recipe out(ItemStack stack) {
            this.out.add(stack);
            return this;
        }

        public Recipe out(Item item, int count) {
            return out(new ItemStack(item, count));
        }

        public Recipe out(Item item) {
            return out(item, 1);
        }

        public Recipe ticks(int ticks) {
            this.ticks = ticks;
            return this;
        }

        public boolean startRecipe(IItemHandler handler, boolean simulate) {
            for (ItemStack in : this.in) {
                if (!Recipes.extract(in, handler, simulate)) {
                    return false;
                }
            }
            return true;
        }

        public void finishRecipe(IItemHandler handler) {
            for (ItemStack out : this.out) {
                ItemHandlerHelper.insertItemStacked(handler, out, false);
            }
        }
    }

    public static class Recipes {

        public static final List<Recipe> list = new ArrayList<>();

        static {
            list.add(new Recipe("/stuff_to_nether_star")
                    .in(Items.DIAMOND)
                    .in(Items.EMERALD)
                    .in(Items.GOLD_INGOT, 4)
                    .out(Items.NETHER_STAR));
        }

        public static Recipe findRecipe(IItemHandler input, @Nullable Recipe lastRecipe) {
            if (lastRecipe != null && lastRecipe.startRecipe(input, true)) return lastRecipe;
            for (Recipe recipe : list) {
                if (recipe.startRecipe(input, true)) {
                    return recipe;
                }
            }
            return null;
        }

        private static boolean extract(ItemStack stack, IItemHandler handler, boolean simulate) {
            if (stack.isEmpty()) return true;
            int extracted = 0;
            for (int i = 0, n = handler.getSlots(); i < n; i++) {
                ItemStack c = handler.extractItem(i, stack.getCount() - extracted, true);
                if (ItemStack.isSameItemSameComponents(stack, c)) {
                    if (!simulate) {
                        c = handler.extractItem(i, stack.getCount() - extracted, false);
                    }
                    extracted += c.getCount();
                    if (extracted >= stack.getCount()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public static IWidget buildMachineUI(ModularPanel<?> panel, IItemHandler in, IItemHandler out, IDoubleValue<?> progress) {
            var val = new StringValue("Option 1");
            IPanelHandler panelHandler = IPanelHandler.simple(panel, (parent, player) -> {
                return new ModularPanel<>("test_sub_panel").size(50).overlay(Text.str("Test"));
            }, true);
            return Flow.col()
                    .coverChildren()
                    .childPadding(2)
                    .child(new DropdownWidget<>("test_drop_down", String.class)
                            .width(70)
                            .height(14)
                            .value(val)
                            .option("Option 1")
                            .option("Option 2")
                            .option("Option 3")
                            .optionToWidget((s, b) -> Text.str(s).asWidget().center().color(Color.WHITE.main).shadow(true).padding(2)))
                    .child(new ButtonWidget<>()
                            .size(70, 14)
                            .overlay(Text.str("Sub panel"))
                            .onMousePressed((ctx, button) -> {
                                panelHandler.openPanel();
                                return true;
                            }))
                    .child(Flow.row().name("slots")
                            .coverChildren()
                            .childPadding(8)
                            .child(SlotGroupWidget.rect(2, 2, i -> new ItemSlot()
                                    .slot(new ModularSlot(in, i))
                                    .recipeRole(RecipeSlotRole.INPUT)))
                            .child(new ProgressWidget()
                                    .value(progress)
                                    .size(20)
                                    .texture(GuiTextures.PROGRESS_ARROW, ProgressDrawable.Direction.RIGHT))
                            .child(SlotGroupWidget.rect(2, 2, i -> new ItemSlot()
                                    .slot(new ModularSlot(out, i).canPut(false))
                                    .recipeRole(RecipeSlotRole.OUTPUT))));
        }

        public static IWidget buildViewerUI(Recipe recipe) {
            var panel = new ModularPanel<>("recipe_viewer_recipe")
                    .coverChildren(60, 40)
                    .invisible();
            IWidget recipeUI = buildMachineUI(panel, EMPTY_INFINITE_ITEM_HANDLER, EMPTY_INFINITE_ITEM_HANDLER, DoubleValue.simulateProgress(5000));
            recipeUI.visitTransformAllChildren(w -> {
                if (w instanceof ItemSlot slot) {
                    List<ItemStack> l = slot.getRecipeRole() == RecipeSlotRole.INPUT ? recipe.in : recipe.out;
                    int index = slot.getSlot().getSlotIndex();
                    ItemStack item = index >= l.size() ? ItemStack.EMPTY : l.get(index);
                    return RecipeViewerSlotWidget.create()
                            .recipeSlotRole(slot.getRecipeRole())
                            .value(item)
                            .copyResizerOf(w);
                }
                return w;
            });
            return panel.child(recipeUI);
            //return recipeUI;
        }
    }

    public static class EMI {

        public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(ModularUI.id("machine"), EmiStack.of(TestRegistration.TEST_MACHINE_BLOCK_ITEM.get()));

        public static void register(EmiRegistry registry) {
            registry.addCategory(CATEGORY);
            Recipes.list.stream()
                    .map(r -> new RecipeDisplay(() -> Recipes.buildViewerUI(r), r))
                    .forEach(registry::addRecipe);
        }

        public static class RecipeDisplay extends ModularUIEmiRecipe {

            private final Recipe recipe;
            @Getter private final List<EmiIngredient> inputs = new ArrayList<>();
            @Getter private final List<EmiStack> outputs = new ArrayList<>();

            public RecipeDisplay(Supplier<IWidget> widgetSupplier, Recipe recipe) {
                super(recipe.resloc, widgetSupplier);
                this.recipe = recipe;
                recipe.in.stream().map(EmiStack::of).map(s -> (EmiIngredient) s).forEach(inputs::add);
                recipe.out.stream().map(EmiStack::of).forEach(outputs::add);
                calculateSize();
            }

            @Override
            public EmiRecipeCategory getCategory() {
                return CATEGORY;
            }
        }
    }
}
