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
package org.flowable.identitylink.service.impl.persistence.entity;

import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.identitylink.api.IdentityLink;

/**
 * IdentityLinkEntity 用于表示流程参与者（如用户或用户组）与流程实例或任务之间的关系，比如谁是任务的拥有者、候选人或者参与者。
 * 这个接口扩展了 IdentityLink 和 Entity 接口，提供了一系列用于管理身份链接的方法。
 */
public interface IdentityLinkEntity extends IdentityLink, Entity {

    // 判断是否为用户类型的身份链接
    boolean isUser();

    // 判断是否为用户组类型的身份链接
    boolean isGroup();

    // 设置身份链接的类型（如所有者、候选人等）
    void setType(String type);

    // 设置与此身份链接关联的用户ID
    void setUserId(String userId);

    // 设置与此身份链接关联的用户组ID
    void setGroupId(String groupId);

    // 设置与此身份链接关联的任务ID
    void setTaskId(String taskId);

    // 设置与此身份链接关联的流程实例ID
    void setProcessInstanceId(String processInstanceId);

    // 获取与此身份链接关联的流程定义ID
    String getProcessDefId();

    // 设置与此身份链接关联的流程定义ID
    void setProcessDefId(String processDefId);

    // 设置与此身份链接关联的范围ID
    void setScopeId(String scopeId);

    // 设置与此身份链接关联的子范围ID
    void setSubScopeId(String subScopeId);

    // 设置与此身份链接关联的范围类型
    void setScopeType(String scopeType);

    // 设置与此身份链接关联的范围定义ID
    void setScopeDefinitionId(String scopeDefinitionId);

    // 获取与此身份链接关联的流程定义ID
    @Override
    String getProcessDefinitionId();

}
