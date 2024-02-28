package uk.ac.cam.optimisingmusicnotation.mxlparser;

record TempoChangeTuple(float time, float factor) {
    @Override
    public float time() {
        return time;
    }

    public float factor() {
        return factor;
    }
}
