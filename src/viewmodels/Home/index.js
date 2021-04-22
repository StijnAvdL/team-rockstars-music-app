import React from "react";
import PropTypes from "prop-types";
import Home from "/src/components/Home";
import moment from "moment";
import { reaction } from "mobx";

const propTypes = {
    go: PropTypes.func,
    timeline: PropTypes.object
};

class HomeViewModel extends React.Component {
    constructor(props) {
        super();

        this.timeline = props.timeline;
        this.state = { date: new Date(), loading: false, data: [] };

        reaction(
            () => this.timeline.init,
            init => {
              if (!init) this.getResults(new Date());
            },
            { fireImmediately: true }
          );

        this.next = this.next.bind(this);
        this.prev = this.prev.bind(this);
    }

    componentDidMount() {
        if(!this.timeline.init) {
            this.getResults(new Date());
        }
    }

    next() {
        const date = moment(this.state.date).add(1, "days");
        this.setState({ date: date, loading: true });
        this.getResults(date);
    }

    prev() {
        const date = moment(this.state.date).subtract(1, "day");
        this.setState({ date: date, loading: true });
        this.getResults(date);
    }

    getResults(date) {
        this.setState({ data: this.timeline.getData(date), loading: false });
    }

    render() {
        const { go } = this.props;
        const today = moment(this.state.date).isSame(moment(new Date()), "day");
        const tasks = [
            {
                title: "TUG",
                onClick: () => { go("/tug") }
            },
            {
                title: "Cognitietest",
                onClick: () => { go("/cognition") }
            }
        ];

        return (
            <Home
                tasks={today ? tasks : []}
                date={today ? "Vandaag" : moment(this.state.date).locale("nl").format("D MMMM")}
                next={today ? null : this.next}
                prev={this.prev}
                data={this.state.data}
            />
        );
    }
}

HomeViewModel.propTypes = propTypes;

export default HomeViewModel;
