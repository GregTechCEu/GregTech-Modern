package com.gregtechceu.gtceu.common.placeholders;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.placeholders.exceptions.PlaceholderException;
import com.gregtechceu.gtceu.common.placeholders.exceptions.UnclosedBracketException;
import com.gregtechceu.gtceu.common.placeholders.exceptions.UnexpectedBracketException;
import com.gregtechceu.gtceu.common.placeholders.exceptions.UnknownPlaceholderException;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;

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
    private static final char NEWLINE = '\n';
    private static final char ESCAPED_NEWLINE = 'n';

    private final Map<String, Placeholder> placeholders = new HashMap<>();

    public void addPlaceholder(Placeholder placeholder) {
        if (placeholders.containsKey(placeholder.getName())) {
            if (placeholders.get(placeholder.getName()).getPriority() <= placeholder.getPriority()) {
                placeholders.put(placeholder.getName(), placeholder);
            }
        } else placeholders.put(placeholder.getName(), placeholder);
    }

    public boolean placeholderExists(MultiLineComponent placeholder) {
        return placeholders.containsKey(placeholder.toString());
    }

    public MultiLineComponent processPlaceholder(List<MultiLineComponent> placeholder,
                                                 PlaceholderContext context) throws PlaceholderException {
        if (!placeholderExists(placeholder.get(0)))
            throw new UnknownPlaceholderException(placeholder.get(0).toString());
        return placeholders.get(placeholder.get(0).toString()).apply(context,
                placeholder.subList(1, placeholder.size()));
    }

    public MultiLineComponent processPlaceholders(String s, PlaceholderContext ctx) {
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
                            GTUtil.getLast(stack.peek()).append(result);
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

    public Set<String> getAllPlaceholderNames() {
        return placeholders.keySet();
    }
}
