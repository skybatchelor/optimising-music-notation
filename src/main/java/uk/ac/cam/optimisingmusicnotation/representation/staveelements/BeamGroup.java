package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A collection of chords beamed together.
 * Its primary responsibility is to alter the stems of chords in the middle of the group to meet the beam.
 * Beam groups have an implicit beam connecting all the notes.
 */
public class BeamGroup implements StaveElement {
    /**
     * Represents a secondary beam.
     */
    private static class Beam {
        /** The index of the chord in the beam group the beam starts at */
        int startIndex;
        /** The index of the chord in the beam group the beam ends at */
        int endIndex;
        // Zero indexed beams
        /** The beam number. The 0th beam is the implicit beam between all the notes */
        int number;

        public Beam(int startIndex, int endIndex, int number) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.number = number;
        }
    }

    /** The chords in the beam group */
    private final List<Chord> chords;
    /** The list of secondary beams. Note that beam groups always have an implicit first level beam */
    private final List<Beam> beams;

    public BeamGroup() {
        chords = new ArrayList<>();
        beams = new ArrayList<>();
    }

    /**
     * Creates a BeamGroup with a given list of chords.
     * @param chords the chords in the beam group
     */
    public BeamGroup(List<Chord> chords) {
        this.chords = chords;
        beams = new ArrayList<>();
    }

    /**
     * Add a secondary beam to the beam group.
     * @param startIndex the index of the chord which starts the secondary beam
     * @param endIndex the index of the chord which ends the secondary beam
     * @param number the number of the beam
     */
    public void addBeam(int startIndex, int endIndex, int number) {
        beams.add(new Beam(startIndex, endIndex, number));
    }

    /**
     * Draws the beam group.
     * It does this by first drawing the start and end chord, after which it modifies the middle chords' stem end anchors to account for the beam.
     * It the draws all the secondary beams.
     * @param canvas the canvas being used to render the score
     * @param chordAnchorsMap the anchor map the chords are putting the anchors into
     * @param <Anchor> the anchor type used by the canvas
     */
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
            // adjust middle chord anchors to account for the beam
            for (Chord chord : chords.subList(1, chords.size() - 1)) {
                ChordAnchors<Anchor> chordAnchors = chordAnchorsMap.get(chord);
                float interpolatePos = (chord.getMusicalPosition().crotchetsIntoLine()
                        - firstChord.getMusicalPosition().crotchetsIntoLine())
                        / (lastChord.getMusicalPosition().crotchetsIntoLine()
                        - firstChord.getMusicalPosition().crotchetsIntoLine());

                Anchor interpolatedAnchor = canvas.interpolateAnchors(firstChordAnchors.stemEnd(), lastChordAnchors.stemEnd(), interpolatePos);
                ChordAnchors<Anchor> middleChordAnchors = new ChordAnchors<>(chordAnchors.lowestNotehead(), chordAnchors.highestNotehead(),
                        interpolatedAnchor, chordAnchors.noteheadOffset(), chordAnchors.stemEndOffset());
                chordAnchorsMap.put(chord, middleChordAnchors);
            }

            // draw chords now to avoid beams being covered by whitespace
            for (Chord chord : chords) {
                chord.draw(canvas, chordAnchorsMap);
            }

            canvas.drawBeam(
                    firstChordAnchors.stemEnd(),
                    0, -sign * RenderingConfiguration.beamOffset,
                    lastChordAnchors.stemEnd(),
                    0, -sign * RenderingConfiguration.beamOffset,
                    RenderingConfiguration.beamWidth);

            for (Beam beam : beams) {
                float beamOffset = -(sign * beam.number * RenderingConfiguration.beamWidth
                        + sign * RenderingConfiguration.gapBetweenBeams * beam.number + sign * RenderingConfiguration.beamOffset);
                if (beam.startIndex == beam.endIndex) {
                    // canvas.drawBeam(chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                    // -1, 3.125f - 1f * beam.number,
                    // chordAnchorsMap.get(chords.get(beam.endIndex)).stemEnd(), 0, 3.125f - 0.8f *
                    // beam.number,
                    // 0.75f);
                    if (beam.startIndex == 0) {
                        if (RenderingConfiguration.hookStart) {
                            canvas.drawBeam(
                                    canvas.interpolateAnchors(
                                            chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                            chordAnchorsMap.get(chords.get(beam.startIndex + 1)).stemEnd(),
                                            RenderingConfiguration.hookRatio
                                    ),
                                    0,
                                    beamOffset,
                                    chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                    0,
                                    beamOffset,
                                    RenderingConfiguration.beamWidth);
                        }
                    } else if (beam.startIndex == chords.size() - 1) {
                        if (RenderingConfiguration.hookEnd) {
                            canvas.drawBeam(
                                    canvas.interpolateAnchors(
                                            chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                            chordAnchorsMap.get(chords.get(beam.startIndex - 1)).stemEnd(),
                                            RenderingConfiguration.hookRatio * chords.get(beam.startIndex).getDurationInCrotchets()
                                                    / chords.get(beam.startIndex - 1).getDurationInCrotchets()
                                    ),
                                    0,
                                    beamOffset,
                                    chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                    0,
                                    beamOffset,
                                    RenderingConfiguration.beamWidth);
                        }
                    } else {
                        if (RenderingConfiguration.hookSingleLeft) {
                            canvas.drawBeam(
                                    canvas.interpolateAnchors(
                                            chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                            chordAnchorsMap.get(chords.get(beam.startIndex - 1)).stemEnd(),
                                            RenderingConfiguration.hookRatio * chords.get(beam.startIndex).getDurationInCrotchets()
                                                    / chords.get(beam.startIndex - 1).getDurationInCrotchets()
                                    ),
                                    0,
                                    beamOffset,
                                    chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                    0,
                                    beamOffset,
                                    RenderingConfiguration.beamWidth);
                        }
                        if (RenderingConfiguration.hookSingleRight) {
                            canvas.drawBeam(
                                    canvas.interpolateAnchors(
                                            chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                            chordAnchorsMap.get(chords.get(beam.startIndex + 1)).stemEnd(),
                                            RenderingConfiguration.hookRatio
                                    ),
                                    0,
                                    beamOffset,
                                    chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                    0,
                                    beamOffset,
                                    RenderingConfiguration.beamWidth);
                        }
                    }
                } else {
                    Anchor start = chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd();
                    Anchor end = chordAnchorsMap.get(chords.get(beam.endIndex)).stemEnd();
                    if (RenderingConfiguration.hookAllLeft && beam.startIndex != 0) {
                        start = canvas.interpolateAnchors(
                                chordAnchorsMap.get(chords.get(beam.startIndex)).stemEnd(),
                                chordAnchorsMap.get(chords.get(beam.startIndex - 1)).stemEnd(),
                                RenderingConfiguration.hookRatio * chords.get(beam.startIndex).getDurationInCrotchets()
                                        / chords.get(beam.startIndex - 1).getDurationInCrotchets()
                        );
                    }
                    if (RenderingConfiguration.hookAllRight && beam.endIndex != chords.size() - 1) {
                        end = canvas.interpolateAnchors(
                                chordAnchorsMap.get(chords.get(beam.endIndex)).stemEnd(),
                                chordAnchorsMap.get(chords.get(beam.endIndex + 1)).stemEnd(),
                                RenderingConfiguration.hookRatio
                        );
                    }
                    canvas.drawBeam(
                            start,
                            0,
                            beamOffset,
                            end,
                            0,
                            beamOffset,
                            RenderingConfiguration.beamWidth);
                }
            }
        }
        else {
            chords.get(0).draw(canvas, chordAnchorsMap);
        }

//        if (chords.size() == 1) {
//            Anchor note = chords.get(0).drawRetAnchor(canvas);
//            canvas.drawBeam(note, -1, 3, 0, 3, 0.75f);
//        } else {
//            List<Anchor> anchors = new ArrayList<Anchor>();
//            Anchor start = chords.get(0).drawRetAnchor(canvas);
//            Anchor end = chords.get(chords.size() - 1).drawRetAnchor(canvas);
//            anchors.add(start);
//            canvas.drawBeam(start, 0, 3.125f, end, 0, 3.125f, 0.75f);
//            for (Chord chord : chords.subList(1, chords.size() - 1)) {
//                anchors.add(chord.drawRetAnchor(canvas));
//            }
//            anchors.add(end);
//            for (Beam beam : beams) {
//                if (beam.startIndex == beam.endIndex) {
//                    canvas.drawBeam(anchors.get(beam.startIndex), -1, 3.125f - 1f * beam.number,
//                            anchors.get(beam.endIndex), 0, 3.125f - 0.8f * beam.number, 0.75f);
//                } else {
//                    canvas.drawBeam(anchors.get(beam.startIndex), 0, 3.125f - 1f * beam.number,
//                            anchors.get(beam.endIndex), 0, 3.125f - 1f * beam.number, 0.75f);
//                }
//            }
//        }
    }
}
