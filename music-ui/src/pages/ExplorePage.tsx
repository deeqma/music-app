import { songs } from '../mock/songData'
import SongTableWithFilter from '../components/SongTableWithFilter'

export default function ExplorePage() {
  return <SongTableWithFilter songs={songs} />
}
