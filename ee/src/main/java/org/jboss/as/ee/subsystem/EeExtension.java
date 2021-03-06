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

package org.jboss.as.ee.subsystem;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.ModelQueryOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.common.CommonDescriptions;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ModelNodeRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.parsing.ParseUtils.*;

/**
 * JBossAS domain extension used to initialize the ee subsystem handlers and associated classes.
 *
 * @author Weston M. Price
 * @author Emanuel Muckenhuber
 */
public class EeExtension implements Extension {

    public static final String NAMESPACE = "urn:jboss:domain:ee:1.0";
    private static final String SUBSYSTEM_NAME = "ee";
    private static final EESubsystemParser parser = new EESubsystemParser();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME);
        final ModelNodeRegistration registration = subsystem.registerSubsystemModel(EeSubsystemProviders.SUBSYSTEM);
        registration.registerOperationHandler(ADD, EeSubsystemAdd.INSTANCE, EeSubsystemProviders.SUBSYSTEM_ADD, false);
        registration.registerOperationHandler(DESCRIBE, EESubsystemDescribeHandler.INSTANCE, EESubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
        subsystem.registerXMLElementWriter(parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(NAMESPACE, parser);
    }

    static ModelNode createAddOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).add(ModelDescriptionConstants.SUBSYSTEM, SUBSYSTEM_NAME);
        return subsystem;
    }


    static class EESubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
            //TODO seems to be a problem with empty elements cleaning up the queue in FormattingXMLStreamWriter.runAttrQueue
            //context.startSubsystemElement(NewEeExtension.NAMESPACE, true);
            context.startSubsystemElement(EeExtension.NAMESPACE, false);
            ModelNode node = context.getModelNode();
            if (node.hasDefined(CommonAttributes.GLOBAL_MODULES)) {
                writer.writeStartElement(Element.GLOBAL_MODULES.getLocalName());
                final ModelNode globalModules = node.get(CommonAttributes.GLOBAL_MODULES);
                for(ModelNode module : globalModules.asList()) {
                    writer.writeStartElement(Element.MODULE.getLocalName());
                    writer.writeAttribute(Attribute.NAME.getLocalName(), module.get(CommonAttributes.NAME).asString());
                    writer.writeAttribute(Attribute.SLOT.getLocalName(), module.get(CommonAttributes.SLOT).asString());
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }

            writer.writeEndElement();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            ParseUtils.requireNoAttributes(reader);

            final ModelNode subsystem = createAddOperation();
            list.add(subsystem);

            // elements
            final EnumSet<Element> encountered = EnumSet.noneOf(Element.class);
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                switch (Namespace.forUri(reader.getNamespaceURI())) {
                    case EE_1_0: {
                        final Element element = Element.forName(reader.getLocalName());
                        if (!encountered.add(element)) {
                            throw unexpectedElement(reader);
                        }
                        switch (element) {
                            case GLOBAL_MODULES: {
                                final ModelNode model = parseGlobalModules(reader);
                                subsystem.get(CommonAttributes.GLOBAL_MODULES).set(model);
                                break;
                            }
                            default: {
                                throw unexpectedElement(reader);
                            }
                        }
                        break;
                    }
                    default: {
                        throw unexpectedElement(reader);
                    }
                }
            }
        }

        static ModelNode parseGlobalModules(XMLExtendedStreamReader reader) throws XMLStreamException {

            ModelNode globalModules = new ModelNode();

            requireNoAttributes(reader);

            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                switch (Element.forName(reader.getLocalName())) {
                    case MODULE: {
                        final int count = reader.getAttributeCount();
                        String name = null;
                        String slot = null;
                        for (int i = 0; i < count; i++) {
                            requireNoNamespaceAttribute(reader, i);
                            final String value = reader.getAttributeValue(i);
                            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
                            switch (attribute) {
                                case NAME:
                                    if (name != null) {
                                        throw unexpectedAttribute(reader, i);
                                    }
                                    name = value;
                                    break;
                                case SLOT:
                                    if (slot != null) {
                                        throw unexpectedAttribute(reader, i);
                                    }
                                    slot = value;
                                    break;
                                default:
                                    unexpectedAttribute(reader, i);
                            }
                        }
                        if (name == null) {
                            missingRequired(reader, Collections.singleton(NAME));
                        }
                        if (slot == null) {
                            slot = "main";
                        }
                        final ModelNode module = new ModelNode();
                        module.get(CommonAttributes.NAME).set(name);
                        module.get(CommonAttributes.SLOT).set(slot);
                        globalModules.add(module);
                        requireNoContent(reader);
                        break;
                    }
                    default: {
                        unexpectedElement(reader);
                    }
                }
            }
            return globalModules;
        }

    }

    private static class EESubsystemDescribeHandler implements ModelQueryOperationHandler, DescriptionProvider {
        static final EESubsystemDescribeHandler INSTANCE = new EESubsystemDescribeHandler();

        @Override
        public OperationResult execute(OperationContext context, ModelNode operation, ResultHandler resultHandler) {
            ModelNode node = new ModelNode();
            node.add(createAddOperation());

            resultHandler.handleResultFragment(Util.NO_LOCATION, node);
            resultHandler.handleResultComplete();
            return new BasicOperationResult();
        }

        @Override
        public ModelNode getModelDescription(Locale locale) {
            return CommonDescriptions.getSubsystemDescribeOperation(locale);
        }
    }

}
