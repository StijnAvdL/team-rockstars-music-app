import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";
import { ListItemText, ListItem, List, ListSubheader } from '@material-ui/core';
import RightIcon from '@material-ui/icons/ChevronRight';

const propTypes = {};

@observer
class Home extends React.Component {

    render() {
        const { tasks } = this.props;

        return (
            <div>
                <List subheader={<ListSubheader component="div">Taken</ListSubheader>} >
                    {tasks.map((task, index) => {
                        return (<ListItem button onClick={task.onClick} key={index}>
                            <ListItemText primary={task.title} />
                            <RightIcon />
                        </ListItem>)
                    })}
                </List>
            </div>
        );
    }
}

Home.propTypes = propTypes;

export default Home;
