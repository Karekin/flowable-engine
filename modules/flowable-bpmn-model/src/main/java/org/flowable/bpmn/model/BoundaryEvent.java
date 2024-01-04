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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * BoundaryEvent 类表示附加到活动边界的事件。
 * 这种事件在活动执行期间发生，用于模型化例如错误处理或消息中断等情况。
 */
public class BoundaryEvent extends Event {

    @JsonIgnore
    protected Activity attachedToRef; // 该事件附加到的活动引用
    protected String attachedToRefId; // 该事件附加到的活动的ID
    protected boolean cancelActivity = true; // 当事件触发时，是否取消活动的执行

    // 获取附加到的活动引用
    public Activity getAttachedToRef() {
        return attachedToRef;
    }

    // 设置附加到的活动引用
    public void setAttachedToRef(Activity attachedToRef) {
        this.attachedToRef = attachedToRef;
    }

    // 获取附加到的活动ID
    public String getAttachedToRefId() {
        return attachedToRefId;
    }

    // 设置附加到的活动ID
    public void setAttachedToRefId(String attachedToRefId) {
        this.attachedToRefId = attachedToRefId;
    }

    // 判断事件触发时是否取消活动
    public boolean isCancelActivity() {
        return cancelActivity;
    }

    // 设置事件触发时是否取消活动
    public void setCancelActivity(boolean cancelActivity) {
        this.cancelActivity = cancelActivity;
    }

    // 克隆一个边界事件对象
    @Override
    public BoundaryEvent clone() {
        BoundaryEvent clone = new BoundaryEvent();
        clone.setValues(this);
        return clone;
    }

    // 从另一个 BoundaryEvent 对象复制属性值
    public void setValues(BoundaryEvent otherEvent) {
        super.setValues(otherEvent);
        setAttachedToRefId(otherEvent.getAttachedToRefId());
        setAttachedToRef(otherEvent.getAttachedToRef());
        setCancelActivity(otherEvent.isCancelActivity());
    }
}
