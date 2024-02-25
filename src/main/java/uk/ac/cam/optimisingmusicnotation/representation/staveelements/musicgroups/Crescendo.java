package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class Crescendo extends MusicGroup{
    public Crescendo() {
        super();
    }
    public Crescendo(List<Chord> chords) {
        super(chords);
    }
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // TODO: range over the elements to have Anchor = argmin(x.verticalPos)
        canvas.drawLine(canvas.getAnchor(firstChord.getMusicalPosition()), 0, -1.5f, canvas.getAnchor(lastChord.getMusicalPosition()), 0, -1, .1f);
        canvas.drawLine(canvas.getAnchor(firstChord.getMusicalPosition()), 0, -1.5f, canvas.getAnchor(lastChord.getMusicalPosition()), 0, -2, .1f);
    }
}
