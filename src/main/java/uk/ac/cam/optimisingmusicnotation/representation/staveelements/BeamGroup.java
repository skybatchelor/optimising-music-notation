package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeamGroup implements StaveElement {
    private static class Beam {
        int startIndex;
        int endIndex;
        // Zero indexed beams
        int number;

        public Beam(int startIndex, int endIndex, int number) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.number = number;
        }
    }

    private final List<Chord> chords;
    // The list of secondary beams. Note that beam groups always have an implicit first level beam.
    private final List<Beam> beams;

    public BeamGroup() {
        chords = new ArrayList<>();
        beams = new ArrayList<>();
    }

    public BeamGroup(List<Chord> chords) {
        this.chords = chords;
        beams = new ArrayList<>();
    }

    public void addBeam(int startIndex, int endIndex, int number) {
        beams.add(new Beam(startIndex, endIndex, number));
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // decide to draw the not stem upwards or downwards
        int sign = RenderingConfiguration.upwardStems ? 1 : -1;
        for (Chord chord : chords) {
            chord.computeAnchors(canvas, chordAnchorsMap);
        }
        if (chords.size() > 1) {
            // draw the implicit first level beam
            Chord firstChord = chords.get(0);
            ChordAnchors<Anchor> firstChordAnchors = chordAnchorsMap.get(firstChord);
            Chord lastChord = chords.get(chords.size() - 1);
            ChordAnchors<Anchor> lastChordAnchors = chordAnchorsMap.get(lastChord);
            // dividing the beam width by two to align the beams with the stems
            canvas.drawBeam(
                    firstChordAnchors.stemEnd(),
                    0, -sign * RenderingConfiguration.beamWidth / 2,
                    lastChordAnchors.stemEnd(),
                    0, -sign * RenderingConfiguration.beamWidth / 2,
                    RenderingConfiguration.beamWidth);
            if (chords.size() > 3) {
                System.out.println("beams with more than 3 chords are illegal, no promises.");
            }
            // adjust middle chord anchors to account for the beam
            if (chords.size() == 3) {
                Chord secondChord = chords.get(1);
                ChordAnchors<Anchor> secondChordAnchors = chordAnchorsMap.get(secondChord);
                float interpolatePos = (secondChord.getMusicalPosition().crotchetsIntoLine()
                        - firstChord.getMusicalPosition().crotchetsIntoLine())
                        / (lastChord.getMusicalPosition().crotchetsIntoLine()
                                - firstChord.getMusicalPosition().crotchetsIntoLine());

                Anchor interpolatedAnchor = canvas.interpolateAnchors(firstChordAnchors.stemEnd(),
                        lastChordAnchors.stemEnd(), interpolatePos);
                ChordAnchors<Anchor> middleChordAnchors = new ChordAnchors<>(secondChordAnchors.notehead(),
                        interpolatedAnchor, secondChordAnchors.noteheadOffset(), secondChordAnchors.stemEndOffset());
                chordAnchorsMap.put(secondChord, middleChordAnchors);

            }

            for (Beam beam : beams) {
                float beamOffset = -(sign * beam.number * RenderingConfiguration.beamWidth
                        + sign * RenderingConfiguration.gapBetweenBeams * beam.number);
                if (beam.startIndex == beam.endIndex) {
                    // TODO: fix beamlets
                    if (beam.startIndex == 0) {
                        Anchor leftAnchor = chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd();
                        Anchor rightAnchor = canvas.interpolateAnchors(leftAnchor,
                                chordAnchorsMap.get(chords.get(beam.startIndex + 1)).stemEnd(), 0.5f);
                        canvas.drawBeam(leftAnchor, 0, beamOffset, rightAnchor, 0, beamOffset,
                                RenderingConfiguration.beamWidth);
                    } else if (beam.endIndex == chords.size() - 1) {
                        Anchor rightAnchor = chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd();
                        Anchor leftAnchor = canvas.interpolateAnchors(rightAnchor,
                                chordAnchorsMap.get(chords.get(beam.endIndex - 1)).stemEnd(), 0.5f);
                        canvas.drawBeam(leftAnchor, 0, beamOffset, rightAnchor, 0, beamOffset,
                                RenderingConfiguration.beamWidth);
                    }
                } else {
                    canvas.drawBeam(
                            chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                            0,
                            beamOffset,
                            chordAnchorsMap.get(chords.get(beam.endIndex)).stemEnd(), 0,
                            beamOffset,
                            RenderingConfiguration.beamWidth);
                }
            }

        }
        for (Chord chord : chords) {
            chord.draw(canvas, chordAnchorsMap);
        }
    }
}
