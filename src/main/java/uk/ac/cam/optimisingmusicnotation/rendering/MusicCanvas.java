package uk.ac.cam.optimisingmusicnotation.rendering;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.awt.*;
import java.io.IOException;
import java.util.function.BiFunction;

/**
 * An interface for the abstract drawing functions.
 * @param <Anchor> the type of the physical anchor the canvas uses
 */
public interface MusicCanvas<Anchor> {

    /**
     * Creates an anchor representing the given musical position at the top of the corresponding stave.
     */
    Anchor getAnchor(MusicalPosition musicalPosition);
    /**
     * Creates an anchor representing the given musical position and pitch.
     */
    Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch);
    /**
     * Creates an anchor representing the top left of the line which the given musical position is on.
     */
    Anchor getLineStartAnchor(MusicalPosition musicalPosition);
    /**
     * Creates an anchor representing the left of the line which the given musical position is on with the given pitch.
     */
    Anchor getLineStartAnchor(MusicalPosition musicalPosition, Pitch pitch);
    /**
     * Creates an anchor representing the given musical position at the bottom of the corresponding stave.
     */
    Anchor getLowestStaveLineAnchor(MusicalPosition musicalPosition);
    /**
     * Creates an anchor representing the bottom left of the given stave on the given line.
     */
    Anchor getLowestStaveLineStartOfLineAnchor(Line line, Stave stave);
    /**
     * Creates an anchor representing the top left of the given stave on the given line.
     */
    Anchor getStartOfLineAnchor(Line line, Stave stave);
    /**
     * Creates an anchor representing the top right of the given stave on the given line.
     */
    Anchor getEndOfLineAnchor(Line line, Stave stave);
    /**
     * Creates a new anchor using an offset from a given anchor.
     */
    Anchor offsetAnchor(Anchor anchor, float x, float y);
    /**
     * Creates a new anchor by linear interpolation between two given anchors.
     */
    Anchor interpolateAnchors(Anchor anchor1, Anchor anchor2, float t);
    /**
     * Creates a new anchor with the horizontal position of the first anchor and the vertical position of the second.
     */
    Anchor getTakeXTakeYAnchor(Anchor anchorX, Anchor anchorY);
    /**
     * Returns the anchor with the minimum value according to the provided comparator function.
     */
    Anchor getMinAnchor(java.util.List<Anchor> anchors, Anchor start, BiFunction<Anchor, Anchor, Boolean> lessThan);
    /**
     * Creates an anchor representing the top left of the output.
     */
    Anchor topLeftAnchor();
    /**
     * Creates an anchor representing the top centre of the output.
     */
    Anchor topCentreAnchor();
    /**
     * Creates an anchor representing the top right of the output.
     */
    Anchor topRightAnchor();
    /**
     * Creates an anchor at the lowest point that any element was drawn on the line with the given number.
     */
    Anchor getTrueBottomAnchor(int line);

    /**
     * Tests if an anchor is below another.
     */
    boolean isAnchorBelow(Anchor anchor1, Anchor anchor2);
    /**
     * Tests if an anchor is above another.
     */
    boolean isAnchorAbove(Anchor anchor1, Anchor anchor2);
    /**
     * Tests if two anchors are on the same page.
     */
    boolean areAnchorsOnSamePage(Anchor anchor1, Anchor anchor2);

    /**
     * Start a new musical line and add a stave to it.
     */
    void addFirstStave(float crotchetsOffset, int staveNumber);
    /**
     * Adds a stave to the lowest musical line with no offset.
     */
    void addStave();
    /**
     * Adds a stave to the lowest musical line with a given offset.
     */
    void addStave(float crotchetsOffset);
    /**
     * Reserves a given amount of height before the next stave is placed.
     */
    void reserveHeight(float height);

    /**
     * Draws a filled circle.
     * @param anchor the default centre of the circle
     * @param x the centre's horizontal offset from the anchor
     * @param y the centre's vertical offset from the anchor
     * @param r the radius
     */
    void drawCircle(Anchor anchor, float x, float y, float r);
    /**
     * Draws a circle.
     * @param anchor the anchor for the centre of the circle
     * @param x the centre's horizontal offset from the anchor
     * @param y the centre's vertical offset from the anchor
     * @param r the radius
     * @param fill if true, fills the circle black, otherwise the centre is white
     */
    void drawCircle(Anchor anchor, float x, float y, float r, boolean fill);

    /**
     * Draws a black straight line.
     * @param anchor the anchor for both endpoints
     * @param x1 the first endpoint's horizontal offset from the anchor
     * @param y1 the first endpoint's vertical offset from the anchor
     * @param x2 the second endpoint's horizontal offset from the anchor
     * @param y2 the second endpoint's vertical offset from the anchor
     * @param lineWidth the width of the line
     */
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth);
    /**
     * Draws a straight line.
     * @param anchor the anchor for both endpoints
     * @param x1 the first endpoint's horizontal offset from the anchor
     * @param y1 the first endpoint's vertical offset from the anchor
     * @param x2 the second endpoint's horizontal offset from the anchor
     * @param y2 the second endpoint's vertical offset from the anchor
     * @param lineWidth the width of the line
     * @param color the colour of the line
     */
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color);
    /**
     * Draws a straight line.
     * @param anchor the anchor for both endpoints
     * @param x1 the first endpoint's horizontal offset from the anchor
     * @param y1 the first endpoint's vertical offset from the anchor
     * @param x2 the second endpoint's horizontal offset from the anchor
     * @param y2 the second endpoint's vertical offset from the anchor
     * @param lineWidth the width of the line
     * @param color the colour of the line
     * @param reserveHeight if false, the line endpoints don't reserve space before the next stave (e.g. for pulse lines)
     */
    void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color, boolean reserveHeight);
    /**
     * Draws a black straight line.
     * @param anchor1 the anchor for the first endpoint
     * @param x1 the first endpoint's horizontal offset from the anchor
     * @param y1 the first endpoint's vertical offset from the anchor
     * @param anchor2 the anchor for the second endpoint
     * @param x2 the second endpoint's horizontal offset from the anchor
     * @param y2 the second endpoint's vertical offset from the anchor
     * @param lineWidth the width of the line
     */
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth);
    /**
     * Draws a straight line.
     * @param anchor1 the anchor for the first endpoint
     * @param x1 the first endpoint's horizontal offset from the anchor
     * @param y1 the first endpoint's vertical offset from the anchor
     * @param anchor2 the anchor for the second endpoint
     * @param x2 the second endpoint's horizontal offset from the anchor
     * @param y2 the second endpoint's vertical offset from the anchor
     * @param lineWidth the width of the line
     * @param color the colour of the line
     */
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color);
    /**
     * Draws a straight line.
     * @param anchor1 the anchor for the first endpoint
     * @param x1 the first endpoint's horizontal offset from the anchor
     * @param y1 the first endpoint's vertical offset from the anchor
     * @param anchor2 the anchor for the second endpoint
     * @param x2 the second endpoint's horizontal offset from the anchor
     * @param y2 the second endpoint's vertical offset from the anchor
     * @param lineWidth the width of the line
     * @param color the colour of the line
     * @param reserveHeight if false, the line endpoints don't reserve space before the next stave (e.g. for pulse lines)
     */
    void drawLine(Anchor anchor1,  float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color, boolean reserveHeight);

    /**
     * Draws whitespace in a given rectangle.
     * @param topLeftAnchor the anchor for the top left corner of the rectangle
     * @param topLeftX the top left corner's horizontal offset from the anchor
     * @param topLeftY the top left corner's vertical offset from the anchor
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height);
    /**
     * Draws whitespace in a given rectangle.
     * @param topLeftAnchor the anchor for the top left corner of the rectangle
     * @param topLeftX the top left corner's horizontal offset from the anchor
     * @param topLeftY the top left corner's vertical offset from the anchor
     * @param bottomRightAnchor the anchor for the bottom right corner of the rectangle
     * @param bottomRightX the bottom right corner's horizontal offset from the anchor
     * @param bottomRightY the bottom right corner's vertical offset from the anchor
     */
    void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY,
                        Anchor bottomRightAnchor, float bottomRightX, float bottomRightY);

    /**
     * Draws an image with the given file name into the given rectangle.
     * @param fileName the file name of the image - currently only supports SVG
     * @param topLeftAnchor the anchor for the top left corner of the rectangle
     * @param topLeftX the top left corner's horizontal offset from the anchor
     * @param topLeftY the top left corner's vertical offset from the anchor
     * @param width the width of the rectangle - set to 0 to preserve aspect ratio
     * @param height the height of the rectangle - set to 0 to preserve aspect ratio
     * @throws IOException if the image file cannot be read
     */
    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException;
    /**
     * Draws an image with the given file name into the given rectangle.
     * @param fileName the file name of the image - currently only supports SVG
     * @param topLeftAnchor the anchor for the top left corner of the rectangle
     * @param topLeftX the top left corner's horizontal offset from the anchor
     * @param topLeftY the top left corner's vertical offset from the anchor
     * @param bottomRightAnchor the anchor for the bottom right corner of the rectangle
     * @param bottomRightX the bottom right corner's horizontal offset from the anchor
     * @param bottomRightY the bottom right corner's vertical offset from the anchor
     * @throws IOException if the image file cannot be read
     */
    void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY,
                   Anchor bottomRightAnchor, float bottomRightX, float bottomRightY)
            throws IOException;

    /**
     * Draws an axis-aligned ellipse.
     * @param centre the anchor for the centre of the ellipse
     * @param x the centre's horizontal offset from the anchor
     * @param y the centre's vertical offset from the anchor
     * @param rx the horizontal radius
     * @param ry the vertical radius
     * @param fill if true, fills the ellipse black, otherwise the centre is white
     */
    void drawEllipse(Anchor centre, float x, float y, float rx, float ry, boolean fill);

    /**
     * Draws a beam (a parallelogram with vertical left and right edges).
     * @param left the anchor for the left endpoint
     * @param leftX the left endpoint's horizontal offset from the anchor
     * @param leftY the left endpoint's vertical offset from the anchor
     * @param right the anchor for the right endpoint
     * @param rightX the right endpoint's horizontal offset from the anchor
     * @param rightY the right endpoint's vertical offset from the anchor
     * @param height the height of the beam
     */
    void drawBeam(Anchor left, float leftX, float leftY, Anchor right, float rightX, float rightY, float height);

    /**
     * Draws a beam (a parallelogram with vertical left and right edges).
     * @param anchor the anchor for both endpoints
     * @param x1 the left endpoint's horizontal offset from the anchor
     * @param y1 the left endpoint's vertical offset from the anchor
     * @param x2 the right endpoint's horizontal offset from the anchor
     * @param y2 the right endpoint's vertical offset from teh anchor
     * @param height the height of the beam
     */
    void drawBeam(Anchor anchor, float x1, float y1, float x2, float y2, float height);

    /**
     * Draws a Bezier curve.
     * @param start the anchor for the left endpoint
     * @param startX the left endpoint's horizontal offset from the anchor
     * @param startY the left endpoint's vertical offset from the anchor
     * @param end the anchor for the right endpoint
     * @param endX the right endpoint's horizontal offset from the anchor
     * @param endY the right endpoint's vertical offset from the anchor
     * @param lineWidth the width of the curve
     * @param up if true, the curve is drawn above the endpoints, and otherwise it is drawn below
     */
    void drawCurve(Anchor start, float startX, float startY, Anchor end, float endX, float endY, float lineWidth, boolean up);

    /**
     * Draws black text with a font provided by the given file into the given rectangle.
     * @param fileName the file name of the font file (TTF)
     * @param text the text to draw
     * @param fontSize the font size
     * @param alignment the alignment of the text in the rectangle
     * @param topLeftAnchor the anchor for the top left corner of the rectangle
     * @param topLeftX the top left corner's horizontal offset from the anchor
     * @param topLeftY the top left corner's vertical offset from the anchor
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @throws IOException if the font file cannot be read
     */
    void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
                  Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException;

    /**
     * Draws text with a font provided by the given file into the given rectangle.
     * @param fileName the file name of the font file (TTF)
     * @param text the text to draw
     * @param fontSize the font size
     * @param alignment the alignment of the text in the rectangle
     * @param topLeftAnchor the anchor for the top left corner of the rectangle
     * @param topLeftX the top left corner's horizontal offset from the anchor
     * @param topLeftY the top left corner's vertical offset from the anchor
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param color the colour of the text
     * @throws IOException if the font file cannot be read
     */
    void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
                         Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height, Color color)
            throws IOException;
}