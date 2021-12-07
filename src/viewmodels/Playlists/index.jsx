import React from 'react'
import PropTypes from 'prop-types'
import Playlists from '/src/components/Playlists'
// import { reaction } from 'mobx'

const propTypes = {
  go: PropTypes.func,
  timeline: PropTypes.object,
}

class PlaylistsViewModel extends React.Component {
  constructor(props) {
    super()
  }

  render() {
    return <Playlists />
  }
}

PlaylistsViewModel.propTypes = propTypes

export default PlaylistsViewModel
