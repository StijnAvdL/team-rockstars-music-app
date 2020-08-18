import { Component } from "react";
// import { computed, observable, action, when, reaction } from "mobx";
import { observer } from "mobx-react";
// import MuiThemeProvider from "material-ui/styles/MuiThemeProvider";
// import theme from "/src/configs/theme";
import Router from "/src/Router";
import RouterModel from "/src/models/Router";

@observer
class App extends Component {
   constructor(props) {
    super(props);

    this.router = new RouterModel();
  }

  render() {
     return (
      // <MuiThemeProvider muiTheme={theme}>
      //   <div id="layout">{content}</div>
      // </MuiThemeProvider>
      <Router
          page={this.router.page}
          params={this.router.params}
          go={this.router.go}
          // timelineModel={this.timelineModel}
        />
    );
  }
}

export default App;
