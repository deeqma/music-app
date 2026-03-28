import { useState, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { usePlaylistsStore } from '../store/playlistsStore'
import SongTableWithFilter from '../components/SongTableWithFilter'
import Icon from '../components/Icon'
import musicRaw  from '../assets/music.svg?raw'
import searchRaw from '../assets/search.svg?raw'
import shareRaw  from '../assets/share.svg?raw'
import copyRaw   from '../assets/copy.svg?raw'
import lockRaw   from '../assets/lock.svg?raw'
import publicRaw from '../assets/public.svg?raw'

function formatTotalDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

export default function PlaylistPage() {
  const { name }    = useParams<{ name: string }>()
  const playlists   = usePlaylistsStore(s => s.playlists)
  const playlist    = playlists.find(p => p.slug === name) ?? null

  const [isPrivate, setIsPrivate] = useState(playlist?.visibility === 'PRIVATE')
  const [shareOpen, setShareOpen] = useState(false)
  const [copied,    setCopied]    = useState(false)
  const copyTimeout = useRef<ReturnType<typeof setTimeout> | null>(null)

  if (!playlist) return <p className="playlist-page__not-found">Playlist not found.</p>

  const totalDuration = playlist.songDtos.reduce((sum: number, s) => sum + s.durationSeconds, 0)
  const shareUrl      = `${globalThis.location.origin}/share/${playlist.shareToken}`

  function toggleVisibility() {
    const makingPublic = isPrivate
    setIsPrivate(prev => !prev)
    if (makingPublic) setShareOpen(false)
  }

  function handleCopy() {
    navigator.clipboard.writeText(shareUrl)
    setCopied(true)
    if (copyTimeout.current) clearTimeout(copyTimeout.current)
    copyTimeout.current = setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="playlist-page">
      <div className="playlist-hero">
        <div className="playlist-hero__cover">
          <Icon src={musicRaw} size={80} color="accent" alt="playlist cover" />
        </div>

        <div className="playlist-hero__info">
          <div className="playlist-hero__label-row">
            <span className="playlist-hero__label">Playlist</span>
            <button
              className="playlist-hero__visibility-btn"
              onClick={toggleVisibility}
              aria-label={isPrivate ? 'Make public' : 'Make private'}
              title={isPrivate ? 'Make public' : 'Make private'}
            >
              <Icon
                src={isPrivate ? lockRaw : publicRaw}
                size={14}
                color={isPrivate ? 'accent' : 'secondary'}
                alt={isPrivate ? 'private' : 'public'}
              />
            </button>
          </div>

          <h1 className="playlist-hero__name">{playlist.playlistName}</h1>
          <p className="playlist-hero__description">{playlist.description}</p>
          <div className="playlist-hero__stats">
            <span>{playlist.totalSongs} songs</span>
            <span className="playlist-hero__dot">·</span>
            <span>{formatTotalDuration(totalDuration)}</span>
          </div>

          <div className="playlist-hero__actions">
            <button className="playlist-hero__btn playlist-hero__btn--edit">Edit</button>
            <button className="playlist-hero__btn playlist-hero__btn--delete">Delete</button>
            <button
              className="playlist-hero__share-btn"
              onClick={() => setShareOpen(o => !o)}
              aria-label="Share"
              style={{ visibility: isPrivate ? 'visible' : 'hidden' }}
            >
              <Icon src={shareRaw} size={16} color="accent" alt="share" />
            </button>
          </div>

          {isPrivate && shareOpen && (
            <div className="playlist-share-bar">
              <span className="playlist-share-bar__url">{shareUrl}</span>
              <button
                className="playlist-share-bar__copy-btn"
                onClick={handleCopy}
                disabled={copied}
                aria-label="Copy link"
              >
                {copied
                  ? <span className="playlist-share-bar__copied">Copied</span>
                  : <Icon src={copyRaw} size={15} color="accent" alt="copy" />
                }
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="playlist-toolbar">
        <div className="playlist-toolbar__search">
          <Icon src={searchRaw} size={16} color="accent" alt="search" />
          <input
            type="text"
            className="playlist-toolbar__search-input"
            placeholder="Search..."
          />
        </div>
      </div>

      <SongTableWithFilter songs={playlist.songDtos} />
    </div>
  )
}
