import { useObserver } from 'mobx-react'

import React from 'react'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableContainer from '@material-ui/core/TableContainer'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography';

function Artist(props) {
  const { songs } = props

  console.log('songs', songs)

  return useObserver(() => (
    <div>
      {songs.length === 0 ? (
        <Typography>No songs found</Typography>
      ) : (
        <TableContainer component={Paper}>
          <Table aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell>Song</TableCell>
                <TableCell>Album</TableCell>
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
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </div>
  ))
}

export default Artist
