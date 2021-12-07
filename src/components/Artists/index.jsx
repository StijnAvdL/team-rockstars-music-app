import { useObserver } from 'mobx-react'

import React from 'react'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableContainer from '@material-ui/core/TableContainer'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import Paper from '@material-ui/core/Paper'

function Artists(props) {
  const { artists = [], go } = props
  return useObserver(() => (
    <TableContainer component={Paper}>
      <Table aria-label="simple table">
        <TableBody>
          {artists.map((artist) => (
            <TableRow key={artist.name} onClick={() => go(`/artist?artist=${artist.name}`)}>
              <TableCell component="th" scope="row">
                {artist.name}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  ))
}

export default Artists
