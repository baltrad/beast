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
package eu.baltrad.beast.scheduler;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.CronTriggerBean;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

/**
 * @author Anders Henja
 *
 */
public class BeastJobInvokerTest extends TestCase {
  private static interface MockMethods {
    public BltTriggerJobMessage createMessage(String id, String name);
  };
  private MockControl methodsControl = null;
  private MockMethods methods = null;
  private MockControl ctxControl = null;
  private JobExecutionContext ctx = null;
  private MockControl msgManagerControl = null;
  private IBltMessageManager msgManager = null;
  
  private BeastJobInvoker classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    methodsControl = MockControl.createControl(MockMethods.class);
    methods = (MockMethods)methodsControl.getMock();
    ctxControl = MockClassControl.createControl(JobExecutionContext.class);
    ctx = (JobExecutionContext)ctxControl.getMock();
    msgManagerControl = MockControl.createControl(IBltMessageManager.class);
    msgManager = (IBltMessageManager)msgManagerControl.getMock();
    
    classUnderTest = new BeastJobInvoker() {
      protected BltTriggerJobMessage createMessage(String id, String name) {
        return methods.createMessage(id, name);
      }
    };
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    classUnderTest = null;
  }
  
  protected void replay() {
    ctxControl.replay();
    msgManagerControl.replay();
    methodsControl.replay();
  }
  
  protected void verify() {
    ctxControl.verify();
    msgManagerControl.verify();
    methodsControl.verify();
  }
  
  public void testExecute() throws Exception {
    CronTriggerBean trigger = new CronTriggerBean();
    trigger.setName("a.id");
    JobDetail detail = new JobDetail();
    detail.setName("a.name");
    detail.getJobDataMap().put("messageManager", msgManager);

    BltTriggerJobMessage msg = new BltTriggerJobMessage();
    
    ctx.getJobDetail();
    ctxControl.setReturnValue(detail);
    ctx.getTrigger();
    ctxControl.setReturnValue(trigger);
    methods.createMessage("a.id", "a.name");
    methodsControl.setReturnValue(msg);
    msgManager.manage(msg);
    
    replay();
    
    classUnderTest.execute(ctx);

    verify();
  }
  
  public void testCreateMessage() throws Exception {
    classUnderTest = new BeastJobInvoker();
    BltTriggerJobMessage result = classUnderTest.createMessage("a.id", "a.name");
    assertEquals("a.id", result.getId());
    assertEquals("a.name", result.getName());
  }
}
