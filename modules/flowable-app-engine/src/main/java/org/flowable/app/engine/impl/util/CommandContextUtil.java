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
package org.flowable.app.engine.impl.util;

import org.flowable.app.api.AppRepositoryService;
import org.flowable.app.engine.AppEngineConfiguration;
import org.flowable.app.engine.impl.persistence.entity.AppDefinitionEntityManager;
import org.flowable.app.engine.impl.persistence.entity.AppDeploymentEntityManager;
import org.flowable.app.engine.impl.persistence.entity.AppResourceEntityManager;
import org.flowable.common.engine.api.delegate.event.FlowableEventDispatcher;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.db.DbSqlSession;
import org.flowable.common.engine.impl.el.ExpressionManager;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.interceptor.EngineConfigurationConstants;
import org.flowable.common.engine.impl.persistence.cache.EntityCache;
import org.flowable.common.engine.impl.persistence.entity.TableDataManager;
import org.flowable.identitylink.service.IdentityLinkService;
import org.flowable.identitylink.service.IdentityLinkServiceConfiguration;
import org.flowable.idm.api.IdmEngineConfigurationApi;
import org.flowable.idm.api.IdmIdentityService;
import org.flowable.variable.service.VariableService;
import org.flowable.variable.service.VariableServiceConfiguration;

/**
 * @author Joram Barrez
 * @author Tijs Rademakers

 * CommandContextUtil 是 Flowable 工作流引擎中的一个实用工具类，它提供了一系列的静态方法，用来从当前的 CommandContext 中检索服务和管理类。
 * CommandContext 通常是与当前执行的命令（即事务）相关联的上下文对象。
 * 这个工具类的设计目的是为了方便开发者在不同层次的代码中访问这些服务和管理类，而不需要手动传递整个配置对象或者上下文对象。

 * CommandContextUtil 的设计
 * CommandContextUtil 的设计反映了几个软件设计原则和模式：
 * 1. **Facade 模式**：这个工具类作为 Flowable 服务和配置访问的 facade（门面），为外部代码提供了一个简单的接口，隐藏了访问复杂服务的内部逻辑。
 * 2. **Singleton 模式**：所有方法都是静态的，这意味着不需要创建实例就能使用这个工具类，它在内部管理对单一 `CommandContext` 的引用。
 * 3. **Dependency Injection (DI) 的替代**：虽然 Flowable 支持依赖注入，但在某些情况下，使用静态工具方法访问服务更方便。这种设计选择避免了在方法或类之间传递大量参数。
 * 4. **Information Expert 原则**：`CommandContextUtil` 将获取 Flowable 服务和配置的职责集中在一个地方，遵循了信息专家原则，即将信息和行为放在拥有完整信息的对象中。

 * 如何设计出 CommandContextUtil 这样的结构
 * 要设计出像 `CommandContextUtil` 这样的工具类，需要：
 * 1. **识别重复的代码和模式**：在你的应用程序中，找出那些经常重复的获取服务和管理类的代码。
 * 2. **抽象和封装**：将这些重复代码抽象成公共的方法，并封装在一个单独的工具类中。
 * 3. **提供全局访问点**：通过静态方法提供这些服务的访问，确保它们可以在应用程序的任何地方轻松访问。
 * 4. **确保线程安全和事务性**：如果你的工具类需要在多线程环境中使用，确保它的设计是线程安全的，并且在事务性上下文中正确工作。
 * 5. **优化性能**：如果需要，为了提高性能，可以缓存一些服务实例，但要确保缓存不会破坏事务的完整性和数据的一致性。
 * 在设计 `CommandContextUtil` 类型的工具时，要特别注意不要滥用全局静态方法，因为这可能会导致代码难以测试和维护。
 * 在现代应用程序设计中，通常推荐使用依赖注入框架来管理这些服务的依赖关系，但在特定的上下文和约束条件下，这样的工具类仍然非常有用。
 */
public class CommandContextUtil {

    public static final String ATTRIBUTE_INVOLVED_CASE_INSTANCE_IDS = "ctx.attribute.involvedCaseInstanceIds";

    public static AppEngineConfiguration getAppEngineConfiguration() {
        return getAppEngineConfiguration(getCommandContext());
    }

    public static AppEngineConfiguration getAppEngineConfiguration(CommandContext commandContext) {
        return (AppEngineConfiguration) commandContext.getEngineConfigurations().get(EngineConfigurationConstants.KEY_APP_ENGINE_CONFIG);
    }

    public static AppRepositoryService getAppRepositoryService() {
        return getAppEngineConfiguration().getAppRepositoryService();
    }

    public static ExpressionManager getExpressionManager() {
        return getExpressionManager(getCommandContext());
    }

    public static ExpressionManager getExpressionManager(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getExpressionManager();
    }
    
    public static FlowableEventDispatcher getEventDispatcher() {
        return getEventDispatcher(getCommandContext());
    }
    
    public static FlowableEventDispatcher getEventDispatcher(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getEventDispatcher();
    }

    public static AppDeploymentEntityManager getAppDeploymentEntityManager() {
        return getAppDeploymentEntityManager(getCommandContext());
    }

    public static AppDeploymentEntityManager getAppDeploymentEntityManager(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getAppDeploymentEntityManager();
    }

    public static AppResourceEntityManager getAppResourceEntityManager() {
        return getAppResourceEntityManager(getCommandContext());
    }

    public static AppResourceEntityManager getAppResourceEntityManager(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getAppResourceEntityManager();
    }

    public static AppDefinitionEntityManager getAppDefinitionEntityManager() {
        return getAppDefinitionEntityManager(getCommandContext());
    }

    public static AppDefinitionEntityManager getAppDefinitionEntityManager(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getAppDefinitionEntityManager();
    }

    public static TableDataManager getTableDataManager() {
        return getTableDataManager(getCommandContext());
    }

    public static TableDataManager getTableDataManager(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getTableDataManager();
    }

    public static VariableService getVariableService() {
        return getVariableService(getCommandContext());
    }

    public static VariableService getVariableService(CommandContext commandContext) {
        VariableService variableService = null;
        VariableServiceConfiguration variableServiceConfiguration = getVariableServiceConfiguration(commandContext);
        if (variableServiceConfiguration != null) {
            variableService = variableServiceConfiguration.getVariableService();
        }
        return variableService;
    }

    // IDM ENGINE

    public static IdmEngineConfigurationApi getIdmEngineConfiguration() {
        return getIdmEngineConfiguration(getCommandContext());
    }

    public static IdmEngineConfigurationApi getIdmEngineConfiguration(CommandContext commandContext) {
        return (IdmEngineConfigurationApi) commandContext.getEngineConfigurations().get(EngineConfigurationConstants.KEY_IDM_ENGINE_CONFIG);
    }

    public static IdmIdentityService getIdmIdentityService() {
        IdmIdentityService identityService = null;
        IdmEngineConfigurationApi idmEngineConfiguration = getIdmEngineConfiguration();
        if (idmEngineConfiguration != null) {
            identityService = idmEngineConfiguration.getIdmIdentityService();
        }

        return identityService;
    }

    public static IdentityLinkServiceConfiguration getIdentityLinkServiceConfiguration() {
        return getIdentityLinkServiceConfiguration(getCommandContext());
    }

    public static IdentityLinkServiceConfiguration getIdentityLinkServiceConfiguration(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getIdentityLinkServiceConfiguration();
    }

    public static IdentityLinkService getIdentityLinkService() {
        return getIdentityLinkService(getCommandContext());
    }

    public static IdentityLinkService getIdentityLinkService(CommandContext commandContext) {
        return getIdentityLinkServiceConfiguration(commandContext).getIdentityLinkService();
    }
    
    public static VariableServiceConfiguration getVariableServiceConfiguration() {
        return getVariableServiceConfiguration(getCommandContext());
    }

    public static VariableServiceConfiguration getVariableServiceConfiguration(CommandContext commandContext) {
        return getAppEngineConfiguration(commandContext).getVariableServiceConfiguration();
    }

    public static DbSqlSession getDbSqlSession() {
        return getDbSqlSession(getCommandContext());
    }

    public static DbSqlSession getDbSqlSession(CommandContext commandContext) {
        return commandContext.getSession(DbSqlSession.class);
    }

    public static EntityCache getEntityCache() {
        return getEntityCache(getCommandContext());
    }

    public static EntityCache getEntityCache(CommandContext commandContext) {
        return commandContext.getSession(EntityCache.class);
    }

    public static CommandContext getCommandContext() {
        return Context.getCommandContext();
    }

}
