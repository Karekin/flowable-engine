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

import java.util.List;
import java.util.Map;

/**
 * HasExtensionAttributes 接口用于访问和管理BPMN元素的扩展属性。
 * 扩展属性允许用户在BPMN元素上添加额外的信息，这些信息不是BPMN标准的一部分。
 */
public interface HasExtensionAttributes {

    /**
     * 获取元素的所有扩展属性。
     *
     * @return 包含扩展属性的Map，键是属性名称，值是包含扩展属性的列表。
     */
    Map<String, List<ExtensionAttribute>> getAttributes();

    /**
     * 根据命名空间和名称获取特定的属性值。
     *
     * @param namespace 命名空间URI，如果属性没有命名空间，则为null。
     * @param name 属性的本地名称。
     * @return 属性的值，如果找不到对应的属性，则返回null。

     * 命名空间在XML和与之相关的技术中是一个重要的概念，用于避免元素或属性名的冲突。
     * 它类似于在现实生活中的姓氏，有助于区分同名的不同人。在编程或标记语言中，命名空间用于区分具有相同名称但不同来源或意义的元素或属性。
     * 例如，在BPMN（业务流程模型和符号）模型中，可能会有来自不同组织或标准的扩展属性。
     * 为了区分这些属性，每个组织可以定义自己的命名空间URI（统一资源标识符），这样即使属性名称相同，由于它们属于不同的命名空间，也可以被视为完全不同的属性。
     * 命名空间URI通常看起来像网址，但它们实际上并不需要指向任何实际的网络位置，它们仅作为一个独一无二的标识符。
     * 在 `getAttributeValue` 方法的上下文中，如果你知道一个属性属于特定的命名空间，你可以使用这个命名空间URI和属性名称来准确地获取该属性的值。
     */
    String getAttributeValue(String namespace, String name);

    /**
     * 向元素添加一个扩展属性。
     *
     * @param attribute 要添加的扩展属性对象。
     */
    void addAttribute(ExtensionAttribute attribute);

    /**
     * 设置元素的所有扩展属性。
     *
     * @param attributes 包含要设置的扩展属性的Map。
     */
    void setAttributes(Map<String, List<ExtensionAttribute>> attributes);
}
