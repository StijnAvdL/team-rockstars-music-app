import React from 'react'
import Artist from '/src/components/Artist'
import { useObserver } from 'mobx-react'

import CircularProgress from '@material-ui/core/CircularProgress'

function ArtistViewModel(props) {
  const { model } = props
  return useObserver(() => <div>{model.initSongs ? <CircularProgress /> : <Artist songs={model.songs} />}</div>)
}

export default ArtistViewModel
