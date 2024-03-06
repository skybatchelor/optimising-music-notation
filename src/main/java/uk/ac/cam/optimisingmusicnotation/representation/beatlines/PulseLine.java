package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;

/**
 * An interface for a generic pulse line.
 */
public interface PulseLine {
    MusicalPosition getMusicalPosition();

    /**
     * Draws the pulse lines around the staves.
     * @param canvas the canvas used for rendering the score
     * @param stave the stave to draw the pulse lines on
     * @param extendUp whether to extend the pulse lines to meet the line above
     * @param extendDown whether to extend the pulse lines below the stave
     * @param downLength how far to extend down the pulse lines
     * @param drawLabel whether to draw the label on the pulse line or not
     * @param <Anchor> the anchor type the canvas uses
     */
    <Anchor> void drawAroundStave(MusicCanvas<Anchor> canvas, Stave stave, boolean extendUp, boolean extendDown, float downLength, boolean drawLabel);

    /**
     * Draws the pulse lines through whitespace.
     * @param canvas the canvas used for rendering the score
     * @param stave the stave that is being drawn through
     * @param <Anchor> the anchor type the canvas uses
     */
    <Anchor> void drawFull(MusicCanvas<Anchor> canvas, Stave stave);
}
