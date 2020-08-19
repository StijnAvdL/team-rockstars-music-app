import React, { Component } from "react";
import { observer } from "mobx-react";
import PropTypes from "prop-types";
import { useTheme } from '@material-ui/core/styles';

import MenuModel from "/src/models/Menu";
import SwipeableDrawer from '@material-ui/core/SwipeableDrawer';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';

import HomeIcon from '@material-ui/icons/Home';
import CognitionIcon from '@material-ui/icons/EmojiObjects';
import WalkingIcon from '@material-ui/icons/DirectionsWalk';
import CognitionIcon2 from '@material-ui/icons/Extension';

const propTypes = {
    go: PropTypes.func
};

const Menu =
    observer(
        (props) => {
            const { go } = props;
            // const theme = useTheme();
            console.log("MenuModel.open", MenuModel.open)

            const menuItems = [
                {
                    title: "Home",
                    icon: <HomeIcon />,
                    onClick: () => { go("/"); MenuModel.close() }
                },
                {
                    title: "Looptest",
                    icon: <WalkingIcon />,
                    onClick: () => { go("/walking"); MenuModel.close() }
                },
                {
                    title: "Cognitietest",
                    icon: <CognitionIcon2 />,
                    onClick: () => { go("/congintie"); MenuModel.close() }
                }
            ];

            return (
                <SwipeableDrawer
                    anchor={"left"}
                    open={MenuModel.open}
                    onClose={MenuModel.close}
                    onOpen={MenuModel.toggle} >
                    <List>
                        {menuItems.map((item, index) => (
                            <ListItem button key={index} onClick={item.onClick}>
                                <ListItemIcon>{item.icon}</ListItemIcon>
                                <ListItemText primary={item.title} />
                            </ListItem>
                        ))}
                    </List>
                </SwipeableDrawer>
            );
        }
    )

Menu.propTypes = propTypes;

export default Menu;