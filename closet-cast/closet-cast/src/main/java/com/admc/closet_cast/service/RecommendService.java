package com.admc.closet_cast.service;

import com.admc.closet_cast.apiPayload.exception.handler.MemberHandler;
import com.admc.closet_cast.apiPayload.exception.handler.WeatherHandler;
import com.admc.closet_cast.apiPayload.form.status.ErrorStatus;
import com.admc.closet_cast.dto.RecommendDto;
import com.admc.closet_cast.entity.HourlyWeather;
import com.admc.closet_cast.entity.Member;
import com.admc.closet_cast.entity.Weather;
import com.admc.closet_cast.repository.HourlyWeatherRepository;
import com.admc.closet_cast.repository.MemberRepository;
import com.admc.closet_cast.repository.WeatherRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

//    LLM결과 string으로 그대로 쏴줄거임.
//    LLM의 ouput을
//    온도:
//    체감온도:
//    옷 종류:
//
//    output을 사용자의 옷 종류에 한정해서 이야기해줘.
//    ex) (맨투맨, 청바지)
//    (맨투맨, 청바지)
//    (String, String)
//    TOP BOTTOM
@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendService {

    @Value("${openai.secret-key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();
    private final MemberRepository memberRepository;
    private final WeatherRepository weatherRepository;
    private final HourlyWeatherRepository hourlyWeatherRepository;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    // 💡 RestTemplate은 생성자 주입(DI) 받는 것을 권장합니다. (하단 설명 참고)
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * GPT Chat API에 옷 추천 요청을 보냅니다.
     */
    public RecommendDto getReply(Long memberId) throws Exception {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND)
        );

        String clothes = member.getClothes().toString(); // ex: "[맨투맨, 후드티, 청바지, 슬랙스]"
        String preference = member.getPreferences().toString();     // ex: "편안한 스타일 선호"
        String tendencies = member.getTendencies().toString(); // ex: "[추위 많이 탐]"

        LocalDateTime today = LocalDateTime.now();
        Weather weather = weatherRepository.findByDate(today.format(DateTimeFormatter.ofPattern("yyyyMMdd"))).orElseThrow(
                () -> new WeatherHandler(ErrorStatus.NO_DATA)
        );
        Double max_temp = weather.getTmx();
        Double min_temp = weather.getTmn();

//        Long weatherId = weather.getId();
        List<HourlyWeather> hourlyWeathers = hourlyWeatherRepository.findByWeather(weather);

        Double max_feel = hourlyWeathers.stream().mapToDouble(HourlyWeather::getTemperature).max().getAsDouble();
        Double min_feel = hourlyWeathers.stream().mapToDouble(HourlyWeather::getTemperature).min().getAsDouble();

        String systemPrompt = String.format(
                "너는 사용자의 옷장 정보를 기반으로 날씨에 맞는 옷을 추천하는 패션 어시턴트야. " +
                        "사용자가 가진 옷 목록은 다음과 같아: [%s]. " +
                        "이 옷 중에서 PUFFER_JACKET, FLEECE, JACKET, WIND_BREAKER는 아우터, SWEATER, HOODIE, SHIRT, LONG_SLEEVE, SHORT_SLEEVE는 상의, JEANS, COTTON_PANTS, SHORTS는 하의야."+
                        "반드시 이 목록 안에서만 (아우터, 상의, 하의) 조합을 추천해야 해. " +
                        "다른 설명, 인사, 날씨 브리핑 없이 오직 (아우터, 상의 아이템, 하의 아이템) 형식으로만 대답해야 해. " +
                        "만약 아우터가 필요 없는 날씨라면, 아우터칸은 None으로 대답해줘." +
                        "예시: (아우터, 맨투맨, 청바지), 또는 (None, 맨투맨, 청바지)",
                clothes
        );

        String userPrompt = String.format(
                "오늘 최고기온 %f도, 최저기온 %f도, 체감 최고기온 %f도, 체감 최저기온 %f도야. " +
                        "내 패션 선호도는 '%s'이고, 내 성향은 '%s'이야. " +
                        "내가 가진 옷 중에서 (아우터, 상의, 하의) 조합 하나만 추천해줘.",
                max_temp, min_temp, max_feel, min_feel, preference, tendencies
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ChatRequest chatRequest = new ChatRequest(
                "gpt-4o",
                Arrays.asList(
                        new ChatMessage("system", systemPrompt),
                        new ChatMessage("user", userPrompt)
                ),
                100,
                0.2
        );

        HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);

        try {
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                    API_URL,
                    entity,
                    ChatResponse.class
            );

//            log.info("API Response: {}", response.toString()); // API 전체 응답 확인

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String gptReply = response.getBody().choices().get(0).message().content();

                // 파싱 로직
                String cleaned = gptReply.replaceAll("[()\\s]", ""); // ( ) 및 공백 제거
                String[] parts = cleaned.split(",");
                String outer = parts.length > 0 ? parts[0] : "";
                String top = parts.length > 1 ? parts[1] : "";
                String bottom = parts.length > 1 ? parts[2] : "";

//                log.info("gpt-reply: {}", gptReply);
//                log.info("cleaned: {}", cleaned);
//                log.info("outer: {}", outer);
//                log.info("top: {}", top);
//                log.info("bottom: {}", bottom);

                //                log.info("Returning DTO: {}", resultDto.toString());

                return new RecommendDto(outer, top, bottom); // 수정된 DTO 반환

            } else {
                throw new Exception("GPT API 호출 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("GPT API 요청 중 오류 발생", e); // 💡 예외 발생 시 로그 남기기
            throw new Exception("GPT API 요청 중 오류 발생", e);
        }
    }

    // --- OpenAI API 요청/응답을 위한 DTO ---
    // (Java 17+의 record 사용, 클래스로 만들어도 무방합니다)

    /**
     * OpenAI Chat API 요청 본문
     */
    private record ChatRequest(
            String model,
            List<ChatMessage> messages,
            int max_tokens,
            double temperature
    ) {}

    /**
     * GPT에게 보낼 메시지 (역할, 내용)
     */
    private record ChatMessage(
            String role, // "system", "user", "assistant"
            String content
    ) {}

    // --- OpenAI API 응답 DTO ---

    /**
     * OpenAI Chat API 응답
     */
    private record ChatResponse(
            List<Choice> choices
    ) {}

    /**
     * API 응답 - 선택지
     */
    private record Choice(
            int index,
            ChatMessage message,
            String finish_reason
    ) {}

}