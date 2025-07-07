package org.zhinanzhen.b.dao.pojo.box;

import com.aliyun.core.annotation.NameInMap;
import com.aliyun.sdk.service.pds20220301.models.ImageMediaMetadata;
import com.aliyun.sdk.service.pds20220301.models.VideoMediaMetadata;
import darabonba.core.TeaModel;

import java.util.List;
import java.util.Map;

public class File extends TeaModel {
    @NameInMap("action_list")
    private List<String> actionList;
    @NameInMap("auto_delete_left_sec")
    private Long autoDeleteLeftSec;
    @NameInMap("category")
    private String category;
    @NameInMap("content_hash")
    private String contentHash;
    @NameInMap("content_hash_name")
    private String contentHashName;
    @NameInMap("content_type")
    private String contentType;
    @NameInMap("crc64_hash")
    private String crc64Hash;
    @NameInMap("created_at")
    private String createdAt;
    @NameInMap("description")
    private String description;
    @NameInMap("dir_size_info")
    private DirSizeInfo dirSizeInfo;
    @NameInMap("domain_id")
    private String domainId;
    @NameInMap("download_url")
    private String downloadUrl;
    @NameInMap("drive_id")
    private String driveId;
    @NameInMap("file_extension")
    private String fileExtension;
    @NameInMap("file_id")
    private String fileId;
    @NameInMap("hidden")
    private Boolean hidden;
    @NameInMap("id_path")
    private String idPath;
    @NameInMap("image_media_metadata")
    private ImageMediaMetadata imageMediaMetadata;
    @NameInMap("labels")
    private List<String> labels;
    @NameInMap("local_created_at")
    private String localCreatedAt;
    @NameInMap("local_modified_at")
    private String localModifiedAt;
    @NameInMap("name")
    private String name;
    @NameInMap("name_path")
    private String namePath;
    @NameInMap("parent_file_id")
    private String parentFileId;
    @NameInMap("revision_id")
    private String revisionId;
    @NameInMap("size")
    private Long size;
    @NameInMap("starred")
    private Boolean starred;
    @NameInMap("status")
    private String status;
    @NameInMap("thumbnail")
    private String thumbnail;
    @NameInMap("thumbnail_urls")
    private Map<String, String> thumbnailUrls;
    @NameInMap("trashed_at")
    private String trashedAt;
    @NameInMap("type")
    private String type;
    @NameInMap("updated_at")
    private String updatedAt;
    @NameInMap("upload_id")
    private String uploadId;
    @NameInMap("user_tags")
    private Map<String, String> userTags;
    @NameInMap("video_media_metadata")
    private VideoMediaMetadata videoMediaMetadata;

    /**
     * 自定义
     */
    @NameInMap("creator_name")
    private String creatorName;
    @NameInMap("last_modifier_type")
    private String lastModifierType;
    @NameInMap("last_modifier_id")
    private String lastModifierId;
    @NameInMap("last_modifier_name")
    private String lastModifierName;


    private File(Builder builder) {
        this.actionList = builder.actionList;
        this.autoDeleteLeftSec = builder.autoDeleteLeftSec;
        this.category = builder.category;
        this.contentHash = builder.contentHash;
        this.contentHashName = builder.contentHashName;
        this.contentType = builder.contentType;
        this.crc64Hash = builder.crc64Hash;
        this.createdAt = builder.createdAt;
        this.description = builder.description;
        this.dirSizeInfo = builder.dirSizeInfo;
        this.domainId = builder.domainId;
        this.downloadUrl = builder.downloadUrl;
        this.driveId = builder.driveId;
        this.fileExtension = builder.fileExtension;
        this.fileId = builder.fileId;
        this.hidden = builder.hidden;
        this.idPath = builder.idPath;
        this.imageMediaMetadata = builder.imageMediaMetadata;
        this.labels = builder.labels;
        this.localCreatedAt = builder.localCreatedAt;
        this.localModifiedAt = builder.localModifiedAt;
        this.name = builder.name;
        this.namePath = builder.namePath;
        this.parentFileId = builder.parentFileId;
        this.revisionId = builder.revisionId;
        this.size = builder.size;
        this.starred = builder.starred;
        this.status = builder.status;
        this.thumbnail = builder.thumbnail;
        this.thumbnailUrls = builder.thumbnailUrls;
        this.trashedAt = builder.trashedAt;
        this.type = builder.type;
        this.updatedAt = builder.updatedAt;
        this.uploadId = builder.uploadId;
        this.userTags = builder.userTags;
        this.videoMediaMetadata = builder.videoMediaMetadata;

        /**
         * 自定义
         */
        this.creatorName = builder.creatorName;
        this.lastModifierType = builder.lastModifierType;
        this.lastModifierId = builder.lastModifierId;
        this.lastModifierName = builder.lastModifierName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static File create() {
        return builder().build();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public List<String> getActionList() {
        return this.actionList;
    }

    public Long getAutoDeleteLeftSec() {
        return this.autoDeleteLeftSec;
    }

    public String getCategory() {
        return this.category;
    }

    public String getContentHash() {
        return this.contentHash;
    }

    public String getContentHashName() {
        return this.contentHashName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getCrc64Hash() {
        return this.crc64Hash;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public String getDescription() {
        return this.description;
    }

    public DirSizeInfo getDirSizeInfo() {
        return this.dirSizeInfo;
    }

    public String getDomainId() {
        return this.domainId;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public String getDriveId() {
        return this.driveId;
    }

    public String getFileExtension() {
        return this.fileExtension;
    }

    public String getFileId() {
        return this.fileId;
    }

    public Boolean getHidden() {
        return this.hidden;
    }

    public String getIdPath() {
        return this.idPath;
    }

    public ImageMediaMetadata getImageMediaMetadata() {
        return this.imageMediaMetadata;
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public String getLocalCreatedAt() {
        return this.localCreatedAt;
    }

    public String getLocalModifiedAt() {
        return this.localModifiedAt;
    }

    public String getName() {
        return this.name;
    }

    public String getNamePath() {
        return this.namePath;
    }

    public String getParentFileId() {
        return this.parentFileId;
    }

    public String getRevisionId() {
        return this.revisionId;
    }

    public Long getSize() {
        return this.size;
    }

    public Boolean getStarred() {
        return this.starred;
    }

    public String getStatus() {
        return this.status;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public Map<String, String> getThumbnailUrls() {
        return this.thumbnailUrls;
    }

    public String getTrashedAt() {
        return this.trashedAt;
    }

    public String getType() {
        return this.type;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public String getUploadId() {
        return this.uploadId;
    }

    public Map<String, String> getUserTags() {
        return this.userTags;
    }

    public VideoMediaMetadata getVideoMediaMetadata() {
        return this.videoMediaMetadata;
    }

    /**
     * 自定义
     */
    public String getCreatorName() {
        return this.creatorName;
    }

    public String getLastModifierType() {
        return this.lastModifierType;
    }

    public String getLastModifierId() {
        return this.lastModifierId;
    }

    public String getLastModifierName() {
        return this.lastModifierName;
    }

    public static class DirSizeInfo extends TeaModel {
        @NameInMap("dir_count")
        private Long dirCount;
        @NameInMap("file_count")
        private Long fileCount;

        private DirSizeInfo(Builder builder) {
            this.dirCount = builder.dirCount;
            this.fileCount = builder.fileCount;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static DirSizeInfo create() {
            return builder().build();
        }

        public Long getDirCount() {
            return this.dirCount;
        }

        public Long getFileCount() {
            return this.fileCount;
        }

        public static final class Builder {
            private Long dirCount;
            private Long fileCount;

            private Builder() {
            }

            private Builder(DirSizeInfo model) {
                this.dirCount = model.dirCount;
                this.fileCount = model.fileCount;
            }

            public Builder dirCount(Long dirCount) {
                this.dirCount = dirCount;
                return this;
            }

            public Builder fileCount(Long fileCount) {
                this.fileCount = fileCount;
                return this;
            }

            public DirSizeInfo build() {
                return new DirSizeInfo(this);
            }
        }
    }

    public static final class Builder {
        private List<String> actionList;
        private Long autoDeleteLeftSec;
        private String category;
        private String contentHash;
        private String contentHashName;
        private String contentType;
        private String crc64Hash;
        private String createdAt;
        private String description;
        private DirSizeInfo dirSizeInfo;
        private String domainId;
        private String downloadUrl;
        private String driveId;
        private String fileExtension;
        private String fileId;
        private Boolean hidden;
        private String idPath;
        private ImageMediaMetadata imageMediaMetadata;
        private List<String> labels;
        private String localCreatedAt;
        private String localModifiedAt;
        private String name;
        private String namePath;
        private String parentFileId;
        private String revisionId;
        private Long size;
        private Boolean starred;
        private String status;
        private String thumbnail;
        private Map<String, String> thumbnailUrls;
        private String trashedAt;
        private String type;
        private String updatedAt;
        private String uploadId;
        private Map<String, String> userTags;
        private VideoMediaMetadata videoMediaMetadata;

        /**
         * 自定义
         */
        private String creatorName;
        private String lastModifierType;
        private String lastModifierId;
        private String lastModifierName;

        private Builder() {
        }

        private Builder(File model) {
            this.actionList = model.actionList;
            this.autoDeleteLeftSec = model.autoDeleteLeftSec;
            this.category = model.category;
            this.contentHash = model.contentHash;
            this.contentHashName = model.contentHashName;
            this.contentType = model.contentType;
            this.crc64Hash = model.crc64Hash;
            this.createdAt = model.createdAt;
            this.description = model.description;
            this.dirSizeInfo = model.dirSizeInfo;
            this.domainId = model.domainId;
            this.downloadUrl = model.downloadUrl;
            this.driveId = model.driveId;
            this.fileExtension = model.fileExtension;
            this.fileId = model.fileId;
            this.hidden = model.hidden;
            this.idPath = model.idPath;
            this.imageMediaMetadata = model.imageMediaMetadata;
            this.labels = model.labels;
            this.localCreatedAt = model.localCreatedAt;
            this.localModifiedAt = model.localModifiedAt;
            this.name = model.name;
            this.namePath = model.namePath;
            this.parentFileId = model.parentFileId;
            this.revisionId = model.revisionId;
            this.size = model.size;
            this.starred = model.starred;
            this.status = model.status;
            this.thumbnail = model.thumbnail;
            this.thumbnailUrls = model.thumbnailUrls;
            this.trashedAt = model.trashedAt;
            this.type = model.type;
            this.updatedAt = model.updatedAt;
            this.uploadId = model.uploadId;
            this.userTags = model.userTags;
            this.videoMediaMetadata = model.videoMediaMetadata;

            /**
             * 自定义
             */
            this.creatorName = model.creatorName;
            this.lastModifierType = model.lastModifierType;
            this.lastModifierId = model.lastModifierId;
            this.lastModifierName = model.lastModifierName;
        }

        public Builder actionList(List<String> actionList) {
            this.actionList = actionList;
            return this;
        }

        public Builder autoDeleteLeftSec(Long autoDeleteLeftSec) {
            this.autoDeleteLeftSec = autoDeleteLeftSec;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder contentHash(String contentHash) {
            this.contentHash = contentHash;
            return this;
        }

        public Builder contentHashName(String contentHashName) {
            this.contentHashName = contentHashName;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder crc64Hash(String crc64Hash) {
            this.crc64Hash = crc64Hash;
            return this;
        }

        public Builder createdAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder dirSizeInfo(DirSizeInfo dirSizeInfo) {
            this.dirSizeInfo = dirSizeInfo;
            return this;
        }

        public Builder domainId(String domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder driveId(String driveId) {
            this.driveId = driveId;
            return this;
        }

        public Builder fileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
            return this;
        }

        public Builder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder hidden(Boolean hidden) {
            this.hidden = hidden;
            return this;
        }

        public Builder idPath(String idPath) {
            this.idPath = idPath;
            return this;
        }

        public Builder imageMediaMetadata(ImageMediaMetadata imageMediaMetadata) {
            this.imageMediaMetadata = imageMediaMetadata;
            return this;
        }

        public Builder labels(List<String> labels) {
            this.labels = labels;
            return this;
        }

        public Builder localCreatedAt(String localCreatedAt) {
            this.localCreatedAt = localCreatedAt;
            return this;
        }

        public Builder localModifiedAt(String localModifiedAt) {
            this.localModifiedAt = localModifiedAt;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder namePath(String namePath) {
            this.namePath = namePath;
            return this;
        }

        public Builder parentFileId(String parentFileId) {
            this.parentFileId = parentFileId;
            return this;
        }

        public Builder revisionId(String revisionId) {
            this.revisionId = revisionId;
            return this;
        }

        public Builder size(Long size) {
            this.size = size;
            return this;
        }

        public Builder starred(Boolean starred) {
            this.starred = starred;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public Builder thumbnailUrls(Map<String, String> thumbnailUrls) {
            this.thumbnailUrls = thumbnailUrls;
            return this;
        }

        public Builder trashedAt(String trashedAt) {
            this.trashedAt = trashedAt;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder updatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder uploadId(String uploadId) {
            this.uploadId = uploadId;
            return this;
        }

        public Builder userTags(Map<String, String> userTags) {
            this.userTags = userTags;
            return this;
        }

        public Builder videoMediaMetadata(VideoMediaMetadata videoMediaMetadata) {
            this.videoMediaMetadata = videoMediaMetadata;
            return this;
        }

        public Builder creatorName(String creatorName) {
            this.creatorName = creatorName;
            return this;
        }
        public Builder lastModifierType(String lastModifierType) {
            this.lastModifierType = lastModifierType;
            return this;
        }
        public Builder lastModifierId(String lastModifierId) {
            this.lastModifierId = lastModifierId;
            return this;
        }
        public Builder lastModifierName(String lastModifierName) {
            this.lastModifierName = lastModifierName;
            return this;
        }


        public File build() {
            return new File(this);
        }
    }
}