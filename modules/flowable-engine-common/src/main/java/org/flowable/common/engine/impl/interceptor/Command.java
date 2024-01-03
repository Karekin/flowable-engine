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
package org.flowable.common.engine.impl.interceptor;

/**
 * 这种写法体现了设计模式中的命令模式（Command Pattern）。
 * 命令模式的核心思想是将一个请求封装为一个对象，从而允许用户使用不同的请求、队列请求或者记录请求日志，以及支持可撤销的操作。
 * 这里的 `Command` 是一个泛型接口，它定义了一个方法 `execute`，这个方法接受一个 `CommandContext` 类型的参数。泛型 `<T>` 表示命令执行后的返回类型可以是任意类型。
 * 在命令模式中，每一个具体的命令都实现了 `Command` 接口，并在 `execute` 方法中实现具体的操作。
 * 这样的设计使得命令的发送者和接收者之间解耦，发送者只需要知道如何发送命令，而不需要知道命令的具体执行逻辑。
 * 在你提供的代码片段中，`protected Command<?> command;` 表示一个具体命令的引用，
 * 而 `Command<T>` 接口定义了所有具体命令类需要实现的 `execute` 方法，它们将在执行时接收一个 `CommandContext` 参数。

 * 这个设计模式通常用在以下场景：
 * - 当需要参数化对象根据要执行的动作来指定、排队和执行请求时。
 * - 当需要支持撤销操作时。
 * - 当需要支持操作的日志记录，并能够实现该操作的重放时。
 * 在流程引擎（如 Flowable）中，命令模式常用于封装流程引擎的各种操作，如启动流程、执行任务等，使得这些操作可以动态地被调度和执行，同时也便于扩展新的操作。
 * @param <T>
 */
public interface Command<T> {

    T execute(CommandContext commandContext);
}
