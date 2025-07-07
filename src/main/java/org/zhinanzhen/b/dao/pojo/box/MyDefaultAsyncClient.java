package org.zhinanzhen.b.dao.pojo.box;

import com.aliyun.core.http.HttpMethod;
import com.aliyun.sdk.service.pds20220301.models.ListFileRequest;
import darabonba.core.RequestStyle;
import darabonba.core.TeaAsyncHandler;
import darabonba.core.TeaRequest;
import darabonba.core.client.ClientConfiguration;
import darabonba.core.client.ClientExecutionParams;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class MyDefaultAsyncClient implements MyAsyncClient {
    protected final String product;
    protected final String version;
    protected final String endpointRule;
    protected final Map<String, String> endpointMap;
    protected final TeaRequest REQUEST;
    protected final TeaAsyncHandler handler;

    protected MyDefaultAsyncClient(ClientConfiguration configuration) {
        this.handler = new TeaAsyncHandler(configuration);
        this.product = "pds";
        this.version = "2022-03-01";
        this.endpointRule = "";
        this.endpointMap = new HashMap();
        this.REQUEST = TeaRequest.create().setProduct(this.product).setEndpointRule(this.endpointRule).setEndpointMap(this.endpointMap).setVersion(this.version);
    }

    public void close() {
        this.handler.close();
    }

    @Override
    public CompletableFuture<ListFileResponse> listFile(ListFileRequest request) {
        try {
            this.handler.validateRequestModel(request);
            TeaRequest teaRequest = this.REQUEST.copy().setStyle(RequestStyle.RESTFUL).setAction("ListFile").setMethod(HttpMethod.POST).setPathRegex("/v2/file/list").setBodyType("json").setBodyIsForm(false).setReqBodyType("json").formModel(request);
            ClientExecutionParams params = (new ClientExecutionParams()).withInput(request).withRequest(teaRequest).withOutput(ListFileResponse.create());
            return this.handler.execute(params);
        } catch (Exception var4) {
            Exception e = var4;
            CompletableFuture<ListFileResponse> future = new CompletableFuture();
            future.completeExceptionally(e);
            return future;
        }
    }
}