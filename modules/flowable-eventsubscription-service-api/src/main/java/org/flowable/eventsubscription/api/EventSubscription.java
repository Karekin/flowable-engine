package org.flowable.eventsubscription.api;

import java.util.Date;

/**
 * EventSubscription 接口代表了流程定义或流程实例中的一个事件订阅，用于处理例如信号或消息这样的事件。
 * 事件订阅是工作流程和业务流程管理中的关键概念，允许流程元素在特定事件（如消息、信号等）发生时进行响应
 */
public interface EventSubscription {

    /**
     * 获取执行实例的唯一标识符。
     */
    String getId();

    /**
     * 返回订阅的类型，例如信号或消息。
     */
    String getEventType();

    /**
     * 获取信号或消息事件的事件名称。
     */
    String getEventName();

    /**
     * 获取此事件订阅的执行实例ID。
     */
    String getExecutionId();

    /**
     * 获取在 BPMN 定义中定义此事件订阅的活动ID。
     */
    String getActivityId();

    /**
     * 获取此事件订阅的流程实例ID。
     */
    String getProcessInstanceId();

    /**
     * 获取此事件订阅的流程定义ID。
     */
    String getProcessDefinitionId();

    /**
     * 获取此事件订阅的子范围ID。
     */
    String getSubScopeId();

    /**
     * 获取此事件订阅的范围ID。
     */
    String getScopeId();

    /**
     * 获取此事件订阅的范围定义ID。
     */
    String getScopeDefinitionId();

    /**
     * 获取此事件订阅的范围类型。
     */
    String getScopeType();

    /**
     * 返回包含此事件订阅额外信息的配置。
     */
    String getConfiguration();

    /**
     * 获取创建此事件订阅的日期/时间。
     */
    Date getCreated();

    /**
     * 如果此事件订阅被锁定，获取其所有者。
     */
    String getLockOwner();

    /**
     * 获取锁定此事件订阅的日期/时间。
     */
    Date getLockTime();

    /**
     * 获取此流程实例的租户标识符。
     */
    String getTenantId();
}
