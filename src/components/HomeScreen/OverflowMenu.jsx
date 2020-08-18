import React from "react";
import PropTypes from "prop-types";
import { observer, PropTypes as MobxPropTypes } from "mobx-react";

import { textStyles } from "/src/configs/theme";

import { IconButton } from "material-ui";
import MenuIcon from "material-ui/svg-icons/navigation/more-vert";
import IconMenu from "material-ui/IconMenu";
import MenuItem from "material-ui/MenuItem";

const propTypes = {
    id: PropTypes.string,
    menuItems: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func
        })
    ),
    style: PropTypes.object,
    disabled: PropTypes.bool
};

const styles = {
    main: {},
    list: {},
    menuItem: Object.assign({}, textStyles.overflowMenu, {}),
    iconButton: {
        height: 26,
        width: 26,
        padding: 2
    },
    iconButtonIcon: {
        height: 21,
        width: 21
    }
};

@observer
class OverflowMenu extends React.Component {
    render() {
        const { id, menuItems, disabled } = this.props;
        const style = Object.assign({}, styles.main, this.props.style);
        if (menuItems && menuItems.length) {
            const iconButton = (
                <IconButton
                    id={id}
                    disabled={disabled}
                    style={styles.iconButton}
                    iconStyle={styles.iconButtonIcon}
                    onClick={event => {
                        event.stopPropagation();
                    }}
                >
                    <MenuIcon />
                </IconButton>
            );
            return (
                <IconMenu
                    iconButtonElement={iconButton}
                    anchorOrigin={{ horizontal: "right", vertical: "top" }}
                    targetOrigin={{ horizontal: "right", vertical: "top" }}
                    style={style}
                    menuStyle={style.list}
                >
                    {menuItems.map(item => (
                        <MenuItem
                            key={item.key || item.label}
                            primaryText={item.label}
                            onClick={item.action}
                            style={styles.menuItem}
                        />
                    ))}
                </IconMenu>
            );
        } else {
            return null;
        }
    }
}

OverflowMenu.propTypes = propTypes;

export default OverflowMenu;
