/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeHolders;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.User;
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
    
    
    public synchronized void addAttributeToCache(AttributeHolders attributeHolders, Attribute attribute) {
        if (applicationCache.get(attributeHolders)!=null) {
            applicationCache.get(attributeHolders).put(attribute.getName(), attribute);
        }
        else {
            Map<String,Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
            mapOfAttributeHoldersAttributes.put(attribute.getName(), attribute);
            applicationCache.put(attributeHolders, mapOfAttributeHoldersAttributes);
        }
    }
    
    public void addAttributeToCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, Attribute attribute) {
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
            this.addAttributeToCache(attributeHolders, attribute);
        }
    }
    
    public void addAttributeToCacheInTransaction(PerunBean primaryHolder, Attribute attribute){
        this.addAttributeToCacheInTransaction(primaryHolder, null, attribute);
    }
    
    
    public void removeAttributeFromCache(AttributeHolders attributeHolders, AttributeDefinition attribute) {
        if (applicationCache.get(attributeHolders)!=null) {
           applicationCache.get(attributeHolders).remove(attribute.getName());
        }
    }

    public void removeAttributeFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, AttributeDefinition attribute) {
        AttributeHolders attributeHolders = new AttributeHolders(primaryHolder, secondaryHolder);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if(actionsInTransaction == null) {
                actionsInTransaction = new HashMap<AttributeHolders, Map<String, Attribute>>();
                TransactionSynchronizationManager.bindResource(this, actionsInTransaction);
            }
            Attribute newAttribute = new Attribute(attribute);
            if (actionsInTransaction.get(attributeHolders)!=null) {
                actionsInTransaction.get(attributeHolders).put(attribute.getName(), newAttribute);
            }
            else {
                Map<String, Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
                mapOfAttributeHoldersAttributes.put(attribute.getName(), newAttribute);
                actionsInTransaction.put(attributeHolders, mapOfAttributeHoldersAttributes);
            }
        } else {
            this.removeAttributeFromCache(attributeHolders, attribute);
        }
    }
    
    public void removeAttributeFromCacheInTransaction(PerunBean primaryHolder, AttributeDefinition attribute) {
        this.removeAttributeFromCacheInTransaction(primaryHolder, null, attribute);
    }
    
    public void removeAllAttributesFromCache(AttributeHolders attributeHolders) {
        if (applicationCache.get(attributeHolders)!=null) {
           applicationCache.get(attributeHolders).clear();
        }
    }
    
    public void removeAllAttributesFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder) {
        AttributeHolders attributeHolders = new AttributeHolders(primaryHolder, secondaryHolder);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if(actionsInTransaction == null) {
                actionsInTransaction = new HashMap<AttributeHolders, Map<String, Attribute>>();
                TransactionSynchronizationManager.bindResource(this, actionsInTransaction);
            }
            List<Attribute> allAttributesOfAttributeHoldersInCache = new ArrayList<>();
            allAttributesOfAttributeHoldersInCache.addAll(this.getAllAttributesFromCache(attributeHolders));
            if (actionsInTransaction.get(attributeHolders)!=null) {
                for (Attribute attribute: allAttributesOfAttributeHoldersInCache) {
                    attribute.setValue(null);
                    actionsInTransaction.get(attributeHolders).put(attribute.getName(), attribute);
                }
            }
            else {
                Map<String, Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
                for (Attribute attribute: allAttributesOfAttributeHoldersInCache) {
                    attribute.setValue(null);
                    mapOfAttributeHoldersAttributes.put(attribute.getName(), attribute);
                }
                actionsInTransaction.put(attributeHolders, mapOfAttributeHoldersAttributes);
            }
        } else {
            this.removeAllAttributesFromCache(attributeHolders);
        }
    }
    
    public void removeAllAttributesFromCacheInTransaction(PerunBean primaryHolder) {
        this.removeAllAttributesFromCacheInTransaction(primaryHolder, null);
    }
    
    public void removeAllUserFacilityAttributesForAnyUserFromCache(PerunBean secondaryHolder) { 
        for (AttributeHolders attributeHolders: applicationCache.keySet()) {
            if (attributeHolders.getSecondary()!=null) {
                if ((attributeHolders.getSecondary().equals(secondaryHolder)) && (attributeHolders.getPrimary() instanceof User)) {
                    this.removeAllAttributesFromCache(attributeHolders);
                }
            }
         }
    }
    
    public void removeAllUserFacilityAttributesForAnyUserFromCacheInTransaction(PerunBean secondaryHolder) {
            if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if(actionsInTransaction == null) {
                actionsInTransaction = new HashMap<AttributeHolders, Map<String, Attribute>>();
                TransactionSynchronizationManager.bindResource(this, actionsInTransaction);
            }
            for (AttributeHolders attributeHolders: applicationCache.keySet()) {
                if (attributeHolders.getSecondary()!=null) {
                    if ((attributeHolders.getSecondary().equals(secondaryHolder)) && (attributeHolders.getPrimary() instanceof User)) {
                            if (actionsInTransaction.get(attributeHolders)!=null) {
                                for (Attribute attribute: applicationCache.get(attributeHolders).values()) {
                                attribute.setValue(null);
                                actionsInTransaction.get(attributeHolders).put(attribute.getName(), attribute);
                                }
                            }
                            else {
                                Map<String, Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
                                for (Attribute attribute: applicationCache.get(attributeHolders).values()) {
                                    attribute.setValue(null);
                                    mapOfAttributeHoldersAttributes.put(attribute.getName(), attribute);
                                }
                                actionsInTransaction.put(attributeHolders, mapOfAttributeHoldersAttributes);
                            }
                    }
                }
            }
        } else {
            this.removeAllUserFacilityAttributesForAnyUserFromCache(secondaryHolder);
        }
    }
    
    public void removeAllUserFacilityAttributesFromCache(PerunBean primaryHolder) {
        for (AttributeHolders attributeHolders: applicationCache.keySet()) {
            if (attributeHolders.getSecondary()!=null) {
                if ((attributeHolders.getPrimary().equals(primaryHolder)) && (attributeHolders.getSecondary() instanceof Facility)) {
                    this.removeAllAttributesFromCache(attributeHolders);
                }
            }
         }
    }
    
    public void removeAllUserFacilityAttributesFromCacheInTransaction(PerunBean primaryHolder) {
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if(actionsInTransaction == null) {
                actionsInTransaction = new HashMap<AttributeHolders, Map<String, Attribute>>();
                TransactionSynchronizationManager.bindResource(this, actionsInTransaction);
            }
            for (AttributeHolders attributeHolders: applicationCache.keySet()) {
                if (attributeHolders.getSecondary()!=null) {
                    if ((attributeHolders.getPrimary().equals(primaryHolder)) && (attributeHolders.getSecondary() instanceof Facility)) {
                            if (actionsInTransaction.get(attributeHolders)!=null) {
                                for (Attribute attribute: applicationCache.get(attributeHolders).values()) {
                                attribute.setValue(null);
                                actionsInTransaction.get(attributeHolders).put(attribute.getName(), attribute);
                                }
                            }
                            else {
                                Map<String, Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
                                for (Attribute attribute: applicationCache.get(attributeHolders).values()) {
                                    attribute.setValue(null);
                                    mapOfAttributeHoldersAttributes.put(attribute.getName(), attribute);
                                }
                                actionsInTransaction.put(attributeHolders, mapOfAttributeHoldersAttributes);
                            }
                    }
                }
            }
        } else {
            this.removeAllUserFacilityAttributesFromCache(primaryHolder);
        }
    }
    
    
    public Attribute getAttributeFromCache(AttributeHolders attributeHolders, String attributeName) {
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
    
    public Attribute getAttributeFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, String attributeName) {
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
            return this.getAttributeFromCache(attributeHolders, attributeName);
    }
    
    public Attribute getAttributeFromCacheInTransaction(PerunBean primaryHolder, String attributeName) {
        return this.getAttributeFromCacheInTransaction(primaryHolder, null, attributeName);
    }
    
    public List<Attribute> getAllAttributesFromCache(AttributeHolders attributeHolders) {
        List<Attribute> listOfAttributes = new ArrayList<>();
        if (applicationCache.get(attributeHolders)!=null) { 
            listOfAttributes.addAll(applicationCache.get(attributeHolders).values());
        }
        return listOfAttributes;
    }
    
    public List<Attribute> getAllAttributesFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder) {
        List<Attribute> listOfAttributes = new ArrayList<>();
        List<Attribute> listOfAttributesFromTransaction = new ArrayList<>();
        AttributeHolders attributeHolders = new AttributeHolders(primaryHolder, secondaryHolder);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if (actionsInTransaction!=null) {
                if ((actionsInTransaction.get(attributeHolders))!=null) {
                    listOfAttributesFromTransaction.addAll(actionsInTransaction.get(attributeHolders).values());
                }
            }
        }
        listOfAttributes.addAll(this.getAllAttributesFromCache(attributeHolders));
        for (Attribute attribute: listOfAttributesFromTransaction) {
            if (!listOfAttributes.contains(attribute)) {
                listOfAttributes.add(attribute);
            }
        }
        return listOfAttributes;
    }
    
    public List<Attribute> getAllAttributesFromCacheInTransaction(PerunBean primaryHolder) {
        return this.getAllAttributesFromCacheInTransaction(primaryHolder, null);
    }
    
    public Attribute getAttributeByIdFromCache(AttributeHolders attributeHolders, int id) {
        if (applicationCache.get(attributeHolders)==null) {
            return null;
        }
        Map<String,Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
        mapOfAttributeHoldersAttributes = applicationCache.get(attributeHolders);
        List<Attribute> attributes = new ArrayList<>();
        attributes.addAll(mapOfAttributeHoldersAttributes.values());
        for (Attribute attributeFromList: attributes) {
            if (attributeFromList.getId()==id) {
                return new Attribute(attributeFromList);
            }
        }
        return null;
    }
    
    public Attribute getAttributeByIdFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, int id) {
        AttributeHolders attributeHolders = new AttributeHolders(primaryHolder, secondaryHolder);
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            Map<AttributeHolders, Map<String, Attribute>> actionsInTransaction = (Map<AttributeHolders, Map<String, Attribute>>) TransactionSynchronizationManager.getResource(this);
            if (actionsInTransaction!=null) {
                if ((actionsInTransaction.get(attributeHolders))!=null) {
                    List<Attribute> attributes = new ArrayList<>();
                    attributes.addAll(actionsInTransaction.get(attributeHolders).values());
                    for (Attribute attributeFromList: attributes) {
                        if (attributeFromList.getId()==id) {
                            return new Attribute(attributeFromList);
                        }
                    }
                }
            }
        }
            return this.getAttributeByIdFromCache(attributeHolders, id);
    }
    
    public Attribute getAttributeByIdFromCacheInTransaction(PerunBean primaryHolder, int id) {
         return this.getAttributeByIdFromCacheInTransaction(primaryHolder, null, id);
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
                if (mapOfAttributeHoldersAttributes.get(attributeName).getValue()==null) {
                    this.removeAttributeFromCache(attributeHolders, mapOfAttributeHoldersAttributes.get(attributeName));
                }
                else {
                    this.addAttributeToCache(attributeHolders, mapOfAttributeHoldersAttributes.get(attributeName));
                }
            }
        }
    }
}
