import PropTypes from 'prop-types'
import { useObserver } from 'mobx-react'

import Artists from '/src/viewmodels/Artists'
import Artist from '/src/viewmodels/Artist'
import Playlists from '/src/viewmodels/Playlists'
import AppBar from '/src/components/AppBar'
import Menu from '/src/components/Menu'
import MenuModel from '/src/models/Menu'
import CircularProgress from '@material-ui/core/CircularProgress'

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object,
}

function Router(props) {
  const { page, go, artistsModel, params } = props
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
      content = <Playlists />
      break
    default:
      content = <p>Wrong adress</p>
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
