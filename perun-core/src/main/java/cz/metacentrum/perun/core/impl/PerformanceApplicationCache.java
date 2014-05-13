/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.implApi.AttributesManagerImplApi;
import java.io.BufferedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author Katarína Hrabovská <katarina.hrabovska1992@gmail.com>
 */
public class PerformanceApplicationCache {
     
     private PerunBl perun;
     private AbstractApplicationContext springCtx;
     private PerunSession perunSession;
     private final static Logger log = LoggerFactory.getLogger(Main.class);
     private final PerunPrincipal pp = new PerunPrincipal("main", ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
     private AttributesManagerImplApi attributesManagerImpl;
     
    
     public PerformanceApplicationCache() throws Exception {
         try {
             this.springCtx = new ClassPathXmlApplicationContext("perun-beans.xml", "perun-datasources.xml", "perun-transaction-manager.xml");
             this.perun = springCtx.getBean("perun", PerunBl.class);
             this.attributesManagerImpl = springCtx.getBean("attributesManagerImpl", AttributesManagerImplApi.class);
             this.perunSession = perun.getPerunSession(pp);
         } catch (Exception e) {
             log.error("Application context loading error.", e);
             throw e;
         }
         int scope = 30;
         AttributeDefinition[] arrayOfAttributes = new AttributeDefinition[scope];
         AttributeDefinition[] arrayOfAttributesFromCache = new AttributeDefinition[scope];
         for (int i=0;i<scope;i++) {
             AttributeDefinition attributeDefinition = new AttributeDefinition();
             attributeDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
             attributeDefinition.setFriendlyName("definition"+i);
             attributeDefinition.setId(i);
             attributeDefinition.setType(String.class.getName());
             createAttribute(attributeDefinition);
             arrayOfAttributes[i] = attributeDefinition;
         }
         for (int z=0; z<10; z++) {
                long startTime = System.currentTimeMillis();
                for (int i=0; i<scope; i++) {
                     AttributeDefinition attributeDefinitionFromCache = getAttribute(arrayOfAttributes[i].getName());
                     arrayOfAttributesFromCache[i] = attributeDefinitionFromCache;
                 //    System.out.println(arrayOfAttributesFromCache[i]);
                }   
                long endTime = System.currentTimeMillis();
                System.out.println("That took " + (endTime - startTime) + " milliseconds");
         }
         for (int i=0; i<scope; i++) {
             deleteAttribute(arrayOfAttributesFromCache[i]);
         }
     }
     
     public static void main(String[] args) throws Exception {
         PerformanceApplicationCache performance = new PerformanceApplicationCache();
         
     }
    
     public void createAttribute(AttributeDefinition attributeDefinition) throws Exception {
         attributeDefinition.setType(String.class.getName());
         attributesManagerImpl.createAttribute(perunSession, attributeDefinition);
     }
     
     public AttributeDefinition getAttribute(String attributeName) throws Exception {
         return attributesManagerImpl.getAttributeDefinition(perunSession, attributeName);
     }
     
     public void deleteAttribute(AttributeDefinition attributeDefinition) throws Exception {
         attributesManagerImpl.deleteAttribute(perunSession, attributeDefinition);
     }
     
     public void executionWithCacheImplementation(AttributeDefinition attributeDefinition) throws Exception {
         createAttribute(attributeDefinition);
         AttributeDefinition attributeDefinitionFromCache = getAttribute(attributeDefinition.getName());
         System.out.println(" " +attributeDefinitionFromCache);
         deleteAttribute(attributeDefinitionFromCache);
     }
     
 }