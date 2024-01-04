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
 * AdhocSubProcess 类表示一个非结构化的子流程，其中包含的活动可以以任意顺序执行。
 * 这种子流程通常用于更灵活、不预定义路径的业务逻辑。
 */
public class AdhocSubProcess extends SubProcess {

    // 定义子流程活动的执行顺序为并行或序列
    public static final String ORDERING_PARALLEL = "Parallel";
    public static final String ORDERING_SEQUENTIALL = "Sequential";

    // 定义子流程完成的条件
    protected String completionCondition;

    // 子流程的执行顺序，可以是并行或序列
    protected String ordering = ORDERING_PARALLEL;

    // 如果一个活动完成或者流程触发了一个中断事件，是否取消其它还在执行的实例
    protected boolean cancelRemainingInstances = true;

    // 获取子流程完成条件的方法
    public String getCompletionCondition() {
        return completionCondition;
    }

    // 设置子流程完成条件的方法
    public void setCompletionCondition(String completionCondition) {
        this.completionCondition = completionCondition;
    }

    // 获取子流程的执行顺序的方法
    public String getOrdering() {
        return ordering;
    }

    // 设置子流程的执行顺序的方法
    public void setOrdering(String ordering) {
        this.ordering = ordering;
    }

    // 判断子流程的执行顺序是否为并行的方法
    public boolean hasParallelOrdering() {
        return !ORDERING_SEQUENTIALL.equals(ordering);
    }

    // 判断子流程的执行顺序是否为序列的方法
    public boolean hasSequentialOrdering() {
        return ORDERING_SEQUENTIALL.equals(ordering);
    }

    // 获取是否取消剩余实例的方法
    public boolean isCancelRemainingInstances() {
        return cancelRemainingInstances;
    }

    // 设置是否取消剩余实例的方法
    public void setCancelRemainingInstances(boolean cancelRemainingInstances) {
        this.cancelRemainingInstances = cancelRemainingInstances;
    }
}
