package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class Diminuendo extends LineElement {
    public Diminuendo(List<Chord> chords, Line line, MusicalPosition startPosition, MusicalPosition endPosition) {
        super(chords, line, startPosition, endPosition);
    }
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // TODO: range over the elements to have Anchor = argmin(x.verticalPos)
        Anchor startAnchor = startPosition != null ? canvas.getLowestStaveLineAnchor(startPosition) : canvas.getLowestStaveLineStartOfLineAnchor(line);
        float startOffset = startPosition != null ? RenderingConfiguration.hairpinHeight / 2 : RenderingConfiguration.hairpinHeight * 3 / 8;
        float endOffset = endPosition != null ? 0 : RenderingConfiguration.hairpinHeight / 8;
        Anchor lowestAnchor = canvas.getMinAnchor(chords.stream().map((chord) -> chordAnchorsMap.get(chord).getLowestAnchor(canvas, chord)).toList(), startAnchor, canvas::isAnchorBelow);
        startAnchor = canvas.getTakeXTakeYAnchor(startPosition != null ? canvas.getAnchor(startPosition) : canvas.getStartOfLineAnchor(line), lowestAnchor);
        Anchor endAnchor = canvas.getTakeXTakeYAnchor(endPosition != null ? canvas.getAnchor(endPosition) : canvas.getEndOfLineAnchor(line), lowestAnchor);
        canvas.drawLine(startAnchor, RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset + startOffset,
                endAnchor, -RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset + endOffset,  .1f);
        canvas.drawLine(startAnchor, RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset - startOffset,
                endAnchor, -RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset - endOffset,  .1f);
    }
}
