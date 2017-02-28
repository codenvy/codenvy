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
