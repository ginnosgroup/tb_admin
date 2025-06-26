package org.zhinanzhen.b.dao.pojo.box;

import com.aliyun.core.annotation.NameInMap;
import darabonba.core.TeaModel;

import java.util.List;

public class ListFileResponseBody extends TeaModel {
    @NameInMap("items")
    private List<File> items;
    @NameInMap("next_marker")
    private String nextMarker;

    private ListFileResponseBody(Builder builder) {
        this.items = builder.items;
        this.nextMarker = builder.nextMarker;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ListFileResponseBody create() {
        return builder().build();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public List<File> getItems() {
        return this.items;
    }

    public String getNextMarker() {
        return this.nextMarker;
    }

    public static final class Builder {
        private List<File> items;
        private String nextMarker;

        private Builder() {
        }

        private Builder(ListFileResponseBody model) {
            this.items = model.items;
            this.nextMarker = model.nextMarker;
        }

        public Builder items(List<File> items) {
            this.items = items;
            return this;
        }

        public Builder nextMarker(String nextMarker) {
            this.nextMarker = nextMarker;
            return this;
        }

        public ListFileResponseBody build() {
            return new ListFileResponseBody(this);
        }
    }
}