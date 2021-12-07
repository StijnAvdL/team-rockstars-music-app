import { useObserver } from 'mobx-react'
import Router from '/src/components/Router'
import RouterModel from '/src/models/Router'
import { ThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import Artists from '/src/models/Artists'
import CircularProgress from '@material-ui/core/CircularProgress'
import Popover from '/src/components/Popover'

const theme = createMuiTheme({
  palette: {
    primary: {
      main: '#FFE000',
      dark: '#FFE000',
      light: '#FFE000',
    },
    secondary: {
      main: '#2196f3',
      dark: '#1769aa',
      light: '#4dabf5',
    },
  },
})
const router = new RouterModel()

const artistsModel = new Artists()

function App(props) {
  return useObserver(() => (
    <ThemeProvider theme={theme}>
      {artistsModel.error ? <Popover error={artistsModel.error} /> : null}
      {artistsModel.init ? (
        <CircularProgress />
      ) : (
        <Router page={router.page} params={router.params} go={router.go} artistsModel={artistsModel} />
      )}
    </ThemeProvider>
  ))
}

export default App
