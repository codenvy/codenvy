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
package com.codenvy.api.machine.server.recipe;

import com.codenvy.api.permission.server.PermissionsManager;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.machine.server.event.RecipePersistedEvent;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/**
 * Tests {@link RecipeCreatorPermissionsProvider}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class RecipeCreatorPermissionsProviderTest {

    @Mock
    private EventService eventService;

    @Mock
    private PermissionsManager permManager;

    @InjectMocks
    private RecipeCreatorPermissionsProvider permProvider;


    @AfterMethod
    public void resetContext() {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldAddPermissions() throws Exception {
        final EnvironmentContext ctx = new EnvironmentContext();
        ctx.setSubject(new SubjectImpl("test-user-name", "test-user-id", "test-token", false));
        EnvironmentContext.setCurrent(ctx);
        final RecipeImpl recipe = createRecipe();

        permProvider.onEvent(new RecipePersistedEvent(recipe));

        final ArgumentCaptor<RecipePermissionsImpl> captor = ArgumentCaptor.forClass(RecipePermissionsImpl.class);
        verify(permManager).storePermission(captor.capture());
        final RecipePermissionsImpl perm = captor.getValue();
        assertEquals(perm.getInstanceId(), recipe.getId());
        assertEquals(perm.getUserId(), "test-user-id");
        assertEquals(perm.getDomainId(), RecipeDomain.DOMAIN_ID);
        assertEquals(perm.getActions(), RecipeDomain.getActions());
    }

    @Test
    public void shouldNotAddPermissionsIfThereIsNoUserInEnvironmentContext() throws Exception {
        permProvider.onEvent(new RecipePersistedEvent(createRecipe()));

        verify(permManager, never()).storePermission(any());
    }

    @Test
    public void shouldSubscribe() {
        permProvider.subscribe();

        verify(eventService).subscribe(permProvider);
    }

    @Test
    public void shouldUnsubscribe() {
        permProvider.unsubscribe();

        verify(eventService).unsubscribe(permProvider);
    }

    private static RecipeImpl createRecipe() {
        return new RecipeImpl("test", "DEBIAN_JDK8", "test", "test", "script", asList("debian", "test"), "description");
    }
}
