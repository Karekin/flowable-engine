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

/**
 * Gateway 抽象类代表了 BPMN 流程图中的网关节点。
 * 网关用于控制流程的分支和合并，基于一定的条件来决定流程的走向。
 * Gateway 是所有具体网关类型的基类，例如排他网关、并行网关等。

 * 在BPMN（业务流程模型和符号）中，网关是控制流程中分支和合并决策的元素。
 * 你可以把它们想象成流程图中的交通信号灯或路口指示牌，它们告诉流程在达到某个点时应该如何“决策”并选择接下来的路径。
 * 这里有几种常见的网关类型：
     * 1. **排他网关（Exclusive Gateway）**：就像多个道路交汇处的一个交通信号灯，根据流量（在这里指的是流程条件）的不同，排他网关只允许流程沿着一个方向流动。
     *    比如，如果审批金额超过1000元，流程就会转向“高级审批”路径；如果不超过，就会转向“快速审批”路径。
     * 2. **并行网关（Parallel Gateway）**：这就像打开了多个收费站的通道，所有车辆（在这里指的是流程的分支）都可以同时通过。
     *    并行网关用于同时启动多个流程路径，或者在这些并行路径都完成时再次汇聚。
     * 3. **包容网关（Inclusive Gateway）**：想象一个大型停车场的出入口，它可以同时允许多辆车进入和离开，但不一定是所有的车辆。
     *    包容网关可以基于条件选择多条路径，也可以选择所有的路径。
     * 4. **复杂网关（Complex Gateway）**：这就像一个有着多个传感器和控制器的高级交通管理系统，它可以根据复杂的规则和条件来决定流量的走向。
     *    复杂网关用于处理更复杂的分支和合并逻辑，这些逻辑通常无法通过其他网关类型来实现。
     * 5. **事件网关（Event Gateway）**：可以将其视为一种特殊类型的网关，它基于事件的发生来决定流程的路径。
     *    比如，它可能等待一个定时事件，当时间到达时，流程就会向特定的方向流动。
 * 网关在BPMN流程中非常重要，它们确保流程可以根据业务规则和逻辑正确地执行。
 */
public abstract class Gateway extends FlowNode {

    // 默认流属性，用于在网关分支条件都不满足时，提供一个默认的执行流程路径
    protected String defaultFlow;

    // 获取默认流属性的方法
    public String getDefaultFlow() {
        return defaultFlow;
    }

    // 设置默认流属性的方法
    public void setDefaultFlow(String defaultFlow) {
        this.defaultFlow = defaultFlow;
    }

    // 抽象方法，用于克隆网关节点
    @Override
    public abstract Gateway clone();

    // 设置这个网关的属性为另一个 Gateway 的属性
    // 该方法用于复制网关节点或在网关之间共享属性
    public void setValues(Gateway otherElement) {
        super.setValues(otherElement);
        setDefaultFlow(otherElement.getDefaultFlow());
    }
}
