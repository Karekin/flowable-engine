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
package org.flowable.engine.impl.delegate.invocation;

import org.flowable.engine.impl.interceptor.DelegateInterceptor;

/**
 * DelegateInvocation 抽象类提供了执行用户代码的上下文，并处理实际的调用。
 * 它是代理拦截器模式中的一部分，用于封装对用户定义逻辑的调用。
 *
 * @see DelegateInterceptor
 */
public abstract class DelegateInvocation {

    // 调用的结果，可能为 null（如果调用没有返回结果）
    protected Object invocationResult;

    // 调用的参数，可能为 null（如果调用没有参数）
    protected Object[] invocationParameters;

    /**
     * 使调用继续进行，执行用户代码的实际调用。
     * 此方法应在子类中被覆盖以实现特定逻辑。
     */
    public void proceed() {
        invoke();
    }

    // 抽象方法，子类应实现该方法以调用用户代码
    protected abstract void invoke();

    /**
     * 获取调用的结果。
     *
     * @return 调用的结果，如果调用没有返回结果，则为 null。
     */
    public Object getInvocationResult() {
        return invocationResult;
    }

    /**
     * 获取调用的参数。
     *
     * @return 调用的参数数组，如果调用没有参数，则为 null。
     */
    public Object[] getInvocationParameters() {
        return invocationParameters;
    }

    /**
     * 获取当前调用的目标，例如 JavaDelegate 或 ValueExpression。
     *
     * @return 当前调用的目标对象。
     */
    public abstract Object getTarget();

}
