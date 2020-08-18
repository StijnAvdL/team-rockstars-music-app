import React from "react";
import PropTypes from "prop-types";

import { IconButton } from "material-ui";
import AddIcon from "material-ui/svg-icons/content/add";

import t from "/src/services/translate";

import OverflowMenu from "./OverflowMenu";
import BlockHeader from "./BlockHeader";
import { StandardButton } from "/orikami/components/main";

const propTypes = {
    note: PropTypes.string,
    add: PropTypes.func,
    edit: PropTypes.func,
    deleteNote: PropTypes.func
};

const styles = {
    note: {},
    container: {}
};

class NotesBlockCtrl extends React.Component {
    render() {
        return <NotesBlockView {...this.props} />;
    }
}

NotesBlockCtrl.propTypes = propTypes;

// const viewPropTypes = {};

export class NotesBlockView extends React.Component {
    render() {
        const { add, edit, deleteNote, note } = this.props;
        let content = null;
        let overflowMenu = null;
        if (this.props.note) {
            content = <div style={styles.note}>{note}</div>;
            const menu = [
                {
                    label: t("HomeScreen.Notes.Edit"),
                    action: edit
                },
                {
                    label: t("HomeScreen.Notes.Delete"),
                    action: deleteNote
                }
            ];
            overflowMenu = <OverflowMenu menuItems={menu} />;
        } else if (this.props.add) {
            overflowMenu = <StandardButton fullWidth={false} label={t("Add")} onClick={add} />;
        }
        return (
            <div id="notes-block" style={styles.container}>
                <BlockHeader title={t("HomeScreen.NotesTitle", "Notitie")} overflowMenu={overflowMenu} />
                {content}
            </div>
        );
    }
}
NotesBlockView.propTypes = propTypes;

export default NotesBlockCtrl;
