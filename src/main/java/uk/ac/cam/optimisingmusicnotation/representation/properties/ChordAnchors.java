package uk.ac.cam.optimisingmusicnotation.representation.properties;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

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
}
