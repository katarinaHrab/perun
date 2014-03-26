/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeHolders;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.implApi.AttributeCacheManagerImplApi;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;


/**
 *
 * @author Katarína Hrabovská
 */
public class ApplicationCacheTest extends AbstractPerunIntegrationTest {
        @Autowired
    	protected AttributesManagerImplApi attributesManagerImpl;
        
        private AttributeCacheManagerImpl attributeCacheManagerImpl = new AttributeCacheManagerImpl();

        //World's variables
        private User user1;
        private User user2;
        private User user3;
        private Attribute attribute1;
        private Attribute attribute2;
        private Attribute attribute3;

    public AttributesManagerImplApi getAttributesManagerImplApi() {
        return attributesManagerImpl;
    }

    public void setAttributesManagerImplApi(AttributesManagerImplApi attributesManagerImpl) {
        this.attributesManagerImpl = attributesManagerImpl;
    }

    
        
    @Before
	public void setUp() throws Exception {
            this.setUpWorld();
    }
    
    public void setUpWorld() throws Exception {
   
                //Set users
                user1 = new User();
                user2 = new User();
                user3 = new User();
        
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
}
