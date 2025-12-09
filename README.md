# 🌤️ ClosetCast
> **내 옷장 속 데이터와 기상청 날씨의 만남** > **"오늘 날씨에 딱 맞는 옷, 당신의 옷장에서 찾아드립니다."**

<br>

## ⚠️ Branch Information (필독)
이 리포지토리는 프론트엔드와 백엔드가 서로 다른 브랜치로 분리되어 관리됩니다.  
작업하려는 파트에 맞춰 **브랜치를 변경(Checkout)** 해주세요.

| 파트 (Part) | 브랜치 (Branch) | 설명 (Description) |
|:---:|:---:|:---|
| **Backend** | **`main`** (Current) | Spring Boot 서버 코드 및 전체 프로젝트 문서 |
| **Frontend** | **`master`** | **Android Studio (Kotlin) 앱 소스 코드** |


<br>

## 📖 Project Overview
**ClosetCast**는 단순한 날씨 기반 추천을 넘어, **사용자가 보유한 옷(Closet Data)**을 고려하여 최적의 코디를 제안하는 서비스입니다.

기존 서비스들이 "패딩을 입으세요"라고 포괄적으로 제안한다면, ClosetCast는 기상청 API와 ChatGPT를 활용하여 **"현재 가지고 계신 '검은색 숏패딩'과 '기모 청바지'가 오늘 날씨에 딱이에요!"** 라고 구체적이고 개인화된 솔루션을 제공합니다.

<br>

## 🛠️ System Architecture
<img width="1550" height="896" alt="image" src="https://github.com/user-attachments/assets/e1902ade-8ebe-4b9a-a114-c50ed4c2bf03" />

<br>

## ✨ Key Features & Differentiation
* **🧥 스마트 옷장 (My Closet):** 사용자가 자신이 보유한 상의, 하의, 아우터 등을 사진과 함께 등록하여 '나만의 디지털 옷장'을 구축합니다.
* **🤖 개인화된 AI 코디 추천:** * **OpenAI(ChatGPT)**가 사용자의 **옷장 데이터베이스**를 조회하여, 현재 날씨에 가장 적합한 조합을 찾아냅니다.
  * 단순한 카테고리 추천이 아닌, 실제 내 옷을 활용한 스타일링 멘트를 제공합니다.

<br>



## 🏗️ Tech Stack

### 💻 Backend (Branch: `main`)
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Amazon EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white">

### 📱 Frontend (Branch: `master`)
<img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"> <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">

### 🔗 External API
<img src="https://img.shields.io/badge/OpenAI (ChatGPT)-412991?style=for-the-badge&logo=openai&logoColor=white"> <img src="https://img.shields.io/badge/Korea Weather API-0052A4?style=for-the-badge&logo=koreagovernment&logoColor=white">


## 📂 API Specification
API 명세는 Swagger를 통해 관리되고 있습니다.
* **Swagger URL:** `(http://3.39.165.91:3000/swagger-ui/index.html)`
<br>

## 🚀 Getting Started

### Clone the repository
```bash
git clone [https://github.com/WY-C/ClosetCast.git](https://github.com/WY-C/ClosetCast.git)
cd ClosetCast
