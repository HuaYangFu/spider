/**
 * Created by 傅華暘 on 2017/7/8.
 */
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;

public class spider {

        private static String commetUrlApi = "https://www.dcard.tw/_api/forums/entertainer/posts?popular=true";

        private static String commetUrlpageApi = "https://www.dcard.tw/_api/forums/entertainer/posts?popular=true&before={articleId}";

        private static String commetApi = "https://www.dcard.tw/_api/posts/{articleId}/comments";

        private static String commetPageApi = "https://www.dcard.tw/_api/posts/{articleId}/comments?after={floor}";

        public static void main(String[] args) throws Exception {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -300);
            long time = cal.getTimeInMillis();
            Instant until = Instant.ofEpochMilli(time);

            WebClient webClient = initializeWebClient();
            List<String> list = loadCommetUrl(webClient, until);
            list.forEach(articleId -> {
                try {
                    System.out.println(articleId);
                    loadCommet(webClient, articleId, until);
                }
                catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            });
            System.out.println("end");
            destoryWebClient(webClient);
        }

        public static List<String> loadCommetUrl(WebClient webClient, Instant until) throws Exception {
            List<String> commetUrlList = new ArrayList<>();
            boolean hasNextPage = true;
            String connctUrl = commetUrlApi;
            while (hasNextPage) {
                List<JsonNode> jsonNodes = connect(webClient, connctUrl);
                hasNextPage = (jsonNodes.size() - 1) > 0;
                if (hasNextPage) {
                    JsonNode lastNode = jsonNodes.get(jsonNodes.size() - 1);
                    connctUrl = commetUrlpageApi.replace("{articleId}", lastNode.findPath("id").asText().trim());
                }
                jsonNodes.stream().forEach(article -> {
                    commetUrlList.add(article.path("id").asText().trim());
                    // System.out.println(article.path("updatedAt").asText().trim());
                    // System.out.println(article.path("id").asText().trim());
                    //
                    // System.out.println(article.path("commentCount").asText().trim());
                    // System.out.println(article.path("likeCount").asText().trim());
                    // System.out.println(article.path("title").asText().trim());
                    // System.out.println(article.path("forumName").asText().trim());
                    // System.out.println(article.path("school").asText().trim());
                    // System.out.println(article.path("excerpt").asText().trim());
                    // System.out.println(article.path("createdAt").asText().trim());
                });
            }
            return commetUrlList;
        }

        public static void loadCommet(WebClient webClient, String articleId, Instant until) throws Exception {
            boolean hasNextPage = true;
            String connetUrl = commetApi.replace("{articleId}", articleId);
            while (hasNextPage) {
                List<JsonNode> commets = connect(webClient, connetUrl);
                hasNextPage = (commets.size() - 1) > 0;
                if (hasNextPage) {
                    JsonNode lastNode = commets.get(commets.size() - 1);
                    connetUrl = commetPageApi.replace("{floor}", lastNode.findPath("floor").asText().trim());
                    connetUrl = connetUrl.replace("{articleId}", articleId);
                }
                commets.stream().filter(commet -> {
                    String content = commet.path("content").asText();
                    return !StringUtils.isBlank(content);
                }).forEach(commet -> {
                    // System.out.println(commet.path("content").asText().trim());
                    // System.out.println(commet.path("department").asText().trim());
                    // System.out.println(commet.path("school").asText().trim());
                    // System.out.println(commet.path("postId").asText().trim());
                    // System.out.println(commet.path("floor").asText().trim());
                    // String dateStr = commet.path("createdAt").asText().trim();
                    // Instant date = ZonedDateTime.parse(dateStr).toInstant();
                    // System.out.println(Date.from(date));
                });
            }
        }

        public static List<JsonNode> connect(WebClient webClient, String urlStr) throws Exception {
            URL url = new URL(urlStr);
            WebRequest requestSettings = new WebRequest(url, HttpMethod.GET);
            WebResponse webResponse = webClient.getPage(requestSettings).getWebResponse();
            String content = webResponse.getContentAsString();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readValue(content, JsonNode.class);
            List<JsonNode> jsonNodes = jsonNode.findParents("id");
            return jsonNodes;
        }


        private static WebClient initializeWebClient() {
            final WebClient webClient = new WebClient(BrowserVersion.CHROME);
            webClient.getOptions().setPrintContentOnFailingStatusCode(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setTimeout(600000);
            webClient.setJavaScriptTimeout(600000);
            return webClient;
        }

        private static void destoryWebClient(WebClient webClient) {
            try {
                if (webClient != null) {
                    webClient.getCurrentWindow().getJobManager().removeAllJobs();
                    webClient.closeAllWindows();
                    System.gc();
                }
            }
            catch (Exception e) {}
        }

}
