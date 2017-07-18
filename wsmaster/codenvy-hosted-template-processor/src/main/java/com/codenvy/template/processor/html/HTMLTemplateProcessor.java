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
package com.codenvy.template.processor.html;

import com.codenvy.template.processor.exception.FailedProcessingTemplateException;
import com.codenvy.template.processor.exception.TemplateNotFoundException;

import java.io.Writer;
import java.util.Map;

/**
 * Defines set of actions for processing HTML templates.
 *
 * @author Anton Korneta
 */
public interface HTMLTemplateProcessor<T> {

    /**
     * Fills specified HTML template with given variables.
     *
     * @param template
     *         HTML template which resolving is provided by implementation
     * @param variables
     *         variables mapping in given HTML template
     * @return full filled HTML template as string
     * @throws TemplateNotFoundException
     *         when given {@code template} not found
     * @throws FailedProcessingTemplateException
     *         when any problems occurs during  template processing
     */
    String process(String template,
                   Map<String, Object> variables) throws TemplateNotFoundException, FailedProcessingTemplateException;


    /**
     * Fills specified HTML template with given variables.
     *
     * @param template
     *         HTML template which resolving is provided by implementation
     * @param variables
     *         variables mapping in given HTML template
     * @param writer
     *         where the processed HTML template will be written to
     * @throws TemplateNotFoundException
     *         when given {@code template} not found
     * @throws FailedProcessingTemplateException
     *         when any problems occurs during  template processing
     */
    void process(String template,
                 Map<String, Object> variables,
                 Writer writer) throws TemplateNotFoundException, FailedProcessingTemplateException;

    /**
     * Fills HTML representation of given template.
     *
     * @param template
     *         template instance to process
     * @return full filled HTML template as string
     */
    String process(T template);

}
