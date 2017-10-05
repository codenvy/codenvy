/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.template.processor.html.thymeleaf;

import static org.thymeleaf.templatemode.TemplateMode.HTML;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.mail.template.Template;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.mail.template.exception.TemplateException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Thymeleaf implementation of {@link TemplateProcessor}.
 *
 * @author Anton Korneta
 */
@Singleton
public class ThymeleafTemplateProcessorImpl implements TemplateProcessor {

  private final TemplateEngine templateEngine;

  @Inject
  public ThymeleafTemplateProcessorImpl() {
    final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setTemplateMode(HTML);
    templateResolver.setSuffix(".html");
    templateResolver.setCacheTTLMs(3600000L);
    this.templateEngine = new TemplateEngine();
    this.templateEngine.setTemplateResolver(templateResolver);
  }

  @Override
  public String process(String templateName, Map<String, Object> variables)
      throws TemplateException {
    final Context context = new Context();
    context.setVariables(variables);
    return templateEngine.process(templateName, context);
  }

  @Override
  public String process(Template template) throws TemplateException {
    return process(template.getName(), template.getAttributes());
  }
}
