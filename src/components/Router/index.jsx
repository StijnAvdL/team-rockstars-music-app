import PropTypes from "prop-types";
import { useObserver } from "mobx-react";

import { AppBar } from "@orikami-nl/orikami-components";
import Sdmt from "@orikami-nl/sdmt-frontend";
import Tug from "@orikami-nl/tug-frontend";

import Home from "/src/viewmodels/Home";
import Menu from "/src/components/Menu";
import MenuModel from "/src/models/Menu";

import Timeline from "/src/models/Timeline";

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object
};

const timeline = new Timeline();

function Router(props) {
  const { page, go } = props;
  var content = null;
  var appBar = <AppBar leftType="menu" title="MM Fit" leftAction={MenuModel.toggle} />;

  switch (page) {
    case "/":
      content = (
        <Home go={go} timeline={timeline} />
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
          exitTest={() => go("/")}
          finishTest={(data) => {
            timeline.all.push({
              type: "sdmt",
              timestamp: new Date(),
              value: `${data.value.correct}/${data.value.nb}`
            });
          }}
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
    case "/tug":
      content = (
        <Tug
          language="nl"
          exitTest={() => go("/")}
          finishTest={(data) => {
            timeline.all.push({
              type: "tug",
              timestamp: new Date(),
              value: `${Math.round(data.value * 10) / 10} sec`
            });
          }}
          serverUrl="https://alpha.orikami-api.nl/v1/janssen-demo" />
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

