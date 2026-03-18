package io.github.deeqma.music;

import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.model.Song;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

public class SongTestData {

    public static Song highwayStar() {
        Song song = new Song();
        song.setSongName("Highway Star");
        song.setArtistName("Deep Purple");
        song.setAlbum("Machine Head");
        song.setGenre("Hard Rock");
        song.setReleaseYear(1972);
        song.setFilePath("mock/highway-star.mp3");
        song.setFileHash("hash-highway-star");
        song.setDurationSeconds(370);
        return song;
    }

    public static Song smokeOnTheWater() {
        Song song = new Song();
        song.setSongName("Smoke on the Water");
        song.setArtistName("Deep Purple");
        song.setAlbum("Machine Head");
        song.setGenre("Hard Rock");
        song.setReleaseYear(1972);
        song.setFilePath("mock/smoke-on-the-water.mp3");
        song.setFileHash("hash-smoke-on-the-water");
        song.setDurationSeconds(340);
        return song;
    }

    public static Song laGrange() {
        Song song = new Song();
        song.setSongName("La Grange");
        song.setArtistName("ZZ Top");
        song.setAlbum("Tres Hombres");
        song.setGenre("Blues Rock");
        song.setReleaseYear(1973);
        song.setFilePath("mock/la-grange.mp3");
        song.setFileHash("hash-la-grange");
        song.setDurationSeconds(330);
        return song;
    }

    public static Song sharpDressedMan() {
        Song song = new Song();
        song.setSongName("Sharp Dressed Man");
        song.setArtistName("ZZ Top");
        song.setAlbum("Eliminator");
        song.setGenre("Rock");
        song.setReleaseYear(1983);
        song.setFilePath("mock/sharp-dressed-man.mp3");
        song.setFileHash("hash-sharp-dressed-man");
        song.setDurationSeconds(280);
        return song;
    }

    public static Song enterSandman() {
        Song song = new Song();
        song.setSongName("Enter Sandman");
        song.setArtistName("Metallica");
        song.setAlbum("Metallica");
        song.setGenre("Metal");
        song.setReleaseYear(1991);
        song.setFilePath("mock/enter-sandman.mp3");
        song.setFileHash("hash-enter-sandman");
        song.setDurationSeconds(331);
        return song;
    }

    public static List<Song> all() {
        return List.of(
                highwayStar(),
                smokeOnTheWater(),
                laGrange(),
                sharpDressedMan(),
                enterSandman()
        );
    }

    public static SongDto highwayStarDto() {
        SongDto dto = new SongDto();
        dto.setId(1L);
        dto.setSongName("Highway Star");
        dto.setArtistName("Deep Purple");
        dto.setAlbum("Machine Head");
        dto.setGenre("Hard Rock");
        dto.setReleaseYear(1972);
        dto.setDurationSeconds(370);
        dto.setFilePath("mock/highway-star.mp3");
        return dto;
    }

    public static MockMultipartFile mockMp3File() {
        return new MockMultipartFile(
                "file",
                "test.mp3",
                "audio/mpeg",
                "mock-mp3-bytes".getBytes()
        );
    }
}
