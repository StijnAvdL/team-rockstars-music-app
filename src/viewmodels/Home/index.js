import React from "react";
import PropTypes from "prop-types";
import Home from "/src/components/Home";

const propTypes = {
    go: PropTypes.func
};

class HomeViewModel extends React.Component {

    render() {
        const { go } = this.props;
        const tasks = [
            {
                title: "Looptest", // 't' translation module?
                onClick: () => { go("/walking") }
            },
            {
                title: "Cognitietest",
                onClick: () => { go("/cognition") }
            }
        ];

        return (
            <Home tasks={tasks} />
        );
    }
}

HomeViewModel.propTypes = propTypes;

export default HomeViewModel;
