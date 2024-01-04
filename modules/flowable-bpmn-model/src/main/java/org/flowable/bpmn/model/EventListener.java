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

/**
 * EventListener 类用于定义全局事件监听器。
 * 这些监听器可以在流程引擎中发生的特定事件时执行自定义的逻辑。
 */
public class EventListener extends BaseElement {

    // 要监听的事件类型，可以是一个特定事件或一组事件
    protected String events;

    // 监听器实现的类型，如 'class'、'expression'、'delegateExpression' 或 'script'
    protected String implementationType;

    // 实现的具体内容，取决于 implementationType 的值
    protected String implementation;

    // 与监听器关联的实体类型
    protected String entityType;

    // 与事务相关的属性，定义监听器在事务的何种状态下被触发
    protected String onTransaction;

    // 获取监听的事件类型
    public String getEvents() {
        return events;
    }

    // 设置监听的事件类型
    public void setEvents(String events) {
        this.events = events;
    }

    // 获取实现类型
    public String getImplementationType() {
        return implementationType;
    }

    // 设置实现类型
    public void setImplementationType(String implementationType) {
        this.implementationType = implementationType;
    }

    // 获取实现
    public String getImplementation() {
        return implementation;
    }

    // 设置实现
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }
    
    public String getOnTransaction() {
        return onTransaction;
    }

    // 设置事务状态
    public void setOnTransaction(String onTransaction) {
        this.onTransaction = onTransaction;
    }

    // 克隆事件监听器的方法
    @Override
    public EventListener clone() {
        EventListener clone = new EventListener();
        clone.setValues(this);
        return clone;
    }

    // 从另一个 EventListener 对象复制属性值的方法
    public void setValues(EventListener otherListener) {
        super.setValues(otherListener);
        setEvents(otherListener.getEvents());
        setImplementation(otherListener.getImplementation());
        setImplementationType(otherListener.getImplementationType());
        setEntityType(otherListener.getEntityType());
        setOnTransaction(otherListener.getOnTransaction());
    }
}
