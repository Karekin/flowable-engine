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
package org.flowable.common.engine.impl.db;

/**
 * HasRevision 接口被用于实现乐观锁机制的实体。
 * 实现此接口的实体可以通过版本号（revision）来控制并发访问，以确保数据一致性。
 */
public interface HasRevision {

    /**
     * 设置实体的当前版本号。
     * 在更新实体时，此版本号用于确保没有其他并发操作修改了同一实体。
     */
    void setRevision(int revision);

    /**
     * 获取实体的当前版本号。
     * 该版本号反映了实体自上次更新以来的修改次数。
     */
    int getRevision();

    /**
     * 获取实体的下一个版本号。
     * 通常在实体更新时使用，以确保实体的版本号递增，从而有效实现乐观锁机制。
     */
    int getRevisionNext();

}
