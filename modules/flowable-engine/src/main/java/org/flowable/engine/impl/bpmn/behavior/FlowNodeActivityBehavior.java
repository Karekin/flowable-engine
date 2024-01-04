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
package org.flowable.engine.impl.bpmn.behavior;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;

/**
 * 所有“可连接”的 BPMN 2.0 流程元素的超类：任务、网关和事件。
 * 这意味着任何子类都可以是序列流的源或目标。
 * 对应于 BPMN 2.0 中的“流程节点”概念。
 * 这个抽象类定义了流程节点的基本行为，并提供了一些默认的行为实现。
 * 子类可以重写这些方法来实现具体的节点行为。
 * @author Joram Barrez
 */
public abstract class FlowNodeActivityBehavior implements TriggerableActivityBehavior {

    private static final long serialVersionUID = 1L;

    // BpmnActivityBehavior 实例，用于处理默认的出口行为。
    protected BpmnActivityBehavior bpmnActivityBehavior = new BpmnActivityBehavior();

    /**
     * 默认行为：执行后直接离开活动，没有额外的功能。
     */
    @Override
    public void execute(DelegateExecution execution) {
        leave(execution);
    }

    /**
     * 离开 BPMN 2.0 活动的默认方式：评估出去序列流上的条件，并且对那些评估为 true 的流进行流转。
     * 这个 `leave` 方法的注释描述了它在 BPMN 2.0（业务流程模型和符号）流程中的作用。让我们用一个简单的方式来解释这个方法：
     * 当一个流程在 BPMN 中的某个活动（比如一个任务或决策节点）完成后，它需要决定下一步往哪个方向走。
     * 在 BPMN 中，一个活动可能有多个出口，这些出口被称为“序列流”。
     * 这些序列流可以有条件，比如：“如果任务完成，则沿着这条路径继续”或“如果满足某个条件，则走另一条路径”。
     * `leave` 方法的作用就是来决定在一个活动完成后，流程应该沿哪些路径继续前进。具体来说，它会做以下几件事：
         * 1. 查看所有从这个活动出发的序列流。
         * 2. 对每个序列流上的条件进行评估。
         * 3. 对那些条件评估结果为真（true）的序列流进行流转，也就是沿这些路径继续流程。
     * 举个例子，想象一个流程中有一个“审批”活动，从这个活动出发有两条路径：一条是在审批通过时走的，另一条是审批未通过时走的。
     * `leave` 方法就会检查审批的结果，然后决定是继续沿着“通过”路径走，还是沿着“未通过”路径走。
     */
    public void leave(DelegateExecution execution) {
        bpmnActivityBehavior.performDefaultOutgoingBehavior((ExecutionEntity) execution);
    }

    /**
     * 离开活动并忽略条件：不评估条件直接进行流转。
     */
    public void leaveIgnoreConditions(DelegateExecution execution) {
        bpmnActivityBehavior.performIgnoreConditionsOutgoingBehavior((ExecutionEntity) execution);
    }

    /**
     * 触发方法：默认情况下抛出异常，表明此活动不等待触发。
     * 需要接受信号的具体活动行为应该重写这个方法。
     *
     * 这个 `trigger` 方法的注释描述了它在业务流程中的作用，尤其是在处理信号和事件方面。让我们用一个通俗的方式来解释这个方法：
     * 在业务流程管理（BPM）中，有些活动可能需要外部的触发来继续执行，比如等待一个特定的信号或事件。
     * 这种外部触发通常用于控制流程的流转，尤其是在涉及到异步事件或消息时。
     * 注释中的“触发方法”指的是这个方法被设计用来响应这类外部的信号或事件。
     * 以下是这个方法的一些关键点：
     * 1. 默认行为：这个方法的默认实现是直接抛出一个异常，表明当前的活动不是设计来等待外部的触发。
     *    这意味着如果这个方法在没有被重写的情况下被调用，它会指出当前活动并不期望或不处理任何外部信号。
     * 2. 重写方法：注释中提到，如果有具体的活动行为需要接受外部信号（如等待一个特定的消息或事件），那么这个方法应该被重写。
     *    重写的方法将包含处理信号的逻辑，例如在接收到信号后继续执行流程，或者根据信号数据做出特定的动作。
     * 3. 参数解释：
     *    - `DelegateExecution execution`：当前的执行上下文，提供了流程实例的状态和变量。
     *    - `String signalName`：触发此方法的信号的名称。
     *    - `Object signalData`：与信号相关的额外数据或信息。
     * 简单来说，这个 `trigger` 方法就像是一个等待接收特定信号的监听器。
     * 在流程的标准实现中，它默认不做任何事情（即不接收任何信号），但如果某个流程活动需要对特定的外部事件做出反应，那么这个方法可以被重写以处理这些事件。
     */
    @Override
    public void trigger(DelegateExecution execution, String signalName, Object signalData) {
        throw new FlowableException("this activity isn't waiting for a trigger");
    }

    /**
     * 解析活动类型：从 FlowNode 的实例获取简单类名作为元素类型，并将其首字母转为小写。
     */
    protected String parseActivityType(FlowNode flowNode) {
        String elementType = flowNode.getClass().getSimpleName();
        elementType = elementType.substring(0, 1).toLowerCase() + elementType.substring(1);
        return elementType;
    }
}
