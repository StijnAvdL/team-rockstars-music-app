import React from 'react'
import PropTypes from 'prop-types'
import Artists from '/src/components/Artists'
// import { reaction } from 'mobx'

const propTypes = {
  go: PropTypes.func,
  timeline: PropTypes.object,
}

class ArtistsViewModel extends React.Component {
  constructor(props) {
    super()
  }

  render() {
    return <Artists />
  }
}

ArtistsViewModel.propTypes = propTypes

export default ArtistsViewModel
