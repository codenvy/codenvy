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

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.RequestBuilder;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

/**
 * Modify each machine request and add the authorization header with the token value.
 *
 * @author Anton Korneta
 */
public class MachineAsyncRequest extends AsyncRequest {

    private final Promise<String> tokenPromise;

    protected MachineAsyncRequest(RequestBuilder.Method method,
                                  String url,
                                  boolean async,
                                  Promise<String> tokenPromise) {
        super(method, url, async);
        this.tokenPromise = tokenPromise;
    }

    @Override
    public Promise<Void> send() {
        requestBuilder.setIncludeCredentials(true);
        final Executor.ExecutorBody<Void> body = new Executor.ExecutorBody<Void>() {
            @Override
            public void apply(final ResolveFunction<Void> resolve, final RejectFunction reject) {
                tokenPromise.then(new Operation<String>() {
                    @Override
                    public void apply(String machine) throws OperationException {
                        MachineAsyncRequest.this.header(AUTHORIZATION, machine);
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        Log.error(getClass(), "Something wrong happened");
                    }
                });
            }
        };
        final Executor<Void> executor = Executor.create(body);
        return Promises.create(executor);
    }

    @Override
    public <R> Promise<R> send(final Unmarshallable<R> unmarshaller) {
        return CallbackPromiseHelper.createFromCallback(new CallbackPromiseHelper.Call<R, Throwable>() {
            @Override
            public void makeCall(final Callback<R, Throwable> callback) {
                send(new AsyncRequestCallback<R>(unmarshaller) {
                    @Override
                    protected void onSuccess(R result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    @Override
    public void send(final AsyncRequestCallback<?> callback) {
        requestBuilder.setIncludeCredentials(true);
        tokenPromise.then(new Operation<String>() {
            @Override
            public void apply(String machineToken) throws OperationException {
                MachineAsyncRequest.this.header(AUTHORIZATION, machineToken);
                MachineAsyncRequest.super.send(callback);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(getClass(), arg);
            }
        });
    }
}
