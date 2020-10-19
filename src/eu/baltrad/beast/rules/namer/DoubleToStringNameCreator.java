/**
 * 
 */
package eu.baltrad.beast.rules.namer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import org.springframework.beans.factory.InitializingBean;

import eu.baltrad.bdb.oh5.Attribute;
import eu.baltrad.bdb.oh5.Metadata;

/**
 * @author anders
 *
 */
public class DoubleToStringNameCreator implements MetadataNameCreator, InitializingBean {
  private Pattern pattern = null;
  private String attributePattern = null;
  private String defaultAttribute = null;
  private String defaultValue = null;
  private Map<Double, String> mapping = null;
  
  @Override
  public boolean supports(String tag) {
    if (tag != null) {
      Matcher m = pattern.matcher(tag);
      return m.matches();
    }
    return false;
  }

  protected Double getAttributeValue(String name, Metadata metadata) {
    Attribute attr = metadata.getAttribute(name);
    if (attr != null) {
      return attr.toDouble();
    }
    return null;    
  }
  
  @Override
  public String createName(String tag, Metadata metadata) {
    if (!supports(tag)) {
      throw new MetadataNameCreatorException("Not supported tag: " + tag);
    }
    
    String result = defaultValue;
    String attrloc = defaultAttribute;
    if (tag.contains(":")) {
      attrloc = tag.substring(tag.indexOf(":")+1);
    }
      
    Double dval = getAttributeValue(attrloc, metadata);
    if (dval != null) {
      if (mapping.containsKey(dval.doubleValue())) {
        result = mapping.get(dval.doubleValue());
      }
    }
    
    return result;
  }

  public String getAttributePattern() {
    return attributePattern;
  }

  public void setAttributePattern(String attributePattern) {
    this.attributePattern = attributePattern;
  }

  public String getDefaultAttribute() {
    return defaultAttribute;
  }

  public void setDefaultAttribute(String defaultAttribute) {
    this.defaultAttribute = defaultAttribute;
  }

  public Map<Double, String> getMapping() {
    return mapping;
  }

  public void setMapping(Map<Double, String> mapping) {
    this.mapping = mapping;
  }

  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (attributePattern == null) {
      throw new RuntimeException("attributePattern not set");
    }
    if (mapping == null)  {
      throw new RuntimeException("mapping not set");
    }
    pattern = Pattern.compile(attributePattern);
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

}
