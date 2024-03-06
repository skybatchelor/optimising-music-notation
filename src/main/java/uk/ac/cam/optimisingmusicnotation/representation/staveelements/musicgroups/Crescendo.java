package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

/**
 * A crescendo hairpin.
 */
public class Crescendo extends LineElement {
    public Crescendo(List<Chord> chords, Line line, Stave stave, MusicalPosition startPosition, MusicalPosition endPosition) {
        super(chords, line, stave, startPosition, endPosition);
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // Anchor = argmin(x.verticalPos)
        Anchor startAnchor = startPosition != null ? canvas.getLowestStaveLineAnchor(startPosition) : canvas.getLowestStaveLineStartOfLineAnchor(line, stave);
        float startOffset = startPosition != null ? 0 : RenderingConfiguration.hairpinHeight / 8;
        float endOffset = endPosition != null ? RenderingConfiguration.hairpinHeight / 2 : RenderingConfiguration.hairpinHeight * 3 / 8;
        Anchor lowestAnchor = canvas.getMinAnchor(chords.stream().map((chord) -> chordAnchorsMap.get(chord).getLowestAnchor(canvas, chord)).toList(), startAnchor, canvas::isAnchorBelow);
        startAnchor = canvas.getTakeXTakeYAnchor(startPosition != null ? canvas.getAnchor(startPosition) : canvas.getStartOfLineAnchor(line, stave), lowestAnchor);
        Anchor endAnchor = canvas.getTakeXTakeYAnchor(endPosition != null ? canvas.getAnchor(endPosition) : canvas.getEndOfLineAnchor(line, stave), lowestAnchor);
        canvas.drawLine(startAnchor, startPosition == null ? 0 : RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset + startOffset,
                endAnchor, endPosition == null ? 0 : -RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset + endOffset,  RenderingConfiguration.hairpinLineWidth);
        canvas.drawLine(startAnchor, startPosition == null ? 0 : RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset - startOffset,
                endAnchor, endPosition == null ? 0 : -RenderingConfiguration.hairpinInset, RenderingConfiguration.dynamicsOffset - endOffset,  RenderingConfiguration.hairpinLineWidth);
    }
}
