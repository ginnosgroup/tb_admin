package org.zhinanzhen.b.utils;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/** 统一解析座位票根图片在磁盘上的保存位置。 */
public final class SeatReservationFileStorage {

    private static final String STORAGE_ROOT_PROPERTY = "seat.storage.root";
    private static final String ALLOWED_PREFIX = "/uploads/seat_reservation/";

    private SeatReservationFileStorage() {
    }

    /**
     * Linux 默认保存到 /data，Windows 默认保存到当前工程所在盘的 data 目录。
     * 如需指定其他位置，可通过 JVM 参数 -Dseat.storage.root=绝对路径 覆盖。
     */
    public static File resolve(String webPath) throws IOException {
        String normalizedPath = StringUtils.trimToEmpty(webPath).replace('\\', '/');
        if (!normalizedPath.startsWith(ALLOWED_PREFIX)
                || normalizedPath.contains("..") || normalizedPath.indexOf('\0') >= 0) {
            throw new IOException("非法的票根图片路径");
        }

        String configuredRoot = System.getProperty(STORAGE_ROOT_PROPERTY);
        File root = StringUtils.isBlank(configuredRoot)
                ? new File("/data").getAbsoluteFile()
                : new File(configuredRoot).getAbsoluteFile();
        File canonicalRoot = root.getCanonicalFile();
        String relativePath = normalizedPath.substring(1);
        File target = new File(canonicalRoot, relativePath).getCanonicalFile();
        String rootPath = canonicalRoot.getPath();
        if (!target.getPath().startsWith(rootPath + File.separator)) {
            throw new IOException("票根图片路径超出存储目录");
        }
        return target;
    }
}
