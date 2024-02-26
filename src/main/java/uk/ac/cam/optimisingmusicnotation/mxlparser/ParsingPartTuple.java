package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.Direction;
import uk.ac.cam.optimisingmusicnotation.representation.properties.KeySignature;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

class ParsingPartTuple {
    List<BeamGroupTuple> beamGroups;
    List<PulseLineTuple> pulseLines;
    List<MusicGroupTuple> musicGroups;

    TreeMap<Float, ChordTuple> chordTuples;
    TreeMap<Float, Direction> directions;
    TreeMap<Float, uk.ac.cam.optimisingmusicnotation.representation.properties.Clef> clefs;
    TreeMap<Float, KeySignature> keySignatures;

    public ParsingPartTuple() {
        beamGroups = new ArrayList<>();
        pulseLines = new ArrayList<>();
        musicGroups = new ArrayList<>();
        chordTuples = new TreeMap<>();
        directions = new TreeMap<>();
        clefs = new TreeMap<>();
        keySignatures = new TreeMap<>();
    }
}
