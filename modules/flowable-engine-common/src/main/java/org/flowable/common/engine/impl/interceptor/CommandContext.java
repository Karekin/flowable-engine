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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.runtime.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Tom Baeyens
 * @author Agim Emruli
 * @author Joram Barrez
 * 用于在命令执行的生命周期内维护上下文信息。它位于 interceptor 包中，这与它在命令执行拦截器链中的角色有关。
 */
public class CommandContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandContext.class);
    // 用于存储与当前命令相关的引擎配置的映射
    protected Map<String, AbstractEngineConfiguration> engineConfigurations;
    // 用于存储引擎配置键的堆栈，这有助于管理嵌套命令的执行
    protected LinkedList<String> engineCfgStack = new LinkedList<>();
    // 当前正在执行的命令
    protected Command<?> command;
    // 会话工厂的映射，用于创建和管理不同类型的会话
    protected Map<Class<?>, SessionFactory> sessionFactories;
    // 当前活跃的会话的映射
    protected Map<Class<?>, Session> sessions = new HashMap<>();
    // 在命令执行过程中捕获的异常
    protected Throwable exception;
    // 在命令上下文关闭时调用的监听器列表
    protected List<CommandContextCloseListener> closeListeners;
    // 在命令上下文生命周期内用于存储任何对象的通用属性映射
    protected Map<String, Object> attributes; // General-purpose storing of anything during the lifetime of a command context
    protected boolean reused;
    protected LinkedList<Object> resultStack = new LinkedList<>(); // needs to be a stack, as JavaDelegates can do api calls again
    protected CommandExecutor commandExecutor;
    protected ClassLoader classLoader;
    protected boolean useClassForNameClassLoading;
    protected Clock clock;
    protected ObjectMapper objectMapper;
    
    public CommandContext(Command<?> command) {
        this.command = command;
    }

    public void close() {

        // The intention of this method is that all resources are closed properly, even if exceptions occur
        // in close or flush methods of the sessions or the transaction context.

        try {
            try {
                try {
                    if (exception == null) {
                        executeCloseListenersClosing();
                        flushSessions();
                    }
                } catch (Throwable exception) {
                    exception(exception);

                } finally {

                    try {
                        if (exception == null) {
                            executeCloseListenersAfterSessionFlushed();
                        }
                    } catch (Throwable exception) {
                        exception(exception);
                    }

                    if (exception != null) {
                        logException();
                        executeCloseListenersCloseFailure();
                    } else {
                        executeCloseListenersClosed();
                    }

                }
            } catch (Throwable exception) {
                // Catch exceptions during rollback
                exception(exception);
            } finally {
                // Sessions need to be closed, regardless of exceptions/commit/rollback
                closeSessions();
            }
            
        } catch (Throwable exception) {
            // Catch exceptions during session closing
            exception(exception);
        }

        if (exception != null) {
            rethrowExceptionIfNeeded();
        }
    }

    protected void logException() {
        if (exception instanceof FlowableException && !((FlowableException) exception).isLogged()) {
            return;
        }
        
        if (exception instanceof FlowableOptimisticLockingException) {
            // reduce log level, as normally we're not interested in logging this exception
            LOGGER.debug("Optimistic locking exception : {}", exception.getMessage(), exception);
            
        } else if (exception instanceof FlowableException && ((FlowableException) exception).isReduceLogLevel()) {
            // reduce log level, because this may have been caused because of job deletion due to cancelActiviti="true"
            LOGGER.info("Error while closing command context", exception);
            
        } else {
            LOGGER.error("Error while closing command context", exception);
            
        }
    }

    protected void rethrowExceptionIfNeeded() throws Error {
        if (exception instanceof Error) {
            throw (Error) exception;
        } else if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        } else {
            throw new FlowableException("exception while executing command " + command, exception);
        }
    }

    public void addCloseListener(CommandContextCloseListener commandContextCloseListener) {
        if (closeListeners == null) {
            closeListeners = new ArrayList<>();
        }
        
        if (!commandContextCloseListener.multipleAllowed()) {
            for (CommandContextCloseListener closeListenerItem : closeListeners) {
    			if (closeListenerItem.getClass().equals(commandContextCloseListener.getClass())) {
    				return;
    			}
    		}
        }
        
        closeListeners.add(commandContextCloseListener);
        
        closeListeners.sort(Comparator.comparing(CommandContextCloseListener::order));
    }

    public List<CommandContextCloseListener> getCloseListeners() {
        return closeListeners;
    }

    protected void executeCloseListenersClosing() {
        if (closeListeners != null) {
            try {
                for (int i = 0; i < closeListeners.size(); i++) {
                    closeListeners.get(i).closing(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void executeCloseListenersAfterSessionFlushed() {
        if (closeListeners != null) {
            try {
                for (int i = 0; i < closeListeners.size(); i++) {
                    closeListeners.get(i).afterSessionsFlush(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void executeCloseListenersClosed() {
        if (closeListeners != null) {
            try {
                for (int i = 0; i < closeListeners.size(); i++) {
                    closeListeners.get(i).closed(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void executeCloseListenersCloseFailure() {
        if (closeListeners != null) {
            try {
                for (int i = 0; i < closeListeners.size(); i++) {
                    closeListeners.get(i).closeFailure(this);
                }
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    protected void flushSessions() {
        for (Session session : sessions.values()) {
            session.flush();
        }
    }

    protected void closeSessions() {
        for (Session session : sessions.values()) {
            try {
                session.close();
            } catch (Throwable exception) {
                exception(exception);
            }
        }
    }

    /**
     * Stores the provided exception on this {@link CommandContext} instance. That exception will be rethrown at the end of closing the {@link CommandContext} instance.
     * 
     * If there is already an exception being stored, a 'masked exception' message will be logged.
     */
    public void exception(Throwable exception) {
        if (this.exception == null) {
            this.exception = exception;

        } else {
            LOGGER.error("masked exception in command context. for root cause, see below as it will be rethrown later.", exception);
        }
    }

    public void resetException() {
        this.exception = null;
    }

    public void addAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new HashMap<>(1);
        }
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        if (attributes != null) {
            return attributes.get(key);
        }
        return null;
    }

    public void removeAttribute(String key) {
        if (attributes != null) {
            attributes.remove(key);

            if (attributes.isEmpty()) {
                attributes = null;
            }
        }
    }
    
    @SuppressWarnings({ "unchecked" })
    public <T> T getSession(Class<T> sessionClass) {
        Session session = sessions.get(sessionClass);
        if (session == null) {
            SessionFactory sessionFactory = sessionFactories.get(sessionClass);
            if (sessionFactory == null) {
                throw new FlowableException("no session factory configured for " + sessionClass.getName());
            }
            session = sessionFactory.openSession(this);
            sessions.put(sessionClass, session);
        }

        return (T) session;
    }

    public Map<Class<?>, SessionFactory> getSessionFactories() {
        return sessionFactories;
    }
    
    public void setSessionFactories(Map<Class<?>, SessionFactory> sessionFactories) {
        this.sessionFactories = sessionFactories;
    }   

    public Map<String, AbstractEngineConfiguration> getEngineConfigurations() {
        return engineConfigurations;
    }
    
    public void setEngineConfigurations(Map<String, AbstractEngineConfiguration> engineConfigurations) {
        this.engineConfigurations = engineConfigurations;
    }

    public void pushEngineCfgToStack(String engineCfgKey) {
        engineCfgStack.push(engineCfgKey);
    }

    public String popEngineCfgStack() {
        return engineCfgStack.pop();
    }

    /**
     * @return Returns whether (at the time of calling this method) the current engine
     * is being executed as the 'root engine'. Or said differently: this will return
     * <em>false</em> when the current engine is being used in a nested {@link Command} execution call
     * and will return <em>true</em> if it is the root usage of the current engine.
     *
     * For example:
     * CMMN engine executes process task, that in its turn calls a CMMN task.
     * The hierarchy of engine will be CMMN (a) - BPMN (b) - CMMN (c).
     *
     * If this method is called in the context of (c), false is returned.
     * In the context of (a), true will be returned. For (b), there is but one
     * usage and it will be true.
     */
    public boolean isRootUsageOfCurrentEngine() {
        String currentEngineCfgKey = engineCfgStack.peek();
        if (currentEngineCfgKey != null) {
            for (int i = 1; i < engineCfgStack.size(); i++) {
                if (currentEngineCfgKey.equals(engineCfgStack.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void addEngineConfiguration(String engineKey, String scopeType, AbstractEngineConfiguration engineConfiguration) {
        if (engineConfigurations == null) {
            engineConfigurations = new HashMap<>();
        }
        engineConfigurations.put(engineKey, engineConfiguration);
        engineConfigurations.put(scopeType, engineConfiguration);
    }
    
    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public boolean isUseClassForNameClassLoading() {
        return useClassForNameClassLoading;
    }

    public void setUseClassForNameClassLoading(boolean useClassForNameClassLoading) {
        this.useClassForNameClassLoading = useClassForNameClassLoading;
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
    
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    // getters and setters
    // //////////////////////////////////////////////////////

    public Command<?> getCommand() {
        return command;
    }

    public Map<Class<?>, Session> getSessions() {
        return sessions;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isReused() {
        return reused;
    }

    public void setReused(boolean reused) {
        this.reused = reused;
    }
    
    public Object getResult() {
        return resultStack.pollLast();
    }

    public void setResult(Object result) {
        resultStack.add(result);
    }
    
}
