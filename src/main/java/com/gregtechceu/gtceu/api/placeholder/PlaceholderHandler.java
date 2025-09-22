package com.gregtechceu.gtceu.api.placeholder;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.placeholder.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnclosedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnexpectedBracketException;
import com.gregtechceu.gtceu.api.placeholder.exceptions.UnknownPlaceholderException;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTStringUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.language.LanguageDefinition;
import com.lowdragmc.lowdraglib.gui.widget.codeeditor.language.TokenTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.function.Consumer;

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

    public static final LanguageDefinition LANG_DEFINITION = new LanguageDefinition(
            "Placeholders",
            List.of(
                    TokenTypes.KEYWORD.createTokenType(PlaceholderHandler.getAllPlaceholderNames().stream().toList()),
                    TokenTypes.IDENTIFIER,
                    TokenTypes.STRING,
                    TokenTypes.COMMENT,
                    TokenTypes.NUMBER,
                    TokenTypes.OPERATOR,
                    TokenTypes.WHITESPACE,
                    TokenTypes.OTHER),
            Set.of());

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
                                                        PlaceholderContext context) throws PlaceholderException {
        if (!placeholderExists(placeholder.get(0)))
            throw new UnknownPlaceholderException(placeholder.get(0).toString());
        return placeholders.get(placeholder.get(0).toString()).apply(context,
                placeholder.subList(1, placeholder.size()));
    }

    public static MultiLineComponent processPlaceholders(String s, PlaceholderContext ctx) {
        if (ctx.level().isClientSide)
            GTCEu.LOGGER.warn("Placeholder processing is running on client instead of server!");
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
                } else if (c == NEWLINE) continue;
                else GTUtil.getLast(stack.peek()).append(c);
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
}
