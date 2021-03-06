package cz.metacentrum.perun.core.blImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ActionType;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ActionTypeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.bl.AuthzResolverBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.core.implApi.AuthzResolverImplApi;

/**
 * Authorization resolver. It decides if the perunPrincipal has rights to do the provided operation.
 * 
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * 
 * @version $Id$
 */
public class AuthzResolverBlImpl implements AuthzResolverBl {

  private final static Logger log = LoggerFactory.getLogger(AuthzResolverBlImpl.class);
  private static AuthzResolverImplApi authzResolverImpl;
  private static PerunBlImpl perunBlImpl;

  /**
   * Retrieves information about the perun principal (in which VOs the principal is admin, ...)
   * 
   * @param sess perunSession
   * @throws InternalErrorException 
   */
  protected static void init(PerunSession sess) throws InternalErrorException {

    log.debug("Initializing AuthzResolver for [{}]", sess.getPerunPrincipal());

    // Load list of perunAdmins from the configuration, split the list by the comma
    List<String> perunAdmins = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.admins").split("[ \t]*,[ \t]*")));

    // Check if the PerunPrincipal is in a group of Perun Admins
    if (perunAdmins.contains(sess.getPerunPrincipal().getActor())) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);
      sess.getPerunPrincipal().setAuthzInitialized(true);
      // We can quit, because perun admin has all privileges
      log.debug("AuthzResolver.init: Perun Admin {} loaded", sess.getPerunPrincipal().getActor());
      return;
    }  

    String perunRpcAdmin = Utils.getPropertyFromConfiguration("perun.rpc.principal");
    if (sess.getPerunPrincipal().getActor().equals(perunRpcAdmin)) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.RPC);
      log.debug("AuthzResolver.init: Perun RPC {} loaded", perunRpcAdmin);
    }

    List<String> perunServiceAdmins = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.service.principals").split("[ \t]*,[ \t]*")));
    if (perunServiceAdmins.contains(sess.getPerunPrincipal().getActor())) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SERVICE);
      log.debug("AuthzResolver.init: Perun Service {} loaded", perunServiceAdmins);
    }

    List<String> perunEngineAdmins = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.engine.principals").split("[ \t]*,[ \t]*")));
    if (perunEngineAdmins.contains(sess.getPerunPrincipal().getActor())) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.ENGINE);
      log.debug("AuthzResolver.init: Perun Engine {} loaded", perunEngineAdmins);
    }

    List<String> perunSynchronizers = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.synchronizer.principals").split("[ \t]*,[ \t]*")));
    if (perunSynchronizers.contains(sess.getPerunPrincipal().getActor())) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SYNCHRONIZER);
      log.debug("AuthzResolver.init: Perun Synchronizer {} loaded", perunSynchronizers);
    }
    
    List<String> perunNotifications = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.notification.principals").split("[ \t]*,[ \t]*")));
    if (perunNotifications.contains(sess.getPerunPrincipal().getActor())) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.NOTIFICATIONS);
      
      //FIXME ted pridame i roli plneho admina
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);
      
      log.debug("AuthzResolver.init: Perun Notifications {} loaded", perunNotifications);
    }

    List<String> perunRegistrars = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.registrar.principals").split("[ \t]*,[ \t]*")));
    if (perunRegistrars.contains(sess.getPerunPrincipal().getActor())) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.REGISTRAR);

      //FIXME ted pridame i roli plneho admina
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.PERUNADMIN);

      log.debug("AuthzResolver.init: Perun Registrar {} loaded", perunRegistrars);
    }

    if (!sess.getPerunPrincipal().getRoles().isEmpty()) {
      // We have some of the service principal, so we can quit
      sess.getPerunPrincipal().setAuthzInitialized(true);
      return;
    }

    // Load all user's roles
    sess.getPerunPrincipal().setRoles(authzResolverImpl.getRoles(sess.getPerunPrincipal().getUser()));
    
    // Add self role for the user
    if (sess.getPerunPrincipal().getUser() != null) {
      sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SELF, sess.getPerunPrincipal().getUser());
      
      // Add service user role
      if (sess.getPerunPrincipal().getUser().isServiceUser()) {
        sess.getPerunPrincipal().getRoles().putAuthzRole(Role.SERVICEUSER);
      }
    }
    sess.getPerunPrincipal().setAuthzInitialized(true);
    log.debug("AuthzResolver: Complete PerunPrincipal: {}", sess.getPerunPrincipal());
  }

  public static boolean isAuthorized(PerunSession sess, Role role, PerunBean complementaryObject) throws InternalErrorException {
    log.trace("Entering isAuthorized: sess='" +  sess + "', role='" +  role + "', complementaryObject='" +  complementaryObject + "'");
    Utils.notNull(sess, "sess");

    // We need to load additional information about the principal
    if (!sess.getPerunPrincipal().isAuthzInitialized()) {
      init(sess);
    }

    // If the user has no roles, deny access
    if (sess.getPerunPrincipal().getRoles() == null) {
      return false;
    }

    // Perun admin can do anything
    if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)) {
      return true;
    }

    // If user doesn't have requested role, deny request
    if (!sess.getPerunPrincipal().getRoles().hasRole(role)) {
      return false;
    }
    
    // Check if the principal has the priviledge
    if (complementaryObject != null) {
      
      // Check various combinations of role and complementary objects
      if (role.equals(Role.VOADMIN)) {
        // VO admin and group, get vo id from group and check if the user is vo admin 
        if (complementaryObject.getBeanName().equals(Group.class.getSimpleName())) {
          return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Group) complementaryObject).getVoId());
        }
        // VO admin and resource, check if the user is vo admin
        if (complementaryObject.getBeanName().equals(Resource.class.getSimpleName())) {
          return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Resource) complementaryObject).getVoId());
        }
        // VO admin and member, check if the member is from that VO
        if (complementaryObject.getBeanName().equals(Member.class.getSimpleName())) {
          return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Member) complementaryObject).getVoId());
        }
      } else if (role.equals(Role.FACILITYADMIN)) {
        // Facility admin and resource, get facility id from resource and check if the user is facility admin 
        if (complementaryObject.getBeanName().equals(Resource.class.getSimpleName())) {
          return sess.getPerunPrincipal().getRoles().hasRole(role, Facility.class.getSimpleName(), ((Resource) complementaryObject).getFacilityId());
        }
      } else if (role.equals(Role.GROUPADMIN)) {
        // Group admin can see some of the date of the VO
        if (complementaryObject.getBeanName().equals(Vo.class.getSimpleName())) {          
          return sess.getPerunPrincipal().getRoles().hasRole(role, Vo.class.getSimpleName(), ((Vo) complementaryObject).getId());
        }
      } else if (role.equals(Role.SELF)) {
        // Check if the member belogs to the self role
        if (complementaryObject.getBeanName().equals(Member.class.getSimpleName())) {
          return sess.getPerunPrincipal().getRoles().hasRole(role, User.class.getSimpleName(), ((Member) complementaryObject).getUserId());
        }
      }
      
      return sess.getPerunPrincipal().getRoles().hasRole(role, complementaryObject);
    } else {
        return true;
    }
  }

  public static boolean isAuthorizedForAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef, Object primaryHolder, Object secondaryHolder) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
    log.trace("Entering isAuthorizedForAttribute: sess='" +  sess + "', actiontType='" + actionType + "', attrDef='" + attrDef + "', primaryHolder='" + primaryHolder + "', secondaryHolder='" + secondaryHolder + "'");
    
    Utils.notNull(sess, "sess");
    Utils.notNull(actionType, "ActionType");
    Utils.notNull(attrDef, "AttributeDefinition");
    getPerunBlImpl().getAttributesManagerBl().checkActionTypeExists(sess, actionType);
    getPerunBlImpl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);
    
    // We need to load additional information about the principal
    if (!sess.getPerunPrincipal().isAuthzInitialized()) {
      init(sess);
    }

    // If the user has no roles, deny access
    if (sess.getPerunPrincipal().getRoles() == null) {
      return false;
    }

    // Perun admin can do anything
    if (sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN)) {
      return true;
    }
    
    // Engine and Service can read attributes
    if ((sess.getPerunPrincipal().getRoles().hasRole(Role.ENGINE) || sess.getPerunPrincipal().getRoles().hasRole(Role.SERVICE)) && actionType.equals(ActionType.READ)) {
        return true;
    }
    
    //If attrDef is type of entityless, return false (only perunAdmin can read and write to entityless)
    if(getPerunBlImpl().getAttributesManagerBl().isFromNamespace(sess, attrDef, AttributesManager.NS_ENTITYLESS_ATTR)) return false;

    //This method get all possible roles which can do action on attribute
    List<Role> roles = getRolesWhichCanWorkWithAttribute(sess, actionType, attrDef);
    
    //Now get information about primary and secondary holders to identify them!
    //All possible useful perunBeans
    Vo vo = null;
    Facility facility = null;
    Group group = null;
    Member member = null;
    User user = null;
    Host host = null;
    Resource resource = null;
    
    //Get object for primaryHolder
    if(primaryHolder != null) {
        if(primaryHolder instanceof Vo) vo = (Vo) primaryHolder;
        else if(primaryHolder instanceof Facility) facility = (Facility) primaryHolder;
        else if(primaryHolder instanceof Group) group = (Group) primaryHolder;
        else if(primaryHolder instanceof Member) member = (Member) primaryHolder;
        else if(primaryHolder instanceof User) user = (User) primaryHolder;
        else if(primaryHolder instanceof Host) host = (Host) primaryHolder;
        else if(primaryHolder instanceof Resource) resource = (Resource) primaryHolder;
        else {
          throw new InternalErrorException("There is unrecognized object in primaryHolder.");
        }
    } else {
        throw new InternalErrorException("Aiding attribtue must have perunBean which is not null.");
    }
    
    //Get object for secondaryHolder
    if(secondaryHolder != null) {
        if(secondaryHolder instanceof Vo) vo = (Vo) secondaryHolder;
        else if(secondaryHolder instanceof Facility) facility = (Facility) secondaryHolder;
        else if(secondaryHolder instanceof Group) group = (Group) secondaryHolder;
        else if(secondaryHolder instanceof Member) member = (Member) secondaryHolder;
        else if(secondaryHolder instanceof User) user = (User) secondaryHolder;
        else if(secondaryHolder instanceof Host) host = (Host) secondaryHolder;
        else if(secondaryHolder instanceof Resource) resource = (Resource) secondaryHolder;
        else {
          throw new InternalErrorException("There is unrecognized perunBean in secondaryHolder.");
        }
    } // If not, its ok, secondary holder can be null
    
    //Important: There is no options for other roles like service, serviceUser and other!
    try {
        if(resource != null && member != null) {
            if(roles.contains(Role.VOADMIN)) {
                List<Vo> vos = getPerunBlImpl().getVosManagerBl().getVosByPerunBean(sess, resource);
                for(Vo v: vos) {
                    if(isAuthorized(sess, Role.VOADMIN, v)) return true;
                }
            }
            if(roles.contains(Role.GROUPADMIN)) {
                List<Group> groups = getPerunBlImpl().getGroupsManagerBl().getGroupsByPerunBean(sess, member);
                groups.retainAll(getPerunBlImpl().getGroupsManagerBl().getGroupsByPerunBean(sess, resource));
                groups = new ArrayList<Group>(new HashSet<Group>(groups));
                for(Group g: groups) {
                    if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
                }
            }
            if(roles.contains(Role.FACILITYADMIN)) {
                Facility facilityFromResource = getPerunBlImpl().getResourcesManagerBl().getFacility(sess, resource);
                if(isAuthorized(sess, Role.FACILITYADMIN, facilityFromResource)) return true;
            }
            if(roles.contains(Role.SELF)) {
                if(getPerunBlImpl().getUsersManagerBl().getUserByMember(sess, member).equals(sess.getPerunPrincipal().getUser())) return true;
            }
        } else if(resource != null && group != null) {
            if(roles.contains(Role.VOADMIN)) {
                List<Vo> vos = getPerunBlImpl().getVosManagerBl().getVosByPerunBean(sess, resource);
                for(Vo v: vos) {
                    if(isAuthorized(sess, Role.VOADMIN, v)) return true;
                }
            } 
            if(roles.contains(Role.GROUPADMIN)) if(isAuthorized(sess, Role.GROUPADMIN, group)) return true;
            if(roles.contains(Role.FACILITYADMIN)) {
                //IMPORTANT "for now possible, but need to discuss"
                if(getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resource).contains(group)) {
                    List<Group> groups = getPerunBlImpl().getGroupsManagerBl().getGroupsByPerunBean(sess, resource);
                    for(Group g: groups) {
                        if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
                    }
                }
            }
            if(roles.contains(Role.SELF)); //Not Allowed
        } else if(user != null && facility != null) {
            if(roles.contains(Role.VOADMIN)) {
                List<Member> membersFromUser = getPerunBlImpl().getMembersManagerBl().getMembersByUser(sess, user);
                List<Resource> resourcesFromUser = new ArrayList<Resource>();
                for(Member memberElement: membersFromUser) {
                    resourcesFromUser.addAll(getPerunBlImpl().getResourcesManagerBl().getAssignedResources(sess, memberElement));
                }
                resourcesFromUser = new ArrayList<Resource>(new HashSet<Resource>(resourcesFromUser));
                resourcesFromUser.retainAll(getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility));
                List<Vo> vos = new ArrayList<Vo>();
                for(Resource resourceElement: resourcesFromUser) {
                    vos.add(getPerunBlImpl().getResourcesManagerBl().getVo(sess, resourceElement));
                }
                for(Vo v: vos) {
                    if(isAuthorized(sess, Role.VOADMIN, v)) return true;
                }
            }
            if(roles.contains(Role.GROUPADMIN)) {
                List<Member> membersFromUser = getPerunBlImpl().getMembersManagerBl().getMembersByUser(sess, user);
                List<Group> groupsFromUser = new ArrayList<Group>();
                for(Member memberElement: membersFromUser) {
                    groupsFromUser.addAll(getPerunBlImpl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
                }
                
                List<Resource> resourcesFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
                List<Group> groupsFromFacility = new ArrayList<Group>();
                for(Resource resourceElement: resourcesFromFacility) {
                    groupsFromFacility.addAll(getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resourceElement));
                }
                groupsFromUser.retainAll(groupsFromFacility);
                groupsFromUser = new ArrayList<Group>(new HashSet<Group>(groupsFromUser));
                for(Group g: groupsFromUser) {
                    if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
                }
            }
            if(roles.contains(Role.FACILITYADMIN)) if(isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
            if(roles.contains(Role.SELF)) if(isAuthorized(sess, Role.SELF, user)) return true;
        } else if(user != null) {
            if(roles.contains(Role.VOADMIN)) {
                //TEMPORARY, PROBABLY WILL BE FALSE
                List<Vo> vosFromUser = getPerunBlImpl().getUsersManagerBl().getVosWhereUserIsMember(sess, user); 
                for(Vo v: vosFromUser) {
                    if(isAuthorized(sess, Role.VOADMIN, v)) return true;
                }
            }
            if(roles.contains(Role.GROUPADMIN)) {
                List<Member> membersFromUser = getPerunBlImpl().getMembersManagerBl().getMembersByUser(sess, user);
                List<Group> groupsFromUser = new ArrayList<Group>();
                for(Member memberElement: membersFromUser) {
                    groupsFromUser.addAll(getPerunBlImpl().getGroupsManagerBl().getAllMemberGroups(sess, memberElement));
                }
                for(Group g: groupsFromUser) {
                    if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
                }
            }
            if(roles.contains(Role.FACILITYADMIN)); //Not allowed
            if(roles.contains(Role.SELF)) if(isAuthorized(sess, Role.SELF, user)) return true;
        } else if(member != null) {
            if(roles.contains(Role.VOADMIN)) {
                Vo v = getPerunBlImpl().getMembersManagerBl().getMemberVo(sess, member);
                if(isAuthorized(sess, Role.VOADMIN, v)) return true;
            }
            if(roles.contains(Role.GROUPADMIN)) {
                List<Group> groupsFromMember = getPerunBlImpl().getGroupsManagerBl().getAllMemberGroups(sess, member);
                for(Group g: groupsFromMember) {
                    if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
                }
            }
            if(roles.contains(Role.FACILITYADMIN)); //Not allowed
            if(roles.contains(Role.SELF)) {
                User u = getPerunBlImpl().getUsersManagerBl().getUserByMember(sess, member);
                if(isAuthorized(sess, Role.SELF, user)) return true;
            }
        } else if(vo != null) {
            if(roles.contains(Role.VOADMIN)) if(isAuthorized(sess, Role.VOADMIN, vo)) return true;
            if(roles.contains(Role.GROUPADMIN)); //Not allowed
            if(roles.contains(Role.FACILITYADMIN)); //Not allowed
            if(roles.contains(Role.SELF)); //Not allowed 
        } else if(group != null) {
            if(roles.contains(Role.VOADMIN)) {
                Vo v = getPerunBlImpl().getGroupsManagerBl().getVo(sess, group);
                if(isAuthorized(sess, Role.VOADMIN, v)) return true;
            }
            if(roles.contains(Role.GROUPADMIN)) if(isAuthorized(sess, Role.GROUPADMIN, group)) return true;
            if(roles.contains(Role.FACILITYADMIN)); //Not allowed
            if(roles.contains(Role.SELF)); //Not allowed
        } else if(resource != null) {
            if(roles.contains(Role.VOADMIN)) {
               Vo v = getPerunBlImpl().getResourcesManagerBl().getVo(sess, resource);
               if(isAuthorized(sess, Role.VOADMIN, vo)) return true;
            }
            if(roles.contains(Role.GROUPADMIN)); {
                List<Group> groupsFromResource = getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resource);
                for(Group g: groupsFromResource) {
                    if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
                }
            }
            if(roles.contains(Role.FACILITYADMIN)) {
                Facility f = getPerunBlImpl().getResourcesManagerBl().getFacility(sess, resource);
                if(isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
            }
            if(roles.contains(Role.SELF)); //Not allowed
        } else if(facility != null) {
            if(roles.contains(Role.VOADMIN)) {
                List<Resource> resourcesFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
                List<Vo> vosFromResources = new ArrayList<Vo>();
                for(Resource resourceElement: resourcesFromFacility) {
                    vosFromResources.add(getPerunBlImpl().getResourcesManagerBl().getVo(sess, resourceElement));
                }
                vosFromResources = new ArrayList<Vo>(new HashSet<Vo>(vosFromResources));
                for(Vo v: vosFromResources) {
                    if(isAuthorized(sess, Role.VOADMIN, v)) return true;
                }
                
            } 
            if(roles.contains(Role.GROUPADMIN)) {
                List<Resource> resourcesFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAssignedResources(sess, facility);
                List<Group> groupsFromFacility = new ArrayList<Group>();
                for(Resource resourceElement: resourcesFromFacility) {
                    groupsFromFacility.addAll(getPerunBlImpl().getResourcesManagerBl().getAssignedGroups(sess, resourceElement));
                }
                groupsFromFacility = new ArrayList<Group>(new HashSet<Group>(groupsFromFacility));
                for(Group g: groupsFromFacility){
                    if(isAuthorized(sess, Role.GROUPADMIN, g)) return true;
                }
            }
            if(roles.contains(Role.FACILITYADMIN)) if(isAuthorized(sess, Role.FACILITYADMIN, facility)) return true;
            if(roles.contains(Role.SELF)) {
                List<User> usersFromFacility = getPerunBlImpl().getFacilitiesManagerBl().getAllowedUsers(sess, facility);
                if(usersFromFacility.contains(sess.getPerunPrincipal().getUser())) {
                    return true;
                }
            }
        } else if(host != null) {
            if(roles.contains(Role.VOADMIN)); //Not allowed
            if(roles.contains(Role.GROUPADMIN)); //Not allowed
            if(roles.contains(Role.FACILITYADMIN)) {
                Facility f = getPerunBlImpl().getFacilitiesManagerBl().getFacilityForHost(sess, host);
                if(isAuthorized(sess, Role.FACILITYADMIN, f)) return true;
            } 
            if(roles.contains(Role.SELF)); //Not allowed
        } else {
            throw new InternalErrorException("There is no other possible variants for now!");
        }
    } catch (VoNotExistsException ex) {
        throw new InternalErrorException(ex);
    }
 
    return false;
  }
  
  public static List<Role> getRolesWhichCanWorkWithAttribute(PerunSession sess, ActionType actionType, AttributeDefinition attrDef) throws InternalErrorException, AttributeNotExistsException, ActionTypeNotExistsException {
    getPerunBlImpl().getAttributesManagerBl().checkAttributeExists(sess, attrDef);
    getPerunBlImpl().getAttributesManagerBl().checkActionTypeExists(sess, actionType);
    return cz.metacentrum.perun.core.impl.AuthzResolverImpl.getRolesWhichCanWorkWithAttribute(sess, actionType, attrDef);
  }
  
  /**
   * Checks if the principal is authorized.
   * 
   * @param sess perunSession
   * @param role required role
   * 
   * @return true if the principal authorized, false otherwise
   * @throws InternalErrorException if something goes wrong 
   */
  public static boolean isAuthorized(PerunSession sess, Role role) throws InternalErrorException {
    return isAuthorized(sess, role, null);
  }


  /**
   * Returns true if the perunPrincipal has requested role.
   * 
   * @param perunPrincipal
   * @param role role to be checked
   */
  public static boolean hasRole(PerunPrincipal perunPrincipal, Role role) {
    return perunPrincipal.getRoles().hasRole(role);
  }

  public String toString() {
    return getClass().getSimpleName() + ":[]";
  }

  public static boolean isVoAdmin(PerunSession sess) {
    return sess.getPerunPrincipal().getRoles().hasRole(Role.VOADMIN);
  }

  public static boolean isGroupAdmin(PerunSession sess) {
      return sess.getPerunPrincipal().getRoles().hasRole(Role.GROUPADMIN);
  }

  public static boolean isFacilityAdmin(PerunSession sess) {
      return sess.getPerunPrincipal().getRoles().hasRole(Role.FACILITYADMIN);
  }

  public static boolean isPerunAdmin(PerunSession sess) {
      return sess.getPerunPrincipal().getRoles().hasRole(Role.PERUNADMIN);
  }

  /*
   * Extracts only roles without complementary objects.
   */
  public static List<String> getPrincipalRoleNames(PerunSession sess) throws InternalErrorException {
    // We need to load the principals roles
    if (!sess.getPerunPrincipal().isAuthzInitialized()) {
      init(sess);
    }

    return sess.getPerunPrincipal().getRoles().getRolesNames();
  }

  public static User getLoggedUser(PerunSession sess) throws UserNotExistsException, InternalErrorException {
    // We need to load additional information about the principal
    if (!sess.getPerunPrincipal().isAuthzInitialized()) {
      init(sess);
    }
    return sess.getPerunPrincipal().getUser();
  }
  
  public static PerunPrincipal getPerunPrincipal(PerunSession sess) throws InternalErrorException, UserNotExistsException {
    Utils.checkPerunSession(sess);
  
    if (!sess.getPerunPrincipal().isAuthzInitialized()) {
      init(sess);
    }
    
    return sess.getPerunPrincipal();
  }
  
  /**
   * Returns all complementary objects for defined role.
   * 
   * @param sess
   * @param role
   * @return list of complementary objects
   * @throws InternalErrorException
   */
  public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role) throws InternalErrorException {
    return AuthzResolverBlImpl.getComplementaryObjectsForRole(sess, role, null);
  }
  
  /**
   * Returns only complementary objects for defined role wich fits perunBeanClass class.
   * 
   * @param sess
   * @param role
   * @param PerunBean particular class (e.g. Vo, Group, ...)
   * @return list of complementary objects
   * @throws InternalErrorException
   */
  public static List<PerunBean> getComplementaryObjectsForRole(PerunSession sess, Role role, Class perunBeanClass) throws InternalErrorException {
    Utils.checkPerunSession(sess);
    Utils.notNull(sess.getPerunPrincipal(), "sess.getPerunPrincipal()");
    
    List<PerunBean> complementaryObjects = new ArrayList<PerunBean>();
    try {
      if (sess.getPerunPrincipal().getRoles().get(role) != null) {
        for (String beanName : sess.getPerunPrincipal().getRoles().get(role).keySet()) {
          // Do we filter results on particular class?
          if (perunBeanClass == null || beanName.equals(perunBeanClass.getSimpleName())) {

            if (beanName.equals(Vo.class.getSimpleName())) {
              for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
                complementaryObjects.add(perunBlImpl.getVosManagerBl().getVoById(sess, beanId));
              }
            }

            if (beanName.equals(Group.class.getSimpleName())) {
              for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
                complementaryObjects.add(perunBlImpl.getGroupsManagerBl().getGroupById(sess, beanId));
              }
            }

            if (beanName.equals(Facility.class.getSimpleName())) {
              for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
                complementaryObjects.add(perunBlImpl.getFacilitiesManagerBl().getFacilityById(sess, beanId));
              }
            }

            if (beanName.equals(Resource.class.getSimpleName())) {
              for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
                complementaryObjects.add(perunBlImpl.getResourcesManagerBl().getResourceById(sess, beanId));
              }
            }

            if (beanName.equals(Service.class.getSimpleName())) {
              for (Integer beanId : sess.getPerunPrincipal().getRoles().get(role).get(beanName)) {
                complementaryObjects.add(perunBlImpl.getServicesManagerBl().getServiceById(sess, beanId));
              }
            }
          }
        }
      }
      
      return complementaryObjects;
      
    } catch (PerunException e) {
      throw new InternalErrorException(e);
    }
  }

  public static void refreshAuthz(PerunSession sess) throws InternalErrorException {
    Utils.checkPerunSession(sess);
    
    if (sess.getPerunPrincipal().isAuthzInitialized()) {
      log.debug("Refreshing authz roles for session {}.", sess);
      sess.getPerunPrincipal().getRoles().clear();
      sess.getPerunPrincipal().setAuthzInitialized(false);
      init(sess);
    } else {
      log.debug("Authz roles for session {} haven't been initialized yet, doing initialization.", sess);
      init(sess);
    }
  }

  // Filled by Spring
  public static AuthzResolverImplApi setAuthzResolverImpl(AuthzResolverImplApi authzResolverImpl) {
    AuthzResolverBlImpl.authzResolverImpl = authzResolverImpl;
    return authzResolverImpl;
  }

  //Filled by Spring
  public static PerunBlImpl setPerunBlImpl(PerunBlImpl perunBlImpl) {
    AuthzResolverBlImpl.perunBlImpl = perunBlImpl;
    return perunBlImpl;
 }
  
  public static PerunBlImpl getPerunBlImpl() {
      return perunBlImpl;
  }
}
