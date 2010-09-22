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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;

import org.easymock.MockControl;

import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRouter;


/**
 * @author Anders Henja
 */
public class BltMessageManagerTest extends TestCase {
  private MockControl routerControl = null;
  private IRouter router = null;
  private MockControl managerControl = null;
  private IBltAdaptorManager manager = null;
  private MockControl executorControl = null;
  private ExecutorService executor = null;
  private BltMessageManager classUnderTest = null;
  
  protected void setUp() throws Exception {
    routerControl = MockControl.createControl(IRouter.class);
    router = (IRouter)routerControl.getMock();
    managerControl = MockControl.createControl(IBltAdaptorManager.class);
    manager = (IBltAdaptorManager)managerControl.getMock();
    executorControl = MockControl.createControl(ExecutorService.class);
    executor = (ExecutorService)executorControl.getMock();
    
    classUnderTest = new BltMessageManager();
    classUnderTest.setRouter(router);
    classUnderTest.setManager(manager);
    classUnderTest.setExecutor(executor);
  }
  
  protected void tearDown() throws Exception {
    routerControl = null;
    router = null;
    managerControl = null;
    manager = null;
    executorControl = null;
    executor = null;
    classUnderTest = null;
  }
  
  protected void replay() {
    routerControl.replay();
    managerControl.replay();
    executorControl.replay();
  }
  
  protected void verify() {
    routerControl.verify();
    managerControl.verify();
    executorControl.verify();
  }
  
  public void testManage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    final Runnable r = new Runnable() {
      @Override
      public void run() {
      }
    };
    
    executor.execute(r);
    
    classUnderTest = new BltMessageManager() {
      protected Runnable createRunnable(IBltMessage message) {
        return r;
      }
    };
    classUnderTest.setExecutor(executor);
    
    replay();
    
    classUnderTest.manage(message);
    
    verify();
  }
  
  public void testCreateRunnable() throws Exception {
    IBltMessage message = new IBltMessage() {};
    List<IMultiRoutedMessage> messages = new ArrayList<IMultiRoutedMessage>();
    BltMultiRoutedMessage m1 = new BltMultiRoutedMessage();
    BltMultiRoutedMessage m2 = new BltMultiRoutedMessage();
    messages.add(m1);
    messages.add(m2);
    
    router.getMultiRoutedMessages(message);
    routerControl.setReturnValue(messages);
    manager.handle(m1);
    manager.handle(m2);
    
    replay();
    
    Runnable r = classUnderTest.createRunnable(message);
    r.run();
    
    verify();
  }
}
