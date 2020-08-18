import React from "react";
import PropTypes from "prop-types";

import { textStyles } from "/src/configs/theme";

import { IconButton } from "material-ui";
import DatePickerDialog from "material-ui/DatePicker/DatePickerDialog";
import PreviousIcon from "material-ui/svg-icons/navigation/chevron-left";
import NextIcon from "material-ui/svg-icons/navigation/chevron-right";

import t from "/src/services/translate";

const propTypes = {
    date: PropTypes.object, // of type Moment
    previous: PropTypes.func,
    next: PropTypes.func,
    // function gotoDate(Date date) {...}
    gotoDate: PropTypes.func
};

const styles = {
    main: {
        width: "100%",
        display: "flex",
        justifyContent: "center",
        alignItems: "center"
    },
    buttonContainer: {
        width: "24px"
    },
    buttonStyle: {},
    dateDisplay: Object.assign({}, textStyles.screenTitle, {
        width: "200px",
        lineHeight: "72px",
        textAlign: "center",
        textTransform: "uppercase"
    })
};

class NavigatorCtrl extends React.Component {
    formatDisplayDate(dateMoment) {
        if (dateMoment.isSame(new Date(), "day")) {
            return t("Today", "vandaag");
        } else {
            return dateMoment.format("D MMMM");
        }
    }

    openDatePicker() {
        if (this.datePicker) {
            this.datePicker.show();
        }
    }

    render() {
        return (
            <div>
                <NavigatorView
                    dateString={this.formatDisplayDate(this.props.date)}
                    goPrevious={this.props.previous}
                    goNext={this.props.next}
                    openDatePicker={this.openDatePicker.bind(this)}
                />
                <DatePickerDialog
                    ref={node => (this.datePicker = node)}
                    DateTimeFormat={global.Intl.DateTimeFormat}
                    locale={localStorage.language || "nl"}
                    firstDayOfWeek={1}
                    initialDate={this.props.date.toDate()}
                    maxDate={new Date()}
                    onAccept={this.props.gotoDate}
                />
            </div>
        );
    }
}

NavigatorCtrl.propTypes = propTypes;

const viewPropTypes = {
    dateString: PropTypes.string.isRequired,
    goPrevious: PropTypes.func,
    goNext: PropTypes.func,
    openDatePicker: PropTypes.func
};

export class NavigatorView extends React.Component {
    render() {
        const { dateString, goPrevious, goNext, openDatePicker } = this.props;
        let previousButton = null;
        if (typeof goPrevious === "function") {
            previousButton = (
                <IconButton
                    id="previous-button"
                    onClick={goPrevious}
                    style={styles.buttonStyle}
                    iconStyle={styles.buttonStyle}
                >
                    <PreviousIcon />
                </IconButton>
            );
        }
        let nextButton = null;
        if (typeof goNext === "function") {
            nextButton = (
                <IconButton id="next-button" onClick={goNext} style={styles.buttonStyle} iconStyle={styles.buttonStyle}>
                    <NextIcon />
                </IconButton>
            );
        }
        return (
            <div id={"navigator-view"} style={styles.main}>
                <div style={styles.buttonContainer}>{previousButton}</div>
                <div style={styles.dateDisplay} onClick={openDatePicker}>
                    {dateString}
                </div>
                <div style={styles.buttonContainer}>{nextButton}</div>
            </div>
        );
    }
}
NavigatorView.propTypes = viewPropTypes;

export default NavigatorCtrl;
