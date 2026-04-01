# Music Application

A personal self-hosted music platform.

Import your own MP3 collection into the platform and stream it offline.

## Features

### Songs
- Import MP3 files and fill metadata (song name, artist, album, genre, release year)
- File is validated on upload, must be MP3, max 30MB, no duplicate files (checked via SHA-256 hash)
- Stream songs 
- Edit song metadata (song name, artist, album, genre, release year)
- Delete songs (removes from database and disk)
- Search songs by name or artist
- Filter songs by genre, artist, album, release year range
- All with pagination

### Liked Songs
- Like or unlike any song (no one else sees what you liked)
- View all your liked songs
- Filter liked songs by genre, artist, album, release year

### Playlists
- Create playlists with a name, description, and visibility (public or private)
- Each user can create up to 30 playlists
- No duplicate playlist names per user
- Add and remove songs from your own playlists
- Toggle visibility between public and private (private has share link, public every can see it)
- Share a private playlist via a unique generated link
- Public playlists are visible and searchable by everyone
- Private playlists are only visible to the owner or anyone with the share link
- Filter and search songs inside a playlist
- Share Playlist link, (shared link anyone can view it without login)
- Playlist URL supports slug-based routing (e.g. `/playlist/hard-rock`) except share link

### Authentication
- Register and login with simple username and password
- JWT-based authentication using HS256
- All endpoints are protected except registration,login, playlist share link and stream song API's
- Role based (DELETE, EDIT are for admin role only, normal user wont see this feature)


# Getting Started 

Recommended way of getting started, minimal requirements, 

### Requirements
- docker installed
- git


open terminal and copy past below commands in orders

```bash
git clone https://github.com/deeqma/music-app.git
```
change directory

```bash
cd music-app
```

```bash
docker compose up -d --build
```


Done, Go to browser and past link: `http:localhost:5173`

---

Shut down

```bash
docker compose down
```



# Getting Started
Second way of getting started, requires more things to be installed

### Requirements
* Backend
  * docker installed
  * java 25
  * Maven installed
  * IDEA (optional)
* Frontend
  * Npm
  * Node

---

Open Terminal

```bash
git clone https://github.com/deeqma/music-app.git
```
change directory

```bash
cd music-app
```

Run database

```bash
docker compose up -d database
```

Run Backend
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

change directory & Run Frontend

```bash
cd music-ui
```

```bash
npm install
```

```bash
npm run dev
```

browser url `http://localhost:5173`




---

## Project Tech Stack

* backend
  * Java 25
  * Spring Boot 4+
  * Spring Security + OAuth2 Resource Server (JWT)
  * Docker
  * PostgreSQL + Spring JPA
  * Audio Metadata jaudiotagger
  * Maven
  * JUnit 6, Mockito, Testcontainers
* Frontend
  * React 19
  * Typscript
  * Vite 8
  * Zustand 5
  * ESLint


# Music UI 

* Dark mode (ember theme)

![img_3.png](music-ui/img_3.png)


* Dark mode (forest theme)

![img_1.png](music-ui/img_1.png)