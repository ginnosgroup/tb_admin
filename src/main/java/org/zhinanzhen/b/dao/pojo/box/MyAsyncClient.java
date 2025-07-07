package org.zhinanzhen.b.dao.pojo.box;

import com.aliyun.core.utils.SdkAutoCloseable;
import com.aliyun.sdk.service.pds20220301.models.ListFileRequest;

import java.util.concurrent.CompletableFuture;

public interface MyAsyncClient extends SdkAutoCloseable {
    static MyDefaultAsyncClientBuilder builder() {
        return new MyDefaultAsyncClientBuilder();
    }

    static MyAsyncClient create() {
        return (MyAsyncClient)builder().build();
    }

    CompletableFuture<ListFileResponse> listFile(ListFileRequest request);
}
