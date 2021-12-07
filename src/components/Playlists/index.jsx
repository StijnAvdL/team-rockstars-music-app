import { useObserver } from 'mobx-react'
import Button from '@material-ui/core/Button'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemText from '@material-ui/core/ListItemText'
import Input from '@material-ui/core/Input'

function App(props) {
  const { playlistsModel, showError, add } = props
  console.log("showError", showError)

  return useObserver(() => (
    <div style={{ margin: 15 }}>
      <Input
        onChange={(event) => {
          // Use of localStorage because function component doesnt work nicely with react state
          localStorage.add = event.target.value
        }}
        fullWidth={true}
      />
      <Button
        variant="contained"
        color="primary"
        onClick={() => {
          add(localStorage.add)
        }}
      >
        Add playlist
      </Button>

      <List>
        {playlistsModel.playlists.map((playlist) => (
          <ListItem key={playlist.name} onClick={() => console.log('test')}>
            <ListItemText primary={playlist.name} secondary={'Songs: ' + playlist.songs.length} />
          </ListItem>
        ))}
      </List>
      {props.showError ? <Popover error="Name cannot be empty!" /> : null}
    </div>
  ))
}

export default App
