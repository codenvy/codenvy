/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.template.processor.html.thymeleaf;

import com.codenvy.template.processor.exception.FailedProcessingTemplateException;
import com.codenvy.template.processor.exception.TemplateNotFoundException;
import com.codenvy.template.processor.html.HTMLTemplateProcessor;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

/**
 * Thymeleaf implementation of {@link HTMLTemplateProcessor}.
 *
 * @author Anton Korneta
 */
@Singleton
public class HTMLTemplateProcessorImpl implements HTMLTemplateProcessor<ThymeleafTemplate> {

    private final TemplateEngine templateEngine;

    @Inject
    public HTMLTemplateProcessorImpl() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    @Override
    public String process(String template,
                          Map<String, Object> variables) throws TemplateNotFoundException,
                                                                FailedProcessingTemplateException {
        final StringWriter stringWriter = new StringWriter();
        this.process(template, variables, stringWriter);
        return stringWriter.toString();
    }

    @Override
    public void process(String template,
                        Map<String, Object> variables,
                        Writer writer) throws TemplateNotFoundException,
                                              FailedProcessingTemplateException {
        final Context context = new Context();
        context.setVariables(variables);
        templateEngine.process(template, context, writer);
    }

    @Override
    public String process(ThymeleafTemplate template) {
        return templateEngine.process(template.getPath(),
                                      template.getContext());
    }

}
