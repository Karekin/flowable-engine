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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * FlowElement 抽象类是 BPMN 流程图中所有元素的基类。
 * 它定义了流程元素的基本属性，如名称和文档说明，并提供了执行监听器的管理。

 * 以下是 `FlowElement` 的一些直接和间接实现类的概览：
 * 1. **SequenceFlow**：表示流程图中的一条流动的路径，连接两个流程节点。
 * 2. **DataStoreReference**：用于描述流程中用于存储数据的实体。
 * 3. **DataObject** 和 **ValuedDataObject** 及其子类
 *      （如 `StringDataObject`, `BooleanDataObject`, `LongDataObject` 等）：表示流程中的数据对象，可以携带流程执行过程中的数据。
 * 4. **FlowNode**：`FlowNode` 是流程节点的基类，代表流程图中的一个点，比如活动或事件。
 *    - **Activity**：活动是流程中的工作单元，比如任务。
 *       - **CallActivity**：表示一个调用活动，它可以调用流程图中的另一个流程。
 *       - **SubProcess**：表示子流程，它是流程中的一个流程片段。
 *          - **Transaction**：特殊的子流程，用于事务管理。
 *          - **EventSubProcess**：事件触发的子流程。
 *          - **AdhocSubProcess**：非结构化的子流程，其中的活动没有严格的执行顺序。
 *       - **Task**：表示一个任务，是活动的一种。
 *          - **ScriptTask**：执行脚本的任务。
 *          - **ManualTask**：人工执行的任务。
 *          - **ReceiveTask**：等待特定消息到达的任务。
 *          - **BusinessRuleTask**：用于执行业务规则的任务。
 *          - **UserTask**：用户交互的任务。
 *    - **Gateway**：网关用于控制流程的分支和汇合。
 *       - **ExclusiveGateway**：排他网关，基于条件选择一条执行路径。
 *       - **InclusiveGateway**：包容性网关，可以基于条件选择多条路径。
 *       - **ParallelGateway**：并行网关，用于分发和汇聚并行执行路径。
 *       - **EventGateway**：事件网关，用于根据事件决定流程的方向。
 *    - **Event**：事件是流程中发生的事情，比如开始、结束或其他中间事件。
 *       - **StartEvent**：标识流程的开始。
 *       - **EndEvent**：标识流程的结束。
 *       - **IntermediateCatchEvent**：用于处理流程中的等待和捕获事件。
 *       - **BoundaryEvent**：附加到活动边界的事件，用于处理活动中的特定情况。
 *       - **ThrowEvent**：用于抛出事件。
 * 这个层次结构允许流程定义具有各种复杂性，从简单的任务到复杂的子流程，每个元素都可以有它自己的行为和属性。
 */
public abstract class FlowElement extends BaseElement implements HasExecutionListeners {

    // 流程元素的名称
    protected String name;

    // 流程元素的文档说明
    protected String documentation;

    // 流程元素的执行监听器列表
    protected List<FlowableListener> executionListeners = new ArrayList<>();

    // 流程元素的父容器，可以是流程本身或子流程等
    protected FlowElementsContainer parentContainer;

    // 获取名称的方法
    public String getName() {
        return name;
    }

    // 设置名称的方法
    public void setName(String name) {
        this.name = name;
    }

    // 获取文档说明的方法
    public String getDocumentation() {
        return documentation;
    }

    // 设置文档说明的方法
    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    // 获取执行监听器的方法
    @Override
    public List<FlowableListener> getExecutionListeners() {
        return executionListeners;
    }

    // 设置执行监听器的方法
    @Override
    public void setExecutionListeners(List<FlowableListener> executionListeners) {
        this.executionListeners = executionListeners;
    }

    // 获取父容器的方法
    @JsonIgnore
    public FlowElementsContainer getParentContainer() {
        return parentContainer;
    }

    // 获取子流程的方法，如果父容器是子流程的话
    @JsonIgnore
    public SubProcess getSubProcess() {
        SubProcess subProcess = null;
        if (parentContainer instanceof SubProcess) {
            subProcess = (SubProcess) parentContainer;
        }

        return subProcess;
    }

    // 设置父容器的方法
    public void setParentContainer(FlowElementsContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    // 抽象方法，用于克隆流程元素
    @Override
    public abstract FlowElement clone();

    // 设置这个元素的属性为另一个 FlowElement 元素的属性的方法
    public void setValues(FlowElement otherElement) {
        super.setValues(otherElement);
        setName(otherElement.getName());
        setDocumentation(otherElement.getDocumentation());

        executionListeners = new ArrayList<>();
        if (otherElement.getExecutionListeners() != null && !otherElement.getExecutionListeners().isEmpty()) {
            for (FlowableListener listener : otherElement.getExecutionListeners()) {
                executionListeners.add(listener.clone());
            }
        }
    }
}
