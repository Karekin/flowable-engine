package org.flowable.bpmn.model;

import java.util.Collection;
import java.util.Map;

/**
 * FlowElementsContainer 接口定义了一个容器，用于存储和管理 BPMN 流程图中的流程元素和工件。
 * 它是所有包含流程元素（如流程定义、子流程等）的类的基础接口。

 * 1. **SubProcess** : 这是一个通用的子流程，它自身是一个流程容器，可以包含其他流程元素，如任务、网关和事件。
 *    子流程是流程的一部分，它们可以在主流程中重复使用。
 * 2. **Transaction** : 这是特殊类型的子流程，模型化一个事务，其行为类似于数据库事务。
 *    事务子流程确保内部的所有活动要么全部完成，要么在出现错误时全部撤销。
 * 3. **EventSubProcess** : 这是一个在特定事件触发时开始执行的子流程。
 *    事件子流程不是流程的正常执行流的一部分，它可以在流程的任何点被触发。
 * 4. **AdhocSubProcess** : 这是一个非结构化的子流程，允许包含的活动没有严格的顺序。
 *    它用于建模不需要固定启动和结束点的活动集合。
 * 5. **Process** : 这代表了一个完整的BPMN流程，是最顶层的容器，包含一个完整的流程定义，可以包含子流程、任务、网关、事件以及其他流程元素。
 * 这些实现允许 Flowable 引擎创建复杂的流程结构，支持从简单的直线型流程到包含多层嵌套和复杂业务逻辑的流程。
 *    每种类型的 `FlowElementsContainer` 实现都有其特定用途和行为，以适应不同的业务需求和工作流程场景。
 */
public interface FlowElementsContainer {

    /**
     * 根据 ID 获取流程元素。
     *
     * @param id 流程元素的 ID。
     * @return 对应的流程元素，如果不存在则返回 null。
     */
    FlowElement getFlowElement(String id);

    /**
     * 获取所有流程元素的集合。
     *
     * @return 包含所有流程元素的集合。
     */
    Collection<FlowElement> getFlowElements();

    /**
     * 获取以 ID 为键的流程元素映射。
     *
     * @return 包含所有流程元素的映射。
     */
    Map<String, FlowElement> getFlowElementMap();

    /**
     * 向容器中添加一个流程元素。
     *
     * @param element 要添加的流程元素。
     */
    void addFlowElement(FlowElement element);

    /**
     * 将一个流程元素添加到映射中。
     *
     * @param element 要添加的流程元素。
     */
    void addFlowElementToMap(FlowElement element);

    /**
     * 根据元素 ID 从容器中移除一个流程元素。
     *
     * @param elementId 要移除的流程元素的 ID。
     */
    void removeFlowElement(String elementId);

    /**
     * 从映射中移除一个流程元素。
     *
     * @param elementId 要移除的流程元素的 ID。
     */
    void removeFlowElementFromMap(String elementId);

    /**
     * 根据 ID 获取工件。
     *
     * @param id 工件的 ID。
     * @return 对应的工件，如果不存在则返回 null。
     */
    Artifact getArtifact(String id);

    /**
     * 获取所有工件的集合。
     *
     * @return 包含所有工件的集合。
     */
    Collection<Artifact> getArtifacts();

    /**
     * 获取以 ID 为键的工件映射。
     *
     * @return 包含所有工件的映射。
     */
    Map<String, Artifact> getArtifactMap();

    /**
     * 向容器中添加一个工件。
     *
     * @param artifact 要添加的工件。
     */
    void addArtifact(Artifact artifact);

    /**
     * 将一个工件添加到映射中。
     *
     * @param artifact 要添加的工件。
     */
    void addArtifactToMap(Artifact artifact);

    /**
     * 根据工件 ID 从容器中移除一个工件。
     *
     * @param artifactId 要移除的工件的 ID。
     */
    void removeArtifact(String artifactId);

    /**
     * 获取容器的 ID。
     *
     * @return 容器的唯一标识符。
     */
    String getId();

}
