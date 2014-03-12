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
   * Add attribute to attributeHolders in cache.
   * 
   * @author Katarina Hrabovska
   * @param attributeHolders couple of entities to add attribute to
   * @param attribute attribute of attributeHolders
   */
    void addToCache(AttributeHolders attributeHolders, Attribute attribute);
   
   /**
    * Add attribute of attributeHolders to transaction.
    * 
    * @author Katarina Hrabovska
    * @param primaryHolder primary entity of object AttributeHolders
    * @param secondaryHolder secondary entity of object AttributeHolders, can be null
    * @param attribute attribute of object AttributeHolders
    */
    void addToCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, Attribute attribute);
    
   /**
    * Add attribute of attributeHolders to transaction.
    * 
    * @author Katarina Hrabovska
    * @param primaryHolder primary entity of object AttributeHolders
    * @param attribute attribute of object AttributeHolders
    */
    void addToCacheInTransaction(PerunBean primaryHolder, Attribute attribute);
    
    
  /**
   * Remove attribute of attributeHolders from cache.
   * 
   * @author Katarina Hrabovska
   * @param attributeHolders couple of entities to remove attribute of,
   * @param attribute attribute of attributeHolders
   */ 
    void removeFromCache(AttributeHolders attributeHolders, AttributeDefinition attribute);

   /**
    * Add attribute of attributeHolders to transaction, while removing from DB.
    * 
    * @author Katarina Hrabovska
    * @param primaryHolder primary entity of object AttributeHolders
    * @param secondaryHolder secondary entity of object AttributeHolders, can be null
    * @param attribute attribute of object AttributeHolders
    */
    void removeFromCacheInTransaction(PerunBean primaryHolder, PerunBean secondaryHolder, AttributeDefinition attribute);
    
   /**
    * Add attribute of attributeHolders to transaction, while removing from DB.
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
   * @param primary 
   * @param secondary couple of attributeHolders to get attribute of,
   *                  secondary can be null
   * @param attributeName name of attribute
   * @return attribute, null if attributeHolders or attribute is not in cache
   */ 
    Attribute getFromCache(PerunBean primary, PerunBean secondary, String attributeName);
    
   /**
    * Get attribute of attributeHolder from cache.
    * 
    * @author Katarina Hrabovska
    * @param primary attributeHolder to get attribute of
    * @param attributeName name of attribute
    * @return attribute, null if attributeHolder or attribute is not in cache
    */
    Attribute getFromCache(PerunBean primary, String attributeName);
    
   /**
    * Unbind transaction resource.
    */
    void clean();
   
   /**
    * Commit actions from transaction to cache.
    */
    void flush();
}