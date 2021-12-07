import { useObserver } from 'mobx-react'
import Popover from '@material-ui/core/Popover'

function Popup(props) {
  return useObserver(() => (
    <Popover
      anchorOrigin={{
        vertical: 'bottom',
        horizontal: 'center',
      }}
      transformOrigin={{
        vertical: 'bottom',
        horizontal: 'center',
      }}
      open={true}
    >
      {props.error || 'Error!'}
    </Popover>
  ))
}

export default Popup
