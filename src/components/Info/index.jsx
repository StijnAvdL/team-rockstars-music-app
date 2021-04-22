import React from "react";
import { useObserver } from "mobx-react";
import { Typography } from '@material-ui/core';

const styles = {
    container: {
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between",
        margin: "15px"
    }
}

function Info(props) {
    return useObserver(() => (
        <div style={styles.container}>
            <Typography variant="p">
                Deze app bevat twee testen, de Timed-Up-and-GO (TUG) en cognitietest. Deze twee testen geven inzicht in uw algemene fitheid. Deze inzichten kunnen u en uw arts gebruiken in uw Multiple Myeloom- behandeltraject.
            </Typography>
        </div>
    ));

}

export default Info;
