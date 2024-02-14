package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

public enum NoteType {
    MAXIMA(16f), BREVE(8f), SEMIBREVE(4f), MINIM(2f), CROTCHET(1f),
    QUAVER(0.5f), SQUAVER(0.25f), DSQUAVER(0.125f), HDSQUAVER(0.6125f);

    public float defaultLengthInCrotchets;

    NoteType(float defaultLengthInCrotchets) {
        this.defaultLengthInCrotchets = defaultLengthInCrotchets;
    }
}
