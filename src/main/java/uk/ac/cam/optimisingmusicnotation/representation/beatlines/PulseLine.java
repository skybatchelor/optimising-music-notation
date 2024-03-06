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
     * @param canvas
     * @param stave
     * @param extendUp
     * @param extendDown
     * @param downLength
     * @param drawLabel
     * @param <Anchor>
     */
    <Anchor> void drawAroundStave(MusicCanvas<Anchor> canvas, Stave stave, boolean extendUp, boolean extendDown, float downLength, boolean drawLabel);

    /**
     * Draws the pulse lines through whitespace.
     * @param canvas
     * @param stave
     * @param <Anchor>
     */
    <Anchor> void drawFull(MusicCanvas<Anchor> canvas, Stave stave);
}
