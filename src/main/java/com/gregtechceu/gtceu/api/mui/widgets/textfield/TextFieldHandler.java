package com.gregtechceu.gtceu.api.mui.widgets.textfield;

import com.gregtechceu.gtceu.api.mui.drawable.text.FontRenderHelper;
import com.gregtechceu.gtceu.api.mui.utils.Point;
import com.gregtechceu.gtceu.api.mui.widget.scroll.ScrollArea;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;

import net.minecraft.network.chat.Component;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles the text itself like inserting and deleting text. Also handles the cursor and marking text.
 */
public class TextFieldHandler {

    private static final Joiner JOINER = Joiner.on('\n');

    @Getter
    private final List<String> text = new ArrayList<>();
    private final Point cursor = new Point(), cursorEnd = new Point();
    private final BaseTextFieldWidget<?> textFieldWidget;
    @Setter
    private TextFieldRenderer renderer;
    @Getter
    @Setter
    @Nullable
    private ScrollArea scrollArea;
    private boolean mainCursorStart = true;
    @Getter
    private int maxLines = 1;
    @Setter
    @Nullable
    private Pattern pattern;
    @Getter
    @Setter
    private int maxCharacters = -1;
    @Getter
    @Setter
    private GuiContext guiContext;

    public TextFieldHandler(BaseTextFieldWidget<?> textFieldWidget) {
        this.textFieldWidget = textFieldWidget;
    }

    public void switchCursors() {
        this.mainCursorStart = !this.mainCursorStart;
    }

    public Point getMainCursor() {
        return this.mainCursorStart ? this.cursor : this.cursorEnd;
    }

    public Point getOffsetCursor() {
        return this.mainCursorStart ? this.cursorEnd : this.cursor;
    }

    public Point getStartCursor() {
        if (!hasTextMarked()) {
            return this.cursor;
        }
        return this.cursor.y > this.cursorEnd.y ||
                (this.cursor.y == this.cursorEnd.y && this.cursor.x > this.cursorEnd.x) ? this.cursorEnd : this.cursor;
    }

    public Point getEndCursor() {
        if (!hasTextMarked()) {
            return this.cursor;
        }
        return this.cursor.y > this.cursorEnd.y ||
                (this.cursor.y == this.cursorEnd.y && this.cursor.x > this.cursorEnd.x) ? this.cursor : this.cursorEnd;
    }

    public boolean hasTextMarked() {
        return this.cursor.y != this.cursorEnd.y || this.cursor.x != this.cursorEnd.x;
    }

    public void setOffsetCursor(int linePos, int charPos) {
        getOffsetCursor().set(charPos, linePos);
    }

    public void setMainCursor(int linePos, int charPos, boolean animate) {
        Point main = getMainCursor();
        if (main.x != charPos || main.y != linePos) {
            main.set(charPos, linePos);
            if (!this.text.isEmpty() && this.renderer != null && this.scrollArea != null) {
                // update actual width
                this.renderer.setSimulate(true);
                this.renderer.draw(guiContext.getGraphics(), getTextAsComponents());
                this.renderer.setSimulate(false);
                this.scrollArea.getScrollX().setScrollSize((int) (this.renderer.getLastWidth() + 0.5f));
                if (this.scrollArea.getScrollX().isScrollBarActive(this.scrollArea)) {
                    String line = this.text.get(main.y);
                    int scrollTo = (int) this.renderer
                            .getPosOf(this.renderer.measureStringLines(Collections.singletonList(line)), main).x;
                    scrollTo -= this.scrollArea.getScrollX().getVisibleSize(this.scrollArea) / 2;
                    if (animate) {
                        this.scrollArea.getScrollX().animateTo(this.scrollArea, scrollTo);
                    } else {
                        this.scrollArea.getScrollX().scrollTo(this.scrollArea, scrollTo);
                    }
                }
            }
        }
    }

    public void setCursor(int linePos, int charPos, boolean animate) {
        setCursor(linePos, charPos, true, animate);
    }

    public void setCursor(int linePos, int charPos, boolean applyToOffset, boolean animate) {
        setMainCursor(linePos, charPos, animate);
        if (applyToOffset) {
            setOffsetCursor(linePos, charPos);
        }
    }

    public void setOffsetCursor(Point cursor) {
        setOffsetCursor(cursor.y, cursor.x);
    }

    public void setMainCursor(Point cursor, boolean animate) {
        setMainCursor(cursor.y, cursor.x, animate);
    }

    public void setCursor(Point cursor, boolean animate) {
        setMainCursor(cursor, animate);
        setOffsetCursor(cursor);
    }

    public void putMainCursorAtStart() {
        if (hasTextMarked() && getMainCursor() != getStartCursor()) {
            switchCursors();
        }
    }

    public void putMainCursorAtEnd() {
        if (hasTextMarked() && getMainCursor() != getEndCursor()) {
            switchCursors();
        }
    }

    public void moveCursorLeft(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.x == 0) {
            if (main.y == 0) return;
            setCursor(main.y - 1, this.text.get(main.y - 1).length(), !shift, true);
        } else {
            int newPos = main.x - 1;
            if (ctrl && newPos > 0) {
                String line = this.text.get(main.y);
                boolean found = false;
                for (int i = newPos; i >= 0; i--) {
                    char c = line.charAt(i);
                    if (!Character.isLetter(c) && !Character.isDigit(c)) {
                        if (i < newPos) newPos = i + 1;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    newPos = 0;
                }
            }
            setCursor(main.y, newPos, !shift, true);
        }
    }

    public void moveCursorRight(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        String line = this.text.get(main.y);
        if (main.x == line.length()) {
            if (main.y == this.text.size() - 1) return;
            setCursor(main.y + 1, 0, !shift, true);
        } else {
            int newPos = main.x + 1;
            if (ctrl && newPos < line.length()) {
                boolean found = false;
                for (int i = main.x; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (!Character.isLetter(c) && !Character.isDigit(c)) {
                        if (newPos < i) newPos = i;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    newPos = line.length();
                }
            }
            setCursor(main.y, newPos, !shift, true);
        }
    }

    public void moveCursorUp(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.y > 0) {
            setCursor(main.y - 1, main.x, !shift, true);
        } else {
            setCursor(main.y, 0, !shift, true);
        }
    }

    public void moveCursorDown(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.y < this.text.size() - 1) {
            setCursor(main.y + 1, main.x, !shift, true);
        } else {
            setCursor(main.y, this.text.get(main.y).length(), !shift, true);
        }
    }

    public void markAll() {
        setOffsetCursor(0, 0);
        setMainCursor(this.text.size() - 1, this.text.get(this.text.size() - 1).length(), true);
    }

    public void markCurrentLine() {
        setOffsetCursor(getMainCursor().y, 0);
        setMainCursor(getMainCursor().y, this.text.get(getMainCursor().y).length(), true);
    }

    public String getTextAsString() {
        return JOINER.join(this.text);
    }

    public List<Component> getTextAsComponents() {
        return FontRenderHelper.asComponents(this.text);
    }

    public boolean isTextEmpty() {
        if (this.text.isEmpty()) return true;
        for (String line : this.text) {
            if (!line.isEmpty()) return false;
        }
        return true;
    }

    public void onChanged() {
        this.textFieldWidget.markTooltipDirty();
    }

    public String getSelectedText() {
        if (!hasTextMarked()) return "";
        Point min = getStartCursor();
        Point max = getEndCursor();
        if (min.y == max.y) {
            return this.text.get(min.y).substring(min.x, max.x);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.text.get(min.y).substring(min.x));
        if (max.y > min.y + 2) {
            for (int i = min.y + 1; i < max.y - 1; i++) {
                builder.append(this.text.get(i));
            }
        }
        builder.append(this.text.get(max.y), 0, max.x);
        return builder.toString();
    }

    public boolean test(String text) {
        return this.maxLines > 1 || ((this.pattern == null || this.pattern.matcher(text).matches()) &&
                (this.maxCharacters < 0 || this.maxCharacters >= text.length()));
    }

    public void insert(String text) {
        insert(Arrays.asList(text.split("\n")));
    }

    public void insert(List<String> text) {
        List<String> copy = new ArrayList<>(this.text);
        Point point = insert(copy, text);
        if (point == null || copy.size() > this.maxLines || !this.renderer.wouldFit(copy)) return;
        this.text.clear();
        this.text.addAll(copy);
        setCursor(point, true);
        onChanged();
    }

    private Point insert(List<String> text, List<String> insertion) {
        if (insertion.isEmpty() || (insertion.size() > 1 && text.size() + insertion.size() - 1 > this.maxLines)) {
            return null;
        }
        int x, y = this.cursor.y;
        if (hasTextMarked()) {
            delete(false);
        }
        if (text.isEmpty()) {
            if (insertion.size() == 1 && !test(insertion.get(0))) {
                return null;
            }
            text.addAll(insertion);
            return new Point(text.get(text.size() - 1).length(), text.size() - 1);
        }
        String lineStart = text.get(this.cursor.y).substring(0, this.cursor.x);
        String lineEnd = text.get(this.cursor.y).substring(this.cursor.x);
        if (insertion.size() == 1 && text.size() == 1 && !test(lineStart + insertion.get(0) + lineEnd)) {
            return null;
        }
        text.set(this.cursor.y, lineStart + insertion.get(0));
        if (insertion.size() == 1) {
            if (!test(insertion.get(0))) {
                return null;
            }
            text.set(this.cursor.y, text.get(this.cursor.y) + lineEnd);
            return new Point(this.cursor.x + insertion.get(0).length(), this.cursor.y);
        } else {
            text.add(this.cursor.y + 1, insertion.get(insertion.size() - 1) + lineEnd);
            x = insertion.get(insertion.size() - 1).length();
            y += 1;
            if (insertion.size() > 2) {
                text.addAll(this.cursor.y + 1, text.subList(1, insertion.size() - 1));
                x = insertion.get(insertion.size() - 1).length();
                y += insertion.size() - 1;
            }
            return new Point(x, y);
        }
    }

    public void newLine() {
        if (hasTextMarked()) {
            delete(false);
        }
        String line = this.text.get(this.cursor.y);
        this.text.set(this.cursor.y, line.substring(0, this.cursor.x));
        this.text.add(this.cursor.y + 1, line.substring(this.cursor.x));
        setCursor(this.cursor.y + 1, 0, false);
    }

    public void clear() {
        markAll();
        delete();
    }

    public void delete() {
        delete(false);
    }

    public void delete(boolean inFront) {
        if (hasTextMarked()) {
            Point min = getStartCursor();
            Point max = getEndCursor();
            String minLine = this.text.get(min.y);
            if (min.y == max.y) {
                this.text.set(min.y, minLine.substring(0, min.x) + minLine.substring(max.x));
            } else {
                String maxLine = this.text.get(Math.min(this.text.size() - 1, max.y));
                this.text.set(min.y, minLine.substring(0, min.x) + maxLine.substring(max.x));
                if (max.y > min.y + 1) {
                    this.text.subList(min.y + 1, max.y + 1).clear();
                }
            }
            setCursor(min.y, min.x, false);
        } else {
            String line = this.text.get(this.cursor.y);
            if (inFront) {
                if (this.cursor.x == line.length()) {
                    if (this.text.size() > this.cursor.y + 1) {
                        this.text.set(this.cursor.y, line + this.text.get(this.cursor.y + 1));
                        this.text.remove(this.cursor.y + 1);
                    }
                } else {
                    line = line.substring(0, this.cursor.x) + line.substring(this.cursor.x + 1);
                    this.text.set(this.cursor.y, line);
                }
            } else {
                if (this.cursor.x == 0) {
                    if (this.cursor.y > 0) {
                        String lineAbove = this.text.get(this.cursor.y - 1);
                        this.text.set(this.cursor.y - 1, lineAbove + line);
                        this.text.remove(this.cursor.y);
                        setCursor(this.cursor.y - 1, lineAbove.length(), false);
                    }
                } else {
                    line = line.substring(0, this.cursor.x - 1) + line.substring(this.cursor.x);
                    this.text.set(this.cursor.y, line);
                    setCursor(this.cursor.y, this.cursor.x - 1, false);
                }
            }
        }
        if (this.scrollArea != null) {
            this.scrollArea.getScrollX().clamp(this.scrollArea);
        }
        onChanged();
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
    }
}
