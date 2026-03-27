import { songs } from '../mock/songData'
import SongTableWithFilter from '../components/SongTableWithFilter'

const likedSongs = songs.filter(s => s.liked)

export default function LikedSongsPage() {
  return <SongTableWithFilter songs={likedSongs} />
}
