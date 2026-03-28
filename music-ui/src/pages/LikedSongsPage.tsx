import { songs } from '../mock/songData'
import SongTableWithFilter from '../components/SongTableWithFilter'
import { usePlayerStore } from '../store/playerStore'

export default function LikedSongsPage() {
  const likedIds    = usePlayerStore(s => s.likedIds)
  const likedSongs  = songs.filter(s => likedIds.has(s.id))
  return <SongTableWithFilter songs={likedSongs} />
}
