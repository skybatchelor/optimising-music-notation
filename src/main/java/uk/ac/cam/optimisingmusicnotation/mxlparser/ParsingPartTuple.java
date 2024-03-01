package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.Direction;
import uk.ac.cam.optimisingmusicnotation.representation.properties.KeySignature;

import java.util.*;

class ParsingPartTuple {
    HashMap<Integer, HashMap<Integer, TreeMap<Float, BeamGroupTuple>>> staveBeamGroups;
    HashMap<Integer, List<MusicGroupTuple>> staveMusicGroups;
    //List<BeamGroupTuple> beamGroups;
    List<PulseLineTuple> pulseLines;
    //List<MusicGroupTuple> musicGroups;

    TreeMap<Float, ChordTuple> chordTuples;
    TreeMap<Float, Direction> directions;
    TreeMap<Float, uk.ac.cam.optimisingmusicnotation.representation.properties.Clef> clefs;
    TreeMap<Float, KeySignature> keySignatures;

    HashMap<Integer, HashMap<Integer, TreeSet<Float>>> artisticWhitespace;

    boolean upwardsStems;
    public ParsingPartTuple() {
        staveBeamGroups = new HashMap<>();
        staveMusicGroups = new HashMap<>();
        //beamGroups = new ArrayList<>();
        pulseLines = new ArrayList<>();
        //musicGroups = new ArrayList<>();
        chordTuples = new TreeMap<>();
        directions = new TreeMap<>();
        clefs = new TreeMap<>();
        keySignatures = new TreeMap<>();

        artisticWhitespace = new HashMap<>();
    }

    public void putInBeamGroup(BeamGroupTuple beamGroup) {
        Util.putInMapInMapMap(staveBeamGroups, HashMap::new, TreeMap::new, beamGroup.staff, beamGroup.voice, beamGroup.startTime, beamGroup);
    }

    public void putInMusicGroup(MusicGroupTuple musicGroup) {
        Util.addToListInMap(staveMusicGroups, musicGroup.staff, musicGroup);
    }

    public void putInArtisticWhitespace(int staff, int voice, float time) {
        Util.addToTreeSetInMapMap(artisticWhitespace, HashMap::new, staff, voice, time);
    }
}
