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

package org.flowable.engine.impl.persistence.entity;

import static java.util.Comparator.comparing;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.impl.db.HasRevision;
import org.flowable.common.engine.impl.persistence.entity.AlwaysUpdatedPersistentObject;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.eventsubscription.service.impl.persistence.entity.EventSubscriptionEntity;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntity;
import org.flowable.variable.service.impl.persistence.entity.VariableInstanceEntity;

/**
 * ExecutionEntity 接口定义了执行流程实例时的主要行为和属性。
 * 它继承了多个接口，包括 DelegateExecution, Execution, ProcessInstance 等，提供了丰富的流程执行相关功能。
 */
public interface ExecutionEntity extends DelegateExecution, Execution, ProcessInstance, Entity, AlwaysUpdatedPersistentObject, HasRevision {

    // 用于按开始时间升序比较 ExecutionEntity 的比较器
    Comparator<ExecutionEntity> EXECUTION_ENTITY_START_TIME_ASC_COMPARATOR = comparing(ProcessInstance::getStartTime);

    // 设置业务关键字
    void setBusinessKey(String businessKey);

    // 设置业务状态
    void setBusinessStatus(String businessStatus);

    // 设置流程定义ID
    void setProcessDefinitionId(String processDefinitionId);

    // 设置流程定义的关键字
    void setProcessDefinitionKey(String processDefinitionKey);

    // 设置流程定义的名称
    void setProcessDefinitionName(String processDefinitionName);

    // 设置流程定义的版本
    void setProcessDefinitionVersion(Integer processDefinitionVersion);

    // 设置部署ID
    void setDeploymentId(String deploymentId);

    // 获取流程实例
    ExecutionEntity getProcessInstance();

    // 设置流程实例
    void setProcessInstance(ExecutionEntity processInstance);

    // 获取父执行实例
    @Override
    ExecutionEntity getParent();

    // 设置父执行实例
    void setParent(ExecutionEntity parent);

    // 获取超级执行实例
    ExecutionEntity getSuperExecution();

    // 设置超级执行实例
    void setSuperExecution(ExecutionEntity superExecution);

    // 获取子流程实例
    ExecutionEntity getSubProcessInstance();

    // 设置子流程实例
    void setSubProcessInstance(ExecutionEntity subProcessInstance);

    // 设置根流程实例ID
    void setRootProcessInstanceId(String rootProcessInstanceId);

    // 获取根流程实例
    ExecutionEntity getRootProcessInstance();

    // 设置根流程实例
    void setRootProcessInstance(ExecutionEntity rootProcessInstance);

    // 获取执行实例的子执行实例列表
    @Override
    List<? extends ExecutionEntity> getExecutions();

    // 添加子执行实例
    void addChildExecution(ExecutionEntity executionEntity);

    // 获取事件订阅列表
    List<EventSubscriptionEntity> getEventSubscriptions();

    // 获取身份链接列表
    List<IdentityLinkEntity> getIdentityLinks();

    // 设置流程实例ID
    void setProcessInstanceId(String processInstanceId);

    // 设置父ID
    void setParentId(String parentId);

    // 设置执行实例是否已结束
    void setEnded(boolean isEnded);

    // 获取删除原因
    String getDeleteReason();

    // 设置删除原因
    void setDeleteReason(String deleteReason);

    // 获取暂停状态
    int getSuspensionState();

    // 设置暂停状态
    void setSuspensionState(int suspensionState);

    // 判断是否为事件范围
    boolean isEventScope();

    // 设置为事件范围
    void setEventScope(boolean isEventScope);

    // 设置名称
    void setName(String name);

    // 设置描述
    void setDescription(String description);

    // 设置本地化名称
    void setLocalizedName(String localizedName);

    // 设置本地化描述
    void setLocalizedDescription(String localizedDescription);

    // 设置租户ID
    void setTenantId(String tenantId);

    // 获取锁定时间
    Date getLockTime();

    // 设置锁定时间
    void setLockTime(Date lockTime);

    // 获取锁定所有者
    String getLockOwner();

    // 设置锁定所有者
    void setLockOwner(String lockOwner);

    // 强制更新
    void forceUpdate();

    // 获取开始活动ID
    String getStartActivityId();

    // 设置开始活动ID
    void setStartActivityId(String startActivityId);

    // 设置开始用户ID
    void setStartUserId(String startUserId);

    // 设置开始时间
    void setStartTime(Date startTime);

    // 设置回调ID
    void setCallbackId(String callbackId);

    // 设置回调类型
    void setCallbackType(String callbackType);

    // 设置变量
    void setVariable(String variableName, Object value, ExecutionEntity sourceExecution, boolean fetchAllVariables);

    // 设置参考ID
    void setReferenceId(String referenceId);

    // 设置参考类型
    void setReferenceType(String referenceType);

    // 设置传播的阶段实例ID
    void setPropagatedStageInstanceId(String propagatedStageInstanceId);

    // 设置本地变量
    Object setVariableLocal(String variableName, Object value, ExecutionEntity sourceExecution, boolean fetchAllVariables);

    // 获取原始流元素
    FlowElement getOriginatingCurrentFlowElement();

    // 设置原始流元素
    void setOriginatingCurrentFlowElement(FlowElement flowElement);

    // 获取查询变量列表
    List<VariableInstanceEntity> getQueryVariables();
}
