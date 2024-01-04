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
package org.flowable.engine.runtime;

/**
 * Execution 接口代表了流程实例中的一个执行路径。
 * 这个路径可以是整个流程实例，也可以是流程中的一个分支或阶段。
 * 注意，一个 {@link ProcessInstance}（流程实例）也是一种 Execution。
 */
public interface Execution {

    /**
     * 获取执行路径的唯一标识符。
     */
    String getId();

    /**
     * 指示执行路径是否被挂起。
     */
    boolean isSuspended();

    /**
     * 指示执行路径是否已结束。
     */
    boolean isEnded();

    /**
     * 返回执行路径当前所在的活动ID。如果执行不是叶子执行（例如并发的父执行），则返回null。
     */
    String getActivityId();

    /**
     * 获取表示流程实例的执行树的根的ID。如果这个执行就是流程实例，它与 getId() 相同。
     */
    String getProcessInstanceId();

    /**
     * 获取这个执行的父执行的ID。如果为null，则表示这个执行代表一个流程实例。
     */
    String getParentId();

    /**
     * 获取这个执行的超级执行的ID。
     */
    String getSuperExecutionId();

    /**
     * 获取没有超级执行的表示流程实例的执行树的根的ID。
     */
    String getRootProcessInstanceId();

    /**
     * 获取这个流程实例的租户标识符。
     */
    String getTenantId();

    /**
     * 返回这个执行的名称。
     */
    String getName();

    /**
     * 返回这个执行的描述。
     */
    String getDescription();

    /**
     * 如果这个执行通过案例任务创建了一个案例，这将返回引用的案例实例ID。
     *
     * @return 如果这个执行创建了一个案例，返回引用的案例实例的ID。
     */
    String getReferenceId();

    /**
     * 如果这个执行通过案例任务创建了一个案例，这将返回引用的案例类型（例如 bpmn-x-to-cmmn-y 类型）。
     *
     * @return 如果这个执行创建了一个案例，返回引用的案例实例的类型。
     */
    String getReferenceType();

    /**
     * 如果这个执行在案例和阶段的上下文中运行，这个方法返回它最近的父阶段实例ID（准确地说是阶段计划项实例ID）。
     *
     * @return 这个执行所属的阶段实例ID，如果这个执行不是案例的一部分或不是阶段的子元素，则返回null。
     */
    String getPropagatedStageInstanceId();
}
