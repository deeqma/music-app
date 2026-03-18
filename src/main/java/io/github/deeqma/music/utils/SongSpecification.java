package io.github.deeqma.music.utils;

import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.model.Song;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class SongSpecification {

    private SongSpecification(){

    }
    public static Specification<Song> filter(SongFilterDto filterDto) {
        return (root, _, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filterDto.getGenre())) {
                predicates.add(cb.like(cb.lower(root.get("genre")),
                        "%" + filterDto.getGenre().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filterDto.getArtistName())) {
                predicates.add(cb.like(cb.lower(root.get("artistName")),
                        "%" + filterDto.getArtistName().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(filterDto.getAlbum())) {
                predicates.add(cb.like(cb.lower(root.get("album")),
                        "%" + filterDto.getAlbum().toLowerCase() + "%"));
            }

            if (filterDto.getYearFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("releaseYear"), filterDto.getYearFrom()));
            }

            if (filterDto.getYearTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("releaseYear"), filterDto.getYearTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}