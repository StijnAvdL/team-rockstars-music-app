import PropTypes from "prop-types";
import { useObserver } from "mobx-react";

import { AppBar } from "@orikami-nl/orikami-components";
import Sdmt from "@orikami-nl/sdmt-frontend";

import Home from "/src/viewmodels/Home";
import Menu from "/src/components/Menu";
import MenuModel from "/src/models/Menu";

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object
};

function Router(props) {
  const { page, go } = props;
  var content = null;
  var appBar = <AppBar leftType="menu" title="Janssen" leftAction={MenuModel.toggle} />;

  switch (page) {
    case "/":
      content = (
        <Home go={go} />
      );
      break;
    case "/walking":
      content = (
        <p>Walking</p>
      );
      break;
    case "/cognition":
      content = (
        <Sdmt
          fullServerUrl="https://alpha.orikami-api.nl/v1/janssen-demo"
          language="nl"
          exitTest={(data) => {console.log("exitTest", data); go("/")}}
          finishTest={(data) => {console.log("finishTest", data);}}
          restart={false}
          practiceBeforeTest={true}
          disableRipple={true}
          overlay={true}
          sensorData={true}
          s3Bucket="orikami-cordova-plugin"
          s3Prefix="dev/uploader/"
        />
      );
      appBar = null;
      break;
  }

  return useObserver(() => (
    <div>
      {appBar}
      {content}
      <Menu go={go} />
    </div>
  ));
}

Router.propTypes = propTypesView;

export default Router;

