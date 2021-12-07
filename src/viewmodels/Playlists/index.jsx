import React from 'react'
import PropTypes from 'prop-types'
import Playlists from '/src/components/Playlists'
import { observable, action } from 'mobx'

const propTypes = {
  go: PropTypes.func,
  timeline: PropTypes.object,
}

class PlaylistsViewModel extends React.Component {
  @observable showError = false

  constructor(props) {
    super(props)

    this.add = this.add.bind(this)
  }

  @action.bound
  add(value) {
    const { playlistsModel } = this.props
    this.showError = false
    if (value) {
      playlistsModel.add(value)
      localStorage.removeItem('add')
    } else {
      // TODO error handling so message
      this.showError = true
    }
  }

  render() {
    const { playlistsModel } = this.props
    return <Playlists playlistsModel={playlistsModel} add={this.add} showError={this.showError} />
  }
}

PlaylistsViewModel.propTypes = propTypes

export default PlaylistsViewModel
