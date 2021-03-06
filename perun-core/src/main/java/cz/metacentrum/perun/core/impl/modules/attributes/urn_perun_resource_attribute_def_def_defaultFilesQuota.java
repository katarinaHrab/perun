package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;

/**
 *
 * @author Michal Stava stavamichal@gmail.com
 * @version $Id$
 */
public class urn_perun_resource_attribute_def_def_defaultFilesQuota extends ResourceAttributesModuleAbstract implements ResourceAttributesModuleImplApi {

  private static final String A_R_defaultFilesLimit = AttributesManager.NS_RESOURCE_ATTR_DEF + ":defaultFilesLimit";

  @Override
  public void checkAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute) throws InternalErrorException, WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    Attribute attrDefaultFilesLimit = null;
    Integer defaultFilesQuota = null;
    Integer defaultFilesLimit = null;
    
    //Get DefaultFilesLimit attribute
    try {
      attrDefaultFilesLimit = perunSession.getPerunBl().getAttributesManagerBl().getAttribute(perunSession, resource, A_R_defaultFilesLimit);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException("Attribute with defaultFilesLimit from resource " + resource.getId() + " could not obtained.", ex);
    }
    
    //Get defaultFilesQuota value
    if(attribute.getValue() != null) {
        defaultFilesQuota = (Integer) attribute.getValue();
    }
    if(defaultFilesQuota != null && defaultFilesQuota < 0) throw new WrongAttributeValueException(attribute, attribute + " cannot be less than 0.");
    
    //Get defaultFilesLimit value
    if(attrDefaultFilesLimit != null &&  attrDefaultFilesLimit.getValue() != null) {
      defaultFilesLimit = (Integer) attrDefaultFilesLimit.getValue();
    }
    if(defaultFilesLimit != null && defaultFilesLimit < 0) throw new ConsistencyErrorException(attrDefaultFilesLimit + " cannot be less than 0.");
    
    //Compare DefaultFilesQuota with DefaultFilesLimit
    if(defaultFilesQuota == null || defaultFilesQuota == 0) {
      if(defaultFilesLimit != null && defaultFilesLimit != 0) throw new WrongReferenceAttributeValueException(attribute, attrDefaultFilesLimit, "Try to set unlimited quota, but limit is still " + defaultFilesLimit);
    }else if(defaultFilesLimit != null && defaultFilesLimit != 0) {
      if(defaultFilesLimit < defaultFilesQuota) throw new WrongReferenceAttributeValueException(attribute, attrDefaultFilesLimit, attribute + " must be less than or equals " + attrDefaultFilesLimit);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("defaultFilesQuota");
    attr.setType(Integer.class.getName());
    attr.setDescription("Soft quota for number of files.");
    return attr;
  }        
}
