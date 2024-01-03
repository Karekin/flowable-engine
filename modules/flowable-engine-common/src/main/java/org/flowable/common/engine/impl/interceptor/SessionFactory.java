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
 * A session factory produces a {@link Session} instance that has the lifespan of one {@link Command}.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez

 * `SessionFactory` 的设计思想体现了设计模式中的**工厂模式**（Factory Pattern），特别是**抽象工厂模式**（Abstract Factory Pattern）。
 * 工厂模式是用于创建对象的一种设计模式，它的目的是为了创建对象提供一个接口，让子类决定实例化哪一个类。工厂模式使得一个类的实例化延迟到其子类。
 * 在这个场景中，`SessionFactory` 接口充当了抽象工厂的角色，它定义了创建对象的接口 `openSession`，该方法用于生产 `Session` 对象。
 * `Session` 接口则定义了 `Session` 对象应该实现的方法，如 `flush` 和 `close`。具体的 `SessionFactory` 实现将决定如何创建和配置这些 `Session` 对象。
 * `sessionFactories` 是一个映射，它保存了与 `Session` 类型相关联的 `SessionFactory` 实例。这允许应用程序在运行时根据需要创建不同类型的 `Session` 对象。

 *  这个设计模式通常用在以下场景：
 *  - 当一个类不知道它所必须创建的对象的类的时候。
 *  - 当一个类希望由其子类来指定创建的对象时。
 *  - 当类将创建对象的职责委托给多个帮助类中的某一个，并且你希望将哪个帮助子类是代理者这一信息局部化时。
 *  在流程引擎中，如 Flowable，使用工厂模式来创建不同类型的会话可以让引擎灵活地扩展，支持不同类型的数据存储会话，例如，针对不同的数据库或持久化机制提供定制的会话实现。
 *  这样做的好处是，增加新的会话类型或改变现有会话的创建方式时，不需要修改到使用会话的代码，提高了代码的可维护性和可扩展性。
 */
public interface SessionFactory {

    Class<?> getSessionType();

    Session openSession(CommandContext commandContext);

}
