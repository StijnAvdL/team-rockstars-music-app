import { useObserver } from 'mobx-react'

import React from 'react'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableContainer from '@material-ui/core/TableContainer'
import TableRow from '@material-ui/core/TableRow'
import Paper from '@material-ui/core/Paper'
import Input from '@material-ui/core/Input'

function Artists(props) {
  const { artists = [], go, search } = props

  return useObserver(() => (
    <div>
      <Input
        onChange={(event) => {
          // Quick fix for now so save it in localStorage and trigger by that the correct artists list
          localStorage.search = event.target.value
          search(event.target.value)
        }}
        fullWidth={true}
        placeholder="Search artist ..."
        value={localStorage.search}
        autoFocus
      />
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
    </div>
  ))
}

export default Artists
