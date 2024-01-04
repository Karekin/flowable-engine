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

package org.flowable.engine.delegate;

import java.util.List;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.variable.api.delegate.VariableScope;

/**
 * 执行用于 {@link JavaDelegate} 和 {@link ExecutionListener} 的接口。
 * 它定义了流程执行期间可以访问和管理的执行上下文相关的属性和行为。
 *
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface DelegateExecution extends VariableScope {

    /**
     * 获取这个执行路径的唯一标识符，这可以作为在等待状态后向引擎提供外部信号的句柄。
     */
    String getId();

    /** 获取关联到这个执行的整个流程实例的引用 */
    String getProcessInstanceId();

    /**
     * 获取'根'流程实例的标识符。当使用调用活动（Call Activity）时，返回的流程实例可能不总是根实例。
     * 此方法返回最顶层的流程实例标识符。
     */
    String getRootProcessInstanceId();

    /**
     * 如果这个执行被传递给 {@link ExecutionListener}，将包含事件名称。
     */
    String getEventName();

    /**
     * 设置当前事件名称（通常是在执行 {@link ExecutionListener} 时）。
     */
    void setEventName(String eventName);

    /**
     * 获取与此执行关联的流程实例的业务键。
     */
    String getProcessInstanceBusinessKey();
    
    /**
     * 获取与此执行关联的流程实例的业务状态。
     */
    String getProcessInstanceBusinessStatus();

    /**
     * 获取与此执行关联的流程定义的键。
     */
    String getProcessDefinitionId();

    /**
     * 如果这个执行在一个案例和阶段的上下文中运行，这个方法返回它最近的父阶段实例标识符（确切地说是阶段计划项实例标识符）。
     *
     * @return 执行所属阶段实例的标识符；如果这个执行根本不是案例的一部分，或者不是阶段子元素，则返回null。
     */
    String getPropagatedStageInstanceId();

    /**
     * 获取这个执行的父执行的标识符。如果返回null，那么这个执行代表一个流程实例。
     */
    String getParentId();

    /**
     * 获取调用执行的标识符。如果不为null，则执行是子流程的一部分。
     */
    String getSuperExecutionId();

    /**
     * 获取当前活动的标识符。
     */
    String getCurrentActivityId();

    /**
     * 如果在流程定义或流程实例上之前设置了租户标识符，则返回该租户标识符。
     */
    @Override
    String getTenantId();

    /**
     * 获取执行当前所在的BPMN元素。
     */
    FlowElement getCurrentFlowElement();

    /**
     * 更改执行当前所在的BPMN元素。
     */
    void setCurrentFlowElement(FlowElement flowElement);

    /**
     * 如果当前正在执行一个与 {@link ExecutionListener} 匹配的 {@link FlowableListener}，则返回它。
     * 否则返回null。
     */
    FlowableListener getCurrentFlowableListener();

    /**
     * 在执行 {@link ExecutionListener} 时被调用。
     */
    void setCurrentFlowableListener(FlowableListener currentListener);

    /**
     * 创建这个 delegate execution 的只读快照。
     *
     * @return {@link ReadOnlyDelegateExecution} 的实例
     */
    ReadOnlyDelegateExecution snapshotReadOnly();

    /* 执行管理 */

    /**
     * 返回这个执行的父执行，如果没有父执行则返回null。
     */
    DelegateExecution getParent();

    /**
     * 返回这个执行作为父执行的执行列表。
     */
    List<? extends DelegateExecution> getExecutions();

    /* 状态管理 */

    /**
     * 设置这个执行是否活跃。
     */
    void setActive(boolean isActive);

    /**
     * 返回这个执行当前是否活跃。
     */
    boolean isActive();

    /**
     * 返回这个执行是否已经结束。
     */
    boolean isEnded();

    /**
     * 更改这个执行上的并发指示器。
     */
    void setConcurrent(boolean isConcurrent);

    /**
     * 返回这个执行是否并发。
     */
    boolean isConcurrent();

    /**
     * 返回这个执行是否为流程实例。
     */
    boolean isProcessInstanceType();

    /**
     * 使这个执行变为非活跃状态。这在例如联接时很有用：执行仍然存在，但不再活跃。
     */
    void inactivate();

    /**
     * 返回这个执行是否是一个作用域。
     */
    boolean isScope();

    /**
     * 更改这个执行是否为一个作用域。
     */
    void setScope(boolean isScope);

    /**
     * 返回这个执行是否是多实例执行的根。
     */
    boolean isMultiInstanceRoot();

    /**
     * 更改这个执行是否是多实例执行的根。
     *
     * @param isMultiInstanceRoot 是否为多实例的根
     */
    void setMultiInstanceRoot(boolean isMultiInstanceRoot);

}
