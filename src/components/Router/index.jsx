import { Component } from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";

import HomeScreen from "/src/viewmodels/HomeScreen";
// import Menu from "/src/components/Menu";
// import menuToggle from "/src/services/menuToggle";

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object
};

@observer
export class RouterView extends Component {
  render() {
    const {
      page,
      go,
      // timelineModel
    } = this.props;
    // var menuHidden = false;

    switch (page) {
      case "/":
        content = (
          <HomeScreen
            go={go}
            timelineModel={timelineModel}
          />
        );
        break;
    }

    return (
      <div>
        {content}
        {/* {menuHidden ? null : (
          <Menu
            go={go}
            currentPath={page}
            userProfile={userModel.profile}
            experiments={
              !study || !study.experiments
                ? []
                : study.experiments.map(experiment => experiment.id)
            }
            isOpen={menuToggle.isOpen}
            toggle={menuToggle.toggle}
            close={menuToggle.close}
          />
        )} */}
      </div>
    );
  }
}
RouterView.propTypes = propTypesView;

export default RouterView;
