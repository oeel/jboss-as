/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.arquillian.container;

import org.jboss.arquillian.spi.ContainerConfiguration;
import org.jboss.arquillian.spi.ContainerProfile;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * JBossAS7 server configuration
 *
 * @author Thomas.Diesler@jboss.com
 * @since 17-Nov-2010
 */
public class JBossAsContainerConfiguration implements ContainerConfiguration {
    private InetAddress bindAddress;
    private int managementPort;
    private int jmxPort;
    private int httpPort;
    private boolean executeWithServlet;
    private long startupTimeout;

    public JBossAsContainerConfiguration() {
        bindAddress = getInetAddress("127.0.0.1");
        managementPort = 9999;
        jmxPort = 1090;
        httpPort = 8080;
        startupTimeout = 30000;
    }

    @Override
    public ContainerProfile getContainerProfile() {
        return ContainerProfile.STANDALONE;
    }

    public InetAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String host) {
        this.bindAddress = getInetAddress(host);
    }

    public int getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(int managementPort) {
        this.managementPort = managementPort;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public boolean isExecuteWithServlet() {
        if (!executeWithServlet) {
            String val = System.getProperty("jboss.arquillian.executeWithServlet", null);
            if (val != null) {
                return Boolean.valueOf(val);
            }
        }
        return executeWithServlet;
    }

    public void setExecuteWithServlet(boolean executeWithServlet) {
        this.executeWithServlet = executeWithServlet;
    }

    public long getStartupTimeout() {
        return startupTimeout;
    }

    public void setStartupTimeout(long startupTimeout) {
        this.startupTimeout = startupTimeout;
    }

    private InetAddress getInetAddress(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host: " + name);
        }
    }
}
