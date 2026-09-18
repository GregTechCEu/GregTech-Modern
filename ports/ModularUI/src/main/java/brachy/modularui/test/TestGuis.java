package brachy.modularui.test;

import brachy.modularui.utils.MUIRenderTypes;
import brachy.modularui.ModularUI;
import brachy.modularui.animation.Animator;
import brachy.modularui.animation.IAnimator;
import brachy.modularui.animation.Wait;
import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.IThemeApi;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.layout.IViewportStack;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.schema.BlockHighlight;
import brachy.modularui.drawable.FluidDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.ItemDrawable;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.drawable.SchemaRenderer;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.graph.GraphDrawable;
import brachy.modularui.drawable.progress.CircularProgressDrawable;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.factory.ClientGUI;
import brachy.modularui.drawable.schema.ArraySchema;
import brachy.modularui.drawable.schema.ISchema;
import brachy.modularui.screen.CustomModularScreen;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.ColorShade;
import brachy.modularui.utils.Interpolation;
import brachy.modularui.utils.Interpolations;
import brachy.modularui.utils.math.DAM;
import brachy.modularui.value.BoolValue;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.value.IntValue;
import brachy.modularui.value.ObjectValue;
import brachy.modularui.value.StringValue;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.ColorPickerDialog;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.ItemDisplayWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.RichTextWidget;
import brachy.modularui.widgets.SchemaWidget;
import brachy.modularui.widgets.ScrollingTextWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.ToggleButton;
import brachy.modularui.widgets.TransformWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.layout.Grid;
import brachy.modularui.widgets.menu.ContextMenuButton;
import brachy.modularui.widgets.menu.DropdownWidget;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import net.minecraft.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.google.common.base.CaseFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestGuis extends CustomModularScreen {

    public static final IntList LIGHT_COLORS = ColorShade.getAll().stream()
            .filter(cs -> !cs.name.contains("accent"))
            .filter(cs -> cs.brighterShadeCount() > 1)
            .mapToInt(cs -> cs.brighter(1))
            .filter(c -> Color.getHSLSaturation(c) > 0.4f)
            .collect(IntArrayList::new, IntList::add, IntList::addAll);

    public static final IntList DARK_COLORS = ColorShade.getAll().stream()
            .filter(cs -> !cs.name.contains("accent"))
            .filter(cs -> cs.darkerShadeCount() > 1)
            .mapToInt(cs -> cs.darker(1))
            .filter(c -> Color.getHSLSaturation(c) > 0.4f)
            .collect(IntArrayList::new, IntList::add, IntList::addAll);

    public static boolean withCode = false;

    public TestGuis() {
        super(ModularUI.MOD_ID);
    }

    /**
     * This method finds all 'build___UI' methods in this class via reflection and adds a button that opens that UI to a list widget.
     * This makes it very convenient to add and test test-screens without having to swap out the screen that the diamond item opens.
     */
    @Override
    public @NotNull ModularPanel<?> buildUI(ModularGuiContext context) {
        // collect all test from all build methods in this class via reflection
        List<Method> uiMethods = new ArrayList<>();
        for (Method method : TestGuis.class.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    ModularPanel.class.isAssignableFrom(method.getReturnType()) &&
                    method.getParameterCount() == 0) {
                uiMethods.add(method);
            }
        }
        uiMethods.sort(Comparator.comparing(Method::getName));

        return new ModularPanel<>("client_tests").height(200).width(170)
                .padding(7)
                .child(Flow.column()
                        .child(Text.str("Client Test UIs").asWidget().margin(1))
                        .child(new ListWidget<>().widthRel(1f).expanded()
                                .children(uiMethods.size(), i -> {
                                    Method m = uiMethods.get(i);
                                    String name = m.getName();
                                    if (name.startsWith("build")) name = name.substring(5);
                                    if (name.endsWith("UI")) name = name.substring(0, name.length() - 2);
                                    String codeTextureName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
                                    name = name.replaceAll("([a-z])([A-Z])", "$1 $2");
                                    return button(name)
                                            .onMousePressed((context1, button) -> {
                                                try {
                                                    ModularPanel<?> panel = (ModularPanel) m.invoke(null);
                                                    if (TestGuis.withCode) {
                                                        // WIP: this is meant to put an image of the code next to ui for showcase purpose
                                                        panel.child(UITexture.builder()
                                                                .location("gui/code/" + codeTextureName)
                                                                .build()
                                                                .asWidget()
                                                                .leftRel(1f)
                                                                .heightRel(1f));
                                                    }
                                                    ClientGUI.open(new ModularScreen(ModularUI.MOD_ID, panel).openParentOnClose(true));
                                                } catch (IllegalAccessException | InvocationTargetException e) {
                                                    ModularUI.LOGGER.throwing(e);
                                                }
                                                return true;
                                            });
                                })
                                /*.child(button("OpenGL test")
                                        .onMousePressed(button -> {
                                            ClientGUI.open(new GLTestGui().openParentOnClose(true));
                                            return true;
                                        }))
                                .child(button("Sortable List")
                                        .onMousePressed(button -> {
                                            ClientGUI.open(new TestGui().openParentOnClose(true));
                                            return true;
                                        }))*/
                                .child(button("Test self")
                                        .onMousePressed((context1, button) -> {
                                            ClientGUI.open(this);
                                            return true;
                                        }))));
    }

    private static ButtonWidget<?> button(String text) {
        return new ButtonWidget<>()
                .height(16).widthRel(1f).margin(0, 1)
                .overlay(Text.str(text));
    }

    public static @NotNull ModularPanel<?> buildToggleGridListUI() {
        boolean[][] states = new boolean[4][16];
        // we need to do this to attach the theme since we have no screen, yet
        // normally you have either UISettings or a ModularScreen at build to set it directly
        return new ModularPanel<>("grid_list")
                .themeOverride(TestHandler.TEST_THEME)
                .height(100)
                .coverChildrenWidth()
                .padding(7)
                .child(new ListWidget<>()
                        .coverChildrenWidth()
                        .heightRel(1f)
                        .children(4, i -> new Grid()
                                .left(0)
                                .coverChildren()
                                .gridOfWidthHeight(4, 4, (x, y, j) -> {
                                    return new ToggleButton()
                                            .overlay(GuiTextures.BOOKMARK)
                                            .value(new BoolValue.Dynamic(() -> states[i][j], val -> states[i][j] = val))
                                            .size(10)
                                            .margin(1)
                                            .name("G:" + i + ",W:" + j);
                                })));

    }

    public static @NotNull ModularPanel<?> buildPendulumAnimationUI() {
        IWidget widget = GuiTextures.MUI_LOGO.asWidget().size(20).pos(65, 65);
        Animator animator = new Animator()
                .bounds(0, 1)
                .curve(Interpolation.SINE_INOUT)
                .reverseOnFinish(true)
                .repeatsOnFinish(-1)
                .duration(1200);

        animator.reset(true);
        animator.animate(true);
        return ModularPanel.defaultPanel("main").size(150)
                .child(new TransformWidget(widget)
                        .transform(stack -> {
                            double angle = Math.PI;
                            float x = (float) (55 * Math.cos(animator.getValue() * angle));
                            float y = (float) (55 * Math.sin(animator.getValue() * angle));
                            stack.translate(x, y);
                        }));
    }

    public static @NotNull ModularPanel<?> buildPostTheLogAnimationUI() {
        Animator post = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(-35, 0);
        Animator the = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(-20, 0);
        Animator extraordinary = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(53, 0);
        Animator log = new Animator().curve(Interpolation.SINE_IN).duration(300).bounds(20, 0);
        Animator logGrow = new Animator().curve(Interpolation.LINEAR).duration(2500).bounds(0f, 1f);
        IAnimator animator = new Wait(300)
                .followedBy(post)
                .followedBy(the)
                .followedBy(extraordinary)
                .followedBy(log)
                .followedBy(logGrow);
        animator.animate();
        Random rnd = new Random();
        return new ModularPanel<>("main")
                .coverChildren()
                .child(Flow.col()
                        .margin(12)
                        .coverChildren()
                        .child(Flow.row()
                                .coverChildren()
                                .child(Text.str("Post ").asWidget()
                                        .transform((widget, stack) -> stack.translate(post.getValue(), 0)))
                                .child(Text.str("the ").asWidget()
                                        .transform((widget, stack) -> stack.translate(0, the.getValue())))
                                .child(Text.str("fucking ").style(Text.OBFUSCATED).asWidget()
                                        .transform((widget, stack) -> stack.translate(extraordinary.getValue(), 0))))
                        .child(Text.str("LOOOOGG!!!!").asWidget()
                                .paddingTop(4)
                                .transform((widget, stack) -> {
                                    float logVal = log.getValue();
                                    float logGrowVal = logGrow.getValue();
                                    stack.translate(rnd.nextInt(5) * logGrowVal, logVal + rnd.nextInt(5) * logGrowVal);
                                    int x0 = widget.getArea().width / 2, y0 = widget.getArea().height;
                                    float scale = Interpolations.lerp(1f, 3f, logGrowVal);
                                    stack.translate(x0, y0);
                                    stack.scale(scale, scale);
                                    stack.translate(-x0, -y0);
                                    widget.color(Color.lerp(0xFF040404, Color.RED.main, Math.min(1f, 1.2f * logGrowVal)));
                                })));
    }

    /*public static @NotNull ModularPanel<?> buildSpriteAndEntityUI() {
        TextureAtlasSprite sprite = SpriteHelper.getSpriteOfBlockState(GameObjectHelper.getBlockState("minecraft", "command_block"), EnumFacing.UP);
        // SpriteHelper.getSpriteOfItem(new ItemStack(Items.DIAMOND));
        Entity entity = FakeEntity.create(EntityDragon.class);
        float period = 3000f;
        return ModularPanel.defaultPanel("main")
                .size(150)
                .overlay(new Rectangle()
                        .color(Color.GREEN.main)
                        .hollow(2)
                        .asIcon().margin(5))
                .child(new TextWidget<>(IKey.str("Test String")).scale(0.6f).horizontalCenter().top(7))
                .child(new DraggableWidget<>()
                        .background(new SpriteDrawable(sprite))
                        .size(20)
                        .alignX(0.5f)
                        .top(20)
                        .tooltipBuilder(tooltip -> {
                            tooltip.addLine(
                                    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.  "
                                            + "Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.  "
                                            + "Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.  "
                                            + "Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Lorem");
                            tooltip.addLine("Longer Line 2");
                            tooltip.addLine("Line 3");
                            tooltip.alignment(Alignment.Center);
                            tooltip.scale(0.5f);
                            tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
                        }))
                .child(new IDrawable() {
                    @Override
                    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
                        GuiDraw.drawEntity(context.getGraphics(), entity, 0, 0, width, height, context.getCurrentDrawingZ(), (graphics, e) -> {
                            // TODO the drawable doesnt seem to update the rotation
                            float scale = 0.9f;
                            graphics.pose().scale(scale, scale, scale);
                            graphics.pose().translate(0, 7, 0);
                            //graphics.pose().rotate(35, 1, 0, 0);
                            //graphics.pose().rotate(360 * (Util.getMillis() % period) / period, 0, 1, 0);
                        }, null);
                    }
                }.asWidget().alignX(0.5f).bottom(10).size(100, 75));
    }*/

    public static @NotNull ModularPanel<?> buildRichTextUI() {
        IntValue integer = new IntValue(0);
        return new ModularPanel<>("main")
                .size(176, 190)
                .child(new RichTextWidget()
                        .sizeRel(1f).margin(7)
                        .autoUpdate(true)
                        .textBuilder(text -> text.add("Hello ")
                                .addDrawable(new ItemDrawable(new ItemStack(Blocks.GRASS_BLOCK))
                                        .asIcon()
                                        .asHoverable()
                                        .tooltip(richTooltip -> richTooltip.addFromItem(new ItemStack(Blocks.GRASS_BLOCK))
                                                .add(Text.GRAY + "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.")))
                                .add(", nice to ")
                                .addDrawable(new ItemDrawable(new ItemStack(Items.PORKCHOP))
                                        .asIcon()
                                        .asInteractable()
                                        .onMousePressed((context, button) -> {
                                            ModularUI.LOGGER.info("Pressed Pork");
                                            return true;
                                        }))
                                .add(" you. ")
                                .add(Text.str("This is a long ").style(Text.GREEN))
                                .addDrawable(Text.str("string").style(Text.DARK_PURPLE)
                                        .asTextIcon()
                                        .asHoverable()
                                        .addTooltipLine("Text Tooltip"))
                                .add(" of characters" + Text.RESET)
                                .add(" and not numbers as some might think...")
                                .newLine()
                                .newLine()
                                .add(Text.comp(Text.comp(
                                                Text.str("Underline all: "),
                                                Text.comp(
                                                                Text.str("Green Text, "),
                                                                Text.str("this is red").style(Text.RED),
                                                                Text.str(" and this should be green again"))
                                                        .style(Text.GREEN),
                                                Text.str(". Still underlined, "))
                                        .style(Text.UNDERLINE), Text.str("but not anymore.")))
                                .newLine()
                                .addDrawable(Text.str("Green, %s, %s and green again",
                                        Text.str("red").style(Text.RED),
                                        Text.str("underline").style(null, Text.UNDERLINE)
                                ).style(Text.GREEN))
                                .newLine()
                                .add(Text.RESET + "" + Text.UNDERLINE + "Underlined" + Text.RESET)
                                .newLine()
                                .add("A long line which should wrap around. You just need §5I§dm§4a§cg§ei§an§ba§3t§7i§1o§5n§7§r to read this.")
                                .newLine()
                                .addLine(Text.comp(Text.str("Dynamic ").style(Text.GOLD), Text.dynamic(() -> {
                                    int i = integer.getIntValue() + 1;
                                    integer.setIntValue(i);
                                    return Text.str("key [%s]", Text.str("arg")
                                                    .style(Text.UNDERLINE, Text.BLACK))
                                            .style(i % 30 > 5 ? Text.RED : Text.DARK_BLUE);
                                }).fallbackStyle(Text.BOLD), Text.str(" Test")))
                                .textShadow(false)
                        ));
    }

    public static @NotNull ModularPanel<?> buildWorldSchemaUI() {
        /*TrackedDummyWorld world = new TrackedDummyWorld();
        world.addBlock(new BlockPos(0, 0, 0), new BlockInfo(Blocks.DIAMOND_BLOCK.getDefaultState()));
        world.addBlock(new BlockPos(0, 1, 0), new BlockInfo(Blocks.BEDROCK.getDefaultState()));
        world.addBlock(new BlockPos(1, 0, 1), new BlockInfo(Blocks.GOLD_BLOCK.getDefaultState()));*/
/*        return ModularPanel.defaultPanel("main")
                .size(150)
                .overlay(new SchemaRenderer(BoxSchema.of(Minecraft.getMinecraft().world, new BlockPos(Platform.getClientPlayer()), 5))
                        .cameraFunc((camera, schema) -> {
                            double pitch = Math.PI / 4;
                            double T = 4000D;
                            double yaw = Minecraft.getSystemTime() % T / T * Math.PI * 2;
                            camera.setLookAt(new BlockPos(Platform.getClientPlayer()), 20, yaw, pitch);
                        })
                        .isometric(true)
                        .asIcon().size(140));*/

        /*MapSchema world = new MapSchema.Builder()
                .add(new BlockPos(0, 0, 0), Blocks.DIAMOND_BLOCK.getDefaultState())
                .add(new BlockPos(0, 1, 0), Blocks.BEDROCK.getDefaultState())
                .add(new BlockPos(0, 2, 0), Blocks.WOOL.getDefaultState())
                .add(new BlockPos(1, 0, 1), Blocks.GOLD_BLOCK.getDefaultState())
                .add(new BlockPos(0, 3, 0), Blocks.BEACON.getDefaultState())
                .build();*/

        ISchema schema = ArraySchema.builder()
                .layer("D   D", "     ", "     ", "     ")
                .layer(" DDD ", " E E ", "     ", "     ")
                .layer(" DDD ", "  E  ", "  G  ", "  B  ")
                .layer(" DDD ", " E E ", "     ", "     ")
                .layer("D   D", "     ", "     ", "     ")
                .where('D', Blocks.GOLD_BLOCK)
                .where('E', Blocks.EMERALD_BLOCK)
                .where('G', Blocks.DIAMOND_BLOCK)
                .where('B', Blocks.BEACON)
                .build();
        var renderer = schema.createRenderer()
                .rayTracing(true)
                .highlightRenderer(new BlockHighlight(Color.withAlpha(Color.RED.main, 0.5f))
                        .allSides(true)
                        .thickness(0.1f));

        var panel = ModularPanel.defaultPanel("main").size(170);
        panel.child(new SchemaWidget(renderer)
                        .full())
                .child(new SchemaWidget.LayerButton(renderer, 0, 3)
                        .bottom(1)
                        .left(1)
                        .size(16));
        return panel;
    }

    public static ModularPanel<?> buildCollapseDisabledChildrenUI() {
        Random rnd = new Random();
        return ModularPanel.defaultPanel("list", 100, 150)
                .padding(7)
                .child(new ListWidget<>()
                        .sizeRel(1f)
                        .collapseDisabledChildren()
                        .children(12, i -> new Widget<>()
                                .widthRel(1f)
                                .height(16)
                                .widgetTheme(IThemeApi.BUTTON)
                                .overlay(Text.str(String.valueOf(i + 1)))
                                .onUpdateListener(w -> {
                                    if (rnd.nextDouble() < 0.05) {
                                        w.setEnabled(!w.isEnabled());
                                    }
                                })));
    }

    public static @NotNull ModularPanel<?> buildSearchTest() {
        StringValue searchValue = new StringValue("");
        return ModularPanel.defaultPanel("search", 130, 200)
                .child(Flow.column()
                        .padding(5)
                        .child(new TextFieldWidget()
                                .value(searchValue)
                                .height(16)
                                .widthRel(1f)
                                .autoUpdateOnChange(true))
                        .child(new ListWidget<>()
                                .collapseDisabledChildren()
                                .expanded()
                                .widthRel(1f)
                                .children(BuiltInRegistries.ITEM, item -> {
                                    ItemStack stack = new ItemStack(item);
                                    String text = stack.getHoverName().getString();
                                    return Flow.row()
                                            .height(20).widthRel(1f)
                                            .padding(2)
                                            .marginBottom(-1)
                                            .widgetTheme(IThemeApi.BUTTON)
                                            .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                            .setEnabledIf(w -> text.toLowerCase().contains(searchValue.getStringValue()))
                                            .child(new ItemDrawable(stack).asWidget())
                                            .child(new ScrollingTextWidget(Text.str(text))
                                                    .widgetTheme(IThemeApi.BUTTON)
                                                    .textAlign(Alignment.CENTER)
                                                    .expanded()
                                                    .height(16)
                                                    .invisible());
                                })));
    }

    public static @NotNull ModularPanel<?> buildColorTheoryUI() {
        List<Pair<Integer, Float>> colors = new ArrayList<>();
        for (ColorShade shade : ColorShade.getAll()) {
            for (int c : shade) {
                colors.add(Pair.of(c, Color.getLuminance(c)));
            }
        }
        colors.sort((a, b) -> Float.compare(a.getRight(), b.getRight()));

        IDrawable luminanceSortedColors = (context1, x, y, width, height, widgetTheme) -> {
            float w = (float) width / colors.size();
            float x0 = x;
            for (Pair<Integer, Float> c : colors) {
                GuiDraw.drawRect(context1.getGraphics(), x0, y, w, height, c.getLeft());
                x0 += w;
            }
        };

        Rectangle color1 = new Rectangle().color(Color.BLACK.main);
        Rectangle color2 = new Rectangle().color(Color.WHITE.main);

        IDrawable gradient = (context1, x, y, width, height, widgetTheme) -> GuiDraw.drawHorizontalGradientRect(context1.getGraphics(), x, y, width, height, color1.getColor(), color2.getColor());
        IDrawable correctedGradient = (context1, x, y, width, height, widgetTheme) -> {
            int points = 500;
            Matrix4f pose = context1.graphicsPose().last().pose();
            VertexConsumer buffer = context1.getGraphics().bufferSource().getBuffer(MUIRenderTypes.guiTriangleStrip());

            float x0 = x;
            float w = (float) width / points;
            for (int i = 0; i < points; i++) {
                int color = Color.lerp(color1.getColor(), color2.getColor(), (float) i / points);
                int r = Color.getRed(color), g = Color.getGreen(color), b = Color.getBlue(color), a = 0xFF;
                buffer.addVertex(pose, x0, y, 0).setColor(r, g, b, a);
                buffer.addVertex(pose, x0, y + height, 0).setColor(r, g, b, a);
                x0 += w;
            }
        };

        ModularPanel<?> panel = new ModularPanel<>("colors").width(300).coverChildrenHeight().padding(7);

        IPanelHandler colorPicker1 = IPanelHandler.simple(panel, (mainPanel, player) -> new ColorPickerDialog("color_picker1", color1.getColor(), true)
                .resultConsumer(color1::color)
                .draggable(true)
                .relative(panel)
                .top(0)
                .rightRel(1f), true);
        IPanelHandler colorPicker2 = IPanelHandler.simple(panel, (mainPanel, player) -> new ColorPickerDialog("color_picker2", color2.getColor(), true)
                .resultConsumer(color2::color)
                .draggable(true)
                .relative(panel)
                .top(0)
                .leftRel(1f), true);

        return panel
                .child(Flow.column()
                        .coverChildrenHeight()
                        .child(Text.str("Colors sorted by luminance").asWidget().margin(1))
                        .child(luminanceSortedColors.asWidget().widthRel(1f).height(10))
                        .child(Text.str("Blending color").asWidget().margin(1).marginTop(2))
                        .child(Flow.row()
                                .coverChildrenHeight()
                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                .child(new ButtonWidget<>()
                                        .name("color picker button 1")
                                        .background(color1)
                                        .disableHoverBackground()
                                        .onMousePressed((context, mouseButton) -> {
                                            colorPicker1.openPanel();
                                            return true;
                                        }))
                                .child(Text.str("<--  Select colors  -->").asWidget())
                                .child(new ButtonWidget<>()
                                        .name("color picker button 2")
                                        .background(color2)
                                        .disableHoverBackground()
                                        .onMousePressed((context, mouseButton) -> {
                                            colorPicker2.openPanel();
                                            return true;
                                        })))
                        .child(Text.str("OpenGL color gradient").asWidget().margin(1))
                        .child(gradient.asWidget().widthRel(1f).height(10))
                        .child(Text.str("Gamma corrected gradient").asWidget().margin(1))
                        .child(correctedGradient.asWidget().widthRel(1f).height(10)));
    }

    public static @NotNull ModularPanel<?> buildViewportTransformUI() {
        return new TestPanel("viewport_transform")
                .child(new Widget<>()
                        .center()
                        .size(50, 50)
                        .background(GuiTextures.MC_BUTTON)
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED));
    }

    public static ModularPanel<?> buildContextMenu() {
        List<String> options1 = IntStream.range(0, 5).mapToObj(i -> "Option " + (i + 1)).collect(Collectors.toList());
        List<String> options2 = IntStream.range(0, 5).mapToObj(i -> "Sub Option " + (i + 1)).collect(Collectors.toList());
        ObjectValue<ItemStack> itemValue = new ObjectValue<>(ItemStack.class, new ItemStack(Items.ACACIA_DOOR));
        return new ModularPanel<>("context_menu_test")
                .size(150)
                .child(new ContextMenuButton<>("menu")
                        .top(7)
                        .width(100)
                        .horizontalCenter()
                        .height(16)
                        .overlay(Text.str("Menu"))
                        .menuList(l -> l
                                .maxSize(80)
                                .children(options1, s -> Text.str(s).asWidget())
                                .child(new ContextMenuButton<>("sub_menu")
                                        .widthRel(1f)
                                        .height(12)
                                        .overlay(Text.str("Sub Menu"))
                                        .openRightDown()
                                        .menuList(l1 -> l1
                                                //.width(90)
                                                .maxSize(80)
                                                .children(options2, s -> Text.str(s).asWidget())))))
                .child(new DropdownWidget<>("test_dropdown", ItemStack.class)
                        .top(45)
                        .width(100)
                        .horizontalCenter()
                        .value(itemValue)
                        .option(new ItemStack(Items.ACACIA_DOOR))
                        .option(new ItemStack(Items.GOLD_INGOT))
                        .option(new ItemStack(Items.APPLE))
                        .option(new ItemStack(Items.FURNACE_MINECART))
                        .option(new ItemStack(Items.IRON_SHOVEL))
                        .option(new ItemStack(Items.STICK))
                        .option(new ItemStack(Items.NETHER_STAR))
                        .optionToWidget((item, forSelected) -> Flow.row()
                                .coverChildrenHeight()
                                .padding(4, 1)
                                .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                                .child(new ItemDrawable(item).asWidget())
                                .child(Text.str(item.getDisplayName().getString()).asWidget()
                                        .widgetTheme(IThemeApi.BUTTON)
                                        .invisible()))
                );
    }

    public static @NotNull ModularPanel<?> buildGraphUI() {
        double[] x = DAM.linspace(-25, 25, 200);
        // sin(x) / x
        double[] y1 = DAM.div(DAM.sin(x, null), x, null);
        return new ModularPanel<>("graph")
                .size(200, 160)
                .padding(5)
                .overlay(new GraphDrawable()
                        .graphAspectRatio(16 / 9f)
                        .plot(x, y1));
    }

    public static @NotNull ModularPanel<?> buildAspectRatioUI() {
        return new ModularPanel<>("aspect_ratio")
                .coverChildren()
                .padding(10)
                .child(Flow.row()
                        .childPadding(10)
                        .coverChildren()
                        .child(new Rectangle().color(Color.BLUE_ACCENT.main)
                                .asIcon().aspectRatio(4f / 3)
                                .asWidget().size(80)
                                .overlay(Text.str("4:3 Free")))
                        .child(new Rectangle().color(Color.RED_ACCENT.main)
                                .asIcon().aspectRatio(4f / 3).width(70)
                                .asWidget().size(80)
                                .overlay(Text.str("4:3 | width = 70")))
                        .child(new Rectangle().color(Color.LIGHT_GREEN.main)
                                .asIcon().aspectRatio(4f / 3).height(45).alignment(Alignment.BottomRight)
                                .asWidget().size(80)
                                .overlay(Text.str("4:3 | height = 45\nBottom Right"))))
                .overlay();
    }

    public static @NotNull ModularPanel<?> buildWrappedFlowUI() {
        IntList colors = new IntArrayList(LIGHT_COLORS);
        Random rnd = new Random();
        int minRectSize = 10;
        int maxRectSize = 40;
        return new ModularPanel<>("wrapped_flow")
                .size(150)
                .padding(4)
                .child(Flow.row()
                        .name("wrap")
                        .wrap()
                        .widthRel(1f)
                        .coverChildrenHeight()
                        .padding(2)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .mainAxisAlignment(Alignment.MainAxis.CENTER)
                        .childPadding(2)
                        .crossAxisChildPadding(2)
                        .background(new Rectangle().color(DARK_COLORS.getInt(rnd.nextInt(DARK_COLORS.size()))))
                        .children(5, i -> {
                            IWidget widget = rndRect(colors, rnd).asWidget()
                                    .width(rnd.nextInt(maxRectSize - minRectSize) + minRectSize)
                                    .height(rnd.nextInt(maxRectSize - minRectSize) + minRectSize)
                                    .name("rect_" + (i + 1));
                            if (i % 2 == 1) widget.resizer().expanded();
                            return widget;
                        }));
    }

    private static Rectangle rndRect(IntList colors, Random random) {
        int i = random.nextInt(colors.size());
        int c = colors.removeInt(i);
        if (colors.isEmpty()) colors.addAll(LIGHT_COLORS);
        return new Rectangle().color(c);
    }

    public static @NotNull ModularPanel<?> buildMachineLikeUI() {
        return new ModularPanel<>("machine_like")
                .coverChildren()
                .padding(7)
                .child(Flow.col()
                        .coverChildren()
                        .childPadding(8)
                        .child(Flow.row().name("machine_inventory")
                                .coverChildren()
                                .childPadding(8)
                                .child(SlotGroupWidget.builder()
                                        .matrix("II", "II")
                                        .key('I', i -> new ItemDisplayWidget().item(TestHandler.getRandomItem()))
                                        .build()
                                        .coverChildren())
                                .child(new ProgressWidget()
                                        .size(20)
                                        .texture(GuiTextures.PROGRESS_ARROW, ProgressDrawable.Direction.RIGHT)
                                        .value(new DoubleValue.Dynamic(() -> Util.getMillis() % 5000 / 5000.0, null)))
                                .child(SlotGroupWidget.builder()
                                        .matrix("II", "II")
                                        .key('I', i -> new ItemDisplayWidget().item(TestHandler.getRandomItem()))
                                        .build()
                                        .coverChildren()))
                        .child(SlotGroupWidget.builder()
                                .matrix("IIIIII", "IIIIII")
                                .key('I', i -> new ItemDisplayWidget().item(ItemStack.EMPTY))
                                .build()
                                .coverChildren()
                                .name("play_inventory_mimic")))
                .child(new ParentWidget<>()
                        .coverChildren()
                        .decoration()
                        .padding(3)
                        .background(GuiTextures.MC_BACKGROUND.getSubArea(0, 0, 1, 0.5f))
                        .horizontalCenter()
                        .anchorTop(1)
                        .child(Text.str("Machine Name").asWidget())
                        .name("title"))
                .child(new ParentWidget<>()
                        .coverChildren()
                        .decoration()
                        .padding(3)
                        .paddingLeft(1)
                        .background(GuiTextures.MC_BACKGROUND.getSubArea(0.5f, 0, 1, 1f))
                        .bottom(7)
                        .rightRelAnchor(0, 1f)
                        .child(new CycleButtonWidget()
                                .value(new IntValue(0))
                                .stateCount(3)
                                .stateOverlay(GuiTextures.CYCLE_BUTTON_DEMO))
                        .name("side_options"));
    }

    public static @NotNull ModularPanel<?> buildProgressUI() {
        Random rnd = new Random();
        return new ModularPanel<>("progress")
                .coverChildren()
                .padding(5)
                .child(Flow.col()
                        .coverChildren()
                        .childPadding(2)
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(2)
                                .child(new ProgressDrawable()
                                        .left()
                                        .progressDuration(3, TimeUnit.SECONDS)
                                        .emptyTexture(new Rectangle().color(0xFFBBBBBB))
                                        .filledTexture(rndRect(DARK_COLORS, rnd))
                                        .asWidget()
                                        .addTooltipLine("Right to Left")
                                        .addTooltipLine("No step size (smooth)"))
                                .child(new ProgressDrawable()
                                        .right()
                                        .progressDuration(3, TimeUnit.SECONDS)
                                        .emptyTexture(new Rectangle().color(0xFFBBBBBB))
                                        .filledTexture(rndRect(DARK_COLORS, rnd))
                                        .progressStepSize(0.2f)
                                        .asWidget()
                                        .addTooltipLine("Left to Right")
                                        .addTooltipLine("0.2 step size"))
                                .child(new ProgressDrawable()
                                        .up()
                                        .progressDuration(3, TimeUnit.SECONDS)
                                        .emptyTexture(new Rectangle().color(0xFFBBBBBB))
                                        .filledTexture(Text.str("Text"))
                                        .progressPixelStepSize(1)
                                        .asWidget()
                                        .width(24)
                                        .height(12)
                                        .addTooltipLine("Down to Up")
                                        .addTooltipLine("1 pixel step size"))
                                .child(new ProgressDrawable()
                                        .down()
                                        .progressDuration(3, TimeUnit.SECONDS)
                                        .emptyTexture(new Rectangle().color(0xFFBBBBBB))
                                        .filledTexture(rndRect(DARK_COLORS, rnd))
                                        .progressPixelStepSize(4)
                                        .asWidget()
                                        .addTooltipLine("Up to Down")
                                        .addTooltipLine("4 pixel step size")))
                        .child(Flow.row()
                                .coverChildren()
                                .childPadding(2)
                                .child(new CircularProgressDrawable()
                                        .progressDuration(3, TimeUnit.SECONDS)
                                        .filledTexture(new ItemDrawable(Items.DIAMOND))
                                        .clockwise()
                                        .asWidget())
                                .child(new CircularProgressDrawable()
                                        .progressDuration(3, TimeUnit.SECONDS)
                                        .filledTexture(new FluidDrawable(new FluidStack(Fluids.WATER, 1)))
                                        .counterClockwise()
                                        .asWidget()))
                        .coverChildren()
                );
    }

    private static class TestPanel extends ModularPanel<TestPanel> {

        public TestPanel(String name) {
            super(name);
            //background(GuiTextures.BACKGROUND);
            size(100, 100);
        }

        @Override
        public void transform(IViewportStack stack) {
            super.transform(stack);
            stack.translate(50, 50);
            // rotate with constant speed CW
            float angle = (float) ((Util.getMillis() % 4000) / 4000f * 2 * Math.PI);
            stack.rotateZ(angle);

            // scale from 0.5 to 1 and back with curve
            float scale;
            long t = Util.getMillis() % 4000;
            if (t <= 2000) {
                scale = Interpolation.BACK_INOUT.interpolate(0.5f, 1f, t / 2000f);
            } else {
                scale = Interpolation.BACK_INOUT.interpolate(0.5f, 1f, (2000 - t + 2000) / 2000f);
            }
            stack.scale(scale, scale);
            stack.translate(-50, -50);
        }
    }
}
