## 🌟 web2app-android (React PWA ↔ Android 하이브리드)

React **PWA(Progressive Web App)**를 **Android WebView**에 임베드하고, **JavaScript Bridge**를 통해 네이티브 **카메라 OCR(광학 문자 인식)** 기능을 연동하는 하이브리드 앱 데모 프로젝트입니다.

이 프로젝트는 기존 웹 서비스(**PWA**)를 최대한 재사용하면서 카메라, OCR 같은 **네이티브 기능을 점진적으로 통합**하는 방법을 보여줍니다.

---

## 🎬 주요 데모 시연

### 1. 앱 실행 & PWA 로딩 (WebView)

사용자가 앱을 실행하면, 스플래시 화면 직후 별도의 네이티브 화면 없이 **React PWA 웹앱이 WebView로 로드**됩니다.

![demo-startup](docs/demo-startup.gif)

* **PWA 재사용:** PWA의 내비게이션 및 라우팅을 그대로 활용합니다.

### 2. 웹에서 카메라 호출 (JS Bridge)

웹 화면의 버튼을 클릭하면, JavaScript에서 `window.Android.startCamera()`를 호출합니다. 이 호출은 **JS 브리지**를 통해 네이티브 `CaptureActivity`를 실행하고 화면을 전환합니다.

![demo-open-camera](docs/demo-open-camera.gif)

* **JS → Native:** 웹 버튼 클릭 → `window.Android.startCamera()` → 네이티브 화면 전환
* **구조:** 멀티 모듈에서 `:app` 모듈이 이 네비게이션을 조율합니다.

### 3. OCR 결과 반환 & 웹 UI 업데이트

네이티브 화면에서 촬영된 이미지는 **ML Kit Korean Text Recognition**으로 OCR 처리됩니다. 인식된 한국어 텍스트 결과는 **JSON으로 직렬화**되어 비동기적으로 `window.onOcrResult(json)`을 통해 다시 웹뷰로 전달됩니다.

![demo-ocr-result](docs/demo-ocr-result.gif)

* **Native → JS:** 네이티브 텍스트 추출 → JSON 변환 → `window.onOcrResult(json)` 호출
* **결과:** 웹 UI(React PWA)가 OCR 결과를 바탕으로 업데이트됩니다.

---

## ✨ 핵심 특징 및 기술

* **PWA 웹앱 재사용:** `WebView + PWA_URL` 방식으로 앱스토어 배포 가능한 하이브리드 앱 구현.
* **JS 브리지 기반 연동:**
    * **웹 → 네이티브:** `window.Android.startCamera()`를 통한 네이티브 카메라 호출.
    * **네이티브 → 웹:** `window.onOcrResult(json)`을 통한 OCR 결과 전달.
* **네이티브 기능:**
    * **카메라:** `CameraX` 기반 실시간 프리뷰 및 캡처.
    * **OCR:** `ML Kit Text Recognition (Korean)` 사용.
* **멀티 모듈 아키텍처:**
    * `:app`, `:core`, `:feature-webview`, `:feature-capture` 모듈 분리.
    * `core`를 통한 **인터페이스 기반의 느슨한 결합** (의존성 역전 원칙/DIP).

---

## 🏗 아키텍처 개요

### 모듈 구조 및 의존성

이 프로젝트는 기능을 명확하게 분리하고 모듈 간의 의존성을 줄이기 위해 멀티 모듈 구조를 채택했습니다.

| 모듈 이름 | 핵심 책임 | 주요 구성 요소 | 의존성 방향 |
|-----------|-----------|------------------|-------------|
| **`app`** | 엔트리 포인트, 네비게이션, DI 조율 | `MainActivity`, `BuildConfig.PWA_URL` | $\rightarrow$ `core`, `feature-webview`, `feature-capture` |
| **`core`** | **공통 모델, 유틸, 추상화 인터페이스** | `OcrResult`, `OcrResultBus`, `CameraLauncher` | **다른 모듈의 의존 대상** |
| **`feature-webview`** | PWA 로딩, **JS $\leftrightarrow$ Android 브리지** | `WebViewFragment`, `AndroidJsBridge` | $\rightarrow$ `core` |
| **`feature-capture`** | **CameraX 촬영 및 ML Kit OCR 처리** | `CaptureActivity` | $\rightarrow$ `core`, `CameraX`, `ML Kit` |

### 주요 인터페이스 상세 (core 모듈)

| 구성 요소 | 역할 | 비고 |
|-----------|------|-------|
| **`OcrResult`** | OCR 결과를 표현하는 데이터 모델 | 네이티브 $\rightarrow$ 웹 JSON 변환의 기반 |
| **`OcrResultBus`** | `SharedFlow<String>` 기반 이벤트 버스 | 네이티브 OCR 결과를 `WebViewFragment`로 비동기 전달 |
| **`CameraLauncher`** | `fun launchCamera()` 단일 인터페이스 | `feature-webview`가 **Host Activity**(`MainActivity`)를 직접 참조하지 않도록 추상화 |

---

## 🔁 PWA ↔ 네이티브 연동 흐름 상세

이 연동은 **JS Bridge**와 **이벤트 버스**(`OcrResultBus`)를 통해 비동기적으로 이루어집니다.

### 1. 웹 $\rightarrow$ 네이티브 (카메라 호출)

1.  **웹 (React PWA):** 사용자 버튼 클릭 시 `window.Android.startCamera()` 호출.
2.  **`feature-webview`:** `AndroidJsBridge`가 이 호출을 받아.
3.  **`core`:** 추상화된 `CameraLauncher.launchCamera()`를 호출.
4.  **`app`:** `MainActivity`에서 `CameraLauncher`를 구현하고, 이를 통해 `CaptureActivity`를 실행.

### 2. 네이티브 $\rightarrow$ 웹 (OCR 결과 전달)

1.  **`feature-capture`:** `CaptureActivity`에서 CameraX 촬영 및 ML Kit OCR 처리.
2.  **`feature-capture`:** 결과를 `OcrResult` 모델로 변환 후 JSON 문자열을 생성.
3.  **`core`:** `OcrResultBus.post(json)`를 호출하여 결과 전송.
4.  **`feature-webview`:** `WebViewFragment`가 `OcrResultBus.resultFlow`를 구독하고 있다가 새 JSON 문자열을 수신.
5.  **`feature-webview`:** `webView.evaluateJavascript("window.onOcrResult(${json});", null)`을 호출하여 결과를 웹뷰 JS 환경으로 전달.
6.  **웹 (React PWA):** `window.onOcrResult(json)` 함수를 받아 웹 UI 업데이트.

---

## 🛠 기술 스택

| 영역 | 기술 스택 | 비고 |
|------|-----------|------|
| **Language** | **Kotlin** (JVM, Kotlin 2.x) | 최신 Android 개발 언어 |
| **Android** | Android Application / Library (AGP 8.x), AndroidX | View 기반 UI (ViewBinding) |
| **UI** | **WebView** (React PWA 임베딩) | |
| **Camera/ML** | **CameraX** (Preview/Capture), **ML Kit Text Recognition (Korean)** | 네이티브 핵심 기능 제공 |
| **Architecture** | **Multi-module**, **Interface 기반 DIP**, **Event-driven (SharedFlow)** | |

---

## 🚀 빌드 & 실행 방법

1.  이 레포지토리를 클론합니다.
    ```bash
    git clone [https://github.com/your-account/web2app-android.git](https://github.com/your-account/web2app-android.git)
    cd web2app-android
    ```
2.  **Android Studio** (Giraffe / Koala / Otter 이상)로 프로젝트 폴더를 엽니다.
3.  Gradle Sync가 완료될 때까지 대기합니다.
4.  실행 타겟 (에뮬레이터 또는 실제 디바이스, **Android 8.0+/API 26 이상**)을 선택 후 **Run** 버튼을 클릭합니다.
5.  앱이 실행되면 PWA 화면이 로드되고, 웹 버튼을 통해 카메라/OCR 기능을 시연할 수 있습니다.

---

## 🌐 PWA URL 설정

프로젝트는 PWA URL을 `local.properties`에서 읽어 `BuildConfig`로 주입하는 안전하고 유연한 방식을 사용합니다.

### 1. `local.properties`에 URL 추가

Android Studio 프로젝트 루트에 있는 **`local.properties`** 파일에 다음 항목을 추가하고 **실제 PWA 주소**로 변경하세요:

```properties
pwa.url=[https://your-pwa-address.com](https://your-pwa-address.com)
```


# web2app-android (React PWA → Android WebApp)

React 기반 웹 앱(PWA)에 네이티브 카메라 OCR(광학 문자 인식) 기능을 연동하기 위해 개발된 Android 하이브리드 앱입니다.
WebView 내에서 웹 콘텐츠는 JavaScript Bridge를 통해 네이티브 카메라 기능을 호출하며, 촬영된 이미지에서 인식된 한국어 텍스트 결과는 비동기적으로 다시 웹뷰로 전달됩니다.
기존 웹 서비스를 최대한 재사용하면서 카메라·OCR 같은 네이티브 기능을 점진적으로 통합하는 데모 프로젝트입니다.

---

## 🎬 데모 시연 (GIF)

### 1. 앱 실행 & PWA 로딩

사용자가 앱을 실행하면, 곧바로 React 기반 PWA 웹앱이 WebView로 로드됩니다.

![demo-startup](docs/demo-startup.gif)

- 스플래시 이후 별도 네이티브 화면 없이 바로 웹앱 홈으로 진입  
- PWA의 네비게이션/라우팅을 그대로 활용

---

### 2. 웹에서 카메라 열기 (window.Android.startCamera)

웹 화면의 버튼을 누르면 JS에서 `window.Android.startCamera()`를 호출하고,  
네이티브 `CaptureActivity`가 실행됩니다.

![demo-open-camera](docs/demo-open-camera.gif)

- 웹 버튼 클릭 → JS 브리지 → 네이티브 화면 전환  
- 멀티 모듈 구조에서 `:app` 모듈이 네비게이션을 조율

---

### 3. 촬영 & OCR 결과를 웹으로 되돌리기 (window.onOcrResult)

카메라로 촬영한 이미지를 ML Kit Korean Text Recognition으로 OCR 처리한 뒤,  
결과를 JSON으로 직렬화하여 `window.onOcrResult(json)`으로 웹에 다시 전달합니다.

![demo-ocr-result](docs/demo-ocr-result.gif)

- 네이티브에서 추출한 텍스트 → JSON 변환  
- WebView 내 JS 함수 호출로 웹 UI 업데이트

---

## ✨ 주요 기능

- **기존 React PWA 웹앱을 그대로 재사용**
  - `WebView + PWA_URL` 로 앱스토어 배포 가능한 하이브리드 앱 구현
- **JS 브리지 기반 네이티브 연동**
  - `window.Android.startCamera()` → 네이티브 카메라 화면 호출
  - `window.onOcrResult(json)` → 네이티브 OCR 결과를 웹에 전달
- **카메라 & OCR 기능**
  - `CameraX` 기반 실시간 프리뷰 + 캡처
  - `ML Kit Text Recognition (Korean)` 으로 텍스트 OCR
- **멀티 모듈 아키텍처**
  - `:app` / `:core` / `:feature-webview` / `:feature-capture`
  - feature 간 의존 없이 `core`를 통한 느슨한 결합

---

## 🏗 아키텍처 개요

### 모듈 구조

```text
web2app-android
├─ app                # 엔트리 포인트, 네비게이션, DI/조율
├─ core               # 공통 모델, 유틸, 인터페이스 (OcrResult, OcrResultBus, CameraLauncher 등)
├─ feature-webview    # PWA WebView + JS 브리지(AndroidJsBridge, WebViewFragment)
└─ feature-capture    # CameraX + ML Kit OCR (CaptureActivity)
```

### 의존성 방향
```text
app
 ├─▶ core
 ├─▶ feature-webview
 └─▶ feature-capture

feature-webview ─▶ core
feature-capture ─▶ core

# feature ↔ feature 직접 의존 없음
```


## 🏗 아키텍처 모듈 설명 (표)

### 모듈별 역할 요약

| 모듈 | 핵심 책임 | 포함 파일 / 구성요소 | 외부 의존성 | 비고 |
|------|-----------|------------------------|---------------|-------|
| **core** | 공통 모델, 유틸, 추상화 인터페이스 | `OcrResult`, `OcrResultBus`, `CameraLauncher` | 없음 (Android Framework/CameraX/WebView에 의존 금지) | feature와 app의 공유 기반. DIP 준수 핵심 |
| **app** | 네비게이션 조율, Host Activity, BuildConfig(PWA_URL) 생성 | `MainActivity` (CameraLauncher 구현), DI(간단한 연결) | feature-webview, feature-capture, core | 앱 전체의 orchestration 담당 |
| **feature-webview** | PWA WebView 로딩, JS ↔ Android 브리지 | `WebViewFragment`, `AndroidJsBridge` | core | WebView에서 JS 기능 확장 담당 |
| **feature-capture** | CameraX 촬영 + ML Kit OCR 처리 | `CaptureActivity` | core, CameraX, ML Kit | 결과를 JSON으로 만들어 core로 전달 |

---

### core 모듈 상세

| 구성 요소 | 역할 | 비고 |
|-----------|------|-------|
| **OcrResult** | OCR 결과를 표현하는 모델 데이터 클래스 | 네이티브→웹 데이터를 JSON으로 만드는 기반 |
| **OcrResultBus** | `SharedFlow<String>` 기반 이벤트 버스 | 네이티브 OCR 결과를 WebViewFragment에 전달 |
| **CameraLauncher** | `fun launchCamera()` 단일 인터페이스 | WebViewFragment가 Activity를 직접 참조하지 않도록 분리된 추상화 |

---

### app 모듈 상세

| 구성 요소 | 역할 | 비고 |
|-----------|------|-------|
| **MainActivity** | CameraLauncher 구현, PWA WebViewFragment attach, 뒤로가기 처리 | 네비게이션 흐름의 중심 |
| **BuildConfig.PWA_URL** | `local.properties`에서 `pwa.url` 읽어 주입 | 환경별 PWA URL 분리 가능 |

---

### feature-webview 모듈 상세

| 구성 요소 | 역할 | 비고 |
|-----------|------|-------|
| **WebViewFragment** | PWA 로딩, 브리지 등록, OcrResultBus collect → JS 호출 | 실질적으로 “웹앱을 네이티브 앱처럼 보이게 하는 핵심” |
| **AndroidJsBridge** | 웹에서 `window.Android.startCamera()` → CameraLauncher 호출 | JS ↔ Android 다리 역할 |

---

### feature-capture 모듈 상세

| 구성 요소 | 역할 | 비고 |
|-----------|------|-------|
| **CaptureActivity** | CameraX 프리뷰/촬영, ML Kit OCR, JSON 생성, OcrResultBus.post | 네이티브 기능(카메라 + OCR) 담당 |
| **CameraX / ML Kit dependency** | 카메라 + OCR 기능 제공 | core에서는 사용하지 않음 (DIP 준수) |

---

## 📌 TODO / 향후 개선 계획

* 실제 PWA URL 연동 및 QA 진행.
* ML Kit OCR 결과를 기반으로 한 **도메인 모델링** (단순 텍스트 외 구조화).
* **Jetpack Compose** 기반의 네이티브 전용 화면 추가 통합.
* **Hilt 또는 Koin** 도입으로 DI 구조 명시화 및 복잡성 관리.
* 에러 처리, 오프라인 모드, PWA 캐싱 전략 등 **안정성 및 성능 개선**.
