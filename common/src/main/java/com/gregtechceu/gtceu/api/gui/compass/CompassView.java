package com.gregtechceu.gtceu.api.gui.compass;

import com.google.gson.JsonParser;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.utils.XmlUtils;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.FileUtility;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.realmsclient.util.JsonUtils;
import lombok.Getter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.input.ReaderInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author KilaBash
 * @date 2023/7/26
 * @implNote CompassView
 */
public class CompassView extends WidgetGroup {
    public static final int LIST_WIDTH = 150;
    public static final Map<ResourceLocation, List<CompassSection>> SECTIONS = new HashMap<>();
    public static final Map<ResourceLocation, List<CompassNode>> NODES = new HashMap<>();
    public final static Map<ResourceLocation, Map<String, Document>> NODE_PAGES = new HashMap<>();

    public final ResourceLocation path = GTCEu.id("compass");

    @Getter
    protected final Map<ResourceLocation, CompassSection> sections = new LinkedHashMap<>();

    protected WidgetGroup listView;
    protected WidgetGroup mainView;

    public CompassView() {
        super(0, 0, 10, 10);
        setClientSideWidget();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        setSize(new Size(screenWidth, screenHeight));
        this.clearAllWidgets();
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        addWidget(listView = new WidgetGroup(0, 0, 150, screenHeight));
        addWidget(mainView = new WidgetGroup(LIST_WIDTH, 0, screenWidth - 150, screenHeight));
        this.listView.setBackground(GuiTextures.BACKGROUND_INVERSE);
        initCompass();
        var sectionList = new DraggableScrollableWidgetGroup(4, 4, 142, screenHeight - 8);
        sectionList.setYScrollBarWidth(2).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1));
        this.addWidget(sectionList);
        for (var section : this.sections.values()) {
            var selectable = new SelectableWidgetGroup(0, sectionList.getAllWidgetSize() * 20, 140, 20);
            selectable.setBackground(GuiTextures.BUTTON);
            selectable.setOnSelected(s -> {
                s.setBackground(new ResourceBorderTexture("gtceu:textures/gui/widget/button.png", 32, 32, 2, 2).setColor(0xff337f7f));
                this.openSection(section);
            });
            selectable.setOnUnSelected(s -> {
                s.setBackground(GuiTextures.BUTTON);
            });
            selectable.addWidget(new ImageWidget(2, 2, 16, 16, section.getButtonTexture()));
            selectable.addWidget(new ImageWidget(22, 0, 115, 20, new TextTexture(section.sectionName.toString()).setWidth(115).setType(TextTexture.TextType.LEFT_HIDE)));
            sectionList.addWidget(selectable);
        }
        this.listView.addWidget(sectionList);
    }

    @Environment(EnvType.CLIENT)
    public void openSection(CompassSection section) {
        mainView.clearAllWidgets();
        var sectionWidget = new CompassSectionWidget(this, section);
        mainView.addWidget(sectionWidget);
    }

    @Environment(EnvType.CLIENT)
    public void openNodeContent(CompassNode node) {
        NODE_PAGES.clear();
        mainView.clearAllWidgets();
        var pageWidget = new LayoutPageWidget(mainView.getSize().width, mainView.getSize().height);
        var map = NODE_PAGES.computeIfAbsent(node.getPage(), x -> new HashMap<>());
        var lang = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
        var document = map.computeIfAbsent(lang, langKey -> {
            var resourceManager = Minecraft.getInstance().getResourceManager();
            var path = node.getPage().getPath().replace(this.path.getPath(), this.path.getPath() + "/pages/%s".formatted(langKey)) + ".xml";
            var option = resourceManager.getResource(new ResourceLocation(node.getPage().getNamespace(), path));
            if (option.isEmpty()) {
                path = node.getPage().getPath().replace(this.path.getPath(), this.path.getPath() + "/pages/en_us") + ".xml";
                option = resourceManager.getResource(new ResourceLocation(node.getPage().getNamespace(), path));
            }
            var resource = option.orElseGet(() -> resourceManager.getResource(GTCEu.id("compass/pages/en_us/missing.xml")).orElseThrow());
            String content;
            try (var inputStream = resource.open()) {
                content = FileUtility.readInputStream(inputStream);
            } catch (Exception e) {
                GTCEu.LOGGER.error("loading compass page {} failed", node.getPage(), e);
                content = """
                    <page>
                        <text>
                            loading page error
                        </text>
                    </page>
                    """;
            }
            try (var stream = new ReaderInputStream(new StringReader(content), StandardCharsets.UTF_8)) {
                return XmlUtils.loadXml(stream);
            } catch (Exception e) {
                GTCEu.LOGGER.error("loading compass page {} failed", node.getPage(), e);
            }
            return null;
        });

        if (document != null) {
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                var xmlNode = nodeList.item(i);
                if (xmlNode instanceof Element element) {
                    ILayoutComponent component = LayoutComponentManager.createComponent(element.getTagName(), element);
                    if (component != null) {
                        component.createWidgets(pageWidget);
                    }
                }
            }
        }
        mainView.addWidget(pageWidget);
    }

    @Environment(EnvType.CLIENT)
    private void initCompass() {
        SECTIONS.clear();
        NODES.clear();
        sections.clear();
        var resourceManager = Minecraft.getInstance().getResourceManager();
        var sectionList = SECTIONS.computeIfAbsent(path, p -> {
            List<CompassSection> sections = new ArrayList<>();
            for (var entry : resourceManager.listResources(p.getPath() + "/sections", rl -> rl.getPath().endsWith(".json") && rl.getNamespace().equals(p.getNamespace())).entrySet()) {
                var resource = entry.getValue();
                try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    var path = p.getPath() + entry.getKey().getPath().replace(p.getPath() + "/sections", "");
                    path = path.substring(0, path.length() - 5);
                    var section = new CompassSection(new ResourceLocation(p.getNamespace(), path), JsonParser.parseReader(reader).getAsJsonObject());
                    sections.add(section);
                } catch (Exception e) {
                    GTCEu.LOGGER.error("loading compass section {} failed", entry.getKey(), e);
                }
                sections.sort(Comparator.comparingInt(a -> a.priority));
            }
            return sections;
        });
        for (CompassSection section : sectionList) {
            sections.put(section.sectionName, section);
        }
        var nodeList = NODES.computeIfAbsent(path, p -> {
            List<CompassNode> nodes = new ArrayList<>();
            for (var entry : resourceManager.listResources(p.getPath() + "/nodes", rl -> rl.getPath().endsWith(".json") && rl.getNamespace().equals(p.getNamespace())).entrySet()) {
                var resource = entry.getValue();
                try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    var path = p.getPath() + entry.getKey().getPath().replace(p.getPath() + "/nodes", "");
                    path = path.substring(0, path.length() - 5);
                    var node = new CompassNode(new ResourceLocation(p.getNamespace(), path), JsonParser.parseReader(reader).getAsJsonObject());
                    nodes.add(node);
                } catch (Exception e) {
                    GTCEu.LOGGER.error("loading compass node {} failed", entry.getKey(), e);
                }
            }
            return nodes;
        });
        // init nodes' section
        for (CompassNode node : nodeList) {
            var sectionName = new ResourceLocation(JsonUtils.getStringOr("section", node.getConfig(), "default"));
            CompassSection section = sections.get(sectionName);
            if (section != null) {
                node.setSection(section);
            }
        }
        // init relation
        for (CompassSection section : sections.values()) {
            section.initRelation();
        }
    }

}
