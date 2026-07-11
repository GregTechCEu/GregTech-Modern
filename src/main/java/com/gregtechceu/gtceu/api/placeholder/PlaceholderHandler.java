package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnclosedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnexpectedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnknownPlaceholderException;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.monitor.IMonitorRenderer;
import com.gregtechceu.gtceu.common.mui.widgets.textfield.CodeEditorWidget;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.value.IBoolValue;
import brachy.modularui.api.value.IStringValue;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.*;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PlaceholderHandler {

    private static final char ARG_SEPARATOR = ' ';
    private static final char PLACEHOLDER_BEGIN = '{';
    private static final char PLACEHOLDER_END = '}';
    private static final char ESCAPE = '\\';
    private static final char LITERAL_ESCAPE = '"';
    private static final char SINGLE_ESCAPE = '\'';
    private static final char NEWLINE = '\n';
    private static final char ESCAPED_NEWLINE = 'n';

    public static final CodeEditorWidget.LanguageDefinition<PlaceholderContext> LANG_DEFINITION = new CodeEditorWidget.LanguageDefinition<>(
            List.of("\\\\.", "\\{", "\\}", " ", "\"", "\\['", "'\\]"),
            TokenFormatter::new);

    @OnlyIn(Dist.CLIENT)
    private static final class RendererHolder {

        public static final Map<String, IPlaceholderRenderer> renderers = new HashMap<>();
    }

    public static void addPlaceholder(Placeholder placeholder) {
        GTRegistries.register(GTRegistries.PLACEHOLDERS, placeholder.getId(), placeholder);
    }

    @OnlyIn(Dist.CLIENT)
    public static void addRenderer(String id, IPlaceholderRenderer renderer) {
        RendererHolder.renderers.put(id, renderer);
    }

    @OnlyIn(Dist.CLIENT)
    public static @Nullable IMonitorRenderer getRenderer(String id, CompoundTag renderData) {
        if (!RendererHolder.renderers.containsKey(id)) {
            GTCEu.LOGGER.warn("Attempt to access a placeholder renderer that doesn't exist ({})", id);
            return null;
        }
        IPlaceholderRenderer renderer = RendererHolder.renderers.get(id);
        CompoundTag tag = renderData.copy();
        return (machine, group,
                partialTick, poseStack, buffer,
                packedLight, packedOverlay) -> renderer.render(
                        machine, group,
                        partialTick, poseStack, buffer,
                        packedLight, packedOverlay, tag);
    }

    public static MultiLineComponent processPlaceholder(List<MultiLineComponent> placeholder,
                                                        @Nullable PlaceholderContext context,
                                                        Object2IntOpenHashMap<String> indices) throws PlaceholderException {
        if (!GTRegistries.PLACEHOLDERS.containsKey(GTCEu.id(placeholder.getFirst().toString())))
            throw new UnknownPlaceholderException(placeholder.getFirst().toString());
        if (context != null && context.level().isClientSide &&
                !GTRegistries.PLACEHOLDERS.get(GTCEu.id(placeholder.getFirst().toString())).isView())
            GTCEu.LOGGER.warn("Placeholder processing is running on client instead of server!");
        return GTRegistries.PLACEHOLDERS.get(GTCEu.id(placeholder.getFirst().toString())).apply(context,
                placeholder.subList(1, placeholder.size()));
    }

    public static MultiLineComponent processPlaceholders(String s, PlaceholderContext ctx) {
        if (ctx.level().isClientSide)
            GTCEu.LOGGER.warn("Placeholder processing is running on client instead of server!");
        List<Exception> exceptions = new ArrayList<>();
        Object2IntOpenHashMap<String> indices = new Object2IntOpenHashMap<>();
        boolean escape = false;
        boolean escapeNext = false;
        boolean literalEscape = false;
        boolean lineBeginningWhitespace = true;
        int singleEscapes = 0;
        char prev = '\0';
        int line = 1;
        int symbol = 1;
        Stack<List<MultiLineComponent>> stack = new Stack<>();
        stack.push(GTUtil.list(MultiLineComponent.empty()));
        for (char c : s.toCharArray()) {
            if (!exceptions.isEmpty()) break;
            if (c == '\'' && prev == '[') {
                singleEscapes++;
                symbol++;
                prev = '\0';
                continue;
            }
            if (c == ']' && prev == '\'') {
                singleEscapes--;
                if (singleEscapes < 0) {
                    exceptions.add(new UnexpectedBracketException());
                }
                symbol++;
                stack.peek().getLast().append(c);
                prev = c;
                continue;
            } else if (prev == '\'') stack.peek().getLast().append('\'');
            if (escape || ((literalEscape || singleEscapes > 0) && c != LITERAL_ESCAPE && c != SINGLE_ESCAPE)) {
                if (c == ESCAPED_NEWLINE && !literalEscape && singleEscapes == 0) {
                    stack.peek().getLast().appendNewline();
                    line++;
                    lineBeginningWhitespace = true;
                    symbol = 0;
                } else if (c != NEWLINE) {
                    lineBeginningWhitespace = false;
                    stack.peek().getLast().append(c);
                } else lineBeginningWhitespace = false;
            } else {
                switch (c) {
                    case ESCAPE -> escapeNext = true;
                    case LITERAL_ESCAPE -> literalEscape = !literalEscape;
                    case NEWLINE -> {
                        stack.peek().getLast().appendNewline();
                        line++;
                        lineBeginningWhitespace = true;
                        symbol = 0;
                    }
                    case ARG_SEPARATOR -> {
                        if (stack.size() == 1) stack.peek().getLast().append(c);
                        else if (!lineBeginningWhitespace) stack.peek().add(MultiLineComponent.empty());
                    }
                    case PLACEHOLDER_BEGIN -> {
                        lineBeginningWhitespace = false;
                        stack.push(GTUtil.list(MultiLineComponent.empty()));
                    }
                    case PLACEHOLDER_END -> {
                        lineBeginningWhitespace = false;
                        List<MultiLineComponent> placeholder = stack.pop();
                        try {
                            if (stack.isEmpty()) throw new UnexpectedBracketException();
                            MultiLineComponent result = processPlaceholder(placeholder, ctx, indices);
                            if (result.isIgnoreSpaces() || stack.size() == 1) {
                                stack.peek().getLast().append(result);
                            } else {
                                for (int i = 0; i < result.size(); i++) {
                                    MutableComponent component = result.get(i);
                                    component.visit((style, string) -> {
                                        String[] split = string.split(String.valueOf(ARG_SEPARATOR));
                                        for (int j = 0; j < split.length; j++) {
                                            String idk = split[j];
                                            stack.peek().getLast()
                                                    .append(MultiLineComponent.literal(idk).withStyle(style));
                                            if (j == split.length - 1) continue;
                                            if (stack.size() == 1) {
                                                stack.peek().getLast().append(ARG_SEPARATOR);
                                            } else {
                                                stack.peek().add(MultiLineComponent.empty());
                                            }
                                        }
                                        return Optional.empty();
                                    }, component.getStyle());
                                    if (i != result.size() - 1) stack.peek().getLast().appendNewline();
                                }
                            }
                        } catch (PlaceholderException e) {
                            exceptions.add(e);
                            e.setLineInfo(line, symbol);
                        }
                    }
                    default -> {
                        lineBeginningWhitespace = false;
                        if (c != SINGLE_ESCAPE) stack.peek().getLast().append(c);
                    }
                }
                if (stack.isEmpty()) break;
            }
            escape = escapeNext;
            escapeNext = false;
            symbol++;
            prev = c;
        }
        if (stack.size() > 1) {
            PlaceholderException exception = new UnclosedBracketException();
            exception.setLineInfo(line, symbol);
            exceptions.add(exception);
        }
        if (exceptions.isEmpty()) {
            MultiLineComponent out = stack.peek().stream().reduce(MultiLineComponent.empty(),
                    MultiLineComponent::append);
            if (out.toString().length() > 16000) return MultiLineComponent.literal("Output too long");
            return out;
        }
        MultiLineComponent out = MultiLineComponent.empty();
        exceptions.forEach(exception -> {
            out.append(exception.getMessage());
            out.appendNewline();
        });
        return out.withStyle(ChatFormatting.DARK_RED);
    }

    public static IPanelHandler createPlaceholderEditor(String name, PanelSyncManager syncManager,
                                                        PlaceholderContext ctx,
                                                        IStringValue<?> code,
                                                        @Nullable DoubleSyncValue scaleDouble,
                                                        @Nullable IStringValue<?> updateInterval,
                                                        @Nullable IBoolValue<?> pause,
                                                        @Nullable Runnable updateText) {
        IPanelHandler helpPanel = syncManager.syncedPanel("placeholder_language_help",
                true,
                (syncManager1, panelHandler1) -> createHelpPanel());
        InteractionSyncHandler runCodeOnce = updateText == null ? null : new InteractionSyncHandler();
        if (updateText != null) runCodeOnce.setOnMousePressed(mouseData -> {
            if (!mouseData.isClient())
                updateText.run();
        });
        return syncManager.syncedPanel(name, true, (psm, handler) -> createPlaceholderEditorPanel(
                name, ctx, code, scaleDouble, updateInterval, pause, helpPanel, runCodeOnce));
    }

    public static ModularPanel<?> createPlaceholderEditorPanel(String name,
                                                               PlaceholderContext ctx,
                                                               IStringValue<?> code,
                                                               @Nullable DoubleSyncValue scaleDouble,
                                                               @Nullable IStringValue<?> updateInterval,
                                                               @Nullable IBoolValue<?> pause,
                                                               IPanelHandler helpPanel,
                                                               @Nullable InteractionSyncHandler runCodeOnce) {
        return new ModularPanel<>(name)
                .size(400, 250)
                .resizeableOnDrag(true)
                .excludeAreaInRecipeViewer()
                .child(Flow.row()
                        .childIf(ctx.itemStackHandler() != null, () -> Flow.column()
                                .coverChildren()
                                .paddingLeft(4)
                                .children(
                                        ctx.itemStackHandler().getSlots(),
                                        i -> new ItemSlot()
                                                .slot(ctx.itemStackHandler(), i)
                                                .addTooltipLine(
                                                        Text.lang("gtceu.gui.computer_monitor_cover.slot_tooltip", i))))
                        .child(Flow.column()
                                .widthRel(.7f)
                                .padding(5)
                                .child(Flow.row()
                                        .height(20)
                                        .childIf(scaleDouble != null,
                                                () -> new TextWidget<>(
                                                        Text.lang("gtceu.gui.central_monitor.text_scale")))
                                        .childIf(scaleDouble != null, () -> new TextFieldWidget()
                                                .setNumbersDouble(x -> Math.max(x, 0))
                                                .setDefaultNumber(1.0)
                                                .value(scaleDouble)
                                                .marginLeft(4))
                                        .childIf(updateInterval != null,
                                                () -> new TextWidget<>(
                                                        Text.lang("gtceu.gui.computer_monitor_cover.update_interval")))
                                        .childIf(updateInterval != null, () -> new TextFieldWidget()
                                                .setNumbers(1, 1000)
                                                .setDefaultNumber(1)
                                                .value(updateInterval)
                                                .marginLeft(4))
                                        .childIf(pause != null, () -> new ToggleButton()
                                                .value(pause)
                                                .marginLeft(10)
                                                .marginRight(10)
                                                .background(true, GuiTextures.MC_BUTTON)
                                                .hoverBackground(true, GuiTextures.MC_BUTTON_HOVERED)
                                                .background(false, GuiTextures.MC_BUTTON)
                                                .hoverBackground(false, GuiTextures.MC_BUTTON_HOVERED)
                                                .overlay(false, GuiTextures.PAUSE)
                                                .overlay(true, GuiTextures.PLAY)
                                                .addTooltip(false, Text.lang("gtceu.gui.central_monitor.pause"))
                                                .addTooltip(true, Text.lang("gtceu.gui.central_monitor.resume")))
                                        .childIf(runCodeOnce != null, () -> new ButtonWidget<>()
                                                .overlay(GuiTextures.RIGHTLOAD)
                                                .addTooltipLine(Text.lang("gtceu.gui.central_monitor.update_once"))
                                                .syncHandler(runCodeOnce))
                                        .child(new ButtonWidget<>()
                                                .right(0)
                                                .overlay(GuiTextures.HELP)
                                                .onMousePressed((GuiContext context, int button) -> {
                                                    helpPanel.openPanel();
                                                    return true;
                                                })))
                                .child(new CodeEditorWidget<>(LANG_DEFINITION)
                                        .value(code)
                                        .langContext(ctx)
                                        .fullWidth()
                                        .heightRelOffset(1, -25)))
                        .child(new ListWidget<>()
                                .widthRel(.25f)
                                .right(0)
                                .paddingBottom(5)
                                .excludeAreaInRecipeViewer()
                                .fullHeight()
                                .children(GTRegistries.PLACEHOLDERS.keySet()
                                        .stream()
                                        .sorted()
                                        .map(s -> (IWidget) Flow.row()
                                                .coverChildren()
                                                .child(new TextWidget<>(s.toString().replaceAll("gtceu:", ""))
                                                        .center())
                                                .tooltip(new RichTooltip()
                                                        .addDrawableLines(LangHandler
                                                                .getSingleOrMultiLang(
                                                                        "gtceu.placeholder_info." + s)
                                                                .stream()
                                                                .map(Text::of)
                                                                .map(key -> (IDrawable) key)
                                                                .toList())))
                                        .toList())));
    }

    public static ModularPanel<?> createHelpPanel() {
        return new ModularPanel<>("placeholder_language_help")
                .size(500, 250)
                .child(Flow.column()
                        .padding(5)
                        .child(new TextWidget<>(Text.lang("gtceu.gui.central_monitor.text_module_help")))
                        .child(new CodeEditorWidget<>(LANG_DEFINITION)
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
        private int unclosedSingleEscapes = 0;
        private int unclosedBrackets = 0;
        private final StringBuilder everything = new StringBuilder();
        private final Stack<Integer> pureStarts = new Stack<>();
        private final Stack<Integer> viewStarts = new Stack<>();
        private final Stack<String> openPlaceholders = new Stack<>();
        private int ifDepth = 0;

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
                if (unclosedSingleEscapes > 0) {
                    onEncounteredError();
                    return Component.literal(" ").withStyle(Style.EMPTY
                            .withUnderlined(true)
                            .withInsertion("")
                            .withColor(0xFF0000)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    unclosedSingleEscapes == 1 ?
                                            Component.translatable("gtceu.placeholder_editor.unclosed_escape") :
                                            Component.translatable("gtceu.placeholder_editor.unclosed_escapes",
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
                case "['" -> {
                    everything.append(s);
                    unclosedSingleEscapes++;
                    return Component.literal(s).withStyle(ChatFormatting.GOLD);
                }
                case "']" -> {
                    everything.append(s);
                    unclosedSingleEscapes--;
                    if (unclosedSingleEscapes < 0) {
                        onEncounteredError();
                        return Component.literal(s).withStyle(Style.EMPTY
                                .withColor(0xFF0000)
                                .withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("gtceu.placeholder_editor.extra_closing_bracket"))));
                    }
                    return Component.literal(s).withStyle(ChatFormatting.GOLD);
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
                        String result = processPlaceholders(everything.substring(pureStarts.peek()), ctx)
                                .toString()
                                .replaceAll("\\n", "\\\\n");
                        int popped = pureStarts.peek();
                        pureStarts.pop();
                        viewStarts.pop();
                        if (!everything.substring(popped).contains(" ")) return Component.literal(s);
                        return Component.literal(s)
                                .withStyle(style -> style.withHoverEvent(new HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        Component.translatable("gtceu.placeholder_editor.constant_value", result)))
                                        .withInsertion(""));
                    }
                    if (!viewStarts.empty()) viewStarts.pop();
                    return Component.literal(s);
                }
            }
            if (prevOpenBracket) {
                prevOpenBracket = false;
                var id = GTCEu.id(s);
                if (GTRegistries.PLACEHOLDERS.containsKey(id)) {
                    if (GTRegistries.PLACEHOLDERS.get(id).isPure()) {
                        pureStarts.push(everything.length() - 1);
                    } else pureStarts.clear();
                    if (GTRegistries.PLACEHOLDERS.get(id).isView()) {
                        viewStarts.push(everything.length() - 1);
                    } else viewStarts.clear();
                    everything.append(s);
                    openPlaceholders.push(s);
                    if (s.equals("if")) ifDepth++;
                    else if (ifDepth > 0 && !GTRegistries.PLACEHOLDERS.get(id).isView()) {
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
            return Component.literal(s);
        }

        private void onEncounteredError() {
            viewStarts.clear();
            pureStarts.clear();
            openPlaceholders.clear();
            ifDepth = 0;
            unclosedSingleEscapes = 0;
        }
    }
}
