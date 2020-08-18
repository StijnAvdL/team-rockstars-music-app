import React, { Component } from "react";
import PropTypes from "prop-types";

import { observable, action } from "mobx";
import { observer } from "mobx-react";

import { Dialog, TextField, RaisedButton as DialogButton, RadioButton, RadioButtonGroup } from "material-ui";

import t from "/src/services/translate";

const propTypes = {
    onSubmit: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
    text: PropTypes.string,
    options: PropTypes.arrayOf(PropTypes.shape({ label: PropTypes.string, value: PropTypes.any })),
    freeTextOption: PropTypes.shape({
        label: PropTypes.string,
        value: PropTypes.any
    })
};

const styles = {
    dialog: {
        width: 300,
        height: 300
    },
    button: {},
    actionContainer: {
        display: "flex",
        justifyContent: "space-evenly"
    },
    radioButton: {
        marginBottom: 16
    }
};

@observer
class ConfirmationDialogCtrl extends Component {
    @observable valueSelected;
    @observable customValue = "";

    @action.bound
    changeValue(event, newValue) {
        this.valueSelected = newValue;
    }

    @action.bound
    changeCustomValue(event, newValue) {
        this.customValue = newValue;
    }

    onSubmit() {
        const customValue = this.valueSelected === "Different" ? this.customValue : null;
        this.props.onSubmit(this.valueSelected, customValue);
    }

    onCancel() {
        this.props.onCancel();
    }

    render() {
        const { freeTextOption, text } = this.props;
        let options = this.props.options.slice();
        if (freeTextOption) {
            options.push(freeTextOption);
        }

        return (
            <ConfirmationDialogView
                text={text}
                valueSelected={this.valueSelected}
                handleChange={this.changeValue}
                handleSubmit={this.onSubmit.bind(this)}
                handleCancel={this.onCancel.bind(this)}
                options={options}
                renderCustomTextField={freeTextOption && this.valueSelected === freeTextOption.value}
                customValue={this.customValue}
                customValueChange={this.changeCustomValue}
            />
        );
    }
}
ConfirmationDialogCtrl.propTypes = propTypes;

class ConfirmationDialogView extends Component {
    render() {
        const {
            valueSelected,
            handleChange,
            handleSubmit,
            handleCancel,
            options,
            text,
            renderCustomTextField,
            customValue,
            customValueChange
        } = this.props;

        const radioButtons = options.map(bProps => {
            return (
                <RadioButton key={bProps.value} label={bProps.label} value={bProps.value} style={styles.radioButton} />
            );
        });
        const actions = [
            <DialogButton
                onClick={handleSubmit}
                primary={true}
                label={t("Next")}
                disabled={typeof valueSelected === "undefined"}
            />,
            <DialogButton label={t("Cancel")} primary={false} onClick={handleCancel} />
        ];
        const textField = renderCustomTextField ? (
            <TextField id="customValue" value={customValue} onChange={customValueChange} />
        ) : null;
        return (
            <Dialog
                actions={actions}
                open={true}
                modal={true}
                contentStyle={styles.dialog}
                actionsContainerStyle={styles.actionContainer}
            >
                <p>{text}</p>
                <RadioButtonGroup name="select_reason" onChange={handleChange} valueSelected={valueSelected}>
                    {radioButtons}
                </RadioButtonGroup>
                {textField}
            </Dialog>
        );
    }
}

ConfirmationDialogView.propTypes = {
    text: PropTypes.string,
    valueSelected: PropTypes.string,
    handleChange: PropTypes.func,
    handleSubmit: PropTypes.func,
    handleCancel: PropTypes.func,
    options: PropTypes.arrayOf(PropTypes.shape({ label: PropTypes.string, value: PropTypes.any })),
    renderCustomTextField: PropTypes.bool,
    customValue: PropTypes.string,
    customValueChange: PropTypes.func
};

export default ConfirmationDialogCtrl;
