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
package org.flowable.bpmn.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * FlowNode 抽象类代表了 BPMN 流程图中的一个流程节点。
 * 它是所有流程节点（如任务、网关、事件等）的基础类，并提供了节点的基本属性和行为。
 */
public abstract class FlowNode extends FlowElement {

    // 表示节点是否是异步的
    protected boolean asynchronous;

    // 表示节点离开时是否是异步的
    protected boolean asynchronousLeave;

    // 表示节点是否是非独占的
    protected boolean notExclusive;

    // 节点的入口序列流列表
    protected List<SequenceFlow> incomingFlows = new ArrayList<>();

    // 节点的出口序列流列表
    protected List<SequenceFlow> outgoingFlows = new ArrayList<>();

    // 节点的行为对象，可以用来定义节点特定的行为
    @JsonIgnore
    protected Object behavior;

    public FlowNode() {

    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

    public void setAsynchronous(boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    public boolean isAsynchronousLeave() {
        return asynchronousLeave;
    }

    public void setAsynchronousLeave(boolean asynchronousLeave) {
        this.asynchronousLeave = asynchronousLeave;
    }

    public boolean isExclusive() {
        return !notExclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.notExclusive = !exclusive;
    }

    public boolean isNotExclusive() {
        return notExclusive;
    }

    public void setNotExclusive(boolean notExclusive) {
        this.notExclusive = notExclusive;
    }

    public Object getBehavior() {
        return behavior;
    }

    public void setBehavior(Object behavior) {
        this.behavior = behavior;
    }

    public List<SequenceFlow> getIncomingFlows() {
        return incomingFlows;
    }

    public void setIncomingFlows(List<SequenceFlow> incomingFlows) {
        this.incomingFlows = incomingFlows;
    }

    public List<SequenceFlow> getOutgoingFlows() {
        return outgoingFlows;
    }

    public void setOutgoingFlows(List<SequenceFlow> outgoingFlows) {
        this.outgoingFlows = outgoingFlows;
    }

    /**
     * 设置这个节点的属性为另一个 FlowNode 节点的属性。
     * 这个方法通常用于复制节点或在节点之间共享属性。

     * 这个 `setValues` 方法的作用是将一个 `FlowNode` （流程节点）的属性复制到另一个 `FlowNode` 上。
     * 这个方法常用于在BPMN流程模型中复制或共享节点的属性。让我们用一个简单的方式来解释这个方法：
     * 想象一下，在一个业务流程图中，你有一个特定的任务节点（比如审批任务），这个节点有一些特定的设置，比如它是异步的、非独占的，还有一些特定的入口和出口路径。
     * 现在，如果你想创建一个新的节点，这个新节点需要和原来的节点有完全相同的行为和路径设置。
     * 这时，你就可以使用 `setValues` 方法。你只需要传入原来的节点（`otherNode`），这个方法就会自动把所有的设置从原来的节点复制到新节点上。
     * 具体来说，它会做以下几件事情：
         * 1. **复制基本属性**：比如节点是否是异步的、是否非独占的等。
         * 2. **复制入口流**：即复制所有指向这个节点的路径。
         * 3. **复制出口流**：即复制所有从这个节点出发的路径。
     * 通过这样做，你就可以很容易地创建一个与原来节点行为完全一样的新节点，而不需要手动设置每一个属性。
     * 这在流程设计中尤其有用，因为它可以节省大量的时间和避免重复劳动。
     */
    public void setValues(FlowNode otherNode) {
        super.setValues(otherNode);
        setAsynchronous(otherNode.isAsynchronous());
        setNotExclusive(otherNode.isNotExclusive());
        setAsynchronousLeave(otherNode.isAsynchronousLeave());

        if (otherNode.getIncomingFlows() != null) {
            setIncomingFlows(otherNode.getIncomingFlows()
                    .stream()
                    .map(SequenceFlow::clone)
                    .collect(Collectors.toList()));
        }

        if (otherNode.getOutgoingFlows() != null) {
            setOutgoingFlows(otherNode.getOutgoingFlows()
                    .stream()
                    .map(SequenceFlow::clone)
                    .collect(Collectors.toList()));
        }
    }
}
