package uk.ac.cam.optimisingmusicnotation.mxlparser;

/**
 * Used to as a tag when tracking which chords need beamlets/flags.
 * @param number the number of beams the beamlet/flag needs
 * @param flag whether a beamlet or a flag is being added
 */
record BeamletInfo(int number, boolean flag) {
}
