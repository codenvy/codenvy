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
package com.codenvy.resource.api.free;

import com.codenvy.resource.api.type.ResourceType;
import com.codenvy.resource.shared.dto.ResourceDto;
import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link ResourceValidator}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class ResourceValidatorTest {
    private static final String      RESOURCE_TYPE         = "test";
    private static final String      DEFAULT_RESOURCE_UNIT = "mb";
    private static final Set<String> SUPPORTED_UNITS       = ImmutableSet.of(DEFAULT_RESOURCE_UNIT, "gb");
    @Mock
    private ResourceType resourceType;

    private ResourceValidator validator;

    @BeforeMethod
    public void setUp() throws Exception {
        when(resourceType.getDefaultUnit()).thenReturn("mb");
        when(resourceType.getId()).thenReturn(RESOURCE_TYPE);
        when(resourceType.getSupportedUnits()).thenReturn(SUPPORTED_UNITS);

        validator = new ResourceValidator(ImmutableSet.of(resourceType));
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Specified resources type 'unsupported' is not supported")
    public void shouldThrowBadRequestExceptionWhenResourceHasNonSupportedType() throws Exception {
        //when
        validator.validate(DtoFactory.newDto(ResourceDto.class)
                                     .withType("unsupported")
                                     .withUnit("mb"));
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Specified resources type 'test' support only following units: mb, gb")
    public void shouldThrowBadRequestExceptionWhenResourceHasNonSupportedUnit() throws Exception {
        //when
        validator.validate(DtoFactory.newDto(ResourceDto.class)
                                     .withType(RESOURCE_TYPE)
                                     .withUnit("kb"));
    }

    @Test
    public void shouldSetDefaultResourceUnitWhenItIsMissed() throws Exception {
        //given
        ResourceDto toValidate = DtoFactory.newDto(ResourceDto.class)
                                           .withType(RESOURCE_TYPE)
                                           .withUnit(null);

        //when
        validator.validate(toValidate);

        //then
        assertEquals(toValidate.getUnit(), DEFAULT_RESOURCE_UNIT);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Resources with type 'test' has negative amount")
    public void shouldThrowBadRequestExceptionWhenResourceHasNegativeAmount() throws Exception {
        //when
        validator.validate(DtoFactory.newDto(ResourceDto.class)
                                     .withType(RESOURCE_TYPE)
                                     .withAmount(-1024)
                                     .withUnit("mb"));
    }

    @Test
    public void shouldNotThrowAnyExceptionsWhenResourceHasSupportedTypeAndUnit() throws Exception {
        //when
        validator.validate(DtoFactory.newDto(ResourceDto.class)
                                     .withType(RESOURCE_TYPE)
                                     .withUnit("mb"));
    }
}
