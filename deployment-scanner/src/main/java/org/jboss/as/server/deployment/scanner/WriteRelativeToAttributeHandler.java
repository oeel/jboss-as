/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.server.deployment.scanner;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.server.operations.ServerWriteAttributeOperationHandler;
import org.jboss.dmr.ModelNode;

/**
 * Update the 'relative-to' attribute on a {@code DeploymentScanner}.
 *
 * @author Brian Stansberry
 */
class WriteRelativeToAttributeHandler extends ServerWriteAttributeOperationHandler {

    static final WriteRelativeToAttributeHandler INSTANCE = new WriteRelativeToAttributeHandler();

    private WriteRelativeToAttributeHandler() {
        super(new StringLengthValidator(1, Integer.MAX_VALUE, true, false));
    }

    @Override
    protected boolean applyUpdateToRuntime(final OperationContext context, final ModelNode operation, final ResultHandler resultHandler,
            final String attributeName, final ModelNode newValue, final ModelNode currentValue) throws OperationFailedException {

        resultHandler.handleResultComplete();

        return true;
    }
}
