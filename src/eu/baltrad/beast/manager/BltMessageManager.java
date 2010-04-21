/* --------------------------------------------------------------------
Copyright (C) 2009-2010 Swedish Meteorological and Hydrological Institute, SMHI,

This file is part of the Beast library.

Beast library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Beast library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with the Beast library library.  If not, see <http://www.gnu.org/licenses/>.
------------------------------------------------------------------------*/
package eu.baltrad.beast.manager;

import java.util.List;

import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRouter;

/**
 * The message manager that will distribute the messages to
 * the available adaptors/routes.
 * @author Anders Henja
 */
public class BltMessageManager implements IBltMessageManager {
  /**
   * The router
   */
  private IRouter router = null;
  
  /**
   * The main adaptor
   */
  private IBltAdaptorManager manager = null;
  
  /**
   * @param router the router to set
   */
  public void setRouter(IRouter router) {
    this.router = router;
  }

  /**
   * @param adaptor the adaptor to set
   */
  public void setManager(IBltAdaptorManager manager) {
    this.manager = manager;
  }

  /**
   * @see IBltMessageManager#manage(IBltMessage)
   */
  public void manage(IBltMessage message) {
    List<IMultiRoutedMessage> msgs = router.getMultiRoutedMessages(message);
    for (IMultiRoutedMessage msg : msgs) {
      manager.handle(msg);
    }
  }
}
