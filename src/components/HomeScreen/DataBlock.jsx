import React from "react";
import PropTypes from "prop-types";
import { PropTypes as MobxPropTypes } from "mobx-react";

import { textStyles } from "/src/configs/theme";

import { Loading } from "/orikami/components/main";

import t from "/src/services/translate";

import DataItem from "./DataItem";
import BlockHeader from "./BlockHeader";

import BatteryIcon from "material-ui/svg-icons/device/battery-std";
import EyeIcon from "material-ui/svg-icons/action/visibility";
import WalkIcon from "material-ui/svg-icons/maps/directions-walk";
import RestIcon from "material-ui/svg-icons/notification/airline-seat-recline-extra";
import PuzzleIcon from "material-ui/svg-icons/action/extension";
import DoneIcon from "material-ui/svg-icons/action/done";
import ErrorIcon from "material-ui/svg-icons/content/clear";

const propTypes = {
    results: MobxPropTypes.arrayOrObservableArrayOf(
        PropTypes.shape({
            label: PropTypes.string,
            value: PropTypes.any,
            action: PropTypes.func,
            overflowActions: MobxPropTypes.arrayOrObservableArrayOf(
                PropTypes.shape({
                    label: PropTypes.string,
                    value: PropTypes.any,
                    action: PropTypes.func
                })
            ),
            // depricated instead use value
            result: PropTypes.string
        })
    )
};
const defaultProps = {
    results: []
};

const styles = {
    itemContainer: {
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between"
    },
    resultMain: textStyles.testResults.main,
    resultTotal: textStyles.testResults.secondary,
    containerResult: {
        lineContainer: {
            display: "flex",
            alignItems: "center",
            flexWrap: "wrap"
        },
        childLabel: {
            display: "inline-block",
            minWidth: "calc(50% + 8px)",
            marginRight: 8,
            display: "flex",
            flexWrap: "wrap",
            overflow: "auto",
            alignItems: "center",
            lineHeight: "21px"
        },
        childValue: {},
        childIcon: {
            height: 18,
            width: 18,
            marginRight: 8
        }
    },
    doneIcon: {
        height: 32,
        width: 32,
        marginLeft: "calc(50% - 16px)"
    }
};

class DataBlock extends React.Component {
    itemResult(item) {
        switch (item.type) {
            case "experiment":
                return this.experimentResult(item);
            case "question":
                return this.questionResult(item);
            case "questionContainer":
                return this.questionContainerResult(item);
            case "taskBattery":
                return this.taskBatteryResult(item);
            default:
                if (typeof item.value === "object") {
                    return item.value.value;
                } else if (!item.value && item.result) {
                    return item.result;
                } else {
                    return item.value;
                }
        }
    }

    experimentResult(item) {
        switch (item.source) {
            case "walking":
                if (item.value.distance && item.value.distance > 0) {
                    return <span style={styles.resultMain}>{`${Math.ceil(item.value.distance)} m`}</span>;
                } else if (item.value.error !== "null" && item.value.error) {
                    return <ErrorIcon style={styles.doneIcon} />;
                } else {
                    return <DoneIcon style={styles.doneIcon} />;
                }
            case "sdmt":
                return (
                    <span>
                        <span style={styles.resultMain}>{item.value.correct}</span>
                        <span style={styles.resultTotal}>/{item.value.nb}</span>
                    </span>
                );
            case "visualResponse":
                return <DoneIcon style={styles.doneIcon} />;
            default:
                if (typeof item.value === "object") {
                    return item.value.value;
                } else if (!item.value && item.result) {
                    return item.result;
                } else {
                    return item.value;
                }
        }
    }

    questionContainerResult(item) {
        if (typeof item.value === "object") {
            if (item.value.children) {
                return item.value.children.map((child, i) => {
                    return (
                        <div key={i} style={styles.containerResult.lineContainer}>
                            <div style={styles.containerResult.childLabel}>{child.label}:</div>
                            <div style={styles.containerResult.childValue}>{child.answer || child.value}</div>
                        </div>
                    );
                });
            } else {
                // old temporary, now obsolete code:
                // kept to prevent errors on staging
                return item.value.value + " vragen";
            }
        } else {
            return item.value;
        }
    }

    questionResult(item) {
        if (typeof item.value === "object") {
            switch (item.value.type) {
                case "Slider-smile":
                case "Slider":
                    // NB find solution for different styling value/max
                    return (
                        <span>
                            <span style={styles.resultMain}>
                                {/* {parseFloat(item.value.value).toFixed(1)} */}
                                {item.value.value}
                            </span>
                            <span style={styles.resultTotal}>/{item.value.max}</span>
                        </span>
                    );
                default:
                    return item.value.value;
            }
        } else {
            return item.value;
        }
    }

    taskBatteryResult(item) {
        const getIcon = task => {
            switch (task) {
                case "VAS":
                    return <BatteryIcon style={styles.containerResult.childIcon} />;
                case "HST":
                case "CST":
                    return <EyeIcon style={styles.containerResult.childIcon} />;
                case "MT":
                    return <WalkIcon style={styles.containerResult.childIcon} />;
                case "CT":
                    return <PuzzleIcon style={styles.containerResult.childIcon} />;
                case "R":
                    return <RestIcon style={styles.containerResult.childIcon} />;
                default:
                    return <div style={styles.containerResult.childIcon} />;
            }
        };
        // child results are derived from the (timeline) save functions of the different
        // experiment models, therefore they differ in value-format
        const prepareChild = child => {
            const checkMark = "\u2713";
            let displayValue;
            if (["HST", "CST", "R"].indexOf(child.task) > -1) {
                displayValue = checkMark;
            } else if (typeof child.value === "undefined" || child.value === null || child.value === true) {
                displayValue = checkMark;
            } else {
                if (child.task === "VAS" && child.value.answer) {
                    displayValue = child.value.answer;
                } else if (typeof child.value !== "object") {
                    displayValue = child.value;
                } else if (typeof child.value.value !== "undefined") {
                    displayValue = child.value.value;
                } else {
                    displayValue = checkMark;
                }
            }
            return {
                label: t("Exp.TaskBattery.TaskLabels." + child.task),
                value: displayValue,
                icon: getIcon(child.task)
            };
        };
        return item.value.children.map((_child, i) => {
            const child = prepareChild(_child);
            return (
                <div key={i} style={styles.containerResult.lineContainer}>
                    <div style={styles.containerResult.childLabel}>
                        {child.icon}
                        {child.label}
                    </div>
                    <div style={styles.containerResult.childValue}>{child.value}</div>
                </div>
            );
        });
    }

    render() {
        const { results } = this.props;
        if (!results.length) {
            return null;
        }
        return (
            <div id="data-block">
                <BlockHeader title={t("HomeScreen.DataTitle", "Gegevens van vandaag")} />
                <div style={styles.itemContainer}>
                    {results.map((item, i) => (
                        <DataItem
                            key={i}
                            label={item.label}
                            type={
                                item.type === "taskBattery" || item.type === "questionContainer"
                                    ? "container"
                                    : "single"
                            }
                            result={this.itemResult(item)}
                            onClick={item.action}
                            loading={item.value.loading}
                            menuItems={item.overflowActions}
                            infoAction={item.infoAction}
                        />
                    ))}
                </div>
            </div>
        );
    }
}

DataBlock.propTypes = propTypes;
DataBlock.defaultProps = defaultProps;

export default DataBlock;
