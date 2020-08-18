import React from "react";
import PropTypes from "prop-types";
import { observer } from "mobx-react";

import { ListItemText, ListItem, List, ListSubheader } from '@material-ui/core';
import RightIcon from '@material-ui/icons/ChevronRight';
import { makeStyles } from '@material-ui/core/styles';

const propTypes = {
    // view: PropTypes.oneOf(["loading", "default", "note"]),
    // dialog: PropTypes.node,
    // date: PropTypes.object, // of type Moment
    // nextDay: PropTypes.func,
    // previousDay: PropTypes.func,
    // gotoDate: PropTypes.func,
    // toDoItems: MobxPropTypes.arrayOrObservableArrayOf(
    //     PropTypes.shape({
    //         label: PropTypes.string,
    //         action: PropTypes.func,
    //         disabled: PropTypes.bool
    //     })
    // ),
    // overflowToDoItems: MobxPropTypes.arrayOrObservableArrayOf(
    //     PropTypes.shape({
    //         label: PropTypes.string,
    //         action: PropTypes.func
    //     })
    // ),
    // results: MobxPropTypes.arrayOrObservableArrayOf(
    //     PropTypes.shape({
    //         label: PropTypes.string,
    //         value: PropTypes.any,
    //         action: PropTypes.func,
    //         overflowActions: MobxPropTypes.arrayOrObservableArrayOf(
    //             PropTypes.shape({
    //                 label: PropTypes.string,
    //                 action: PropTypes.func
    //             })
    //         ),
    //         // depricated instead use value
    //         result: PropTypes.string
    //     })
    // ),
    // addNote: PropTypes.func,
    // editNote: PropTypes.func,
    // deleteNote: PropTypes.func,
    // saveNote: PropTypes.func,
    // cancelNote: PropTypes.func,
    // goToTimegraph: PropTypes.func.isRequired
};

// const useStyles = makeStyles((theme) => ({
//     root: {
//         width: '100%',
//         maxWidth: 360,
//         backgroundColor: theme.palette.background.paper,
//     },
//     nested: {
//         paddingLeft: theme.spacing(4),
//     },
// }));


@observer
class HomeScreen extends React.Component {

    handleClick() {
        console.log("hooi")
    }

    render() {
        // const { } = this.props;
        // const classes = useStyles();
        return (
            <div>
                {/* <AppBar
                    leftButtonType="mainMenu"
                    leftButtonOptions={{ action: menuToggle.toggle }}
                    rightButtonType={"action"}
                    rightButtonOptions={{
                        icon: <AnalysisIcon />,
                        action: goToTimegraph
                    }}
                /> */}
                <List
                    component="nav"
                    aria-labelledby="nested-list-subheader"
                    subheader={
                        <ListSubheader component="div" id="nested-list-subheader">
                            Openstaande taken
                        </ListSubheader>
                    }
                    // className={classes.root}
                >
                    <ListItem button onClick={this.handleClick}>
                        <ListItemText primary="Looptest" />
                        <RightIcon />
                    </ListItem>
                    <ListItem button onClick={this.handleClick}>
                        <ListItemText primary="Cognitietest" />
                        <RightIcon />
                    </ListItem>
                </List>
            </div>
        );
    }
}

HomeScreen.propTypes = propTypes;

export default HomeScreen;
