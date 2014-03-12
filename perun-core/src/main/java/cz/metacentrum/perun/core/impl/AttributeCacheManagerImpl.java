/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeHolders;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.AttributeCacheManagerImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.VirtualAttributesModuleImplApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 *
 * @author Katarína Hrabovská <katarina.hrabovska1992@gmail.com>
 */
public class AttributeCacheManagerImpl implements AttributeCacheManagerImplApi{
    
    private Map<AttributeHolders,Map<String,Attribute>> applicationCache; 
    
    public AttributeCacheManagerImpl() {
        applicationCache = new ConcurrentHashMap<AttributeHolders,Map<String,Attribute>>();
    }
    public Map<AttributeHolders,Map<String,Attribute>> getApplicationCache() {
        return Collections.unmodifiableMap(applicationCache);
    }
    
    public void flushCache() {
        applicationCache.clear();
    }
    
    
    public synchronized void addToCache(AttributeHolders attributeHolders, Attribute attribute) {
        if (applicationCache.get(attributeHolders)!=null) {
            applicationCache.get(attributeHolders).put(attribute.getName(), attribute);
        }
        else {
            Map<String,Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
            mapOfAttributeHoldersAttributes.put(attribute.getName(), attribute);
            applicationCache.put(attributeHolders, mapOfAttributeHoldersAttributes);
        }
    }
    
    public void addToCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, Attribute attribute) {
        AttributeHolders attributeHolders = new AttributeHolders(primaryHolder, secondaryHolder);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if(actionsInTransaction == null) {
                actionsInTransaction = new HashMap<AttributeHolders, Map<String, Attribute>>();
                TransactionSynchronizationManager.bindResource(this, actionsInTransaction);
            }
            if (actionsInTransaction.get(attributeHolders)!=null) {
                actionsInTransaction.get(attributeHolders).put(attribute.getName(), attribute);
            }
            else {
                Map<String, Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
                mapOfAttributeHoldersAttributes.put(attribute.getName(), attribute);
                actionsInTransaction.put(attributeHolders, mapOfAttributeHoldersAttributes);
            }
        } else {
            this.addToCache(attributeHolders, attribute);
        }
    }
    
    public void addToCacheInTransaction(PerunBean primaryHolder, Attribute attribute){
        this.addToCacheInTransaction(primaryHolder, null, attribute);
    }
    
    
    public void removeFromCache(AttributeHolders attributeHolders, AttributeDefinition attribute) {
        if (applicationCache.get(attributeHolders)!=null) {
           applicationCache.get(attributeHolders).remove(attribute.getName());
        }
    }

    public void removeFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, AttributeDefinition attribute) {
        AttributeHolders attributeHolders = new AttributeHolders(primaryHolder, secondaryHolder);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if(actionsInTransaction == null) {
                actionsInTransaction = new HashMap<AttributeHolders, Map<String, Attribute>>();
                TransactionSynchronizationManager.bindResource(this, actionsInTransaction);
            }
            Attribute newAttribute = new Attribute(attribute);
            newAttribute.setValue("remove");
            if (actionsInTransaction.get(attributeHolders)!=null) {
                actionsInTransaction.get(attributeHolders).put(attribute.getName(), newAttribute);
            }
            else {
                Map<String, Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
                mapOfAttributeHoldersAttributes.put(attribute.getName(), newAttribute);
                actionsInTransaction.put(attributeHolders, mapOfAttributeHoldersAttributes);
            }
        } else {
            this.removeFromCache(attributeHolders, attribute);
        }
    }
    
    public void removeFromCacheInTransaction(PerunBean primaryHolder, AttributeDefinition attribute) {
        this.removeFromCacheInTransaction(primaryHolder, null, attribute);
    }
    
    public Attribute getFromCache(PerunBean primaryHolder, PerunBean secondaryHolder, String attributeName) {
        AttributeHolders attributeHolders = new AttributeHolders(primaryHolder, secondaryHolder);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if (actionsInTransaction!=null) {
                if ((actionsInTransaction.get(attributeHolders))!=null) {
                    Attribute attribute = actionsInTransaction.get(attributeHolders).get(attributeName);
                    if (attribute!=null) {
                        return attribute;
                    }
                }
            }
        }
        if (applicationCache.get(attributeHolders)==null) {
            return null;
        }
        Map<String,Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
        mapOfAttributeHoldersAttributes = applicationCache.get(attributeHolders);
        if (mapOfAttributeHoldersAttributes.get(attributeName) == null) {
            return null;
        }
        Attribute attribute = new Attribute(mapOfAttributeHoldersAttributes.get(attributeName));
        return attribute;
    }
    
    public Attribute getFromCache(PerunBean primaryHolder, String attributeName) {
        return this.getFromCache(primaryHolder, null, attributeName);
    }
    
    public void clean() {
        Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.unbindResourceIfPossible(this);
    }
    
    public void flush() {
        Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.unbindResourceIfPossible(this);
        if(actionsInTransaction == null) {
            return;
        }

        for(AttributeHolders attributeHolders: actionsInTransaction.keySet()) {
            Map<String, Attribute> mapOfAttributeHoldersAttributes = actionsInTransaction.get(attributeHolders);
            for(String attributeName: mapOfAttributeHoldersAttributes.keySet()) {
                if (mapOfAttributeHoldersAttributes.get(attributeName).getValue().equals("remove")) {
                    this.removeFromCache(attributeHolders, mapOfAttributeHoldersAttributes.get(attributeName));
                }
                else {
                    this.addToCache(attributeHolders, mapOfAttributeHoldersAttributes.get(attributeName));
                }
            }
        }
    }
}
