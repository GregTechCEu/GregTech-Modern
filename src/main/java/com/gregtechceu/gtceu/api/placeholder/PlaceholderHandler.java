package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.IPanelHandler;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.value.IBoolValue;
import com.gregtechceu.gtceu.api.mui.base.value.IDoubleValue;
import com.gregtechceu.gtceu.api.mui.base.value.IStringValue;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.BorderDrawable;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.StringValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.SortableListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.TextWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.CodeEditorWidget;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnclosedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnexpectedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnknownPlaceholderException;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.RichTooltip;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.*;

import java.util.*;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlaceholderHandler {

    private static final char ARG_SEPARATOR = ' ';
    private static final char PLACEHOLDER_BEGIN = '{';
    private static final char PLACEHOLDER_END = '}';
    private static final char ESCAPE = '\\';
    private static final char LITERAL_ESCAPE = '"';
    private static final char NEWLINE = '\n';
    private static final char ESCAPED_NEWLINE = 'n';

    private static final Map<String, Placeholder> placeholders = new HashMap<>();

    public static final CodeEditorWidget.LanguageDefinition<PlaceholderContext> LANG_DEFINITION = new CodeEditorWidget.LanguageDefinition<>(
            List.of("\\\\.", "\\{", "\\}", " ", "\""),
            TokenFormatter::new);

    public static void addPlaceholder(Placeholder placeholder) {
        if (placeholders.containsKey(placeholder.getName())) {
            if (placeholders.get(placeholder.getName()).getPriority() <= placeholder.getPriority()) {
                placeholders.put(placeholder.getName(), placeholder);
            }
        } else placeholders.put(placeholder.getName(), placeholder);
    }

    public static boolean placeholderExists(MultiLineComponent placeholder) {
        return placeholders.containsKey(placeholder.toString());
    }

    public static MultiLineComponent processPlaceholder(List<MultiLineComponent> placeholder,
                                                        @Nullable PlaceholderContext context) throws PlaceholderException {
        if (!placeholderExists(placeholder.get(0)))
            throw new UnknownPlaceholderException(placeholder.get(0).toString());
        if (context != null && context.level().isClientSide &&
                !placeholders.get(placeholder.get(0).toString()).isPure())
            GTCEu.LOGGER.warn("Placeholder processing is running on client instead of server!");
        return placeholders.get(placeholder.get(0).toString()).apply(context,
                placeholder.subList(1, placeholder.size()));
    }

    public static MultiLineComponent processPlaceholders(String s, @Nullable PlaceholderContext ctx) {
        List<Exception> exceptions = new ArrayList<>();
        boolean escape = false;
        boolean escapeNext = false;
        boolean literalEscape = false;
        int line = 1;
        int symbol = 1;
        Stack<List<MultiLineComponent>> stack = new Stack<>();
        stack.push(GTUtil.list(MultiLineComponent.empty()));
        for (char c : s.toCharArray()) {
            if (escape || (literalEscape && c != LITERAL_ESCAPE)) {
                if (c == ESCAPED_NEWLINE && !literalEscape) {
                    GTUtil.getLast(stack.peek()).appendNewline();
                    line++;
                    symbol = 0;
                } else if (c != NEWLINE) GTUtil.getLast(stack.peek()).append(c);
            } else {
                switch (c) {
                    case ESCAPE -> escapeNext = true;
                    case LITERAL_ESCAPE -> literalEscape = !literalEscape;
                    case NEWLINE -> {
                        GTUtil.getLast(stack.peek()).appendNewline();
                        line++;
                        symbol = 0;
                    }
                    case ARG_SEPARATOR -> {
                        if (stack.size() == 1) GTUtil.getLast(stack.peek()).append(c);
                        else stack.peek().add(MultiLineComponent.empty());
                    }
                    case PLACEHOLDER_BEGIN -> stack.push(GTUtil.list(MultiLineComponent.empty()));
                    case PLACEHOLDER_END -> {
                        List<MultiLineComponent> placeholder = stack.pop();
                        try {
                            if (stack.isEmpty()) throw new UnexpectedBracketException();
                            MultiLineComponent result = processPlaceholder(placeholder, ctx);
                            if (result.isIgnoreSpaces() || stack.size() == 1) {
                                GTUtil.getLast(stack.peek()).append(result);
                            } else {
                                for (int i = 0; i < result.size(); i++) {
                                    MutableComponent component = result.get(i);
                                    component.visit((style, string) -> {
                                        String[] split = string.split(String.valueOf(ARG_SEPARATOR));
                                        for (int j = 0; j < split.length; j++) {
                                            String idk = split[j];
                                            GTUtil.getLast(stack.peek())
                                                    .append(MultiLineComponent.literal(idk).withStyle(style));
                                            if (j == split.length - 1) continue;
                                            if (stack.size() == 1) {
                                                GTUtil.getLast(stack.peek()).append(ARG_SEPARATOR);
                                            } else {
                                                stack.peek().add(MultiLineComponent.empty());
                                            }
                                        }
                                        return Optional.empty();
                                    }, component.getStyle());
                                    if (i != result.size() - 1) GTUtil.getLast(stack.peek()).appendNewline();
                                }
                            }
                        } catch (PlaceholderException e) {
                            e.setLineInfo(line, symbol);
                            exceptions.add(e);
                        } catch (RuntimeException e) {
                            exceptions.add(e);
                        }
                    }
                    default -> GTUtil.getLast(stack.peek()).append(c);
                }
                if (stack.isEmpty()) break;
            }
            escape = escapeNext;
            escapeNext = false;
            symbol++;
        }
        if (stack.size() > 1) {
            PlaceholderException exception = new UnclosedBracketException();
            exception.setLineInfo(line, symbol);
            exceptions.add(exception);
        }
        if (exceptions.isEmpty())
            return stack.peek().stream().reduce(MultiLineComponent.empty(), MultiLineComponent::append);
        MultiLineComponent out = MultiLineComponent.empty();
        exceptions.forEach(exception -> {
            out.append(exception.getMessage());
            out.appendNewline();
        });
        return out.withStyle(ChatFormatting.DARK_RED);
    }

    public static Set<String> getAllPlaceholderNames() {
        return placeholders.keySet();
    }

    public static Widget getPlaceholderHandlerUI(String filter) {
        DraggableScrollableWidgetGroup placeholderReference = new DraggableScrollableWidgetGroup(280, 15, 100, 200);
        Consumer<String> onSearch = (newSearch) -> {
            placeholderReference.clearAllWidgets();
            int y = 2;
            ArrayList<String> placeholders = new ArrayList<>(getAllPlaceholderNames());
            placeholders.removeIf(s -> s == null || !s.contains(newSearch));
            placeholders.sort(String::compareTo);
            for (String placeholder : placeholders) {
                TextTextureWidget placeholderName = new TextTextureWidget(0, y, 80, 15, placeholder);
                placeholderName.getTextTexture().type = TextTexture.TextType.LEFT;
                placeholderName.setHoverTooltips(GTStringUtils
                        .toImmutable(LangHandler.getSingleOrMultiLang("gtceu.placeholder_info." + placeholder)));
                placeholderReference.addWidget(placeholderName);
                y += 15;
            }
        };
        onSearch.accept(filter);
        TextTextureWidget placeholderReferenceLabel = new TextTextureWidget(
                280, 0,
                160, 15,
                GTStringUtils.componentsToString(
                        LangHandler.getMultiLang("gtceu.gui.computer_monitor_cover.placeholder_reference")));
        placeholderReferenceLabel.getTextTexture().type = TextTexture.TextType.LEFT;
        WidgetGroup out = new WidgetGroup();
        out.addWidget(placeholderReferenceLabel);
        out.addWidget(placeholderReference);
        return out;
    }

    public static IWidget createPlaceholderEditor(PanelSyncManager syncManager,
                                                  PlaceholderContext ctx,
                                                  IStringValue<?> code,
                                                  @Nullable IBoolValue<?> pause,
                                                  @Nullable IDoubleValue<?> scaleDouble,
                                                  @Nullable Runnable updateText) {
        IPanelHandler helpPanel = syncManager.panel("placeholder_language_help",
                (syncManager1, panelHandler1) -> createHelpPanel(syncManager1), true);
        // because the args are nullable, intellij complains about everything, even though childIf is used
        // noinspection DataFlowIssue
        return Flow.row()
                .childIf(ctx.itemStackHandler() != null, () -> Flow.column()
                        .coverChildren()
                        .paddingLeft(4)
                        .children(
                                ctx.itemStackHandler().getSlots(),
                                i -> new ItemSlot()
                                        .slot(ctx.itemStackHandler(), i)
                                        .addTooltipLine(
                                                IKey.lang("gtceu.gui.computer_monitor_cover.slot_tooltip", i))))
                .child(Flow.column()
                        .widthRel(.8f)
                        .padding(5)
                        .child(Flow.row()
                                .height(20)
                                .childIf(scaleDouble != null,
                                        new TextWidget<>(IKey.str("gtceu.gui.central_monitor.text_scale")))
                                .childIf(scaleDouble != null, () -> new TextFieldWidget()
                                        .setNumbersDouble(x -> Math.max(x, 0))
                                        .setDefaultNumber(1.0)
                                        .value(SyncHandlers.string(
                                                () -> String.valueOf(scaleDouble.getDoubleValue()),
                                                s -> scaleDouble.setDoubleValue(Double.parseDouble(s))))
                                        .marginLeft(4))
                                .childIf(pause != null, () -> new ToggleButton()
                                        .value(pause)
                                        .background(false, GTGuiTextures.PAUSE)
                                        .background(true, GTGuiTextures.PLAY)
                                        .addTooltip(false, IKey.lang("gtceu.gui.central_monitor.pause"))
                                        .addTooltip(true, IKey.lang("gtceu.gui.central_monitor.resume"))
                                        .margin(4))
                                .childIf(updateText != null, () -> new ButtonWidget<>()
                                        .background(GTGuiTextures.RIGHTLOAD)
                                        .hoverBackground(GTGuiTextures.RIGHTLOAD, new BorderDrawable())
                                        .addTooltipLine(IKey.lang("gtceu.gui.central_monitor.update_once"))
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            if (updateText != null) updateText.run();
                                            return true;
                                        }))
                                .child(new ButtonWidget<>()
                                        .background(GTGuiTextures.HELP)
                                        .hoverBackground(GTGuiTextures.HELP, new BorderDrawable())
                                        .margin(4)
                                        .onMousePressed((mouseX, mouseY, button) -> {
                                            helpPanel.openPanel();
                                            return true;
                                        })))
                        .child(new CodeEditorWidget<>(PlaceholderHandler.LANG_DEFINITION, syncManager)
                                .value(code)
                                .langContext(ctx)
                                .widthRel(.95f)
                                .heightRelOffset(() -> 1, -25)))
                .child(new SortableListWidget<String>()
                        .widthRel(.2f)
                        .paddingBottom(5)
                        .excludeAreaInXei()
                        .children(PlaceholderHandler.getAllPlaceholderNames()
                                .stream()
                                .sorted()
                                .map(SortableListWidget.Item::new)
                                .map(w -> w
                                        .child(new TextWidget<>(w.getWidgetValue())
                                                .sizeRel(1)
                                                .alignment(Alignment.CENTER))
                                        .tooltip(new RichTooltip()
                                                .addDrawableLines(LangHandler
                                                        .getSingleOrMultiLang(
                                                                "gtceu.placeholder_info." + w.getWidgetValue())
                                                        .stream()
                                                        .map(IKey::lang)
                                                        .map(key -> (IDrawable) key)
                                                        .toList())))
                                .toList()));
    }

    public static ModularPanel createHelpPanel(PanelSyncManager syncManager) {
        return new ModularPanel("placeholder_language_help")
                .size(500, 250)
                .child(Flow.column()
                        .padding(5)
                        .child(new TextWidget<>(IKey.lang("gtceu.gui.central_monitor.text_module_help")))
                        .child(new CodeEditorWidget<>(LANG_DEFINITION, syncManager)
                                .padding(5)
                                .widthRel(.95f)
                                .height(100)
                                .value(new StringValue(
                                        """
                                                Energy: {calc {energy}00.0 / {energyCapacity}}%
                                                Bar: {repeat {calc {energy}0.0 / {energyCapacity}} {color green {block}}}
                                                Status: \\
                                                {if {cmp {energy} >= {calc 0.7 * {energyCapacity}}} {color green OK} \\
                                                {if {cmp {energy} >= {calc 0.4 * {energyCapacity}}} {color yellow WARNING} \\
                                                {if {cmp {energy} >= {calc 0.2 * {energyCapacity}}} {color red LOW} \\
                                                {color red CRITICAL}}}}

                                                {eval {if {cmp {energy} < {calc 0.5 * {energyCapacity}}} "{redstone set 15}" "{redstone set 0}"}
                                                """))));
    }

    public static class TokenFormatter implements CodeEditorWidget.ITokenFormatter<PlaceholderContext> {

        private boolean prevOpenBracket = false;
        private boolean inString = false;
        private int unclosedBrackets = 0;
        private final StringBuilder everything = new StringBuilder();
        private final Stack<Integer> pureStarts = new Stack<>();
        private final Stack<Integer> viewStarts = new Stack<>();
        private final Stack<String> openPlaceholders = new Stack<>();
        private int ifDepth = 0;
        private Component endOfLineValue = null;

        @Override
        public Component apply(String s, @Nullable PlaceholderContext ctx) {
            if (s.equals("\0")) {
                if (unclosedBrackets > 0) {
                    onEncounteredError();
                    return Component.literal(" ").withStyle(Style.EMPTY
                            .withUnderlined(true)
                            .withInsertion("")
                            .withColor(0xFF0000)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    unclosedBrackets == 1 ?
                                            Component.translatable("gtceu.placeholder_editor.unclosed_bracket") :
                                            Component.translatable("gtceu.placeholder_editor.unclosed_brackets",
                                                    unclosedBrackets))));
                }
                return Component.empty();
            }
            if (s.matches("\\\\.")) {
                prevOpenBracket = false;
                everything.append(s);
                return Component.literal(s).withStyle(ChatFormatting.GOLD);
            }
            if (inString && !s.equals("\"")) {
                prevOpenBracket = false;
                everything.append(s);
                return Component.literal(s).withStyle(ChatFormatting.DARK_GREEN);
            }
            switch (s) {
                case "\"" -> {
                    inString = !inString;
                    everything.append(s);
                    return Component.literal(s).withStyle(ChatFormatting.DARK_GREEN);
                }
                case "{" -> {
                    prevOpenBracket = true;
                    unclosedBrackets++;
                    everything.append(s);
                    return Component.literal(s);
                }
                case "}" -> {
                    prevOpenBracket = false;
                    unclosedBrackets--;
                    if (unclosedBrackets < 0) {
                        unclosedBrackets = 0;
                        onEncounteredError();
                        return Component.literal(s).withStyle(Style.EMPTY
                                .withColor(0xFF0000)
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("gtceu.placeholder_editor.extra_closing_bracket"))));
                    }
                    everything.append(s);
                    if (!openPlaceholders.empty()) {
                        if (openPlaceholders.peek().equals("if")) ifDepth--;

                        openPlaceholders.pop();
                    }
                    if (!pureStarts.empty()) {
                        String result = processPlaceholders(everything.substring(pureStarts.peek()), ctx).toString();
                        result = result.replaceAll("\\n", "\\\\n");
                        int popped = pureStarts.peek();
                        pureStarts.pop();
                        viewStarts.pop();
                        if (!everything.substring(popped).contains(" ")) return Component.literal(s);
                        if (result.length() > 10) {
                            result = result.substring(0, 10) + "…";
                        }
                        endOfLineValue = null;
                        return Component.literal(s)
                                .append(Component.literal("='%s'".formatted(result))
                                        .withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE)
                                        .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                                HoverEvent.Action.SHOW_TEXT,
                                                Component.translatable("gtceu.placeholder_editor.constant_value")))
                                                .withInsertion("")));
                    }
                    if (!viewStarts.empty() && ctx != null && !ctx.level().isClientSide()) {
                        String result = processPlaceholders(everything.substring(viewStarts.peek()), ctx).toString();
                        result = result.replaceAll("\\n", "\\\\n");
                        if (result.length() > 10) {
                            result = result.substring(0, 10) + "…";
                        }
                        viewStarts.pop();
                        endOfLineValue = Component.literal("='%s'".formatted(result))
                                .withStyle(ChatFormatting.DARK_GRAY)
                                .withStyle(style -> style.withInsertion(""));
                        return Component.literal(s);
                    } else if (!viewStarts.empty()) viewStarts.pop();
                    return Component.literal(s);
                }
            }
            if (prevOpenBracket) {
                prevOpenBracket = false;
                if (getAllPlaceholderNames().contains(s)) {
                    if (placeholders.get(s).isPure()) {
                        pureStarts.push(everything.length() - 1);
                    } else pureStarts.clear();
                    if (placeholders.get(s).isView()) {
                        viewStarts.push(everything.length() - 1);
                    } else viewStarts.clear();
                    everything.append(s);
                    openPlaceholders.push(s);
                    if (s.equals("if")) ifDepth++;
                    else if (ifDepth > 0 && !placeholders.get(s).isView()) {
                        return Component.literal(s)
                                .withStyle(ChatFormatting.BLUE, ChatFormatting.UNDERLINE)
                                .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("gtceu.placeholder_editor.write_in_if"))));
                    }
                    return Component.literal(s).withStyle(ChatFormatting.BLUE);
                } else {
                    onEncounteredError();
                    return Component.literal(s).withStyle(Style.EMPTY
                            .withColor(0xFF0000)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("gtceu.placeholder_editor.no_placeholder",
                                            s.replaceAll("\\n", "\\\\n")))));
                }
            }
            everything.append(s);
            if (s.contains("\n") && endOfLineValue != null) {
                String start = s.substring(0, s.indexOf('\n'));
                String end = s.substring(s.indexOf('\n'));
                Component ret = Component.literal(start).append(endOfLineValue).append(end);
                endOfLineValue = null;
                return ret;
            }
            return Component.literal(s);
        }

        private void onEncounteredError() {
            viewStarts.clear();
            pureStarts.clear();
            openPlaceholders.clear();
            endOfLineValue = null;
            ifDepth = 0;
        }
    }
}
