package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class Diminuendo extends LineElement {
    public Diminuendo(List<Chord> chords, MusicalPosition startPosition, MusicalPosition endPosition) {
        super(chords, startPosition, endPosition);
    }
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // TODO: range over the elements to have Anchor = argmin(x.verticalPos)
        canvas.drawLine(canvas.getAnchor(startPosition), 0, -1, canvas.getAnchor(endPosition), 0, -1.5f, .1f);
        canvas.drawLine(canvas.getAnchor(startPosition), 0, -2, canvas.getAnchor(endPosition), 0, -1.5f, .1f);
    }
}
