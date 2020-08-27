import { useObserver } from "mobx-react";
import Router from "/src/components/Router";
import RouterModel from "/src/models/Router";
import { ThemeProvider, createMuiTheme } from '@material-ui/core/styles';

const theme = createMuiTheme({
  palette: {
    primary: {
      main: '#009688',
      dark: "#00695f",
      light: "#33ab9f"
    },
    secondary: {
      main: '#2196f3',
      dark: "#1769aa",
      light: "#4dabf5"
    },
  },
});
const router = new RouterModel();

function App(props) {
  return useObserver(() => (
    <ThemeProvider theme={theme}>
      <Router
        page={router.page}
        params={router.params}
        go={router.go} />
    </ThemeProvider>
  ));
}


export default App;
