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
 * Event 抽象类表示 BPMN 流程图中的一个事件节点。
 * 事件是流程中发生的动作或结果，比如开始、结束或捕获信号等。
 * 本类是所有具体事件节点的基类，比如开始事件、结束事件或中间事件。
 */
public abstract class Event extends FlowNode {

    // 与事件相关的事件定义列表
    protected List<EventDefinition> eventDefinitions = new ArrayList<>();

    // 事件的输入参数列表
    protected List<IOParameter> inParameters = new ArrayList<>();

    // 事件的输出参数列表
    protected List<IOParameter> outParameters = new ArrayList<>();

    // 获取事件定义的方法
    public List<EventDefinition> getEventDefinitions() {
        return eventDefinitions;
    }

    // 设置事件定义的方法
    public void setEventDefinitions(List<EventDefinition> eventDefinitions) {
        this.eventDefinitions = eventDefinitions;
    }

    // 向事件添加一个事件定义
    public void addEventDefinition(EventDefinition eventDefinition) {
        eventDefinitions.add(eventDefinition);
    }

    // 获取输入参数的方法
    public List<IOParameter> getInParameters() {
        return inParameters;
    }

    // 设置输入参数的方法
    public void setInParameters(List<IOParameter> inParameters) {
        this.inParameters = inParameters;
    }

    // 获取输出参数的方法
    public List<IOParameter> getOutParameters() {
        return outParameters;
    }

    // 设置输出参数的方法
    public void setOutParameters(List<IOParameter> outParameters) {
        this.outParameters = outParameters;
    }

    // 从另一个 Event 对象复制属性值的方法
    public void setValues(Event otherEvent) {
        super.setValues(otherEvent);

        eventDefinitions = new ArrayList<>();
        if (otherEvent.getEventDefinitions() != null && !otherEvent.getEventDefinitions().isEmpty()) {
            for (EventDefinition eventDef : otherEvent.getEventDefinitions()) {
                eventDefinitions.add(eventDef.clone());
            }
        }
        
        inParameters = new ArrayList<>();
        if (otherEvent.getInParameters() != null && !otherEvent.getInParameters().isEmpty()) {
            for (IOParameter parameter : otherEvent.getInParameters()) {
                inParameters.add(parameter.clone());
            }
        }

        outParameters = new ArrayList<>();
        if (otherEvent.getOutParameters() != null && !otherEvent.getOutParameters().isEmpty()) {
            for (IOParameter parameter : otherEvent.getOutParameters()) {
                outParameters.add(parameter.clone());
            }
        }
    }
}
