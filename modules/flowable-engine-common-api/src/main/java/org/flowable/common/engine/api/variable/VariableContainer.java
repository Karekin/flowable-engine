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
package org.flowable.common.engine.api.variable;

/**
 * 一个通用接口，用于定义变量容器的操作。
 * 变量容器是存储流程变量的地方，这些变量可能是持久性的或者瞬态的（即仅在当前流程执行上下文中有效）。
 * 变量容器通常用于在流程实例执行期间传递数据和状态信息。
 *
 * @author Joram Barrez
 */
public interface VariableContainer {

    /**
     * 提供一个空的变量容器实例，这是一个null对象模式的应用。
     * 通常用于需要返回一个不含任何变量的容器场景，避免返回null造成的潜在错误。
     *
     * @return 一个空的变量容器实例。
     */
    public static VariableContainer empty(){
        return EmptyVariableContainer.INSTANCE;
    }

    /**
     * 检查容器中是否包含指定名称的变量。
     *
     * @param variableName 要检查的变量名称。
     * @return 如果变量存在则返回true，否则返回false。
     */
    boolean hasVariable(String variableName);

    /**
     * 根据变量名称获取变量值。
     *
     * @param variableName 要获取的变量名称。
     * @return 变量的值，如果不存在，则返回null。
     */
    Object getVariable(String variableName);

    /**
     * 设置变量的值，如果变量已经存在，则更新其值。
     *
     * @param variableName 要设置的变量名称。
     * @param variableValue 要设置的变量值。
     */
    void setVariable(String variableName, Object variableValue);

    /**
     * 设置一个瞬态变量的值，瞬态变量不会持久化到数据库中，仅在当前流程执行的上下文中有效。
     *
     * @param variableName 要设置的瞬态变量名称。
     * @param variableValue 要设置的瞬态变量值。
     */
    void setTransientVariable(String variableName, Object variableValue);

    /**
     * 获取租户的标识符。在多租户的系统中，租户标识符用于区分不同租户的数据。
     *
     * @return 租户的标识符，如果没有设置则可能返回null。
     */
    String getTenantId();

}
