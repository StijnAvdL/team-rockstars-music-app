import React from "react";
import PropTypes from "prop-types";
import { PropTypes as MobxPropTypes } from "mobx-react";

import { textStyles } from "/src/configs/theme";

import OverflowMenu from "./OverflowMenu";

import Paper from "material-ui/Paper";
import InfoIcon from "material-ui/svg-icons/action/info";

const propTypes = {
    type: PropTypes.oneOf(["single", "container"]),
    label: PropTypes.string,
    result: PropTypes.oneOfType([PropTypes.node, PropTypes.string]),
    onClick: PropTypes.func,
    menuItems: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            action: PropTypes.func
        })
    )
};

const defaultProps = {
    type: "single"
};

const getStyles = props => {
    return {
        paper: {
            height: props.type === "single" ? 100 : "auto",
            marginBottom: 8,
            width: props.type === "single" ? "calc(50% - 4px)" : "100%",
            paddingTop: 6,
            paddingRight: 9,
            paddingBottom: props.type === "single" ? 40 : 16,
            paddingLeft: 9,
            position: "relative",
            overflow: "hidden"
        },
        label: Object.assign({}, textStyles.testResultsLabel, {
            marginBottom: 10
        }),
        result: Object.assign(
            {},
            textStyles.testResults,
            props.type === "single"
                ? {
                      marginTop: 20,
                      marginLeft: 8
                  }
                : {}
        ),
        overflowMenu: {
            position: "absolute",
            top: 4,
            right: 3
        }
    };
};

class DataItem extends React.Component {
    render() {
        const { label, result, onClick, menuItems, infoAction, loading } = this.props;
        const styles = getStyles(this.props);
        var rightCorner = null;
        if (infoAction) {
            rightCorner = <InfoIcon onClick={infoAction} style={styles.overflowMenu} />;
        } else if (menuItems) {
            rightCorner = <OverflowMenu menuItems={menuItems} style={styles.overflowMenu} />;
        }
        return (
            <Paper style={styles.paper} onClick={onClick}>
                {rightCorner}
                <div style={styles.label}>{label}</div>
                <div style={styles.result}>{result}</div>
            </Paper>
        );
    }
}
DataItem.propTypes = propTypes;
DataItem.defaultProps = defaultProps;

export default DataItem;
