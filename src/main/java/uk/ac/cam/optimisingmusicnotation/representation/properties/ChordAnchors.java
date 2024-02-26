package uk.ac.cam.optimisingmusicnotation.representation.properties;

public record ChordAnchors<Anchor>(Anchor notehead, Anchor stemEnd, float noteheadOffset, float stemEndOffset) {
}
