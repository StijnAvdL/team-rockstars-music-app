import { useObserver } from 'mobx-react'

function App(props) {
  return useObserver(() => <div>Playlists</div>)
}

export default App
