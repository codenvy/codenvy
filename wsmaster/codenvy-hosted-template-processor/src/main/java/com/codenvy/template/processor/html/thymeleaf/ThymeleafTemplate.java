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


import org.thymeleaf.context.Context;

/**
 * Describes thymeleaf specific template.
 *
 * @author Anton Korneta
 */
public abstract class ThymeleafTemplate {

    protected final Context context;

    public ThymeleafTemplate() {
        this.context = new Context();
    }

    public Context getContext() {
        return context;
    }

    public abstract String getPath();

}
