# Music Application

A personal self-hosted music platform. Upload your own MP3 collection, stream it using your or desktop app offline,
All your MP3 files stays on your local machine.

## Features

### Songs
- Upload MP3 files with metadata (song name, artist, album, genre, release year)
- File is validated on upload, must be MP3, max 30MB, no duplicate files (checked via SHA-256 hash)
- Stream songs 
- Edit song metadata (song name, artist, album, genre, release year)
- Delete songs (removes from database and disk)
- Search songs by name or artist
- Filter songs by genre, artist, album, release year range
- All with pagination

### Liked Songs
- Like or unlike any song
- View all your liked songs
- Filter liked songs by genre, artist, album, release year
- Liked songs are per user, no one else sees what you liked

### Playlists
- Create playlists with a name, description, and visibility (public or private)
- Each user can create up to 30 playlists
- No duplicate playlist names per user
- Add and remove songs from your own playlists
- Toggle visibility between public and private
- Share a private playlist via a unique generated link (YouTube-style token)
- Public playlists are visible and searchable by everyone
- Private playlists are only visible to the owner or anyone with the share link
- Filter and search songs inside a playlist
- Playlist URL supports slug-based routing (e.g. `/playlist/hard-rock`)

### Authentication
- Register and login with simple username and password
- JWT-based authentication using HS256
- All endpoints are protected except registration and login

## Tech Stack

* Java 25
* Spring Boot 4+
* Spring Security + OAuth2 Resource Server (JWT)
* Docker
* PostgreSQL + Spring JPA
* Audio Metadata jaudiotagger
* Maven
* JUnit 6, Mockito, Testcontainers

## Getting Started backend

- open terminal

```bash
git clone https://github.com/deeqma/music-app.git
```
```bash
cd music-app
```

```bash
docker compose up -d --build
```

### Run the backend with dev profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

# Requirements

- docker installed
- java 25
- Maven installed
- IDEA (optional)
