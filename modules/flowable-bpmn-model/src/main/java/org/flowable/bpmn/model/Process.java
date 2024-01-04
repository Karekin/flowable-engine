/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.bpmn.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Process 类表示一个完整的BPMN流程定义。
 * 它包含了流程中的所有元素，包括活动、网关、事件、数据对象以及候选启动者等。
 */
public class Process extends BaseElement implements FlowElementsContainer, HasExecutionListeners {

    // 流程的名称
    protected String name;

    // 指示流程是否可执行的标志
    protected boolean executable = true;

    // 流程的文档描述
    protected String documentation;

    // 流程的输入输出规范
    protected IOSpecification ioSpecification;

    // 流程的执行监听器列表
    protected List<FlowableListener> executionListeners = new ArrayList<>();

    /**
     * 流程的泳道列表
     * 在BPMN（业务流程模型和符号）中，泳道（Lane）是一种用于组织和分类流程中的活动的工具。

     * 你可以把泳道想象成一个游泳池中的分道线，每条泳道可以代表一个特定的部门、角色、系统或参与者，它们负责流程中的不同活动或任务。
     * 举个例子，想象一个典型的采购流程，这个流程可能包括三个主要参与者：申请人、审批人和采购部门。在流程图中，你可以为每个参与者创建一个泳道：
         * - **申请人泳道**：包含提交采购请求的活动。
         * - **审批人泳道**：包含审批或拒绝请求的活动。
         * - **采购部门泳道**：包含执行采购订单和管理供应商关系的活动。
     * 使用泳道的好处是，它可以清晰地显示哪些活动是由哪个部门或个人负责，使得流程的职责分工一目了然。
     * 这有助于简化流程理解和沟通，同时确保每个任务都有明确的责任主体。在流程管理系统中，泳道还可以用来控制权限，确保只有相关责任人才能看到或执行特定的任务。
     */
    protected List<Lane> lanes = new ArrayList<>();

    // 流程的流程元素列表
    protected List<FlowElement> flowElementList = new ArrayList<>();

    // 流程的数据对象列表
    protected List<ValuedDataObject> dataObjects = new ArrayList<>();

    // 流程的工件列表
    protected List<Artifact> artifactList = new ArrayList<>();

    // 流程的候选启动用户列表
    protected List<String> candidateStarterUsers = new ArrayList<>();

    // 流程的候选启动组列表
    protected List<String> candidateStarterGroups = new ArrayList<>();

    // 流程的事件监听器列表
    protected List<EventListener> eventListeners = new ArrayList<>();

    // 流程元素的映射（id -> 元素）
    protected Map<String, FlowElement> flowElementMap = new LinkedHashMap<>();

    // 工件的映射（id -> 工件）
    protected Map<String, Artifact> artifactMap = new LinkedHashMap<>();

    // 在流程定义解析期间添加的初始流程元素
    protected FlowElement initialFlowElement;

    // 性能设置：是否启用急切的执行树获取
    protected boolean enableEagerExecutionTreeFetching;

    public Process() {

    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isExecutable() {
        return executable;
    }

    public void setExecutable(boolean executable) {
        this.executable = executable;
    }

    public IOSpecification getIoSpecification() {
        return ioSpecification;
    }

    public void setIoSpecification(IOSpecification ioSpecification) {
        this.ioSpecification = ioSpecification;
    }

    @Override
    public List<FlowableListener> getExecutionListeners() {
        return executionListeners;
    }

    @Override
    public void setExecutionListeners(List<FlowableListener> executionListeners) {
        this.executionListeners = executionListeners;
    }

    public List<Lane> getLanes() {
        return lanes;
    }

    public void setLanes(List<Lane> lanes) {
        this.lanes = lanes;
    }

    @Override
    public Map<String, FlowElement> getFlowElementMap() {
        return flowElementMap;
    }

    public void setFlowElementMap(Map<String, FlowElement> flowElementMap) {
        this.flowElementMap = flowElementMap;
    }

    public boolean containsFlowElementId(String id) {
        return flowElementMap.containsKey(id);
    }

    @Override
    public FlowElement getFlowElement(String flowElementId) {
        return getFlowElement(flowElementId, false);
    }

    /**
     * @param searchRecursive
     *            searches the whole process, including subprocesses
     */
    public FlowElement getFlowElement(String flowElementId, boolean searchRecursive) {
        if (searchRecursive) {
            return flowElementMap.get(flowElementId);
        } else {
            return findFlowElementInList(flowElementId);
        }
    }

    public List<Association> findAssociationsWithSourceRefRecursive(String sourceRef) {
        return findAssociationsWithSourceRefRecursive(this, sourceRef);
    }

    protected List<Association> findAssociationsWithSourceRefRecursive(FlowElementsContainer flowElementsContainer, String sourceRef) {
        List<Association> associations = new ArrayList<>();
        for (Artifact artifact : flowElementsContainer.getArtifacts()) {
            if (artifact instanceof Association) {
                Association association = (Association) artifact;
                if (association.getSourceRef() != null && association.getTargetRef() != null && association.getSourceRef().equals(sourceRef)) {
                    associations.add(association);
                }
            }
        }

        for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
            if (flowElement instanceof FlowElementsContainer) {
                associations.addAll(findAssociationsWithSourceRefRecursive((FlowElementsContainer) flowElement, sourceRef));
            }
        }
        return associations;
    }

    public List<Association> findAssociationsWithTargetRefRecursive(String targetRef) {
        return findAssociationsWithTargetRefRecursive(this, targetRef);
    }

    protected List<Association> findAssociationsWithTargetRefRecursive(FlowElementsContainer flowElementsContainer, String targetRef) {
        List<Association> associations = new ArrayList<>();
        for (Artifact artifact : flowElementsContainer.getArtifacts()) {
            if (artifact instanceof Association) {
                Association association = (Association) artifact;
                if (association.getTargetRef() != null && association.getTargetRef().equals(targetRef)) {
                    associations.add(association);
                }
            }
        }

        for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
            if (flowElement instanceof FlowElementsContainer) {
                associations.addAll(findAssociationsWithTargetRefRecursive((FlowElementsContainer) flowElement, targetRef));
            }
        }
        return associations;
    }

    /**
     * Searches the whole process, including subprocesses
     */
    public FlowElementsContainer getFlowElementsContainer(String flowElementId) {
        return getFlowElementsContainer(this, flowElementId);
    }

    protected FlowElementsContainer getFlowElementsContainer(FlowElementsContainer flowElementsContainer, String flowElementId) {
        for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
            if (flowElement.getId() != null && flowElement.getId().equals(flowElementId)) {
                return flowElementsContainer;
            } else if (flowElement instanceof FlowElementsContainer) {
                FlowElementsContainer result = getFlowElementsContainer((FlowElementsContainer) flowElement, flowElementId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    protected FlowElement findFlowElementInList(String flowElementId) {
        for (FlowElement f : flowElementList) {
            if (f.getId() != null && f.getId().equals(flowElementId)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public Collection<FlowElement> getFlowElements() {
        return flowElementList;
    }

    @Override
    public void addFlowElement(FlowElement element) {
        flowElementList.add(element);
        element.setParentContainer(this);
        addFlowElementToMap(element);
    }

    @Override
    public void addFlowElementToMap(FlowElement element) {
        if (element != null && StringUtils.isNotEmpty(element.getId())) {
            flowElementMap.put(element.getId(), element);
        }
    }

    @Override
    public void removeFlowElement(String elementId) {
        FlowElement element = flowElementMap.get(elementId);
        if (element != null) {
            flowElementList.remove(element);
            flowElementMap.remove(element.getId());
        }
    }

    @Override
    public void removeFlowElementFromMap(String elementId) {
        if (StringUtils.isNotEmpty(elementId)) {
            flowElementMap.remove(elementId);
        }
    }

    @Override
    public Artifact getArtifact(String id) {
        Artifact foundArtifact = null;
        for (Artifact artifact : artifactList) {
            if (id.equals(artifact.getId())) {
                foundArtifact = artifact;
                break;
            }
        }
        return foundArtifact;
    }

    @Override
    public Collection<Artifact> getArtifacts() {
        return artifactList;
    }
    
    @Override
    public Map<String, Artifact> getArtifactMap() {
        return artifactMap;
    }

    @Override
    public void addArtifact(Artifact artifact) {
        artifactList.add(artifact);
        addArtifactToMap(artifact);
    }
    
    @Override
    public void addArtifactToMap(Artifact artifact) {
        if (artifact != null && StringUtils.isNotEmpty(artifact.getId())) {
            artifactMap.put(artifact.getId(), artifact);
        }
    }

    @Override
    public void removeArtifact(String artifactId) {
        Artifact artifact = getArtifact(artifactId);
        if (artifact != null) {
            artifactList.remove(artifact);
        }
    }

    public List<String> getCandidateStarterUsers() {
        return candidateStarterUsers;
    }

    public void setCandidateStarterUsers(List<String> candidateStarterUsers) {
        this.candidateStarterUsers = candidateStarterUsers;
    }

    public List<String> getCandidateStarterGroups() {
        return candidateStarterGroups;
    }

    public void setCandidateStarterGroups(List<String> candidateStarterGroups) {
        this.candidateStarterGroups = candidateStarterGroups;
    }

    public List<EventListener> getEventListeners() {
        return eventListeners;
    }

    public void setEventListeners(List<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }

    public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsOfType(Class<FlowElementType> type) {
        return findFlowElementsOfType(type, true);
    }

    @SuppressWarnings("unchecked")
    public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsOfType(Class<FlowElementType> type, boolean goIntoSubprocesses) {
        List<FlowElementType> foundFlowElements = new ArrayList<>();
        for (FlowElement flowElement : this.getFlowElements()) {
            if (type.isInstance(flowElement)) {
                foundFlowElements.add((FlowElementType) flowElement);
            }

            if (flowElement instanceof SubProcess) {
                if (goIntoSubprocesses) {
                    foundFlowElements.addAll(findFlowElementsInSubProcessOfType((SubProcess) flowElement, type));
                }
            }
        }
        return foundFlowElements;
    }

    public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsInSubProcessOfType(SubProcess subProcess, Class<FlowElementType> type) {
        return findFlowElementsInSubProcessOfType(subProcess, type, true);
    }

    @SuppressWarnings("unchecked")
    public <FlowElementType extends FlowElement> List<FlowElementType> findFlowElementsInSubProcessOfType(SubProcess subProcess, Class<FlowElementType> type, boolean goIntoSubprocesses) {

        List<FlowElementType> foundFlowElements = new ArrayList<>();
        for (FlowElement flowElement : subProcess.getFlowElements()) {
            if (type.isInstance(flowElement)) {
                foundFlowElements.add((FlowElementType) flowElement);
            }
            if (flowElement instanceof SubProcess) {
                if (goIntoSubprocesses) {
                    foundFlowElements.addAll(findFlowElementsInSubProcessOfType((SubProcess) flowElement, type));
                }
            }
        }
        return foundFlowElements;
    }

    public FlowElementsContainer findParent(FlowElement childElement) {
        return findParent(childElement, this);
    }

    public FlowElementsContainer findParent(FlowElement childElement, FlowElementsContainer flowElementsContainer) {
        for (FlowElement flowElement : flowElementsContainer.getFlowElements()) {
            if (childElement.getId() != null && childElement.getId().equals(flowElement.getId())) {
                return flowElementsContainer;
            }
            if (flowElement instanceof FlowElementsContainer) {
                FlowElementsContainer result = findParent(childElement, (FlowElementsContainer) flowElement);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Override
    public Process clone() {
        Process clone = new Process();
        clone.setValues(this);
        return clone;
    }

    // 从另一个 Process 对象复制属性值的方法
    public void setValues(Process otherElement) {
        super.setValues(otherElement);

        // setBpmnModel(bpmnModel);
        setName(otherElement.getName());
        setExecutable(otherElement.isExecutable());
        setDocumentation(otherElement.getDocumentation());
        if (otherElement.getIoSpecification() != null) {
            setIoSpecification(otherElement.getIoSpecification().clone());
        }

        executionListeners = new ArrayList<>();
        if (otherElement.getExecutionListeners() != null && !otherElement.getExecutionListeners().isEmpty()) {
            for (FlowableListener listener : otherElement.getExecutionListeners()) {
                executionListeners.add(listener.clone());
            }
        }

        candidateStarterUsers = new ArrayList<>();
        if (otherElement.getCandidateStarterUsers() != null && !otherElement.getCandidateStarterUsers().isEmpty()) {
            candidateStarterUsers.addAll(otherElement.getCandidateStarterUsers());
        }

        candidateStarterGroups = new ArrayList<>();
        if (otherElement.getCandidateStarterGroups() != null && !otherElement.getCandidateStarterGroups().isEmpty()) {
            candidateStarterGroups.addAll(otherElement.getCandidateStarterGroups());
        }
        
        enableEagerExecutionTreeFetching = otherElement.enableEagerExecutionTreeFetching;

        eventListeners = new ArrayList<>();
        if (otherElement.getEventListeners() != null && !otherElement.getEventListeners().isEmpty()) {
            for (EventListener listener : otherElement.getEventListeners()) {
                eventListeners.add(listener.clone());
            }
        }

        /*
         * This is required because data objects in Designer have no DI info and are added as properties, not flow elements
         * 
         * Determine the differences between the 2 elements' data object
         */
        for (ValuedDataObject thisObject : getDataObjects()) {
            boolean exists = false;
            for (ValuedDataObject otherObject : otherElement.getDataObjects()) {
                if (thisObject.getId().equals(otherObject.getId())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                // missing object
                removeFlowElement(thisObject.getId());
            }
        }

        dataObjects = new ArrayList<>();
        if (otherElement.getDataObjects() != null && !otherElement.getDataObjects().isEmpty()) {
            for (ValuedDataObject dataObject : otherElement.getDataObjects()) {
                ValuedDataObject clone = dataObject.clone();
                dataObjects.add(clone);
                // add it to the list of FlowElements
                // if it is already there, remove it first so order is same as
                // data object list
                removeFlowElement(clone.getId());
                addFlowElement(clone);
            }
        }
    }

    public List<ValuedDataObject> getDataObjects() {
        return dataObjects;
    }

    public void setDataObjects(List<ValuedDataObject> dataObjects) {
        this.dataObjects = dataObjects;
    }

    public FlowElement getInitialFlowElement() {
        return initialFlowElement;
    }

    public void setInitialFlowElement(FlowElement initialFlowElement) {
        this.initialFlowElement = initialFlowElement;
    }

    public boolean isEnableEagerExecutionTreeFetching() {
        return enableEagerExecutionTreeFetching;
    }

    public void setEnableEagerExecutionTreeFetching(boolean enableEagerExecutionTreeFetching) {
        this.enableEagerExecutionTreeFetching = enableEagerExecutionTreeFetching;
    }

}
