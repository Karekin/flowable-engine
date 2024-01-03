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
package org.flowable.common.engine.api.repository;

import java.util.Date;
import java.util.Map;

/**
 * Represents a deployment that is already present in the engine repository.
 * A deployment is a container for resources such as process definitions, case definitions, images, forms, etc.
 * @author Tijs Rademakers

 * ### 设计模式
 * 1. 接口继承和抽象类：`AppDeploymentEntity`继承自`AppDeployment`和`Entity`接口，`AppDeploymentEntityImpl`实现`AppDeploymentEntity`。
 *    这是典型的接口继承和抽象类使用，允许多态性和代码复用。
 * 2. 模板方法模式：`AbstractEntityNoRevision`类可能实现了一些基本方法（如`getId`, `setId`等），这些方法在子类中被继承。
 *    这是模板方法设计模式的特征，其中抽象类定义了操作的骨架，而子类实现了这些操作的某些特定步骤。
 * 3. 策略模式：由于存在不同的部署类型（如BPMN、CMMN、DMN等），`EngineDeployment`的实现可能会被不同的策略类代替。
 *    这些策略类提供了相同接口的不同实现，这允许算法的变化独立于使用算法的客户端。

 * ### 设计此类结构的方法
 * - **定义明确的接口和抽象类**：确定哪些操作是基本的并应该在所有实现中提供，哪些操作是特定于特定类型的实体。
 * - **识别共享行为**：将共享行为抽取到抽象类中，以便多个子类可以继承。
 * - **使用组合和继承**：识别何时使用继承和何时使用组合。例如，`AppDeploymentEntityImpl`可以包含资源（组合），同时继承基本实体行为（继承）。
 * - **考虑扩展性**：设计接口和类时，考虑到将来可能的变化和扩展。例如，`EngineDeployment`接口
 */
public interface EngineDeployment {

    String getId();

    String getName();

    Date getDeploymentTime();

    String getCategory();

    String getKey();
    
    String getDerivedFrom();

    String getDerivedFromRoot();

    String getTenantId();
    
    String getEngineVersion();
    
    boolean isNew();

    Map<String, EngineResource> getResources();
}
