import { useRef, useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import Icon        from './components/Icon'
import heartRaw   from './assets/heart.svg?raw'
import playRaw    from './assets/play.svg?raw'
import pauseRaw   from './assets/pause.svg?raw'
import prevRaw    from './assets/previous.svg?raw'
import nextRaw    from './assets/next.svg?raw'
import shuffleRaw from './assets/shuffle.svg?raw'
import repeatRaw  from './assets/repeat.svg?raw'
import vol0Raw    from './assets/volume-0.svg?raw'
import vol2Raw    from './assets/volume-2.svg?raw'
import vol3Raw    from './assets/volume-3.svg?raw'
import { usePlayerStore } from './store/playerStore'
import { usePlaylistsStore } from './store/playlistsStore'

function getSourceLabel(route: string, playlists: { slug: string; playlistName: string }[]): string {
  if (route === '/explore')      return 'Explore'
  if (route === '/liked-songs')  return 'Liked Songs'
  if (route.startsWith('/playlist/')) {
    const slug = route.slice('/playlist/'.length)
    return playlists.find(p => p.slug === slug)?.playlistName ?? 'Playlist'
  }
  return ''
}

function formatTime(s: number): string {
  if (!isFinite(s) || s < 0) return '0:00'
  const m   = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec.toString().padStart(2, '0')}`
}

export default function AudioContainer() {
  const audioRef    = useRef<HTMLAudioElement>(null)
  const isSeekingRef = useRef(false)

  const [currentTime, setCurrentTime] = useState(0)
  const [duration,    setDuration]    = useState(0)
  const [volume,      setVolume]      = useState(1)
  const [prevVolume,  setPrevVolume]  = useState(1)

  const navigate  = useNavigate()
  const location  = useLocation()
  const playlists = usePlaylistsStore(s => s.playlists)

  const {
    currentSong, isPlaying, likedIds, shuffle, repeatMode, sourceRoute,
    setIsPlaying, toggleLike, playNext, playPrev,
    toggleShuffle, toggleRepeat,
  } = usePlayerStore()

  function handleSongNameClick() {
    if (!currentSong || !sourceRoute) return
    if (location.pathname === sourceRoute) {
      const row = document.querySelector<HTMLElement>(`[data-song-id="${currentSong.id}"]`)
      row?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    } else {
      navigate(sourceRoute, { state: { scrollToSongId: currentSong.id } })
    }
  }

  const isLiked = currentSong ? likedIds.has(currentSong.id) : false

  // Reset timers when audio unloads a track (fires on load())
  function handleEmptied() {
    setCurrentTime(0)
    setDuration(0)
  }

  // Load + auto-play when currentSong changes
  useEffect(() => {
    const audio = audioRef.current
    if (!audio || !currentSong) return
    audio.src = currentSong.filePath
    audio.load()
    audio.play().catch(() => {})
  }, [currentSong?.id])  // eslint-disable-line react-hooks/exhaustive-deps

  // Sync play/pause state
  useEffect(() => {
    const audio = audioRef.current
    if (!audio || !currentSong) return
    if (isPlaying) audio.play().catch(() => {})
    else           audio.pause()
  }, [isPlaying])  // eslint-disable-line react-hooks/exhaustive-deps

  // Audio event handlers

  function handleTimeUpdate() {
    // Skip state update while the user is dragging the seek bar
    if (!isSeekingRef.current && audioRef.current) {
      setCurrentTime(audioRef.current.currentTime)
    }
  }

  function handleLoadedMetadata() {
    if (audioRef.current) setDuration(audioRef.current.duration || 0)
  }

  function handleEnded() {
    if (repeatMode === 'one' && audioRef.current) {
      audioRef.current.currentTime = 0
      audioRef.current.play().catch(() => {})
    } else if (repeatMode === 'all' || shuffle) {
      playNext()
    } else {
      setIsPlaying(false)
    }
  }

  // Seek bar — smooth drag

  function handleSeekStart() {
    isSeekingRef.current = true
  }

  function handleSeekMove(e: React.ChangeEvent<HTMLInputElement>) {
    setCurrentTime(Number(e.target.value))
  }

  function handleSeekCommit(e: React.MouseEvent<HTMLInputElement> | React.TouchEvent<HTMLInputElement>) {
    const t = Number((e.target as HTMLInputElement).value)
    setCurrentTime(t)
    isSeekingRef.current = false
    if (audioRef.current) audioRef.current.currentTime = t
  }

  // Volume

  function handleVolumeChange(e: React.ChangeEvent<HTMLInputElement>) {
    const v = Number(e.target.value)
    setVolume(v)
    if (audioRef.current) audioRef.current.volume = v
  }

  function toggleMute() {
    if (volume === 0) {
      const v = prevVolume > 0 ? prevVolume : 0.7
      setVolume(v)
      if (audioRef.current) audioRef.current.volume = v
    } else {
      setPrevVolume(volume)
      setVolume(0)
      if (audioRef.current) audioRef.current.volume = 0
    }
  }

  // Derived values

  const seekPct   = duration > 0 ? (currentTime / duration) * 100 : 0
  const volPct    = volume * 100
  const remaining = duration > currentTime ? duration - currentTime : 0
  const volIcon   = volume === 0 ? vol0Raw : volume > 0.5 ? vol3Raw : vol2Raw

  const repeatActive = repeatMode !== 'off'
  const seekBg = `linear-gradient(to right, var(--color-accent) ${seekPct}%, var(--color-border) ${seekPct}%)`
  const volBg  = `linear-gradient(to right, var(--color-accent) ${volPct}%, var(--color-border) ${volPct}%)`

  return (
    <div className="audio-container">
      <audio
        ref={audioRef}
        onTimeUpdate={handleTimeUpdate}
        onLoadedMetadata={handleLoadedMetadata}
        onEmptied={handleEmptied}
        onEnded={handleEnded}
      />

      {/* Left: heart + song info */}
      <div className="audio-container__left">
        <button
          className="audio-container__side-btn"
          onClick={() => { if (currentSong) toggleLike(currentSong.id) }}
          disabled={!currentSong}
          aria-label={isLiked ? 'Unlike' : 'Like'}
        >
          <Icon src={heartRaw} size={16} color={isLiked ? 'accent' : 'secondary'} />
        </button>

        <div className="audio-container__song-info">
          <button
            className="audio-container__song-name"
            onClick={handleSongNameClick}
            disabled={!currentSong || !sourceRoute}
          >
            {currentSong?.songName ?? '—'}
          </button>
          {currentSong && (
            <span className="audio-container__song-artist">{currentSong.artistName}</span>
          )}
          {sourceRoute && (
            <button
              className="audio-container__source"
              onClick={handleSongNameClick}
            >
              From: {getSourceLabel(sourceRoute, playlists)}
            </button>
          )}
        </div>
      </div>

      {/* Center: progress + controls */}
      <div className="audio-container__center">

        <div className="audio-container__progress">
          <span className="audio-container__time">{formatTime(currentTime)}</span>
          <input
            type="range"
            className="audio-container__seek"
            min={0}
            max={duration || 1}
            step={0.1}
            value={currentTime}
            disabled={!currentSong}
            onMouseDown={handleSeekStart}
            onTouchStart={handleSeekStart}
            onChange={handleSeekMove}
            onMouseUp={handleSeekCommit}
            onTouchEnd={handleSeekCommit}
            style={{ background: seekBg }}
          />
          <span className="audio-container__time">-{formatTime(remaining)}</span>
        </div>

        <div className="audio-container__controls">
          <button
            className={`audio-container__ctrl${shuffle ? ' audio-container__ctrl--on' : ''}`}
            onClick={toggleShuffle}
            aria-label="Shuffle"
          >
            <Icon src={shuffleRaw} size={14} color={shuffle ? 'accent' : 'secondary'} />
          </button>

          <button
            className="audio-container__ctrl"
            onClick={playPrev}
            disabled={!currentSong}
            aria-label="Previous"
          >
            <Icon src={prevRaw} size={14} color="secondary" />
          </button>

          <button
            className="audio-container__play-btn"
            onClick={() => setIsPlaying(!isPlaying)}
            disabled={!currentSong}
            aria-label={isPlaying ? 'Pause' : 'Play'}
          >
            <Icon src={isPlaying ? pauseRaw : playRaw} size={18} color={isPlaying ? 'accent' : 'primary'} />
          </button>

          <button
            className="audio-container__ctrl"
            onClick={playNext}
            disabled={!currentSong}
            aria-label="Next"
          >
            <Icon src={nextRaw} size={14} color="secondary" />
          </button>

          <button
            className={`audio-container__ctrl${repeatActive ? ' audio-container__ctrl--on' : ''}`}
            onClick={toggleRepeat}
            aria-label="Repeat"
          >
            <Icon src={repeatRaw} size={14} color={repeatActive ? 'accent' : 'secondary'} />
            {repeatMode === 'one' && (
              <span className="audio-container__repeat-one">1</span>
            )}
          </button>
        </div>
      </div>

      {/* Right: volume */}
      <div className="audio-container__right">
        <button
          className="audio-container__ctrl"
          onClick={toggleMute}
          aria-label={volume === 0 ? 'Unmute' : 'Mute'}
        >
          <Icon src={volIcon} size={16} color="secondary" />
        </button>
        <input
          type="range"
          className="audio-container__vol-slider"
          min={0}
          max={1}
          step={0.01}
          value={volume}
          onChange={handleVolumeChange}
          style={{ background: volBg }}
        />
      </div>
    </div>
  )
}
