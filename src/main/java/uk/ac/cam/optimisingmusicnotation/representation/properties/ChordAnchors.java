package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

/**
 * A record for tracking the anchors associated with a given note
 * @param lowestNotehead the anchor for the lowest notehead in the chord
 * @param highestNotehead the anchor for the highest notehead in the chord
 * @param stemEnd the anchor for the end of the stem
 * @param noteheadOffset the offset from the notehead, for use by chord markings
 * @param stemEndOffset the offset from the end of the stem, for use by chord markings
 * @param <Anchor> the type of Anchor being stored, dependent on the canvas implementation being used
 */
public record ChordAnchors<Anchor>(Anchor lowestNotehead, Anchor highestNotehead, Anchor stemEnd, float noteheadOffset, float stemEndOffset) {
    public ChordAnchors<Anchor> withNoteheadOffset(float noteheadOffset) {
        return new ChordAnchors<>(lowestNotehead(), highestNotehead(), stemEnd(), noteheadOffset, stemEndOffset());
    }
    // farthest notehead from stemEnd
    public Anchor notehead() {
        return RenderingConfiguration.upwardStems ? lowestNotehead() : highestNotehead();
    }

    public Anchor getLowestAnchor(MusicCanvas<Anchor> canvas, Chord chord) {
        if (chord.getNoteType().defaultLengthInCrotchets <= 2) {
            if (canvas.isAnchorBelow(lowestNotehead, stemEnd)) {
                return lowestNotehead;
            } else {
                return stemEnd;
            }
        } else {
            return lowestNotehead;
        }
    }

    public Anchor getHighestAnchor(MusicCanvas<Anchor> canvas, Chord chord) {
        if (chord.getNoteType().defaultLengthInCrotchets <= 2) {
            if (canvas.isAnchorAbove(highestNotehead, stemEnd)) {
                return highestNotehead;
            } else {
                return stemEnd;
            }
        } else {
            return highestNotehead;
        }
    }
}
