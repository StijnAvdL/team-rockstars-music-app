import React from 'react'
import PropTypes from 'prop-types'
import Artists from '/src/components/Artists'

const propTypes = {
  go: PropTypes.func,
  artists: PropTypes.object,
}

class ArtistsViewModel extends React.Component {
  constructor(props) {
    super(props)

    this.search = this.search.bind(this)
  }

  search(value) {
    this.props.model.searchArtists(value)
  }

  render() {
    const { go, artists } = this.props
    return <Artists artists={artists} go={go} search={this.search} />
  }
}

ArtistsViewModel.propTypes = propTypes

export default ArtistsViewModel
