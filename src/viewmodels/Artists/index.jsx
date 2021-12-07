import React from 'react'
import PropTypes from 'prop-types'
import Artists from '/src/components/Artists'
// import { reaction } from 'mobx'

const propTypes = {
  go: PropTypes.func,
  artists: PropTypes.object,
}

class ArtistsViewModel extends React.Component {
  constructor(props) {
    super()
  }

  render() {
    const { artists, go } = this.props
    return <Artists artists={artists} go={go} />
  }
}

ArtistsViewModel.propTypes = propTypes

export default ArtistsViewModel