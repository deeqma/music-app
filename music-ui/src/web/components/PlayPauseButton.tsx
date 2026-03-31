import Icon from './Icon'
import playRaw  from '../assets/play.svg?raw'
import pauseRaw from '../assets/pause.svg?raw'

interface PlayPauseButtonProps {
  readonly isPlaying: boolean
  readonly onToggle:  () => void
  readonly size?:     number
}

export default function PlayPauseButton({ isPlaying, onToggle, size = 15 }: PlayPauseButtonProps) {
  return (
    <button
      className={`song-table__play-btn${isPlaying ? ' song-table__play-btn--playing' : ''}`}
      onClick={onToggle}
      aria-label={isPlaying ? 'Pause' : 'Play'}
    >
      <Icon
        src={isPlaying ? pauseRaw : playRaw}
        size={size}
        color={isPlaying ? 'accent' : 'secondary'}
        alt={isPlaying ? 'pause' : 'play'}
      />
    </button>
  )
}
