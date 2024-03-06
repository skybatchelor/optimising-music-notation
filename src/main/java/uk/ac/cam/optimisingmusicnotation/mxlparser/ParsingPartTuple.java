package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.Direction;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Clef;
import uk.ac.cam.optimisingmusicnotation.representation.properties.KeySignature;

import java.util.*;

/**
 * Holds all the information for a part as it is being parsed.
 */
class ParsingPartTuple {
    public void putInBeamGroup(BeamGroupTuple beamGroup) {
        Util.putInMapInMapMap(staveBeamGroups, HashMap::new, TreeMap::new, beamGroup.staff, beamGroup.voice, beamGroup.startTime, beamGroup);
    }

    HashMap<Integer, HashMap<Integer, TreeMap<Float, BeamGroupTuple>>> staveBeamGroups;

    public void putInMusicGroup(MusicGroupTuple musicGroup) {
        Util.addToListInMap(staveMusicGroups, musicGroup.staff, musicGroup);
    }

    HashMap<Integer, List<MusicGroupTuple>> staveMusicGroups;
    //List<BeamGroupTuple> beamGroups;
    List<PulseLineTuple> pulseLines;
    //List<MusicGroupTuple> musicGroups;

    HashMap<Integer, HashMap<Integer, TreeMap<Float, ChordTuple>>> chordTuples;
    TreeMap<Float, Direction> directions;

    public void putInClef(int stave, float time, Clef clef) {
        Util.putInMapInMap(clefs, TreeMap::new, stave, time, clef);
    }
    HashMap<Integer, TreeMap<Float, Clef>> clefs;
    TreeMap<Float, KeySignature> keySignatures;

    void addCapital(int staff, int voice, Float time) {
        Util.addToTreeSetInMapMap(capitalNotes, HashMap::new, staff, voice, time);
    }

    HashMap<Integer, HashMap<Integer, TreeSet<Float>>> capitalNotes;

    void addNextCapital(int staff, int voice, Float time) {
        Util.addToTreeSetInMapMap(capitalNotes, HashMap::new, staff, voice, time);
    }

    HashMap<Integer, HashMap<Integer, TreeSet<Float>>> capitalNextNotes;

    TreeSet<Float> globalCapitalNotes = new TreeSet<>();
    TreeSet<Float> globalCapitalNextNotes = new TreeSet<>();

    public void putInArtisticWhitespace(int staff, int voice, float time) {
        Util.addToTreeSetInMapMap(artisticWhitespace, HashMap::new, staff, voice, time);
    }

    HashMap<Integer, HashMap<Integer, TreeSet<Float>>> artisticWhitespace;

    boolean upwardsStems;
    public ParsingPartTuple() {
        staveBeamGroups = new HashMap<>();
        staveMusicGroups = new HashMap<>();
        //beamGroups = new ArrayList<>();
        pulseLines = new ArrayList<>();
        //musicGroups = new ArrayList<>();
        chordTuples = new HashMap<>();
        directions = new TreeMap<>();
        clefs = new HashMap<>();
        keySignatures = new TreeMap<>();
        capitalNotes = new HashMap<>();
        capitalNextNotes = new HashMap<>();
        artisticWhitespace = new HashMap<>();
    }
}
