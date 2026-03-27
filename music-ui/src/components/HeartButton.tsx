import Icon from './Icon'
import heartRaw from '../assets/heart.svg?raw'

interface HeartButtonProps {
  readonly liked:    boolean
  readonly onToggle: () => void
  readonly size?:    number
}

export default function HeartButton({ liked, onToggle, size = 16 }: HeartButtonProps) {
  return (
    <button
      className="song-table__heart-btn"
      onClick={onToggle}
      aria-label={liked ? 'Unlike' : 'Like'}
    >
      <Icon
        src={heartRaw}
        size={size}
        color={liked ? 'accent' : 'secondary'}
        alt={liked ? 'liked' : 'not liked'}
      />
    </button>
  )
}
