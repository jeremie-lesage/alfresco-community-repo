/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.web.scripts.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * @author Nick Smith
 *
 */
public class WorkflowModelBuilder
{
    public static final String PERSON_LAST_NAME = "lastName";
    public static final String PERSON_FIRST_NAME = "firstName";
    public static final String PERSON_USER_NAME = "userName";

    public static final String TASK_PROPERTIES = "properties";
    public static final String TASK_OWNER = "owner";
    public static final String TASK_TYPE_DEFINITION_TITLE = "typeDefinitionTitle";
    public static final String TASK_STATE = "state";
    public static final String TASK_DESCRIPTION = "description";
    public static final String TASK_TITLE = "title";
    public static final String TASK_NAME = "name";
    public static final String TASK_URL = "url";
    public static final String TASK_IS_POOLED = "isPooled";
    public static final String TASK_ID = "id";
    public static final String TASK_PATH = "path";
    public static final String TASK_DEFINITION = "definition";

    public static final String TASK_DEFINITION_ID = "id";
    public static final String TASK_DEFINITION_URL = "url";
    public static final String TASK_DEFINITION_TYPE = "type";
    public static final String TASK_DEFINITION_NODE = "node";

    public static final String TASK_WORKFLOW_INSTANCE = "workflowInstance";
    
    public static final String TASK_WORKFLOW_INSTANCE_ID = "id";
    public static final String TASK_WORKFLOW_INSTANCE_URL = "url";
    public static final String TASK_WORKFLOW_INSTANCE_NAME = "name";
    public static final String TASK_WORKFLOW_INSTANCE_TITLE = "title";
    public static final String TASK_WORKFLOW_INSTANCE_DESCRIPTION = "description";
    public static final String TASK_WORKFLOW_INSTANCE_IS_ACTIVE = "isActive";
    public static final String TASK_WORKFLOW_INSTANCE_START_DATE = "startDate";
    public static final String TASK_WORKFLOW_INSTANCE_END_DATE = "endDate";
    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR = "initiator";
    public static final String TASK_WORKFLOW_INSTANCE_DEFINITION_URL = "definitionUrl";

    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_USERNAME = "userName";
    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_FIRSTNAME = "firstName";
    public static final String TASK_WORKFLOW_INSTANCE_INITIATOR_LASTNAME = "lastName";
    
    public static final String TYPE_DEFINITION_NAME = "name";
    public static final String TYPE_DEFINITION_TITLE = "title";
    public static final String TYPE_DEFINITION_DESCRIPTION = "description";
    public static final String TYPE_DEFINITION_URL = "url";

    public static final String WORKFLOW_NODE_NAME = "name";
    public static final String WORKFLOW_NODE_TITLE = "title";
    public static final String WORKFLOW_NODE_DESCRIPTION = "description";
    public static final String WORKFLOW_NODE_IS_TASK_NODE = "isTaskNode";
    public static final String WORKFLOW_NODE_TRANSITIONS = "transitions";

    public static final String WORKFLOW_NODE_TRANSITION_ID = "id";
    public static final String WORKFLOW_NODE_TRANSITION_TITLE = "title";
    public static final String WORKFLOW_NODE_TRANSITION_DESCRIPTION = "description";
    public static final String WORKFLOW_NODE_TRANSITION_IS_DEFAULT = "isDefault";
    public static final String WORKFLOW_NODE_TRANSITION_IS_HIDDEN = "isHidden";

    public static final String WORKFLOW_DEFINITION_ID = "id";
    public static final String WORKFLOW_DEFINITION_URL = "url";
    public static final String WORKFLOW_DEFINITION_NAME = "name";
    public static final String WORKFLOW_DEFINITION_TITLE = "title";
    public static final String WORKFLOW_DEFINITION_DESCRIPTION = "description";

    private static final String PREFIX_SEPARATOR = Character.toString(QName.NAMESPACE_PREFIX);

    private final NamespaceService namespaceService;
    private final NodeService nodeService;
    private final PersonService personService;

    public WorkflowModelBuilder(NamespaceService namespaceService, NodeService nodeService, PersonService personService)
    {
        this.namespaceService = namespaceService;
        this.nodeService = nodeService;
        this.personService = personService;
    }

    /**
     * Returns a simple representation of a {@link WorkflowTask}.
     * @param task The task to be represented.
     * @param propertyFilters Specify which properties to include.
     * @return
     */
    public Map<String, Object> buildSimple(WorkflowTask task, Collection<String> propertyFilters)
    {
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put(TASK_ID, task.getId());
        model.put(TASK_URL, getUrl(task));
        model.put(TASK_NAME, task.getName());
        model.put(TASK_TITLE, task.getTitle());
        model.put(TASK_DESCRIPTION, task.getDescription());
        model.put(TASK_STATE, task.getState().name());
        model.put(TASK_TYPE_DEFINITION_TITLE, task.getDefinition().getMetadata().getTitle());

        model.put(TASK_IS_POOLED, isPooled(task.getProperties()));
        Serializable owner = task.getProperties().get(ContentModel.PROP_OWNER);
        model.put(TASK_OWNER, getPersonModel( owner));

        model.put(TASK_PROPERTIES, buildProperties(task, propertyFilters));
        return model;
    }

    /**
     * Returns a detailed representation of a {@link WorkflowTask}.
     * @param workflowTask The task to be represented.     
     * @return
     */
    public Map<String, Object> buildDetailed(WorkflowTask workflowTask)
    {
        Map<String, Object> model = buildSimple(workflowTask, null);

        model.put(TASK_PATH, getUrl(workflowTask.getPath()));

        // workflow instance part
        model.put(TASK_WORKFLOW_INSTANCE, buildWorkflowInstance(workflowTask.path.instance));

        // definition part
        model.put(TASK_DEFINITION, buildTaskDefinition(workflowTask.definition, workflowTask));

        return model;
    }

    /**
     * Returns a simple representation of a {@link WorkflowDefinition}.
     * 
     * @param workflowDefinition the WorkflowDefinition object to be represented.
     * @return
     */
    public Map<String, Object> buildSimple(WorkflowDefinition workflowDefinition)
    {
        HashMap<String, Object> model = new HashMap<String, Object>();

        model.put(WORKFLOW_DEFINITION_ID, workflowDefinition.getId());
        model.put(WORKFLOW_DEFINITION_URL, getUrl(workflowDefinition));
        model.put(WORKFLOW_DEFINITION_NAME, workflowDefinition.getName());
        model.put(WORKFLOW_DEFINITION_TITLE, workflowDefinition.getTitle());
        model.put(WORKFLOW_DEFINITION_DESCRIPTION, workflowDefinition.getDescription());

        return model;
    }

    private Object isPooled(Map<QName, Serializable> properties)
    {
        Collection<?> actors = (Collection<?>) properties.get(WorkflowModel.ASSOC_POOLED_ACTORS);
        return actors != null && !actors.isEmpty();
    }

    private Map<String, Object> buildProperties(WorkflowTask task, Collection<String> propertyFilters)
    {
        Map<QName, Serializable> properties = task.getProperties();
        Collection<QName> keys;
        if (propertyFilters == null || propertyFilters.size() == 0)
        {
            TypeDefinition taskType = task.getDefinition().getMetadata();
            Map<QName, PropertyDefinition> propDefs = taskType.getProperties();
            Map<QName, AssociationDefinition> assocDefs = taskType.getAssociations();
            Set<QName> propKeys = properties.keySet();
            keys = new HashSet<QName>(propDefs.size() + assocDefs.size() + propKeys.size());
            keys.addAll(propDefs.keySet());
            keys.addAll(assocDefs.keySet());
            keys.addAll(propKeys);
        }
        else
            keys = buildQNameKeys(propertyFilters);
        return buildQNameProperties(properties, keys);
    }

    private Map<String, Object> buildQNameProperties(Map<QName, Serializable> properties, Collection<QName> keys)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        for (QName key : keys)
        {
            Object value = convertValue(properties.get(key));
            String prefixedKey = key.toPrefixString(namespaceService);
            String strKey = prefixedKey.replace(PREFIX_SEPARATOR, "_");
            model.put(strKey, value);
        }
        return model;
    }

    private Object convertValue(Object value)
    {
        if (value == null || value instanceof Boolean || value instanceof Number || value instanceof String)
        {
            return value;
        }
        if (value instanceof Collection<?>)
        {
            Collection<?> collection = (Collection<?>) value;
            ArrayList<Object> results = new ArrayList<Object>(collection.size());
            for (Object obj : collection)
            {
                results.add(convertValue(obj));
            }
            return results;
        }
        return DefaultTypeConverter.INSTANCE.convert(String.class, value);
    }

    private Collection<QName> buildQNameKeys(Collection<String> keys)
    {
        List<QName> qKeys = new ArrayList<QName>(keys.size());
        for (String key : keys)
        {
            String prefixedName = key.replaceFirst("_", PREFIX_SEPARATOR);
            try
            {
                QName qKey = QName.createQName(prefixedName, namespaceService);
                qKeys.add(qKey);
            }
            catch (NamespaceException e)
            {
                throw new AlfrescoRuntimeException("Invalid property key: " + key, e);
            }
        }
        return qKeys;
    }

    private Map<String, Object> getPersonModel(Serializable nameSer)
    {
        if (!(nameSer instanceof String))
            return null;

        String name = (String) nameSer;
        NodeRef person = personService.getPerson(name);
        Map<QName, Serializable> properties = nodeService.getProperties(person);

        // TODO Person URL?
        Map<String, Object> model = new HashMap<String, Object>();
        model.put(PERSON_USER_NAME, name);
        model.put(PERSON_FIRST_NAME, properties.get(ContentModel.PROP_FIRSTNAME));
        model.put(PERSON_LAST_NAME, properties.get(ContentModel.PROP_LASTNAME));
        return model;
    }

    private Map<String, Object> buildWorkflowInstance(WorkflowInstance workflowInstance)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(TASK_WORKFLOW_INSTANCE_ID, workflowInstance.id);
        model.put(TASK_WORKFLOW_INSTANCE_URL, getUrl(workflowInstance));
        model.put(TASK_WORKFLOW_INSTANCE_NAME, workflowInstance.definition.name);
        model.put(TASK_WORKFLOW_INSTANCE_TITLE, workflowInstance.definition.title);
        model.put(TASK_WORKFLOW_INSTANCE_DESCRIPTION, workflowInstance.definition.description);
        model.put(TASK_WORKFLOW_INSTANCE_IS_ACTIVE, workflowInstance.active);

        if (workflowInstance.startDate == null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_START_DATE, workflowInstance.startDate);
        }
        else
        {
            model.put(TASK_WORKFLOW_INSTANCE_START_DATE, ISO8601DateFormat.format(workflowInstance.startDate));
        }

        if (workflowInstance.endDate == null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_END_DATE, workflowInstance.endDate);
        }
        else
        {
            model.put(TASK_WORKFLOW_INSTANCE_END_DATE, ISO8601DateFormat.format(workflowInstance.endDate));
        }

        if (workflowInstance.initiator == null)
        {
            model.put(TASK_WORKFLOW_INSTANCE_INITIATOR, null);
        }
        else
        {
            model.put(TASK_WORKFLOW_INSTANCE_INITIATOR, getPersonModel(nodeService.getProperty(workflowInstance.initiator, ContentModel.PROP_USERNAME)));
        }
        model.put(TASK_WORKFLOW_INSTANCE_DEFINITION_URL, getUrl(workflowInstance.definition));

        return model;
    }
    
    private Map<String, Object> buildTaskDefinition(WorkflowTaskDefinition workflowTaskDefinition, WorkflowTask workflowTask)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(TASK_DEFINITION_ID, workflowTaskDefinition.id);
        model.put(TASK_DEFINITION_URL, getUrl(workflowTaskDefinition));
        model.put(TASK_DEFINITION_TYPE, buildTypeDefinition(workflowTaskDefinition.metadata));
        model.put(TASK_DEFINITION_NODE, buildWorkflowNode(workflowTaskDefinition.node, workflowTask));

        return model;
    }

    private Map<String, Object> buildTypeDefinition(TypeDefinition typeDefinition)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(TYPE_DEFINITION_NAME, typeDefinition.getName());
        model.put(TYPE_DEFINITION_TITLE, typeDefinition.getTitle());
        model.put(TYPE_DEFINITION_DESCRIPTION, typeDefinition.getDescription());
        model.put(TYPE_DEFINITION_URL, getUrl(typeDefinition));

        return model;
    }

    private Map<String, Object> buildWorkflowNode(WorkflowNode workflowNode, WorkflowTask workflowTask)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        model.put(WORKFLOW_NODE_NAME, workflowNode.getName());
        model.put(WORKFLOW_NODE_TITLE, workflowNode.getTitle());
        model.put(WORKFLOW_NODE_DESCRIPTION, workflowNode.getDescription());
        model.put(WORKFLOW_NODE_IS_TASK_NODE, workflowNode.isTaskNode());

        List<Map<String, Object>> transitions = new ArrayList<Map<String, Object>>();
        List<?> hiddenTransitions = getHiddenTransitions(workflowTask.getProperties());
        for (WorkflowTransition workflowTransition : workflowNode.getTransitions())
        {
            Map<String, Object> transitionModel = build(workflowTransition, hiddenTransitions);
            transitions.add(transitionModel);
        }
        model.put(WORKFLOW_NODE_TRANSITIONS, transitions);
        return model;
    }

    /**
     * @param properties
     * @return
     */
    private List<?> getHiddenTransitions(Map<QName, Serializable> properties)
    {
        Serializable hiddenSer = properties.get(WorkflowModel.PROP_HIDDEN_TRANSITIONS);
        if(hiddenSer instanceof List<?>)
            return (List<?>) hiddenSer;
        else if(hiddenSer instanceof String)
        {
            String hiddenStr = (String) hiddenSer;
            return Arrays.asList(hiddenStr.split(","));
        }
        return null;
    }

    public Map<String, Object> build(WorkflowTransition workflowTransition, List<?> hiddenTransitions)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        String id = workflowTransition.getId();
        model.put(WORKFLOW_NODE_TRANSITION_ID, id);
        model.put(WORKFLOW_NODE_TRANSITION_TITLE, workflowTransition.getTitle());
        model.put(WORKFLOW_NODE_TRANSITION_DESCRIPTION, workflowTransition.getDescription());
        model.put(WORKFLOW_NODE_TRANSITION_IS_DEFAULT, workflowTransition.isDefault);
        model.put(WORKFLOW_NODE_TRANSITION_IS_HIDDEN, isHiddenTransition(id, hiddenTransitions));
        return model;
    }

    private boolean isHiddenTransition(String transitionId, List<?> hiddenTransitions)
    {
        if(hiddenTransitions == null)
            return false;
        return hiddenTransitions.contains(transitionId);
    }

    private String getUrl(WorkflowTask task)
    {
        return "api/task-instances/" + task.getId();
    }

    private String getUrl(WorkflowDefinition workflowDefinition)
    {
        return "api/workflow-definitions/" + workflowDefinition.getId();
    }

    private String getUrl(WorkflowTaskDefinition workflowTaskDefinition)
    {
        return "api/task-definitions/" + workflowTaskDefinition.getId();
    }

    private String getUrl(TypeDefinition typeDefinition)
    {
        return "api/classes/" + typeDefinition.getName().toPrefixString().replace(PREFIX_SEPARATOR, "_");
    }

    private String getUrl(WorkflowPath path)
    {
        return "api/workflow-paths/" + path.getId();
    }

    private String getUrl(WorkflowInstance workflowInstance)
    {
        return "api/workflow-instances/" + workflowInstance.getId();
    }

}
