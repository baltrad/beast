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

import eu.baltrad.beast.manager.IBltMessageManager;
import eu.baltrad.beast.message.IBltMessage;

/**
 * @author Anders Henja
 *
 */
public class BeastJobInvokerTest extends TestCase {
  private MockControl ctxControl = null;
  private JobExecutionContext ctx = null;
  private MockControl jobControl = null;
  private IBeastJob job = null;
  private MockControl msgManagerControl = null;
  private IBltMessageManager msgManager = null;
  
  private BeastJobInvoker classUnderTest = null;
  
  public void setUp() throws Exception {
    super.setUp();
    classUnderTest = new BeastJobInvoker();
    ctxControl = MockClassControl.createControl(JobExecutionContext.class);
    ctx = (JobExecutionContext)ctxControl.getMock();
    jobControl = MockControl.createControl(IBeastJob.class);
    job = (IBeastJob)jobControl.getMock();
    msgManagerControl = MockControl.createControl(IBltMessageManager.class);
    msgManager = (IBltMessageManager)msgManagerControl.getMock();    
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
    classUnderTest = null;
  }
  
  protected void replay() {
    ctxControl.replay();
    jobControl.replay();
    msgManagerControl.replay();
  }
  
  protected void verify() {
    ctxControl.verify();
    jobControl.verify();
    msgManagerControl.verify();
  }
  
  public void testExecute() throws Exception {
    JobDetail detail = new JobDetail();
    detail.getJobDataMap().put("job", job);
    detail.getJobDataMap().put("messageManager", msgManager);
    
    IBltMessage msg = new IBltMessage() {};
    ctx.getJobDetail();
    ctxControl.setReturnValue(detail);
    job.trigger();
    jobControl.setReturnValue(msg);
    msgManager.manage(msg);
    
    replay();
    
    classUnderTest.execute(ctx);

    verify();
  }
  
  public void testExecute_noReturnedMessage() throws Exception {
    JobDetail detail = new JobDetail();
    detail.getJobDataMap().put("job", job);
    detail.getJobDataMap().put("messageManager", msgManager);
    
    ctx.getJobDetail();
    ctxControl.setReturnValue(detail);
    job.trigger();
    jobControl.setReturnValue(null);
    
    replay();
    
    classUnderTest.execute(ctx);

    verify();
  }
}
