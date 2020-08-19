import React from "react";
import PropTypes from "prop-types";
import { useObserver } from "mobx-react";
import { ListItemText, ListItem, List, ListSubheader } from '@material-ui/core';
import RightIcon from '@material-ui/icons/ChevronRight';
import { useTheme } from '@material-ui/core/styles';

const propTypes = {
    go: PropTypes.func
};

function Home(props) {
    const { tasks } = props;
    const theme = useTheme();

    return useObserver(() => (
        <div>
            <List subheader={
                <ListSubheader
                    component="div"
                    style={{ color: theme.palette.primary.main, fontWeight: "bold" }}>
                    Taken
                </ListSubheader>} >
                {tasks.map((task, index) => {
                    return (<ListItem button onClick={task.onClick} key={index}>
                        <ListItemText primary={task.title} />
                        <RightIcon />
                    </ListItem>)
                })}
            </List>
        </div>
    ));

}

Home.propTypes = propTypes;

export default Home;
