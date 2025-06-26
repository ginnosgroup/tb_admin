package org.zhinanzhen.b.dao.pojo.box;

import com.aliyun.sdk.gateway.pop.BaseClientBuilder;

public final class MyDefaultAsyncClientBuilder extends BaseClientBuilder<MyDefaultAsyncClientBuilder, MyAsyncClient> {
    public MyDefaultAsyncClientBuilder() {
    }

    protected String serviceName() {
        return "pds20220301";
    }

    protected final MyAsyncClient buildClient() {
        return new MyDefaultAsyncClient(super.applyClientConfiguration());
    }
}