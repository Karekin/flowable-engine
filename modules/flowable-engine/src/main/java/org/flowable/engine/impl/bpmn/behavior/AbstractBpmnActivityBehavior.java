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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.flowable.bpmn.model.BoundaryEvent;
import org.flowable.bpmn.model.CompensateEventDefinition;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.util.CollectionUtil;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.ActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;

/**
 * AbstractBpmnActivityBehavior 是所有BPMN活动行为的基类，如任务、子流程和调用活动。
 * 它提供了处理多实例特性和执行边界事件的基本行为。
 */
public class AbstractBpmnActivityBehavior extends FlowNodeActivityBehavior {

    private static final long serialVersionUID = 1L;

    // 处理多实例活动的行为
    protected MultiInstanceActivityBehavior multiInstanceActivityBehavior;

    /**
     * 在正常的 {@link FlowNodeActivityBehavior#leave(DelegateExecution)} 被调用之前，子类调用 leave() 会先通过此方法。
     * 这样，我们可以检查活动是否有循环特性，并在这种情况下委托给相应的行为。
     */
    @Override
    public void leave(DelegateExecution execution) {
        FlowElement currentFlowElement = execution.getCurrentFlowElement();
        Collection<BoundaryEvent> boundaryEvents = findBoundaryEventsForFlowNode(execution.getProcessDefinitionId(), currentFlowElement);
        if (CollectionUtil.isNotEmpty(boundaryEvents)) {
            executeCompensateBoundaryEvents(boundaryEvents, execution);
        }
        if (!hasLoopCharacteristics()) {
            super.leave(execution);
        } else if (hasMultiInstanceCharacteristics()) {
            multiInstanceActivityBehavior.leave(execution);
        }
    }

    // 执行与流程节点关联的补偿边界事件
    protected void executeCompensateBoundaryEvents(Collection<BoundaryEvent> boundaryEvents, DelegateExecution execution) {

        // The parent execution becomes a scope, and a child execution is created for each of the boundary events
        for (BoundaryEvent boundaryEvent : boundaryEvents) {

            if (CollectionUtil.isEmpty(boundaryEvent.getEventDefinitions())) {
                continue;
            }

            if (!(boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition)) {
                continue;
            }

            ExecutionEntity childExecutionEntity = CommandContextUtil.getExecutionEntityManager().createChildExecution((ExecutionEntity) execution);
            childExecutionEntity.setParentId(execution.getId());
            childExecutionEntity.setCurrentFlowElement(boundaryEvent);
            childExecutionEntity.setScope(false);

            ActivityBehavior boundaryEventBehavior = ((ActivityBehavior) boundaryEvent.getBehavior());
            boundaryEventBehavior.execute(childExecutionEntity);
        }

    }

    protected Collection<BoundaryEvent> findBoundaryEventsForFlowNode(final String processDefinitionId, final FlowElement flowElement) {
        Process process = getProcessDefinition(processDefinitionId);

        // This could be cached or could be done at parsing time
        List<BoundaryEvent> results = new ArrayList<>(1);
        Collection<BoundaryEvent> boundaryEvents = process.findFlowElementsOfType(BoundaryEvent.class, true);
        for (BoundaryEvent boundaryEvent : boundaryEvents) {
            if (boundaryEvent.getAttachedToRefId() != null && boundaryEvent.getAttachedToRefId().equals(flowElement.getId())) {
                results.add(boundaryEvent);
            }
        }
        return results;
    }

    // 获取流程定义
    protected Process getProcessDefinition(String processDefinitionId) {
        // TODO: must be extracted / cache should be accessed in another way
        return ProcessDefinitionUtil.getProcess(processDefinitionId);
    }

    // 判断活动是否具有循环特性
    protected boolean hasLoopCharacteristics() {
        return hasMultiInstanceCharacteristics();
    }

    // 判断活动是否具有多实例特性
    protected boolean hasMultiInstanceCharacteristics() {
        return multiInstanceActivityBehavior != null;
    }

    // 获取多实例活动的行为
    public MultiInstanceActivityBehavior getMultiInstanceActivityBehavior() {
        return multiInstanceActivityBehavior;
    }

    // 设置多实例活动的行为
    public void setMultiInstanceActivityBehavior(MultiInstanceActivityBehavior multiInstanceActivityBehavior) {
        this.multiInstanceActivityBehavior = multiInstanceActivityBehavior;
    }

}
