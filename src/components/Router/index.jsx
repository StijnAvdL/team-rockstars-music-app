import PropTypes from 'prop-types'
import { useObserver } from 'mobx-react'

import Artists from '/src/viewmodels/Artists'
import Artist from '/src/viewmodels/Artist'
import Playlists from '/src/viewmodels/Playlists'
import AppBar from '/src/components/AppBar'
import Menu from '/src/components/Menu'
import MenuModel from '/src/models/Menu'
import Typography from '@material-ui/core/Typography'

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object,
  artistsModel: PropTypes.object,
  playlistsModel: PropTypes.object,
}

function Router(props) {
  const { page, go, artistsModel, playlistsModel, params } = props
  var content = null
  var title = null

  switch (page) {
    case '/':
      title = 'Artists'
      content = <Artists go={go} model={artistsModel} artists={artistsModel.artists} />
      break
    case '/artist':
      artistsModel.getSongs(params.artist)
      title = params.artist
      content = <Artist model={artistsModel} />
      break
    case '/playlists':
      title = 'Playlists'
      content = <Playlists playlistsModel={playlistsModel} />
      break
    default:
      content = <Typography>Page doesn't exists, use the menu to go back</Typography>
      break
  }

  return useObserver(() => (
    <div>
      <AppBar title={title} menuAction={MenuModel.toggle} />
      <Menu go={go} />
      {content}
    </div>
  ))
}

Router.propTypes = propTypesView

export default Router
