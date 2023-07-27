package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author KilaBash
 * @date 2022/9/4
 * @implNote XmlUtils
 */
public class XmlUtils {
    public final static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    @Nullable
    public static Document loadXml(InputStream inputstream) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(inputstream);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getAsInt(Element element, String name, int defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Integer.parseInt(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static long getAsLong(Element element, String name, long defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Long.parseLong(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static boolean getAsBoolean(Element element, String name, boolean defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Boolean.parseBoolean(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static String getAsString(Element element, String name, String defaultValue) {
        if (element.hasAttribute(name)) {
            return element.getAttribute(name);
        }
        return defaultValue;
    }

    public static float getAsFloat(Element element, String name, float defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Float.parseFloat(element.getAttribute(name));
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static int getAsColor(Element element, String name, int defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                return Long.decode(element.getAttribute(name)).intValue();
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static BlockPos getAsBlockPos(Element element, String name, BlockPos defaultValue) {
        if (element.hasAttribute("block")) {
            String pos = getAsString(element, name, "0 0 0");
            try {
                var s = pos.split(" ");
                return new BlockPos(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]));
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T getAsEnum(Element element, String name, Class<T> enumClass, T defaultValue) {
        if (element.hasAttribute(name)) {
            try {
                String data = element.getAttribute(name);
                Enum<T>[] values = enumClass.getEnumConstants();
                for (Enum<T> value : values) {
                    if (value.name().equals(data)) {
                        return (T)value;
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return defaultValue;
    }

    public static ItemStack getItemStack(Element element) {
        var ingredient = getIngredient(element);
        if (ingredient.getItems().length > 0) {
            return ingredient.getItems()[0];
        }
        return ItemStack.EMPTY;
    }

    public static Ingredient getIngredient(Element element) {
        int count = getAsInt(element, "count", 1);
        Ingredient ingredient = Ingredient.EMPTY;
        if (element.hasAttribute("item")) {
            Item item = Registry.ITEM.get(new ResourceLocation(element.getAttribute("item")));
            if (item != Items.AIR) {
                ItemStack itemStack = new ItemStack(item, count);
                NodeList nodeList = element.getChildNodes();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    String text = node.getTextContent().replaceAll("\\h*\\R+\\h*", " ");
                    if (!text.isEmpty() && text.charAt(0) == ' ') {
                        text = text.substring(1);
                    }
                    builder.append(text);
                }
                if (!builder.isEmpty()) {
                    try {
                        itemStack.setTag(TagParser.parseTag(builder.toString()));
                    } catch (CommandSyntaxException ignored) {}
                }
                ingredient = SizedIngredient.create(itemStack);
            }
        } else if (element.hasAttribute("tag")){
            ingredient = SizedIngredient.create(element.getAttribute("tag"), count);
        }
        return ingredient;
    }

    public static FluidStack getFluidStack(Element element) {
        long amount = getAsLong(element, "amount", 1L) * FluidHelper.getBucket() / 1000;
        FluidStack fluidStack = FluidStack.empty();
        if (element.hasAttribute("fluid")) {
            var fluid = Registry.FLUID.get(new ResourceLocation(element.getAttribute("fluid")));
            if (fluid != Fluids.EMPTY) {
                fluidStack = FluidStack.create(fluid, amount);
                NodeList nodeList = element.getChildNodes();
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    String text = node.getTextContent().replaceAll("\\h*\\R+\\h*", " ");
                    if (!text.isEmpty() && text.charAt(0) == ' ') {
                        text = text.substring(1);
                    }
                    builder.append(text);
                }
                if (!builder.isEmpty()) {
                    try {
                        fluidStack.setTag(TagParser.parseTag(builder.toString()));
                    } catch (CommandSyntaxException ignored) {}
                }
            }
        }
        return fluidStack;
    }

    public static BlockInfo getBlockInfo(Element element) {
        BlockInfo blockInfo = BlockInfo.EMPTY;
        if (element.hasAttribute("block")) {
            Block block = Registry.BLOCK.get(new ResourceLocation(element.getAttribute("block")));
            if (block != null) {
                return BlockInfo.fromBlock(block);
            }
        }
        return blockInfo;
    }

    public static String getContent(Element element, boolean pretty) {
        NodeList nodeList = element.getChildNodes();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                String text = node.getTextContent();
                if (pretty) {
                    text = text.replaceAll("\\h*\\R+\\h*", " ");
                }
                if (!text.isEmpty() && text.charAt(0) == ' ') {
                    text = text.substring(1);
                }
                builder.append(text);
            } else if (node instanceof Element && node.getNodeName().equals("br")) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    public static List<MutableComponent> getComponents(Element element, Style style) {
        NodeList nodeList = element.getChildNodes();
        List<MutableComponent> results = new ArrayList<>();
        MutableComponent component = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                String text = node.getTextContent();
                text = text.replaceAll("\\h*\\R+\\h*", " ");
                if (!text.isEmpty() && text.charAt(0) == ' ') {
                    text = text.substring(1);
                }
                if (component == null) {
                    component = Component.literal(text).withStyle(style);
                } else {
                    component = component.append(Component.literal(text).withStyle(style));
                }
            } else if (node instanceof Element nodeElement) {
                if ( node.getNodeName().equals("br")) {
                    results.add(Objects.requireNonNullElseGet(component, Component::empty));
                    component = Component.empty();
                }
                if (node.getNodeName().equals("style")) {
                    Style newStyle = style;
                    if (nodeElement.hasAttribute("color")) {
                        newStyle = newStyle.withColor(getAsColor(nodeElement, "color", 0XFFFFFFFF));
                    }
                    if (nodeElement.hasAttribute("bold")) {
                        newStyle = newStyle.withBold(getAsBoolean(nodeElement, "bold", true));
                    }
                    if (nodeElement.hasAttribute("font")) {
                        newStyle = newStyle.withFont(new ResourceLocation(nodeElement.getAttribute("font")));
                    }
                    if (nodeElement.hasAttribute("italic")) {
                        newStyle = newStyle.withItalic(getAsBoolean(nodeElement, "italic", true));
                    }
                    if (nodeElement.hasAttribute("underlined")) {
                        newStyle = newStyle.withUnderlined(getAsBoolean(nodeElement, "underlined", true));
                    }
                    if (nodeElement.hasAttribute("strikethrough")) {
                        newStyle = newStyle.withStrikethrough(getAsBoolean(nodeElement, "strikethrough", true));
                    }
                    if (nodeElement.hasAttribute("obfuscated")) {
                        newStyle = newStyle.withObfuscated(getAsBoolean(nodeElement, "obfuscated", true));
                    }
                    var components = getComponents(nodeElement, newStyle);
                    for (int j = 0; j < components.size(); j++) {
                        if (j == 0) {
                            if (component != null) {
                                component.append(components.get(j));
                            } else {
                                component = components.get(j);
                            }
                        } else {
                            results.add(component);
                            component = components.get(j);
                        }
                    }
                }
            }
        }
        if (component != null) {
            results.add(component);
        }
        return results;
    }
}
