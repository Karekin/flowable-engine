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
 * MultiInstanceLoopCharacteristics 类表示 BPMN 流程图中的多实例循环特性。
 * 这些特性定义了一个活动如何重复执行，以及如何处理这些重复执行的实例。
 */
public class MultiInstanceLoopCharacteristics extends BaseElement {

    // 用于多实例循环的输入数据项
    protected String inputDataItem;

    // 一个表达式或变量，定义了包含多实例集合的字符串
    protected String collectionString;

    // 处理集合的处理器
    protected CollectionHandler collectionHandler;

    // 循环的基数，定义了多实例循环将执行的次数
    protected String loopCardinality;

    // 完成条件，定义了多实例循环何时停止
    protected String completionCondition;

    // 每个循环实例的元素变量
    protected String elementVariable;

    // 每个循环实例的索引变量
    protected String elementIndexVariable;

    // 指示循环是否顺序执行
    protected boolean sequential;

    // 指示在异步离开时是否没有等待状态
    protected boolean noWaitStatesAsyncLeave;

    // 变量聚合定义，定义了多实例循环结束时如何聚合变量
    protected VariableAggregationDefinitions aggregations;

    public String getInputDataItem() {
        return inputDataItem;
    }

    public void setInputDataItem(String inputDataItem) {
        this.inputDataItem = inputDataItem;
    }

    public String getCollectionString() {
        return collectionString;
    }

    public void setCollectionString(String collectionString) {
        this.collectionString = collectionString;
    }

    public CollectionHandler getHandler() {
		return collectionHandler;
	}

	public void setHandler(CollectionHandler collectionHandler) {
		this.collectionHandler = collectionHandler;
	}

	public String getLoopCardinality() {
        return loopCardinality;
    }

    public void setLoopCardinality(String loopCardinality) {
        this.loopCardinality = loopCardinality;
    }

    public String getCompletionCondition() {
        return completionCondition;
    }

    public void setCompletionCondition(String completionCondition) {
        this.completionCondition = completionCondition;
    }

    public String getElementVariable() {
        return elementVariable;
    }

    public void setElementVariable(String elementVariable) {
        this.elementVariable = elementVariable;
    }

    public String getElementIndexVariable() {
        return elementIndexVariable;
    }

    public void setElementIndexVariable(String elementIndexVariable) {
        this.elementIndexVariable = elementIndexVariable;
    }

    public boolean isSequential() {
        return sequential;
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public boolean isNoWaitStatesAsyncLeave() {
        return noWaitStatesAsyncLeave;
    }

    public void setNoWaitStatesAsyncLeave(boolean noWaitStatesAsyncLeave) {
        this.noWaitStatesAsyncLeave = noWaitStatesAsyncLeave;
    }

    public VariableAggregationDefinitions getAggregations() {
        return aggregations;
    }

    public void setAggregations(VariableAggregationDefinitions aggregations) {
        this.aggregations = aggregations;
    }

    public void addAggregation(VariableAggregationDefinition aggregation) {
        if (this.aggregations == null) {
            this.aggregations = new VariableAggregationDefinitions();
        }

        this.aggregations.getAggregations().add(aggregation);
    }

    @Override
    public MultiInstanceLoopCharacteristics clone() {
        MultiInstanceLoopCharacteristics clone = new MultiInstanceLoopCharacteristics();
        clone.setValues(this);
        return clone;
    }

    /**
     * 设置这个多实例循环特性对象的属性为另一个对象的属性。
     */
    public void setValues(MultiInstanceLoopCharacteristics otherLoopCharacteristics) {
        super.setValues(otherLoopCharacteristics);
        setInputDataItem(otherLoopCharacteristics.getInputDataItem());
        setCollectionString(otherLoopCharacteristics.getCollectionString());
        if (otherLoopCharacteristics.getHandler() != null) {
        	setHandler(otherLoopCharacteristics.getHandler().clone());
        }
        setLoopCardinality(otherLoopCharacteristics.getLoopCardinality());
        setCompletionCondition(otherLoopCharacteristics.getCompletionCondition());
        setElementVariable(otherLoopCharacteristics.getElementVariable());
        setElementIndexVariable(otherLoopCharacteristics.getElementIndexVariable());
        setSequential(otherLoopCharacteristics.isSequential());
        setNoWaitStatesAsyncLeave(otherLoopCharacteristics.isNoWaitStatesAsyncLeave());

        if (otherLoopCharacteristics.getAggregations() != null) {
            setAggregations(otherLoopCharacteristics.getAggregations().clone());
        }
    }
}
