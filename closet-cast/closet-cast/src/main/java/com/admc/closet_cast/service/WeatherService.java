package com.admc.closet_cast.service;

import com.admc.closet_cast.dto.DailyWeatherDto;
import com.admc.closet_cast.dto.HourlyWeatherDto;
import com.admc.closet_cast.entity.HourlyWeather;
import com.admc.closet_cast.entity.Weather;
import com.admc.closet_cast.repository.HourlyWeatherRepository;
import com.admc.closet_cast.repository.WeatherRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final WebClient.Builder webClientBuilder;
    private final WeatherRepository weatherRepository;
    private final HourlyWeatherRepository hourlyWeatherRepository;

    @Transactional
    public List<DailyWeatherDto> getForecast(String baseDate, String baseTime, int nx, int ny) {
        WebClient webClient = webClientBuilder.baseUrl("https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0").build();
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

        List<DailyWeatherDto> result = parseWeatherResponse(json);
        saveWeatherToDB(result);

        return result;
    }

    @Transactional
    public void saveWeatherToDB(List<DailyWeatherDto> result) {
        for (DailyWeatherDto dailyWeatherDto : result) {
            Weather weather = weatherRepository.findByDate(dailyWeatherDto.getDate())
                    .orElseGet(Weather::new); // 없으면 새로 생성

            weather.setDate(dailyWeatherDto.getDate());

            // null이 아닐 때만 갱신
            if (dailyWeatherDto.getTmx() != null) {
                weather.setTmx(dailyWeatherDto.getTmx());
            }
            if (dailyWeatherDto.getTmn() != null) {
                weather.setTmn(dailyWeatherDto.getTmn());
            }

            // 시간별 데이터가 있을 때만 갱신
            if (dailyWeatherDto.getHourlyList() != null && !dailyWeatherDto.getHourlyList().isEmpty()) {
                weather.getHourlyList().clear(); // 기존 시간대 데이터 초기화
                for (HourlyWeatherDto hourDto : dailyWeatherDto.getHourlyList()) {
                    HourlyWeather hourly = new HourlyWeather();

                    if (hourDto.getFcstTime() != null)
                        hourly.setFcstTime(hourDto.getFcstTime());
                    if (hourDto.getTemperature() != null)
                        hourly.setTemperature(hourDto.getTemperature());
                    if (hourDto.getApparentTemp() != null)
                        hourly.setApparentTemp(hourDto.getApparentTemp());

                    weather.addHourly(hourly);
                }
            }

            weatherRepository.save(weather);
        }
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

    @Transactional(readOnly = true)
    public List<DailyWeatherDto> getDailyWeather(String today) {
        List<DailyWeatherDto> result = new ArrayList<>();
        String tomorrow = getNextDate(today, 1);
        String twoDay = getNextDate(today, 2);

        Weather todayWeather = weatherRepository.findByDate(today).get();
        Weather tomorrowWeather = weatherRepository.findByDate(tomorrow).get();
        Weather twoDayWeather = weatherRepository.findByDate(twoDay).get();

        List<HourlyWeatherDto> todayHourly = new ArrayList<>();
        Map<String, Double> todayApparent = new HashMap<>();
        List<HourlyWeatherDto> tomorrowHourly = new ArrayList<>();
        Map<String, Double> tomorrowApparent = new HashMap<>();
        List<HourlyWeatherDto> twoDayHourly = new ArrayList<>();
        Map<String, Double> twoDayApparent = new HashMap<>();

        List<HourlyWeather> todayHourlyWeather = hourlyWeatherRepository.findByWeather(todayWeather);
        List<HourlyWeather> tomorrowHourlyWeather = hourlyWeatherRepository.findByWeather(tomorrowWeather);
        List<HourlyWeather> twoDayHourlyWeather = hourlyWeatherRepository.findByWeather(twoDayWeather);

        for (HourlyWeather hour : todayHourlyWeather) {
            HourlyWeatherDto hourDto = new HourlyWeatherDto(hour.getFcstTime(), hour.getTemperature(), hour.getApparentTemp());
            todayHourly.add(hourDto);
            todayApparent.put(hour.getFcstTime(), hour.getApparentTemp());
        }

        for (HourlyWeather hour : tomorrowHourlyWeather) {
            HourlyWeatherDto hourDto = new HourlyWeatherDto(hour.getFcstTime(), hour.getTemperature(), hour.getApparentTemp());
            tomorrowHourly.add(hourDto);
            tomorrowApparent.put(hour.getFcstTime(), hour.getApparentTemp());
        }

        for (HourlyWeather hour : twoDayHourlyWeather) {
            HourlyWeatherDto hourDto = new HourlyWeatherDto(hour.getFcstTime(), hour.getTemperature(), hour.getApparentTemp());
            twoDayHourly.add(hourDto);
            twoDayApparent.put(hour.getFcstTime(), hour.getApparentTemp());
        }

        DailyWeatherDto todayWeatherDto = new DailyWeatherDto(todayWeather.getDate(), todayWeather.getTmx(), todayWeather.getTmn(), todayHourly, todayApparent);
        DailyWeatherDto tomorrowWeatherDto = new DailyWeatherDto(tomorrowWeather.getDate(), tomorrowWeather.getTmx(), tomorrowWeather.getTmn(), tomorrowHourly, tomorrowApparent);
        DailyWeatherDto twoDayWeatherDto = new DailyWeatherDto(twoDayWeather.getDate(), twoDayWeather.getTmx(),  twoDayWeather.getTmn(), twoDayHourly, twoDayApparent);
        result.add(todayWeatherDto);
        result.add(tomorrowWeatherDto);
        result.add(twoDayWeatherDto);

        return result;
    }

    private String getNextDate(String dateStr, int plusDays) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(dateStr, formatter);
        LocalDate nextDate = date.plusDays(plusDays);
        return nextDate.format(formatter);
    }
}
