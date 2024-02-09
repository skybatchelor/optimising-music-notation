package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.util.Marshalling;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Section;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.BeamGroup;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;
import java.lang.String;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Parser {
    private static class ChordTuple {
        List<Note> notes;
        float crochets;
        int lowestLine;

        public ChordTuple(float crochets, int lowestLine) {
            notes = new ArrayList<>();
            this.crochets = crochets;
            this.lowestLine = lowestLine;
        }
    }

    private static class BeamTuple {
        List<ChordTuple> chords;

        public BeamTuple() {
            chords = new ArrayList<>();
        }
    }

    public static Score parseToScore(Object mxl) {
        if (mxl instanceof ScorePartwise partwise) {
            TreeMap<Float, ChordTuple> chords = new TreeMap<>();
            TreeMap<String, List<BeamTuple>> beamGroups = new TreeMap<>();
            TreeMap<Float, Direction> directions = new TreeMap<>();
            TreeSet<Float> newlines = new TreeSet<>() {{ add(0f); }};
            TreeMap<String, Part> parts = new TreeMap<>();

            List<Object> scoreParts = partwise.getPartList().getPartGroupOrScorePart();
            for (Object part : scoreParts) {
                if (part instanceof ScorePart) {
                    ScorePart scorePart = (ScorePart) part;
                    Part ret = new Part();
                    parts.put(scorePart.getId(), ret);
                    ret.setName(scorePart.getPartName().getValue());
                    ret.setAbbreviation(scorePart.getPartAbbreviation().getValue());
                } else if (part instanceof PartGroup) {
                    PartGroup partGroup = (PartGroup) part;
                } else {

                }
            }

            float totalLength = 0;

            TreeMap<String, List<List<BeamGroup>>> partLines = new TreeMap<>();
            List<ScorePartwise.Part> musicParts = partwise.getPart();
            for (ScorePartwise.Part part : musicParts) {
                String partId = "";
                if (part.getId() instanceof ScorePart scorePart) {
                    partId = scorePart.getId();
                }
                partLines.put(partId, new ArrayList<>());
                beamGroups.put(partId, new ArrayList<>());
                float measureStartTime = 0;
                TimeSignature currentTimeSignature = new TimeSignature();
                int divisions = 0;
                float prevChange = 0;
                int lowestLineGrandStaveLine = 0;
                List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> currentChordPitches = new ArrayList<>();
                List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> currentChordAccidentals = new ArrayList<>();
                ChordTuple currentChord = new ChordTuple(0, 0);
                BeamTuple beamGroup = new BeamTuple();
                List<ScorePartwise.Part.Measure> measures = part.getMeasure();

                for (ScorePartwise.Part.Measure measure : measures) {
                    float measureTime = 0;
                    for(Object component : measure.getNoteOrBackupOrForward()) {
                        if (component instanceof Attributes attributes) {
                            if (attributes.getTime() != null) {
                                for(Time time : attributes.getTime()) {
                                    var timeSignature = parseTimeSignature(time.getTimeSignature());
                                    if (timeSignature != null) {
                                        currentTimeSignature = timeSignature;
                                    }
                                }
                            }
                            if (attributes.getClef() != null) {
                                for(org.audiveris.proxymusic.Clef clef : attributes.getClef()) {
                                    Clef parsed = parseClef(clef);
                                    lowestLineGrandStaveLine = clefToLowestLineGrandStaveLine(parsed);
                                    currentChord.lowestLine = lowestLineGrandStaveLine;
                                }
                            }
                            if (attributes.getDivisions() != null) {
                                divisions = attributes.getDivisions().intValue();
                            }
                        } else if (component instanceof Note note) {
                            if (note.getDuration() != null) {
                                prevChange = note.getDuration().intValue() / (float)divisions;
                            } else {
                                prevChange = 0;
                            }
                            if (note.getChord() == null) {
                                if (currentChord.notes.size() != 0) {
                                    chords.put(measureStartTime + measureTime, currentChord);
                                }
                                measureTime += prevChange;
                                currentChord = new ChordTuple(measureStartTime + measureTime, lowestLineGrandStaveLine);
                                currentChord.notes.add(note);
                            } else {
                                currentChord.notes.add(note);
                            }
                            boolean addedToBeamGroup = false;
                            if (note.getBeam() != null) {
                                for(Beam beam : note.getBeam()) {
                                    if (beam.getNumber() == 1) {
                                        switch (beam.getValue()) {
                                            case BEGIN:
                                                beamGroup.chords.add(currentChord);
                                                addedToBeamGroup = true;
                                                break;
                                            case END:
                                                beamGroup.chords.add(currentChord);
                                                beamGroups.get(partId).add(beamGroup);
                                                beamGroup = new BeamTuple();
                                                addedToBeamGroup = true;
                                                break;
                                            case CONTINUE:
                                                beamGroup.chords.add(currentChord);
                                                addedToBeamGroup = true;
                                                break;
                                            case FORWARD_HOOK:
                                                beamGroup.chords.add(currentChord);
                                                beamGroups.get(partId).add(beamGroup);
                                                beamGroup = new BeamTuple();
                                                addedToBeamGroup = true;
                                                break;
                                            case BACKWARD_HOOK:
                                                beamGroup.chords.add(currentChord);
                                                beamGroups.get(partId).add(beamGroup);
                                                beamGroup = new BeamTuple();
                                                addedToBeamGroup = true;
                                                break;
                                        }
                                    }
                                }
                            }
                            if (!addedToBeamGroup && note.getChord() == null) {
                                beamGroup.chords.add(currentChord);
                                beamGroups.get(partId).add(beamGroup);
                                beamGroup = new BeamTuple();
                            }
                        } else if (component instanceof Backup backup) {
                            measureTime -= backup.getDuration().intValue() / (float) divisions;
                        } else if (component instanceof Direction direction) {
                            float offset = 0;
                            if (direction.getOffset() != null) {
                                offset = direction.getOffset().getValue().intValue() / (float) divisions;
                            }
                            if (isNewline(direction)) {
                                newlines.add(measureStartTime + measureTime + offset);
                            }
                            directions.put(measureStartTime + measureTime + offset, direction);
                        }
                    }
                    measureStartTime += currentTimeSignature.beatNum * 4f / (currentTimeSignature.beatType);
                }
                totalLength = Math.max(measureStartTime, totalLength);
            }

            Map<Float, Integer> lineIndices = new HashMap<>();
            List<Float> lineLengths = new ArrayList<>();
            int index = 0;
            float prevLineStart = 0;
            for (Iterator<Float> it = newlines.iterator(); it.hasNext(); ) {
                Float newline = it.next();
                lineIndices.put(newline, index);
                if (index != 0) {
                    lineLengths.add(newline - prevLineStart);
                    prevLineStart = newline;
                }
                for (List<List<BeamGroup>> partList : partLines.values()) {
                    partList.add(new ArrayList<>());
                }
                ++index;
            }
            lineLengths.add(totalLength - prevLineStart);

            for (String partId : partLines.navigableKeySet()) {
                List<BeamTuple> partNotes = beamGroups.get(partId);
                for (BeamTuple beam : partNotes) {
                    float lineStart = newlines.floor(beam.chords.get(0).crochets);
                    int lineNum = lineIndices.get(lineStart);
                    partLines.get(partId).get(lineNum).add(beamTupleToBeamGroup(beam, lineStart, lineNum));
                }
            }
            TreeMap<String, List<Line>> finalLines = new TreeMap<>();

            for (Map.Entry<String, List<List<BeamGroup>>> part : partLines.entrySet()) {
                finalLines.put(part.getKey(), new ArrayList<>());
                for (int i = 0; i < part.getValue().size(); ++i) {
                    List<BeamGroup> line = part.getValue().get(i);
                    List<StaveElement> elements = new ArrayList<>(line);
                    Stave stave = new Stave(elements, new ArrayList<>());
                    finalLines.get(part.getKey()).add(new Line(new ArrayList<>() {{ add(stave); }}, lineLengths.get(i), 0));
                }
            }
            TreeMap<String, List<Section>> sections = new TreeMap<>();

            for (Map.Entry<String, List<Line>> part : finalLines.entrySet()) {
                List<Section> partSections = new ArrayList<>();
                for (Line line : part.getValue()) {
                    partSections.add(new Section(line));
                }
                sections.put(part.getKey(), partSections);
            }

            for (Map.Entry<String, List<Section>> part : sections.entrySet()) {
                parts.get(part.getKey()).sections = part.getValue();
            }

            return new Score(getWorkTitle(partwise), parts.values().stream().toList());
        }
        return null;
    }

    static String getWorkTitle(ScorePartwise score) {
        if (score.getWork() != null && score.getWork().getWorkTitle() != null) {
            return score.getWork().getWorkTitle();
        } else {
            return "";
        }
    }

    static BeamGroup beamTupleToBeamGroup(BeamTuple tuple, float lineTime, int lineNum) {
        List<BeamGroup> chords = new ArrayList<>();
        for (ChordTuple chordTuple : tuple.chords) {
            chords.add(chordTupleToChord(chordTuple, lineTime, lineNum));
        }
        return new BeamGroup(chords);
    }

    static Chord chordTupleToChord(ChordTuple chord, float lineTime, int lineNum) {
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches = new ArrayList<>();
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals = new ArrayList<>();
        for (Note note : chord.notes) {
            if (note.getPitch() != null) {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(pitchToGrandStaveLine(note.getPitch()) - chord.lowestLine, 0));
            } else {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(0, 0));
            }
            accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NONE);
        }
        return new Chord(pitches, accidentals, chord.crochets - lineTime);
    }

    // translates a pitch into the number of lines above the root of A0.
    static int pitchToGrandStaveLine(Pitch pitch) {
        int line = 0;
        switch (pitch.getStep()) {
            case A -> { }
            case B -> { line += 1; }
            case C -> { line += 2; }
            case D -> { line += 3; }
            case E -> { line += 4; }
            case F -> { line += 5; }
            case G -> { line += 6; }
        }
        line += 7 * pitch.getOctave();
        return line;
    }

    static int pitchToGrandStaveLine(Step step, int octave) {
        int line = 0;
        switch (step) {
            case A -> { }
            case B -> { line += 1; }
            case C -> { line += 2; }
            case D -> { line += 3; }
            case E -> { line += 4; }
            case F -> { line += 5; }
            case G -> { line += 6; }
        }
        line += 7 * octave;
        return line;
    }

    static boolean isNewline(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId) {
                            if (((FormattedTextId) wordOrSymbol).getValue().equals("\\n")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static TimeSignature parseTimeSignature(List<JAXBElement<String>> timeSignature) {
        if (timeSignature != null) {
            TimeSignature ret = new TimeSignature();
            for (JAXBElement<String> timeElement : timeSignature) {
                if (timeElement.getName().getLocalPart().equals("beats")) {
                    ret.setBeatNum(Integer.valueOf(timeElement.getValue()));
                } else if (timeElement.getName().getLocalPart().equals("beat-type")) {
                    ret.setBeatType(Integer.valueOf(timeElement.getValue()));
                }
            }
            return ret;
        }
        return null;
    }

    static int clefToLowestLineGrandStaveLine(Clef clef) {
        int lowestLineGrandStaveLine = 0;
        switch (clef.getClefType()) {
            case C -> { lowestLineGrandStaveLine = pitchToGrandStaveLine(Step.C, 4); }
            case F -> { lowestLineGrandStaveLine = pitchToGrandStaveLine(Step.F, 3); }
            case G -> { lowestLineGrandStaveLine = pitchToGrandStaveLine(Step.G, 4); }
        }
        lowestLineGrandStaveLine += 7 * clef.getOctaveOffset();
        lowestLineGrandStaveLine -= clef.getStaveLine();
        return lowestLineGrandStaveLine;
    }

    static Clef parseClef(org.audiveris.proxymusic.Clef clef) {
        int octaveOffset = 0;
        if (clef.getClefOctaveChange() != null) {
            octaveOffset = clef.getClefOctaveChange().intValue();
        }
        int staveLine = 0;
        if (clef.getLine() != null) {
            staveLine = clef.getLine().intValue();
        }
        switch (clef.getSign()) {
            case C -> {
                return new Clef(Clef.ClefType.C, staveLine, octaveOffset);
            }
            case F -> {
                return new Clef(Clef.ClefType.F, staveLine, octaveOffset);
            }
            case G -> {
                return new Clef(Clef.ClefType.G, staveLine, octaveOffset);
            }
            case PERCUSSION -> {
                return new Clef(Clef.ClefType.G, 0, 0);
            }
        }
        throw new IllegalArgumentException("Unknown clef symbol");
    }

    public static Object openMXL(String input) {
        try (ZipInputStream xml = new ZipInputStream(new FileInputStream(input))) {
            ZipEntry zipEntry = xml.getNextEntry();
            while (zipEntry != null) {
                if(zipEntry.getName().equals("score.xml")) {
                    return Marshalling.unmarshal(xml);
                }
                zipEntry = xml.getNextEntry();
            }
            return null;
        } catch (Exception e) {

        }
        return null;
    }

    private Parser() {}

    public static void main(String[] args) {
        Object mxl = Parser.openMXL("TestScore.mxl");
        System.out.println(mxl.toString());
        Score score = Parser.parseToScore(mxl);
    }
}
