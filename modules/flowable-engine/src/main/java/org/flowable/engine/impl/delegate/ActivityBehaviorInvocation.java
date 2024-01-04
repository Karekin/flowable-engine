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
package org.flowable.engine.impl.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.invocation.DelegateInvocation;

/**
 * ActivityBehaviorInvocation 类用于封装对 ActivityBehavior 实例的调用。
 * 这个类实现了具体的行为调用逻辑，用于执行与BPMN活动相关联的行为。
 */
public class ActivityBehaviorInvocation extends DelegateInvocation {

    // 封装的 ActivityBehavior 实例，定义了BPMN活动的具体行为
    protected final ActivityBehavior behaviorInstance;

    // 流程执行对象，提供了当前流程执行的上下文
    protected final DelegateExecution execution;

    // 构造函数，初始化 ActivityBehavior 实例和流程执行对象
    public ActivityBehaviorInvocation(ActivityBehavior behaviorInstance, DelegateExecution execution) {
        this.behaviorInstance = behaviorInstance;
        this.execution = execution;
    }

    // 调用封装的 ActivityBehavior 实例的 execute 方法
    @Override
    protected void invoke() {
        behaviorInstance.execute(execution);
    }

    // 获取调用目标，即 ActivityBehavior 实例
    @Override
    public Object getTarget() {
        return behaviorInstance;
    }

}
