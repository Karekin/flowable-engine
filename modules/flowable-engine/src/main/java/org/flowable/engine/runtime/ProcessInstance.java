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
package org.flowable.engine.runtime;

import java.util.Date;
import java.util.Map;

import org.flowable.engine.repository.ProcessDefinition;

/**
 * ProcessInstance 接口代表了一个流程定义{@link ProcessDefinition}的执行实例。
 * 它提供了访问流程实例属性和状态的方法。
 */
public interface ProcessInstance extends Execution {

    /**
     * 获取流程实例对应的流程定义ID。
     */
    String getProcessDefinitionId();

    /**
     * 获取流程实例对应的流程定义名称。
     */
    String getProcessDefinitionName();

    /**
     * 获取流程实例对应的流程定义关键字。
     */
    String getProcessDefinitionKey();

    /**
     * 获取流程实例对应的流程定义版本。
     */
    Integer getProcessDefinitionVersion();

    /**
     * 获取流程实例对应的流程定义的部署ID。
     */
    String getDeploymentId();

    /**
     * 获取流程实例的业务关键字。
     */
    String getBusinessKey();

    /**
     * 获取流程实例的业务状态。
     */
    String getBusinessStatus();

    /**
     * 返回流程实例是否被挂起。
     */
    @Override
    boolean isSuspended();

    /**
     * 如果在流程实例查询中请求，则返回流程变量。
     */
    Map<String, Object> getProcessVariables();

    /**
     * 获取流程实例的租户标识符。
     */
    @Override
    String getTenantId();

    /**
     * 返回流程实例的名称。
     */
    @Override
    String getName();

    /**
     * 返回流程实例的描述。
     */
    @Override
    String getDescription();

    /**
     * 返回流程实例的本地化名称。
     */
    String getLocalizedName();

    /**
     * 返回流程实例的本地化描述。
     */
    String getLocalizedDescription();

    /**
     * 返回流程实例的开始时间。
     */
    Date getStartTime();

    /**
     * 返回启动此流程实例的用户ID。
     */
    String getStartUserId();

    /**
     * 返回此流程实例的回调ID。
     */
    String getCallbackId();

    /**
     * 返回此流程实例的回调类型。
     */
    String getCallbackType();
}
