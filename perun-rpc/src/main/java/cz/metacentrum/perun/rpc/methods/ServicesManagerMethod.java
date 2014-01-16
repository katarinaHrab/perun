package cz.metacentrum.perun.rpc.methods;

import java.util.ArrayList;
import java.util.List;


import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServiceAttributes;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum ServicesManagerMethod implements ManagerMethod {

	/*#
	 * Creates a new service.
	 * 
	 * @param service Service JSON object
	 * @param owner int Owner ID
	 * @return Service Created Service
	 */
    createService {

        @Override
        public Service call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            return ac.getServicesManager().createService(ac.getSession(),
                    parms.read("service", Service.class),
                    ac.getOwnerById(parms.readInt("owner")));
        }
    },
    
    /*#
	 * Deletes a service.
	 * 
	 * @param service int Service ID
	 */
    deleteService {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().deleteService(ac.getSession(),
                    ac.getServiceById(parms.readInt("service")));
            return null;
        }
    },
    
    /*#
	 * Updates a service.
	 * 
	 * @param service Service JSON object
	 * @return Service Updated Service
	 */
    updateService {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().updateService(ac.getSession(),
                    parms.read("service", Service.class));
            return null;
        }
    },
    
    /*#
	 * Returns a service by its ID.
	 * 
	 * @param id int Service ID
	 * @return Service Found Service
	 */
    getServiceById {

        @Override
        public Service call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServiceById(parms.readInt("id"));
        }
    },
    
    /*#
	 * Returns a service by its name.
	 * 
	 * @param name String Service Name
	 * @return Service Found Service
	 */
    getServiceByName {

        @Override
        public Service call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServicesManager().getServiceByName(ac.getSession(),
                    parms.readString("name"));
        }
    },
    
    /*#
     * Returns all services.
     * 
     * @return List<Service> All services
     */
    getServices {

        @Override
        public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServicesManager().getServices(ac.getSession());
        }
    },
    
    /*#
     * Get all services with given attribute.
     * 
     * @return all services with given attribute
     */
    getServicesByAttributeDefinition {
        
        @Override
        public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServicesManager().getServicesByAttributeDefinition(ac.getSession(),
                    ac.getAttributeDefinitionById(parms.readInt("attributeDefinition")));
        }
    },
    
    /*#
     * Generates the list of attributes per each member associated with the resource. 
     * 
     * @param service int Service ID
     * @param facility int Facility ID. You will get attributes for this facility, resources asociated with it and members assigned to the resources.
     * @return List<ServiceAttributes> Attributes in special structure. Facility is in the root, facility childrens are resources. And resource childrens are members.
  <pre> 
    Facility 
      +---Attrs 
      +---ChildNodes
             +------Resource
             |      +---Attrs 
             |      +---ChildNodes
             |             +------Member
             |             |        +-------Attrs
             |             +------Member
             |             |        +-------Attrs
             |             +...
             |
             +------Resource
             |      +---Attrs 
             |      +---ChildNodes
             .             +------Member
             .             |        +-------Attrs
             .             +------Member
                           |        +-------Attrs
                           +...
  </pre>
     * 
     */
    getHierarchicalData {

      @Override
      public ServiceAttributes call(ApiCaller ac, Deserializer parms) throws PerunException {
          return ac.getServicesManager().getHierarchicalData(ac.getSession(),
                  ac.getServiceById(parms.readInt("service")),
                  ac.getFacilityById(parms.readInt("facility")));
      }
    },
    
    /*#
     * Generates the list of attributes per each user and per each resource. Never return member or member-resource attribute.
     * 
     * @param service int Service ID. You will get attributes required by this service
     * @param facility int Facility ID. You will get attributes for this facility, resources asociated with it and members assigned to the resources
     * @return ServiceAttributes Attributes in special structure. The facility is in the root. Facility first children is abstract node which contains no attributes and it's childrens are all resources. Facility second child is abstract node with no attribute and it's childrens are all users.
  <pre> 
    Facility 
      +---Attrs 
      +---ChildNodes
             +------()
             |      +---ChildNodes
             |             +------Resource
             |             |        +-------Attrs
             |             +------Resource
             |             |        +-------Attrs
             |             +...
             |
             +------()
                    +---ChildNodes
                           +------User
                           |        +-------Attrs (do NOT return member, member-resource attributes)
                           +------User 
                           |        +-------Attrs (do NOT return member, member-resource attributes)
                           +...
  </pre>
                   
     *
     */
    getFlatData {

      @Override
      public ServiceAttributes call(ApiCaller ac, Deserializer parms) throws PerunException {
          return ac.getServicesManager().getFlatData(ac.getSession(),
                ac.getServiceById(parms.readInt("service")),
                ac.getFacilityById(parms.readInt("facility")));
      }
    },
    
    /*#
     * Generates the list of attributes per each member associated with the resources and groups.
     * 
     * @param service int Service ID. You will get attributes reuqired by this service
     * @param facility int Facility ID. You will get attributes for this facility, resources asociated with it and members assigned to the resources
     * @return ServiceAttributes Attributes in special structure. Facility is in the root, facility children are resources. 
     *         Resource first chil is abstract structure which children are groups.
     *         Resource  second chi is abstract structure which children are members.
     *         Group first chil is abstract structure which children are groups.
     *         Group second chi is abstract structure which children are members.
  <pre> 
    Facility                          
      +---Attrs                              ...................................................
      +---ChildNodes                         |                                                 .
             +------Resource                 |                                                 .
             |       +---Attrs
             |       +---ChildNodes          |                                                 .
             |              +------()        V                                                 .
             |              |       +------Group                                               .
             |              |       |        +-------Attrs                                     .
             |              |       |        +-------ChildNodes                                .
             |              |       |                   +-------()                             .
             |              |       |                   |        +---ChildNodes                .
             |              |       |                   |               +------- GROUP (same structure as any other group)
             |              |       |                   |               +------- GROUP (same structure as any other group)
             |              |       |                   |               +...
             |              |       |                   +-------()
             |              |       |                            +---ChildNodes
             |              |       |                                   +------Member
             |              |       |                                   |        +----Attrs
             |              |       |                                   +------Member
             |              |       |                                   |        +----Attrs
             |              |       |                                   +...
             |              |       |
             |              |       +------Group
             |              |       |        +-------Attrs
             |              |       |        +-------ChildNodes
             |              |       |                   +-------()
             |              |       |                   |        +---ChildNodes
             |              |       |                   |               +------- GROUP (same structure as any other group)
             |              |       |                   |               +------- GROUP (same structure as any other group)
             |              |       |                   |               +...
             |              |       |                   +-------()
             |              |       |                            +---ChildNodes
             |              |       |                                   +------Member
             |              |       |                                   |        +----Attrs
             |              |       |                                   +------Member
             |              |       |                                   |        +----Attrs
             |              |       |                                   +...
             |              |       |
             |              |       +...
             |              |
             |              +------()
             |                      +------Member
             |                      |         +----Attrs   
             |                      |
             |                      +------Member
             |                      |         +----Attrs   
             |                      +...
             |
             +------Resource
             |       +---Attrs
             |       +---ChildNodes
             |              +------()
             |              |       +...
             |              |       +...
             |              |
             |              +------()
             |                      +...
             .                      +...
             .
             .
  </pre>
     * 
     */
    getDataWithGroups {

      @Override
      public ServiceAttributes call(ApiCaller ac, Deserializer parms) throws PerunException {
          return ac.getServicesManager().getDataWithGroups(ac.getSession(),
                  ac.getServiceById(parms.readInt("service")),
                  ac.getFacilityById(parms.readInt("facility")));
      }
    },
    
    /*#
     * Returns packages.
     * 
     * @return List<ServicesPackage> Packages.
     */
    getServicesPackages {

        @Override
        public List<ServicesPackage> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServicesManager().getServicesPackages(ac.getSession());
        }
    },
    
    /*#
     * Gets package by ID.
     * 
     * @param servicesPackageId int ServicesPackage ID.
     * @return ServicesPackage Found ServicesPackage
     */
    getServicesPackageById {

        @Override
        public ServicesPackage call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServicesPackageById(parms.readInt("servicesPackageId"));
        }
    },
    
    /*#
     * Gets package by name.
     * 
     * @param name String ServicesPackage name.
     * @return ServicesPackage Found ServicesPackage
     */
    getServicesPackageByName {

        @Override
        public ServicesPackage call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServicesManager().getServicesPackageByName(ac.getSession(),
                     parms.readString("name"));
        }
    },
    
    /*#
     * Creates a new services package.
     * 
     * @param servicesPackage ServicesPackage JSON object.
     * @return ServicesPackage Created ServicesPackage 
     */
    createServicesPackage {

        @Override
        public ServicesPackage call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            return ac.getServicesManager().createServicesPackage(ac.getSession(),
                    parms.read("servicesPackage", ServicesPackage.class));
        }
    },
    
    /*#
     * Deletes a services package.
     * 
     * @param servicesPackage int ServicesPackage ID
     */
    deleteServicesPackage {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().deleteServicesPackage(ac.getSession(),
                    ac.getServicesPackageById(parms.readInt("servicesPackage")));
            return null;
        }
    },
    
    /*#
     * Updates a service package.
     * 
     * @param servicesPackage ServicesPackage JSON object. 
     */
    updateServicesPackage {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().updateServicesPackage(ac.getSession(),
                    parms.read("servicesPackage", ServicesPackage.class));
            return null;
        }
    },
    
    /*#
     * Adds a Service to a Services Package.
     * 
     * @param servicesPackage int Services package ID to which the service supposed to be added
     * @param service int Service ID to be added to the services package
     */
    addServiceToServicesPackage {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().addServiceToServicesPackage(ac.getSession(),
                    ac.getServicesPackageById(parms.readInt("servicesPackage")),
                    ac.getServiceById(parms.readInt("service")));
            return null;
        }
    },
    
    /*#
     * Removes a Service from a Services Package.
     * 
     * @param servicesPackage int Services package ID from which the service supposed to be removed
     * @param service int Service ID that will be removed from the services package
     */
    removeServiceFromServicesPackage {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().removeServiceFromServicesPackage(ac.getSession(),
                    ac.getServicesPackageById(parms.readInt("servicesPackage")),
                    ac.getServiceById(parms.readInt("service")));
            return null;
        }
    },
    
    /*#
     * Lists services stored in a package.
     * 
     * @param servicesPackage int ServicesPackage ID
     * @return List<Service> List of services
     */
    getServicesFromServicesPackage {

        @Override
        public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {
            return ac.getServicesManager().getServicesFromServicesPackage(ac.getSession(),
                    ac.getServicesPackageById(parms.readInt("servicesPackage")));
        }
    },
    
    /*#
     * Mark the attribute as required for the service. Required attribues are requisite for Service to run.
     * If you add attribute which has a default attribute then this default attribute will be automatically add too.
     *
     * @param service int Service ID
     * @param attribute int Attribute ID
     */
    addRequiredAttribute {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().addRequiredAttribute(ac.getSession(),
                    ac.getServiceById(parms.readInt("service")),
                    ac.getAttributeDefinitionById(parms.readInt("attribute")));
            return null;
        }
    },
    
    /*#
     * Batch version of addRequiredAttribute.
     * 
     * @param service int Service ID
     * @param attributes int[] Attribute IDs
     */
    addRequiredAttributes {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            int[] ids = parms.readArrayOfInts("attributes");
            List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>(ids.length);

            for (int i : ids) {
                attributes.add(ac.getAttributeDefinitionById(i));
            }

            ac.getServicesManager().addRequiredAttributes(ac.getSession(),
                    ac.getServiceById(parms.readInt("service")),
                    attributes);
            return null;
        }
    },
    
    /*#
     * Remove required attribute from service. 
     * @param service int Service ID
     * @param attribute int Attribute ID
     */
    removeRequiredAttribute {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().removeRequiredAttribute(ac.getSession(),
                    ac.getServiceById(parms.readInt("service")),
                    ac.getAttributeDefinitionById(parms.readInt("attribute")));
            return null;
        }
    },
    
    /*#
     * Remove required attributes from service. 
     * @param service int Service ID
     * @param attributes int[] Attribute IDs
     */
    removeRequiredAttributes {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            int[] ids = parms.readArrayOfInts("attributes");
            List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>(ids.length);

            for (int i : ids) {
                attributes.add(ac.getAttributeDefinitionById(i));
            }

            ac.getServicesManager().removeRequiredAttributes(ac.getSession(),
                    ac.getServiceById(parms.readInt("service")),
                    attributes);
            return null;
        }
    },
    
    /*#
     * Remove all required attributes from service. 
     * @param service int Service ID
     */
    removeAllRequiredAttributes {

        @Override
        public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
            ac.stateChangingCheck();

            ac.getServicesManager().removeAllRequiredAttributes(ac.getSession(),
                    ac.getServiceById(parms.readInt("service")));
            return null;
        }
    },
    
    /*#
     * Returns a destination by its ID.
     * 
     * @param id int Destination ID
     * @return Destination Found Destination
     */
    getDestinationById {

      @Override
      public Destination call(ApiCaller ac, Deserializer parms) throws PerunException {
        return ac.getServicesManager().getDestinationById(ac.getSession(),            
               parms.readInt("id"));
      }
    },
    
    /*#
     * Returns list of all destinations defined for the service and facility.
     * 
     * @param service int Service ID
     * @param facility int Facility ID
     * @return List<Destination> Found Destinations
     */
    getDestinations {

      @Override
      public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {
        return ac.getServicesManager().getDestinations(ac.getSession(),            
            ac.getServiceById(parms.readInt("service")),
            ac.getFacilityById(parms.readInt("facility")));
      }
    },
    
    /*#
     * Returns all rich destinations defined for a facility.
     * 
     * @param facility int Facility ID
     * @return List<RichDestination> Found RichDestinations
     */
    /*#
     * Returns all rich destinations defined for a service.
     * 
     * @param service int Service ID
     * @return List<RichDestination> Found RichDestinations
     */
    getAllRichDestinations {
      public List<RichDestination> call(ApiCaller ac, Deserializer parms) throws PerunException {
        if (parms.contains("facility")) {
        return ac.getServicesManager().getAllRichDestinations(ac.getSession(),
            ac.getFacilityById(parms.readInt("facility")));
        }else{
         return ac.getServicesManager().getAllRichDestinations(ac.getSession(),
             ac.getServiceById(parms.readInt("service")));   
        }
      }  
    },
    
    /*#
     * Returns list of all rich destinations defined for the service and facility.
     * 
     * @param service int Service ID
     * @param facility int Facility ID
     * @return List<RichDestination> Found RichDestination
     */
    getRichDestinations {
      public List<RichDestination> call(ApiCaller ac, Deserializer parms) throws PerunException {
        return ac.getServicesManager().getRichDestinations(ac.getSession(), 
            ac.getFacilityById(parms.readInt("facility")),         
            ac.getServiceById(parms.readInt("service")));
      }  
    },
    
    /*#
     * Adds an destination for a facility and service. Destination.id doesn't need to be filled. If destination doesn't exist it will be created.
     * 
     * @param service int Service ID
     * @param facility int Facility ID
     * @param destination String Destination
     * @param type String Type
     * @return Destination Created destination.
     */
    /*#
     * Adds an destination for a facility and list of services. Destination.id doesn't need to be filled. If destination doesn't exist it will be created.
     *
     * @param services List<Service> Services
     * @param facility int Facility ID
     * @param destination String Destination
     * @param type String Type
     * @return Destination Created destination.
     */
    addDestination {

      @Override
      public Destination call(ApiCaller ac, Deserializer parms) throws PerunException {
        ac.stateChangingCheck();
        
        if(parms.contains("services")) {
            return ac.getServicesManager().addDestination(ac.getSession(),
                parms.readList("services", Service.class),
                ac.getFacilityById(parms.readInt("facility")),
                ac.getDestination(parms.readString("destination"), parms.readString("type")));
        } else {
            return ac.getServicesManager().addDestination(ac.getSession(),
                ac.getServiceById(parms.readInt("service")),
                ac.getFacilityById(parms.readInt("facility")),
                ac.getDestination(parms.readString("destination"), parms.readString("type")));
        }

        
      }
    },
    
    /*#
     * Adds destination for all services defined on the facility.
     * 
     * @param facility int Facility ID
     * @param destination String Destination
     * @param type String Type
     * @return List<Destinations> Added destinations
     */
    addDestinationsForAllServicesOnFacility {

      @Override
      public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {
        ac.stateChangingCheck();

        return ac.getServicesManager().addDestinationsForAllServicesOnFacility(ac.getSession(),
            ac.getFacilityById(parms.readInt("facility")),
            ac.getDestination(parms.readString("destination"), parms.readString("type")));
      }
    },
    
    /*#
     * Defines service destination for all cluster hosts using theirs hostnames.
     * 
     * @param service int Service ID
     * @param facility int Facility ID
     * @return List<Destinations> Added destinations
     */
    addDestinationsDefinedByHostsOnCluster {

      @Override
      public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {
        ac.stateChangingCheck();

        return ac.getServicesManager().addDestinationsDefinedByHostsOnCluster(ac.getSession(),
            ac.getServiceById(parms.readInt("service")),
            ac.getFacilityById(parms.readInt("facility")));
      }
    },
    
    /*#
     * Add services destinations for all services currently available on facility
     * (assigned to all facility's resources). Destinations names are taken from
     * all facility's host hostnames.
     * 
     * @param service int Service ID
     * @param facility int Facility ID
     * @return List<Destinations> Added destinations
     */
    /*#
     * Add services destinations for list of services. Destinations names are taken from
     * all facility's host hostnames.
     *
     * @param services List<Service> Services
     * @param facility int Facility ID
     * @return List<Destinations> Added destinations
     */
    /*#
     * Add services destinations for one service. Destinations names are taken from
     * all facility's host hostnames.
     *
     * @param service int Service ID
     * @param facility int Facility ID
     * @return List<Destinations> Added destinations
     */
    addDestinationsDefinedByHostsOnFacility {
        
      @Override
      public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {
        ac.stateChangingCheck();

        if(parms.contains("service")) {
            return ac.getServicesManager().addDestinationsDefinedByHostsOnFacility(ac.getSession(),
            ac.getServiceById(parms.readInt("service")),
            ac.getFacilityById(parms.readInt("facility")));
        } else if (parms.contains("services")) {
            return ac.getServicesManager().addDestinationsDefinedByHostsOnFacility(ac.getSession(),
            parms.readList("services", Service.class),
            ac.getFacilityById(parms.readInt("facility")));
        } else {
            return ac.getServicesManager().addDestinationsDefinedByHostsOnFacility(ac.getSession(),
            ac.getFacilityById(parms.readInt("facility")));
        }
        
      }
    },

    /*#
     * Removes an destination from a facility and service.
     * @param service int Service ID
     * @param facility int Facility ID
     * @param destination String Destination
     * @param type String Type
     */
    removeDestination {

      @Override
      public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
        ac.stateChangingCheck();

        ac.getServicesManager().removeDestination(ac.getSession(),
            ac.getServiceById(parms.readInt("service")),
            ac.getFacilityById(parms.readInt("facility")),
            ac.getDestination(parms.readString("destination"), parms.readString("type")));

        return null;
      }
    },
    
    /*#
     * Removes all destinations from a facility and service.
     * @param service int Service ID
     * @param facility int Facility ID
     */
    removeAllDestinations {

      @Override
      public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
        ac.stateChangingCheck();

        ac.getServicesManager().removeAllDestinations(ac.getSession(),
            ac.getServiceById(parms.readInt("service")),
            ac.getFacilityById(parms.readInt("facility")));

        return null;
      }
    },
    
    /*#
     * Returns owner of a Service.
     * @param service int Service ID
     * @return Owner Owner
     */
    getOwner {

      @Override
      public Owner call(ApiCaller ac, Deserializer parms) throws PerunException {
        ac.stateChangingCheck();

        return ac.getServicesManager().getOwner(ac.getSession(),
                  ac.getServiceById(parms.readInt("service")));
      }
    },
    
    /*#
     * List all destinations for all facilities which are joined by resources to the VO.
     * 
     * @param vo int VO ID
     * @return List<Destination> Found destinations
     */
    getFacilitiesDestinations {
      @Override
      public List<Destination> call(ApiCaller ac, Deserializer parms) throws PerunException {

        return ac.getServicesManager().getFacilitiesDestinations(ac.getSession(),
                  ac.getVoById(parms.readInt("vo")));
      }  
    },
    
    /*#
     * List all services associated with the facility (via resource).
     * 
     * @param facility int Facility ID
     * @return List<Service> Found services
     */
    getAssignedServices {

      @Override
      public List<Service> call(ApiCaller ac, Deserializer parms) throws PerunException {

        return ac.getServicesManager().getAssignedServices(ac.getSession(),
                  ac.getFacilityById(parms.readInt("facility")));
      }
    };
}
