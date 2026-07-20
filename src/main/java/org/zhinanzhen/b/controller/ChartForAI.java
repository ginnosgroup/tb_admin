package org.zhinanzhen.b.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
import org.zhinanzhen.tb.controller.Response;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/ChartForAI")
@Slf4j
public class ChartForAI {

    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
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
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .build();

    /**
     * 请在 application.properties 的 openai.api.key 配置项中填写 API Key。
     */
    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4.1}")
    private String openAiModel;

    /**
     * 请在 application.properties 的 deepseek.pdf.api.key 配置项中填写 API Key。
     */
    @Value("${deepseek.pdf.api.key:}")
    private String deepSeekApiKey;

    @Value("${deepseek.pdf.model:deepseek-v4-flash}")
    private String deepSeekModel;

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
        // 原OpenAI方式保留，需要切回时取消下面代码以及requestOpenAi调用的注释。
//        if (openAiApiKey == null || openAiApiKey.trim().isEmpty()) {
//            return new Response<JSONObject>(1, "请先在application.properties中填写openai.api.key", null);
//        }

        if (deepSeekApiKey == null || deepSeekApiKey.trim().isEmpty()) {
            return new Response<JSONObject>(1, "请先在application.properties中填写deepseek.pdf.api.key", null);
        }

        try {
            if (!isPdf(pdfBytes)) {
                return new Response<JSONObject>(1, "上传的文件不是有效的PDF", null);
            }

            // 原OpenAI调用方式保留，需要时可直接切回。
//            String result = requestOpenAi(pdfBytes, filename);

            String pdfText = extractPdfText(pdfBytes);
            if (pdfText.isEmpty()) {
                return new Response<JSONObject>(1,
                        "未从PDF中提取到文字，文件可能是扫描件；DeepSeek API不能直接读取PDF，请先进行OCR", null);
            }

            String result = requestDeepSeek(pdfText);
            JSONObject resultJson = parsePdfAnalysisResult(result);
            log.info("PDF DeepSeek分析结果: {}", resultJson.toJSONString());
            System.out.println("PDF DeepSeek分析结果: " + resultJson.toJSONString());
            return new Response<JSONObject>(0, "分析成功", resultJson);
        } catch (Exception e) {
            log.error("PDF DeepSeek分析失败", e);
            return new Response<JSONObject>(1, "PDF DeepSeek分析失败: " + e.getMessage(), null);
        }
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

    private String requestDeepSeek(String pdfText) throws IOException {
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", PDF_ANALYSIS_PROMPT
                + "\n\n以下是从PDF中提取的文字内容：\n" + pdfText);

        JSONArray messages = new JSONArray();
        messages.add(userMessage);

        JSONObject thinking = new JSONObject();
        thinking.put("type", "disabled");

        JSONObject responseFormat = new JSONObject();
        responseFormat.put("type", "json_object");

        JSONObject requestJson = new JSONObject();
        requestJson.put("model", deepSeekModel);
        requestJson.put("messages", messages);
        requestJson.put("thinking", thinking);
        requestJson.put("response_format", responseFormat);
        requestJson.put("stream", false);

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

    private String requestOpenAi(byte[] pdfBytes, String filename) throws IOException {
        JSONObject fileContent = new JSONObject();
        fileContent.put("type", "input_file");
        fileContent.put("filename", filename);
        fileContent.put("file_data", "data:application/pdf;base64,"
                + Base64.getEncoder().encodeToString(pdfBytes));
        fileContent.put("detail", "high");

        JSONObject promptContent = new JSONObject();
        promptContent.put("type", "input_text");
        promptContent.put("text", PDF_ANALYSIS_PROMPT);

        JSONArray content = new JSONArray();
        content.add(fileContent);
        content.add(promptContent);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", content);

        JSONArray input = new JSONArray();
        input.add(message);

        JSONObject requestJson = new JSONObject();
        requestJson.put("model", openAiModel);
        requestJson.put("input", input);
        // PDF中包含学生及收款信息，不在OpenAI侧保存本次响应供后续检索。
        requestJson.put("store", false);

        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, requestJson.toJSONString());
        Request request = new Request.Builder()
                .url(OPENAI_RESPONSES_URL)
                .header("Authorization", "Bearer " + openAiApiKey.trim())
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (okhttp3.Response response = HTTP_CLIENT.newCall(request).execute()) {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI请求失败(" + response.code() + "): "
                        + extractErrorMessage(responseBody));
            }
            return extractOutputText(responseBody);
        }
    }

    private String extractOutputText(String responseBody) throws IOException {
        JSONObject responseJson;
        try {
            responseJson = JSON.parseObject(responseBody);
        } catch (Exception e) {
            throw new IOException("OpenAI返回了无法解析的数据", e);
        }

        StringBuilder result = new StringBuilder();
        JSONArray output = responseJson.getJSONArray("output");
        if (output != null) {
            for (int i = 0; i < output.size(); i++) {
                JSONObject outputItem = output.getJSONObject(i);
                if (outputItem == null) {
                    continue;
                }
                JSONArray outputContent = outputItem.getJSONArray("content");
                if (outputContent == null) {
                    continue;
                }
                for (int j = 0; j < outputContent.size(); j++) {
                    JSONObject contentItem = outputContent.getJSONObject(j);
                    if (contentItem != null && "output_text".equals(contentItem.getString("type"))) {
                        String text = contentItem.getString("text");
                        if (text != null && !text.trim().isEmpty()) {
                            if (result.length() > 0) {
                                result.append(System.lineSeparator());
                            }
                            result.append(text);
                        }
                    }
                }
            }
        }

        if (result.length() == 0) {
            throw new IOException("OpenAI返回成功，但没有文本分析结果");
        }
        return result.toString();
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
