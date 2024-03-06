package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;

import java.util.Map;

/**
 * Represents a drawn elements on a stave, primarily {@link Chord} and {@link BeamGroup}.
 */
public interface StaveElement {

    /**
     * Draws the given stave element on the score, adding the generated anchors to the anchor map.
     * @param canvas the canvas being used to render the score
     * @param chordAnchorsMap the anchor map the chords are putting the anchors into
     * @param <Anchor> the anchor type the canvas uses
     */
    <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap);
}
