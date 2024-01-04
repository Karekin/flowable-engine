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
