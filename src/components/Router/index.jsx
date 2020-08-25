import PropTypes from "prop-types";
import { useObserver } from "mobx-react";

import { AppBar } from "@orikami-nl/orikami-components";

import Home from "/src/viewmodels/Home";
import Menu from "/src/components/Menu";
import MenuModel from "/src/models/Menu";

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object
};

function Router(props)  {
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
    case "/congintie":
      content = (
        <p>SDMT</p>
      );
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

