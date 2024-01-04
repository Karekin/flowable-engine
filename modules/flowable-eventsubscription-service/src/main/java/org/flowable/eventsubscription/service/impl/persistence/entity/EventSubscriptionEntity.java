package org.flowable.eventsubscription.service.impl.persistence.entity;

import java.util.Date;

import org.flowable.common.engine.impl.db.HasRevision;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.eventsubscription.api.EventSubscription;

/**
 * EventSubscriptionEntity 接口定义了事件订阅的属性和行为。
 * 它继承了 EventSubscription, Entity, HasRevision 等接口，提供了丰富的事件订阅相关功能。
 */
public interface EventSubscriptionEntity extends EventSubscription, Entity, HasRevision {

    // 设置事件类型（如信号、消息等）
    void setEventType(String eventType);

    // 设置事件名称
    void setEventName(String eventName);

    // 设置执行实例的ID
    void setExecutionId(String executionId);

    // 设置流程实例的ID
    void setProcessInstanceId(String processInstanceId);

    // 设置事件的配置信息
    void setConfiguration(String configuration);

    // 设置与事件关联的活动ID
    void setActivityId(String activityId);

    // 设置事件订阅的创建时间
    void setCreated(Date created);

    // 设置流程定义的ID
    void setProcessDefinitionId(String processDefinitionId);

    // 设置子范围ID
    void setSubScopeId(String subScopeId);

    // 设置范围ID
    void setScopeId(String scopeId);

    // 设置范围定义ID
    void setScopeDefinitionId(String scopeDefinitionId);

    // 设置范围类型
    void setScopeType(String scopeType);

    // 设置锁定所有者
    void setLockOwner(String lockOwner);

    // 设置锁定时间
    void setLockTime(Date lockTime);

    // 设置租户ID
    void setTenantId(String tenantId);
}
