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

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.mo.BltTriggerJobMessage;

/**
 * @author Anders Henja
 */
public class BeastJobInvokerTest extends EasyMockSupport {
  private static interface MockMethods {
    public BltTriggerJobMessage createMessage(String id, String name);
  };
  private JobExecutionContext ctx = null;
  private IBltMessageManager msgManager = null;
  private MockMethods methods = null;
  private BeastJobInvoker classUnderTest = null;

  @Before
  public void setUp() throws Exception {
    methods = createMock(MockMethods.class);
    ctx = createMock(JobExecutionContext.class);
    msgManager = createMock(IBltMessageManager.class);
    classUnderTest = new BeastJobInvoker() {
      protected BltTriggerJobMessage createMessage(String id, String name) {
        return methods.createMessage(id, name);
      }
    };
  }

  @After
  public void tearDown() throws Exception {
    classUnderTest = null;
  }

  @Test
  public void testExecute() throws Exception {
    CronTrigger trigger = createMock(CronTrigger.class);
    JobDetail detail = createMock(JobDetail.class); //new JobDetail();
    TriggerKey key = new TriggerKey("a.id","beast");
    JobKey jobKey = new JobKey("a.name", "beast");
    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put("messageManager", msgManager);

    BltTriggerJobMessage msg = new BltTriggerJobMessage();
    
    expect(ctx.getJobDetail()).andReturn(detail);
    expect(detail.getKey()).andReturn(jobKey);
    expect(detail.getJobDataMap()).andReturn(jobDataMap);
    
    expect(ctx.getTrigger()).andReturn(trigger);
    expect(trigger.getKey()).andReturn(key);
    expect(methods.createMessage("a.id","a.name")).andReturn(msg);
    
    msgManager.manage(msg);

    replayAll();
    
    classUnderTest.execute(ctx);

    verifyAll();
  }
  
  @Test
  public void testCreateMessage() throws Exception {
    classUnderTest = new BeastJobInvoker();
    BltTriggerJobMessage result = classUnderTest.createMessage("a.id", "a.name");
    Assert.assertEquals("a.id", result.getId());
    Assert.assertEquals("a.name", result.getName());
  }
}
