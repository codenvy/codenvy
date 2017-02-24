/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
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
