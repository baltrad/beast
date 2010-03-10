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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import eu.baltrad.beast.pgfwk.BaltradXmlRpcServer;
import eu.baltrad.beast.router.RouteDefinition;
import eu.baltrad.beast.router.impl.BltRouter;

/**
 * @author Anders Henja
 *
 */
public class BltManagerTest extends TestCase {
  /**
   * The class under test
   */
  private BltMessageManager classUnderTest = null;
  
  /**
   * The xmlrpc server 
   */
  private BaltradXmlRpcServer server = null;
  
  /**
   * The timing generator plugin
   */
  private TimingGeneratorPlugin generator = null;
  
  /**
   * Extracts only the class name (no package included).
   * @param clz the class
   * @return the name
   */
  @SuppressWarnings("unchecked")
  private static String getClassName(Class clz) {
    String nm = clz.getName();
    int li = nm.lastIndexOf(".");
    if (li > 0) {
      nm = nm.substring(li+1);
    }
    return nm;
  }
  
  protected void initializeDatabase() throws Exception {
    String cln = getClassName(this.getClass());
    String cname = cln + "-dbcontext.xml";
    File f = new File(this.getClass().getResource(cname).getFile());
    
    ApplicationContext dbcontext = new ClassPathXmlApplicationContext("file:"+f.getAbsolutePath());
    SimpleJdbcTemplate template = (SimpleJdbcTemplate)dbcontext.getBean("jdbcTemplate");
    template.update("delete from router_dest");
    template.update("delete from adaptors_xmlrpc");
    template.update("delete from adaptors");
    template.update("delete from router_rules");
  }
  
  public void setUp() throws Exception {
    /*
    String cln = getClassName(this.getClass());
    String cname = cln + "-context.xml";
    File f = new File(this.getClass().getResource(cname).getFile());
    server = BaltradXmlRpcServer.getInstance("file:"+f.getAbsolutePath());
    server.start();
    classUnderTest = (BltMessageManager)server.getContext().getBean("manager");
    generator = (TimingGeneratorPlugin)server.getContext().getBean("");
    */
  }
  
  public void tearDown() throws Exception {
    /*
    server.shutdown();
    server = null;
    classUnderTest = null;
    generator = null;
    */
  }
  
  public void testManage() throws Exception {
    /*
    BltRouter router = (BltRouter)server.getContext().getBean("router");
    RouteDefinition def = new RouteDefinition();
    List<String> recipients = new ArrayList<String>();
    recipients.add("PGFWK");
    def.setName("bltmanagertestroute");
    def.setAuthor("anders");
    def.setDescription("test");
    def.setRecipients(recipients);
    router.storeDefinition(def);
    */
  }
  
}
