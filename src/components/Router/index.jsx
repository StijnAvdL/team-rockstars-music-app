import PropTypes from 'prop-types'
import { useObserver } from 'mobx-react'

import Artists from '/src/viewmodels/Artists'
import Playlists from '/src/viewmodels/Playlists'
import AppBar from '/src/components/AppBar'
import Menu from '/src/components/Menu'
import MenuModel from '/src/models/Menu'

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object,
}

function Router(props) {
  const { page, go, artistsModel } = props
  var content = null
  var title = null

  switch (page) {
    case '/':
      title = 'Artists'
      content = <Artists artists={artistsModel.artists} />
      break
    case '/playlists':
      title = 'Playlists'
      content = <Playlists />
      break
    default:
      content = <p>Error</p>
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
