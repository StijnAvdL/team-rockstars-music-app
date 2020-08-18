import React from "react";
import PropTypes from "prop-types";
import { PropTypes as MobxPropTypes } from "mobx-react";

import { textStyles, colorPalette } from "/src/configs/theme";

import { List, ListItem } from "material-ui/List";
import NextIcon from "material-ui/svg-icons/navigation/chevron-right";

import t from "/src/services/translate";

import OverflowMenu from "./OverflowMenu";
import BlockHeader from "./BlockHeader";

const propTypes = {
    itemList: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func,
            key: PropTypes.string
        })
    ),
    overflowToDoItems: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func
        })
    )
};

const styles = {
    container: {
        position: "relative"
    },
    list: {
        padding: 0,
        paddingBottom: 8
    },
    listItem: Object.assign({}, textStyles.listItem, {
        paddingLeft: 0,
        paddingRight: 0,
        marginRight: -16
    }),
    disabled: {
        fontStyle: "italic",
        color: colorPalette.disabled
    },
    listItemIcon: {},
    allDone: {
        fontStyle: "italic"
    }
};

class ToDoBlockCtrl extends React.Component {
    render() {
        const { itemList, overflowToDoItems } = this.props;
        return <ToDoBlockView itemList={itemList} overflowToDoItems={overflowToDoItems} />;
    }
}

ToDoBlockCtrl.propTypes = propTypes;

const viewPropTypes = {
    itemList: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func,
            disabled: PropTypes.bool
        })
    ),
    overflowToDoItems: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func
        })
    )
};

export class ToDoBlockView extends React.Component {
    render() {
        const { itemList, overflowToDoItems } = this.props;
        let content;
        if (itemList && itemList.length) {
            const items = itemList.map((item, index) => {
                return (
                    <ListItem
                        key={item.key || index}
                        primaryText={item.label}
                        rightIcon={
                            <NextIcon
                                style={styles.listItemIcon}
                                color={item.disabled ? colorPalette.disabled : colorPalette.accentDark}
                            />
                        }
                        onClick={item.action}
                        innerDivStyle={Object.assign({}, styles.listItem, item.disabled ? styles.disabled : {})}
                        disabled={item.disabled}
                    />
                );
            });
            content = <List style={styles.list}>{items}</List>;
        } else {
            content = <span style={styles.allDone}>{t("HomeScreen.AllDone")}</span>;
        }
        const overflowMenu = overflowToDoItems ? (
            <OverflowMenu id="todo-overflow-menu" menuItems={overflowToDoItems} />
        ) : null;
        return (
            <div id="todo-block-view" style={styles.container}>
                <BlockHeader title={t("HomeScreen.Todo", "Nog uitvoeren")} overflowMenu={overflowMenu} />
                {content}
            </div>
        );
    }
}
ToDoBlockView.propTypes = viewPropTypes;

export default ToDoBlockCtrl;
