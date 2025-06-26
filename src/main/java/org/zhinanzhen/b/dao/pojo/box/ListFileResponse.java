package org.zhinanzhen.b.dao.pojo.box;

import com.aliyun.core.annotation.NameInMap;
import com.aliyun.sdk.gateway.pop.models.Response;

import java.util.Map;

public class ListFileResponse extends Response {
    @NameInMap("headers")
    private Map<String, String> headers;
    @NameInMap("statusCode")
    private Integer statusCode;
    @NameInMap("body")
    private ListFileResponseBody body;

    private ListFileResponse(BuilderImpl builder) {
        super(builder);
        this.headers = builder.headers;
        this.statusCode = builder.statusCode;
        this.body = builder.body;
    }

    public static ListFileResponse create() {
        return (new BuilderImpl()).build();
    }

    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }

    public ListFileResponseBody getBody() {
        return this.body;
    }

    private static final class BuilderImpl extends Response.BuilderImpl<ListFileResponse, Builder> implements Builder {
        private Map<String, String> headers;
        private Integer statusCode;
        private ListFileResponseBody body;

        private BuilderImpl() {
        }

        private BuilderImpl(ListFileResponse response) {
            super(response);
            this.headers = response.headers;
            this.statusCode = response.statusCode;
            this.body = response.body;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder body(ListFileResponseBody body) {
            this.body = body;
            return this;
        }

        public ListFileResponse build() {
            return new ListFileResponse(this);
        }
    }

    public interface Builder extends Response.Builder<ListFileResponse, Builder> {
        Builder headers(Map<String, String> var1);

        Builder statusCode(Integer var1);

        Builder body(ListFileResponseBody var1);

        ListFileResponse build();
    }
}
