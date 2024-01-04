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
import java.util.List;

/**
 * Activity 抽象类代表了 BPMN 流程图中的一个活动节点。
 * 它是所有具体活动类型的基类，提供了活动的基本属性和行为。
 */
public abstract class Activity extends FlowNode {

    // 默认流程的ID，用于在活动中作为默认的流转路径
    protected String defaultFlow;

    // 标识该活动是否是补偿活动
    protected boolean forCompensation;

    // 多实例循环特性，描述活动如何多次执行
    protected MultiInstanceLoopCharacteristics loopCharacteristics;

    // 输入/输出规范，定义了活动的数据输入和输出
    protected IOSpecification ioSpecification;

    // 数据输入关联，定义了与活动的数据输入相关的数据
    protected List<DataAssociation> dataInputAssociations = new ArrayList<>();

    // 数据输出关联，定义了与活动的数据输出相关的数据
    protected List<DataAssociation> dataOutputAssociations = new ArrayList<>();

    // 边界事件，与活动关联的事件，如错误事件或消息事件
    protected List<BoundaryEvent> boundaryEvents = new ArrayList<>();

    // 失败作业重试时间周期值，用于定义作业失败时的重试策略
    protected String failedJobRetryTimeCycleValue;

    // 异常映射条目，定义了活动发生异常时的处理规则
    protected List<MapExceptionEntry> mapExceptions = new ArrayList<>();

    public String getFailedJobRetryTimeCycleValue() {
        return failedJobRetryTimeCycleValue;
    }

    public void setFailedJobRetryTimeCycleValue(String failedJobRetryTimeCycleValue) {
        this.failedJobRetryTimeCycleValue = failedJobRetryTimeCycleValue;
    }

    public boolean isForCompensation() {
        return forCompensation;
    }

    public void setForCompensation(boolean forCompensation) {
        this.forCompensation = forCompensation;
    }

    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

    public void setBoundaryEvents(List<BoundaryEvent> boundaryEvents) {
        this.boundaryEvents = boundaryEvents;
    }

    public String getDefaultFlow() {
        return defaultFlow;
    }

    public void setDefaultFlow(String defaultFlow) {
        this.defaultFlow = defaultFlow;
    }

    public MultiInstanceLoopCharacteristics getLoopCharacteristics() {
        return loopCharacteristics;
    }

    public void setLoopCharacteristics(MultiInstanceLoopCharacteristics loopCharacteristics) {
        this.loopCharacteristics = loopCharacteristics;
    }

    public boolean hasMultiInstanceLoopCharacteristics() {
        return getLoopCharacteristics() != null;
    }

    public IOSpecification getIoSpecification() {
        return ioSpecification;
    }

    public void setIoSpecification(IOSpecification ioSpecification) {
        this.ioSpecification = ioSpecification;
    }

    public List<DataAssociation> getDataInputAssociations() {
        return dataInputAssociations;
    }

    public void setDataInputAssociations(List<DataAssociation> dataInputAssociations) {
        this.dataInputAssociations = dataInputAssociations;
    }

    public List<DataAssociation> getDataOutputAssociations() {
        return dataOutputAssociations;
    }

    public void setDataOutputAssociations(List<DataAssociation> dataOutputAssociations) {
        this.dataOutputAssociations = dataOutputAssociations;
    }

    public List<MapExceptionEntry> getMapExceptions() {
        return mapExceptions;
    }

    public void setMapExceptions(List<MapExceptionEntry> mapExceptions) {
        this.mapExceptions = mapExceptions;
    }

    /**
     * 设置这个活动节点的属性为另一个 Activity 节点的属性。
     * 该方法用于复制活动节点或在活动节点之间共享属性。
     */
    public void setValues(Activity otherActivity) {
        super.setValues(otherActivity);
        // 复制其他活动的属性
        setFailedJobRetryTimeCycleValue(otherActivity.getFailedJobRetryTimeCycleValue());
        setDefaultFlow(otherActivity.getDefaultFlow());
        setForCompensation(otherActivity.isForCompensation());

        // 复制多实例循环特性
        if (otherActivity.getLoopCharacteristics() != null) {
            setLoopCharacteristics(otherActivity.getLoopCharacteristics().clone());
        }

        // 复制输入/输出规范
        if (otherActivity.getIoSpecification() != null) {
            setIoSpecification(otherActivity.getIoSpecification().clone());
        }

        // 复制数据输入和输出关联
        dataInputAssociations = new ArrayList<>();
        if (otherActivity.getDataInputAssociations() != null && !otherActivity.getDataInputAssociations().isEmpty()) {
            for (DataAssociation association : otherActivity.getDataInputAssociations()) {
                dataInputAssociations.add(association.clone());
            }
        }

        dataOutputAssociations = new ArrayList<>();
        if (otherActivity.getDataOutputAssociations() != null && !otherActivity.getDataOutputAssociations().isEmpty()) {
            for (DataAssociation association : otherActivity.getDataOutputAssociations()) {
                dataOutputAssociations.add(association.clone());
            }
        }

        // 复制边界事件
        boundaryEvents.clear();
        boundaryEvents.addAll(otherActivity.getBoundaryEvents());
    }
}
