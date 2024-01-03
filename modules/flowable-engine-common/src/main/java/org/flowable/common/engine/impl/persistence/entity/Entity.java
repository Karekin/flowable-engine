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
package org.flowable.common.engine.impl.persistence.entity;

/**
 * @author Tom Baeyens
 * @author Joram Barrez
 * @author Tijs Rademakers
 *
 * 这是一个基础接口，定义了实体的基本行为，如获取和设置ID、检查实体状态（插入、更新、删除）以及获取持久状态
 */
public interface Entity {

    String getId();

    void setId(String id);
    
    String getIdPrefix();

    boolean isInserted();

    void setInserted(boolean inserted);

    boolean isUpdated();

    void setUpdated(boolean updated);

    boolean isDeleted();

    void setDeleted(boolean deleted);

    /**
     * Returns a representation of the object, as would be stored in the database. Used when deciding if updates have occurred to the object or not since it was last loaded.
     */
    Object getPersistentState();
    
    Object getOriginalPersistentState();
    
    void setOriginalPersistentState(Object persistentState);
}
