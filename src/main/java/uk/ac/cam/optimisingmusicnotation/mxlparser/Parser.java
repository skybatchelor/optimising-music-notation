package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.Pitch;
import org.audiveris.proxymusic.util.Marshalling;
import uk.ac.cam.optimisingmusicnotation.representation.*;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Clef;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.RightBeamSegment;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.LeftBeamSegment;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import javax.xml.bind.JAXBElement;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.String;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A collection of static methods for parsing MusicXML into a {@link Score} object.
 */
public class Parser {
    /** Whether a key signature also creates a new key signature */
    public static final boolean NEW_SECTION_FOR_KEY_SIGNATURE = true;
    /** An epsilon value used to judge how close two floats are to each other */
    public static final float EPSILON = 0.001f;
    /** How close two whitespaces have to be in order to be combined into one whitespace */
    public static final float WHITESPACE_EPSILON = 0.08f;
    /** Whether the bar line at the end of a line has a time signature */
    public static final boolean END_BAR_LINE_TIME_SIGNATURE = false;
    /** Whether the bar line at the end of a line has a name, typically the bar number */
    public static final boolean END_BAR_LINE_NAME = false;
    /** Whether the parser normalises the time spacing to account for tempo or not */
    public static final boolean TIME_NORMALISED_PARSING = true;
    /** How long one second of time is in the normalised time */
    public static final float TIME_NORMALISATION_FACTOR = 60 * 12;
    /** An offset to codas to account for float errors in parsing */
    public static final float ADD_TO_CODA = 0.004f;
    /** The BPM to start the score at */
    public static float startBpm = 120f;


    /**
     * Parses a given mxl object into a score, according to the static settings in Parser, and settings in {@link RenderingConfiguration}.
     * @param mxl the mxl to parse
     * @return the score representing the parsed mxl
     */
    public static Score parseToScore(Object mxl) {
        if (mxl instanceof ScorePartwise partwise) {

            // the newlines in the score
            TreeMap<Float, Float> newlines = new TreeMap<>() {{ put(0f, 0f); }};
            // the new sections in the score
            TreeSet<Float> newSections = new TreeSet<>() {{ add(0f); }};
            TreeMap<String, Part> parts = new TreeMap<>();

            TreeMap<Float, TempoTuple> tempoMarkings = new TreeMap<>();

            TreeMap<Float, Float> tempoChanges = new TreeMap<>() {{ put(0f, startBpm); }};

            TreeMap<Float, List<TimeSignature.BeatTuple>> beatChanges = new TreeMap<>();

            TreeSet<Float> globalCapitalNotes = new TreeSet<>();
            TreeSet<Float> globalCapitalNextNotes = new TreeSet<>();

            if (RenderingConfiguration.newlineAddsCapital == RenderingConfiguration.NoneThisNext.THIS
                    || RenderingConfiguration.newSectionAddsCapital == RenderingConfiguration.NoneThisNext.THIS) globalCapitalNotes.add(0f);
            if (RenderingConfiguration.newlineAddsCapital == RenderingConfiguration.NoneThisNext.NEXT
                    || RenderingConfiguration.newSectionAddsCapital == RenderingConfiguration.NoneThisNext.NEXT) globalCapitalNextNotes.add(0f);

            Function<ParsingPartTuple, BiConsumer<Float, Float>> addNewlineGenerator = (currentPart) -> {
                switch (RenderingConfiguration.newlineAddsCapital) {
                    case NONE -> {
                        return (newlines::put);
                    }
                    case THIS -> {
                        return ((time, offset) -> { globalCapitalNotes.add(time); newlines.put(time, offset); });
                    }
                    case NEXT -> {
                        return ((time, offset) -> { globalCapitalNextNotes.add(time); newlines.put(time, offset); });
                    }
                }
                throw new IllegalArgumentException();
            };
            Function<ParsingPartTuple, BiConsumer<Float, Float>> addNewSectionGenerator = (currentPart) -> {
                switch (RenderingConfiguration.newSectionAddsCapital) {
                    case NONE -> {
                        return ((time, offset) -> { newSections.add(time);
                            addNewlineGenerator.apply(currentPart).accept(time, offset); });
                    }
                    case THIS -> {
                        return ((time, offset) -> { globalCapitalNotes.add(time); newSections.add(time);
                            addNewlineGenerator.apply(currentPart).accept(time, offset); });
                    }
                    case NEXT -> {
                        return ((time, offset) -> { globalCapitalNextNotes.add(time); newSections.add(time);
                            addNewlineGenerator.apply(currentPart).accept(time, offset); });
                    }
                }
                throw new IllegalArgumentException();
            };
            Function<KeySignature, BiFunction<ParsingPartTuple, Integer, BiConsumer<Integer, Float>>>
                    addNewArtisticWhitespaceGenerator = (tempKeySig) -> (currentPart, staff) -> (voice, time) -> {
                    currentPart.putInArtisticWhitespace(staff, voice, time);
                    var whitespace = new BeamGroupTuple();
                    whitespace.startTime = (time) - RenderingConfiguration.artisticWhitespaceWidth;
                    whitespace.endTime = (time);
                    var restChord = new ChordTuple(whitespace.startTime, 0, tempKeySig);
                    restChord.duration = RenderingConfiguration.artisticWhitespaceWidth;
                    var restNote = new Note();
                    restNote.setRest(new org.audiveris.proxymusic.Rest());
                    restChord.notes.add(restNote);
                    whitespace.addChord(restChord);
                    whitespace.staff = staff;
                    whitespace.voice = voice;
                    switch (RenderingConfiguration.artisticWhitespaceAddsCapital) {
                        case THIS -> currentPart.addCapital(whitespace.staff, voice, time);
                        case NEXT -> currentPart.addNextCapital(whitespace.staff, voice, time);
                    }
                    currentPart.putInBeamGroup(whitespace); };

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

            // parse each part into a ParsingPartTuple
            for (ScorePartwise.Part part : partwise.getPart()) {
                float currentTempo = startBpm;

                TreeMap<MusicGroupType, TreeMap<Integer, MusicGroupTuple>> musicGroupTuples = new TreeMap<>();
                for (MusicGroupType musicGroupType : MusicGroupType.values()) {
                    musicGroupTuples.put(musicGroupType, new TreeMap<>());
                }

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
                int prevDivs;
                TreeMap<Integer, Integer> lowestLineGrandStaveLines = new TreeMap<>() {{ put(1, 0); }};
                ChordTuple currentChord = new ChordTuple(0, 0, currentKeySignature);
                BeamGroupTuple beamGroup = new BeamGroupTuple();

                // parse each measure
                for (var measure : part.getMeasure()) {
                    boolean newTimeSignature = false;
                    float measureTime = 0;
                    int divisionsInBar = 0;
                    float measureLength = currentTimeSignature.getBeatNum() * 4f / (currentTimeSignature.getBeatType());

                    // parse the elements of the measure
                    for(Object component : measure.getNoteOrBackupOrForward()) {
                        if (component instanceof Attributes attributes) {
                            // parse attributes
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
                                            if (measureTime == 0) {
                                                addNewSectionGenerator.apply(currentPart).accept(measureStartTime + measureTime, 0f);
                                            } else {
                                                addNewSectionGenerator.apply(currentPart).accept(measureStartTime + measureTime, measureTime - measureLength);
                                            }
                                        }
                                    }
                                }
                            }
                            if (attributes.getClef() != null) {
                                int i = 1;
                                for(org.audiveris.proxymusic.Clef clef : attributes.getClef()) {
                                    uk.ac.cam.optimisingmusicnotation.representation.properties.Clef parsed = parseClef(clef);
                                    lowestLineGrandStaveLines.put(i, clefToLowestLineGrandStaveLine(parsed));
                                    currentChord.lowestLine = clefToLowestLineGrandStaveLine(parsed);
                                    currentPart.putInClef(getStaff(clef.getNumber()), measureStartTime + measureTime, parsed);
                                    i++;
                                }
                            }
                            if (attributes.getDivisions() != null) {
                                divisions = attributes.getDivisions().intValue();
                            }
                            measureLength = currentTimeSignature.getBeatNum() * 4f / (currentTimeSignature.getBeatType());

                        } else if (component instanceof Note note) {
                            // parse notes
                            parseSlurs(musicGroupTuples, currentPart, divisions, note, measureStartTime + measureTime);
                            if (note.getDuration() != null) {
                                prevChange = note.getDuration().intValue() / (float)divisions;
                                prevDivs = note.getDuration().intValue();
                            } else {
                                prevChange = 0;
                                prevDivs = 0;
                            }
                            // deal with if the note is in a chord or not
                            if (note.getChord() == null) {
                                currentChord = new ChordTuple(measureStartTime + measureTime,
                                        lowestLineGrandStaveLines.get(getStaff(note.getStaff())), currentKeySignature);
                                measureTime += prevChange;
                                divisionsInBar += prevDivs;
                            }
                            currentChord.notes.add(note);
                            // get chord duration
                            if (note.getDuration() != null) {
                                currentChord.duration = note.getDuration().intValue() / (float)divisions;
                            }
                            // handle beams
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
                                                currentPart.putInBeamGroup(beamGroup);
                                                beamGroup = new BeamGroupTuple();
                                                addedToBeamGroup = true;
                                            }
                                        }
                                    }
                                }
                            }
                            if (!addedToBeamGroup && note.getChord() == null) {
                                beamGroup.addChord(currentChord);
                                currentPart.putInBeamGroup(beamGroup);
                                beamGroup = new BeamGroupTuple();
                            }

                        } else if (component instanceof Backup backup) {
                            // parse backup
                            divisionsInBar -= backup.getDuration().intValue();
                            measureTime = divisionsInBar / (float) divisions;

                        } else if (component instanceof Direction direction) {
                            // parse directions
                            // get the direction time
                            float offset = 0;
                            if (direction.getOffset() != null) {
                                offset = direction.getOffset().getValue().intValue() / (float) divisions;
                            }
                            // get the newline offset
                            float newlineOffset = 0;
                            if (measureTime + offset != 0) {
                                newlineOffset = measureTime + offset - measureLength;
                            }
                            // parse directives
                            parseMusicDirective(musicGroupTuples, currentPart, direction,
                                    measureStartTime + measureTime + offset, newlineOffset,
                                    beatChanges, currentTimeSignature,
                                    addNewlineGenerator.apply(currentPart),
                                    addNewSectionGenerator.apply(currentPart),
                                    addNewArtisticWhitespaceGenerator.apply(currentKeySignature).apply(currentPart, getStaff(direction.getStaff())),
                                    (voice, time) -> currentPart.addCapital(getStaff(direction.getStaff()), voice, time));
                            // parse tempo changes
                            currentTempo = parseTempoMarking(tempoMarkings, tempoChanges, currentTempo, direction, measureStartTime + measureTime + offset);
                            // for safe keeping if future extensions need it later
                            currentPart.directions.put(measureStartTime + measureTime + offset, direction);
                        }
                    }

                    // Add pulse lines
                    var beatChange = beatChanges.lowerEntry(measureStartTime + measureTime);
                    if (beatChange != null && beatChange.getKey() >= measureStartTime) {
                        currentTimeSignature.setBeatPattern(beatChange.getValue());
                    }
                    if (newTimeSignature) {
                        addPulseLines(currentTimeSignature, measureStartTime, currentPart.pulseLines,
                                measure.getText() == null ? measure.getNumber() == null ? "" : measure.getNumber() : measure.getText(), currentTimeSignature);
                    } else {
                        addPulseLines(currentTimeSignature, measureStartTime, currentPart.pulseLines,
                                measure.getText() == null ? measure.getNumber() == null ? "" : measure.getNumber() : measure.getText(), null);
                    }

                    // Update measure start time
                    measureStartTime += measureLength;
                }
                totalLength = Math.max(measureStartTime, totalLength);
                currentPart.pulseLines.add(new PulseLineTuple(measureStartTime, "", 0, null));
            }

            for (var part : parsingParts.entrySet()) {
                part.getValue().globalCapitalNotes = globalCapitalNotes;
                part.getValue().globalCapitalNextNotes = globalCapitalNextNotes;
            }

            // integrate time
            var integratedTime = integrateTime(tempoChanges);

            // normalise newlines and new sections
            var nNewlines = normalisedNewlines(newlines, integratedTime);
            var nNewSections = normalisedSections(newSections, integratedTime);

            // generate line indices
            Map<Float, Integer> lineIndices = new HashMap<>();
            List<Float> lineLengths = new ArrayList<>();
            List<Float> lineOffsets = new ArrayList<>();
            int index = 0;
            float prevLineStart = 0;
            for (Float newline : nNewlines.keySet()) {
                lineIndices.put(newline, index);
                lineOffsets.add(nNewlines.get(newline));
                if (index != 0) {
                    float lineLength = newline - prevLineStart;
                    lineLengths.add(lineLength);
                    for (List<LineTuple> partList : partLines.values()) {
                        partList.add(new LineTuple(prevLineStart, nNewlines.get(prevLineStart), lineLength));
                    }
                    prevLineStart = newline;
                }
                ++index;
            }
            float lineLength = normaliseTime(totalLength, integratedTime) - prevLineStart;
            for (List<LineTuple> partList : partLines.values()) {
                partList.add(new LineTuple(prevLineStart, nNewlines.get(prevLineStart), lineLength));
            }
            lineLengths.add(lineLength);

            // generate section indices
            var sectionIndices = createSectionIndices(nNewSections, partSections);

            // collate the parsed data into the final form
            var finalSections = finaliseSections(
                    populatePartSections(
                            partSections,
                            instantiateLines(
                                    nNewlines,
                                    populatePartLines(
                                            partLines, parsingParts, tempoMarkings, nNewlines, lineIndices, integratedTime),
                                    lineLengths, lineOffsets, parsingParts),
                            nNewSections, sectionIndices),
                    parsingParts);

            for (Map.Entry<String, List<Section>> part : finalSections.entrySet()) {
                parts.get(part.getKey()).setSections(part.getValue());
                parts.get(part.getKey()).setUpwardsStems(parsingParts.get(part.getKey()).upwardsStems);
            }
            return new Score(getWorkTitle(partwise), getComposer(partwise), parts.values().stream().toList());
        }
        return null;
    }

    /**
     * Gets the staff from a BigInteger
     * @param staff the staff value to get
     * @return a non-null integer representing the staff number
     */
    static int getStaff(BigInteger staff) {
        return staff == null ? 1 : staff.intValue();
    }

    /**
     * Gets the voice from a String
     * @param voice the voice value to get
     * @return a non-null integer representing the voice number
     */
    static int getVoice(String voice) {
        if (voice != null) {
            return Integer.parseInt(voice);
        }
        return 1;
    }

    /**
     * Takes all the tempo changes, and produces an integrated time list, which has the time for each tempo change.
     * @param tempoChanges the list of tempo changes
     * @return the map of integrated time
     */
    static TreeMap<Float, TempoChangeTuple> integrateTime(TreeMap<Float, Float> tempoChanges) {
        TreeMap<Float, TempoChangeTuple> integratedTime = new TreeMap<>() {{ put(0f, new TempoChangeTuple(0f, 0f, TIME_NORMALISATION_FACTOR / tempoChanges.firstEntry().getValue())); }};
        if (TIME_NORMALISED_PARSING) {
            for (Map.Entry<Float, Float> entry : tempoChanges.entrySet()) {
                var timeEntry = integratedTime.lowerEntry(entry.getKey());
                var tempoEntry = tempoChanges.lowerEntry(entry.getKey());
                if (timeEntry != null && tempoEntry != null) {
                    integratedTime.put(entry.getKey(), new TempoChangeTuple(
                            entry.getKey(),
                            timeEntry.getValue().time() + (entry.getKey() - timeEntry.getKey()) * (TIME_NORMALISATION_FACTOR / tempoEntry.getValue()),
                            TIME_NORMALISATION_FACTOR / entry.getValue()));
                }
            }
        } else {
            integratedTime.clear();
            integratedTime.put(0f, new TempoChangeTuple(0f, 0f, 1));
        }
        return integratedTime;
    }

    /**
     * Normalises the time for all the newlines
     * @param newlines the newlines
     * @param integratedTime the integrated time list
     * @return the time normalised newlines
     */
    static TreeMap<Float, Float> normalisedNewlines(TreeMap<Float, Float> newlines, TreeMap<Float, TempoChangeTuple> integratedTime) {
        var normalisedNewlines = new TreeMap<Float, Float>();
        for (var newline : newlines.entrySet()) {
            normalisedNewlines.put(normaliseTime(newline.getKey(), integratedTime), normaliseDuration(newline.getKey(), newline.getValue(), integratedTime));
        }
        return normalisedNewlines;
    }

    /**
     * Normalises the time for all the sections
     * @param newSections the new sections
     * @param integratedTime the integrated time list
     * @return the time normalised new sections
     */
    static TreeSet<Float> normalisedSections(TreeSet<Float> newSections, TreeMap<Float, TempoChangeTuple> integratedTime) {
        var normalisedSections = new TreeSet<Float>();
        for (var newSection : newSections) {
            normalisedSections.add(normaliseTime(newSection, integratedTime));
        }
        return normalisedSections;
    }

    static Map<Float, Integer> createSectionIndices(TreeSet<Float> newSections, TreeMap<String, List<TreeMap<Float, Line>>> partSections) {
        Map<Float, Integer> sectionIndices = new HashMap<>();
        int index = 0;
        for (Float newSection : newSections) {
            sectionIndices.put(newSection, index);
            for (List<TreeMap<Float, Line>> lineList : partSections.values()) {
                lineList.add(new TreeMap<>());
            }
            ++index;
        }
        return sectionIndices;
    }

    /**
     * Populates a map of lists of {@link LineTuple}s with the information from a map of {@link ParsingPartTuple}s.
     * Converts tuples from their base forms to their instantiated forms.
     * @param partLines the map of lists of line tuples to populate
     * @param parsingParts the map of parsing part tuples to convert
     * @param tempoMarkings the list of tempo markings to add
     * @param newlines the time normalised newlines
     * @param lineIndices a map converting from a time start line to a newline number
     * @param integratedTime the integrated time
     * @return partLines
     */
    static TreeMap<String, List<LineTuple>> populatePartLines(TreeMap<String, List<LineTuple>> partLines,
                                                              TreeMap<String, ParsingPartTuple> parsingParts,
                                                              TreeMap<Float, TempoTuple> tempoMarkings,
                                                              TreeMap<Float, Float> newlines,
                                                              Map<Float, Integer> lineIndices,
                                                              TreeMap<Float, TempoChangeTuple> integratedTime) {

        // parse each part
        for (Map.Entry<String, ParsingPartTuple> part : parsingParts.entrySet()) {
            // parse beam groups
            for (var staffEntry : part.getValue().staveBeamGroups.entrySet()) {
                for (var voiceEntry : staffEntry.getValue().entrySet()) {
                    TreeSet<Float> beamBreaks = new TreeSet<>(newlines.keySet());

                    if (part.getValue().artisticWhitespace.containsKey(staffEntry.getKey())
                            && part.getValue().artisticWhitespace.get(staffEntry.getKey()).containsKey(voiceEntry.getKey())) {
                        for (var time : part.getValue().artisticWhitespace.get(staffEntry.getKey()).get(voiceEntry.getKey())) {
                            float newTime = normaliseTime(time, integratedTime);
                            beamBreaks.add(newTime);
                        }
                    }

                    // convert beam groups into their instantiated forms, depending on if they are rests or not
                    for (BeamGroupTuple beam : voiceEntry.getValue().values()) {
                        if (beam.isRest()) {
                            // beam group is rest, so turn into a rest split by newlines
                            beam.splitToInstantiatedRestTuple(newlines, lineIndices, integratedTime, partLines.get(part.getKey()));
                        } else {
                            // beam group is beam group, so turn into a beam group split by beam breaks
                            beam.splitToInstantiatedBeamGroupTuple(beamBreaks, newlines, lineIndices, integratedTime, part.getValue(), partLines.get(part.getKey()));
                        }
                    }
                }
            }

            // Capitalise staff and voice specific capitals
            TreeSet<Float> normalisedCapitalEntries = new TreeSet<>();
            for (var staffEntry : part.getValue().capitalNextNotes.entrySet()) {
                for (var voiceEntry : staffEntry.getValue().entrySet()) {
                    for (var entry : voiceEntry.getValue()) {
                        normalisedCapitalEntries.add(normaliseTime(entry, integratedTime));
                    }
                    for (var entry : normalisedCapitalEntries) {
                        for (var lineEntry : partLines.get(part.getKey())) {
                            var target = lineEntry.chordGroups
                                    .get(staffEntry.getKey()).get(voiceEntry.getKey())
                                    .ceilingEntry(entry - lineEntry.startTime);
                            if (target != null) {
                                Float priorTime = normalisedCapitalEntries.floor(target.getValue().crotchetsIntoLine + lineEntry.startTime);
                                if (entry.equals(priorTime)) {
                                    target.getValue().capital = true;
                                    break;
                                }
                            }
                        }
                    }
                    normalisedCapitalEntries.clear();
                }
            }
            // capitalise global capitals
            HashMap<Integer, HashMap<Integer, HashSet<Float>>> seenMap = new HashMap<>();
            HashMap<Integer, HashSet<Float>> emptyMap = new HashMap<>();
            HashSet<Float> emptySet = new HashSet<>();
            // normalise the next capitals entries
            for (var entry : part.getValue().globalCapitalNextNotes) {
                normalisedCapitalEntries.add(normaliseTime(entry, integratedTime));
            }
            for (var entry : normalisedCapitalEntries) {
                for (int i = 0; i < partLines.get(part.getKey()).size(); ++i) {
                    var lineEntry = partLines.get(part.getKey()).get(i);
                    for (var staffEntry : lineEntry.chordGroups.entrySet()) {
                        for (var voiceEntry : staffEntry.getValue().entrySet()) {
                            var target = voiceEntry.getValue()
                                    .ceilingEntry(entry - lineEntry.startTime);
                            if (target != null) {
                                Float priorTime = normalisedCapitalEntries.floor(target.getValue().crotchetsIntoLine + lineEntry.startTime + EPSILON);
                                if (entry.equals(priorTime) && !seenMap
                                        .getOrDefault(staffEntry.getKey(), emptyMap)
                                        .getOrDefault(voiceEntry.getKey(), emptySet)
                                        .contains(entry)) {
                                    target.getValue().capital = true;
                                    seenMap.putIfAbsent(staffEntry.getKey(), new HashMap<>());
                                    seenMap.get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new HashSet<>());
                                    seenMap.get(staffEntry.getKey()).get(voiceEntry.getKey()).add(entry);
                                }
                            }
                        }
                    }
                }
            }
            normalisedCapitalEntries.clear();

            // convert the music groups
            for (var staffEntry : part.getValue().staveMusicGroups.entrySet()) {
                for (MusicGroupTuple musicGroup : staffEntry.getValue()) {
                    musicGroup.splitToInstantiatedMusicGroupTuple(newlines, lineIndices, integratedTime, partLines.get(part.getKey()));
                }
            }

            // convert the tempo markings
            for (Map.Entry<Float, TempoTuple> entry : tempoMarkings.entrySet()) {
                float lineStart = newlines.floorKey(normaliseTime(entry.getValue().time, integratedTime));
                int lineNum = lineIndices.get(lineStart);
                partLines.get(part.getKey()).get(lineNum).tempoMarkings.add(entry.getValue().toInstantiatedTempoTuple(lineStart, integratedTime));
            }

            // convert the pulse lines
            for (PulseLineTuple pulseLine : part.getValue().pulseLines) {
                float lineStart = newlines.floorKey(normaliseTime(pulseLine.time, integratedTime));
                int lineNum = lineIndices.get(lineStart);
                partLines.get(part.getKey()).get(lineNum).pulses.add(pulseLine.toInstantiatedPulseTuple(lineStart, integratedTime));
                Float lowerLineStart = newlines.lowerKey(normaliseTime(pulseLine.time, integratedTime));
                if (lowerLineStart != null) {
                    int lowerLineNum = lineIndices.get(lowerLineStart);
                    if (lowerLineNum != lineNum) {
                        var pulseTuple = pulseLine.toInstantiatedPulseTuple(lowerLineStart, integratedTime);
                        if (!END_BAR_LINE_NAME) {
                            pulseTuple.name = "";
                        }
                        if (!END_BAR_LINE_TIME_SIGNATURE) {
                            pulseTuple.timeSig = null;
                        }
                        partLines.get(part.getKey()).get(lowerLineNum).pulses.add(pulseTuple);
                    }
                }
            }

            parseLineExtensions(partLines.get(part.getKey()));
        }

        return partLines;
    }

    /**
     * Decide which lines need to have their pulse lines extended.
     * @param lines the lines to process
     */
    static void parseLineExtensions(List<LineTuple> lines) {
        for (int i = 0; i < lines.size() - 1; ++i) {
            int j = i;
            if (lines.get(i).pulses.stream().allMatch(
                    (pulse) -> matchPulseLine(pulse, lines.get(j), lines.get(j + 1))
            ) && (lines.get(i + 1).pulses.stream().allMatch(
                    (pulse) -> matchPulseLine(pulse, lines.get(j + 1), lines.get(j))
            ) ) ) {
                lines.get(i).extendDown = true;
                lines.get(i + 1).extendUp = true;
            }
        }
    }

    /**
     * A function to test whether a pulse line matches a pulse line in the other line.
     * @param tuple the pulse line to compare
     * @param line the line tuple holding the pulse line being used
     * @param other the line tuple to compare against
     * @return whether there is a match or not
     */
    static boolean matchPulseLine(InstantiatedPulseLineTuple tuple, LineTuple line, LineTuple other) {
        if (tuple.beatWeight > 1) return true;
        float position = tuple.timeInLine + line.offset;
        if (position < other.offset || position > other.offset + other.length) return true;
        return other.pulses.stream().anyMatch((otherTuple) -> {
            float otherPosition = otherTuple.timeInLine + other.offset;
            float difference = otherPosition - position;
            return tuple.beatWeight == otherTuple.beatWeight && -EPSILON < difference && difference < EPSILON;
        });
    }

    /**
     * Converts a map of lists of {@link LineTuple}s to a map of lists of {@link Line}s.
     * @param newlines the newlines
     * @param partLines the map of the lists
     * @param lineLengths the lengths of the lines
     * @param lineOffsets the offsets of the lines
     * @param parsingPart the parsing information
     * @return the map of lists of lines
     */
    static TreeMap<String, List<InstantiatedLineTuple>> instantiateLines(TreeMap<Float, Float> newlines, TreeMap<String, List<LineTuple>> partLines,
                                                                         List<Float> lineLengths, List<Float> lineOffsets, TreeMap<String, ParsingPartTuple> parsingPart) {
        TreeMap<String, List<InstantiatedLineTuple>> finalLines = new TreeMap<>();
        List<Float> newlinesList = newlines.keySet().stream().toList();

        StaveLineAverager averager = new MeanAverager();

        for (Map.Entry<String, List<LineTuple>> part : partLines.entrySet()) {
            finalLines.put(part.getKey(), new ArrayList<>());
            averager.reset();

            List<HashMap<Integer, HashMap<Integer, TreeMap<Float, Chord>>>> chords = new ArrayList<>();
            List<HashMap<Integer, HashMap<Integer, TreeMap<Float, Whitespace>>>> rests = new ArrayList<>();
            List<HashMap<Integer, HashMap<Integer, Map<Chord, BeamletInfo>>>> needsFlag = new ArrayList<>();
            List<HashMap<Integer, HashMap<Integer, Map<Chord, BeamletInfo>>>> needsBeamlet = new ArrayList<>();
            for (int i = 0; i < part.getValue().size(); ++i) {

                chords.add(new HashMap<>());
                rests.add(new HashMap<>());
                needsFlag.add(new HashMap<>());
                needsBeamlet.add(new HashMap<>());

                Line tempLine = new Line(new ArrayList<>(), newlinesList.get(i), lineLengths.get(i), lineOffsets.get(i), i,
                        part.getValue().get(i).extendUp, part.getValue().get(i).extendDown);
                finalLines.get(part.getKey()).add(new InstantiatedLineTuple(newlinesList.get(i), tempLine));

                // add the rests
                for (var staffEntry : part.getValue().get(i).rests.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), () -> new Stave(tempLine, tempLine.getStaves().size()), staffEntry.getKey() - 1);
                    chords.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    rests.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    needsFlag.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    needsBeamlet.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        chords.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new TreeMap<>());
                        rests.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new TreeMap<>());
                        needsFlag.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new HashMap<>());
                        needsBeamlet.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new HashMap<>());
                        var fusedRests = InstantiatedRestTuple.fuseRestTuples(voiceEntry.getValue().values().stream().toList());
                        for (InstantiatedRestTuple restTuple : fusedRests) {
                            tempLine.getStaves().get(staffEntry.getKey() - 1)
                                    .addWhiteSpace(restTuple.toRest(tempLine.getStaves().get(staffEntry.getKey() - 1), rests.get(i)));
                        }
                    }
                }

                // add the beam groups
                for (var staffEntry : part.getValue().get(i).beamGroups.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), () -> new Stave(tempLine, tempLine.getStaves().size()), staffEntry.getKey() - 1);
                    chords.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    rests.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    needsFlag.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    needsBeamlet.get(i).putIfAbsent(staffEntry.getKey(), new HashMap<>());
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        chords.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new TreeMap<>());
                        rests.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new TreeMap<>());
                        needsFlag.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new HashMap<>());
                        needsBeamlet.get(i).get(staffEntry.getKey()).putIfAbsent(voiceEntry.getKey(), new HashMap<>());
                        for (var beamTuple : voiceEntry.getValue().values()) {
                            beamTuple.addToAverager(averager);
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addStaveElement(beamTuple.toBeamGroup(tempLine.getStaves().get(staffEntry.getKey() - 1), chords.get(i), needsFlag.get(i), needsBeamlet.get(i)));
                        }
                    }
                }

                // add the pulse lines
                for (InstantiatedPulseLineTuple pulseTuple : part.getValue().get(i).pulses) {
                    tempLine.addPulseLine(pulseTuple.toPulseLine(tempLine));
                }

                // add the music groups
                for (var staffEntry : part.getValue().get(i).musicGroups.entrySet()) {
                    Util.ensureCapacity(tempLine.getStaves(), () -> new Stave(tempLine, tempLine.getStaves().size()), staffEntry.getKey() - 1);
                    for (InstantiatedMusicGroupTuple musicGroupTuple : staffEntry.getValue()) {
                        tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(musicGroupTuple.toMusicGroup(
                                tempLine.getStaves().get(staffEntry.getKey() - 1), chords.get(i)));
                    }
                }

                // add the tempo markings
                for (InstantiatedTempoTuple tempoTuple : part.getValue().get(i).tempoMarkings) {
                    tempLine.getStaves().get(0).addMusicGroup(tempoTuple.toMusicGroup(tempLine.getStaves().get(0), chords.get(i)));
                }
            }

            // decide which way round the stems should go
            parsingPart.get(part.getKey()).upwardsStems = averager.getAverageStaveLine() < 4;

            // remove pre ties on notes that do not start a line
            for (var lineEntry : chords) {
                for (var staffEntry : lineEntry.entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var chordEntry : voiceEntry.getValue().entrySet()) {
                            if (chordEntry.getKey() > voiceEntry.getValue().firstKey()) {
                                chordEntry.getValue().removeTiesTo();
                            }
                        }
                    }
                }
            }

            // add left beam segments
            HashMap<Integer, TreeMap<Float, Chord>> emptyStaffChords = new HashMap<>();
            HashMap<Integer, TreeMap<Float, Whitespace>> emptyStaffRests = new HashMap<>();
            TreeMap<Float, Chord> emptyVoiceChords = new TreeMap<>();
            TreeMap<Float, Whitespace> emptyVoiceRests = new TreeMap<>();
            for (int i = 0; i < needsFlag.size(); ++i) {
                for (var staffEntry : needsFlag.get(i).entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var entry : voiceEntry.getValue().entrySet()) {
                            Line tempLine = finalLines.get(part.getKey()).get(i).line;
                            boolean prevLine = false;
                            var preEntry = chords.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).lowerEntry(entry.getKey().getCrotchetsIntoLine());
                            var preChord = preEntry == null ? null : preEntry.getValue();
                            var preRestEntry = rests.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).floorEntry(entry.getKey().getCrotchetsIntoLine());
                            var preRest = preRestEntry == null ? null : preRestEntry.getValue();
                            if (preChord == null && i > 0) {
                                preEntry = chords.get(i-1).getOrDefault(staffEntry.getKey(), emptyStaffChords).getOrDefault(voiceEntry.getKey(), emptyVoiceChords).lastEntry();
                                if (preEntry != null) {
                                    preChord = preEntry.getValue().moveToNextLine(tempLine);
                                    prevLine = true;
                                }
                            }
                            if (preRest == null && i > 0) {
                                preRestEntry = rests.get(i-1).getOrDefault(staffEntry.getKey(), emptyStaffRests).getOrDefault(voiceEntry.getKey(), emptyVoiceRests).lastEntry();
                                if (preRestEntry != null) {
                                    preRest = preRestEntry.getValue().moveToNextLine(tempLine);
                                }
                            }
                            if (preRest != null && preChord != null && preRest.getEndCrotchets() > preChord.getCrotchetsIntoLine()) {
                                preChord = null;
                            }
                            if (preChord != null && prevLine) {
                                tempLine.getStaves().get(staffEntry.getKey() - 1).addStaveElement(preChord);
                            }
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(new LeftBeamSegment(preChord, entry.getKey(), tempLine,
                                    tempLine.getStaves().get(staffEntry.getKey() - 1),
                                    entry.getValue().number(), entry.getValue().flag()));
                        }
                    }
                }
            }

            // add right beam segments
            for (int i = 0; i < needsFlag.size(); ++i) {
                for (var staffEntry : needsBeamlet.get(i).entrySet()) {
                    for (var voiceEntry : staffEntry.getValue().entrySet()) {
                        for (var entry : voiceEntry.getValue().entrySet()) {
                            Line tempLine = finalLines.get(part.getKey()).get(i).line;
                            boolean nextLine = false;
                            var postEntry = chords.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).higherEntry(entry.getKey().getCrotchetsIntoLine());
                            var postChord = postEntry == null ? null : postEntry.getValue();
                            var postRestEntry = rests.get(i).get(staffEntry.getKey()).get(voiceEntry.getKey()).ceilingEntry(entry.getKey().getCrotchetsIntoLine());
                            var postRest = postRestEntry == null ? null : postRestEntry.getValue();
                            if (postChord == null && i < chords.size() - 1) {
                                postEntry = chords.get(i+1).getOrDefault(staffEntry.getKey(), emptyStaffChords).getOrDefault(voiceEntry.getKey(), emptyVoiceChords).firstEntry();
                                if (postEntry != null) {
                                    postChord = postEntry.getValue().moveToPrevLine(tempLine);
                                    nextLine = true;
                                }
                            }
                            if (postRest == null && i < chords.size() - 1) {
                                postRestEntry = rests.get(i+1).getOrDefault(staffEntry.getKey(), emptyStaffRests).getOrDefault(voiceEntry.getKey(), emptyVoiceRests).firstEntry();
                                if (postRestEntry != null) {
                                    postRest = postRestEntry.getValue().moveToPrevLine(tempLine);
                                }
                            }
                            if (postRest != null && postChord != null && postRest.getStartCrotchets() < postChord.getCrotchetsIntoLine()) {
                                postChord = null;
                            }
                            if (postChord != null && nextLine) {
                                tempLine.getStaves().get(staffEntry.getKey() - 1).addStaveElement(postChord);
                            }
                            tempLine.getStaves().get(staffEntry.getKey() - 1).addMusicGroup(new RightBeamSegment(postChord, entry.getKey(), tempLine,
                                    tempLine.getStaves().get(staffEntry.getKey() - 1),
                                    entry.getValue().number(), entry.getValue().flag()));
                        }
                    }
                }
            }
        }
        return finalLines;
    }

    /**
     * Collates the lines into groups according to their sections.
     * @param partSections the map of lists of lines
     * @param finalLines the map of list of instantiated lines
     * @param newSections the set of new sections
     * @param sectionIndices the map converting section start times to section indices
     * @return a map of lists of maps of lines
     */
    static TreeMap<String, List<TreeMap<Float, Line>>> populatePartSections(TreeMap<String, List<TreeMap<Float, Line>>> partSections,
                                                                            TreeMap<String, List<InstantiatedLineTuple>> finalLines,
                                                                            TreeSet<Float> newSections, Map<Float, Integer> sectionIndices) {
        for (Map.Entry<String, List<InstantiatedLineTuple>> part : finalLines.entrySet()) {
            for (InstantiatedLineTuple instantiatedLineTuple : part.getValue()) {
                Float capturedStart = newSections.floor(instantiatedLineTuple.startTime);
                float sectionStart = capturedStart == null ? 0 : capturedStart;
                int sectionNum = sectionIndices.get(sectionStart);
                partSections.get(part.getKey()).get(sectionNum).put(instantiatedLineTuple.startTime, instantiatedLineTuple.line);
            }
        }
        return partSections;
    }

    /**
     * Groups the collated line sections in a map of lists of sections.
     * @param partSections the collated lines
     * @param parsingParts the parsing information
     * @return the map of lists of sections
     */
    static TreeMap<String, List<Section>> finaliseSections(TreeMap<String, List<TreeMap<Float, Line>>> partSections, TreeMap<String, ParsingPartTuple> parsingParts) {
        TreeMap<String, List<Section>> finalSections = new TreeMap<>();

        for (Map.Entry<String, List<TreeMap<Float, Line>>> part : partSections.entrySet()) {
            List<Section> sections = new ArrayList<>();
            for (TreeMap<Float, Line> lines : part.getValue()) {
                if (lines.size() != 0) {
                    ArrayList<Clef> clefs = new ArrayList<>();
                    for (int i = 0; i < lines.firstEntry().getValue().getStaves().size(); ++i) {
                        clefs.add(parsingParts.get(part.getKey()).clefs.get(i + 1).floorEntry(lines.firstKey()).getValue());
                    }
                    sections.add(new Section(lines.values().stream().toList(),
                            clefs,
                            parsingParts.get(part.getKey()).keySignatures.floorEntry(lines.firstKey()).getValue()));
                }
            }
            finalSections.put(part.getKey(), sections);
        }
        return finalSections;
    }

    /**
     * Gets the work title of a score.
     * @param score the score to get the work title from
     * @return the work title
     */
    static String getWorkTitle(ScorePartwise score) {
        if (score.getWork() != null && score.getWork().getWorkTitle() != null) {
            return score.getWork().getWorkTitle();
        }
        return "";
    }

    /**
     * Gets the composer of a score.
     * @param score the score to get the composer from
     * @return the composer
     */
    static String getComposer(ScorePartwise score) {
        if (score.getIdentification() != null && score.getIdentification().getCreator() != null) {
            var composers = new ArrayList<String>();
            for (TypedText text : score.getIdentification().getCreator()) {
                if (text.getType().equals("composer")) {
                    composers.add(text.getValue());
                }
            }
            return String.join(", ", composers);
        }
        return "";
    }

    /**
     * Converts a given time into a normalised time.
     * The initial time is given in crotchets since the start of the piece.
     * The resulting time is the normalised time, which may be tempo normalised depending on the parsing settings.
     * @param time the crotchets from the start of the piece
     * @param integratedTime the list of integrated time changes
     * @return the normalised time
     */
    static float normaliseTime(float time, TreeMap<Float, TempoChangeTuple> integratedTime) {
        if (TIME_NORMALISED_PARSING) {
            var timeEntry = integratedTime.floorEntry(time);
            if (timeEntry != null) {
                return timeEntry.getValue().modulateTime(time);
            } else {
                return integratedTime.firstEntry().getValue().modulateTime(time);
            }
        } else {
            return time;
        }
    }

    /**
     * Converts a given duration at a given time into a normalised time.
     * The initial time is given in crotchets since the start of the piece. The duration is given in crotchets.
     * The resulting duration is the normalised duration, which may be tempo normalised depending on the parsing settings.
     * @param time the crotchets from the start of the piece
     * @param duration the duration to be normalised
     * @param integratedTime the list of integrated time changes
     * @return the normalised time
     */
    static float normaliseDuration(float time, float duration, TreeMap<Float, TempoChangeTuple> integratedTime) {
        var startTime = normaliseTime(time, integratedTime);
        var endTime = normaliseTime(time + duration, integratedTime);
        return endTime - startTime;
    }

    /**
     * Translates a pitch into the number of spaces and lines, that is named pitches, above the root of C0.
     * @param pitch the pitch to translate
     * @return the number of spaces and lines above the root of C0
     */
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

    /**
     * Translates a pitch into the number of spaces and lines, that is named pitches, above the root of C0.
     * @param unpitched the pitch to translate
     * @return the number of spaces and lines above the root of C0
     */
    static int pitchToGrandStaveLine(Unpitched unpitched) {
        return switch (unpitched.getDisplayStep()) {
            case C -> 0;
            case D -> 1;
            case E -> 2;
            case F -> 3;
            case G -> 4;
            case A -> 5;
            case B -> 6;
        } + 7 * unpitched.getDisplayOctave();
    }

    /**
     * Translates a pitch into the number of spaces and lines, that is named pitches, above the root of C0.
     * @param step the pitch name
     * @param octave the octave of the pitch
     * @return the number of spaces and lines above the root of C0
     */
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

    static uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType convertNoteType(NoteType noteType) {
        return convertNoteType(noteType.getValue());
    }

    static uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType convertNoteType(String noteType) {
        return switch (noteType) {
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

    /**
     * Determines if a direction marks a newline.
     * If so, it calls addNewline with the line start time and offset, and then returns true.
     * Otherwise, it returns false.
     * @param formattedText the text being checked
     * @param time the time of the text
     * @param offset the offset for the newline
     * @param addNewline a function to add a newline
     * @return whether a newline was successfully added or not
     */
    static boolean isNewline(FormattedTextId formattedText, float time, float offset, BiConsumer<Float, Float> addNewline) {
        String text = formattedText.getValue().toLowerCase();
        if (text.equals("n") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            addNewline.accept(time, offset);
            return true;
        } else if (text.equals("\\n")) {
            addNewline.accept(time, offset);
            return true;
        }
        return false;
    }


    /**
     * Determines if a direction marks a new section.
     * If so, it calls addNewSection with the line start time and offset, and then returns true.
     * Otherwise, it returns false.
     * @param formattedText the text being checked
     * @param time the time of the text
     * @param offset the offset for the newline
     * @param addNewSection a function to add a new section
     * @return whether a new section was successfully added or not
     */
    static boolean isNewSection(FormattedTextId formattedText, float time, float offset, BiConsumer<Float, Float> addNewSection) {
        String text = formattedText.getValue().toLowerCase();
        if (text.equals("s") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            addNewSection.accept(time, offset);
            return true;
        } else if (text.equals("\\s")) {
            addNewSection.accept(time, offset);
            return true;
        }
        return false;
    }

    /**
     * Determines if a direction is a pulse directive, changing the pulse line layout.
     * If so, it changes the pulse settings on the time signature, adds the change, and return true.
     * Otherwise, it returns false.
     * @param formattedText the text being checked
     * @param time the time of the text
     * @param beatChanges a record of all the beat changes in the score
     * @param timeSig the current time signature
     * @return whether this was a pulse line directive or not
     */
    static boolean isPulseDirective(FormattedTextId formattedText, float time, TreeMap<Float, List<TimeSignature.BeatTuple>> beatChanges, TimeSignature timeSig) {
        String text = formattedText.getValue().toLowerCase();
        String pulseRules;
        if (text.startsWith("t") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            pulseRules = text.substring(1);
        } else if (text.startsWith("\\t")) {
            pulseRules = text.substring(2);
        } else {
            return false;
        }
        try {
            var beats = new ArrayList<TimeSignature.BeatTuple>();
            String[] beatInfos = pulseRules.split("\\|");
            for (var beatInfo : beatInfos) {
                String[] splitBeat = beatInfo.split("/");
                if (splitBeat.length == 1) {
                    if (splitBeat[0].equals("")) {
                        return false;
                    } else {
                        beats.add(new TimeSignature.BeatTuple(Integer.parseInt(splitBeat[0]),
                                timeSig.getBeatType(), 1));
                    }
                } else if (splitBeat.length == 2) {
                    beats.add(new TimeSignature.BeatTuple(Integer.parseInt(splitBeat[0]),
                            timeSig.getBeatType(),
                            Integer.parseInt(splitBeat[1])));
                } else if (splitBeat.length == 3) {
                    beats.add(new TimeSignature.BeatTuple(Integer.parseInt(splitBeat[0]),
                            Integer.parseInt(splitBeat[2]),
                            Integer.parseInt(splitBeat[1])));
                }
            }
            beatChanges.put(time, beats);
            timeSig.setBeatPattern(beats);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Determines if a direction is for adding a breath whitespace or not.
     * If so, it calls addArtisticWhitespace with the voice number and the start time and returns true.
     * Otherwise, it returns false.
     * @param formattedText the text being checked
     * @param time the time of the text
     * @param addArtisticWhitespace the function to add breath whitespace
     * @return whether the directive added a breath whitespace or not
     */
    static boolean isArtisticWhitespace(FormattedTextId formattedText, float time, BiConsumer<Integer, Float> addArtisticWhitespace) {
        String text = formattedText.getValue().toLowerCase();
        if (text.startsWith("w") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            try {
                if (text.length() == 1) {
                    addArtisticWhitespace.accept(1, time);
                    return true;
                }
                addArtisticWhitespace.accept(Integer.parseInt(formattedText.getValue().substring(1)), time);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (text.startsWith(("\\w"))) {
            try {
                if (text.length() == 2) {
                    addArtisticWhitespace.accept(1, time);
                    return true;
                }
                addArtisticWhitespace.accept(Integer.parseInt(formattedText.getValue().substring(2)), time);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Determines if a directive is a capital note directive.
     * If so, it adds the capital note and returns true.
     * Otherwise, it returns false.
     * @param formattedText the text being checked
     * @param time the time of the text
     * @param addCapitalNote a function to add a capital note
     * @return whether a capital note was added or not
     */
    static boolean isCapitalNote(FormattedTextId formattedText, float time, BiConsumer<Integer, Float> addCapitalNote) {
        String text = formattedText.getValue().toLowerCase();
        if (text.startsWith("c") && formattedText.getEnclosure() == EnclosureShape.RECTANGLE) {
            try {
                if (text.length() == 1) {
                    addCapitalNote.accept(1, time);
                    return true;
                }
                addCapitalNote.accept(Integer.parseInt(formattedText.getValue().substring(1)), time);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (text.startsWith(("\\c"))) {
            try {
                if (text.length() == 2) {
                    addCapitalNote.accept(1, time);
                    return true;
                }
                addCapitalNote.accept(Integer.parseInt(formattedText.getValue().substring(2)), time);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    static List<MusicGroupType> wedgeGroups = new ArrayList<>(2) {{ add(MusicGroupType.DIM); add(MusicGroupType.CRESC); }};

    /**
     * Parses a music directive.
     * @param target the music group tuples to add to
     * @param currentPart the current part being parsed
     * @param direction the direction to parse
     * @param time the time the direction occurs at
     * @param newlineOffset the time offset a newline would need
     * @param beatChanges the beat changes map used to track all beat changes in a score
     * @param timeSig the current time signature
     * @param addNewline the function to add newlines
     * @param addNewSection the function to add new sections
     * @param addArtisticWhitespace the function to add breath whitespaces
     * @param addCapital the function to add capital notes
     */
    static void parseMusicDirective(TreeMap<MusicGroupType, TreeMap<Integer, MusicGroupTuple>> target, ParsingPartTuple currentPart, Direction direction,
                                    float time, float newlineOffset,
                                    TreeMap<Float, List<TimeSignature.BeatTuple>> beatChanges, TimeSignature timeSig,
                                    BiConsumer<Float, Float> addNewline, BiConsumer<Float, Float> addNewSection,
                                    BiConsumer<Integer, Float> addArtisticWhitespace, BiConsumer<Integer, Float> addCapital) {
        if (direction.getDirectionType() != null) {
            for (DirectionType directionType : direction.getDirectionType()) {
                if (directionType.getWedge() != null) {
                    Wedge wedge = directionType.getWedge();
                    switch (wedge.getType().name()) {
                        case "DIMINUENDO" -> target.get(MusicGroupType.DIM).put(wedge.getNumber(), new MusicGroupTuple(time, MusicGroupType.DIM, getStaff(direction.getStaff())));
                        case "CRESCENDO" -> target.get(MusicGroupType.CRESC).put(wedge.getNumber(), new MusicGroupTuple(time, MusicGroupType.CRESC, getStaff(direction.getStaff())));
                        case "STOP" -> {
                            for (var type : wedgeGroups) {
                                if (target.get(type).containsKey(wedge.getNumber())) {
                                    var tuple = target.get(type).remove(wedge.getNumber());
                                    tuple.endTime = time;
                                    tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                                    currentPart.putInMusicGroup(tuple);
                                    break;
                                }
                            }
                        }
                    }
                }
                if (directionType.getDynamics() != null) {
                    for (var dynamics : directionType.getDynamics()) {
                        if (dynamics.getPOrPpOrPpp() != null) {
                            for (var element : dynamics.getPOrPpOrPpp()) {
                                var tuple = new MusicGroupTuple(time, MusicGroupType.DYNAMIC, getStaff(direction.getStaff()));
                                tuple.endTime = time;
                                tuple.text = element.getName().getLocalPart();
                                tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                                currentPart.putInMusicGroup(tuple);
                            }
                        }
                    }
                }
                if (directionType.getWordsOrSymbol() != null) {
                    for (var wordOrSymbol : directionType.getWordsOrSymbol()) {
                        if (wordOrSymbol instanceof FormattedTextId text) {
                            if (!isPulseDirective(text, time, beatChanges, timeSig)
                                    && !isNewline(text, time, newlineOffset, addNewline)
                                    && !isNewSection(text, time, newlineOffset, addNewSection)
                                    && !isArtisticWhitespace(text, time, addArtisticWhitespace)
                                    && !isCapitalNote(text, time, addCapital)) {
                                var tuple = new MusicGroupTuple(time, MusicGroupType.TEXT, getStaff(direction.getStaff()));
                                tuple.endTime = time;
                                tuple.text = text.getValue();
                                tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                                currentPart.putInMusicGroup(tuple);
                            }
                        }
                    }
                }
                if (directionType.getCoda() != null) {
                    for (var coda : directionType.getCoda()) {
                        var tuple = new MusicGroupTuple(time + ADD_TO_CODA, MusicGroupType.CODA, getStaff(direction.getStaff()));
                        tuple.endTime = time + ADD_TO_CODA;
                        tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                        currentPart.putInMusicGroup(tuple);
                        currentPart.putInMusicGroup(tuple);
                    }
                }
                if (directionType.getSegno() != null) {
                    for (var segno : directionType.getSegno()) {
                        var tuple = new MusicGroupTuple(time + ADD_TO_CODA, MusicGroupType.SEGNO, getStaff(direction.getStaff()));
                        tuple.endTime = time + ADD_TO_CODA;
                        tuple.aboveStave = direction.getPlacement() == AboveBelow.ABOVE || direction.getPlacement() == null;
                        currentPart.putInMusicGroup(tuple);
                        currentPart.putInMusicGroup(tuple);
                    }
                }
            }
        }
    }

    /**
     * Parses a tempo marking, and returns the new tempo.
     * @param tempoMarkings the tempo marking to parse
     * @param tempoChanges the map of tempo changes
     * @param currentTempo the current tempo
     * @param direction the direction the tempo was in
     * @param time the time the tempo marking occurs at
     * @return the new tempo
     */
    static float parseTempoMarking(TreeMap<Float, TempoTuple> tempoMarkings, TreeMap<Float, Float> tempoChanges, float currentTempo, Direction direction, float time) {
        if (direction.getDirectionType() != null) {
            for (DirectionType directionType : direction.getDirectionType()) {
                if (directionType.getMetronome() != null) {
                    Metronome met = directionType.getMetronome();
                    uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType leftItem = uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET;
                    int leftDots = 0;
                    boolean seenLeft = false;
                    uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType rightItem = uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType.CROTCHET;
                    int rightDots = 0;
                    boolean seenRight = false;
                    if (met.getBeatUnit() != null) {
                        for (var unit : met.getBeatUnit()) {
                            if (!seenLeft) {
                                if (unit instanceof String noteString) {
                                    leftItem = convertNoteType(noteString);
                                    seenLeft = true;
                                }
                            } else if (!seenRight) {
                                if (unit instanceof Empty) {
                                    leftDots += 1;
                                } else if (unit instanceof String noteString) {
                                    rightItem = convertNoteType(noteString);
                                    seenRight = true;
                                }
                            } else {
                                if (unit instanceof Empty) {
                                    rightDots += 1;
                                }
                            }
                        }
                    }
                    if (met.getPerMinute() != null) {
                        TempoTuple tuple = new TempoTuple(leftItem, leftDots, met.getPerMinute().getValue(), time);
                        tempoMarkings.put(time, tuple);
                        tempoChanges.put(time, tuple.bpmValue);
                        return tuple.bpmValue;
                    } else {
                        TempoTuple tuple = new TempoTuple(leftItem, leftDots, rightItem, rightDots, currentTempo, time);
                        tempoMarkings.put(time, tuple);
                        tempoChanges.put(time, tuple.bpmValue);
                        return tuple.bpmValue;
                    }
                }
            }
        }
        return currentTempo;
    }

    /**
     * Parses slurs and ties from within notes.
     * @param target the music group map to add to
     * @param currentPart the current part being parsed
     * @param divisions the current time divisions
     * @param note the note being processed
     * @param time the time the note starts at
     */
    static void parseSlurs(TreeMap<MusicGroupType, TreeMap<Integer, MusicGroupTuple>> target, ParsingPartTuple currentPart, int divisions, Note note, float time) {
        if (note.getNotations() != null) {
            for (var notation : note.getNotations()) {
                for (var s : notation.getTiedOrSlurOrTuplet()) {
                    if (s instanceof Slur slur) {
                        switch (slur.getType()) {
                            case STOP -> {
                                if (target.get(MusicGroupType.SLUR).containsKey(slur.getNumber())) {
                                    var tuple = target.get(MusicGroupType.SLUR).remove(slur.getNumber());
                                    tuple.endTime = time;
                                    currentPart.putInMusicGroup(tuple);
                                }
                            }
                            case START -> {
                                MusicGroupTuple tuple = new MusicGroupTuple(time, MusicGroupType.SLUR, getStaff(note.getStaff()));
                                tuple.voice = getVoice(note.getVoice());
                                target.get(MusicGroupType.SLUR).put(slur.getNumber(), tuple);
                            }
                        }
                    } else if (s instanceof Tuplet tuplet) {
                        switch (tuplet.getType()) {
                            case STOP -> {
                                int tupletNum = tuplet.getNumber() == null ? 1 : tuplet.getNumber();
                                if (target.get(MusicGroupType.TUPLET).containsKey(tupletNum)) {
                                    var tuple = target.get(MusicGroupType.TUPLET).remove(tupletNum);
                                    tuple.endTime = time + (RenderingConfiguration.tupletFillPeriod ? note.getDuration().intValue() / (float)divisions : 0);
                                    currentPart.putInMusicGroup(tuple);
                                }
                            }
                            case START -> {
                                int tupletNum = tuplet.getNumber() == null ? 1 : tuplet.getNumber();
                                MusicGroupTuple tuple = new MusicGroupTuple(time, MusicGroupType.TUPLET, getStaff(note.getStaff()));
                                if (tuplet.getTupletActual() != null) {
                                    tuple.num = tuplet.getTupletActual().getTupletNumber().getValue().intValue();
                                } else if (note.getTimeModification() != null) {
                                    tuple.num = note.getTimeModification().getActualNotes().intValue();
                                } else {
                                    tuple.num = 1;
                                }
                                tuple.bool = tuplet.getBracket() == YesNo.YES;
                                tuple.voice = getVoice(note.getVoice());
                                target.get(MusicGroupType.TUPLET).put(tupletNum, tuple);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses time signatures.
     * @param timeSignature the time signature to parse
     * @return the parsed time signature.
     */
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
            ret.setBeatPatternToDefault();
            return ret;
        }
        return null;
    }

    /**
     * Parses key signatures.
     * @param keySignature the key signature to parse
     * @param currentKeySignature the current key signature
     * @return the parsed key signature.
     */
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

    /**
     * Gets the grand stave line (named pitches above C0) for the lowest stave line for a stave marked by the given clef
     * @param clef the given clef
     * @return the lowest grand stave line of the lowest stave line
     */
    static int clefToLowestLineGrandStaveLine(uk.ac.cam.optimisingmusicnotation.representation.properties.Clef clef) {
        return switch (clef.getSign()) {
            case C -> pitchToGrandStaveLine(Step.C, 4);
            case F -> pitchToGrandStaveLine(Step.F, 3);
            case G, PERCUSSION -> pitchToGrandStaveLine(Step.G, 4);
            case TAB -> 0;
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

    /**
     * Adds pulse lines after parsing a measure
     * @param timeSig the current time signature
     * @param measureStartTime the start time of the measure
     * @param pulseLines the list to add the pulse lines to
     * @param measureName the name of the measure, typically the bar number
     * @param displaySig the time signature to display, which is null if no time signature needs to be displayed
     */
    static void addPulseLines(TimeSignature timeSig, float measureStartTime, List<PulseLineTuple> pulseLines, String measureName, TimeSignature displaySig) {
        float fromStartTime = 0;
        for (var beatTuple : timeSig.getBeatPattern()) {
            float duration = beatTuple.durationInUnits() * 4f / beatTuple.beatType();
            if (fromStartTime == 0) {
                pulseLines.add(new PulseLineTuple(measureStartTime, measureName, 0, displaySig));
            } else {
                pulseLines.add(new PulseLineTuple(measureStartTime + fromStartTime, measureName, 1, displaySig));
            }
            for (int i = 0; i < beatTuple.subBeats(); ++i) {
                pulseLines.add(new PulseLineTuple(measureStartTime + fromStartTime + duration * i / beatTuple.subBeats(), measureName, 2, displaySig));
            }
            fromStartTime += duration;
        }
    }

    /**
     * Opens a mxl, or musicXML.
     * @param input the file name to be opened
     * @return an object representing the mxl file
     * @throws IOException for a file which cannot be found
     */
    public static Object openMXL(String input) throws IOException {
        try (FileInputStream xml = new FileInputStream(input)) {
            return Marshalling.unmarshal(xml);
        } catch (Marshalling.UnmarshallingException e) {
            String[] splitPath = input.split("[/\\\\]");
            String scoreMxlName = splitPath[splitPath.length - 1];
            String scoreXmlName = scoreMxlName.split("\\.")[0] + ".xml";
            String scoreXmlNameOnlyASCII = scoreXmlName.replaceAll("[^\\x00-\\x7F]", ""); // ZipInputStream will just delete non-ascii characters
            try (ZipInputStream xml = new ZipInputStream(new FileInputStream(input))) {
                ZipEntry zipEntry = xml.getNextEntry();
                while (zipEntry != null) {
                    if(zipEntry.getName().replaceAll("[^\\x00-\\x7F]", "").equals(scoreXmlNameOnlyASCII) || zipEntry.getName().equals("score.xml")) {
                        return Marshalling.unmarshal(xml);
                    }
                    zipEntry = xml.getNextEntry();
                }
                return null;
            } catch (Marshalling.UnmarshallingException ex) {
                System.err.println("Problem with xml file.");
                throw new RuntimeException(ex);
            }
        }
    }

    private Parser() {}
}
