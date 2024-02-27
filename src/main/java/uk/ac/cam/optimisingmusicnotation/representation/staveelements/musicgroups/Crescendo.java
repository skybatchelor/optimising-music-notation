package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
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
        Anchor startAnchor = canvas.getLowestStaveLineAnchor(startPosition);
        Anchor lowestAnchor = canvas.getLowestAnchor(chords.stream().map((chord) -> chordAnchorsMap.get(chord).getLowestAnchor(canvas, chord)).toList(), startAnchor);
        startAnchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(startPosition), lowestAnchor);
        Anchor endAnchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(endPosition), lowestAnchor);
        canvas.drawLine(startAnchor, RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset,
                endAnchor, -RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset + RenderingConfiguration.hairpinHeight / 2,  .1f);
        canvas.drawLine(startAnchor, RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset,
                endAnchor, -RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset - RenderingConfiguration.hairpinHeight / 2,  .1f);
    }
}
