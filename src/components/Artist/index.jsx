import { useObserver } from 'mobx-react'

import React from 'react'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableContainer from '@material-ui/core/TableContainer'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import AddIcon from '@material-ui/icons/Add'

function Artist(props) {
  const { songs } = props

  return useObserver(() => (
    <div>
      {songs.length === 0 ? (
        <Typography>No songs found</Typography>
      ) : (
        <div>
          <TableContainer component={Paper}>
            <Table aria-label="simple table">
              <TableHead>
                <TableRow>
                  <TableCell>Song</TableCell>
                  <TableCell>Album</TableCell>
                  <TableCell></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {songs.map((song) => (
                  <TableRow key={song.name}>
                    <TableCell component="th" scope="row">
                      {song.name}
                    </TableCell>
                    <TableCell component="th" scope="row">
                      {song.album}
                    </TableCell>
                    <TableCell component="th" scope="row" aria-controls="simple-menu" aria-haspopup="true">
                      <AddIcon onClick={() => console.log('open popup to show list a playlists')} />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      )}
    </div>
  ))
}

export default Artist
