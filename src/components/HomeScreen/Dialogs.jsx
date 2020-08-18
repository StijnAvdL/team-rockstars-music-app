import PropTypes from "prop-types";
import t from "/src/services/translate";
import { Dialog } from "material-ui";
import { StandardButton } from "/orikami/components/main";
import ConfirmationDialogWithOptions from "/src/components/Dialogs/ConfirmationWithOptions";
import DefaultConfirmationDialog from "/src/components/Dialogs/Confirmation";

export const ConfirmationDialog = props => <DefaultConfirmationDialog {...props} />;

export const ChangedPrivacyDialog = ({ onConfirm }) => {
    return (
        <Dialog
            title={t("PrivacyStatement.Adjust.Title")}
            actions={[<StandardButton label="Open" primary={true} onClick={onConfirm} fullWidth={false} />]}
            open={true}
        >
            {t("PrivacyStatement.Adjust.Message")}
        </Dialog>
    );
};
ChangedPrivacyDialog.propTypes = {
    onConfirm: PropTypes.func
};

export const RedoDeleteConfirmation = ({ text, onConfirm, onCancel }) => {
    const options = [
        {
            label: t("HomeScreen.Results.DeleteReasons.Disturbed"),
            value: "Disturbed"
        },
        {
            label: t("HomeScreen.Results.DeleteReasons.Malfunction"),
            value: "Malfunction"
        }
    ];
    const optionDifferent = {
        label: t("HomeScreen.Results.DeleteReasons.Different"),
        value: "Different"
    };
    return (
        <ConfirmationDialogWithOptions
            text={text}
            onSubmit={onConfirm}
            onCancel={onCancel}
            options={options}
            freeTextOption={optionDifferent}
        />
    );
};
RedoDeleteConfirmation.propTypes = {
    text: PropTypes.string,
    onConfirm: PropTypes.func,
    onCancel: PropTypes.func
};

export const ErrorDialog = ({ text, onConfirm }) => {
    return (
        <Dialog
            title={t("HomeScreen.ErrorTitle")}
            actions={[<StandardButton label={t("OK")} primary={true} onClick={onConfirm} fullWidth={false} />]}
            open={true}
        >
            {text}
        </Dialog>
    );
};
ErrorDialog.propTypes = {
    text: PropTypes.string,
    onConfirm: PropTypes.func
};
