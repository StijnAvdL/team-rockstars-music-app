import PropTypes from "prop-types";
import { useObserver } from "mobx-react";

const propTypesView = {
  page: PropTypes.string,
  go: PropTypes.func,
  params: PropTypes.object
};

function Router(props) {
  const { page, go } = props;
  var content = null;


  switch (page) {
    case "/":
      content = <p>Home</p>;
      break;
    case "/error":
      content = <p>Error</p>;
      break;
  }

  return useObserver(() => (
    <div>
      {content}
      <Menu go={go} />
    </div>
  ));
}

Router.propTypes = propTypesView;

export default Router;

