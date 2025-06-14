package com.gregtechceu.gtceu.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TagExprFilter {

    public static class TagExprParser {

        public enum TokenType {
            LParen,
            RParen,
            And,
            Or,
            Not,
            Xor,
            String
        }

        public static class Token {

            public String lexeme;
            public TokenType type;

            public Token(TokenType type) {
                this.type = type;
            }

            public Token(TokenType type, String lexeme) {
                this.lexeme = lexeme;
                this.type = type;
            }
        }

        public static abstract class MatchExpr {

            public abstract boolean matches(Set<String> input);
        }

        private static class BinExpr extends MatchExpr {

            MatchExpr left, right;
            Token op;

            public BinExpr(Token op, MatchExpr left, MatchExpr right) {
                this.op = op;
                this.left = left;
                this.right = right;
            }

            @Override
            public boolean matches(Set<String> input) {
                if (left == null || right == null) {
                    return false;
                }
                return switch (op.type) {
                    case And -> left.matches(input) && right.matches(input);
                    case Or -> left.matches(input) || right.matches(input);
                    case Xor -> left.matches(input) ^ right.matches(input);
                    default -> false;
                };
            }
        }

        private static class UnaryExpr extends MatchExpr {

            Token token;
            MatchExpr expr;

            public UnaryExpr(Token token, MatchExpr expr) {
                this.token = token;
                this.expr = expr;
            }

            @Override
            public boolean matches(Set<String> input) {
                if (token.type == TokenType.Not) {
                    return expr != null && !expr.matches(input);
                }

                return false;
            }
        }

        private static class StringExpr extends MatchExpr {

            String value;

            public StringExpr(String value) {
                this.value = value;
            }

            @Override
            public boolean matches(Set<String> input) {
                if (value == null || value.isEmpty()) return false;
                if (value.equals("$") && input.isEmpty()) return true;
                if (!value.contains(":") && !value.startsWith("*")) {
                    value = "forge:" + value;
                }

                String val = quote(value);
                return input.stream().anyMatch(inp -> Pattern.matches(val, inp));
            }

            private String quote(String str) {
                if (str.contains("*")) {
                    var idx = str.indexOf("*");
                    if (idx == str.length() - 1) {
                        return quote(str.substring(0, idx)) + ".*";
                    } else {
                        return quote(str.substring(0, idx)) + ".*" + quote(str.substring(idx + 1));
                    }
                }

                return Pattern.quote(str);
            }
        }

        private static class GroupingExpr extends MatchExpr {

            MatchExpr inner;

            public GroupingExpr(MatchExpr inner) {
                this.inner = inner;
            }

            @Override
            public boolean matches(Set<String> input) {
                return inner != null && inner.matches(input);
            }
        }

        List<Token> tokens;
        int idx = 0;
        Token prev = null;

        public MatchExpr parse(String expr) {
            tokens = tokenize(expr);

            return expression();
        }

        private boolean match(TokenType tt) {
            if (idx >= tokens.size()) {
                return false;
            }

            if (tokens.get(idx).type == tt) {
                prev = tokens.get(idx);
                idx++;
                return true;
            }

            return false;
        }

        private MatchExpr expression() {
            return term();
        }

        private MatchExpr term() {
            MatchExpr lhs = unary();

            BinExpr result = null;
            while (match(TokenType.And) || match(TokenType.Or) || match(TokenType.Xor)) {
                if (result == null) {
                    result = new BinExpr(prev, lhs, unary());
                } else {
                    result = new BinExpr(prev, result, unary());
                }
            }

            if (result != null) {
                return result;
            }

            return lhs;
        }

        private MatchExpr unary() {
            if (match(TokenType.Not)) {
                return new UnaryExpr(prev, id());
            }

            return id();
        }

        private MatchExpr id() {
            if (match(TokenType.LParen)) {
                MatchExpr inner = expression();
                match(TokenType.RParen);

                return new GroupingExpr(inner);
            }

            if (match(TokenType.String)) {
                return new StringExpr(prev.lexeme);
            }

            return null;
        }

        private List<Token> tokenize(String expr) {
            List<Token> result = new ArrayList<>();

            int idx = 0;
            while (idx < expr.length()) {
                char cur = expr.charAt(idx);

                if (Character.isWhitespace(cur)) {
                    idx++;
                    continue;
                }

                // Parse strings
                {
                    int stringLen = 0;
                    while (cur != '(' && cur != ')' && cur != '!' && cur != '&' && cur != '|' && cur != '^' &&
                            cur != ' ') {
                        stringLen++;

                        if (stringLen + idx == expr.length()) {
                            break;
                        }

                        cur = expr.charAt(idx + stringLen);
                    }

                    if (stringLen > 0) {
                        result.add(new Token(TokenType.String, expr.substring(idx, idx + stringLen)));
                        idx += stringLen;
                        continue;
                    }
                }

                // Parse operators
                switch (cur) {
                    case '!' -> result.add(new Token(TokenType.Not));
                    case '&' -> result.add(new Token(TokenType.And));
                    case '|' -> result.add(new Token(TokenType.Or));
                    case '^' -> result.add(new Token(TokenType.Xor));
                    case '(' -> result.add(new Token(TokenType.LParen));
                    case ')' -> result.add(new Token(TokenType.RParen));
                }

                idx++;
            }

            return result;
        }
    }

    /**
     * Parses the given expression and puts them into the given list.
     *
     * @param expression expr to parse
     * @return The parsed expression tree
     */
    public static TagExprParser.MatchExpr parseExpression(String expression) {
        return new TagExprParser().parse(expression);
    }

    /**
     * Matches the given item against a list of rules
     *
     * @param expr  to check against
     * @param stack item to check
     * @return if any of the items oreDicts matches the rules
     */
    public static boolean tagsMatch(TagExprParser.MatchExpr expr, ItemStack stack) {
        Set<String> tags = stack.getTags()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());

        return expr != null && expr.matches(tags);
    }

    public static boolean tagsMatch(TagExprParser.MatchExpr expr, FluidStack stack) {
        Set<String> tags = stack.getFluid().defaultFluidState().getTags()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());

        return expr != null && expr.matches(tags);
    }
}
