package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Holds the bracket and number for a tuplet.
 */
public class Tuplet extends LineElement {

    private final int num;
    private final boolean bracket;

    public Tuplet(List<Chord> chords, Line line, Stave stave, MusicalPosition startPosition, MusicalPosition endPosition, int num, boolean bracket) {
        super(chords, line, stave, startPosition, endPosition);
        this.num = num;
        this.bracket = bracket;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // TODO: Draw tuplets

        Anchor startAnchor = startPosition != null ?
                RenderingConfiguration.upwardStems ? canvas.getAnchor(startPosition) : canvas.getLowestStaveLineAnchor(startPosition) :
                canvas.getStartOfLineAnchor(line, stave);
        Anchor minAnchor = canvas.getMinAnchor(
                chords.stream().map(
                        RenderingConfiguration.upwardStems
                                ? (chord) -> chordAnchorsMap.get(chord).getHighestAnchor(canvas, chord)
                                : (chord) -> chordAnchorsMap.get(chord).getLowestAnchor(canvas, chord)
                ).toList(), startAnchor, RenderingConfiguration.upwardStems ? canvas::isAnchorAbove : canvas::isAnchorBelow);
        startAnchor = canvas.getTakeXTakeYAnchor(startPosition != null ? canvas.getAnchor(startPosition) : canvas.getStartOfLineAnchor(line, stave), minAnchor);
        Anchor endAnchor = canvas.getTakeXTakeYAnchor(endPosition != null ? canvas.getAnchor(endPosition) : canvas.getEndOfLineAnchor(line, stave), minAnchor);

        if (!bracket && startPosition != null && endPosition != null) {
            float startTime = startPosition.crotchetsIntoLine();
            float endTime = endPosition.crotchetsIntoLine();
            float midTime = (startTime + endTime) / 2;
            for (var chord : chords) {
                float entryTime = chord.getCrotchetsIntoLine();
                if (startTime < entryTime && entryTime <= midTime) {
                    startTime = entryTime;
                    startAnchor = chordAnchorsMap.get(chord).stemEnd();
                }
                if (midTime <= entryTime && entryTime < endTime) {
                    endTime = entryTime;
                    endAnchor = chordAnchorsMap.get(chord).stemEnd();
                }
            }

            Anchor midAnchor;
            if (startTime != endTime) midAnchor = canvas.interpolateAnchors(startAnchor, endAnchor,
                    (midTime - startTime) / (endTime - startTime)
            );
            else midAnchor = startAnchor;

            // Render the number:
            String label = String.valueOf(num);
            float width = label.length() * 1.5f;

            try {
                canvas.drawText(RenderingConfiguration.defaultFontFilePath, label,10f, TextAlignment.CENTRE, midAnchor,
                        -width / 2,
                        RenderingConfiguration.tupletNumHeight / 2 + (RenderingConfiguration.upwardStems
                                ? RenderingConfiguration.tupletOffset
                                : -RenderingConfiguration.tupletOffset), width, RenderingConfiguration.staveTextHeight);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            Anchor midAnchor = canvas.interpolateAnchors(startAnchor, endAnchor, 0.5f);

            // Render the number:
            String label = String.valueOf(num);
            float width = label.length() * 1.5f;

            try {
                canvas.drawText(RenderingConfiguration.defaultFontFilePath, label,10f, TextAlignment.CENTRE, midAnchor,
                        -width / 2,
                        RenderingConfiguration.tupletNumHeight / 2 + (RenderingConfiguration.upwardStems
                                ? RenderingConfiguration.tupletOffset
                                : -RenderingConfiguration.tupletOffset), width, RenderingConfiguration.staveTextHeight);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Render the line:
            canvas.drawLine(startAnchor, -RenderingConfiguration.tupletOverHook,
                    RenderingConfiguration.upwardStems
                            ? RenderingConfiguration.tupletOffset
                            : -RenderingConfiguration.tupletOffset, midAnchor, -width / 2,
                    RenderingConfiguration.upwardStems
                            ? RenderingConfiguration.tupletOffset
                            : -RenderingConfiguration.tupletOffset, RenderingConfiguration.tupletLineWidth);
            canvas.drawLine(endAnchor, RenderingConfiguration.tupletOverHook,
                    RenderingConfiguration.upwardStems
                            ? RenderingConfiguration.tupletOffset
                            : -RenderingConfiguration.tupletOffset, midAnchor, width / 2,
                    RenderingConfiguration.upwardStems
                            ? RenderingConfiguration.tupletOffset
                            : -RenderingConfiguration.tupletOffset, RenderingConfiguration.tupletLineWidth);

            // Render the bracket ends:
            if (startPosition != null) {
                canvas.drawLine(startAnchor, -RenderingConfiguration.tupletOverHook,
                        RenderingConfiguration.upwardStems
                                ? RenderingConfiguration.tupletOffset
                                : -RenderingConfiguration.tupletOffset, -RenderingConfiguration.tupletOverHook,
                        RenderingConfiguration.upwardStems
                                ? RenderingConfiguration.tupletOffset - RenderingConfiguration.tupletEndHook
                                : -RenderingConfiguration.tupletOffset + RenderingConfiguration.tupletEndHook,
                        RenderingConfiguration.tupletLineWidth);
            }
            if (endPosition != null) {
                canvas.drawLine(endAnchor, RenderingConfiguration.tupletOverHook,
                        RenderingConfiguration.upwardStems
                                ? RenderingConfiguration.tupletOffset
                                : -RenderingConfiguration.tupletOffset, RenderingConfiguration.tupletOverHook,
                        RenderingConfiguration.upwardStems
                                ? RenderingConfiguration.tupletOffset - RenderingConfiguration.tupletEndHook
                                : -RenderingConfiguration.tupletOffset + RenderingConfiguration.tupletEndHook,
                        RenderingConfiguration.tupletLineWidth);
            }
        }
    }
}
