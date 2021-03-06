package cz.metacentrum.perun.rpc.deserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.cabinet.model.*;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.model.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AuditMessage;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.rpc.RpcException;

/**
 * Deserializer that reads values from JSON content.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @version $Id$
 * @since 0.1
 */
public class JsonDeserializer extends Deserializer {

    @JsonIgnoreProperties({"name","baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
    private interface AttributeMixIn {}

    @JsonIgnoreProperties({"name", "value", "baseFriendlyName", "friendlyNameParameter", "entity", "beanName"})
    private interface AttributeDefinitionMixIn {}

    @JsonIgnoreProperties({"commonName", "displayName", "beanName"})
    private interface UserMixIn {}

    @JsonIgnoreProperties({"fullMessage"})
    private interface AuditMessageMixIn {}

    @JsonIgnoreProperties({"beanName"})
    private interface PerunBeanMixIn {}
    
    @JsonIgnoreProperties({"userExtSources"})
    private interface CandidateMixIn {}

    @JsonIgnoreProperties({"name"})
    private interface PerunExceptionMixIn {}
    
    @JsonIgnoreProperties({"hostNameFromDestination", "beanName"})
    private interface DestinationMixIn {}
    
    @JsonIgnoreProperties({"shortName", "beanName"})
    private interface GroupMixIn {}


    private interface MemberMixIn {
      @JsonIgnore
      void setStatus(String status);

      @JsonDeserialize
      void setStatus(Status status);
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.getDeserializationConfig().addMixInAnnotations(Attribute.class, AttributeMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(AttributeDefinition.class, AttributeDefinitionMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(User.class, UserMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Member.class, MemberMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(AuditMessage.class, AuditMessageMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(PerunBean.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Candidate.class, CandidateMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(PerunException.class, PerunExceptionMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Destination.class, DestinationMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Group.class, GroupMixIn.class);

        mapper.getDeserializationConfig().addMixInAnnotations(Application.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(ApplicationForm.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItem.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(ApplicationFormItemWithPrefilledValue.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(ApplicationMail.class, PerunBeanMixIn.class);

        mapper.getDeserializationConfig().addMixInAnnotations(Author.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Category.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Publication.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(PublicationForGUI.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(PublicationSystem.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(Thanks.class, PerunBeanMixIn.class);
        mapper.getDeserializationConfig().addMixInAnnotations(ThanksForGUI.class, PerunBeanMixIn.class);

    }
    
    private JsonNode root;

    /**
     * @param in {@code InputStream} to read JSON data from
     * @throws IOException if an IO error occurs
     * @throws RpcException if content of {@code in} is wrongly formatted
     */
    public JsonDeserializer(InputStream in) throws IOException, RpcException {
        try {
            root = mapper.readTree(in);
        } catch (JsonProcessingException ex) {
            throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not correct JSON data", ex);
        }

        if (!root.isObject()) {
            throw new RpcException(RpcException.Type.WRONGLY_FORMATTED_CONTENT, "not a JSON Object");
        }
    }

    @Override
    public boolean contains(String name) {
        if (root.get(name) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String readString(String name) throws RpcException {
        JsonNode node = root.get(name);

        if (node == null) {
            throw new RpcException(RpcException.Type.MISSING_VALUE, name);
        }
        if (node.isNull()) {
            return null;
        }
        if (!node.isValueNode()) {
            throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as String");
        }

        return node.getValueAsText();
    }

    @Override
    public int readInt(String name) throws RpcException {
        JsonNode node = root.get(name);

        if (node == null) {
            throw new RpcException(RpcException.Type.MISSING_VALUE, name);
        }
        if (!node.isInt()) {
            if (!node.isTextual()) {
                throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int");
            } else {
                try {
                    return Integer.parseInt(node.getTextValue());
                } catch (NumberFormatException ex) {
                    throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int", ex);
                }
            }
        }

        return node.getIntValue();
    }

    @Override
    public int[] readArrayOfInts(String name) throws RpcException {
        JsonNode node = root.get(name);

        if (node == null) {
            throw new RpcException(RpcException.Type.MISSING_VALUE, name);
        }
        if (node.isNull()) {
            return null;
        }
        if (!node.isArray()) {
            throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int[] - not an array");
        }

        int[] array = new int[node.size()];

        for (int i = 0; i < node.size(); ++i) {
            JsonNode value = node.get(i);
            if (!value.isInt()) {
                throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as int");
            }
            array[i] = node.get(i).getIntValue();
        }
        return array;
    }

    @Override
    public <T> T read(String name, Class<T> valueType) throws RpcException {
        JsonNode node = root.get(name);

        if (node == null) {
            throw new RpcException(RpcException.Type.MISSING_VALUE, name);
        }
        if (node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as " + valueType.getSimpleName());
        }

        try {
            T obj = mapper.readValue(node, valueType);
            return obj;
        } catch (IOException ex) {
            throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as " + valueType.getSimpleName(), ex);
        }
    }

    @Override
    public <T> List<T> readList(String name, Class<T> valueType) throws RpcException {
        JsonNode node = root.get(name);

        if (node == null) {
            throw new RpcException(RpcException.Type.MISSING_VALUE, name);
        }
        if (node.isNull()) {
            return null;
        }
        if (!node.isArray()) {
            throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<" + valueType.getSimpleName() + "> - not an array");
        }

        try {
            List<T> list = new ArrayList<T>(node.size());
            for (JsonNode e : node) {
                list.add(mapper.readValue(e, valueType));
            }
            return list;
        } catch (IOException ex) {
            throw new RpcException(RpcException.Type.CANNOT_DESERIALIZE_VALUE, node.toString() + " as List<" + valueType.getSimpleName() + ">", ex);
        }
    }
    
    public String readAll() throws RpcException {
      return root.toString();
    }
}
