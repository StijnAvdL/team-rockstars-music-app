import { Component } from "react";
import { observer } from "mobx-react";
import Router from "/src/components/Router";
import RouterModel from "/src/models/Router";
import { ThemeProvider, createMuiTheme  } from '@material-ui/core/styles';

import purple from '@material-ui/core/colors/purple';
import green from '@material-ui/core/colors/green';

const theme = createMuiTheme({
  palette: {
    primary: {
      main: purple[500],
    },
    secondary: {
      main: green[500],
    },
  },
});

@observer
class App extends Component {
  constructor(props) {
    super(props);

    this.router = new RouterModel();
  }

  render() {
    return (
      <ThemeProvider theme={theme}>
        <Router
          page={this.router.page}
          params={this.router.params}
          go={this.router.go} />
      </ThemeProvider>
    );
  }
}

export default App;
