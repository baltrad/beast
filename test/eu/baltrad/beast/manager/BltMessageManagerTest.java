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

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.baltrad.beast.adaptor.IBltAdaptorManager;
import eu.baltrad.beast.message.IBltMessage;
import eu.baltrad.beast.message.mo.BltMultiRoutedMessage;
import eu.baltrad.beast.router.IMultiRoutedMessage;
import eu.baltrad.beast.router.IRouter;

/**
 * @author Anders Henja
 */
public class BltMessageManagerTest extends EasyMockSupport {
  private IRouter router = null;
  private IBltAdaptorManager manager = null;
  private ExecutorService executor = null;
  private BltMessageManager classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    router = createMock(IRouter.class);
    manager = createMock(IBltAdaptorManager.class);
    executor = createMock(ExecutorService.class);
    
    classUnderTest = new BltMessageManager();
    classUnderTest.setRouter(router);
    classUnderTest.setManager(manager);
    classUnderTest.setExecutor(executor);
  }

  @After
  public void tearDown() throws Exception {
    router = null;
    manager = null;
    executor = null;
    classUnderTest = null;
  }
 
  @Test
  public void testManage() throws Exception {
    IBltMessage message = new IBltMessage() {};
    final Runnable r = new Runnable() {
      @Override
      public void run() {
      }
    };
    
    expect(executor.isShutdown()).andReturn(false);
    
    executor.execute(r);
    
    classUnderTest = new BltMessageManager() {
      protected Runnable createRunnable(IBltMessage message) {
        return r;
      }
    };
    classUnderTest.setExecutor(executor);
    
    replayAll();
    
    classUnderTest.manage(message);
    
    verifyAll();
  }
  
  
  @Test
  public void testManage_onShutdown() {
    IBltMessage message = new IBltMessage() {};
    final Runnable r = new Runnable() {
      @Override
      public void run() {
      }
    };
    
    expect(executor.isShutdown()).andReturn(true);

    classUnderTest = new BltMessageManager() {
      protected Runnable createRunnable(IBltMessage message) {
        return r;
      }
    };
    classUnderTest.setExecutor(executor);
    
    replayAll();
    
    classUnderTest.manage(message);
    
    verifyAll();
  }
  
  @Test
  public void testCreateRunnable() throws Exception {
    IBltMessage message = new IBltMessage() {};
    List<IMultiRoutedMessage> messages = new ArrayList<IMultiRoutedMessage>();
    BltMultiRoutedMessage m1 = new BltMultiRoutedMessage();
    BltMultiRoutedMessage m2 = new BltMultiRoutedMessage();
    messages.add(m1);
    messages.add(m2);
    
    expect(router.getMultiRoutedMessages(message)).andReturn(messages);
    manager.handle(m1);
    manager.handle(m2);
    
    replayAll();
    
    Runnable r = classUnderTest.createRunnable(message);
    r.run();
    
    verifyAll();
  }
}
