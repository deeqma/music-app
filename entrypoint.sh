#!/bin/sh

MP3_DIR="/app/music/songs"
DATA_DIR="/app/music/data"

mkdir -p "$MP3_DIR"
mkdir -p "$DATA_DIR"

if [ -z "$(ls -A $MP3_DIR/*.mp3 2>/dev/null)" ]; then
    echo "No MP3s found in $MP3_DIR, copying defaults..."
    cp /app/default-songs/*.mp3 "$MP3_DIR/"
else
    echo "MP3s already exist in $MP3_DIR, skipping copy"
fi

if [ ! -f "$DATA_DIR/songs.json" ]; then
    echo "No songs.json found in $DATA_DIR, copying default..."
    cp /app/default-data/songs.json "$DATA_DIR/songs.json"
else
    echo "songs.json already exists in $DATA_DIR, using existing file"
fi

exec java -jar app.jar