package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;

class TempoTuple {
    float time;
    NoteType leftItem;
    int leftDots;
    String rightText;
    NoteType rightItem;
    int rightDots;

    float bpmValue;

    static float dotsToFactor(int dots) {
        switch (dots) {
            case 0 -> {
                return 1;
            }
            case 1 -> {
                return 1.5f;
            }
            case 2 -> {
                return 1.75f;
            }
            case 3 -> {
                return 1.875f;
            }
            case 4 -> {
                return 1.9375f;
            }
        }
        return 1;
    }

    public TempoTuple(NoteType leftItem, int leftDots, String rightText, float time) {
        this.time = time;
        this.leftItem = leftItem;
        this.leftDots = leftDots;
        this.rightText = rightText;
        this.rightItem = NoteType.MAXIMA;
        this.rightDots = 0;

        bpmValue = Float.parseFloat(rightText) * (leftItem.defaultLengthInCrotchets * dotsToFactor(leftDots));
    }


    public TempoTuple(NoteType leftItem, int leftDots, NoteType rightItem, int rightDots, float currentTempo, float time) {
        this.time = time;
        this.leftItem = leftItem;
        this.leftDots = leftDots;
        this.rightText = null;
        this.rightItem = rightItem;
        this.rightDots = rightDots;

        bpmValue = currentTempo / (leftItem.defaultLengthInCrotchets * dotsToFactor(leftDots)) * (rightItem.defaultLengthInCrotchets * dotsToFactor(rightDots));
    }

    public InstantiatedTempoTuple toInstantiatedTempoTuple(float lineStart) {
        return new InstantiatedTempoTuple(leftItem, leftDots, rightText, rightItem, rightDots, time - lineStart, bpmValue);
    }
}
