/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.resource.shared.dto;

import com.codenvy.resource.model.Resource;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface ResourceDto extends Resource {
    @Override
    String getType();

    void setType(String type);

    ResourceDto withType(String type);

    @Override
    long getAmount();

    void setAmount(long amount);

    ResourceDto withAmount(long amount);

    @Override
    String getUnit();

    void setUnit(String unit);

    ResourceDto withUnit(String unit);
}
