package Rift.Radio;

import Rift.Radio.model.Song;
import org.junit.jupiter.api.BeforeAll;

public class Tests {
    public final static String FILE_DIRECTORY = "src/test/java/Rift/Radio/songMP3Test/";

    protected final static String SHOT_IN_THE_DARK_MP3 = "AC DC - Shot In The Dark (Official Audio).mp3";
    protected final static String BACK_IN_THE_SADDLE_MP3 = "Aerosmith - Back In The Saddle (Audio).mp3";
    protected final static String SNORTIN_WHISKEY_MP3 = "Snortin’ Whiskey.mp3";
    protected final static String WHISKEY_IN_THE_JAR_MP3 = "Metallica - Whiskey in the jar.mp3";
    protected final static String BAD_TO_THE_BONE_MP3 = "George Thorogood & The Destroyers - Bad To The Bone.mp3";
    protected final static String TOP_SHARP_DRESSED_MAN_MP3 = "ZZ Top Sharp Dressed Man.mp3";

    protected final static String DUPLICATE_SONG = "back in the saddle";
    protected final static String NAME_NONEXISTENT = "Nonexistent";

    protected static Song SONG_SHOT_IN_THE_DARK;
    protected static Song SONG_BACK_IN_THE_SADDLE;
    protected static Song SONG_SNORTING_WHISKEY;
    protected static Song SONG_WHISKEY_IN_THE_JAR;
    protected static Song SONG_BAD_IN_THE_BONE;
    protected static Song SONG_SHARP_DRESSED_MAN;
    @BeforeAll
    protected static void beforeAll() {

         SONG_SHOT_IN_THE_DARK = new Song( "Shot in the dark", "AC DC", "Power Up", "Klassisk rock", 2020, FILE_DIRECTORY + SHOT_IN_THE_DARK_MP3);
         SONG_BACK_IN_THE_SADDLE = new Song("back in the saddle", "aerosmith", "Rocks", "hard Rock", 1976, FILE_DIRECTORY + BACK_IN_THE_SADDLE_MP3);
         SONG_SNORTING_WHISKEY = new Song("Snorting Whiskey", "Pat Travers", "Crash and Burn", "Rock", 1980, FILE_DIRECTORY + SNORTIN_WHISKEY_MP3);
         SONG_WHISKEY_IN_THE_JAR = new Song( "Whiskey in the jar", "Metalica", " Garage Inc.", "Heavy Metal", 1998, FILE_DIRECTORY + WHISKEY_IN_THE_JAR_MP3);
         SONG_BAD_IN_THE_BONE = new Song( "Bad to the Bone", "George Thorogood", "Bad to the Bone", "Hard Rock/Blues", 1982, FILE_DIRECTORY + BAD_TO_THE_BONE_MP3);
         SONG_SHARP_DRESSED_MAN = new Song( "Sharp Dressed Man", "", "", " Blues Rock", 1983, FILE_DIRECTORY + TOP_SHARP_DRESSED_MAN_MP3);
    }
}
