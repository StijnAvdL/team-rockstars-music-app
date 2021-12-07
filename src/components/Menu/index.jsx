import React from 'react'
import { useObserver } from 'mobx-react'
import PropTypes from 'prop-types'

import MenuModel from '/src/models/Menu'
import SwipeableDrawer from '@material-ui/core/SwipeableDrawer'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import ListItemText from '@material-ui/core/ListItemText'

import PersonIcon from '@material-ui/icons/Person'
import QueueMusicIcon from '@material-ui/icons/QueueMusic'

const propTypes = {
  go: PropTypes.func,
}

function Menu(props) {
  const { go } = props
  const menuItems = [
    {
      title: 'Artists',
      icon: <PersonIcon />,
      onClick: () => {
        go('/')
        MenuModel.close()
      },
    },
    {
      title: 'Playlists',
      icon: <QueueMusicIcon />,
      onClick: () => {
        go('/playlists')
        MenuModel.close()
      },
    },
  ]

  return useObserver(() => (
    <SwipeableDrawer anchor={'left'} open={MenuModel.open} onClose={MenuModel.close} onOpen={MenuModel.toggle}>
      <List style={{ paddingRight: '15px' }}>
        {menuItems.map((item, index) => (
          <ListItem button key={index} onClick={item.onClick}>
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.title} />
          </ListItem>
        ))}
      </List>
    </SwipeableDrawer>
  ))
}

Menu.propTypes = propTypes

export default Menu
