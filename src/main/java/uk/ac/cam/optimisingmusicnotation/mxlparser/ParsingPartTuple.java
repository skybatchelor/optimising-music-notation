package uk.ac.cam.optimisingmusicnotation.mxlparser;

import com.sun.source.tree.Tree;
import org.audiveris.proxymusic.Direction;
import uk.ac.cam.optimisingmusicnotation.representation.properties.KeySignature;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

class ParsingPartTuple {
    TreeMap<Integer, TreeMap<Integer, List<BeamGroupTuple>>> staveBeamGroups;
    TreeMap<Integer,  List<MusicGroupTuple>> staveMusicGroups;
    //List<BeamGroupTuple> beamGroups;
    List<PulseLineTuple> pulseLines;
    //List<MusicGroupTuple> musicGroups;

    TreeMap<Float, ChordTuple> chordTuples;
    TreeMap<Float, Direction> directions;
    TreeMap<Float, uk.ac.cam.optimisingmusicnotation.representation.properties.Clef> clefs;
    TreeMap<Float, KeySignature> keySignatures;

    TreeMap<Integer, TreeMap<Integer, TreeSet<Float>>> artisticWhitespace;

    boolean upwardsStems;
    public ParsingPartTuple() {
        staveBeamGroups = new TreeMap<>() {{ put(1, new TreeMap<>() {{ put(1, new ArrayList<>()); }}); }};
        staveMusicGroups = new TreeMap<>() {{ put(1, new ArrayList<>()); }};
        //beamGroups = new ArrayList<>();
        pulseLines = new ArrayList<>();
        //musicGroups = new ArrayList<>();
        chordTuples = new TreeMap<>();
        directions = new TreeMap<>();
        clefs = new TreeMap<>();
        keySignatures = new TreeMap<>();

        artisticWhitespace = new TreeMap<>();
    }

    public void putInBeamGroup(BeamGroupTuple beamGroup) {
        Util.addToListInMapMap(staveBeamGroups, TreeMap::new, beamGroup.staff, beamGroup.voice, beamGroup);
    }

    public void putInMusicGroup(MusicGroupTuple musicGroup) {
        Util.addToListInMap(staveMusicGroups, musicGroup.staff, musicGroup);
    }

    public void putInArtisticWhitespace(int staff, int voice, float time) {
        Util.addToTreeSetInMapMap(artisticWhitespace, TreeMap::new, staff, voice, time);
    }
}
