package uk.ac.cam.optimisingmusicnotation.representation.properties;

public record ChordAnchors<Anchor>(Anchor lowestNotehead, Anchor highestNotehead, Anchor stemEnd, float noteheadOffset, float stemEndOffset) {
}
