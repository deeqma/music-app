#!/bin/sh

MP3_DIR="/app/music/songs"

mkdir -p "$MP3_DIR"

if [ -z "$(ls -A $MP3_DIR 2>/dev/null)" ]; then
    echo "Copying default MP3s to $MP3_DIR"
    cp /app/default-songs/*.mp3 "$MP3_DIR/"
fi

exec java -jar app.jar