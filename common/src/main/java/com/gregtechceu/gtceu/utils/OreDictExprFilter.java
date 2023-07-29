package com.gregtechceu.gtceu.utils;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author brachy84
 */
public class OreDictExprFilter {

    private static final Pattern PARTS_PATTERN = Pattern.compile("\\*+");

    /**
     * Parses the given expression and creates a List.
     *
     * @param expression expr to parse
     * @return match rule list
     */
    public static List<MatchRule> parseExpression(String expression) {
        List<MatchRule> rules = new ArrayList<>();
        parseExpression(rules, expression);
        return rules;
    }

    /**
     * Parses the given expression and puts them into the given list.
     *
     * @param rules      list to fill
     * @param expression expr to parse
     * @return the position of the expr. Is only relevant for sub rules
     */
    public static int parseExpression(List<MatchRule> rules, String expression) {
        rules.clear();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == ' ')
                continue;
            if (c == '(') {
                List<MatchRule> subRules = new ArrayList<>();
                i = parseExpression(subRules, expression.substring(i + 1)) + i + 1;
                rules.add(MatchRule.group(subRules, builder.toString()));
                builder = new StringBuilder();
            } else {
                switch (c) {
                    case '&' -> {
                        rules.add(new MatchRule(builder.toString()));
                        rules.add(new MatchRule(MatchLogic.AND));
                        builder = new StringBuilder();
                    }
                    case '|' -> {
                        rules.add(new MatchRule(builder.toString()));
                        rules.add(new MatchRule(MatchLogic.OR));
                        builder = new StringBuilder();
                    }
                    case '^' -> {
                        rules.add(new MatchRule(builder.toString()));
                        rules.add(new MatchRule(MatchLogic.XOR));
                        builder = new StringBuilder();
                    }
                    case ')' -> {
                        rules.add(new MatchRule(builder.toString()));
                        return i + 1;
                    }
                    default -> builder.append(c);
                }
            }
        }
        if (builder.length() > 0) {
            rules.add(new MatchRule(builder.toString()));
        }
        return expression.length();
    }

    /**
     * Matches the given item against a list of rules
     *
     * @param rules to check against
     * @param stack item to check
     * @return if any of the items oreDicts matches the rules
     */
    public static boolean matchesOreDict(List<MatchRule> rules, ItemStack stack) {
        Set<String> oreDicts = stack.getTags().map(TagKey::location).map(ResourceLocation::getPath).collect(Collectors.toSet());
        if (oreDicts.isEmpty())
            return false;

        if (rules == null || rules.isEmpty())
            return false;

        for (String oreDict : oreDicts) {
            if (matches(rules, oreDict))
                return true;
        }
        return false;
    }

    public static boolean matchesOreDict(List<MatchRule> rules, FluidStack stack) {
        Set<String> oreDicts = stack.getFluid().defaultFluidState().getTags().map(TagKey::location).map(ResourceLocation::getPath).collect(Collectors.toSet());
        if (oreDicts.size() == 0)
            return false;

        if (rules == null || rules.isEmpty())
            return false;

        for (String oreDict : oreDicts) {
            if (matches(rules, oreDict))
                return true;
        }
        return false;
    }

    /**
     * Matches the given string against a list of rules.
     * The string does not have to be an oreDict.
     *
     * @param rules   to check against
     * @param oreDict string to check
     * @return if the string matches the rules
     */
    public static boolean matches(List<MatchRule> rules, String oreDict) {
        boolean first = true;
        boolean lastResult = false;
        MatchLogic lastLogic = null;
        for (MatchRule rule : rules) {
            if (lastLogic == null) {
                if (rule.logic == MatchLogic.AND || rule.logic == MatchLogic.OR || rule.logic == MatchLogic.XOR) {
                    lastLogic = rule.logic;
                    continue;
                }
            }
            if (lastLogic != null || first) {
                if (lastLogic != null) {
                    switch (lastLogic) {
                        case AND: {
                            if (!lastResult)
                                return false;
                            break;
                        }
                        case OR: {
                            if (lastResult)
                                return true;
                            break;
                        }
                    }
                }

                boolean newResult;
                if (rule.isGroup())
                    newResult = rule.logic == MatchLogic.NOT ^ matches(rule.subRules, oreDict);
                else
                    newResult = matches(rule, oreDict);

                if (lastLogic == MatchLogic.XOR) {
                    if (lastResult == newResult)
                        return false;
                }

                lastLogic = null;
                lastResult = newResult;
                first = false;
            }

        }

        return lastResult;
    }

    private static boolean matches(MatchRule rule, String oreDict) {
        String filter = rule.expression;

        if (filter.equals("*"))
            return true;

        boolean startWild = filter.startsWith("*"), endWild = filter.endsWith("*");
        if (startWild) {
            filter = filter.substring(1);
        }

        String[] parts = PARTS_PATTERN.split(filter);

        return (rule.logic == MatchLogic.NOT) ^ matches(parts, oreDict, startWild, endWild);
    }

    private static boolean matches(String[] filter, String oreDict, boolean startWild, boolean endWild) {
        String lastlastPart = filter[0];
        String lastPart = filter[0];
        int index = oreDict.indexOf(lastPart);
        if ((!startWild && index != 0) || index < 0)
            return false;
        boolean didGoBack = false;

        for (int i = 1; i < filter.length; i++) {
            String part = filter[i];
            int newIndex = oreDict.indexOf(part, index + lastPart.length());
            if (newIndex < 0) {
                if (i > 1 && !didGoBack) {
                    i -= 2;
                    lastPart = lastlastPart;
                    didGoBack = true;
                    continue;
                }
                return false;
            }
            lastlastPart = lastPart;
            lastPart = part;
            index = newIndex;
            if (didGoBack)
                didGoBack = false;
        }

        if (endWild || lastPart.length() + index == oreDict.length())
            return true;

        for (int i = filter.length - 1; i < filter.length; i++) {
            String part = filter[i];
            int newIndex = oreDict.indexOf(part, index + lastPart.length());
            if (newIndex < 0) {
                if (i > 1 && !didGoBack) {
                    i -= 2;
                    lastPart = lastlastPart;
                    didGoBack = true;
                    continue;
                }
                return false;
            }
            lastlastPart = lastPart;
            lastPart = part;
            index = newIndex;
            if (didGoBack)
                didGoBack = false;
        }
        return lastPart.length() + index == oreDict.length();
    }

    public static class MatchRule {
        public final MatchLogic logic;
        public final String expression;
        private final List<MatchRule> subRules;

        private MatchRule(MatchLogic logic, String expression, List<MatchRule> subRules) {
            if (expression.startsWith("!")) {
                logic = MatchLogic.NOT;
                expression = expression.substring(1);
            }
            this.logic = logic;
            this.expression = expression;
            this.subRules = subRules;
        }

        public MatchRule(MatchLogic logic, String expression) {
            this(logic, expression, null);
        }

        public MatchRule(MatchLogic logic) {
            this(logic, "");
        }

        public MatchRule(String expression) {
            this(MatchLogic.ANY, expression);
        }

        public static MatchRule not(String expression, boolean not) {
            return new MatchRule(not ? MatchLogic.NOT : MatchLogic.ANY, expression);
        }

        public static MatchRule group(List<MatchRule> subRules, String expression) {
            MatchLogic logic = expression.startsWith("!") ? MatchLogic.NOT : MatchLogic.ANY;
            return new MatchRule(logic, "", subRules);
        }

        public boolean isGroup() {
            return subRules != null;
        }

        @Nullable
        public List<MatchRule> getSubRules() {
            return subRules;
        }
    }

    public enum MatchLogic {
        OR, AND, XOR, NOT, ANY
    }
}
