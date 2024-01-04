package org.flowable.engine.impl.bpmn.behavior;

import java.io.Serializable;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;

/**
 * 这个类是 BPMN 2.0 活动的辅助类，提供了特定于 BPMN 2.0 的便利方法。
 * 这个类可以通过继承或聚合来使用。
 */
public class BpmnActivityBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 执行默认的BPMN 2.0出口行为，即对于出口序列流创建并行的执行路径。
     * 具体来说：选择每一个条件评估为真（或没有条件）的序列流（一个节点可能有多条出口路径）来继续流程实例。如果选中了多个序列流，将创建多个并行的执行路径。

     * "出口行为"（Outgoing Behavior）是指在业务流程管理和工作流程自动化中，一个流程节点（比如任务、决策点等）完成后，决定如何向下一个节点或多个节点转移的逻辑。
     * 这个概念主要出现在像BPMN（业务流程模型和符号）这样的流程建模标准中。在这种上下文中，流程节点通常有一个或多个“出口”，每个出口都对应到流程的另一个节点。
     * 出口行为可能包括以下几个方面：
     * 1. 条件判断：对于那些带有条件的出口（比如“如果完成任务A，则走路径1”），出口行为将基于这些条件来决定是否选择该路径。
     * 2. 路径选择：如果一个节点有多个出口路径，出口行为会根据设定的规则（比如条件判断、默认路径等）选择一个或多个路径进行流程转移。
     * 3. 并行与串行流程：在某些情况下，如果多个出口路径同时满足条件，出口行为可能会创建并行的执行路径，意味着流程会在几个方向上同时进行。
     *    相对的，也可能只选择一个路径，使流程串行地继续。
     * 4. 异常处理：如果没有可用的出口路径（比如没有路径满足条件），出口行为可能包括触发异常或进行特定的错误处理。
     * 在Flowable这样的流程引擎中，出口行为是通过编程来实现的，确保流程按照预定的逻辑顺畅地转移。这对于保证业务流程的连贯性和有效性至关重要。
     */
    public void performDefaultOutgoingBehavior(ExecutionEntity activityExecution) {
        performOutgoingBehavior(activityExecution, true, false);
    }

    /**
     * 执行默认的BPMN 2.0出口行为（参见 {@link #performDefaultOutgoingBehavior(ExecutionEntity)}），但不检查出口序列流上的条件。
     * 这意味着无论是否有条件，都会选择每一个出口序列流来继续流程实例。在多个出口序列流的情况下，将创建多个并行的执行路径。
     */
    public void performIgnoreConditionsOutgoingBehavior(ExecutionEntity activityExecution) {
        performOutgoingBehavior(activityExecution, false, false);
    }

    /**
     * 实际实现离开一个活动的行为。
     *
     * @param execution 当前执行上下文
     * @param checkConditions 是否在决定是否采取转换之前检查条件。
     * @param throwExceptionIfExecutionStuck 如果为真，在找不到离开活动的转换时会抛出 {@link FlowableException}。
     */
    protected void performOutgoingBehavior(ExecutionEntity execution, boolean checkConditions, boolean throwExceptionIfExecutionStuck) {
        CommandContextUtil.getAgenda().planTakeOutgoingSequenceFlowsOperation(execution, true);
    }

}
