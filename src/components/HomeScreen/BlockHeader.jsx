import React from "react";
import PropTypes from "prop-types";

import { textStyles } from "/src/configs/theme";

import Subheader from "material-ui/Subheader";

const propTypes = {
    title: PropTypes.string,
    overflowMenu: PropTypes.node
};

const styles = {
    title: {
        main: {
            width: "100%",
            display: "flex",
            alignItems: "center",
            marginTop: 8
        },
        label: Object.assign({}, textStyles.groupTitle, {
            lineHeight: "48px",
            padding: 0,
            flex: 1
        })
    }
};

const BlockHeader = ({ title, overflowMenu }) => {
    return (
        <div style={styles.title.main}>
            <Subheader style={styles.title.label}>{title}</Subheader>
            {overflowMenu}
        </div>
    );
};

BlockHeader.propTypes = propTypes;
BlockHeader.defaultProps = {
    overflowMenu: null
};

export default BlockHeader;
