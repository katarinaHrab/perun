/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeHolders;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.exceptions.AttributeExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.AttributeCacheManagerImplApi;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import sun.jdbc.odbc.ee.DataSource;


/**
 *
 * @author Katarína Hrabovská
 */
public class ApplicationCacheTest extends AbstractPerunIntegrationTest {
        
    
        
        private AttributeCacheManagerImpl attributeCacheManagerImpl = new AttributeCacheManagerImpl();

        //World's variables
        private User user1;
        private User user2;
        private User user3;
        private Facility facility1;
        private Facility facility2;
        private Facility facility3;
        private Attribute attribute1;
        private Attribute attribute2;
        private Attribute attribute3;
        private AttributeDefinition attributeDefinition1;
        private AttributeDefinition attributeDefinition2;
        private String key1;
        private String key2;

   
    
        
    @Before
	public void setUp() throws Exception {
            this.setUpWorld();
    }
    
    public void setUpWorld() throws Exception {                
                
                        
                //Set users
                user1 = new User();
                user2 = new User();
                user3 = new User();
                
                //Set facility
                facility1 = new Facility();
                facility2 = new Facility();
                facility3 = new Facility();
                
        
                //Set attributes
                attribute1 = new Attribute();
                attribute1.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                attribute1.setFriendlyName("name1");
                attribute1.setValue(1);
                attribute1.setId(111);
              
                
                
                attribute2 = new Attribute();
                attribute2.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                attribute2.setFriendlyName("name2");
                attribute2.setValue(2);
                attribute2.setId(222);
                
                attribute3 = new Attribute();
                attribute3.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                attribute3.setFriendlyName("name3");
                attribute3.setValue(3);
                attribute3.setId(333);
                
                attributeDefinition1 = new AttributeDefinition();
                attributeDefinition1.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                attributeDefinition1.setFriendlyName("definitionName1");
                attributeDefinition1.setId(11);
                
                
                attributeDefinition2 = new AttributeDefinition();
                attributeDefinition2.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                attributeDefinition2.setFriendlyName("definitionName2");
                attributeDefinition2.setId(22);
                
                key1 = new String("key1");
                key2 = new String("key2");
    }
    
    @After
    public void tearDown() {
        attributeCacheManagerImpl.flushCache();
    }
    
    @Test
        public void addAndGetFromCacheTest() {
            System.out.println("attributeCacheManagerImpl.addAndGetFromCacheTest");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            Attribute attributeFromCache = new Attribute();
            attributeFromCache = attributeCacheManagerImpl.getAttributeFromCache(attributeHolders, attribute1.getName());
            assertEquals(attribute1, attributeFromCache);
         }
    
    @Test
        public void removeFromCacheTest() {
            System.out.println("attributeCacheManagerImpl.removeFromCacheTest");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            attributeCacheManagerImpl.removeAttributeFromCache(attributeHolders, attribute1);
            assertEquals(null, attributeCacheManagerImpl.getAttributeFromCache(attributeHolders, attribute1.getName()));
    }
    
    @Test
        public void addMoreAttributesToOneUserTest() {
            System.out.println("attributeCacheManagerImpl.addMoreAttributesToOneUserTest");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute2);
            assertEquals(attribute1, attributeCacheManagerImpl.getAttributeFromCache(attributeHolders, attribute1.getName()));
            assertEquals(attribute2, attributeCacheManagerImpl.getAttributeFromCache(attributeHolders, attribute2.getName()));
    }
    
    @Test 
        public void addMoreUsersTest() {
            System.out.println("attributeCacheManagerImpl.addMoreUsersTest");
            AttributeHolders attributeHolders1 = new AttributeHolders(user1, null);
            AttributeHolders attributeHolders2 = new AttributeHolders(user2, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders1, attribute1);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders2, attribute2);
            assertEquals(attribute1, attributeCacheManagerImpl.getAttributeFromCache(attributeHolders1, attribute1.getName()));
            assertEquals(attribute2, attributeCacheManagerImpl.getAttributeFromCache(attributeHolders2, attribute2.getName()));
    }
    
    @Test
        public void consistencyOfObjectTest() {
            System.out.println("attributeCacheManagerImpl.consistencyOfObjectTest");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            attributeCacheManagerImpl.getAttributeFromCache(attributeHolders, attribute1.getName()).setFriendlyName("name2");
            String attributeName = attributeCacheManagerImpl.getAttributeFromCache(attributeHolders, attribute1.getName()).getFriendlyName();
            assertEquals(attribute1.getFriendlyName(), attributeName);
    }
    
    @Test
        public void updateOfAttributeWithDiffValue() {
            System.out.println("attributeCacheManagerImpl.updateOfAttributeWithDiffValue");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            attribute1.setValue(123);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            assertEquals(123, attributeCacheManagerImpl.getAttributeFromCache(attributeHolders, attribute1.getName()).getValue());
            AttributeHolders attrHolder = new AttributeHolders(user1, null);
            assertEquals(1, attributeCacheManagerImpl.getApplicationCache().get(attrHolder).size());
    }
        
    @Test
        public void getAllAttributesFromCache() {
            System.out.println("attributeCacheManagerImpl.getAllAttributesFromCache");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute2);
            assertEquals(2, attributeCacheManagerImpl.getAllAttributesFromCache(attributeHolders).size());
            assertEquals(true, attributeCacheManagerImpl.getAllAttributesFromCache(attributeHolders).contains(attribute1));
            assertEquals(true, attributeCacheManagerImpl.getAllAttributesFromCache(attributeHolders).contains(attribute2));
        }
        
     @Test
        public void getAttributeByIdFromCache() {
            System.out.println("attributeCacheManagerImpl.getAttributeByIdFromCache");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            assertEquals(attribute1, attributeCacheManagerImpl.getAttributeByIdFromCache(attributeHolders, attribute1.getId()));
        }
        
     @Test
        public void removeAllAttributesFromCache() {
            System.out.println("attributeCacheManagerImpl.removeAllAttributesFromCache");
            AttributeHolders attributeHolders = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute2);
            attributeCacheManagerImpl.removeAllAttributesFromCache(attributeHolders);
            assertEquals(0, attributeCacheManagerImpl.getApplicationCache().get(attributeHolders).size());
        }
        
      @Test
        public void removeAllUserFacilityAttributesForAnyUserFromCache() {
            System.out.println("attributeCacheManagerImpl.removeAllUserFacilityAttributesForAnyUserFromCache");
            AttributeHolders attributeHolders1 = new AttributeHolders(user1, facility1);
            AttributeHolders attributeHolders2 = new AttributeHolders(user2, facility1);
            AttributeHolders attributeHolders3 = new AttributeHolders(user1, null);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders1, attribute1);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders1, attribute2);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders2, attribute1);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders3, attribute3);
            attributeCacheManagerImpl.removeAllUserFacilityAttributesForAnyUserFromCache(facility1);
            assertEquals(0, attributeCacheManagerImpl.getApplicationCache().get(attributeHolders1).size());
            assertEquals(0, attributeCacheManagerImpl.getApplicationCache().get(attributeHolders2).size());
            assertEquals(1, attributeCacheManagerImpl.getApplicationCache().get(attributeHolders3).size());
        }
        
      @Test
        public void removeAllUserFacilityAttributesFromCache() {
             System.out.println("attributeCacheManagerImpl.removeAllUserFacilityAttributesFromCache");
             AttributeHolders attributeHolders1 = new AttributeHolders(user1, facility1);
             AttributeHolders attributeHolders2 = new AttributeHolders(user1, facility2);
             AttributeHolders attributeHolders3 = new AttributeHolders(user1, null);
             attributeCacheManagerImpl.addAttributeToCache(attributeHolders1, attribute1);
             attributeCacheManagerImpl.addAttributeToCache(attributeHolders1, attribute2);
             attributeCacheManagerImpl.addAttributeToCache(attributeHolders2, attribute1);
             attributeCacheManagerImpl.addAttributeToCache(attributeHolders3, attribute3);
             attributeCacheManagerImpl.removeAllUserFacilityAttributesFromCache(user1);
             assertEquals(0, attributeCacheManagerImpl.getApplicationCache().get(attributeHolders1).size());
             assertEquals(0, attributeCacheManagerImpl.getApplicationCache().get(attributeHolders2).size());
             assertEquals(1, attributeCacheManagerImpl.getApplicationCache().get(attributeHolders3).size());
        }
        
      @Test
        public void addAndGetAttributeToCacheForString() {
            System.out.println("attributeCacheManagerImpl.addAndGetAttributeToCacheForString");
            attributeCacheManagerImpl.addAttributeToCacheForString(key1, attribute1);
            Attribute attributeFromCache = attributeCacheManagerImpl.getAttributeFromCacheForString(key1, attribute1.getName());
            assertEquals(attribute1, attributeFromCache);
        }
        
      @Test
        public void removeFromCacheForStringTest() {
            System.out.println("attributeCacheManagerImpl.removeFromCacheForStringTest");
            attributeCacheManagerImpl.addAttributeToCacheForString(key1, attribute1);
            attributeCacheManagerImpl.removeAttributeFromCacheForString(key1, attribute1);
            assertEquals(null, attributeCacheManagerImpl.getAttributeFromCacheForString(key1, attribute1.getName()));
        }
        
      @Test
        public void addAndGetAttributeToCacheForAttributes() {
            System.out.println("attributeCacheManagerImpl.addAndGetAttributeToCacheForAttributes");
            attributeCacheManagerImpl.addAttributeToCacheForAttributes(attributeDefinition1);
            AttributeDefinition attributeFromCache = attributeCacheManagerImpl.getAttributeFromCacheForAttributes(attributeDefinition1.getName());
            assertEquals(attributeDefinition1, attributeFromCache);
        }
        
      @Test
        public void removeFromCacheForAttributesTest() {
            System.out.println("attributeCacheManagerImpl.removeFromCacheForAttributesTest");
            attributeCacheManagerImpl.addAttributeToCacheForAttributes(attributeDefinition1);
            attributeCacheManagerImpl.removeAttributeFromCacheForAttributes(attributeDefinition1);
            assertEquals(null, attributeCacheManagerImpl.getAttributeFromCacheForAttributes(attributeDefinition1.getName()));
        }
        
      @Test
        public void getAttributeByIdFromCacheForAttributes() {
            System.out.println("attributeCacheManagerImpl.getAttributeByIdFromCacheForAttributes");
            attributeCacheManagerImpl.addAttributeToCacheForAttributes(attributeDefinition1);
            AttributeDefinition attributeFromCache = attributeCacheManagerImpl.getAttributeByIdFromCacheForAttributes(attributeDefinition1.getId());
            assertEquals(attributeDefinition1, attributeFromCache);
        }
        
       @Test
       public void getAttributeFromCacheForReverseEntities() {
            System.out.println("attributeCacheManagerImpl.getAttributeFromCacheForReverseEntities");
            AttributeHolders attributeHolders = new AttributeHolders(user1, facility1);
            AttributeHolders attributeHoldersReverse = new AttributeHolders(facility1, user1);
            attributeCacheManagerImpl.addAttributeToCache(attributeHolders, attribute1);
            Attribute attributeFromCache = new Attribute();
            attributeFromCache = attributeCacheManagerImpl.getAttributeFromCache(attributeHoldersReverse, attribute1.getName());
            assertEquals(attribute1, attributeFromCache);
        }
       
    /*   @Test
       public void createAndGetAttributeDefinitionTest() throws PrivilegeException, InternalErrorException, AttributeExistsException, AttributeNotExistsException {
           System.out.println("attributeCacheManagerImpl.veryStupidTest");
           attributeDefinition1.setType(String.class.getName());
           perun.getAttributesManagerBl().createAttribute(sess, attributeDefinition1);
           AttributeDefinition attributeDef = perun.getAttributesManagerBl().getAttributeDefinition(sess, attributeDefinition1.getName());
           AttributeDefinition attributeDefFromCache = perun.getAttributesManagerBl().getCacheManager().getAttributeFromCacheForAttributesInTransaction(attributeDefinition1.getName());
           assertEquals(attributeDef, attributeDefinition1);
           assertEquals(attributeDefFromCache, attributeDefinition1);
         
       } */
      
}
