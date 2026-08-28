package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zhinanzhen.b.service.LowPriceApprovalImageAnalyzer;
import org.zhinanzhen.tb.controller.Response;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/ChartForAI")
@Slf4j
public class ChartForAI {

    private static final String DEEPSEEK_CHAT_COMPLETIONS_URL = "https://api.deepseek.com/chat/completions";

    private static final String PDF_ANALYSIS_PROMPT =
            "请从PDF文字中提取信息，并且只返回一个合法的JSON对象。\n"
                    + "必须严格使用以下三个字段，不要返回Markdown代码块、解释或其他字段：\n"
                    + "{\"name\":\"学生姓名\",\"amount\":700,\"service\":\"服务项目\"}\n"
                    + "规则：\n"
                    + "1. name：客户或学生姓名，未找到时返回null。\n"
                    + "2. amount：收款金额，优先取Total或总金额；必须返回JSON数字，不包含币种、逗号或单位；"
                    + "不能把签证类别或Subclass编号当作金额，未找到时返回null。\n"
                    + "3. service：服务项目，可以包含服务类别和Subclass编号，未找到时返回null。\n"
                    + "4. 只能根据PDF文字提取，不要猜测。";

    /** 问题为空时使用的默认文字提取指令（问题暂时留空，由调用方填充）。 */
    private static final String DEFAULT_TEXT_EXTRACT_PROMPT =
            "请提取这份文件中的全部文字内容并原样返回；如果文件中没有文字，请直接说明。";
    private static final int MAX_OCR_TEXT_LENGTH = 30000;

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build();

    /**
     * 请在 application.properties 的 deepseek.pdf.api.key 配置项中填写 API Key。
     */
    @Value("${deepseek.pdf.api.key:}")
    private String deepSeekApiKey;

    @Value("${deepseek.pdf.model:deepseek-v4-flash}")
    private String deepSeekModel;

    @Resource
    private LowPriceApprovalImageAnalyzer imageOcrService;

    /**
     * 接收 PDF 并交给 AI 分析。
     *
     * 请求方式：POST /ChartForAI/analyzePdf，表单字段名：file。
     */
    @RequestMapping(value = "/analyzePdf", method = RequestMethod.POST)
    @ResponseBody
    public Response<JSONObject> analyzePdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new Response<JSONObject>(1, "请上传PDF文件", null);
        }

        try {
            return analyzePdf(file.getBytes(), getPdfFilename(file));
        } catch (IOException e) {
            log.error("读取PDF文件失败", e);
            return new Response<JSONObject>(1, "读取PDF文件失败: " + e.getMessage(), null);
        }
    }

    /**
     * 分析服务器本地已有的 PDF 文件，供其他业务流程复用。
     */
    public Response<JSONObject> analyzePdf(File file) {
        if (file == null || !file.isFile() || file.length() == 0) {
            return new Response<JSONObject>(1, "PDF文件不存在或为空", null);
        }

        try {
            return analyzePdf(Files.readAllBytes(file.toPath()), file.getName());
        } catch (IOException e) {
            log.error("读取PDF文件失败, file={}", file.getAbsolutePath(), e);
            return new Response<JSONObject>(1, "读取PDF文件失败: " + e.getMessage(), null);
        }
    }

    private Response<JSONObject> analyzePdf(byte[] pdfBytes, String filename) {
        if (deepSeekApiKey == null || deepSeekApiKey.trim().isEmpty()) {
            return new Response<JSONObject>(1, "请先在application.properties中填写deepseek.pdf.api.key", null);
        }

        try {
            if (!isPdf(pdfBytes)) {
                return new Response<JSONObject>(1, "上传的文件不是有效的PDF", null);
            }

            String pdfText = extractPdfText(pdfBytes);
            if (pdfText.isEmpty()) {
                return new Response<JSONObject>(1,
                        "未从PDF中提取到文字，文件可能是扫描件；DeepSeek API不能直接读取PDF，请先进行OCR", null);
            }

            String result = requestDeepSeek(PDF_ANALYSIS_PROMPT
                    + "\n\n以下是从PDF中提取的文字内容：\n" + pdfText, true);
            JSONObject resultJson = parsePdfAnalysisResult(result);
            log.info("PDF DeepSeek分析结果: {}", resultJson.toJSONString());
            System.out.println("PDF DeepSeek分析结果: " + resultJson.toJSONString());
            return new Response<JSONObject>(0, "分析成功", resultJson);
        } catch (Exception e) {
            log.error("PDF DeepSeek分析失败", e);
            return new Response<JSONObject>(1, "PDF DeepSeek分析失败: " + e.getMessage(), null);
        }
    }

    /**
     * 调取deepseek进行文字提取。
     *
     * 接收前端上传的图片或PDF文件，将文件内容连同问题一起发送给DeepSeek进行文字提取。
     * 图片先通过腾讯云OCR提取文字，PDF先在本地提取文字，再把纯文本传给DeepSeek。
     *
     * 请求方式：POST /ChartForAI/analyzeFile，表单字段名：file，可选字段：question。
     * 问题暂未由前端传入（当前留空），默认使用文字提取指令，后续在 resolveQuestion 中填充即可。
     */
    @RequestMapping(value = "/analyzeFile", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> analyzeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "question", required = false) String question) {
        if (file == null || file.isEmpty()) {
            return new Response<String>(1, "请上传文件", null);
        }

        try {
            return analyzeFile(file.getBytes(), file.getOriginalFilename(), question);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            return new Response<String>(1, "读取上传文件失败: " + e.getMessage(), null);
        }
    }

    /**
     * 调取deepseek进行文字提取（字节流版本，供其他业务流程复用）。
     * 注意：MultipartFile 被 transferTo 消费后临时文件会被移动，无法再调用 file.getBytes()，
     * 复用方应在消费 MultipartFile 之前先读取字节再传入本方法。
     */
    public Response<String> analyzeFile(byte[] fileBytes, String filename, String question) {
        return analyzeFile(fileBytes, filename, question, false);
    }

    /**
     * 调取deepseek进行文字提取（支持指定JSON响应模式）。
     *
     * @param jsonMode true时要求DeepSeek只返回合法JSON对象
     */
    public Response<String> analyzeFile(byte[] fileBytes, String filename, String question, boolean jsonMode) {
        if (deepSeekApiKey == null || deepSeekApiKey.trim().isEmpty()) {
            return new Response<String>(1, "请先在application.properties中填写deepseek.pdf.api.key", null);
        }

        try {
            String result;
            if (isImage(fileBytes)) {
                String imageText = imageOcrService.extractText(fileBytes);
                if (StringUtils.isBlank(imageText)) {
                    return new Response<String>(1, "未从图片中识别到文字", null);
                }
                result = requestDeepSeek(resolveQuestion(question)
                        + "\n\n以下是从图片OCR中提取的文字内容：\n" + truncateOcrText(imageText), jsonMode);
            } else if (isPdf(fileBytes)) {
                String pdfText = extractPdfText(fileBytes);
                if (pdfText.isEmpty()) {
                    return new Response<String>(1,
                            "未从PDF中提取到文字，文件可能是扫描件；DeepSeek API不能直接读取PDF，请先进行OCR", null);
                }
                result = requestDeepSeek(resolveQuestion(question)
                        + "\n\n以下是从PDF中提取的文字内容：\n" + pdfText, jsonMode);
            } else {
                return new Response<String>(1, "仅支持图片或PDF文件", null);
            }

            log.info("DeepSeek文字提取结果: {}", result);
            return new Response<String>(0, "提取成功", result);
        } catch (Exception e) {
            log.error("文件文字提取失败", e);
            return new Response<String>(1, "文件文字提取失败: " + e.getMessage(), null);
        }
    }

    /**
     * 解析要发送给 DeepSeek 的问题。
     * 目前问题留空，先使用默认的文字提取指令；由调用方在此填充实际的问题内容。
     */
    private String resolveQuestion(String question) {
        String userQuestion = StringUtils.trimToEmpty(question);
        if (userQuestion.isEmpty()) {
            // TODO 问题内容当前留空，待调用方填充。
            userQuestion = DEFAULT_TEXT_EXTRACT_PROMPT;
        }
        return userQuestion;
    }

    private String extractPdfText(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            // 表单填写内容存放在AcroForm控件中，PDFTextStripper默认不会读取。
            // 先在内存中扁平化，使姓名、金额、服务类型等表单值进入页面文字流。
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            if (acroForm != null) {
                acroForm.flatten();
            }

            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setSortByPosition(true);
            String text = textStripper.getText(document);
            return text == null ? "" : text.trim();
        }
    }

    /**
     * 调取 DeepSeek 接口。
     *
     * @param content user消息的纯文本内容
     * @param jsonMode 是否要求返回 JSON（response_format = json_object）
     */
    private String requestDeepSeek(String content, boolean jsonMode) throws IOException {
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", content);

        JSONArray messages = new JSONArray();
        messages.add(userMessage);

        JSONObject thinking = new JSONObject();
        thinking.put("type", "disabled");

        JSONObject requestJson = new JSONObject();
        requestJson.put("model", deepSeekModel);
        requestJson.put("messages", messages);
        requestJson.put("thinking", thinking);
        requestJson.put("stream", false);

        if (jsonMode) {
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");
            requestJson.put("response_format", responseFormat);
        }

        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, requestJson.toJSONString());
        Request request = new Request.Builder()
                .url(DEEPSEEK_CHAT_COMPLETIONS_URL)
                .header("Authorization", "Bearer " + deepSeekApiKey.trim())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (okhttp3.Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("DeepSeek请求失败(" + response.code() + "): "
                        + extractErrorMessage(responseBody));
            }
            return extractDeepSeekOutputText(responseBody);
        }
    }

    private String extractDeepSeekOutputText(String responseBody) throws IOException {
        try {
            JSONObject responseJson = JSON.parseObject(responseBody);
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice == null ? null : firstChoice.getJSONObject("message");
                String content = message == null ? null : message.getString("content");
                if (content != null && !content.trim().isEmpty()) {
                    return content.trim();
                }
            }
        } catch (Exception e) {
            throw new IOException("DeepSeek返回了无法解析的数据", e);
        }
        throw new IOException("DeepSeek返回成功，但没有文本分析结果");
    }

    private JSONObject parsePdfAnalysisResult(String result) throws IOException {
        try {
            JSONObject aiResult = JSON.parseObject(result);
            JSONObject normalizedResult = new JSONObject();
            normalizedResult.put("name", aiResult.get("name"));
            normalizedResult.put("amount", aiResult.get("amount"));
            normalizedResult.put("service", aiResult.get("service"));
            return normalizedResult;
        } catch (Exception e) {
            throw new IOException("DeepSeek返回的分析结果不是有效JSON", e);
        }
    }

    private String extractErrorMessage(String responseBody) {
        try {
            JSONObject responseJson = JSON.parseObject(responseBody);
            JSONObject error = responseJson.getJSONObject("error");
            if (error != null && error.getString("message") != null) {
                return error.getString("message");
            }
        } catch (Exception ignored) {
            // 返回内容不是JSON时，使用下面的通用提示，避免把大段HTML写入日志和响应。
        }
        return "未返回可识别的错误信息";
    }

    private boolean isPdf(byte[] bytes) {
        if (bytes == null || bytes.length < 5) {
            return false;
        }
        return "%PDF-".equals(new String(bytes, 0, 5, StandardCharsets.US_ASCII));
    }

    /** 通过文件头魔数识别腾讯云OCR支持的图片：PNG / JPEG / BMP。 */
    private boolean isImage(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 'P'
                && bytes[2] == 'N'
                && bytes[3] == 'G'
                && (bytes[4] & 0xFF) == 0x0D
                && (bytes[5] & 0xFF) == 0x0A
                && (bytes[6] & 0xFF) == 0x1A
                && (bytes[7] & 0xFF) == 0x0A) {
            return true;
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }
        return bytes[0] == 'B' && bytes[1] == 'M';
    }

    private String truncateOcrText(String text) {
        if (text.length() <= MAX_OCR_TEXT_LENGTH) {
            return text;
        }
        int half = MAX_OCR_TEXT_LENGTH / 2;
        return text.substring(0, half)
                + "\n...[OCR内容过长，中间部分已省略]...\n"
                + text.substring(text.length() - half);
    }

    private String getPdfFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "document.pdf";
        }
        String filename = originalFilename.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1);
        return filename.toLowerCase().endsWith(".pdf") ? filename : filename + ".pdf";
    }
}
