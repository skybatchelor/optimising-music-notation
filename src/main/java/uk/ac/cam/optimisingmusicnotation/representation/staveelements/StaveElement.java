package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;

import java.util.Map;

/**
 * Represents a drawn elements on a stave, primarily {@link Chord} and {@link BeamGroup}.
 */
public interface StaveElement {
    <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap);
}
