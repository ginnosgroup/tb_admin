package org.zhinanzhen.b.service.pojo;

import lombok.Data;
import java.util.List;

/** A bounded, scoped set lookup. A successful empty result means ABSENT, not an error. */
@Data
public class SyncLookupRequest {
    private String driveId;
    private List<Key> keys;

    @Data
    public static class Key {
        private Integer userId;
        private String relativePath;
    }
}
