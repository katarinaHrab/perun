/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributeHolders;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.User;
import java.util.Map;

/**
 *
 * @author Katarína Hrabovská <katarina.hrabovska1992@gmail.com> 
 */
public interface AttributeCacheManagerImplApi {
    
   
  /**
   * Get map with all attributeHolders and their attributes stored in cache.
   * 
   * @author Katarina Hrabovska
   * @param null
   * @return map of attributeHolders and attributes
   */
    Map<AttributeHolders,Map<String,Attribute>> getApplicationCache();
    
  /**
   * Remove all attributeHolders and attributes from cache.
   * 
   * @author Katarina Hrabovska
   */
    void flushCache();
    
  /**
   * Add attribute to attributeHolders in cache. If attributeHolders is not in cache, it will be created.
   * 
   * @author Katarina Hrabovska
   * @param attributeHolders couple of entities to add attribute to
   * @param attribute attribute of attributeHolders
   */
    void addToCache(AttributeHolders attributeHolders, Attribute attribute);
   
   /**
    * Add attribute of attributeHolders to transaction.
    * Retrieve actions for an actual transaction and store their to map. New attribute is added there.
    * If an actual transaction is not active, it will be called method addToCache.
    * 
    * @author Katarina Hrabovska
    * @param primaryHolder primary entity of object AttributeHolders
    * @param secondaryHolder secondary entity of object AttributeHolders, can be null
    * @param attribute attribute of object AttributeHolders
    */
    void addToCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, Attribute attribute);
    
   /**
    * Add attribute of attributeHolders to transaction.
    * Call method addToCacheInTransaction for AttributeHolders, which secondaryHolder is null in.
    * 
    * @author Katarina Hrabovska
    * @param primaryHolder primary entity of object AttributeHolders
    * @param attribute attribute of object AttributeHolders
    */
    void addToCacheInTransaction(PerunBean primaryHolder, Attribute attribute);
    
    
  /**
   * Remove attribute of attributeHolders from cache. If attribute or attributeHolders are not in cache, nothing happen.
   * 
   * @author Katarina Hrabovska
   * @param attributeHolders couple of entities to remove attribute of,
   * @param attribute attribute of attributeHolders
   */ 
    void removeFromCache(AttributeHolders attributeHolders, AttributeDefinition attribute);

   /**
    * Add attribute of attributeHolders to transaction, until removing from DB.
    * Retrieve actions for an actual transaction and store their to map. Attribute is added there.
    * If an actual transaction is not active, it will be called method removeFromCache.
    * 
    * @author Katarina Hrabovska
    * @param primaryHolder primary entity of object AttributeHolders
    * @param secondaryHolder secondary entity of object AttributeHolders, can be null
    * @param attribute attribute of object AttributeHolders
    */
    void removeFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, AttributeDefinition attribute);
    
   /**
    * Add attribute of attributeHolders to transaction, until removing from DB.
    * Call method removeFromCacheInTransaction for AttributeHolders, which secondaryHolder is null in.
    * 
    * @author Katarina Hrabovska
    * @param primaryHolder primary entity of object AttributeHolders
    * @param attribute attribute of object AttributeHolders
    */
    void removeFromCacheInTransaction(PerunBean primaryHolder, AttributeDefinition attribute);
    
  /**
   * Get attribute of attributeHolders from cache.
   * 
   * @author Katarina Hrabovska
   * @param attributeHolders couple of entities to get attribute of,
   * @param attributeName name of attribute
   * @return attribute, null if attributeHolders or attribute is not in cache
   */ 
    Attribute getFromCache(AttributeHolders attributeHolders, String attributeName);
    
   /**
    * Get attribute of attributeHolders from transaction.
    * Retrieve actions for an actual transaction and store their to map. Attribute is looked for there.
    * If an actual transaction is not active or attribute is not in, it will be called method getFromCache.
    * 
    * @param primaryHolder primary entity of object AttributeHolders
    * @param secondaryHolder secondary entity of object AttributeHolders, can be null
    * @param attributeName name of attribute
    * @return attribute, null if attributeHolders or attribute is not in transaction or cache
    */
    Attribute getFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, String attributeName);
    
    
   /**
    * Get attribute of attributeHolder from transaction.
    * Call method getFromCacheInTransaction for AttributeHolders, which secondaryHolder is null in.
    * 
    * @author Katarina Hrabovska
    * @param primary attributeHolder to get attribute of
    * @param attributeName name of attribute
    * @return attribute, null if attributeHolder or attribute is not in transaction or cache
    */
    Attribute getFromCacheInTransaction(PerunBean primary, String attributeName);
    
   /**
    * Unbind transaction resource.
    */
    void clean();
   
   /**
    * Commit actions from transaction to cache. For attributes with null value is called method removeFromCache and for others method addToCache.
    * Flush transaction's map.
    */
    void flush();
}
