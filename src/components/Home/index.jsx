import React from "react";
import PropTypes from "prop-types";
import { useObserver } from "mobx-react";
import { ListItemText, ListItem, List, ListSubheader, Card, CardContent, Typography } from '@material-ui/core';
import RightIcon from '@material-ui/icons/ChevronRight';
import { useTheme } from '@material-ui/core/styles';
import { Navigator } from "@orikami-nl/orikami-components";

const propTypes = {
    go: PropTypes.func
};

const styles = {
    container: {
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between",
        margin: "0px 15px"
    },
    result: {
        marginRight: 15,
        marginBottom: 15,
        width: "calc(50% - 20px)"
    }
}

function Home(props) {
    const { tasks, date, next, prev, data } = props;
    const theme = useTheme();

    return useObserver(() => (
        <div>
            <Navigator date={date} next={next} prev={prev} style={{textTransform: "uppercase"}} />
            {tasks.length === 0 ? null :
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
            }

            {data.length === 0 ? null :
                <ListSubheader
                    component="div"
                    style={{ color: theme.palette.primary.main, fontWeight: "bold" }}>
                    Test resultaten
                </ListSubheader>
            }
            <div style={styles.container}>
                {data.map((d, index) => {
                    return (
                        <Card variant="outlined" key={index} style={styles.result}>
                            <CardContent>
                                <Typography variant="body2">{d.type}</Typography>
                                <Typography variant="h5">{d.value}</Typography>
                            </CardContent>
                        </Card>)
                })}
            </div>
        </div>
    ));

}

Home.propTypes = propTypes;

export default Home;
