package uk.ac.cam.optimisingmusicnotation.mxlparser;

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
