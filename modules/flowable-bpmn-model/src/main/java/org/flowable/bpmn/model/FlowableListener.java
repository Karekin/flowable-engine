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
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * FlowableListener 类表示在流程执行过程中的特定事件上配置的监听器。
 * 这些监听器可以用于执行一些附加的自定义逻辑，如任务完成时发送通知。
 */
public class FlowableListener extends BaseElement implements HasScriptInfo {

    // 监听器触发的事件类型，如 'create'、'complete' 等
    protected String event;

    // 监听器实现的类型，如 'class'、'expression'、'delegateExpression' 或 'script'
    protected String implementationType;

    // 实现的具体内容，取决于 implementationType 的值
    protected String implementation;

    // 字段扩展列表，允许为监听器提供额外的参数
    protected List<FieldExtension> fieldExtensions = new ArrayList<>();

    // 与事务相关的监听器属性，定义监听器在事务的何种状态下被触发
    protected String onTransaction;

    // 自定义属性解析器的实现类型
    protected String customPropertiesResolverImplementationType;

    // 自定义属性解析器的具体实现
    protected String customPropertiesResolverImplementation;

    // 监听器的实例，可以直接设置，这样的话这个实例会被重复使用
    @JsonIgnore
    protected Object instance;

    // 如果实现类型是 'script'，这里会保存脚本的相关信息
    protected ScriptInfo scriptInfo;

    // 构造函数中生成一个随机的UUID作为监听器的唯一标识符
    public FlowableListener() {
        setId(UUID.randomUUID().toString());
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(String implementationType) {
        this.implementationType = implementationType;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public List<FieldExtension> getFieldExtensions() {
        return fieldExtensions;
    }

    public void setFieldExtensions(List<FieldExtension> fieldExtensions) {
        this.fieldExtensions = fieldExtensions;
    }

    public String getOnTransaction() {
        return onTransaction;
    }

    public void setOnTransaction(String onTransaction) {
        this.onTransaction = onTransaction;
    }

    public String getCustomPropertiesResolverImplementationType() {
        return customPropertiesResolverImplementationType;
    }

    public void setCustomPropertiesResolverImplementationType(String customPropertiesResolverImplementationType) {
        this.customPropertiesResolverImplementationType = customPropertiesResolverImplementationType;
    }

    public String getCustomPropertiesResolverImplementation() {
        return customPropertiesResolverImplementation;
    }

    public void setCustomPropertiesResolverImplementation(String customPropertiesResolverImplementation) {
        this.customPropertiesResolverImplementation = customPropertiesResolverImplementation;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    /**
     * Return the script info, if present.
     * <p>
     * ScriptInfo must be populated, when {@code <executionListener type="script" ...>} e.g. when
     * implementationType is 'script'.
     * </p>
     */
    @Override
    public ScriptInfo getScriptInfo() {
        return scriptInfo;
    }

    /**
     * Sets the script info
     *
     * @see #getScriptInfo()
     */
    @Override
    public void setScriptInfo(ScriptInfo scriptInfo) {
        this.scriptInfo = scriptInfo;
    }
    // 克隆监听器的方法，用于创建此监听器的副本
    @Override
    public FlowableListener clone() {
        FlowableListener clone = new FlowableListener();
        clone.setValues(this);
        return clone;
    }

    // 从另一个 FlowableListener 对象复制属性值的方法
    public void setValues(FlowableListener otherListener) {
        super.setValues(otherListener);
        setEvent(otherListener.getEvent());
        setImplementation(otherListener.getImplementation());
        setImplementationType(otherListener.getImplementationType());
        if (otherListener.getScriptInfo() != null) {
            setScriptInfo(otherListener.getScriptInfo().clone());
        }
        fieldExtensions = new ArrayList<>();
        if (otherListener.getFieldExtensions() != null && !otherListener.getFieldExtensions().isEmpty()) {
            for (FieldExtension extension : otherListener.getFieldExtensions()) {
                fieldExtensions.add(extension.clone());
            }
        }

        setOnTransaction(otherListener.getOnTransaction());
        setCustomPropertiesResolverImplementationType(otherListener.getCustomPropertiesResolverImplementationType());
        setCustomPropertiesResolverImplementation(otherListener.getCustomPropertiesResolverImplementation());
    }
}
