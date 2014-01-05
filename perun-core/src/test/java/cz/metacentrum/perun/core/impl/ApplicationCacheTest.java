/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
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
                
                attribute2 = new Attribute();
                attribute2.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                attribute2.setFriendlyName("name2");
                attribute2.setValue(2);
                
                attribute3 = new Attribute();
                attribute3.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                attribute3.setFriendlyName("name3");
                attribute3.setValue(3);
    }
    
    @After
    public void tearDown() {
        attributesManagerImpl.flushCache();
    }
    
    @Test
        public void addAndGetFromCacheTest() {
            System.out.println("attributesManagerImpl.addAndGetFromCacheTest");
            attributesManagerImpl.addToCache(user1, attribute1);
            Attribute attributeFromCache = new Attribute();
            attributeFromCache = attributesManagerImpl.getFromCache(user1, attribute1.getName());
            assertEquals(attribute1, attributeFromCache);
         }
    
    @Test
        public void removeFromCacheTest() {
            System.out.println("attributesManagerImpl.removeFromCacheTest");
            attributesManagerImpl.addToCache(user1, attribute1);
            attributesManagerImpl.removeFromCache(user1, attribute1);
            assertEquals(null, attributesManagerImpl.getFromCache(user1, attribute1.getName()));
    }
    
    @Test
        public void addMoreAttributesToOneUserTest() {
            System.out.println("attributesManagerImpl.addMoreAttributesToOneUserTest");
            attributesManagerImpl.addToCache(user1, attribute1);
            attributesManagerImpl.addToCache(user1, attribute2);
            assertEquals(attribute1, attributesManagerImpl.getFromCache(user1, attribute1.getName()));
            assertEquals(attribute2, attributesManagerImpl.getFromCache(user1, attribute2.getName()));
    }
    
    @Test 
        public void addMoreUsersTest() {
            System.out.println("attributesManagerImpl.addMoreUsersTest");
            attributesManagerImpl.addToCache(user1, attribute1);
            attributesManagerImpl.addToCache(user2, attribute2);
            assertEquals(attribute1, attributesManagerImpl.getFromCache(user1, attribute1.getName()));
            assertEquals(attribute2, attributesManagerImpl.getFromCache(user2, attribute2.getName()));
    }
    
    @Test
        public void consistencyOfObjectTest() {
            System.out.println("attributesManagerImpl.consistencyOfObjectTest");
            attributesManagerImpl.addToCache(user1, attribute1);
            attributesManagerImpl.getFromCache(user1, attribute1.getName()).setFriendlyName("name2");
            String attributeName = attributesManagerImpl.getFromCache(user1, attribute1.getName()).getFriendlyName();
            assertEquals(attribute1.getFriendlyName(), attributeName);
    }
    
    @Test
        public void updateOfAttributeWithDiffValue() {
            System.out.println("attributesManagerImpl.updateOfAttributeWithDiffValue");
            attributesManagerImpl.addToCache(user1, attribute1);
            attribute1.setValue(123);
            attributesManagerImpl.addToCache(user1, attribute1);
            assertEquals(123, attributesManagerImpl.getFromCache(user1, attribute1.getName()).getValue());
            assertEquals(1, attributesManagerImpl.getCacheByUserAndName().get(user1).size());
    }
}
