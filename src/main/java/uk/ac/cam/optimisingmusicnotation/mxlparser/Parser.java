package uk.ac.cam.optimisingmusicnotation.mxlparser;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.Clef;
import org.audiveris.proxymusic.Pitch;
import org.audiveris.proxymusic.util.Marshalling;
import uk.ac.cam.optimisingmusicnotation.rendering.PdfMusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.*;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BarLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BeatLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.PulseLine;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.BeamGroup;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.Accent;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.Staccato;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.StrongAccent;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.Tenuto;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Rest;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.String;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Parser {
    public static final boolean NEW_SECTION_FOR_KEY_SIGNATURE = true;

    private static class SplitChordTuple {
        List<InstantiatedChordTuple> pre;
        List<InstantiatedChordTuple> post;

        public SplitChordTuple() {
            pre = new ArrayList<>();
            post = new ArrayList<>();
        }
    }

    private static class ChordTuple {
        List<Note> notes;
        float crotchets;
        float duration;
        int lowestLine;

        public ChordTuple(float crochets, int lowestLine) {
            notes = new ArrayList<>();
            this.crotchets = crochets;
            this.lowestLine = lowestLine;
        }
    }

    private static class InstantiatedChordTuple {
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches;
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals;
        float crotchetsIntoLine;
        float duration;
        uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType;
        int dots;
        List<ChordMarking> markings;

        public InstantiatedChordTuple(List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches, List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals,
                                      float crotchetsIntoLine, float duration, uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType, int dots, List<ChordMarking> markings) {
            this.pitches = pitches;
            this.accidentals = accidentals;
            this.crotchetsIntoLine = crotchetsIntoLine;
            this.duration = duration;
            this.noteType = noteType;
            this.dots = dots;
            this.markings = markings;
        }
    }

    private static class BeamTuple {
        int start;
        int end;
        int number;

        public BeamTuple(int start, int end, int number) {
            this.start = start; this.end = end; this.number = number;
        }
    }

    private static class BeamGroupTuple {
        private List<ChordTuple> chords;
        float startTime;
        float endTime;

        public BeamGroupTuple() {
            chords = new ArrayList<>();
            startTime = -1;
            endTime = -1;
        }

        public void addChord(ChordTuple chord) {
            chords.add(chord);
            if (startTime == -1 || chord.crotchets < startTime) {
                startTime = chord.crotchets;
            }
            if (endTime == -1 || chord.crotchets + chord.duration > endTime) {
                endTime = chord.crotchets + chord.duration;
            }
        }
    }

    private static class InstantiatedBeamGroupTuple {
        List<InstantiatedChordTuple> chords;
        List<BeamTuple> beams;

        public InstantiatedBeamGroupTuple() { chords = new ArrayList<>(); beams = new ArrayList<>(); }
    }

    private static class RestTuple {
        float startTime;
        float endTime;

        public RestTuple(float startTime, float endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }

    private static class PulseLineTuple {
        float time;
        String name;
        int beatWeight;
        TimeSignature timeSig;

        PulseLineTuple(float time, String name, int beatWeight, TimeSignature timeSig) {
            this.time = time;
            this.name = name;
            this.beatWeight = beatWeight;
            this.timeSig = timeSig;
        }
    }

    private static class InstantiatedPulseLineTuple {
        float timeInLine;
        String name;
        int beatWeight;
        TimeSignature timeSig;

        InstantiatedPulseLineTuple(float timeInLine, String name, int beatWeight, TimeSignature timeSig) {
            this.timeInLine = timeInLine;
            this.name = name;
            this.beatWeight = beatWeight;
            this.timeSig = timeSig;
        }
    }

    private static class ParsingPartTuple {
        List<BeamGroupTuple> beamGroups;
        List<PulseLineTuple> pulseLines;

        TreeMap<Float, Direction> directions;
        TreeMap<Float, uk.ac.cam.optimisingmusicnotation.representation.properties.Clef> clefs;
        TreeMap<Float, KeySignature> keySignatures;

        public ParsingPartTuple() {
            beamGroups = new ArrayList<>();
            pulseLines = new ArrayList<>();
            directions = new TreeMap<>();
            clefs = new TreeMap<>();
            keySignatures = new TreeMap<>();
        }
    }

    private static class LineTuple {
        float startTime;
        List<RestTuple> rests;
        List<InstantiatedPulseLineTuple> pulses;
        List<InstantiatedBeamGroupTuple> notes;

        LineTuple(float startTime) {
            this.startTime = startTime;
            rests = new ArrayList<>();
            pulses = new ArrayList<>();
            notes = new ArrayList<>();
        }
    }

    private static class InstantiatedLineTuple {
        float startTime;
        Line line;

        InstantiatedLineTuple(float startTime, Line line) {
            this.startTime = startTime;
            this.line = line;
        }
    }

    public static Score parseToScore(Object mxl) {
        if (mxl instanceof ScorePartwise partwise) {
            TreeMap<Float, Float> newlines = new TreeMap<>() {{ put(0f, 0f); }};
            TreeSet<Float> newSections = new TreeSet<>() {{ add(0f); }};
            TreeMap<String, Part> parts = new TreeMap<>();

            List<Object> scoreParts = partwise.getPartList().getPartGroupOrScorePart();
            for (Object part : scoreParts) {
                if (part instanceof ScorePart scorePart) {
                    Part ret = new Part();
                    parts.put(scorePart.getId(), ret);
                    ret.setName(scorePart.getPartName().getValue());
                    ret.setAbbreviation(scorePart.getPartAbbreviation().getValue());
                } else if (part instanceof PartGroup partGroup) {

                } else {

                }
            }

            float totalLength = 0;

            TreeMap<String, ParsingPartTuple> parsingParts = new TreeMap<>();
            TreeMap<String, List<LineTuple>> partLines = new TreeMap<>();

            TreeMap<String, List<TreeMap<Float, Line>>> partSections = new TreeMap<>();
            List<ScorePartwise.Part> musicParts = partwise.getPart();

            for (ScorePartwise.Part part : musicParts) {
                String partId = "";
                if (part.getId() instanceof ScorePart scorePart) {
                    partId = scorePart.getId();
                }
                ParsingPartTuple currentPart = new ParsingPartTuple();
                parsingParts.put(partId, currentPart);
                partLines.put(partId, new ArrayList<>());
                partSections.put(partId, new ArrayList<>());
                float measureStartTime = 0;
                TimeSignature currentTimeSignature = new TimeSignature();
                KeySignature currentKeySignature = new KeySignature();
                int divisions = 0;
                float prevChange;
                int lowestLineGrandStaveLine = 0;
                ChordTuple currentChord = new ChordTuple(0, 0);
                BeamGroupTuple beamGroup = new BeamGroupTuple();
                List<ScorePartwise.Part.Measure> measures = part.getMeasure();

                for (ScorePartwise.Part.Measure measure : measures) {
                    boolean newTimeSignature = false;
                    float measureTime = 0;
                    float measureLength = currentTimeSignature.getBeatNum() * 4f / (currentTimeSignature.getBeatType());

                    for(Object component : measure.getNoteOrBackupOrForward()) {
                        if (component instanceof Attributes attributes) {
                            if (attributes.getTime() != null) {
                                for(Time time : attributes.getTime()) {
                                    var timeSignature = parseTimeSignature(time.getTimeSignature());
                                    if (timeSignature != null) {
                                        currentTimeSignature = timeSignature;
                                        newTimeSignature = true;
                                    }
                                }
                            }
                            if (attributes.getKey() != null) {
                                for(Key key : attributes.getKey()) {
                                    var keySignature = parseKeySignature(key, currentKeySignature);
                                    if (keySignature != null) {
                                        currentKeySignature = keySignature;
                                        currentPart.keySignatures.put(measureStartTime + measureTime, currentKeySignature);
                                        if (NEW_SECTION_FOR_KEY_SIGNATURE) {
                                            newSections.add(measureStartTime + measureTime);
                                            newlines.put(measureStartTime + measureTime, measureTime - measureLength);
                                        }
                                    }
                                }
                            }
                            if (attributes.getClef() != null) {
                                for(org.audiveris.proxymusic.Clef clef : attributes.getClef()) {
                                    uk.ac.cam.optimisingmusicnotation.representation.properties.Clef parsed = parseClef(clef);
                                    lowestLineGrandStaveLine = clefToLowestLineGrandStaveLine(parsed);
                                    currentChord.lowestLine = lowestLineGrandStaveLine;
                                    currentPart.clefs.put(measureStartTime + measureTime, parsed);
                                }
                            }
                            if (attributes.getDivisions() != null) {
                                divisions = attributes.getDivisions().intValue();
                            }
                            measureLength = currentTimeSignature.getBeatNum() * 4f / (currentTimeSignature.getBeatType());
                        } else if (component instanceof Note note) {
                            if (note.getDuration() != null) {
                                prevChange = note.getDuration().intValue() / (float)divisions;
                            } else {
                                prevChange = 0;
                            }
                            if (note.getChord() == null) {
                                currentChord = new ChordTuple(measureStartTime + measureTime, lowestLineGrandStaveLine);
                                measureTime += prevChange;
                            }
                            currentChord.notes.add(note);
                            if (note.getDuration() != null) {
                                currentChord.duration = note.getDuration().intValue() / (float)divisions;
                            }
                            boolean addedToBeamGroup = false;
                            if (note.getBeam() != null) {
                                for(Beam beam : note.getBeam()) {
                                    if (beam.getNumber() == 1) {
                                        switch (beam.getValue()) {
                                            case BEGIN, CONTINUE -> {
                                                beamGroup.addChord(currentChord);
                                                addedToBeamGroup = true;
                                            }
                                            case END, FORWARD_HOOK, BACKWARD_HOOK -> {
                                                beamGroup.addChord(currentChord);
                                                currentPart.beamGroups.add(beamGroup);
                                                beamGroup = new BeamGroupTuple();
                                                addedToBeamGroup = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!addedToBeamGroup && note.getChord() == null) {
                                beamGroup.addChord(currentChord);
                                currentPart.beamGroups.add(beamGroup);
                                beamGroup = new BeamGroupTuple();
                            }
                        } else if (component instanceof Backup backup) {
                            measureTime -= backup.getDuration().intValue() / (float) divisions;
                        } else if (component instanceof Direction direction) {
                            float offset = 0;
                            if (direction.getOffset() != null) {
                                offset = direction.getOffset().getValue().intValue() / (float) divisions;
                            }
                            if (isNewline(direction)) {
                                newlines.put(measureStartTime + measureTime + offset, measureTime + offset - measureLength);
                            }
                            if (isNewSection(direction)) {
                                newSections.add(measureStartTime + measureTime + offset);
                                newlines.put(measureStartTime + measureTime + offset, measureTime + offset - measureLength);
                            }
                            if (isArtisticWhitespace(direction)) {
                                var whitespace = new BeamGroupTuple();
                                whitespace.startTime = (measureStartTime + measureTime + offset) - RenderingConfiguration.artisticWhitespaceWidth;
                                whitespace.endTime = (measureStartTime + measureTime + offset);
                                var restChord = new ChordTuple(whitespace.startTime, 0);
                                restChord.duration = RenderingConfiguration.artisticWhitespaceWidth;
                                var restNote = new Note();
                                restNote.setRest(new org.audiveris.proxymusic.Rest());
                                restChord.notes.add(restNote);
                                whitespace.addChord(restChord);
                                currentPart.beamGroups.add(whitespace);
                            }
                            currentPart.directions.put(measureStartTime + measureTime + offset, direction);
                        }
                    }

                    if (newTimeSignature) {
                        addPulseLines(currentTimeSignature, measureStartTime, currentPart.pulseLines, measure.getText(), currentTimeSignature);
                    } else {
                        addPulseLines(currentTimeSignature, measureStartTime, currentPart.pulseLines, measure.getText(), null);
                    }

                    measureStartTime += measureLength;
                }
                totalLength = Math.max(measureStartTime, totalLength);
                currentPart.pulseLines.add(new PulseLineTuple(measureStartTime, "", 0, null));
            }

            Map<Float, Integer> lineIndices = new HashMap<>();
            List<Float> lineLengths = new ArrayList<>();
            List<Float> lineOffsets = new ArrayList<>();
            int index = 0;
            float prevLineStart = 0;
            for (Float newline : newlines.keySet()) {
                lineIndices.put(newline, index);
                lineOffsets.add(newlines.get(newline));
                if (index != 0) {
                    lineLengths.add(newline - prevLineStart);
                    prevLineStart = newline;
                }
                for (List<LineTuple> partList : partLines.values()) {
                    partList.add(new LineTuple(newline));
                }
                ++index;
            }
            lineLengths.add(totalLength - prevLineStart);

            Map<Float, Integer> sectionIndices = new HashMap<>();
            index = 0;
            for (Float newSection : newSections) {
                sectionIndices.put(newSection, index);
                for (List<TreeMap<Float, Line>> lineList : partSections.values()) {
                    lineList.add(new TreeMap<>());
                }
                ++index;
            }

            for (Map.Entry<String, ParsingPartTuple> part : parsingParts.entrySet()) {
                for (BeamGroupTuple beam : part.getValue().beamGroups) {
                    float lineStart = newlines.floorKey(beam.chords.get(0).crotchets);
                    int lineNum = lineIndices.get(lineStart);
                    if (isRest(beam)) {
                        splitToRestTuple(beam, newlines, lineIndices, partLines.get(part.getKey()));
                    } else {
                        partLines.get(part.getKey()).get(lineNum).notes.add(beamTupleToInstantiatedBeamTuple(beam, lineStart, lineNum));
                    }
                }
            }

            for (Map.Entry<String, ParsingPartTuple> part : parsingParts.entrySet()) {
                for (PulseLineTuple pulseLine : part.getValue().pulseLines) {
                    float lineStart = newlines.floorKey(pulseLine.time);
                    int lineNum = lineIndices.get(lineStart);
                    partLines.get(part.getKey()).get(lineNum).pulses.add(pulseTupleToInstantiatedPulseTuple(pulseLine, lineStart, lineNum));
                    Float lowerLineStart = newlines.lowerKey(pulseLine.time);
                    if (lowerLineStart != null) {
                        int lowerLineNum = lineIndices.get(lowerLineStart);
                        if (lowerLineNum != lineNum) {
                            partLines.get(part.getKey()).get(lowerLineNum).pulses.add(pulseTupleToInstantiatedPulseTuple(pulseLine, lowerLineStart, lowerLineNum));
                        }
                    }
                }
            }
            TreeMap<String, List<InstantiatedLineTuple>> finalLines = new TreeMap<>();
            List<Float> newlinesList = newlines.keySet().stream().toList();


            for (Map.Entry<String, List<LineTuple>> part : partLines.entrySet()) {
                finalLines.put(part.getKey(), new ArrayList<>());
                for (int i = 0; i < part.getValue().size(); ++i) {
                    List<StaveElement> elements = new ArrayList<>();
                    Stave stave = new Stave(elements, new ArrayList<>(),new ArrayList<>());

                    Line tempLine = new Line(new ArrayList<>() {{ add(stave); }}, lineLengths.get(i), lineOffsets.get(i), i);
                    finalLines.get(part.getKey()).add(new InstantiatedLineTuple(newlinesList.get(i), tempLine));

                    var fusedRests = fuseRestTuples(part.getValue().get(i).rests);
                    for (RestTuple restTuple : fusedRests) {
                        tempLine.getStaves().get(0).addWhiteSpace(restTupleToRest(restTuple, tempLine));
                    }

                    for (InstantiatedBeamGroupTuple beamTuple : part.getValue().get(i).notes) {
                        tempLine.getStaves().get(0).addStaveElement(instantiatedBeamTupleToBeamGroup(beamTuple, tempLine));
                    }

                    for (InstantiatedPulseLineTuple pulseTuple : part.getValue().get(i).pulses) {
                        tempLine.addPulseLine(instantiatedPulseLineTupleToPulseLine(pulseTuple, tempLine));
                    }
                }
            }

            for (Map.Entry<String, List<InstantiatedLineTuple>> part : finalLines.entrySet()) {
                for (InstantiatedLineTuple instantiatedLineTuple : part.getValue()) {
                    float sectionStart = newSections.floor(instantiatedLineTuple.startTime);
                    int sectionNum = sectionIndices.get(sectionStart);
                    partSections.get(part.getKey()).get(sectionNum).put(instantiatedLineTuple.startTime, instantiatedLineTuple.line);
                }
            }

            TreeMap<String, List<Section>> finalSections = new TreeMap<>();

            for (Map.Entry<String, List<TreeMap<Float, Line>>> part : partSections.entrySet()) {
                List<Section> sections = new ArrayList<>();
                for (TreeMap<Float, Line> lines : part.getValue()) {
                    if (lines.size() != 0) {
                        sections.add(new Section(lines.values().stream().toList(),
                                parsingParts.get(part.getKey()).clefs.floorEntry(lines.firstKey()).getValue(),
                                parsingParts.get(part.getKey()).keySignatures.floorEntry(lines.firstKey()).getValue()));
                    }
                }
                finalSections.put(part.getKey(), sections);
            }

            for (Map.Entry<String, List<Section>> part : finalSections.entrySet()) {
                parts.get(part.getKey()).setSections(part.getValue());
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

    static boolean isRest(BeamGroupTuple tuple) {
        return (tuple.chords.get(0).notes.get(0).getRest() != null);
    }

    static SplitChordTuple splitInstantiatedChordTuple(InstantiatedChordTuple tuple, float newLine) {
        SplitChordTuple res = new SplitChordTuple();
        res.pre.add(tuple);
        return res;
    }

    static List<InstantiatedBeamGroupTuple> splitInstantiatedBeamTuple(InstantiatedBeamGroupTuple tuple, TreeMap<Float, Float> newlines, TreeMap<Float, Integer> lineIndices) {
        return null;
    }

    static InstantiatedBeamGroupTuple beamTupleToInstantiatedBeamTuple(BeamGroupTuple tuple, float lineTime, int lineNum) {
        InstantiatedBeamGroupTuple beamTuple = new InstantiatedBeamGroupTuple();
        for (ChordTuple chordTuple : tuple.chords) {
            beamTuple.chords.add(chordTupleToInstantiatedChordTuple(chordTuple, lineTime, lineNum));
        }

        Integer[] beaming = new Integer[10];
        Arrays.fill(beaming, -1);
        Integer[] beamStarts = new Integer[10];
        Arrays.fill(beamStarts, -1);
        for (int i = 0; i < beamTuple.chords.size(); ++i) {
            for (Note note : tuple.chords.get(i).notes) {
                for (Beam beam : note.getBeam()) {
                    if (beam.getNumber() != 1) {
                        switch (beam.getValue()) {
                            case FORWARD_HOOK:
                            case BACKWARD_HOOK:
                                if (beaming[beam.getNumber() - 2] == -1) {
                                    beamTuple.beams.add(new BeamTuple(i, i, beam.getNumber() - 1));
                                } else {
                                    System.out.println("Started hook beaming when beam had already started");
                                }
                                break;
                            case BEGIN:
                                if (beaming[beam.getNumber() - 2] == -1) {
                                    beamStarts[beam.getNumber() - 2] = i;
                                    beaming[beam.getNumber() - 2] = i;
                                } else {
                                    System.out.println("Started beam when beam had already started");
                                }
                                break;
                            case CONTINUE:
                                if (beaming[beam.getNumber() - 2] == i - 1) {
                                    beaming[beam.getNumber() - 2] = i;
                                } else {
                                    System.out.println("Continued invalid beam");
                                }
                                break;
                            case END:
                                if (beaming[beam.getNumber() - 2] == i - 1) {
                                    beaming[beam.getNumber() - 2] = -1;
                                    if (beamStarts[beam.getNumber() - 2] != -1) {
                                        beamTuple.beams.add(new BeamTuple(beamStarts[beam.getNumber() - 2], i, beam.getNumber() - 1));
                                    }
                                } else {
                                    System.out.println("Ended invalid beam");
                                }
                                break;
                        }
                    }
                }
            }
        }
        return beamTuple;
    }

    static boolean isBeamed(uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType) {
        switch (noteType) {
            case MAXIMA, BREVE, SEMIBREVE, MINIM, CROTCHET -> { return false; }
            case QUAVER, SQUAVER, DSQUAVER, HDSQUAVER -> { return true; }
        }
        return false;
    }

    static BeamGroup instantiatedBeamTupleToBeamGroup(InstantiatedBeamGroupTuple beamTuple, Line line) {
        if (beamTuple.chords.size() == 1) {
            if (!isBeamed(beamTuple.chords.get(0).noteType)) {
                return instantiatedChordTupleToChord(beamTuple.chords.get(0), line);
            }
        }
        List<Chord> chords = new ArrayList<>();
        for (InstantiatedChordTuple chordTuple : beamTuple.chords) {
            chords.add(instantiatedChordTupleToChord(chordTuple, line));
        }
        BeamGroup group = new BeamGroup(chords);
        for (BeamTuple tuple : beamTuple.beams) {
            group.addBeam(tuple.start, tuple.end, tuple.number);
        }
        return group;
    }

    static void splitToRestTuple(BeamGroupTuple tuple, TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, List<LineTuple> target) {
        float endTime = tuple.endTime;
        while (endTime > tuple.startTime) {
            float newEndTime = newlines.lowerKey(endTime);
            target.get(lineIndices.get(newEndTime)).rests.add(new RestTuple(Math.max(newEndTime, tuple.startTime) - newEndTime, endTime - newEndTime));
            endTime = newEndTime;
        }
    }

    static Whitespace restTupleToRest(RestTuple tuple, Line line) {
        return new Rest(new MusicalPosition(line, tuple.startTime), new MusicalPosition(line, tuple.endTime));
    }

    static List<RestTuple> fuseRestTuples(List<RestTuple> rests) {
        TreeMap<Float, RestTuple> fusedRests = new TreeMap<>();

        for (RestTuple rest : rests) {
            if (fusedRests.size() == 0) {
                fusedRests.put(rest.startTime, rest);
            } else {
                var currentRest = rest;
                boolean changed = true;
                while (changed) {
                    changed = false;
                    var entry = fusedRests.floorEntry(currentRest.startTime);
                    if (entry != null && entry.getValue().endTime >= currentRest.startTime) {
                        currentRest = new RestTuple(entry.getKey(), Math.max(currentRest.endTime, entry.getValue().endTime));
                        fusedRests.remove(entry.getKey());
                        changed = true;
                        continue;
                    }
                    entry = fusedRests.floorEntry(currentRest.endTime);
                    if (entry != null && entry.getValue().startTime >= currentRest.startTime) {
                        currentRest = new RestTuple(currentRest.startTime, Math.max(currentRest.endTime, entry.getValue().endTime));
                        fusedRests.remove(entry.getKey());
                        changed = true;
                    }
                }
                fusedRests.put(currentRest.startTime, currentRest);
            }
        }

        return fusedRests.values().stream().toList();
    }

    static InstantiatedChordTuple chordTupleToInstantiatedChordTuple(ChordTuple chord, float lineTime, int lineNum) {
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches = new ArrayList<>();
        List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals = new ArrayList<>();
        List<ChordMarking> markings = new ArrayList<>();
        for (Note note : chord.notes) {
            if (note.getPitch() != null) {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(pitchToGrandStaveLine(note.getPitch()) - chord.lowestLine, 0));
            } else {
                pitches.add(new uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch(0, 0));
            }
            if (note.getAccidental() != null) {
                switch (note.getAccidental().getValue()) {
                    case FLAT -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case SHARP -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case FLAT_FLAT -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.DOUBLE_FLAT);
                    case DOUBLE_SHARP -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.DOUBLE_SHARP);
                    case NATURAL -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NATURAL);
                    default -> accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NONE);
                }
            } else {
                accidentals.add(uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NONE);
            }
            addMarkings(note, markings);
        }
        return new InstantiatedChordTuple(pitches, accidentals, chord.crotchets - lineTime, chord.duration, convertNoteType(chord.notes.get(0).getType()), getDotNumber(chord.notes.get(0)), markings);
    }

    static void addMarkings(Note note, List<ChordMarking> target) {
        if (note.getNotations() != null) {
            for (Notations notations : note.getNotations()) {
                if (notations.getTiedOrSlurOrTuplet() != null) {
                    for (Object object : notations.getTiedOrSlurOrTuplet()) {
                        if (object instanceof Articulations articulations) {
                            for (JAXBElement element : articulations.getAccentOrStrongAccentOrStaccato()) {
                                switch (element.getName().getLocalPart()) {
                                    case "staccato" -> target.add(new Staccato());
                                    case "tenuto" -> target.add(new Tenuto());
                                    case "accent" -> target.add(new Accent());
                                    case "strong-accent" -> target.add(new StrongAccent());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static int getDotNumber(Note note) {
        if (note.getDot() == null) {
            return 0;
        }
        return note.getDot().size();
    }

    static Chord instantiatedChordTupleToChord(InstantiatedChordTuple chordTuple, Line line) {
        return new Chord(chordTuple.pitches, chordTuple.accidentals, new MusicalPosition(line, chordTuple.crotchetsIntoLine), chordTuple.duration, chordTuple.noteType, chordTuple.dots, chordTuple.markings);
    }

    static InstantiatedPulseLineTuple pulseTupleToInstantiatedPulseTuple(PulseLineTuple tuple, float lineTime, int lineNum) {
        return new InstantiatedPulseLineTuple(tuple.time - lineTime, tuple.name, tuple.beatWeight, tuple.timeSig);
    }

    static PulseLine instantiatedPulseLineTupleToPulseLine(InstantiatedPulseLineTuple pulseTuple, Line line) {
        return switch (pulseTuple.beatWeight) {
            case 0 -> new BarLine(new MusicalPosition(line, pulseTuple.timeInLine), pulseTuple.name, pulseTuple.timeSig);
            default -> new BeatLine(new MusicalPosition(line, pulseTuple.timeInLine), pulseTuple.beatWeight);
        };
    }

    static uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType convertNoteType(NoteType noteType) {
        return switch (noteType.getValue()) {
            case "maxima" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MAXIMA;
            case "long" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.BREVE;
            case "whole" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SEMIBREVE;
            case "half" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.MINIM;
            case "quarter" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET;
            case "eighth" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.QUAVER;
            case "16th" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.SQUAVER;
            case "32nd" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.DSQUAVER;
            case "64th" -> uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.HDSQUAVER;
            default -> throw new IllegalArgumentException();
        };
    }

    // translates a pitch into the number of lines above the root of C0.
    static int pitchToGrandStaveLine(Pitch pitch) {
        return switch (pitch.getStep()) {
            case C -> 0;
            case D -> 1;
            case E -> 2;
            case F -> 3;
            case G -> 4;
            case A -> 5;
            case B -> 6;
        } + 7 * pitch.getOctave();
    }

    static int pitchToGrandStaveLine(Step step, int octave) {
        return switch (step) {
            case C -> 0;
            case D -> 1;
            case E -> 2;
            case F -> 3;
            case G -> 4;
            case A -> 5;
            case B -> 6;
        } + 7 * octave;
    }

    static boolean isNewline(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId formattedText) {
                            if (formattedText.getValue().equals("n") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
                                return true;
                            }
                            if (formattedText.getValue().equals("\\n")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean isNewSection(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId formattedText) {
                            if (formattedText.getValue().equals("s") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
                                return true;
                            }
                            if (formattedText.getValue().equals("\\s")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    static boolean isArtisticWhitespace(Direction direction) {
        if (direction.getDirectionType() != null) {
            for (var directionType : direction.getDirectionType()) {
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId formattedText) {
                            if (formattedText.getValue().equals("w") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
                                return true;
                            }
                            if (formattedText.getValue().equals("\\w")) {
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
                    ret.setBeatNum(Integer.parseInt(timeElement.getValue()));
                } else if (timeElement.getName().getLocalPart().equals("beat-type")) {
                    ret.setBeatType(Integer.parseInt(timeElement.getValue()));
                }
            }
            return ret;
        }
        return null;
    }

    static KeySignature parseKeySignature(Key keySignature, KeySignature currentKeySignature) {
        if (keySignature != null) {
            KeySignature ret = new KeySignature();
            if (keySignature.getFifths() != null) {
                switch (keySignature.getFifths().intValue()) {
                    case -7:
                        ret.addAlteration(0, PitchName.F, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -6:
                        ret.addAlteration(0, PitchName.C, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -5:
                        ret.addAlteration(0, PitchName.G, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -4:
                        ret.addAlteration(0, PitchName.D, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -3:
                        ret.addAlteration(0, PitchName.A, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -2:
                        ret.addAlteration(0, PitchName.E, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                    case -1:
                        ret.addAlteration(0, PitchName.B, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.FLAT);
                        break;
                    case 7:
                        ret.addAlteration(0, PitchName.B, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 6:
                        ret.addAlteration(0, PitchName.E, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 5:
                        ret.addAlteration(0, PitchName.A, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 4:
                        ret.addAlteration(0, PitchName.D, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 3:
                        ret.addAlteration(0, PitchName.G, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 2:
                        ret.addAlteration(0, PitchName.C, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                    case 1:
                        ret.addAlteration(0, PitchName.F, uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.SHARP);
                        break;
                    case 0:
                        for (KeySignature.Alteration alteration : currentKeySignature.getAlterations()) {
                            if (alteration.getAccidental() != uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NATURAL) {
                                ret.addAlteration(alteration.getAlteredPitch(), uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental.NATURAL);
                            }
                        }
                        break;
                }
            }
            return ret;
        }
        return null;
    }

    static int clefToLowestLineGrandStaveLine(uk.ac.cam.optimisingmusicnotation.representation.properties.Clef clef) {
        return switch (clef.getSign()) {
            case C -> pitchToGrandStaveLine(Step.C, 4);
            case F -> pitchToGrandStaveLine(Step.F, 3);
            case G -> pitchToGrandStaveLine(Step.G, 4);
            case PERCUSSION, TAB -> 0;
        } + 7 * clef.getOctaveChange() - clef.getLine();
    }

    static uk.ac.cam.optimisingmusicnotation.representation.properties.Clef parseClef(org.audiveris.proxymusic.Clef clef) {
        int octaveOffset = 0;
        if (clef.getClefOctaveChange() != null) {
            octaveOffset = clef.getClefOctaveChange().intValue();
        }
        int staveLine = 0;
        if (clef.getLine() != null) {
            staveLine = (clef.getLine().intValue() - 1) * 2;
        }
        switch (clef.getSign()) {
            case C -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.C, staveLine, octaveOffset);
            }
            case F -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.F, staveLine, octaveOffset);
            }
            case G -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.G, staveLine, octaveOffset);
            }
            case PERCUSSION -> {
                return new uk.ac.cam.optimisingmusicnotation.representation.properties.Clef(
                        uk.ac.cam.optimisingmusicnotation.representation.properties.ClefSign.PERCUSSION, staveLine, octaveOffset);
            }
        }
        throw new IllegalArgumentException("Unknown clef symbol");
    }

    static void addPulseLines(TimeSignature time, float measureStartTime, List<PulseLineTuple> pulseLines, String measureName, TimeSignature timeSig) {
        switch (time.getBeatNum()) {
            case 2 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 1, timeSig));
            }
            case 3 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 1, timeSig));
            }
            case 4 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
            }
            case 5 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            case 6 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            case 7 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6 * 4f / time.getBeatType(), measureName, 1, timeSig));
            }
            case 9 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 7 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 8 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            case 12 -> {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 2 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 3 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 4 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 5 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 6 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 7 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 8 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 9 * 4f / time.getBeatType(), measureName, 1, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 10 * 4f / time.getBeatType(), measureName, 2, timeSig));
                pulseLines.add(new PulseLineTuple(measureStartTime + 11 * 4f / time.getBeatType(), measureName, 2, timeSig));
            }
            default -> pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, timeSig));
        }
    }

    public static Object openMXL(String input) {
        try (FileInputStream xml = new FileInputStream(input)) {
            return Marshalling.unmarshal(xml);
        } catch (Exception e1) {
            try (ZipInputStream xml = new ZipInputStream(new FileInputStream(input))) {
                ZipEntry zipEntry = xml.getNextEntry();
                while (zipEntry != null) {
                    if(zipEntry.getName().equals("score.xml")) {
                        return Marshalling.unmarshal(xml);
                    }
                    zipEntry = xml.getNextEntry();
                }
                return null;
            } catch (Exception e2) {

            }
        }
        return null;
    }

    private Parser() {}

    public static void main(String[] args) {
        String target = "test_scores/TestScore2.mxl";
        if (args.length > 0) {
            target = args[0];
        }

        Object mxl = Parser.openMXL(target);
        Score score = Parser.parseToScore(mxl);
        String outDir = "./out/"; // Output Directory
        Path outDirPath = Paths.get(outDir);
        if (!Files.exists(outDirPath)) {
            try {
                Files.createDirectory(outDirPath);
            } catch (IOException e) {
                System.err.println("Error while creating output directory path: ");
                e.printStackTrace();
            }
        }

        String outTarget = "output";

        if (args.length > 2) {
            outTarget = args[2];
        }

        int targetPart = 0;
        if (args.length > 1) {
            targetPart = Integer.parseInt(args[1]);
        }

        if (targetPart == -1) {
            for (Part part : score.getParts()) {
                try (PdfWriter writer = new PdfWriter(outDir + outTarget + "_" + part.getName() + ".pdf")) {
                    PdfDocument pdf = new PdfDocument(writer);
                    PageSize ps = PageSize.A4;
                    pdf.addNewPage(ps);

                    PdfMusicCanvas canvas = new PdfMusicCanvas(pdf);
                    part.draw(canvas, score.getWorkTitle());
                    pdf.close();
                }
                catch (Exception e) {
                    System.err.println("Error while creating PDF: ");
                    e.printStackTrace();
                }
            }
        } else {
            try (PdfWriter writer = new PdfWriter(outDir + outTarget + ".pdf")) {
                PdfDocument pdf = new PdfDocument(writer);
                PageSize ps = PageSize.A4;
                pdf.addNewPage(ps);

                PdfMusicCanvas canvas = new PdfMusicCanvas(pdf);
                Part testPart = score.getParts().get(targetPart);
                testPart.draw(canvas, score.getWorkTitle());
                pdf.close();
            }
            catch (IOException e) {
                System.err.println("Error while creating PDF: ");
                e.printStackTrace();
            }
        }
    }
}
