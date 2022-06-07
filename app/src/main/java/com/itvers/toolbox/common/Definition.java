package com.itvers.toolbox.common;

import android.Manifest;

import java.util.UUID;

public class Definition {
    public static final String DEVICE_NAME_DFU = "QM UPGRAD";                                   // DFU
    public static final String DEVICE_NAME_HEADER_SMARTPATCH = "QMOUSE-MP";                     // 스마트 패치
    public static final String DEVICE_NAME_HEADER_SLIMMOUSE = "QMOUSE-US";                      // 마우스

    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };                                                                                          // 퍼미션

    public static final String PACKEGE_FUNKEY = "com.itvers.icapture";                          // FUNKEY 패키지

    public final static long SCAN_DURATION = 10000;                                             // 스캔 시간
    public final static long PROGRESS_DURATION = 8000;                                          // 프로그레스 시간

    public final static String URL_YOUTUBE_SMART_PATCH
            = "https://www.youtube.com/watch_popup?v=hKIPKj1bE7w&t=5s";                         // 유투브 스마트 패치 URL
    public final static String URL_YOUTUBE_MAIN_MOVIE
            = "https://www.youtube.com/watch_popup?v=hKIPKj1bE7w&t=5s";                         // 유투브 메인 URL
    public final static String URL_YOUTUBE_ROBOSTICK
            = "https://www.youtube.com/watch?v=knTuqaNlDa8&t=9s";                               // 로보스틱  URL
    public final static String URL_YOUTUBE_SLIM_MOUSE
            = "https://www.youtube.com/watch?v=Uz5TA9Agybg&t=3s";                               // 슬림 마우스
    // 업데이트 파일 경로
    public final static String FILE_PATH_SMART_PATCH = "smartpatch_app_package_v40.zip";        // 스마트패치 전체이미지 파일 경로
    public final static String FILE_PATH_MOUSE = "slimmouse_app_package_v02.zip";               // 마우스 업데이트 파일 경로

    public final static String FIRMWARE_VERSION_SMART_PATCH = "2.4.0";                          // 펌웨어 버전

    // TODO 테스트
    public final static int FIRMWARE_VERSION_SMART_PATCH_MIN = 230;                             // 펌웨어 Min 버전

    public final static int VENDOR_CODE_VERSION = 0;                                            // VENDOR CODE 버전

    /**
     * UART
     */
    public final static String ACTION_GATT_CONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED";                                     // GATT 연결
    public final static String ACTION_GATT_DISCONNECTED =
            "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED";                                  // GATT 연결 끊김
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED";                           // GATT 서비스 발견
    public final static String ACTION_DATA_AVAILABLE =
            "com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE";                                     // GATT DATA 사용 가능
    public final static String EXTRA_DATA =
            "com.nordicsemi.nrfUART.EXTRA_DATA";                                                // GATT DATA
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";                              // GATT 디바이스 지원 안함

    private static final String CLIENT_CHARACTERISTIC_CONFIG_UUID
            = "00002902-0000-1000-8000-00805f9b34fb";
    private static final String RX_SERVICE_UUID
            = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String RX_CHAR_UUID
            = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String TX_CHAR_UUID
            = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    public static final UUID UUID_CLIENT_CHARACTERISTIC_CONFIG
            = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG_UUID);
    public static final UUID UUID_RX_SERVICE
            = UUID.fromString(RX_SERVICE_UUID);
    public static final UUID UUID_RX_CHAR
            = UUID.fromString(RX_CHAR_UUID);
    public static final UUID UUID_TX_CHAR
            = UUID.fromString(TX_CHAR_UUID);

    public static final int TOTAL_DATA_REQUEST_READ_SETTING_LENGTH = 8;                        // 총 데이터 길이 (설정 읽기 요청)
    public static final int TOTAL_DATA_RESPONSE_READ_SETTING_LENGTH = 8;                       // 총 데이터 길이 (설정 읽기 응답)
    public static final int TOTAL_DATA_RESPONSE_WRITE_SETTING_LENGTH = 5;                      // 총 데이터 길이 (설정 쓰기 응답)
    public static final int TOTAL_DATA_RESPONSE_CHECK_BATTERY_LENGTH = 5;                      // 총 데이터 길이 (배터리 체크 응답)
    public static final int TOTAL_DATA_SETTING_BINARY_LENGTH = 32;                             // 총 데이터 길이 (설정 바이너리)
    public static final int TOTAL_DATA_FIRMWAVER_LENGTH = 9;                                   // 총 데이터 길이 (펌웨어)
    public static final int TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH = 12;              // 총 데이터 길이 (설정 읽기 응답)

    public static final int DATA_SETTING_LENGTH = 4;                                           // 데이터 길이 (설정)

    public static final String REQUEST_COMMAND_FIRMWARE_VERSION = "01";                         // 요청 명령 펌웨어 버전
    public static final String REQUEST_COMMAND_READ_USER_SETTING = "02";                        // 요청 명령 사용자 살정 읽기
    public static final String REQUEST_COMMAND_WRITE_USER_SETTING = "03";                       // 요청 명령 사용자 살정 쓰기
    public static final String REQUEST_COMMAND_BATTERY_CHECK = "04";                            // 요청 배터리 체크
    public static final String REQUEST_COMMAND_DEVICE_AWAKE = "05";                             // 요청 배터리 체크
    public static final String REQUEST_COMMAND_JUMP_BOOTLOADER = "06";                          // 요청 DFU 모드

    public static final String RESPONSE_COMMAND_FIRMWARE_VERSION = "81";                        // 응답 명령 펌웨어 버전
    public static final String RESPONSE_COMMAND_READ_USER_SETTING = "82";                       // 응답 명령 사용자 살정
    public static final String RESPONSE_COMMAND_WRITE_USER_SETTING = "83";                      // 응답 명령 사용자 살정 쓰기
    public static final String RESPONSE_COMMAND_BATTERY_CHECK = "84";                           // 응답 배터리 체크
    public static final String RESPONSE_COMMAND_FIRMWARE_UPDATE = "86";                         // 응답 명령 펌웨어 버전
    public static final String RESPONSE_COMMAND_FIRMWARE_INFORMATION = "89";                    // 응답 펌웨어 정보

    public static final int ACTIVITY_MODE_PATCH = 0;                                            // 엑티비티 모드 패치
    public static final int ACTIVITY_MODE_MOUSE = 1;                                            // 엑티비티 모드 마우스
    public static final int ACTIVITY_MODE_BUTTONS = 2;                                          // 엑티비티 모드 심플 패치

    public static final int INDEX_RESERVED_1 = 0;                                               // 기타 8
    public static final int INDEX_RESERVED_2 = 1;                                               // 기타 7
    public static final int INDEX_RESERVED_3 = 2;                                               // 기타 6
    public static final int INDEX_RESERVED_4 = 3;                                               // 기타 5

    public static final int INDEX_RESERVED_5 = 4;                                               // 기타 4
    public static final int INDEX_RESERVED_6 = 5;                                               // 기타 3
    public static final int INDEX_RESERVED_7 = 6;                                               // 기타 2
    public static final int INDEX_RESERVED_8 = 7;                                               // 기타 1

    // (0:Slim Mouse, 1:Smart Patch, 2:Robostick)
    public static final int INDEX_PRODUCE_MODEL_1 = 8;                                          // 제품 모델 1
    public static final int INDEX_PRODUCE_MODEL_2 = 9;                                          // 제품 모델 2
    public static final int INDEX_PRODUCE_MODEL_3 = 10;                                         // 제품 모델 3

    public static final int INDEX_MODE_WHEEL = 11;                                              // 휠 모드 (0, 1)

    // (00:Windows, 01:MacOS, 10:Android, 11:iOS)
    public static final int INDEX_TARGET_OS_1 = 12;                                             // 타겟 OS 1
    public static final int INDEX_TARGET_OS_2 = 13;                                             // 타겟 OS 2

    // (00:Presenter, 01:Camera, 10:Music, 11:Reserved)
    public static final int INDEX_WORKING_MODE_1 = 14;                                          // 작동 모드 1
    public static final int INDEX_WORKING_MODE_2 = 15;                                          // 작동 모드 2

    public static final int INDEX_VENDOR_1 = 16;                                                // 제조사 인덱스 1
    public static final int INDEX_VENDOR_2 = 17;                                                // 제조사 인덱스 2
    public static final int INDEX_VENDOR_3 = 18;                                                // 제조사 인덱스 3
    public static final int INDEX_VENDOR_4 = 19;                                                // 제조사 인덱스 4
    public static final int INDEX_VENDOR_5 = 20;                                                // 제조사 인덱스 5
    public static final int INDEX_VENDOR_6 = 21;                                                // 제조사 인덱스 6
    public static final int INDEX_VENDOR_7 = 22;                                                // 제조사 인덱스 7
    public static final int INDEX_VENDOR_8 = 23;                                                // 제조사 인덱스 8

    // (000:Default, 001:HD(1440*720), 010:FHD(2220*1080), 011:QHD(2960*1440), 100:UHD(3840*2160))
    public static final int INDEX_SCREEN_RESOLUTION_1 = 24;                                     // 해상도 인덱스 1
    public static final int INDEX_SCREEN_RESOLUTION_2 = 25;                                     // 해상도 인덱스 2
    public static final int INDEX_SCREEN_RESOLUTION_3 = 26;                                     // 해상도 인덱스 3

    public static final int INDEX_MODE = 27;                                                    // 동작모드 (0:Keyboard, 1:Mouse)

    // (00:Dull, 01:Sensitive1, 10:Sensitive2, 11:Sensitive3)
    public static final int INDEX_SENSITIVE_1 = 28;                                             // 민감도1
    public static final int INDEX_SENSITIVE_2 = 29;                                             // 민감도2

    public static final int INDEX_LR = 30;                                                      // 0:Normal, 1:Reverse

    public static final int INDEX_OS = 31;                                                      // 0:iOS, 1:Android

    /**
     * 키
     */
    public static final String KEY_INTENT_UUID = "uuid";                                        // UUID키
    public static final String KEY_INTENT_ACTIVITY_MODE = "activity_mode";                      // 엑티비티 모드 키
    public static final String KEY_INTENT_IS_FIRMWARE = "is_firmware";                          // 펌웨어 여부 키

    public static final String KEY_INTENT_BATTERY_LEVEL = "battery_level";                      // 배터리 레벨 키
    public static final String KEY_INTENT_FIRMWARE_INFORMATION_DATA = "firmware_information";   // 펌웨어 정보 데이터 키

    public static final String KEY_INTENT_URL = "url";                                          // URL 키

    public static final String KEY_DIALOG_SINGLE = "dialog_single";                             // 싱글 다이얼로그 키
    public static final String KEY_DIALOG_DUAL = "dialog_dual";                                 // 듀얼 다이얼로그 키
    public static final String KEY_DIALOG_TRIPLE = "dialog_triple";                             // 트리플 다이얼로그 키
    public static final String KEY_DIALOG_LIST = "dialog_list";                                 // 리스트 다이얼로그 키
    public static final String KEY_DIALOG_SINGLE_INPUT = "dialog_single_input";                 // 싱글 입력 다이얼로그 키
    public static final String KEY_DIALOG_MULTIPLE_INPUT = "dialog_multiple_input";             // 멀티 입력 다이얼로그 키
    public static final String KEY_DIALOG_SINGLE_SELECTION = "dialog_single_selection";         // 싱글 선택 다이얼로그 키
    public static final String KEY_DIALOG_MULTIPLE_SELECTION = "dialog_multiple_selection";     // 멀티 선택 다이얼로그 키

    public static final String KEY_BLUETOOTH_DEVICE = "bluetooth_device";                       // 블루투스 장비 키
    public static final String KEY_IS_CONNECTED = "is_connected";                               // 연결 여부
    public static final String KEY_IS_EMERGENCY = "is_emergency";                               // 긴급 여부
    public static final String KEY_IS_NUS= "is_nus";                                            // Nordic UART Service 여부


    public static final int DIALOG_BUTTON_POSITIVE = 1;                                         // 다이얼로그 확인 버튼 키
    public static final int DIALOG_BUTTON_NETURAL = 2;                                          // 다이얼로그 중간 버튼 키
    public static final int DIALOG_BUTTON_NEGATIVE = 0;                                         // 다이얼로그 취소 버튼 키

    public static final long GATT_INTERVAL_TIME = 2000;                                         // GATT 통신 대기 시간
    public static final long FINISH_INTERVAL_TIME = 2000;                                       // 앱 종료 시간 간격
    public static final long PROGRESS_INTERVAL_TIME = 8000;                                     // 프로그레스 다이얼로그 시간
    public static final long IMAGE_SCROLL_INTERVAL_TIME = 5000;                                 // 이미지 스크롤 시간 간격

    public static final int PMOD_SLIM_MOUSE = 0;                                                // 슬립 마우스
    public static final int PMOD_SMART_PATCH = 1;                                               // 스마트 패치
    public static final int PMOD_ROBOSTICK = 2;                                                 // 로보스틱

    public static final int MOUSE_CURSOR = 0;                                                   // 마우스 커서
    public static final int MOUSE_WHEEL = 1;                                                    // 마우스 휠

    public static final int TOS_WINDOWS = 0;                                                    // Windows
    public static final int TOS_MACOS = 1;                                                      // MacOS
    public static final int TOS_ANDROID = 2;                                                    // Android
    public static final int TOS_IOS = 3;                                                        // iOS

    public static final int WMOD_PRESENTER = 0;                                                 // Presenter
    public static final int WMOD_CAMERA = 1;                                                    // Camera
    public static final int WMOD_MUSIC = 2;                                                     // Music

    public static final int VCOD_B2C_MODEL = 0;                                                 // B2C Model
    public static final int VCOD_ITVERS = 1;                                                    // ITVERS
    public static final int VCOD_KT_MNS = 2;                                                    // KT M&S
    public static final int VCOD_SUNING = 3;                                                    // SUNING
    public static final int VCOD_MAX = 4;                                                       // SUNING

    public static final int RESOLUTION_DEFAULT = 0;                                             // 기본값
    public static final int RESOLUTION_HD = 1;                                                  // HD 사이즈
    public static final int RESOLUTION_FHD = 2;                                                 // FHD 사이즈
    public static final int RESOLUTION_QHD = 3;                                                 // QHD 사이즈
    public static final int RESOLUTION_UHD = 4;                                                 // UHD 사이즈

    public static final int KEYBOARD_MODE = 0;                                                  // 키보드 모드
    public static final int MOUSE_MODE = 1;                                                     // 마우스 모드

    public static final int MMV_DULL = 0;                                                       // 민감도 기본
    public static final int MMV_SENSITIVE_1 = 1;                                                // 민감도 1
    public static final int MMV_SENSITIVE_2 = 2;                                                // 민감도 2
    public static final int MMV_SENSITIVE_3 = 3;                                                // 민감도 3

    public static final int IOS = 0;                                                            // iOS
    public static final int ANDROID = 1;                                                        // 안드로이드

    public static final int LR_NORMAL = 0;                                                      // 기본
    public static final int LR_REVERSE = 1;                                                     // 반전

    public static final int DISPLAY_HD = 1280;                                                  // HD 사이즈
    public static final int DISPLAY_FHD = 1920;                                                 // FHD 사이즈
    public static final int DISPLAY_QHD = 2560;                                                 // QHD 사이즈

    public static final String SAMSUNG_EXCEPTION = "SM-G955N";                                  // 삼성 예외 모델

    public static final String SCAN_FRAGMENT = "scan_fragment";                                 // 스캔 다이얼로그 id

    public static final String PREFERENCES_NAME = "preferences";                                // 사용자 설정 파일명

    public static final String PREFERENCES_KEY_FIRMWARE_VERSION = "firmware_version";           // 사용자 설정 펌웨어 버전 키

    public static final String PREFERENCES_KEY_HOT_KEY_MESSAGING_URL
            = "hot_key_ messaging _url";                                                        // 핫키 1번 (MESSAGING) URL
    public static final String PREFERENCES_KEY_HOT_KEY_EMAIL_URL
            = "hot_key_email_url";                                                              // 핫키 2번 (EMAIL) URL
    public static final String PREFERENCES_KEY_HOT_KEY_MAP_URL
            = "hot_key_map_url";                                                                // 핫키 3번 (MAP) URL

    public static final String PREFERENCES_KEY_HOT_KEY_MESSAGING_APP_NAME
            = "hot_key_messaging_app_name";                                                     // 핫키 1번 (MESSAGING) APP NAME
    public static final String PREFERENCES_KEY_HOT_KEY_EMAIL_APP_NAME
            = "hot_key_email_app_name";                                                         // 핫키 2번 (EMAIL) APP NAME
    public static final String PREFERENCES_KEY_HOT_KEY_MAP_APP_NAME
            = "hot_key_map_app_name";                                                           // 핫키 3번 (MAP) APP NAME

    public static final String PREFERENCES_KEY_HOT_KEY_MESSAGING_PACKAG_NAME
            = "hot_key_messaging_package_name";                                                 // 핫키 1번 (MESSAGING) PACKAGE NAME
    public static final String PREFERENCES_KEY_HOT_KEY_EMAIL_PACKAG_NAME
            = "hot_key_email_package_name";                                                     // 핫키 2번 (EMAIL) PACKAGE NAME
    public static final String PREFERENCES_KEY_HOT_KEY_MAP_PACKAG_NAME
            = "hot_key_map_package_name";                                                       // 핫키 3번 (MAP) PACKAGE NAME

    public static final String PREFERENCES_KEY_HOT_KEY_MESSAGING_IS_APP
            = "hot_key_messaging_is_app";                                                       // 핫키 1번 APP 여부
    public static final String PREFERENCES_KEY_HOT_KEY_EMAIL_IS_APP
            = "hot_key_email_is_app";                                                           // 핫키 2번 APP 여부
    public static final String PREFERENCES_KEY_HOT_KEY_MAP_IS_APP
            = "hot_key_map_is_app";                                                             // 핫키 3번 APP 여부

    // VENDOR
    public static final String PREFERENCES_KEY_VENDOR_CODES                                     // VENDOR CODE 리스트
            = "vendor_codes";
    public static final String PREFERENCES_KEY_VENDOR_CODE_VERSION                              // VENDOR CODE 버전
            = "vendor_code_version";
    public static final String PREFERENCES_KEY_VENDOR_CODE                                      // VENDOR CODE
            = "vendor_code";

    // HOT KEY
    public static final String KEY_HOTKEY_TYPE = "hotkey_type";                                 // HOT KEY 타입
    public static final String KEY_HOTKEY_URL = "hotkey_url";                                   // HOT KEY URL
    public static final String KEY_HOTKEY_PACKAGE_NAME = "hotkey_package_name";                 // HOT KEY 패키지명
    public static final String KEY_HOTKEY_APP_NAME = "hotkey_app_name";                         // HOT KEY 앱명
    public static final String KEY_HOTKEY_PHONE = "hotkey_phone";                               // HOT KEY 전화번호

    public static final String KEY_DIALOG_TYPE = "dialog_type";                                 // 다이얼로그 타입

    public static final int TYPE_DIALOG_HOTKEY = 1000;                                          // 다이얼로그 핫키
    public static final int TYPE_DIALOG_INPUT_WEB = 1001;                                       // 다이얼로그 웹주소 입력
    public static final int TYPE_DIALOG_INPUT_PHONE_NUMBER = 1002;                              // 다이얼로그 전화번호 입력
    public static final int TYPE_DIALOG_SELECT_DUAL = 1003;                                     // 다이얼로그 듀얼 선택
    public static final int TYPE_DIALOG_RESET_HOTKEY = 1004;                                    // 다이얼로그 핫키 리셋
    public static final int TYPE_DIALOG_BLE_ENABLE = 1005;                                      // 다이얼로그 블루투스 사용

    public static final String PREFERENCES_KEY_B_KEY_DOUBLE                                     // B Key Double
            = "bkey_double";
    public static final String PREFERENCES_KEY_B_KEY_LONG                                       // B Key Long
            = "bkey_long";
    public static final String PREFERENCES_KEY_C_KEY_DOUBLE                                     // C Key Double
            = "ckey_double";
    public static final String PREFERENCES_KEY_C_KEY_LONG                                       // C Key Long
            = "ckey_long";

    public static final String PREFE    RENCES_KEY_IS_SET_HOT_KEY                                   // HOT KEY 설정
            = "is_set_hotkey";

    public static final String KEY_PACKAGE_NAME = "package_name";                               // 패키지명
    public static final String KEY_APP_TITLE = "app_title";                                     // 앱 타이틀

    public static final String VENDORCODE_PASSWORD = "9251107";                                 // 제조사 코드 패스워드
    public static final String UPGRADE_PASSWORD = "7801185";                                    // 업그레이드 패스워드

    public static final String URL_NAVER = "http://www.naver.com";                              // NAVER URL
    public static final String URL_ITVERS = "http://www.itvers.com";                            // ITVERS URL
    public static final String URL_KTMNS = "http://www.ktmns.com";                              // KT M&S URL
    public static final String URL_SUNING = "http://www.suning.com";                            // SUNING URL

    public static final int[] VENDOR_TYPE_1 = { 1, 2 };                                         // Vendor Type 1
    public static final int[] VENDOR_TYPE_2 = { 1 };                                            // Vendor Type 2
    public static final int[] VENDOR_TYPE_3 = { -1 };                                           // Vendor Type 3
}