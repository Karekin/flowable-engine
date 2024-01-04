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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * BaseElement 类是 BPMN 流程定义中所有元素的基础抽象类。
 * 它定义了所有元素共有的基本属性，如id、扩展元素和属性。
 */
public abstract class BaseElement implements HasExtensionAttributes {

    // 元素的唯一标识符
    protected String id;

    // 元素在XML文档中的行号
    protected int xmlRowNumber;

    // 元素在XML文档中的列号
    protected int xmlColumnNumber;

    // 扩展元素的集合，这些是非标准的额外信息，可以附加到任何BPMN元素上
    protected Map<String, List<ExtensionElement>> extensionElements = new LinkedHashMap<>();

    // 扩展属性的集合，这些可以是任何BPMN元素的额外属性
    protected Map<String, List<ExtensionAttribute>> attributes = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getXmlRowNumber() {
        return xmlRowNumber;
    }

    public void setXmlRowNumber(int xmlRowNumber) {
        this.xmlRowNumber = xmlRowNumber;
    }

    public int getXmlColumnNumber() {
        return xmlColumnNumber;
    }

    public void setXmlColumnNumber(int xmlColumnNumber) {
        this.xmlColumnNumber = xmlColumnNumber;
    }

    public Map<String, List<ExtensionElement>> getExtensionElements() {
        return extensionElements;
    }

    public void addExtensionElement(ExtensionElement extensionElement) {
        if (extensionElement != null && StringUtils.isNotEmpty(extensionElement.getName())) {
            List<ExtensionElement> elementList = null;
            if (!this.extensionElements.containsKey(extensionElement.getName())) {
                elementList = new ArrayList<>();
                this.extensionElements.put(extensionElement.getName(), elementList);
            }
            this.extensionElements.get(extensionElement.getName()).add(extensionElement);
        }
    }

    public void setExtensionElements(Map<String, List<ExtensionElement>> extensionElements) {
        this.extensionElements = extensionElements;
    }

    @Override
    public Map<String, List<ExtensionAttribute>> getAttributes() {
        return attributes;
    }

    @Override
    public String getAttributeValue(String namespace, String name) {
        List<ExtensionAttribute> attributes = getAttributes().get(name);
        if (attributes != null && !attributes.isEmpty()) {
            for (ExtensionAttribute attribute : attributes) {
                if ((namespace == null && attribute.getNamespace() == null) || namespace.equals(attribute.getNamespace())) {
                    return attribute.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void addAttribute(ExtensionAttribute attribute) {
        if (attribute != null && StringUtils.isNotEmpty(attribute.getName())) {
            List<ExtensionAttribute> attributeList = null;
            if (!this.attributes.containsKey(attribute.getName())) {
                attributeList = new ArrayList<>();
                this.attributes.put(attribute.getName(), attributeList);
            }
            this.attributes.get(attribute.getName()).add(attribute);
        }
    }

    /**
     * 设置当前元素的所有扩展属性。
     */
    @Override
    public void setAttributes(Map<String, List<ExtensionAttribute>> attributes) {
        this.attributes = attributes;
    }

    /**
     * 复制另一个 BaseElement 的属性到当前元素。
     */
    public void setValues(BaseElement otherElement) {
        setId(otherElement.getId());

        extensionElements = new LinkedHashMap<>();
        if (otherElement.getExtensionElements() != null && !otherElement.getExtensionElements().isEmpty()) {
            for (String key : otherElement.getExtensionElements().keySet()) {
                List<ExtensionElement> otherElementList = otherElement.getExtensionElements().get(key);
                if (otherElementList != null && !otherElementList.isEmpty()) {
                    List<ExtensionElement> elementList = new ArrayList<>();
                    for (ExtensionElement extensionElement : otherElementList) {
                        elementList.add(extensionElement.clone());
                    }
                    extensionElements.put(key, elementList);
                }
            }
        }

        attributes = new LinkedHashMap<>();
        if (otherElement.getAttributes() != null && !otherElement.getAttributes().isEmpty()) {
            for (String key : otherElement.getAttributes().keySet()) {
                List<ExtensionAttribute> otherAttributeList = otherElement.getAttributes().get(key);
                if (otherAttributeList != null && !otherAttributeList.isEmpty()) {
                    List<ExtensionAttribute> attributeList = new ArrayList<>();
                    for (ExtensionAttribute extensionAttribute : otherAttributeList) {
                        attributeList.add(extensionAttribute.clone());
                    }
                    attributes.put(key, attributeList);
                }
            }
        }
    }

    /**
     * 克隆当前元素的方法，每个具体的子类都需要实现这个方法。
     */
    @Override
    public abstract BaseElement clone();
}
