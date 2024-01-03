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
package org.flowable.common.engine.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.apache.ibatis.type.ArrayTypeHandler;
import org.apache.ibatis.type.BigDecimalTypeHandler;
import org.apache.ibatis.type.BlobInputStreamTypeHandler;
import org.apache.ibatis.type.BlobTypeHandler;
import org.apache.ibatis.type.BooleanTypeHandler;
import org.apache.ibatis.type.ByteTypeHandler;
import org.apache.ibatis.type.ClobTypeHandler;
import org.apache.ibatis.type.DateOnlyTypeHandler;
import org.apache.ibatis.type.DateTypeHandler;
import org.apache.ibatis.type.DoubleTypeHandler;
import org.apache.ibatis.type.FloatTypeHandler;
import org.apache.ibatis.type.IntegerTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.LongTypeHandler;
import org.apache.ibatis.type.NClobTypeHandler;
import org.apache.ibatis.type.NStringTypeHandler;
import org.apache.ibatis.type.ShortTypeHandler;
import org.apache.ibatis.type.SqlxmlTypeHandler;
import org.apache.ibatis.type.StringTypeHandler;
import org.apache.ibatis.type.TimeOnlyTypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventDispatcher;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.api.engine.EngineLifecycleListener;
import org.flowable.common.engine.impl.agenda.AgendaOperationRunner;
import org.flowable.common.engine.impl.cfg.CommandExecutorImpl;
import org.flowable.common.engine.impl.cfg.IdGenerator;
import org.flowable.common.engine.impl.cfg.TransactionContextFactory;
import org.flowable.common.engine.impl.cfg.standalone.StandaloneMybatisTransactionContextFactory;
import org.flowable.common.engine.impl.db.CommonDbSchemaManager;
import org.flowable.common.engine.impl.db.DbSqlSessionFactory;
import org.flowable.common.engine.impl.db.LogSqlExecutionTimePlugin;
import org.flowable.common.engine.impl.db.MybatisTypeAliasConfigurator;
import org.flowable.common.engine.impl.db.MybatisTypeHandlerConfigurator;
import org.flowable.common.engine.impl.db.SchemaManager;
import org.flowable.common.engine.impl.event.EventDispatchAction;
import org.flowable.common.engine.impl.event.FlowableEventDispatcherImpl;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandConfig;
import org.flowable.common.engine.impl.interceptor.CommandContextFactory;
import org.flowable.common.engine.impl.interceptor.CommandContextInterceptor;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.flowable.common.engine.impl.interceptor.CommandInterceptor;
import org.flowable.common.engine.impl.interceptor.CrDbRetryInterceptor;
import org.flowable.common.engine.impl.interceptor.DefaultCommandInvoker;
import org.flowable.common.engine.impl.interceptor.LogInterceptor;
import org.flowable.common.engine.impl.interceptor.SessionFactory;
import org.flowable.common.engine.impl.interceptor.TransactionContextInterceptor;
import org.flowable.common.engine.impl.lock.LockManager;
import org.flowable.common.engine.impl.lock.LockManagerImpl;
import org.flowable.common.engine.impl.logging.LoggingListener;
import org.flowable.common.engine.impl.logging.LoggingSession;
import org.flowable.common.engine.impl.logging.LoggingSessionFactory;
import org.flowable.common.engine.impl.persistence.GenericManagerFactory;
import org.flowable.common.engine.impl.persistence.StrongUuidGenerator;
import org.flowable.common.engine.impl.persistence.cache.EntityCache;
import org.flowable.common.engine.impl.persistence.cache.EntityCacheImpl;
import org.flowable.common.engine.impl.persistence.entity.ByteArrayEntityManager;
import org.flowable.common.engine.impl.persistence.entity.ByteArrayEntityManagerImpl;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.common.engine.impl.persistence.entity.PropertyEntityManager;
import org.flowable.common.engine.impl.persistence.entity.PropertyEntityManagerImpl;
import org.flowable.common.engine.impl.persistence.entity.TableDataManager;
import org.flowable.common.engine.impl.persistence.entity.TableDataManagerImpl;
import org.flowable.common.engine.impl.persistence.entity.data.ByteArrayDataManager;
import org.flowable.common.engine.impl.persistence.entity.data.PropertyDataManager;
import org.flowable.common.engine.impl.persistence.entity.data.impl.MybatisByteArrayDataManager;
import org.flowable.common.engine.impl.persistence.entity.data.impl.MybatisPropertyDataManager;
import org.flowable.common.engine.impl.runtime.Clock;
import org.flowable.common.engine.impl.service.CommonEngineServiceImpl;
import org.flowable.common.engine.impl.util.DefaultClockImpl;
import org.flowable.common.engine.impl.util.IoUtil;
import org.flowable.common.engine.impl.util.ReflectUtil;
import org.flowable.eventregistry.api.EventRegistryEventConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
/**
 * 这是 Flowable 流程引擎配置的抽象基类，它提供了构建和初始化流程引擎所需的基本服务和配置。
 * 它包括了数据源的设置，MyBatis SQL会话工厂的配置，事务管理器，命令拦截器和其他核心组件。
 * AbstractEngineConfiguration 被不同的流程引擎实现类所继承和扩展：
     * AppEngineConfiguration（应用引擎配置）
     * CmmnEngineConfiguration（案例管理模型引擎配置）
     * DmnEngineConfiguration（决策模型引擎配置）
     * EventRegistryEngineConfiguration（事件注册引擎配置）
     * FormEngineConfiguration（表单引擎配置）
     * IdmEngineConfiguration（身份管理引擎配置）
     * ProcessEngineConfiguration（流程引擎配置）
 * 每个实现类代表了流程引擎的一个特定方面或模块的配置。
 * 例如，ProcessEngineConfiguration是用于配置和初始化流程引擎本身的类，它可能包括数据库设置、事务管理、会话工厂和其他核心服务。
 * 而像FormEngineConfiguration则专注于与表单处理相关的配置。

 * 在面向对象编程中，这种设计模式允许开发者通过继承抽象基类并实现特定的方法来创建具有特定功能的配置类。这样做的好处包括：
     * 代码复用：AbstractEngineConfiguration中公共的、通用的配置代码可以被所有继承它的子类复用。
     * 可扩展性：可以容易地为流程引擎添加新的配置类来支持新的功能，而不必修改现有的代码。
     * 隔离性：每个特定的流程引擎配置类只关心它需要管理的特定部分，这使得管理和维护变得更加简单。

 * 通过这种方式，Flowable流程引擎可以提供灵活、可扩展且维护性好的配置管理，同时也允许开发者根据特定的需求定制和扩展流程引擎的功能。

 * 这种设计模式是“工厂方法模式”（Factory Method）和“模板方法模式”（Template Method）的结合。
 * 1. 工厂方法模式：AbstractEngineConfiguration 中定义了一些创建对象的方法，这些方法的具体实现将被推迟到其子类中。
 *    例如，可能有一个方法用于创建数据访问对象（Data Access Object, DAO），而每个具体的配置类
 *    （如 `ProcessEngineConfiguration` 或 `FormEngineConfiguration`）将提供这些方法的实现。
 * 2. 模板方法模式：AbstractEngineConfiguration 类中可能还包含了一些算法的骨架，这些算法定义了执行的顺序，但具体的步骤则留给子类去实现。
 *    这就是模板方法模式的典型应用，它允许子类在不改变算法结构的情况下重新定义算法的某些步骤。
 * 通过继承 `AbstractEngineConfiguration`，不同的配置类可以实现自己特定的配置细节，同时保持了通用配置处理的一致性。
 * 这种设计支持了高度的可配置性和扩展性，同时还保持了代码的整洁和可维护性。
 */
public abstract class AbstractEngineConfiguration {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // 特殊常量定义
    public static final String NO_TENANT_ID = "";

    // 数据库架构更新常量
    public static final String DB_SCHEMA_UPDATE_FALSE = "false";
    public static final String DB_SCHEMA_UPDATE_CREATE = "create";
    public static final String DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";
    public static final String DB_SCHEMA_UPDATE_DROP_CREATE = "drop-create";
    public static final String DB_SCHEMA_UPDATE_TRUE = "true";

    protected boolean forceCloseMybatisConnectionPool = true;

    // 数据源和数据库配置
    protected String databaseType;
    protected String jdbcDriver = "org.h2.Driver";
    protected String jdbcUrl = "jdbc:h2:tcp://localhost/~/flowable";
    protected String jdbcUsername = "sa";
    protected String jdbcPassword = "";
    protected String dataSourceJndiName;
    protected int jdbcMaxActiveConnections = 16;
    protected int jdbcMaxIdleConnections = 8;
    protected int jdbcMaxCheckoutTime;
    protected int jdbcMaxWaitTime;
    protected boolean jdbcPingEnabled;
    protected String jdbcPingQuery;
    protected int jdbcPingConnectionNotUsedFor;
    protected int jdbcDefaultTransactionIsolationLevel;
    protected DataSource dataSource;
    protected SchemaManager commonSchemaManager;
    protected SchemaManager schemaManager;
    protected Command<Void> schemaManagementCmd;

    protected String databaseSchemaUpdate = DB_SCHEMA_UPDATE_FALSE;

    // 是否使用锁更新数据库架构
    protected boolean useLockForDatabaseSchemaUpdate = false;

    protected String xmlEncoding = "UTF-8";

    // 命令执行器配置
    protected CommandExecutor commandExecutor;
    // 命令拦截器配置
    protected Collection<? extends CommandInterceptor> defaultCommandInterceptors;
    protected CommandConfig defaultCommandConfig;
    protected CommandConfig schemaCommandConfig;
    protected CommandContextFactory commandContextFactory;
    protected CommandInterceptor commandInvoker;

    protected AgendaOperationRunner agendaOperationRunner = (commandContext, runnable) -> runnable.run();

    protected List<CommandInterceptor> customPreCommandInterceptors;
    protected List<CommandInterceptor> customPostCommandInterceptors;
    protected List<CommandInterceptor> commandInterceptors;

    // 系统引擎配置，可以在多引擎中共享
    protected Map<String, AbstractEngineConfiguration> engineConfigurations = new HashMap<>();
    protected Map<String, AbstractServiceConfiguration> serviceConfigurations = new HashMap<>();

    protected ClassLoader classLoader;

    // 使用 Class.forName 或 ClassLoader.loadClass 进行类加载的标志
    protected boolean useClassForNameClassLoading = true;

    // 生命周期监听器
    protected List<EngineLifecycleListener> engineLifecycleListeners;

    // 事件注册
    protected Map<String, EventRegistryEventConsumer> eventRegistryEventConsumers = new HashMap<>();

    // MyBatis SQL会话工厂配置
    protected boolean isDbHistoryUsed = true;
    protected DbSqlSessionFactory dbSqlSessionFactory;
    protected SqlSessionFactory sqlSessionFactory;
    protected TransactionFactory transactionFactory;
    protected TransactionContextFactory transactionContextFactory;

    // 是否启用批量插入
    protected boolean isBulkInsertEnabled = true;

    // 批量插入语句的最大数量配置
    protected int maxNrOfStatementsInBulkInsert = 100;

    // 默认最大批量插入语句数量对于SQL Server数据库
    public int DEFAULT_MAX_NR_OF_STATEMENTS_BULK_INSERT_SQL_SERVER = 55;

    protected String mybatisMappingFile;
    protected Set<Class<?>> customMybatisMappers;
    protected Set<String> customMybatisXMLMappers;
    protected List<Interceptor> customMybatisInterceptors;

    protected Set<String> dependentEngineMyBatisXmlMappers;
    protected List<MybatisTypeAliasConfigurator> dependentEngineMybatisTypeAliasConfigs;
    protected List<MybatisTypeHandlerConfigurator> dependentEngineMybatisTypeHandlerConfigs;

    // 会话工厂配置
    protected List<SessionFactory> customSessionFactories;
    protected Map<Class<?>, SessionFactory> sessionFactories;

    protected boolean enableEventDispatcher = true;
    protected FlowableEventDispatcher eventDispatcher;
    protected List<FlowableEventListener> eventListeners;
    protected Map<String, List<FlowableEventListener>> typedEventListeners;
    protected List<EventDispatchAction> additionalEventDispatchActions;

    protected LoggingListener loggingListener;

    protected boolean transactionsExternallyManaged;

    // 标记是否使用关系数据库
    protected boolean usingRelationalDatabase = true;

    // 是否使用架构管理
    protected boolean usingSchemaMgmt = true;

    // 数据库表前缀配置
    protected String databaseTablePrefix = "";

    // 用于数据库通配符搜索的转义字符
    protected String databaseWildcardEscapeCharacter;

    // 数据库目录配置
    protected String databaseCatalog = "";

    // 数据库架构配置
    protected String databaseSchema;

    // 表前缀是否作为架构
    protected boolean tablePrefixIsSchema;

    // 是否总是检索最新的定义版本
    protected boolean alwaysLookupLatestDefinitionVersion;

    // 是否在默认情况下回退到默认租户
    protected boolean fallbackToDefaultTenant;

    /**
     * 【默认租户提供者配置】
     * Lambda表达式提供了一种简洁的方式来实现只有一个方法的接口，这种接口被称为函数式接口（functional interface）
     * DefaultTenantProvider 是一个函数式接口，它定义了一个方法，该方法接受三个参数：tenantId，scope，和scopeKey，并且返回一个字符串。
     * Lambda表达式的右侧是该方法的实现，它简单地返回了常量 NO_TENANT_ID，这通常表示这个上下文中没有租户ID。
     * 整个表达式创建了一个 DefaultTenantProvider 的实例，这个实例的方法会忽略它接收到的任何参数，并且总是返回 NO_TENANT_ID。
     * 这样的实现通常用在多租户应用中，当你想要提供一个默认实现，它不会特定于任何租户时。
     */
    protected DefaultTenantProvider defaultTenantProvider = (tenantId, scope, scopeKey) -> NO_TENANT_ID;

    // 是否启用 MyBatis 插件来记录 SQL 语句的执行时间
    protected boolean enableLogSqlExecutionTime;

    // 默认数据库类型映射配置
    protected Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

    // 锁检查间隔
    protected Duration lockPollRate = Duration.ofSeconds(10);

    // 架构锁等待时间
    protected Duration schemaLockWaitTime = Duration.ofMinutes(5);

    // 数据管理器配置
    protected PropertyDataManager propertyDataManager;
    protected ByteArrayDataManager byteArrayDataManager;
    protected TableDataManager tableDataManager;

    // 实体管理器配置
    protected PropertyEntityManager propertyEntityManager;
    protected ByteArrayEntityManager byteArrayEntityManager;

    // 自定义部署器配置
    protected List<EngineDeployer> customPreDeployers;
    protected List<EngineDeployer> customPostDeployers;
    protected List<EngineDeployer> deployers;

    // 配置器配置
    protected boolean enableConfiguratorServiceLoader = true;
    protected List<EngineConfigurator> configurators;
    protected List<EngineConfigurator> allConfigurators;
    protected EngineConfigurator idmEngineConfigurator;
    protected EngineConfigurator eventRegistryConfigurator;

    // 数据库产品名称常量
    public static final String PRODUCT_NAME_POSTGRES = "PostgreSQL";
    public static final String PRODUCT_NAME_CRDB = "CockroachDB";

    // 数据库类型常量
    public static final String DATABASE_TYPE_H2 = "h2";
    public static final String DATABASE_TYPE_HSQL = "hsql";
    public static final String DATABASE_TYPE_MYSQL = "mysql";
    public static final String DATABASE_TYPE_ORACLE = "oracle";
    public static final String DATABASE_TYPE_POSTGRES = "postgres";
    public static final String DATABASE_TYPE_MSSQL = "mssql";
    public static final String DATABASE_TYPE_DB2 = "db2";
    public static final String DATABASE_TYPE_COCKROACHDB = "cockroachdb";
    /**
     * 默认数据库类型映射配置，用于映射不同数据库产品到Flowable支持的数据库类型。
     */
    public static Properties getDefaultDatabaseTypeMappings() {
        Properties databaseTypeMappings = new Properties();
        databaseTypeMappings.setProperty("H2", DATABASE_TYPE_H2);
        databaseTypeMappings.setProperty("HSQL Database Engine", DATABASE_TYPE_HSQL);
        databaseTypeMappings.setProperty("MySQL", DATABASE_TYPE_MYSQL);
        databaseTypeMappings.setProperty("MariaDB", DATABASE_TYPE_MYSQL);
        databaseTypeMappings.setProperty("Oracle", DATABASE_TYPE_ORACLE);
        databaseTypeMappings.setProperty(PRODUCT_NAME_POSTGRES, DATABASE_TYPE_POSTGRES);
        databaseTypeMappings.setProperty("Microsoft SQL Server", DATABASE_TYPE_MSSQL);
        databaseTypeMappings.setProperty(DATABASE_TYPE_DB2, DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/NT", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/NT64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2 UDP", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUX", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUX390", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUXX8664", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUXZ64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUXPPC64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUXPPC64LE", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/400 SQL", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/6000", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2 UDB iSeries", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/AIX64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/HPUX", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/HP64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/SUN", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/SUN64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/PTX", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/2", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2 UDB AS400", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty(PRODUCT_NAME_CRDB, DATABASE_TYPE_COCKROACHDB);
        return databaseTypeMappings;
    }

    protected Map<Object, Object> beans;

    protected IdGenerator idGenerator;
    protected boolean usePrefixId;

    protected Clock clock;
    protected ObjectMapper objectMapper;

    // Variables

    public static final int DEFAULT_GENERIC_MAX_LENGTH_STRING = 4000;
    public static final int DEFAULT_ORACLE_MAX_LENGTH_STRING = 2000;

    /**
     * 定义在数据库中存储String类型变量的最大长度。主要用于Oracle数据库的NVARCHAR2限制为2000个字符
     */
    protected int maxLengthStringVariableType = -1;
    // 初始化引擎配置
    protected void initEngineConfigurations() {
        addEngineConfiguration(getEngineCfgKey(), getEngineScopeType(), this);
    }

    // DataSource
    // ///////////////////////////////////////////////////////////////
    // 初始化数据源配置
    protected void initDataSource() {
        if (dataSource == null) {
            if (dataSourceJndiName != null) {
                try {
                    dataSource = (DataSource) new InitialContext().lookup(dataSourceJndiName);
                } catch (Exception e) {
                    throw new FlowableException("couldn't lookup datasource from " + dataSourceJndiName + ": " + e.getMessage(), e);
                }

            } else if (jdbcUrl != null) {
                if ((jdbcDriver == null) || (jdbcUsername == null)) {
                    throw new FlowableException("DataSource or JDBC properties have to be specified in a process engine configuration");
                }

                logger.debug("initializing datasource to db: {}", jdbcUrl);

                if (logger.isInfoEnabled()) {
                    logger.info("Configuring Datasource with following properties (omitted password for security)");
                    logger.info("datasource driver : {}", jdbcDriver);
                    logger.info("datasource url : {}", jdbcUrl);
                    logger.info("datasource user name : {}", jdbcUsername);
                }

                PooledDataSource pooledDataSource = new PooledDataSource(this.getClass().getClassLoader(), jdbcDriver, jdbcUrl, jdbcUsername, jdbcPassword);

                if (jdbcMaxActiveConnections > 0) {
                    pooledDataSource.setPoolMaximumActiveConnections(jdbcMaxActiveConnections);
                }
                if (jdbcMaxIdleConnections > 0) {
                    pooledDataSource.setPoolMaximumIdleConnections(jdbcMaxIdleConnections);
                }
                if (jdbcMaxCheckoutTime > 0) {
                    pooledDataSource.setPoolMaximumCheckoutTime(jdbcMaxCheckoutTime);
                }
                if (jdbcMaxWaitTime > 0) {
                    pooledDataSource.setPoolTimeToWait(jdbcMaxWaitTime);
                }
                if (jdbcPingEnabled) {
                    pooledDataSource.setPoolPingEnabled(true);
                    if (jdbcPingQuery != null) {
                        pooledDataSource.setPoolPingQuery(jdbcPingQuery);
                    }
                    pooledDataSource.setPoolPingConnectionsNotUsedFor(jdbcPingConnectionNotUsedFor);
                }
                if (jdbcDefaultTransactionIsolationLevel > 0) {
                    pooledDataSource.setDefaultTransactionIsolationLevel(jdbcDefaultTransactionIsolationLevel);
                }
                dataSource = pooledDataSource;
            }
        }

        if (databaseType == null) {
            initDatabaseType();
        }
    }

    public void initDatabaseType() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            logger.debug("database product name: '{}'", databaseProductName);

            // CRDB does not expose the version through the jdbc driver, so we need to fetch it through version().
            if (PRODUCT_NAME_POSTGRES.equalsIgnoreCase(databaseProductName)) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("select version() as version;");
                        ResultSet resultSet = preparedStatement.executeQuery()) {
                    String version = null;
                    if (resultSet.next()) {
                        version = resultSet.getString("version");
                    }

                    if (StringUtils.isNotEmpty(version) && version.toLowerCase().startsWith(PRODUCT_NAME_CRDB.toLowerCase())) {
                        databaseProductName = PRODUCT_NAME_CRDB;
                        logger.info("CockroachDB version '{}' detected", version);
                    }
                }
            }

            databaseType = databaseTypeMappings.getProperty(databaseProductName);
            if (databaseType == null) {
                throw new FlowableException("couldn't deduct database type from database product name '" + databaseProductName + "'");
            }
            logger.debug("using database type: {}", databaseType);

        } catch (SQLException e) {
            throw new RuntimeException("Exception while initializing Database connection", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Exception while closing the Database connection", e);
            }
        }

        // Special care for MSSQL, as it has a hard limit of 2000 params per statement (incl bulk statement).
        // Especially with executions, with 100 as default, this limit is passed.
        if (DATABASE_TYPE_MSSQL.equals(databaseType)) {
            maxNrOfStatementsInBulkInsert = DEFAULT_MAX_NR_OF_STATEMENTS_BULK_INSERT_SQL_SERVER;
        }
    }

    public void initSchemaManager() {
        if (this.commonSchemaManager == null) {
            this.commonSchemaManager = new CommonDbSchemaManager();
        }
    }

    // 初始化会话工厂
    public void addSessionFactory(SessionFactory sessionFactory) {
        sessionFactories.put(sessionFactory.getSessionType(), sessionFactory);
    }
    // 初始化命令上下文工厂
    public void initCommandContextFactory() {
        if (commandContextFactory == null) {
            commandContextFactory = new CommandContextFactory();
        }
    }
    // 初始化事务上下文工厂
    public void initTransactionContextFactory() {
        if (transactionContextFactory == null) {
            transactionContextFactory = new StandaloneMybatisTransactionContextFactory();
        }
    }
    // 初始化命令执行器相关配置
    public void initCommandExecutors() {
        initDefaultCommandConfig();
        initSchemaCommandConfig();
        initCommandInvoker();
        initCommandInterceptors();
        initCommandExecutor();
    }

    // 初始化默认命令配置
    public void initDefaultCommandConfig() {
        if (defaultCommandConfig == null) {
            defaultCommandConfig = new CommandConfig();
        }
    }
    // 初始化模式命令配置
    public void initSchemaCommandConfig() {
        if (schemaCommandConfig == null) {
            schemaCommandConfig = new CommandConfig();
        }
    }
    // 初始化命令调用器
    public void initCommandInvoker() {
        if (commandInvoker == null) {
            commandInvoker = new DefaultCommandInvoker();
        }
    }
    // 初始化命令拦截器
    public void initCommandInterceptors() {
        if (commandInterceptors == null) {
            commandInterceptors = new ArrayList<>();
            if (customPreCommandInterceptors != null) {
                commandInterceptors.addAll(customPreCommandInterceptors);
            }
            commandInterceptors.addAll(getDefaultCommandInterceptors());
            if (customPostCommandInterceptors != null) {
                commandInterceptors.addAll(customPostCommandInterceptors);
            }
            commandInterceptors.add(commandInvoker);
        }
    }
    // 获取默认命令拦截器集合
    public Collection<? extends CommandInterceptor> getDefaultCommandInterceptors() {
        if (defaultCommandInterceptors == null) {
            List<CommandInterceptor> interceptors = new ArrayList<>();
            // 添加日志拦截器，记录命令执行日志
            interceptors.add(new LogInterceptor());

            // 针对CockroachDB数据库添加重试拦截器
            if (DATABASE_TYPE_COCKROACHDB.equals(databaseType)) {
                interceptors.add(new CrDbRetryInterceptor());
            }

            // 添加事务拦截器
            CommandInterceptor transactionInterceptor = createTransactionInterceptor();
            if (transactionInterceptor != null) {
                interceptors.add(transactionInterceptor);
            }

            // 添加命令上下文拦截器
            if (commandContextFactory != null) {
                String engineCfgKey = getEngineCfgKey();
                CommandContextInterceptor commandContextInterceptor = new CommandContextInterceptor(commandContextFactory, 
                        classLoader, useClassForNameClassLoading, clock, objectMapper);
                engineConfigurations.put(engineCfgKey, this);
                commandContextInterceptor.setEngineCfgKey(engineCfgKey);
                commandContextInterceptor.setEngineConfigurations(engineConfigurations);
                interceptors.add(commandContextInterceptor);
            }

            // 如果配置了事务上下文工厂，添加事务上下文拦截器
            if (transactionContextFactory != null) {
                interceptors.add(new TransactionContextInterceptor(transactionContextFactory));
            }

            // 添加额外的命令拦截器
            List<CommandInterceptor> additionalCommandInterceptors = getAdditionalDefaultCommandInterceptors();
            if (additionalCommandInterceptors != null) {
                interceptors.addAll(additionalCommandInterceptors);
            }

            // 设置默认命令拦截器集合
            defaultCommandInterceptors = interceptors;
        }
        return defaultCommandInterceptors;
    }

    // 抽象方法：获取引擎配置键
    public abstract String getEngineCfgKey();

    // 抽象方法：获取引擎作用域类型
    public abstract String getEngineScopeType();

    // 添加额外的默认命令拦截器
    public List<CommandInterceptor> getAdditionalDefaultCommandInterceptors() {
        return null; // 可由子类覆盖实现
    }

    // 初始化命令执行器
    public void initCommandExecutor() {
        if (commandExecutor == null) {
            CommandInterceptor first = initInterceptorChain(commandInterceptors);
            commandExecutor = new CommandExecutorImpl(getDefaultCommandConfig(), first);
        }
    }
    // 初始化拦截器链
    public CommandInterceptor initInterceptorChain(List<CommandInterceptor> chain) {
        if (chain == null || chain.isEmpty()) {
            throw new FlowableException("invalid command interceptor chain configuration: " + chain);
        }
        for (int i = 0; i < chain.size() - 1; i++) {
            chain.get(i).setNext(chain.get(i + 1));
        }
        return chain.get(0);
    }

    // 抽象方法：创建事务拦截器
    public abstract CommandInterceptor createTransactionInterceptor();

    // 初始化Beans
    public void initBeans() {
        if (beans == null) {
            beans = new HashMap<>();
        }
    }

    // 初始化ID生成器
    public void initIdGenerator() {
        if (idGenerator == null) {
            idGenerator = new StrongUuidGenerator();
        }
    }

    public void initObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        }
    }
    // 初始化时钟
    public void initClock() {
        if (clock == null) {
            clock = new DefaultClockImpl();
        }
    }

    // Data managers ///////////////////////////////////////////////////////////

    public void initDataManagers() {
        if (propertyDataManager == null) {
            propertyDataManager = new MybatisPropertyDataManager(idGenerator);
        }

        if (byteArrayDataManager == null) {
            byteArrayDataManager = new MybatisByteArrayDataManager(idGenerator);
        }
    }

    // Entity managers //////////////////////////////////////////////////////////

    public void initEntityManagers() {
        if (propertyEntityManager == null) {
            propertyEntityManager = new PropertyEntityManagerImpl(this, propertyDataManager);
        }

        if (byteArrayEntityManager == null) {
            byteArrayEntityManager = new ByteArrayEntityManagerImpl(byteArrayDataManager, getEngineCfgKey(), this::getEventDispatcher);
        }

        if (tableDataManager == null) {
            tableDataManager = new TableDataManagerImpl(this);
        }
    }

    // services
    // /////////////////////////////////////////////////////////////////

    protected void initService(Object service) {
        if (service instanceof CommonEngineServiceImpl) {
            ((CommonEngineServiceImpl) service).setCommandExecutor(commandExecutor);
        }
    }

    // 初始化MyBatis SQL会话工厂
    // ////////////////////////////////////////////////
    public void initSessionFactories() {
        if (sessionFactories == null) {
            sessionFactories = new HashMap<>();

            if (usingRelationalDatabase) {
                initDbSqlSessionFactory();
            }

            addSessionFactory(new GenericManagerFactory(EntityCache.class, EntityCacheImpl.class));
            
            if (isLoggingSessionEnabled()) {
                if (!sessionFactories.containsKey(LoggingSession.class)) {
                    LoggingSessionFactory loggingSessionFactory = new LoggingSessionFactory();
                    loggingSessionFactory.setLoggingListener(loggingListener);
                    loggingSessionFactory.setObjectMapper(objectMapper);
                    sessionFactories.put(LoggingSession.class, loggingSessionFactory);
                }
            }
            
            commandContextFactory.setSessionFactories(sessionFactories);
            
        } else {
            if (usingRelationalDatabase) {
                initDbSqlSessionFactoryEntitySettings();
            }
        }

        if (customSessionFactories != null) {
            for (SessionFactory sessionFactory : customSessionFactories) {
                addSessionFactory(sessionFactory);
            }
        }
    }

    public void initDbSqlSessionFactory() {
        if (dbSqlSessionFactory == null) {
            dbSqlSessionFactory = createDbSqlSessionFactory();
        }
        dbSqlSessionFactory.setDatabaseType(databaseType);
        dbSqlSessionFactory.setSqlSessionFactory(sqlSessionFactory);
        dbSqlSessionFactory.setDbHistoryUsed(isDbHistoryUsed);
        dbSqlSessionFactory.setDatabaseTablePrefix(databaseTablePrefix);
        dbSqlSessionFactory.setTablePrefixIsSchema(tablePrefixIsSchema);
        dbSqlSessionFactory.setDatabaseCatalog(databaseCatalog);
        dbSqlSessionFactory.setDatabaseSchema(databaseSchema);
        dbSqlSessionFactory.setMaxNrOfStatementsInBulkInsert(maxNrOfStatementsInBulkInsert);

        initDbSqlSessionFactoryEntitySettings();

        addSessionFactory(dbSqlSessionFactory);
    }

    public DbSqlSessionFactory createDbSqlSessionFactory() {
        return new DbSqlSessionFactory(usePrefixId);
    }

    protected abstract void initDbSqlSessionFactoryEntitySettings();

    protected void defaultInitDbSqlSessionFactoryEntitySettings(List<Class<? extends Entity>> insertOrder, List<Class<? extends Entity>> deleteOrder) {
        if (insertOrder != null) {
            for (Class<? extends Entity> clazz : insertOrder) {
                dbSqlSessionFactory.getInsertionOrder().add(clazz);
    
                if (isBulkInsertEnabled) {
                    dbSqlSessionFactory.getBulkInserteableEntityClasses().add(clazz);
                }
            }
        }

        if (deleteOrder != null) {
            for (Class<? extends Entity> clazz : deleteOrder) {
                dbSqlSessionFactory.getDeletionOrder().add(clazz);
            }
        }
    }

    public void initTransactionFactory() {
        if (transactionFactory == null) {
            if (transactionsExternallyManaged) {
                transactionFactory = new ManagedTransactionFactory();
                Properties properties = new Properties();
                properties.put("closeConnection", "false");
                this.transactionFactory.setProperties(properties);
            } else {
                transactionFactory = new JdbcTransactionFactory();
            }
        }
    }

    public void initSqlSessionFactory() {
        if (sqlSessionFactory == null) {
            InputStream inputStream = null;
            try {
                inputStream = getMyBatisXmlConfigurationStream();

                Environment environment = new Environment("default", transactionFactory, dataSource);
                Reader reader = new InputStreamReader(inputStream);
                Properties properties = new Properties();
                properties.put("prefix", databaseTablePrefix);

                String wildcardEscapeClause = "";
                if ((databaseWildcardEscapeCharacter != null) && (databaseWildcardEscapeCharacter.length() != 0)) {
                    wildcardEscapeClause = " escape '" + databaseWildcardEscapeCharacter + "'";
                }
                properties.put("wildcardEscapeClause", wildcardEscapeClause);

                // set default properties
                properties.put("limitBefore", "");
                properties.put("limitAfter", "");
                properties.put("limitBetween", "");
                properties.put("limitBeforeNativeQuery", "");
                properties.put("limitAfterNativeQuery", "");
                properties.put("blobType", "BLOB");
                properties.put("boolValue", "TRUE");

                if (databaseType != null) {
                    properties.load(getResourceAsStream(pathToEngineDbProperties()));
                }

                Configuration configuration = initMybatisConfiguration(environment, reader, properties);
                sqlSessionFactory = new DefaultSqlSessionFactory(configuration);

            } catch (Exception e) {
                throw new FlowableException("Error while building ibatis SqlSessionFactory: " + e.getMessage(), e);
            } finally {
                IoUtil.closeSilently(inputStream);
            }
        } else {
            // This is needed when the SQL Session Factory is created by another engine.
            // When custom XML Mappers are registered with this engine they need to be loaded in the configuration as well
            applyCustomMybatisCustomizations(sqlSessionFactory.getConfiguration());
        }
    }

    public String pathToEngineDbProperties() {
        return "org/flowable/common/db/properties/" + databaseType + ".properties";
    }

    public Configuration initMybatisConfiguration(Environment environment, Reader reader, Properties properties) {
        XMLConfigBuilder parser = new XMLConfigBuilder(reader, "", properties);
        Configuration configuration = parser.getConfiguration();

        if (databaseType != null) {
            configuration.setDatabaseId(databaseType);
        }

        configuration.setEnvironment(environment);

        initMybatisTypeHandlers(configuration);
        initCustomMybatisInterceptors(configuration);
        if (isEnableLogSqlExecutionTime()) {
            initMyBatisLogSqlExecutionTimePlugin(configuration);
        }

        configuration = parseMybatisConfiguration(parser);
        return configuration;
    }

    public void initCustomMybatisMappers(Configuration configuration) {
        if (getCustomMybatisMappers() != null) {
            for (Class<?> clazz : getCustomMybatisMappers()) {
                if (!configuration.hasMapper(clazz)) {
                    configuration.addMapper(clazz);
                }
            }
        }
    }

    public void initMybatisTypeHandlers(Configuration configuration) {
        // When mapping into Map<String, Object> there is currently a problem with MyBatis.
        // It will return objects which are driver specific.
        // Therefore we are registering the mappings between Object.class and the specific jdbc type here.
        // see https://github.com/mybatis/mybatis-3/issues/2216 for more info
        TypeHandlerRegistry handlerRegistry = configuration.getTypeHandlerRegistry();

        handlerRegistry.register(Object.class, JdbcType.BOOLEAN, new BooleanTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.BIT, new BooleanTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.TINYINT, new ByteTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.SMALLINT, new ShortTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.INTEGER, new IntegerTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.FLOAT, new FloatTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.DOUBLE, new DoubleTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.CHAR, new StringTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.CLOB, new ClobTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.VARCHAR, new StringTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.LONGVARCHAR, new StringTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.NVARCHAR, new NStringTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.NCHAR, new NStringTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.NCLOB, new NClobTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.ARRAY, new ArrayTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.BIGINT, new LongTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.REAL, new BigDecimalTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.DECIMAL, new BigDecimalTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.NUMERIC, new BigDecimalTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.BLOB, new BlobInputStreamTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.LONGVARBINARY, new BlobTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.DATE, new DateOnlyTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.TIME, new TimeOnlyTypeHandler());
        handlerRegistry.register(Object.class, JdbcType.TIMESTAMP, new DateTypeHandler());

        handlerRegistry.register(Object.class, JdbcType.SQLXML, new SqlxmlTypeHandler());
    }

    public void initCustomMybatisInterceptors(Configuration configuration) {
      if (customMybatisInterceptors!=null){
        for (Interceptor interceptor :customMybatisInterceptors){
            configuration.addInterceptor(interceptor);
        }
      }
    }

    public void initMyBatisLogSqlExecutionTimePlugin(Configuration configuration) {
        configuration.addInterceptor(new LogSqlExecutionTimePlugin());
    }

    public Configuration parseMybatisConfiguration(XMLConfigBuilder parser) {
        Configuration configuration = parser.parse();

        applyCustomMybatisCustomizations(configuration);
        return configuration;
    }

    protected void applyCustomMybatisCustomizations(Configuration configuration) {
        initCustomMybatisMappers(configuration);

        if (dependentEngineMybatisTypeAliasConfigs != null) {
            for (MybatisTypeAliasConfigurator typeAliasConfig : dependentEngineMybatisTypeAliasConfigs) {
                typeAliasConfig.configure(configuration.getTypeAliasRegistry());
            }
        }
        if (dependentEngineMybatisTypeHandlerConfigs != null) {
            for (MybatisTypeHandlerConfigurator typeHandlerConfig : dependentEngineMybatisTypeHandlerConfigs) {
                typeHandlerConfig.configure(configuration.getTypeHandlerRegistry());
            }
        }

        parseDependentEngineMybatisXMLMappers(configuration);
        parseCustomMybatisXMLMappers(configuration);
    }

    public void parseCustomMybatisXMLMappers(Configuration configuration) {
        if (getCustomMybatisXMLMappers() != null) {
            for (String resource : getCustomMybatisXMLMappers()) {
                parseMybatisXmlMapping(configuration, resource);
            }
        }
    }

    public void parseDependentEngineMybatisXMLMappers(Configuration configuration) {
        if (getDependentEngineMyBatisXmlMappers() != null) {
            for (String resource : getDependentEngineMyBatisXmlMappers()) {
                parseMybatisXmlMapping(configuration, resource);
            }
        }
    }

    protected void parseMybatisXmlMapping(Configuration configuration, String resource) {
        // see XMLConfigBuilder.mapperElement()
        XMLMapperBuilder mapperParser = new XMLMapperBuilder(getResourceAsStream(resource), configuration, resource, configuration.getSqlFragments());
        mapperParser.parse();
    }

    protected InputStream getResourceAsStream(String resource) {
        ClassLoader classLoader = getClassLoader();
        if (classLoader != null) {
            return getClassLoader().getResourceAsStream(resource);
        } else {
            return this.getClass().getClassLoader().getResourceAsStream(resource);
        }
    }

    public void setMybatisMappingFile(String file) {
        this.mybatisMappingFile = file;
    }

    public String getMybatisMappingFile() {
        return mybatisMappingFile;
    }

    public abstract InputStream getMyBatisXmlConfigurationStream();
    
    public void initConfigurators() {

        allConfigurators = new ArrayList<>();
        allConfigurators.addAll(getEngineSpecificEngineConfigurators());

        // Configurators that are explicitly added to the config
        if (configurators != null) {
            allConfigurators.addAll(configurators);
        }

        // Auto discovery through ServiceLoader
        if (enableConfiguratorServiceLoader) {
            ClassLoader classLoader = getClassLoader();
            if (classLoader == null) {
                classLoader = ReflectUtil.getClassLoader();
            }

            ServiceLoader<EngineConfigurator> configuratorServiceLoader = ServiceLoader.load(EngineConfigurator.class, classLoader);
            int nrOfServiceLoadedConfigurators = 0;
            for (EngineConfigurator configurator : configuratorServiceLoader) {
                allConfigurators.add(configurator);
                nrOfServiceLoadedConfigurators++;
            }

            if (nrOfServiceLoadedConfigurators > 0) {
                logger.info("Found {} auto-discoverable Process Engine Configurator{}", nrOfServiceLoadedConfigurators, nrOfServiceLoadedConfigurators > 1 ? "s" : "");
            }

            if (!allConfigurators.isEmpty()) {

                // Order them according to the priorities (useful for dependent
                // configurator)
                allConfigurators.sort(new Comparator<EngineConfigurator>() {

                    @Override
                    public int compare(EngineConfigurator configurator1, EngineConfigurator configurator2) {
                        int priority1 = configurator1.getPriority();
                        int priority2 = configurator2.getPriority();

                        if (priority1 < priority2) {
                            return -1;
                        } else if (priority1 > priority2) {
                            return 1;
                        }
                        return 0;
                    }
                });

                // Execute the configurators
                logger.info("Found {} Engine Configurators in total:", allConfigurators.size());
                for (EngineConfigurator configurator : allConfigurators) {
                    logger.info("{} (priority:{})", configurator.getClass(), configurator.getPriority());
                }

            }

        }
    }
    // 关闭流程引擎配置，清理资源
    public void close() {
        if (forceCloseMybatisConnectionPool && dataSource instanceof PooledDataSource) {
            /*
             * When the datasource is created by a Flowable engine (i.e. it's an instance of PooledDataSource),
             * the connection pool needs to be closed when closing the engine.
             * Note that calling forceCloseAll() multiple times (as is the case when running with multiple engine) is ok.
             */
            ((PooledDataSource) dataSource).forceCloseAll();
        }
    }

    protected List<EngineConfigurator> getEngineSpecificEngineConfigurators() {
        // meant to be overridden if needed
        return Collections.emptyList();
    }

    public void configuratorsBeforeInit() {
        for (EngineConfigurator configurator : allConfigurators) {
            logger.info("Executing beforeInit() of {} (priority:{})", configurator.getClass(), configurator.getPriority());
            configurator.beforeInit(this);
        }
    }
    
    public void configuratorsAfterInit() {
        for (EngineConfigurator configurator : allConfigurators) {
            logger.info("Executing configure() of {} (priority:{})", configurator.getClass(), configurator.getPriority());
            configurator.configure(this);
        }
    }

    public LockManager getLockManager(String lockName) {
        return new LockManagerImpl(commandExecutor, lockName, getLockPollRate(), getEngineCfgKey());
    }

    // getters and setters
    // //////////////////////////////////////////////////////

    public abstract String getEngineName();

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public AbstractEngineConfiguration setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public boolean isUseClassForNameClassLoading() {
        return useClassForNameClassLoading;
    }

    public AbstractEngineConfiguration setUseClassForNameClassLoading(boolean useClassForNameClassLoading) {
        this.useClassForNameClassLoading = useClassForNameClassLoading;
        return this;
    }

    public void addEngineLifecycleListener(EngineLifecycleListener engineLifecycleListener) {
        if (this.engineLifecycleListeners == null) {
            this.engineLifecycleListeners = new ArrayList<>();
        }
        this.engineLifecycleListeners.add(engineLifecycleListener);
    }

    public List<EngineLifecycleListener> getEngineLifecycleListeners() {
        return engineLifecycleListeners;
    }

    public AbstractEngineConfiguration setEngineLifecycleListeners(List<EngineLifecycleListener> engineLifecycleListeners) {
        this.engineLifecycleListeners = engineLifecycleListeners;
        return this;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public AbstractEngineConfiguration setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public AbstractEngineConfiguration setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public SchemaManager getSchemaManager() {
        return schemaManager;
    }

    public AbstractEngineConfiguration setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        return this;
    }

    public SchemaManager getCommonSchemaManager() {
        return commonSchemaManager;
    }

    public AbstractEngineConfiguration setCommonSchemaManager(SchemaManager commonSchemaManager) {
        this.commonSchemaManager = commonSchemaManager;
        return this;
    }
    
    public Command<Void> getSchemaManagementCmd() {
        return schemaManagementCmd;
    }

    public AbstractEngineConfiguration setSchemaManagementCmd(Command<Void> schemaManagementCmd) {
        this.schemaManagementCmd = schemaManagementCmd;
        return this;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public AbstractEngineConfiguration setJdbcDriver(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public AbstractEngineConfiguration setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public AbstractEngineConfiguration setJdbcUsername(String jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
        return this;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public AbstractEngineConfiguration setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
        return this;
    }

    public int getJdbcMaxActiveConnections() {
        return jdbcMaxActiveConnections;
    }

    public AbstractEngineConfiguration setJdbcMaxActiveConnections(int jdbcMaxActiveConnections) {
        this.jdbcMaxActiveConnections = jdbcMaxActiveConnections;
        return this;
    }

    public int getJdbcMaxIdleConnections() {
        return jdbcMaxIdleConnections;
    }

    public AbstractEngineConfiguration setJdbcMaxIdleConnections(int jdbcMaxIdleConnections) {
        this.jdbcMaxIdleConnections = jdbcMaxIdleConnections;
        return this;
    }

    public int getJdbcMaxCheckoutTime() {
        return jdbcMaxCheckoutTime;
    }

    public AbstractEngineConfiguration setJdbcMaxCheckoutTime(int jdbcMaxCheckoutTime) {
        this.jdbcMaxCheckoutTime = jdbcMaxCheckoutTime;
        return this;
    }

    public int getJdbcMaxWaitTime() {
        return jdbcMaxWaitTime;
    }

    public AbstractEngineConfiguration setJdbcMaxWaitTime(int jdbcMaxWaitTime) {
        this.jdbcMaxWaitTime = jdbcMaxWaitTime;
        return this;
    }

    public boolean isJdbcPingEnabled() {
        return jdbcPingEnabled;
    }

    public AbstractEngineConfiguration setJdbcPingEnabled(boolean jdbcPingEnabled) {
        this.jdbcPingEnabled = jdbcPingEnabled;
        return this;
    }

    public int getJdbcPingConnectionNotUsedFor() {
        return jdbcPingConnectionNotUsedFor;
    }

    public AbstractEngineConfiguration setJdbcPingConnectionNotUsedFor(int jdbcPingConnectionNotUsedFor) {
        this.jdbcPingConnectionNotUsedFor = jdbcPingConnectionNotUsedFor;
        return this;
    }

    public int getJdbcDefaultTransactionIsolationLevel() {
        return jdbcDefaultTransactionIsolationLevel;
    }

    public AbstractEngineConfiguration setJdbcDefaultTransactionIsolationLevel(int jdbcDefaultTransactionIsolationLevel) {
        this.jdbcDefaultTransactionIsolationLevel = jdbcDefaultTransactionIsolationLevel;
        return this;
    }

    public String getJdbcPingQuery() {
        return jdbcPingQuery;
    }

    public AbstractEngineConfiguration setJdbcPingQuery(String jdbcPingQuery) {
        this.jdbcPingQuery = jdbcPingQuery;
        return this;
    }

    public String getDataSourceJndiName() {
        return dataSourceJndiName;
    }

    public AbstractEngineConfiguration setDataSourceJndiName(String dataSourceJndiName) {
        this.dataSourceJndiName = dataSourceJndiName;
        return this;
    }

    public CommandConfig getSchemaCommandConfig() {
        return schemaCommandConfig;
    }

    public AbstractEngineConfiguration setSchemaCommandConfig(CommandConfig schemaCommandConfig) {
        this.schemaCommandConfig = schemaCommandConfig;
        return this;
    }

    public boolean isTransactionsExternallyManaged() {
        return transactionsExternallyManaged;
    }

    public AbstractEngineConfiguration setTransactionsExternallyManaged(boolean transactionsExternallyManaged) {
        this.transactionsExternallyManaged = transactionsExternallyManaged;
        return this;
    }

    public Map<Object, Object> getBeans() {
        return beans;
    }

    public AbstractEngineConfiguration setBeans(Map<Object, Object> beans) {
        this.beans = beans;
        return this;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public AbstractEngineConfiguration setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        return this;
    }

    public boolean isUsePrefixId() {
        return usePrefixId;
    }

    public AbstractEngineConfiguration setUsePrefixId(boolean usePrefixId) {
        this.usePrefixId = usePrefixId;
        return this;
    }

    public String getXmlEncoding() {
        return xmlEncoding;
    }

    public AbstractEngineConfiguration setXmlEncoding(String xmlEncoding) {
        this.xmlEncoding = xmlEncoding;
        return this;
    }

    public CommandConfig getDefaultCommandConfig() {
        return defaultCommandConfig;
    }

    public AbstractEngineConfiguration setDefaultCommandConfig(CommandConfig defaultCommandConfig) {
        this.defaultCommandConfig = defaultCommandConfig;
        return this;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public AbstractEngineConfiguration setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
        return this;
    }

    public CommandContextFactory getCommandContextFactory() {
        return commandContextFactory;
    }

    public AbstractEngineConfiguration setCommandContextFactory(CommandContextFactory commandContextFactory) {
        this.commandContextFactory = commandContextFactory;
        return this;
    }

    public CommandInterceptor getCommandInvoker() {
        return commandInvoker;
    }

    public AbstractEngineConfiguration setCommandInvoker(CommandInterceptor commandInvoker) {
        this.commandInvoker = commandInvoker;
        return this;
    }

    public AgendaOperationRunner getAgendaOperationRunner() {
        return agendaOperationRunner;
    }

    public AbstractEngineConfiguration setAgendaOperationRunner(AgendaOperationRunner agendaOperationRunner) {
        this.agendaOperationRunner = agendaOperationRunner;
        return this;
    }

    public List<CommandInterceptor> getCustomPreCommandInterceptors() {
        return customPreCommandInterceptors;
    }

    public AbstractEngineConfiguration setCustomPreCommandInterceptors(List<CommandInterceptor> customPreCommandInterceptors) {
        this.customPreCommandInterceptors = customPreCommandInterceptors;
        return this;
    }

    public List<CommandInterceptor> getCustomPostCommandInterceptors() {
        return customPostCommandInterceptors;
    }

    public AbstractEngineConfiguration setCustomPostCommandInterceptors(List<CommandInterceptor> customPostCommandInterceptors) {
        this.customPostCommandInterceptors = customPostCommandInterceptors;
        return this;
    }

    public List<CommandInterceptor> getCommandInterceptors() {
        return commandInterceptors;
    }

    public AbstractEngineConfiguration setCommandInterceptors(List<CommandInterceptor> commandInterceptors) {
        this.commandInterceptors = commandInterceptors;
        return this;
    }

    public Map<String, AbstractEngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }

    public AbstractEngineConfiguration setEngineConfigurations(Map<String, AbstractEngineConfiguration> engineConfigurations) {
        this.engineConfigurations = engineConfigurations;
        return this;
    }

    public void addEngineConfiguration(String key, String scopeType, AbstractEngineConfiguration engineConfiguration) {
        if (engineConfigurations == null) {
            engineConfigurations = new HashMap<>();
        }
        engineConfigurations.put(key, engineConfiguration);
        engineConfigurations.put(scopeType, engineConfiguration);
    }

    public Map<String, AbstractServiceConfiguration> getServiceConfigurations() {
        return serviceConfigurations;
    }

    public AbstractEngineConfiguration setServiceConfigurations(Map<String, AbstractServiceConfiguration> serviceConfigurations) {
        this.serviceConfigurations = serviceConfigurations;
        return this;
    }

    public void addServiceConfiguration(String key, AbstractServiceConfiguration serviceConfiguration) {
        if (serviceConfigurations == null) {
            serviceConfigurations = new HashMap<>();
        }
        serviceConfigurations.put(key, serviceConfiguration);
    }

    public Map<String, EventRegistryEventConsumer> getEventRegistryEventConsumers() {
        return eventRegistryEventConsumers;
    }

    public AbstractEngineConfiguration setEventRegistryEventConsumers(Map<String, EventRegistryEventConsumer> eventRegistryEventConsumers) {
        this.eventRegistryEventConsumers = eventRegistryEventConsumers;
        return this;
    }
    
    public void addEventRegistryEventConsumer(String key, EventRegistryEventConsumer eventRegistryEventConsumer) {
        if (eventRegistryEventConsumers == null) {
            eventRegistryEventConsumers = new HashMap<>();
        }
        eventRegistryEventConsumers.put(key, eventRegistryEventConsumer);
    }

    public AbstractEngineConfiguration setDefaultCommandInterceptors(Collection<? extends CommandInterceptor> defaultCommandInterceptors) {
        this.defaultCommandInterceptors = defaultCommandInterceptors;
        return this;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public AbstractEngineConfiguration setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
        return this;
    }

    public boolean isDbHistoryUsed() {
        return isDbHistoryUsed;
    }

    public AbstractEngineConfiguration setDbHistoryUsed(boolean isDbHistoryUsed) {
        this.isDbHistoryUsed = isDbHistoryUsed;
        return this;
    }

    public DbSqlSessionFactory getDbSqlSessionFactory() {
        return dbSqlSessionFactory;
    }

    public AbstractEngineConfiguration setDbSqlSessionFactory(DbSqlSessionFactory dbSqlSessionFactory) {
        this.dbSqlSessionFactory = dbSqlSessionFactory;
        return this;
    }

    public TransactionFactory getTransactionFactory() {
        return transactionFactory;
    }

    public AbstractEngineConfiguration setTransactionFactory(TransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        return this;
    }

    public TransactionContextFactory getTransactionContextFactory() {
        return transactionContextFactory;
    }

    public AbstractEngineConfiguration setTransactionContextFactory(TransactionContextFactory transactionContextFactory) {
        this.transactionContextFactory = transactionContextFactory;
        return this;
    }

    public int getMaxNrOfStatementsInBulkInsert() {
        return maxNrOfStatementsInBulkInsert;
    }

    public AbstractEngineConfiguration setMaxNrOfStatementsInBulkInsert(int maxNrOfStatementsInBulkInsert) {
        this.maxNrOfStatementsInBulkInsert = maxNrOfStatementsInBulkInsert;
        return this;
    }

    public boolean isBulkInsertEnabled() {
        return isBulkInsertEnabled;
    }

    public AbstractEngineConfiguration setBulkInsertEnabled(boolean isBulkInsertEnabled) {
        this.isBulkInsertEnabled = isBulkInsertEnabled;
        return this;
    }

    public Set<Class<?>> getCustomMybatisMappers() {
        return customMybatisMappers;
    }

    public AbstractEngineConfiguration setCustomMybatisMappers(Set<Class<?>> customMybatisMappers) {
        this.customMybatisMappers = customMybatisMappers;
        return this;
    }

    public Set<String> getCustomMybatisXMLMappers() {
        return customMybatisXMLMappers;
    }

    public AbstractEngineConfiguration setCustomMybatisXMLMappers(Set<String> customMybatisXMLMappers) {
        this.customMybatisXMLMappers = customMybatisXMLMappers;
        return this;
    }

    public Set<String> getDependentEngineMyBatisXmlMappers() {
        return dependentEngineMyBatisXmlMappers;
    }

    public AbstractEngineConfiguration setCustomMybatisInterceptors(List<Interceptor> customMybatisInterceptors) {
        this.customMybatisInterceptors = customMybatisInterceptors;
        return  this;
    }

    public List<Interceptor> getCustomMybatisInterceptors() {
        return customMybatisInterceptors;
    }

    public AbstractEngineConfiguration setDependentEngineMyBatisXmlMappers(Set<String> dependentEngineMyBatisXmlMappers) {
        this.dependentEngineMyBatisXmlMappers = dependentEngineMyBatisXmlMappers;
        return this;
    }

    public List<MybatisTypeAliasConfigurator> getDependentEngineMybatisTypeAliasConfigs() {
        return dependentEngineMybatisTypeAliasConfigs;
    }

    public AbstractEngineConfiguration setDependentEngineMybatisTypeAliasConfigs(List<MybatisTypeAliasConfigurator> dependentEngineMybatisTypeAliasConfigs) {
        this.dependentEngineMybatisTypeAliasConfigs = dependentEngineMybatisTypeAliasConfigs;
        return this;
    }

    public List<MybatisTypeHandlerConfigurator> getDependentEngineMybatisTypeHandlerConfigs() {
        return dependentEngineMybatisTypeHandlerConfigs;
    }

    public AbstractEngineConfiguration setDependentEngineMybatisTypeHandlerConfigs(List<MybatisTypeHandlerConfigurator> dependentEngineMybatisTypeHandlerConfigs) {
        this.dependentEngineMybatisTypeHandlerConfigs = dependentEngineMybatisTypeHandlerConfigs;
        return this;
    }

    public List<SessionFactory> getCustomSessionFactories() {
        return customSessionFactories;
    }

    public AbstractEngineConfiguration addCustomSessionFactory(SessionFactory sessionFactory) {
        if (customSessionFactories == null) {
            customSessionFactories = new ArrayList<>();
        }
        customSessionFactories.add(sessionFactory);
        return this;
    }

    public AbstractEngineConfiguration setCustomSessionFactories(List<SessionFactory> customSessionFactories) {
        this.customSessionFactories = customSessionFactories;
        return this;
    }

    public boolean isUsingRelationalDatabase() {
        return usingRelationalDatabase;
    }

    public AbstractEngineConfiguration setUsingRelationalDatabase(boolean usingRelationalDatabase) {
        this.usingRelationalDatabase = usingRelationalDatabase;
        return this;
    }
    
    public boolean isUsingSchemaMgmt() {
        return usingSchemaMgmt;
    }

    public AbstractEngineConfiguration setUsingSchemaMgmt(boolean usingSchema) {
        this.usingSchemaMgmt = usingSchema;
        return this;
    }

    public String getDatabaseTablePrefix() {
        return databaseTablePrefix;
    }

    public AbstractEngineConfiguration setDatabaseTablePrefix(String databaseTablePrefix) {
        this.databaseTablePrefix = databaseTablePrefix;
        return this;
    }

    public String getDatabaseWildcardEscapeCharacter() {
        return databaseWildcardEscapeCharacter;
    }

    public AbstractEngineConfiguration setDatabaseWildcardEscapeCharacter(String databaseWildcardEscapeCharacter) {
        this.databaseWildcardEscapeCharacter = databaseWildcardEscapeCharacter;
        return this;
    }

    public String getDatabaseCatalog() {
        return databaseCatalog;
    }

    public AbstractEngineConfiguration setDatabaseCatalog(String databaseCatalog) {
        this.databaseCatalog = databaseCatalog;
        return this;
    }

    public String getDatabaseSchema() {
        return databaseSchema;
    }

    public AbstractEngineConfiguration setDatabaseSchema(String databaseSchema) {
        this.databaseSchema = databaseSchema;
        return this;
    }

    public boolean isTablePrefixIsSchema() {
        return tablePrefixIsSchema;
    }

    public AbstractEngineConfiguration setTablePrefixIsSchema(boolean tablePrefixIsSchema) {
        this.tablePrefixIsSchema = tablePrefixIsSchema;
        return this;
    }

    public boolean isAlwaysLookupLatestDefinitionVersion() {
        return alwaysLookupLatestDefinitionVersion;
    }

    public AbstractEngineConfiguration setAlwaysLookupLatestDefinitionVersion(boolean alwaysLookupLatestDefinitionVersion) {
        this.alwaysLookupLatestDefinitionVersion = alwaysLookupLatestDefinitionVersion;
        return this;
    }

    public boolean isFallbackToDefaultTenant() {
        return fallbackToDefaultTenant;
    }

    public AbstractEngineConfiguration setFallbackToDefaultTenant(boolean fallbackToDefaultTenant) {
        this.fallbackToDefaultTenant = fallbackToDefaultTenant;
        return this;
    }

    /**
     * @return name of the default tenant
     * @deprecated use {@link AbstractEngineConfiguration#getDefaultTenantProvider()} instead
     */
    @Deprecated
    public String getDefaultTenantValue() {
        return getDefaultTenantProvider().getDefaultTenant(null, null, null);
    }

    public AbstractEngineConfiguration setDefaultTenantValue(String defaultTenantValue) {
        this.defaultTenantProvider = (tenantId, scope, scopeKey) -> defaultTenantValue;
        return this;
    }

    public DefaultTenantProvider getDefaultTenantProvider() {
        return defaultTenantProvider;
    }

    public AbstractEngineConfiguration setDefaultTenantProvider(DefaultTenantProvider defaultTenantProvider) {
        this.defaultTenantProvider = defaultTenantProvider;
        return this;
    }

    public boolean isEnableLogSqlExecutionTime() {
        return enableLogSqlExecutionTime;
    }

    public void setEnableLogSqlExecutionTime(boolean enableLogSqlExecutionTime) {
        this.enableLogSqlExecutionTime = enableLogSqlExecutionTime;
    }

    public Map<Class<?>, SessionFactory> getSessionFactories() {
        return sessionFactories;
    }

    public AbstractEngineConfiguration setSessionFactories(Map<Class<?>, SessionFactory> sessionFactories) {
        this.sessionFactories = sessionFactories;
        return this;
    }

    public String getDatabaseSchemaUpdate() {
        return databaseSchemaUpdate;
    }

    public AbstractEngineConfiguration setDatabaseSchemaUpdate(String databaseSchemaUpdate) {
        this.databaseSchemaUpdate = databaseSchemaUpdate;
        return this;
    }

    public boolean isUseLockForDatabaseSchemaUpdate() {
        return useLockForDatabaseSchemaUpdate;
    }

    public AbstractEngineConfiguration setUseLockForDatabaseSchemaUpdate(boolean useLockForDatabaseSchemaUpdate) {
        this.useLockForDatabaseSchemaUpdate = useLockForDatabaseSchemaUpdate;
        return this;
    }

    public boolean isEnableEventDispatcher() {
        return enableEventDispatcher;
    }

    public AbstractEngineConfiguration setEnableEventDispatcher(boolean enableEventDispatcher) {
        this.enableEventDispatcher = enableEventDispatcher;
        return this;
    }

    public FlowableEventDispatcher getEventDispatcher() {
        return eventDispatcher;
    }

    public AbstractEngineConfiguration setEventDispatcher(FlowableEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        return this;
    }

    public List<FlowableEventListener> getEventListeners() {
        return eventListeners;
    }

    public AbstractEngineConfiguration setEventListeners(List<FlowableEventListener> eventListeners) {
        this.eventListeners = eventListeners;
        return this;
    }

    public Map<String, List<FlowableEventListener>> getTypedEventListeners() {
        return typedEventListeners;
    }

    public AbstractEngineConfiguration setTypedEventListeners(Map<String, List<FlowableEventListener>> typedEventListeners) {
        this.typedEventListeners = typedEventListeners;
        return this;
    }

    public List<EventDispatchAction> getAdditionalEventDispatchActions() {
        return additionalEventDispatchActions;
    }

    public AbstractEngineConfiguration setAdditionalEventDispatchActions(List<EventDispatchAction> additionalEventDispatchActions) {
        this.additionalEventDispatchActions = additionalEventDispatchActions;
        return this;
    }

    public void initEventDispatcher() {
        if (this.eventDispatcher == null) {
            this.eventDispatcher = new FlowableEventDispatcherImpl();
        }

        initAdditionalEventDispatchActions();

        this.eventDispatcher.setEnabled(enableEventDispatcher);

        initEventListeners();
        initTypedEventListeners();
    }

    protected void initEventListeners() {
        if (eventListeners != null) {
            for (FlowableEventListener listenerToAdd : eventListeners) {
                this.eventDispatcher.addEventListener(listenerToAdd);
            }
        }
    }

    protected void initAdditionalEventDispatchActions() {
        if (this.additionalEventDispatchActions == null) {
            this.additionalEventDispatchActions = new ArrayList<>();
        }
    }

    protected void initTypedEventListeners() {
        if (typedEventListeners != null) {
            for (Map.Entry<String, List<FlowableEventListener>> listenersToAdd : typedEventListeners.entrySet()) {
                // Extract types from the given string
                FlowableEngineEventType[] types = FlowableEngineEventType.getTypesFromString(listenersToAdd.getKey());

                for (FlowableEventListener listenerToAdd : listenersToAdd.getValue()) {
                    this.eventDispatcher.addEventListener(listenerToAdd, types);
                }
            }
        }
    }

    public boolean isLoggingSessionEnabled() {
        return loggingListener != null;
    }
    
    public LoggingListener getLoggingListener() {
        return loggingListener;
    }

    public void setLoggingListener(LoggingListener loggingListener) {
        this.loggingListener = loggingListener;
    }

    public Clock getClock() {
        return clock;
    }

    public AbstractEngineConfiguration setClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public AbstractEngineConfiguration setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }

    public int getMaxLengthString() {
        if (maxLengthStringVariableType == -1) {
            if ("oracle".equalsIgnoreCase(databaseType)) {
                return DEFAULT_ORACLE_MAX_LENGTH_STRING;
            } else {
                return DEFAULT_GENERIC_MAX_LENGTH_STRING;
            }
        } else {
            return maxLengthStringVariableType;
        }
    }

    public int getMaxLengthStringVariableType() {
        return maxLengthStringVariableType;
    }

    public AbstractEngineConfiguration setMaxLengthStringVariableType(int maxLengthStringVariableType) {
        this.maxLengthStringVariableType = maxLengthStringVariableType;
        return this;
    }

    public PropertyDataManager getPropertyDataManager() {
        return propertyDataManager;
    }

    public Duration getLockPollRate() {
        return lockPollRate;
    }

    public AbstractEngineConfiguration setLockPollRate(Duration lockPollRate) {
        this.lockPollRate = lockPollRate;
        return this;
    }

    public Duration getSchemaLockWaitTime() {
        return schemaLockWaitTime;
    }

    public void setSchemaLockWaitTime(Duration schemaLockWaitTime) {
        this.schemaLockWaitTime = schemaLockWaitTime;
    }

    public AbstractEngineConfiguration setPropertyDataManager(PropertyDataManager propertyDataManager) {
        this.propertyDataManager = propertyDataManager;
        return this;
    }

    public PropertyEntityManager getPropertyEntityManager() {
        return propertyEntityManager;
    }

    public AbstractEngineConfiguration setPropertyEntityManager(PropertyEntityManager propertyEntityManager) {
        this.propertyEntityManager = propertyEntityManager;
        return this;
    }

    public ByteArrayDataManager getByteArrayDataManager() {
        return byteArrayDataManager;
    }

    public AbstractEngineConfiguration setByteArrayDataManager(ByteArrayDataManager byteArrayDataManager) {
        this.byteArrayDataManager = byteArrayDataManager;
        return this;
    }

    public ByteArrayEntityManager getByteArrayEntityManager() {
        return byteArrayEntityManager;
    }

    public AbstractEngineConfiguration setByteArrayEntityManager(ByteArrayEntityManager byteArrayEntityManager) {
        this.byteArrayEntityManager = byteArrayEntityManager;
        return this;
    }

    public TableDataManager getTableDataManager() {
        return tableDataManager;
    }

    public AbstractEngineConfiguration setTableDataManager(TableDataManager tableDataManager) {
        this.tableDataManager = tableDataManager;
        return this;
    }

    public List<EngineDeployer> getDeployers() {
        return deployers;
    }

    public AbstractEngineConfiguration setDeployers(List<EngineDeployer> deployers) {
        this.deployers = deployers;
        return this;
    }

    public List<EngineDeployer> getCustomPreDeployers() {
        return customPreDeployers;
    }

    public AbstractEngineConfiguration setCustomPreDeployers(List<EngineDeployer> customPreDeployers) {
        this.customPreDeployers = customPreDeployers;
        return this;
    }

    public List<EngineDeployer> getCustomPostDeployers() {
        return customPostDeployers;
    }

    public AbstractEngineConfiguration setCustomPostDeployers(List<EngineDeployer> customPostDeployers) {
        this.customPostDeployers = customPostDeployers;
        return this;
    }
    
    public boolean isEnableConfiguratorServiceLoader() {
        return enableConfiguratorServiceLoader;
    }
    
    public AbstractEngineConfiguration setEnableConfiguratorServiceLoader(boolean enableConfiguratorServiceLoader) {
        this.enableConfiguratorServiceLoader = enableConfiguratorServiceLoader;
        return this;
    }

    public List<EngineConfigurator> getConfigurators() {
        return configurators;
    }

    public AbstractEngineConfiguration addConfigurator(EngineConfigurator configurator) {
        if (configurators == null) {
            configurators = new ArrayList<>();
        }
        configurators.add(configurator);
        return this;
    }

    /**
     * @return All {@link EngineConfigurator} instances. Will only contain values after init of the engine.
     * Use the {@link #getConfigurators()} or {@link #addConfigurator(EngineConfigurator)} methods otherwise.
     */
    public List<EngineConfigurator> getAllConfigurators() {
        return allConfigurators;
    }

    public AbstractEngineConfiguration setConfigurators(List<EngineConfigurator> configurators) {
        this.configurators = configurators;
        return this;
    }

    public EngineConfigurator getIdmEngineConfigurator() {
        return idmEngineConfigurator;
    }

    public AbstractEngineConfiguration setIdmEngineConfigurator(EngineConfigurator idmEngineConfigurator) {
        this.idmEngineConfigurator = idmEngineConfigurator;
        return this;
    }

    public EngineConfigurator getEventRegistryConfigurator() {
        return eventRegistryConfigurator;
    }

    public AbstractEngineConfiguration setEventRegistryConfigurator(EngineConfigurator eventRegistryConfigurator) {
        this.eventRegistryConfigurator = eventRegistryConfigurator;
        return this;
    }

    public AbstractEngineConfiguration setForceCloseMybatisConnectionPool(boolean forceCloseMybatisConnectionPool) {
        this.forceCloseMybatisConnectionPool = forceCloseMybatisConnectionPool;
        return this;
    }

    public boolean isForceCloseMybatisConnectionPool() {
        return forceCloseMybatisConnectionPool;
    }
}
