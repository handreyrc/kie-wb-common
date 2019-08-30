/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.definition.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import gwt.jsonix.marshallers.xjc.plugin.JsUtils;
import org.kie.workbench.common.dmn.api.definition.HasComponentWidths;
import org.kie.workbench.common.dmn.api.definition.model.BusinessKnowledgeModel;
import org.kie.workbench.common.dmn.api.definition.model.DRGElement;
import org.kie.workbench.common.dmn.api.definition.model.DecisionService;
import org.kie.workbench.common.dmn.api.definition.model.FunctionDefinition;
import org.kie.workbench.common.dmn.api.definition.model.InformationItemPrimary;
import org.kie.workbench.common.dmn.api.definition.model.KnowledgeRequirement;
import org.kie.workbench.common.dmn.api.definition.model.KnowledgeSource;
import org.kie.workbench.common.dmn.api.property.background.BackgroundSet;
import org.kie.workbench.common.dmn.api.property.dimensions.GeneralRectangleDimensionsSet;
import org.kie.workbench.common.dmn.api.property.dmn.Description;
import org.kie.workbench.common.dmn.api.property.dmn.Id;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.api.property.font.FontSet;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITAuthorityRequirement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITBusinessKnowledgeModel;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITDMNElementReference;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITFunctionDefinition;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITInformationItem;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.js.model.dmn12.JSITKnowledgeRequirement;
import org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.definition.model.dd.ComponentWidths;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.dmn.webapp.kogito.marshaller.mapper.definition.model.HrefBuilder.getHref;
import static org.kie.workbench.common.stunner.core.definition.adapter.binding.BindableAdapterUtils.getDefinitionId;

public class BusinessKnowledgeModelConverter implements NodeConverter<JSITBusinessKnowledgeModel, org.kie.workbench.common.dmn.api.definition.model.BusinessKnowledgeModel> {

    private FactoryManager factoryManager;

    public BusinessKnowledgeModelConverter(final FactoryManager factoryManager) {
        super();
        this.factoryManager = factoryManager;
    }

    @Override
    public Node<View<BusinessKnowledgeModel>, ?> nodeFromDMN(final JSITBusinessKnowledgeModel dmn,
                                                             final BiConsumer<String, HasComponentWidths> hasComponentWidthsConsumer) {
        @SuppressWarnings("unchecked")
        final Node<View<BusinessKnowledgeModel>, ?> node = (Node<View<BusinessKnowledgeModel>, ?>) factoryManager.newElement(dmn.getId(),
                                                                                                                             getDefinitionId(BusinessKnowledgeModel.class)).asNode();
        final Id id = new Id(dmn.getId());
        final Description description = DescriptionPropertyConverter.wbFromDMN(dmn.getDescription());
        final Name name = new Name(dmn.getName());
        final InformationItemPrimary informationItem = InformationItemPrimaryPropertyConverter.wbFromDMN(dmn.getVariable(), dmn);
        final JSITFunctionDefinition dmnFunctionDefinition = dmn.getEncapsulatedLogic();
        final FunctionDefinition functionDefinition = FunctionDefinitionPropertyConverter.wbFromDMN(dmnFunctionDefinition,
                                                                                                    hasComponentWidthsConsumer);
        final BusinessKnowledgeModel bkm = new BusinessKnowledgeModel(id,
                                                                      description,
                                                                      name,
                                                                      informationItem,
                                                                      functionDefinition,
                                                                      new BackgroundSet(),
                                                                      new FontSet(),
                                                                      new GeneralRectangleDimensionsSet());
        node.getContent().setDefinition(bkm);

        if (informationItem != null) {
            informationItem.setParent(bkm);
        }
        if (functionDefinition != null) {
            functionDefinition.setParent(bkm);
        }

        if (Objects.nonNull(dmnFunctionDefinition)) {
            hasComponentWidthsConsumer.accept(dmnFunctionDefinition.getId(),
                                              functionDefinition);
        }

        DMNExternalLinksToExtensionElements.loadExternalLinksFromExtensionElements(dmn, bkm);

        return node;
    }

    @Override
    @SuppressWarnings("unchecked")
    public JSITBusinessKnowledgeModel dmnFromNode(final Node<View<BusinessKnowledgeModel>, ?> node,
                                                  final Consumer<ComponentWidths> componentWidthsConsumer) {
        final BusinessKnowledgeModel source = node.getContent().getDefinition();
        final JSITBusinessKnowledgeModel result = new JSITBusinessKnowledgeModel();
        result.setId(source.getId().getValue());
        result.setDescription(DescriptionPropertyConverter.dmnFromWB(source.getDescription()));
        result.setName(source.getName().getValue());
        DMNExternalLinksToExtensionElements.loadExternalLinksIntoExtensionElements(source, result);
        final JSITInformationItem variable = InformationItemPrimaryPropertyConverter.dmnFromWB(source.getVariable(), source);
        if (variable != null) {
            variable.setParent(result);
        }
        result.setVariable(variable);
        final JSITFunctionDefinition functionDefinition = FunctionDefinitionPropertyConverter.dmnFromWB(source.getEncapsulatedLogic(),
                                                                                                        componentWidthsConsumer);

        final FunctionDefinition wbFunctionDefinition = source.getEncapsulatedLogic();
        if (Objects.nonNull(wbFunctionDefinition)) {
            final String uuid = wbFunctionDefinition.getId().getValue();
            if (Objects.nonNull(uuid)) {
                final ComponentWidths componentWidths = new ComponentWidths();
                componentWidths.setDmnElementRef(new QName(XMLConstants.NULL_NS_URI,
                                                           uuid,
                                                           XMLConstants.DEFAULT_NS_PREFIX));
                componentWidths.setWidths(new ArrayList<>(source.getEncapsulatedLogic().getComponentWidths()));
                componentWidthsConsumer.accept(componentWidths);
            }
        }

        if (functionDefinition != null) {
            functionDefinition.setParent(result);
        }
        result.setEncapsulatedLogic(functionDefinition);

        // DMN spec table 2: Requirements connection rules
        final List<Edge<?, ?>> inEdges = (List<Edge<?, ?>>) node.getInEdges();
        for (Edge<?, ?> e : inEdges) {
            final Node<?, ?> sourceNode = e.getSourceNode();
            if (sourceNode.getContent() instanceof View<?>) {
                final View<?> view = (View<?>) sourceNode.getContent();
                if (view.getDefinition() instanceof DRGElement) {
                    final DRGElement drgElement = (DRGElement) view.getDefinition();
                    if (drgElement instanceof BusinessKnowledgeModel) {
                        final JSITKnowledgeRequirement iReq = new JSITKnowledgeRequirement();
                        iReq.setId(e.getUUID());
                        final JSITDMNElementReference ri = new JSITDMNElementReference();
                        ri.setHref(getHref(drgElement));
                        iReq.setRequiredKnowledge(ri);
                        JsUtils.add(result.getKnowledgeRequirement(), iReq);
                    } else if (drgElement instanceof KnowledgeSource) {
                        final JSITAuthorityRequirement iReq = new JSITAuthorityRequirement();
                        iReq.setId(e.getUUID());
                        final JSITDMNElementReference ri = new JSITDMNElementReference();
                        ri.setHref(getHref(drgElement));
                        iReq.setRequiredAuthority(ri);
                        JsUtils.add(result.getAuthorityRequirement(), iReq);
                    } else if (drgElement instanceof DecisionService) {
                        if (e.getContent() instanceof View && ((View) e.getContent()).getDefinition() instanceof KnowledgeRequirement) {
                            final JSITKnowledgeRequirement iReq = new JSITKnowledgeRequirement();
                            iReq.setId(e.getUUID());
                            final JSITDMNElementReference ri = new JSITDMNElementReference();
                            ri.setHref(getHref(drgElement));
                            iReq.setRequiredKnowledge(ri);
                            JsUtils.add(result.getKnowledgeRequirement(), iReq);
                        } else {
                            throw new UnsupportedOperationException("wrong model definition.");
                        }
                    } else {
                        throw new UnsupportedOperationException("wrong model definition.");
                    }
                }
            }
        }
        return result;
    }
}
