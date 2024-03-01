package uk.ac.cam.optimisingmusicnotation.rendering;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.awt.*;
import java.io.IOException;
import java.util.function.BiFunction;

public interface MusicCanvas<Anchor> {

    Anchor getAnchor(MusicalPosition musicalPosition);
    Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch);
    Anchor getLineStartAnchor(MusicalPosition musicalPosition);
    Anchor getLineStartAnchor(MusicalPosition musicalPosition, Pitch pitch);
    Anchor getLowestStaveLineAnchor(MusicalPosition musicalPosition);
    Anchor getLowestStaveLineStartOfLineAnchor(Line line);
    Anchor getStartOfLineAnchor(Line line);
    Anchor getEndOfLineAnchor(Line line);
    Anchor offsetAnchor(Anchor anchor, float x, float y);
    Anchor interpolateAnchors(Anchor anchor1, Anchor anchor2, float t);
    Anchor getTakeXTakeYAnchor(Anchor anchorX, Anchor anchorY);
    Anchor getMinAnchor(java.util.List<Anchor> anchors, Anchor start, BiFunction<Anchor, Anchor, Boolean> lessThan);
    Anchor topLeftAnchor();
    Anchor topCentreAnchor();
    Anchor topRightAnchor();
    Anchor getTrueBottomAnchor(int line);

    boolean isAnchorBelow(Anchor anchor1, Anchor anchor2);
    boolean isAnchorAbove(Anchor anchor1, Anchor anchor2);
    boolean areAnchorsOnSamePage(Anchor anchor1, Anchor anchor2);

    void addLine();
    void addLine(float crotchetsOffset);
    void reserveHeight(float height);

    void drawCircle(Anchor anchor, float x, float y, float r);
    void drawCircle(Anchor anchor, float x, float y, float r, boolean fill);

    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth);
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color);
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color, boolean reserveHeight);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color);
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color, boolean reserveHeight);

    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height);
    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY,
                        Anchor bottomRightAnchor, float bottomRightX, float bottomRightY);

    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException;
    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY,
                   Anchor bottomRightAnchor, float bottomRightX, float bottomRightY)
            throws IOException;

    void drawEllipse(Anchor centre, float x, float y, float rx, float ry, boolean fill);

    void drawBeam(Anchor left, float leftX, float leftY, Anchor right, float rightX, float rightY, float height);
    void drawBeam(Anchor anchor, float x1, float y1, float x2, float y2, float height);

    void drawCurve(Anchor start, float startX, float startY, Anchor end, float endX, float endY, float lineWidth, boolean up);

    void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
                  Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException;

    void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
                         Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height, Color color)
            throws IOException;
}