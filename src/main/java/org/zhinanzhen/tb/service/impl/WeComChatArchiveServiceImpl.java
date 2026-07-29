package org.zhinanzhen.tb.service.impl;

import cn.hutool.crypto.PemUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zhinanzhen.tb.dao.WeComChatArchiveDAO;
import org.zhinanzhen.tb.dao.pojo.WeComChatMessageDO;
import org.zhinanzhen.tb.dao.pojo.WeComChatParticipantDO;
import org.zhinanzhen.tb.dao.pojo.WeComChatSyncStateDO;
import org.zhinanzhen.tb.service.WeComChatArchiveService;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WeComChatArchiveServiceImpl implements WeComChatArchiveService {

    private static final String SYNC_KEY = "DATA_INTELLIGENCE_SYNC_MSG";
    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @Resource
    private WeComChatArchiveDAO weComChatArchiveDAO;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.social.wecom.app-id}")
    private String corpId;

    @Value("${spring.social.wecom.app-secret}")
    private String applicationSecret;

    @Value("${wecom.chat.archive.program-id:"
            + "prog6vL2uqnEg8GPqd2IeQC4TcGExKm0Ph5Z}")
    private String programId;

    @Value("${wecom.chat.archive.ability-id:invoke_sync_msg}")
    private String abilityId;

    @Value("${wecom.chat.archive.callback-token:}")
    private String callbackToken;

    @Value("${wecom.chat.archive.notify-id:}")
    private String notifyId;

    @Value("${wecom.chat.archive.rsa-private-key-path:"
            + "/opt/wecom/keys/wecom_private_key.pem}")
    private String rsaPrivateKeyPath;

    @Value("${wecom.chat.archive.page-size:500}")
    private int syncPageSize;

    @Value("${wecom.chat.archive.max-pages-per-sync:10}")
    private int maxPagesPerSync;

    @Value("${wecom.chat.archive.enabled:false}")
    private boolean scheduledSyncEnabled;

    private final Object syncLock = new Object();

    @Scheduled(fixedDelayString = "${wecom.chat.archive.sync-interval-ms:300000}")
    public void scheduledSync() {
        if (!scheduledSyncEnabled) {
            return;
        }
        try {
            JSONObject result = syncNow();
            log.info("Scheduled WeCom chat archive sync completed: {}", result.toJSONString());
        } catch (Exception ex) {
            log.error("Scheduled WeCom chat archive sync failed", ex);
        }
    }

    @Override
    public JSONObject syncNow() throws Exception {
        synchronized (syncLock) {
            validateSyncConfiguration();
            PrivateKey privateKey = readPrivateKey();
            String accessToken = getApplicationAccessToken();
            WeComChatSyncStateDO state = weComChatArchiveDAO.getSyncState(SYNC_KEY);
            String cursor = state == null ? "" : emptyIfNull(state.getNextCursor());
            int totalProcessed = 0;
            int pages = 0;
            int hasMore = 0;

            try {
                while (pages < maxPagesPerSync) {
                    JSONObject response = callSyncMessage(accessToken, cursor);
                    JSONArray messages = response.getJSONArray("msg_list");
                    int pageCount = messages == null ? 0 : messages.size();

                    for (int i = 0; i < pageCount; i++) {
                        persistMessage(messages.getJSONObject(i), privateKey);
                    }

                    String nextCursor = response.getString("next_cursor");
                    hasMore = response.getIntValue("has_more");
                    if (!isBlank(nextCursor)) {
                        cursor = nextCursor;
                    }
                    weComChatArchiveDAO.saveSyncState(
                            SYNC_KEY, cursor, hasMore, pageCount, null);
                    totalProcessed += pageCount;
                    pages++;

                    if (hasMore != 1 || pageCount == 0 || isBlank(cursor)) {
                        break;
                    }
                }
            } catch (Exception ex) {
                weComChatArchiveDAO.saveSyncState(
                        SYNC_KEY, cursor, hasMore, 0, abbreviate(ex.getMessage(), 4000));
                throw ex;
            }

            JSONObject result = getArchiveStatus();
            result.put("processedThisSync", totalProcessed);
            result.put("pagesThisSync", pages);
            result.put("hasMore", hasMore);
            result.put("nextCursor", cursor);
            return result;
        }
    }

    @Override
    public JSONObject getArchiveStatus() {
        WeComChatSyncStateDO state = weComChatArchiveDAO.getSyncState(SYNC_KEY);
        JSONObject result = new JSONObject();
        result.put("messageCount", weComChatArchiveDAO.countAllMessages());
        result.put("earliestMessageTime", weComChatArchiveDAO.getEarliestMessageTime());
        result.put("latestMessageTime", weComChatArchiveDAO.getLatestMessageTime());
        result.put("lastSyncTime", state == null || state.getLastSyncTime() == null
                ? null : state.getLastSyncTime().getTime());
        result.put("hasMore", state == null ? 0 : state.getHasMore());
        result.put("lastError", state == null ? null : state.getLastError());
        result.put("scheduledSyncEnabled", scheduledSyncEnabled);
        result.put("syncConfigured", !isBlank(rsaPrivateKeyPath)
                && !isBlank(programId) && !isBlank(abilityId));
        return result;
    }

    @Override
    public JSONObject queryMessages(String employeeUserId,
                                    String externalUserId,
                                    long startTime,
                                    long endTime,
                                    int pageNum,
                                    int pageSize) {
        int directTotal = weComChatArchiveDAO.countDirectMessages(
                employeeUserId, externalUserId, startTime, endTime);
        int groupTotal = weComChatArchiveDAO.countGroupMessages(
                employeeUserId, externalUserId, startTime, endTime);
        int total = directTotal + groupTotal;
        int offset = pageNum * pageSize;
        List<WeComChatMessageDO> messages = weComChatArchiveDAO.listMessages(
                employeeUserId, externalUserId, startTime, endTime, offset, pageSize);

        JSONArray directMessages = new JSONArray();
        JSONArray groupMessages = new JSONArray();
        for (WeComChatMessageDO message : messages) {
            JSONObject item = toPreviewMessage(message);
            if ("GROUP".equals(message.getConversationType())) {
                groupMessages.add(item);
            } else {
                directMessages.add(item);
            }
        }

        JSONObject result = new JSONObject();
        result.put("directMessages", directMessages);
        result.put("groupMessages", groupMessages);
        result.put("directTotal", directTotal);
        result.put("groupTotal", groupTotal);
        result.put("total", total);
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("hasMore", offset + messages.size() < total);
        result.put("returnedCount", messages.size());
        return result;
    }

    private void persistMessage(JSONObject source, PrivateKey privateKey) throws Exception {
        String msgId = source.getString("msgid");
        if (isBlank(msgId)) {
            throw new IllegalStateException("sync_msg returned a message without msgid");
        }
        JSONObject encryptInfo = source.getJSONObject("service_encrypt_info");
        if (encryptInfo == null) {
            throw new IllegalStateException(
                    "sync_msg message has no service_encrypt_info, msgid=" + msgId);
        }
        String secretKey = decryptSecretKey(
                encryptInfo.getString("encrypted_secret_key"), privateKey);

        WeComChatMessageDO message = new WeComChatMessageDO();
        message.setMsgId(msgId);
        message.setSendTimeEpochMillis(toEpochMillis(source.getLongValue("send_time")));
        message.setSenderJson(jsonValueToText(source.get("sender")));
        message.setReceiverJson(jsonValueToText(source.get("receiver_list")));
        message.setChatId(emptyToNull(source.getString("chatid")));
        Object msgType = source.get("msgtype");
        message.setMsgType(msgType == null ? null : String.valueOf(msgType));
        message.setSecretKey(secretKey);
        weComChatArchiveDAO.upsertMessage(message);

        Map<String, WeComChatParticipantDO> participants = new LinkedHashMap<>();
        collectParticipant(participants, msgId, message.getChatId(),
                source.get("sender"), "SENDER");
        Object receiverList = source.get("receiver_list");
        JSONArray receivers = receiverList instanceof JSONArray
                ? (JSONArray) receiverList : JSONArray.parseArray(
                receiverList == null ? "[]" : String.valueOf(receiverList));
        for (int i = 0; receivers != null && i < receivers.size(); i++) {
            collectParticipant(participants, msgId, message.getChatId(),
                    receivers.get(i), "RECEIVER");
        }

        for (WeComChatParticipantDO participant : participants.values()) {
            weComChatArchiveDAO.insertMessageParticipant(participant);
            if (!isBlank(message.getChatId())) {
                weComChatArchiveDAO.insertChatParticipant(participant);
            }
        }
    }

    private void collectParticipant(Map<String, WeComChatParticipantDO> participants,
                                    String msgId,
                                    String chatId,
                                    Object source,
                                    String role) {
        if (source == null) {
            return;
        }
        JSONObject json = source instanceof JSONObject
                ? (JSONObject) source : JSONObject.parseObject(String.valueOf(source));
        String participantId = json.getString("id");
        if (isBlank(participantId)) {
            return;
        }
        WeComChatParticipantDO participant = participants.get(participantId);
        if (participant == null) {
            participant = new WeComChatParticipantDO();
            participant.setMsgId(msgId);
            participant.setChatId(chatId);
            participant.setParticipantId(participantId);
            participant.setParticipantType(json.getInteger("type"));
            participant.setParticipantRole(role);
            participants.put(participantId, participant);
        } else if ("SENDER".equals(role)) {
            participant.setParticipantRole(role);
        }
    }

    private JSONObject callSyncMessage(String accessToken, String cursor) throws Exception {
        JSONObject syncRequest = new JSONObject();
        if (!isBlank(cursor)) {
            syncRequest.put("cursor", cursor);
        }
        if (!isBlank(callbackToken)) {
            syncRequest.put("token", callbackToken);
        }
        syncRequest.put("limit", syncPageSize);

        JSONObject programRequest = new JSONObject();
        programRequest.put("program_id", programId);
        programRequest.put("ability_id", abilityId);
        if (!isBlank(notifyId)) {
            programRequest.put("notify_id", notifyId);
        }
        programRequest.put("request_data", syncRequest.toJSONString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<>(programRequest.toJSONString(), headers);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/chatdata/sync_call_program"
                + "?access_token=" + urlEncode(accessToken);
        String responseText = restTemplate.postForObject(url, entity, String.class);
        JSONObject response = JSONObject.parseObject(responseText);
        checkWeComResponse("sync_call_program", response);
        String responseData = response.getString("response_data");
        if (isBlank(responseData)) {
            throw new IllegalStateException("sync_call_program returned empty response_data");
        }
        JSONObject syncResponse = JSONObject.parseObject(responseData);
        checkWeComResponse("sync_msg", syncResponse);
        return syncResponse;
    }

    private String getApplicationAccessToken() throws Exception {
        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
                + urlEncode(corpId) + "&corpsecret=" + urlEncode(applicationSecret);
        JSONObject response = JSONObject.parseObject(
                restTemplate.getForObject(url, String.class));
        checkWeComResponse("gettoken", response);
        String accessToken = response.getString("access_token");
        if (isBlank(accessToken)) {
            throw new IllegalStateException("gettoken returned an empty access_token");
        }
        return accessToken;
    }

    private PrivateKey readPrivateKey() throws Exception {
        Path path = Paths.get(rsaPrivateKeyPath).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "企业微信会话存档 RSA 私钥文件不存在：" + path);
        }
        try (InputStream inputStream = Files.newInputStream(path)) {
            PrivateKey privateKey = PemUtil.readPemPrivateKey(inputStream);
            if (privateKey == null) {
                throw new IllegalArgumentException(
                        "无法读取企业微信会话存档 RSA 私钥：" + path);
            }
            return privateKey;
        }
    }

    private String decryptSecretKey(String encryptedSecretKey, PrivateKey privateKey)
            throws Exception {
        if (isBlank(encryptedSecretKey)) {
            throw new IllegalArgumentException("encrypted_secret_key 为空");
        }
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(
                Base64.getDecoder().decode(encryptedSecretKey));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    private JSONObject toPreviewMessage(WeComChatMessageDO message) {
        JSONObject item = new JSONObject();
        item.put("msgid", message.getMsgId());
        item.put("secretKey", message.getSecretKey());
        item.put("sendTimeEpochMillis", message.getSendTimeEpochMillis());
        item.put("sendTimeText", formatMessageTime(message.getSendTimeEpochMillis()));
        item.put("sender", parseJsonValue(message.getSenderJson()));
        item.put("receiverList", parseJsonValue(message.getReceiverJson()));
        item.put("senderText", emptyIfNull(message.getSenderJson()));
        item.put("receiverText", emptyIfNull(message.getReceiverJson()));
        item.put("chatId", emptyIfNull(message.getChatId()));
        item.put("msgType", emptyIfNull(message.getMsgType()));
        item.put("conversationType", message.getConversationType());
        return item;
    }

    private Object parseJsonValue(String value) {
        if (isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("[")) {
            return JSONArray.parseArray(trimmed);
        }
        if (trimmed.startsWith("{")) {
            return JSONObject.parseObject(trimmed);
        }
        return value;
    }

    private void validateSyncConfiguration() {
        StringBuilder missing = new StringBuilder();
        appendMissing(missing, "spring.social.wecom.app-id(corpId)", corpId);
        appendMissing(missing, "spring.social.wecom.app-secret(appSecret)",
                applicationSecret);
        appendMissing(missing, "wecom.chat.archive.program-id(programId)", programId);
        appendMissing(missing, "wecom.chat.archive.ability-id(abilityId)", abilityId);
        appendMissing(missing,
                "wecom.chat.archive.rsa-private-key-path(RSA私钥路径)",
                rsaPrivateKeyPath);
        if (missing.length() > 0) {
            throw new IllegalArgumentException(
                    "企业微信会话存档同步配置不完整，缺少：" + missing);
        }
        if (syncPageSize < 1 || syncPageSize > 1000) {
            throw new IllegalArgumentException(
                    "wecom.chat.archive.page-size 必须在 1 到 1000 之间");
        }
        if (maxPagesPerSync < 1) {
            throw new IllegalArgumentException(
                    "wecom.chat.archive.max-pages-per-sync 必须大于 0");
        }
    }

    private static void appendMissing(
            StringBuilder missing, String propertyName, String value) {
        if (!isBlank(value)) {
            return;
        }
        if (missing.length() > 0) {
            missing.append("、");
        }
        missing.append(propertyName);
    }

    private static void checkWeComResponse(String operation, JSONObject response) {
        if (response == null) {
            throw new IllegalStateException(operation + " returned an empty response");
        }
        int errorCode = response.getIntValue("errcode");
        if (errorCode != 0) {
            throw new IllegalStateException(operation + " response error, errcode="
                    + errorCode + ", errmsg=" + response.getString("errmsg"));
        }
    }

    private static long toEpochMillis(long timestamp) {
        if (timestamp <= 0L) {
            return 0L;
        }
        return timestamp >= 100000000000L ? timestamp : timestamp * 1000L;
    }

    private static String formatMessageTime(Long epochMillis) {
        if (epochMillis == null || epochMillis <= 0L) {
            return "";
        }
        return MESSAGE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis));
    }

    private static String jsonValueToText(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof JSONObject) {
            return ((JSONObject) value).toJSONString();
        }
        if (value instanceof JSONArray) {
            return ((JSONArray) value).toJSONString();
        }
        return String.valueOf(value);
    }

    private static String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private static String abbreviate(String value, int maximumLength) {
        if (value == null || value.length() <= maximumLength) {
            return value;
        }
        return value.substring(0, maximumLength);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
