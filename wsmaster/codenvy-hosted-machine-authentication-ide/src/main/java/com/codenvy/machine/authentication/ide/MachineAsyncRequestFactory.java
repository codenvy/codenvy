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
package com.codenvy.machine.authentication.ide;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;
import com.google.gwt.http.client.RequestBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.util.loging.Log;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;
import static org.eclipse.che.ide.rest.HTTPMethod.POST;

/**
 * Looks at the request and substitutes an appropriate implementation.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineAsyncRequestFactory extends AsyncRequestFactory implements WorkspaceStoppedEvent.Handler,
                                                                               WorkspaceStartedEvent.Handler {
    private static final String DTO_CONTENT_TYPE   = APPLICATION_JSON;

    private final Provider<MachineTokenServiceClient> machineTokenServiceProvider;
    private final DtoFactory                          dtoFactory;
    private final Provider<WorkspaceServiceClient>    workspaceServiceClientProvider;
    private final AppContext appContext;

    private String machineToken;
    private String wsAgentBaseUrl;

    @Inject
    public MachineAsyncRequestFactory(DtoFactory dtoFactory,
                                      Provider<MachineTokenServiceClient> machineTokenServiceProvider,
                                      Provider<WorkspaceServiceClient> workspaceServiceClientProvider,
                                      AppContext appContext,
                                      EventBus eventBus) {
        super(dtoFactory);
        this.machineTokenServiceProvider = machineTokenServiceProvider;
        this.dtoFactory = dtoFactory;
        this.workspaceServiceClientProvider = workspaceServiceClientProvider;
        this.appContext = appContext;
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
    }

    @Override
    protected AsyncRequest doCreateRequest(RequestBuilder.Method method,
                                           String url,
                                           Object dtoBody,
                                           boolean async) {
        if (!isWsAgentRequest(url)) {
            return super.doCreateRequest(method, url, dtoBody, async);
        }
        return doCreateMachineRequest(method, url, dtoBody, async);
    }

    private AsyncRequest doCreateMachineRequest(RequestBuilder.Method method, String url, Object dtoBody, boolean async) {
        final AsyncRequest asyncRequest = new MachineAsyncRequest(method, url, async, getMachineToken());
        if (dtoBody != null) {
            if (dtoBody instanceof List) {
                asyncRequest.data(dtoFactory.toJson((List)dtoBody));
            } else {
                asyncRequest.data(dtoFactory.toJson(dtoBody));
            }
            asyncRequest.header(CONTENT_TYPE, DTO_CONTENT_TYPE);
        } else if (method != null && POST.equals(method.toString())) {
            asyncRequest.header(CONTENT_TYPE, TEXT_PLAIN);
        }

        return asyncRequest;
    }

    private Promise<String> getMachineToken() {
        if (!isNullOrEmpty(machineToken)) {
            return Promises.resolve(machineToken);
        } else {
            return machineTokenServiceProvider.get()
                                              .getMachineToken()
                                              .then(new Function<MachineTokenDto, String>() {
                                                  @Override
                                                  public String apply(MachineTokenDto tokenDto) throws FunctionException {
                                                      machineToken = tokenDto.getMachineToken();
                                                      return machineToken;
                                                  }
                                              });
        }
    }

    // since the machine token lives with the workspace runtime,
    // we need to invalidate it on stopping workspace.
    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        machineToken = null;
    }

    /**
     * Going to check ist this request goes to WsAgent
     * @param url
     * @return
     */
    private boolean isWsAgentRequest(String url) {
        if (appContext.getWorkspace() == null || !RUNNING.equals(appContext.getWorkspace().getStatus())) {
            return false; //ws-agent not started
        }
        if (isNullOrEmpty(wsAgentBaseUrl)) {
            final DevMachine devMachine = appContext.getDevMachine();
            if (devMachine != null) {
                wsAgentBaseUrl = devMachine.getWsAgentBaseUrl();
            } else {
                return false;
            }
        }
        return url.contains(nullToEmpty(wsAgentBaseUrl));
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
//        workspaceServiceClientProvider.get().getWorkspace(event.getWorkspace().getId()).then(new Operation<WorkspaceDto>() {
//            @Override
//            public void apply(WorkspaceDto ws) throws OperationException {
//                MachineDto devMachineDto = ws.getRuntime().getDevMachine();
//                wsAgentBaseUrl = new DevMachine(devMachineDto).getWsAgentBaseUrl();
//
//            }
//        }).catchError(new Operation<PromiseError>() {
//            @Override
//            public void apply(PromiseError err) throws OperationException {
//                Log.error(getClass(), err.getCause());
//            }
//        });
    }


    private String getWsAgentUrl(){
        final DevMachine devMachine = appContext.getDevMachine();
        if (devMachine != null) {
            return devMachine.getWsAgentBaseUrl();
        }
        else {
            workspaceServiceClientProvider.get().getWorkspace(appContext.getWorkspaceId()).then(new Function<WorkspaceDto, String>() {
                @Override
                public String apply(WorkspaceDto ws) throws FunctionException {
                    MachineDto devMachineDto = ws.getRuntime().getDevMachine();
                    return new DevMachine(devMachineDto).getWsAgentBaseUrl();

                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError err) throws OperationException {
                    Log.error(getClass(), err.getCause());
                }
            });
        }
        return "";
    }
}
