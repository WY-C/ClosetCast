package com.admc.closet_cast.service;

import com.admc.closet_cast.dto.DailyWeatherDto;
import com.admc.closet_cast.dto.HourlyWeatherDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class WeatherService {
    private final WebClient webClient;

    public WeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0").build();
    }

    public List<DailyWeatherDto> getForecast(String baseDate, String baseTime, int nx, int ny) {
        String authKey = "iUT6NVMERleE-jVTBFZX_g";
        String uri = UriComponentsBuilder.fromPath("/getVilageFcst")
                .queryParam("authKey", authKey)
                .queryParam("pageNo", "1")
                .queryParam("numOfRows", "1000")
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .toUriString();

        String json = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseWeatherResponse(json);
    }

    private List<DailyWeatherDto> parseWeatherResponse(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode items = root.path("response").path("body").path("items").path("item");
            Map<String, DailyWeatherDto> dailyMap = new LinkedHashMap<>();

            // fcstDate+fcstTime 기준으로 풍속 저장용
            Map<String, Double> windMap = new HashMap<>();

            for (JsonNode item : items) {
                String fcstDate = item.get("fcstDate").asText();
                String fcstTime = item.get("fcstTime").asText();
                String category = item.get("category").asText();
                String fcstValueStr = item.get("fcstValue").asText();

                double value;
                try {
                    value = Double.parseDouble(fcstValueStr);
                } catch (NumberFormatException e) {
                    continue; // 숫자 아닌 값 무시
                }

                DailyWeatherDto daily = dailyMap.computeIfAbsent(fcstDate, k -> new DailyWeatherDto(fcstDate));

                switch (category) {
                    case "TMP": // 기온
                        daily.getHourlyList().add(new HourlyWeatherDto(fcstTime, value, null));
                        break;
                    case "WSD": // 풍속 저장
                        windMap.put(fcstDate + fcstTime, value);
                        break;
                    case "TMX": // 최고기온
                        daily.setTmx(value);
                        break;
                    case "TMN": // 최저기온
                        daily.setTmn(value);
                        break;
                }
            }

            // 체감온도 계산 후 매핑
            for (DailyWeatherDto day : dailyMap.values()) {
                for (HourlyWeatherDto hour : day.getHourlyList()) {
                    double temp = hour.getTemperature();
                    Double wind = windMap.get(day.getDate() + hour.getFcstTime());

                    if (wind != null) {
                        double apparent = 13.12 + 0.6215 * temp
                                - 11.37 * Math.pow(wind, 0.16)
                                + 0.3965 * temp * Math.pow(wind, 0.16);
                        hour.setApparentTemp(Math.round(apparent * 10) / 10.0);
                    }
                }
            }

            return new ArrayList<>(dailyMap.values());

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
