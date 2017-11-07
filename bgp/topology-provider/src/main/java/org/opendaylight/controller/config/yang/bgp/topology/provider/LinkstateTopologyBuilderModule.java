/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: config-bgp-topology-provider  yang module local name: bgp-linkstate-topology
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Tue Nov 19 15:22:41 CET 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.controller.config.yang.bgp.topology.provider;

import org.opendaylight.bgpcep.bgp.topology.provider.LinkstateTopologyBuilder;
import org.opendaylight.bgpcep.bgp.topology.provider.config.BackwardsCssTopologyProvider;
import org.opendaylight.controller.config.api.DependencyResolver;
import org.opendaylight.controller.config.api.JmxAttributeValidationException;
import org.opendaylight.controller.config.api.ModuleIdentifier;
import org.osgi.framework.BundleContext;

/**
 *
 */
@Deprecated
public final class LinkstateTopologyBuilderModule extends AbstractLinkstateTopologyBuilderModule {

    private BundleContext bundleContext;

    public LinkstateTopologyBuilderModule(final ModuleIdentifier identifier, final DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LinkstateTopologyBuilderModule(final ModuleIdentifier identifier, final DependencyResolver dependencyResolver,
            final LinkstateTopologyBuilderModule oldModule, final AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void validate() {
        super.validate();
        JmxAttributeValidationException.checkNotNull(getTopologyId(), "is not set.", topologyIdJmxAttribute);
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        return BackwardsCssTopologyProvider.createBackwardsCssInstance(LinkstateTopologyBuilder.LINKSTATE_TOPOLOGY_TYPE, getTopologyId(), this.bundleContext,
                getLocalRibDependency().getInstanceIdentifier());
    }
    public void setBundleContext(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}