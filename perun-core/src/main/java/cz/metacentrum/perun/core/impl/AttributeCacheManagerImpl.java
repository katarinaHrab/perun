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
import cz.metacentrum.perun.core.implApi.AttributeCacheManagerImplApi;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    
    
    public synchronized void addToCache(PerunBean primary, PerunBean secondary, Attribute attribute) {
        AttributeHolders attrHolders = new AttributeHolders(primary, secondary);
        if (applicationCache.get(attrHolders)!=null) {
            applicationCache.get(attrHolders).put(attribute.getName(), attribute);
        }
        else {
            Map<String,Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
            mapOfAttributeHoldersAttributes.put(attribute.getName(), attribute);
            applicationCache.put(attrHolders, mapOfAttributeHoldersAttributes);
        }
    }
    
    public void addToCache(PerunBean primary, Attribute attribute) {
        this.addToCache(primary, null, attribute);
    }
    
    public void removeFromCache(PerunBean primary, PerunBean secondary, AttributeDefinition attribute) {
        AttributeHolders attrHolders = new AttributeHolders(primary, secondary);
        if (applicationCache.get(attrHolders)!=null) {
           applicationCache.get(attrHolders).remove(attribute.getName());
        }
    }
    
    public void removeFromCache(PerunBean primary, AttributeDefinition attribute) {
        this.removeFromCache(primary, null, attribute);
    }

    public Attribute getFromCache(PerunBean primary, PerunBean secondary, String attributeName) {
        AttributeHolders attrHolders = new AttributeHolders(primary, secondary);
        if (applicationCache.get(attrHolders)==null) {
            return null;
        }
        Map<String,Attribute> mapOfAttributeHoldersAttributes = new HashMap<>();
        mapOfAttributeHoldersAttributes = applicationCache.get(attrHolders);
        if (mapOfAttributeHoldersAttributes.get(attributeName) == null) {
            return null;
        }
        Attribute attribute = new Attribute(mapOfAttributeHoldersAttributes.get(attributeName));
        return attribute;
    }
    
    public Attribute getFromCache(PerunBean primary, String attributeName) {
        return this.getFromCache(primary, null, attributeName);
    }
}
