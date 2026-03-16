package io.github.deeqma.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/*Verify Test class working as it should*/
public class TestTests extends Tests {

    @Test
    public void testSongProperties() {
        assertEquals("Shot in the dark", SONG_SHOT_IN_THE_DARK.getSongName());
        assertEquals("AC DC", SONG_SHOT_IN_THE_DARK.getArtistName());
        assertEquals("Power Up", SONG_SHOT_IN_THE_DARK.getAlbum());
        assertEquals("Klassisk rock", SONG_SHOT_IN_THE_DARK.getGenre());
        assertEquals(2020, SONG_SHOT_IN_THE_DARK.getReleaseYear());
        assertEquals(FILE_DIRECTORY + SHOT_IN_THE_DARK_MP3, SONG_SHOT_IN_THE_DARK.getFilePath());

        assertEquals("back in the saddle", SONG_BACK_IN_THE_SADDLE.getSongName());
        assertEquals("aerosmith", SONG_BACK_IN_THE_SADDLE.getArtistName());
        assertEquals("Rocks", SONG_BACK_IN_THE_SADDLE.getAlbum());
        assertEquals("hard Rock", SONG_BACK_IN_THE_SADDLE.getGenre());
        assertEquals(1976, SONG_BACK_IN_THE_SADDLE.getReleaseYear());
        assertEquals(FILE_DIRECTORY + BACK_IN_THE_SADDLE_MP3, SONG_BACK_IN_THE_SADDLE.getFilePath());

        assertEquals("Snorting Whiskey", SONG_SNORTING_WHISKEY.getSongName());
        assertEquals("Pat Travers", SONG_SNORTING_WHISKEY.getArtistName());
        assertEquals("Crash and Burn", SONG_SNORTING_WHISKEY.getAlbum());
        assertEquals("Rock", SONG_SNORTING_WHISKEY.getGenre());
        assertEquals(1980, SONG_SNORTING_WHISKEY.getReleaseYear());
        assertEquals(FILE_DIRECTORY + SNORTIN_WHISKEY_MP3, SONG_SNORTING_WHISKEY.getFilePath());

        assertEquals("Whiskey in the jar", SONG_WHISKEY_IN_THE_JAR.getSongName());
        assertEquals("Metalica", SONG_WHISKEY_IN_THE_JAR.getArtistName());
        assertEquals(" Garage Inc.", SONG_WHISKEY_IN_THE_JAR.getAlbum());
        assertEquals("Heavy Metal", SONG_WHISKEY_IN_THE_JAR.getGenre());
        assertEquals(1998, SONG_WHISKEY_IN_THE_JAR.getReleaseYear());
        assertEquals(FILE_DIRECTORY + WHISKEY_IN_THE_JAR_MP3, SONG_WHISKEY_IN_THE_JAR.getFilePath());

        assertEquals("Bad to the Bone", SONG_BAD_IN_THE_BONE.getSongName());
        assertEquals("George Thorogood", SONG_BAD_IN_THE_BONE.getArtistName());
        assertEquals("Bad to the Bone", SONG_BAD_IN_THE_BONE.getAlbum());
        assertEquals("Hard Rock/Blues", SONG_BAD_IN_THE_BONE.getGenre());
        assertEquals(1982, SONG_BAD_IN_THE_BONE.getReleaseYear());
        assertEquals(FILE_DIRECTORY + BAD_TO_THE_BONE_MP3, SONG_BAD_IN_THE_BONE.getFilePath());

        assertEquals("Sharp Dressed Man", SONG_SHARP_DRESSED_MAN.getSongName());
        assertEquals("", SONG_SHARP_DRESSED_MAN.getArtistName());
        assertEquals("", SONG_SHARP_DRESSED_MAN.getAlbum());
        assertEquals(" Blues Rock", SONG_SHARP_DRESSED_MAN.getGenre());
        assertEquals(1983, SONG_SHARP_DRESSED_MAN.getReleaseYear());
        assertEquals(FILE_DIRECTORY + TOP_SHARP_DRESSED_MAN_MP3, SONG_SHARP_DRESSED_MAN.getFilePath());
    }

    }