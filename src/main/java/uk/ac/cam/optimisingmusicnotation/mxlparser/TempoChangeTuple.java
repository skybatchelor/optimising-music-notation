package uk.ac.cam.optimisingmusicnotation.mxlparser;

/**
 * A record for tracking the integrated time changes.
 * @param crotchets the crotchets form the start of the piece the tempo change occurs at
 * @param time the integrated time the tempo change occurs at
 * @param factor the new time factor
 */
record TempoChangeTuple(float crotchets, float time, float factor) {
    @Override
    public float time() {
        return time;
    }

    public float factor() {
        return factor;
    }

    @Override
    public float crotchets() { return crotchets; }

    public float modulateTime(float crotchets) {
        return (crotchets - this.crotchets) * factor + this.time;
    }

}
