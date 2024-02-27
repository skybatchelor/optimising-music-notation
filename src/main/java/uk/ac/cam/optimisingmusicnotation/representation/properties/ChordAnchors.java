package uk.ac.cam.optimisingmusicnotation.representation.properties;

public record ChordAnchors<Anchor>(Anchor notehead, Anchor stemEnd, float noteheadOffset, float stemEndOffset) {
    public ChordAnchors<Anchor> withNoteheadOffset(float noteheadOffset) {
        return new ChordAnchors<>(notehead(), stemEnd(), noteheadOffset, stemEndOffset());
    }
}
