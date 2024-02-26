package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class Crescendo extends LineElement {
    public Crescendo(List<Chord> chords, MusicalPosition startPosition, MusicalPosition endPosition) {
        super(chords, startPosition, endPosition);
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // TODO: range over the elements to have Anchor = argmin(x.verticalPos)
        float YPos = -6;
        float h = .75f;
        canvas.drawLine(canvas.getAnchor(startPosition), 0, YPos, canvas.getAnchor(endPosition), 0, YPos + h, .1f);
        canvas.drawLine(canvas.getAnchor(startPosition), 0, YPos, canvas.getAnchor(endPosition), 0, YPos - h, .1f);
    }
}
