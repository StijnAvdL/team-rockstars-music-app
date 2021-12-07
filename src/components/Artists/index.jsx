import { useObserver } from 'mobx-react'

function App(props) {
  return useObserver(() => <div>Artists</div>)
}

export default App
