package com.itvers.toolbox.activity.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itvers.toolbox.App;
import com.itvers.toolbox.R;
import com.itvers.toolbox.activity.HomeActivity;
import com.itvers.toolbox.activity.InformationActivity;
import com.itvers.toolbox.activity.QuickGuideActivity;
import com.itvers.toolbox.activity.main.admin.PasswordActivity;
import com.itvers.toolbox.activity.SelectDeviceActivity;
import com.itvers.toolbox.activity.WebViewActivity;
import com.itvers.toolbox.activity.main.hotkey.HotKeySettingsActivity;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.common.RequestCode;
import com.itvers.toolbox.item.Result;
import com.itvers.toolbox.item.Type;
import com.itvers.toolbox.item.UARTStatus;
import com.itvers.toolbox.dialog.DialogQMProgress;
import com.itvers.toolbox.view.HeightWrappingViewPager;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.ParserUtil;
import com.itvers.toolbox.util.PreferencesUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        App.BlutoothListener {

    private final static String TAG = MainActivity.class.getSimpleName();   // 디버그 태그

    int activityType = Definition.ACTIVITY_MODE_PATCH;                      // 엑티비티 타입
    private long backPressedTime = 0;                                       // 백버튼 클릭 타임
    private Intent intent;                                                  // 인텐트
    private ImageAdapter imageAdapter;                                      // 이미지 어댑터
    private int currentPage;                                                // 현재 페이지
    private Handler imageHandle;                                            // 이미지 핸들러
    private Runnable imageRunnable;                                         // 이미지 런에이블
    private static Handler progressHandler;                                 // 프로그레스 다이얼로그 핸들러
    private DialogQMProgress dialogQMProgress;                              // 프로그레스 다이얼로그

    private int[] images;                                                   // 배너 이미지

    private String[] arrayData;                                             // 데이터
    private String[] arrayTemp;                                             // 임시 데이터
    private  int vendorCode = -1;                                           // 제품 코드

    private boolean isDestroy = false;                                      // 종료 여부

    HeightWrappingViewPager viewPager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.i(TAG, "onActivityResult() -> "
                + "requestCode : " + requestCode
                + ", resultCode : " + resultCode
                + ", data : " + data);
        switch (requestCode) {
            case RequestCode.ACTIVITY_REQUEST_CODE_PASSWORD:
                // 뷰 페이저 설정
                setViewPager();
                break;
            default:
                break;
        }
    }

    @Override
    public void onUARTServiceChange(UARTStatus status) {
        LogUtil.i(TAG, "onUARTServiceChange() -> Start !!!(Status : " + status);
        //LogUtil.i(TAG, "onUARTServiceChange() -> status : " + status);

        // 데이터 초기화
        arrayData = new String[Definition.DATA_SETTING_LENGTH];
        for (int i = 0; i < Definition.DATA_SETTING_LENGTH; i++) {
            arrayData[i] = "0";
        }
        LogUtil.d(TAG, "onCreate() -> arrayData : " + arrayData.length);

        // 임시 데이터 초기화
        arrayTemp = new String[Definition.DATA_SETTING_LENGTH];
        for (int i = 0; i < Definition.DATA_SETTING_LENGTH; i++) {
            arrayTemp[i] = "0";
        }
        LogUtil.d(TAG, "onCreate() -> arrayTemp : " + arrayTemp.length);

        switch (status) {
            case SERVICE_CONNECTED: {
                // GATT 연결
                if (null != App.getInstance().getUARTService()) {
                    BluetoothDevice bluetoothDevice = App.getInstance().getUARTService().addBondedDevice(Definition.ACTIVITY_MODE_PATCH);
                    if (null != bluetoothDevice
                            && StringUtil.isNotNull(bluetoothDevice.getAddress())) {
                        App.getInstance().getUARTService().connect(bluetoothDevice.getAddress());
                    }
                }
            }
            break;
            case GATT_CONNECTED: {
                // 데이터 쓰기
                App.getInstance().writeData(Type.FIRMWARE_INFORMATION);
            }
            break;
            case GATT_SERVICES_DISCOVERED:
                break;
            case DATA_AVAILABLE:
                break;
            case SERVICE_DISCONNECTED:
            case SERVICE_BINDING_DIED:
            case DEVICE_DOES_NOT_SUPPORT_UART:
            case ERROR_EMPTY_UART_SERVICE:
            case ERROR_UART_SERVICE_INITIALIZATION:
            case ERROR_UART_SERVICE_CONNECT:
            case GATT_DISCONNECTED:
            case EMPTY_DEVICE:
            case EMPTY_DEVICE_ADDRESS:

                App.getInstance().setFirmwareInformationData(null);
                App.getInstance().setConnectedDevice(false);
                App.getInstance().setConnectedBluetoothDevice(null);

                // 장비 선택 엑티비티
                goToSelectDeviceActivity();
                break;
        }
    }

    @Override
    public void onUARTServiceData(Intent intent) {
        LogUtil.d(TAG, "onUARTServiceData() -> Start !!!");
        LogUtil.d(TAG, "onUARTServiceData() -> intent : " + intent);

        if (null != intent) {
            byte[] byteArray = intent.getByteArrayExtra(Definition.EXTRA_DATA);
            // 데이터 읽기
            Result result = App.getInstance().readData(byteArray, Type.FIRMWARE_INFORMATION);
            if (result == Result.SUCCESS) {
                if (byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH) {
                    App.getInstance().setFirmwareInformationData(byteArray);
                    // 레벨 데이터 파싱
                    parseBatteryLevelData();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        LogUtil.d(TAG, "onCreate() -> Start !!!");

        // 툴바
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 드로우 레이아웃
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                LogUtil.i(TAG, "onDrawerOpened() -> Start !!!");
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                LogUtil.i(TAG, "addDrawerListener() -> Start !!!");

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                LogUtil.i(TAG, "onDrawerClosed() -> Start !!!");
                if (null != intent) {
                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_NEXT_ACTIVITY);
                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                }
                intent = null;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                LogUtil.i(TAG, "onDrawerStateChanged() -> Start !!!");
            }
        });

        // 네비게이션 뷰
        NavigationView navigationView = findViewById(R.id.navigation_view);
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_setting);
        View view = navigationView.inflateHeaderView(R.layout.nav_header_main);
        LinearLayout layoutHeader = view.findViewById(R.id.header_layout);
        layoutHeader.setBackgroundResource(R.drawable.navigation_title_patch);
        view.findViewById(R.id.nav_header_main_btn_user).setOnClickListener(this);

        // 버튼 리스너 등록
        findViewById(R.id.content_main_btn_setting).setOnClickListener(this);
        findViewById(R.id.content_main_btn_hotkey).setOnClickListener(this);
        findViewById(R.id.content_main_btn_quickguide).setOnClickListener(this);
        findViewById(R.id.content_main_btn_firmware_upgrade).setOnClickListener(this);
        findViewById(R.id.app_bar_main_iv_home).setOnClickListener(this);
        findViewById(R.id.navigation_youtube).setOnClickListener(this);
        findViewById(R.id.app_bar_main_iv_battery).setOnClickListener(this);

        // TODO 테스트 홈버튼 숨기기
        findViewById(R.id.app_bar_main_iv_home).setVisibility(View.GONE);

        // 인텐트 모드
        Intent intent = getIntent();
        activityType = intent.getIntExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
        LogUtil.d(TAG, "onCreate() -> activityType : " + activityType);

        switch (activityType) {
            case Definition.ACTIVITY_MODE_PATCH:
                layoutHeader.setBackgroundResource(R.drawable.navigation_title_patch);
                ((TextView) findViewById(R.id.content_main_tv_setting)).setText(R.string.main_setting);
                menuItem.setTitle(R.string.main_setting);
                break;
            case Definition.ACTIVITY_MODE_MOUSE:
                layoutHeader.setBackgroundResource(R.drawable.navigation_title_mouse);
                ((TextView) findViewById(R.id.content_main_tv_setting)).setText(R.string.pointer_speed);
                menuItem.setTitle(R.string.pointer_speed);
                break;
            default:
                break;
        }
        navigationView.setNavigationItemSelectedListener(this);

        byte[] byteArray = App.getInstance().getFirmwareInformationData();
        LogUtil.d(TAG, "onCreate() -> byteArray : " + byteArray);

        if (null != byteArray) LogUtil.d(TAG, "onCreate() -> byteArray : " + byteArray.length);
        if (null != byteArray)
            LogUtil.d(TAG, "onCreate() -> getBluetoothDevice : " + App.getInstance().getBluetoothDevice());
        if (null != App.getInstance().getBluetoothDevice())
            LogUtil.d(TAG, "onCreate() -> getName : " + App.getInstance().getBluetoothDevice().getName());
        if (null != byteArray
                && byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH
                && null != App.getInstance().getBluetoothDevice()
                && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getName())) {
            // 사용자 정보 파싱
            parseUserSettingData();
        } else {
            if (null == App.getInstance().getBluetoothDevice()) {
                // TODO Offline
                finish();
            } else {
                // 프로그레스 다이얼로그 시작
                showProgress();

                if (null != App.getInstance().getUARTService()
                        && null != App.getInstance().getBluetoothDevice()
                        && StringUtil.isNotNull(App.getInstance().getBluetoothDevice().getAddress())) {
                    App.getInstance().getUARTService().connect(App.getInstance().getBluetoothDevice().getAddress());
                }
            }
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        LogUtil.d(TAG, "onCreate() >> size.x: " + size.x + ", size.y: " + size.y);
        if (size.y == 2560) {
            LinearLayout.LayoutParams layout
                    = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layout.weight = 5.2f;
            findViewById(R.id.content_main_rl_image).setLayoutParams(layout);

            layout = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layout.weight = 4.8f;
            findViewById(R.id.content_main_rl_menu).setLayoutParams(layout);
        }

        // 뷰 페이저 설정
        setViewPager();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume() -> Start !!!");

        // BLE 리스너 등록
        App.getInstance().setBlutoothListener(this, MainActivity.this);

        boolean success = PreferencesUtil.getInstance(MainActivity.this).clearFirmwareVersion();
        LogUtil.d(TAG, "onResume() -> clearFirmwareVersion : " + success);
        if (null != imageHandle)
            imageHandle.postDelayed(imageRunnable, Definition.IMAGE_SCROLL_INTERVAL_TIME);

        LogUtil.d(TAG, "onResume() -> getBluetoothAdapter : " + App.getInstance().getBluetoothAdapter());
        // BLE 어댑터 사용 여부 체크
        if (null != App.getInstance().getBluetoothAdapter()) {
            if (!App.getInstance().getBluetoothAdapter().isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, RequestCode.REQUEST_ENABLE_BLUTOOTH);
                return;
            }
        }
        LogUtil.d(TAG, "onResume() -> getBlutoothListener : " + App.getInstance().getBlutoothListener());
        LogUtil.d(TAG, "onResume() -> getBlutoothListener : " + App.getInstance().getBlutoothListener());

        if (!App.getInstance().getConnectedDevice()) {
            // 장비 선택 엑티비티
            goToSelectDeviceActivity();
            return;
        }

        byte[] byteArray = App.getInstance().getFirmwareInformationData();
        LogUtil.d(TAG, "onResume() -> byteArray : " + byteArray);
        if (null != byteArray
                && byteArray.length == Definition.TOTAL_DATA_RESPONSE_FIRMWARE_INFORMATION_LENGTH) {
            // 레벨 데이터 파싱
            parseBatteryLevelData();
        } else {
            if (null == App.getInstance().getUARTService()) {
                showProgress();
                App.getInstance().startUARTService();
            }
        }
        LogUtil.d(TAG, "onResume() -> getBlutoothListener : " + App.getInstance().getBlutoothListener());
        success = App.getInstance().setBlutoothListener(this, MainActivity.this);
        LogUtil.d(TAG, "onResume() -> setBlutoothListener : " + success);
        LogUtil.d(TAG, "onResume() -> getBlutoothListener : " + App.getInstance().getBlutoothListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e(TAG, "onPause() -> Start !!!");
        if (null != imageHandle
                && null != imageRunnable) {
            imageHandle.removeCallbacks(imageRunnable);
        }
    }

    // 백버튼
    @Override
    public void onBackPressed() {
        LogUtil.e(TAG, "onBackPressed() -> Start !!!");
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            // TODO MOUSE 사용 안하므로 바로 종료
//            super.onBackPressed();
//            LogUtil.i(TAG, "onBackPressed() -> Start !!!");
//            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);

            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPressedTime;
            if (0 <= intervalTime
                    && Definition.FINISH_INTERVAL_TIME >= intervalTime) {
                isDestroy = true;
                super.onBackPressed();
            } else {
                backPressedTime = tempTime;
                ToastUtil.getInstance().show(MainActivity.this, getResources().getString(R.string.back_exit), false);
            }
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.e(TAG, "onDestroy() -> Start !!!");
        super.onDestroy();

        // 이미지 핸들러 종료
        if (null != imageHandle) imageHandle.removeMessages(0);

        // BLE 리스너 해제
//        App.getInstance().setBlutoothListener(null, MainActivity.this);

        // UART 서비스 종료
        if (isDestroy) App.getInstance().stopUARTService();

        // 프로그레스 종료
        dismissProgress();
    }

    public class ImageAdapter extends PagerAdapter {
        Context context;

        ImageAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView imageView = new ImageView(context);
            imageView.setPadding(0, 0, 0, 0);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageResource(images[position]);
            container.addView(imageView, 0);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    switch (position) {
                        case 0:
                            switch(vendorCode) {
                                case 2: // KT M&S
                                    intent = new Intent(MainActivity.this, WebViewActivity.class);
                                    intent.putExtra(Definition.KEY_INTENT_URL, Definition.URL_KTMNS);
                                    break;
                                case 3: // SUNING
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Definition.URL_SUNING));
                                    startActivity(intent);
                                    break;
                                default: //B2C or ITVERS
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Definition.URL_ITVERS));
                                    startActivity(intent);
                                    break;
                            }
                            break;
                        case 1:
                            switch(vendorCode) {
                                case 2:
                                case 3:
                                    //intent = new Intent(MainActivity.this, WebViewActivity.class);
                                    //intent.putExtra(Definition.KEY_INTENT_URL, Definition.URL_YOUTUBE_SLIM_MOUSE);
                                    //intent.putExtra("force_fullscreen",true);
                                    //break;
                                default:
                                    //Steve_20190802 //For full screen!!
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse( Definition.URL_YOUTUBE_SLIM_MOUSE));
                                    intent.putExtra("VIDEO_ID", Definition.URL_YOUTUBE_SLIM_MOUSE);
                                    intent.putExtra("force_fullscreen",true);
                                    break;
                            }
                            break;
                        case 2:
                            switch(vendorCode) {
                                case 2:
                                case 3:
                                    //Steve_20190802 //For full screen!!
                                    //intent = new Intent(Intent.ACTION_VIEW, Uri.parse( Definition.URL_YOUTUBE_MAIN_MOVIE));
                                    //intent.putExtra("VIDEO_ID", Definition.URL_YOUTUBE_MAIN_MOVIE);
                                    //intent.putExtra("force_fullscreen",true);
                                    //break;
                                default:
                                    //Steve_20190802 //For full screen!!
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse( Definition.URL_YOUTUBE_MAIN_MOVIE));
                                    intent.putExtra("VIDEO_ID", Definition.URL_YOUTUBE_MAIN_MOVIE);
                                    intent.putExtra("force_fullscreen",true);
                                    break;
                            }
                            break;
                        case 3:
                            switch(vendorCode) {
                                case 2:
                                case 3:
                                    //Steve_20190802 //For full screen!!
                                    //intent = new Intent(Intent.ACTION_VIEW, Uri.parse( Definition.URL_YOUTUBE_ROBOSTICK));
                                    //intent.putExtra("VIDEO_ID", Definition.URL_YOUTUBE_ROBOSTICK);
                                    //intent.putExtra("force_fullscreen",true);
                                    //break;
                                default:
                                    //Steve_20190802 //For full screen!!
                                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse( Definition.URL_YOUTUBE_ROBOSTICK));
                                    intent.putExtra("VIDEO_ID", Definition.URL_YOUTUBE_ROBOSTICK);
                                    intent.putExtra("force_fullscreen",true);
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                    if (null != intent) { startActivity(intent); }
                }
            });
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }
    }

    /**
     * 뷰 페이저 설정
     */
    private void setViewPager() {
        LogUtil.d(TAG, "setViewPager() -> Start !!!");

        vendorCode = PreferencesUtil.getInstance(MainActivity.this).getVendorCode();
        LogUtil.d(TAG, "onResume() -> vendorCode : " + vendorCode);
        switch(vendorCode) {
            case 2:
                images = new int[]{
                        R.drawable.banner_ktmns,
                        R.drawable.banner_02,
                        R.drawable.banner_03,
                        R.drawable.banner_04
                };
                break;
            case 3:
                images = new int[]{
                        R.drawable.banner_suning,
                        R.drawable.banner_02,
                        R.drawable.banner_03,
                        R.drawable.banner_04
                };
                break;
            default:
                images = new int[]{
                        R.drawable.banner_itvers,
                        R.drawable.banner_02,
                        R.drawable.banner_03,
                        R.drawable.banner_04
                };
                break;
        }

        // 뷰 페이지
        viewPager = findViewById(R.id.content_main_view_pager);
        viewPager.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageAdapter = new ImageAdapter(MainActivity.this);
        viewPager.setAdapter(imageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                LogUtil.i(TAG, "onPageScrolled() -> Start !!!");
            }

            @Override
            public void onPageSelected(int position) {
//                LogUtil.i(TAG, "onPageSelected() -> Start !!!");
                currentPage = position;
                if (null != imageHandle
                        && null != imageRunnable) {
                    imageHandle.removeCallbacks(imageRunnable);
                    imageHandle.postDelayed(imageRunnable, Definition.IMAGE_SCROLL_INTERVAL_TIME);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                LogUtil.i(TAG, "onPageScrollStateChanged() -> Start !!!");
            }
        });

        // 이미지 핸들러
        imageHandle = new Handler();
        imageRunnable = new Runnable() {
            public void run() {
                if (imageAdapter.getCount() == currentPage) {
                    currentPage = 0;
                } else {
                    currentPage++;
                }
                viewPager.setCurrentItem(currentPage, true);
                imageHandle.postDelayed(this, Definition.IMAGE_SCROLL_INTERVAL_TIME);
            }
        };

        // 탭 레이아웃 (Dots)
        TabLayout tabLayout = findViewById(R.id.content_main_tab_dots);
        tabLayout.setupWithViewPager(viewPager, true);

        if (vendorCode == 3) tabLayout.setVisibility(View.GONE);
    }

    /**
     * 네비게이션 리스트 버튼
     *
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        LogUtil.i(TAG, "onNavigationItemSelected() -> Start !!!");
        int id = item.getItemId();

        if (id == R.id.nav_setting) {
            if (activityType == Definition.ACTIVITY_MODE_PATCH) {
                intent = new Intent(MainActivity.this, SettingPatchActivity.class);
                LogUtil.d(TAG, "onNavigationItemSelected() -> byteArray : " + App.getInstance().getFirmwareInformationData());
            } else {
                intent = new Intent(MainActivity.this, SettingMouseActivity.class);
            }
        } else if (id == R.id.nav_hotkey) {
            intent = new Intent(MainActivity.this, HotKeySettingsActivity.class);
        } else if (id == R.id.nav_manual) {
            intent = new Intent(MainActivity.this, QuickGuideActivity.class);
        } else if (id == R.id.nav_firmware) {
            intent = new Intent(MainActivity.this, FirmwareActivity.class);
        } else if (id == R.id.nav_information) {
            intent = new Intent(MainActivity.this, InformationActivity.class);
        }
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * 클릭 이벤트
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        LogUtil.i(TAG, "onClick() -> Start !!!");
        boolean isFinish = false;
        boolean isPassword = false;
        switch (v.getId()) {
            case R.id.content_main_btn_setting:
                if (activityType == Definition.ACTIVITY_MODE_PATCH) {
                    intent = new Intent(MainActivity.this, SettingPatchActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, SettingMouseActivity.class);
                }
                break;
            case R.id.content_main_btn_hotkey:
                intent = new Intent(MainActivity.this, HotKeySettingsActivity.class);
                break;
            case R.id.content_main_btn_quickguide:
                intent = new Intent(MainActivity.this, QuickGuideActivity.class);
                intent.putExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
                break;
            case R.id.content_main_btn_firmware_upgrade:
                intent = new Intent(MainActivity.this, FirmwareActivity.class);
                intent.putExtra(Definition.KEY_INTENT_ACTIVITY_MODE, Definition.ACTIVITY_MODE_PATCH);
                break;
            case R.id.navigation_youtube:
                intent = new Intent(MainActivity.this, WebViewActivity.class);
                intent.putExtra(Definition.KEY_INTENT_URL, Definition.URL_YOUTUBE_SMART_PATCH);
                break;
            case R.id.app_bar_main_iv_home:
                intent = new Intent(MainActivity.this, HomeActivity.class);
                isFinish = true;
                break;
            case R.id.app_bar_main_iv_battery:
                //Steve_20190731 //App terminated abnormally!!!
                intent = null;
                // 장비 선택 엑티비티
                goToSelectDeviceActivity();
                break;
            case R.id.nav_header_main_btn_user:
                intent = new Intent(MainActivity.this, PasswordActivity.class);
                ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
                isPassword = true;
                break;
        }

        LogUtil.d(TAG, "onClick() -> isFinish: " + isFinish);
        if (isFinish) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            // 엑티비티 종료
            finish();
            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
        } else {
            if (null != intent) {
                if (isPassword) {
                    startActivityForResult(intent, RequestCode.ACTIVITY_REQUEST_CODE_PASSWORD);
                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                } else {
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit);
                }
            }
        }
        intent = null;
    }

    /**
     * 배터리 레벨
     */
    private void parseBatteryLevelData() {
        LogUtil.i(TAG, "parseBatteryLevelData() -> Start !!!");
        if (null == App.getInstance().getFirmwareInformationData()) return;
        String hexadecimal = ParserUtil.byteArrayToHexadecimal(App.getInstance().getFirmwareInformationData());
        LogUtil.d(TAG, "parseBatteryLevelData() -> hexadecimal : " + hexadecimal);
        // TODO 텍스트
        String[] array = hexadecimal.split("\\p{Z}");
        LogUtil.d(TAG, "parseBatteryLevelData() -> array : " + array);
//        for (String string : array) {
//            LogUtil.d(TAG, "parseBatteryLevelData() -> string : " + string);
//        }
        LogUtil.d(TAG, "parseBatteryLevelData() -> array : " + array.length);
        // 유효성 체크
        String stx = array[0];
        LogUtil.d(TAG, "parseBatteryLevelData() -> stx : " + stx);
        String length = array[2];
        LogUtil.d(TAG, "parseBatteryLevelData() -> length : " + length);
        String etx = array[11];
        LogUtil.d(TAG, "parseBatteryLevelData() -> etx : " + etx);
        String command = array[1];
        LogUtil.d(TAG, "parseBatteryLevelData() -> command : " + command);

        // 펌웨어 정보
        if (Definition.RESPONSE_COMMAND_FIRMWARE_INFORMATION.equals(command)) {
            LogUtil.d(TAG, "parseBatteryLevelData() -> body : " + array[3]);
            // 배터리 레벨
            int batteryLevel = Integer.parseInt(array[10], 16);
            LogUtil.d(TAG, "parseBatteryLevelData() -> batteryLevel : " + batteryLevel);

            // 배터리 아이콘, 레벨 보이기
//            visibleBatteyIconNLevel();

            //Steve_20190812
            // 배터리 잔량 아이콘
            displayBatteryLevel(batteryLevel);
        }
    }

    /**
     * 프로그레스 다이얼로그 핸들러 시작
     */
    private void startProgressHandler() {
        LogUtil.i(TAG, "startProgressHandler() -> Start !!!");
        progressHandler = new Handler();
        progressHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 프로그레스 다이얼로그 후처리
                postProgress();
            }
        }, Definition.PROGRESS_INTERVAL_TIME);
    }

    /**
     * 프로그레스 다이얼로그 핸들러 중지
     */
    private void stopProgressHandler() {
        LogUtil.i(TAG, "stopProgressHandler() -> Start !!!");
        if (null != progressHandler) progressHandler.removeMessages(0);
    }

    /**
     * 프로그레스 후처리
     */
    private void postProgress() {
        LogUtil.i(TAG, "postProgress() -> Start !!!");
        // 프로그레스 다이얼로그 종료
        dismissProgress();
    }

    /**
     * 프로그레스 다이얼로그 시작
     */
    private void showProgress() {
        LogUtil.i(TAG, "showProgress() -> Start !!!");

        if (App.getInstance().getIsBackground()) return;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
        LogUtil.d(TAG, "showProgress() -> componentName: " + componentName);
        LogUtil.d(TAG, "showProgress() -> className: " + componentName.getClassName());
        LogUtil.d(TAG, "showProgress() -> getName: " + MainActivity.class.getName());

        // 프로그레스 다이얼로그 종료
        dismissProgress();

        if (componentName.getClassName().equals(MainActivity.class.getName())) {
            dialogQMProgress = new DialogQMProgress(this, null, true);
            dialogQMProgress.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            if (null != dialogQMProgress) {
                dialogQMProgress.show();

                // 프로그레스 다이얼로그 핸들러 시작
                startProgressHandler();
            }
        }
    }

    /**
     * 프로그레스 종료
     */
    private void dismissProgress() {
        LogUtil.i(TAG, "dismissProgress() -> Start !!!");

        // 프로그레스 다이얼로그 핸들러 중지
        stopProgressHandler();

        if (null != dialogQMProgress
                && dialogQMProgress.isShowing()) {
            LogUtil.d(TAG, "dismissProgress() -> dialogQMProgress: " + dialogQMProgress);
//            dismissDialog(MainActivity.this, dialogQMProgress);
            dialogQMProgress.cancel();
            dialogQMProgress = null;
        }
    }

    /**
     * 다이얼로그 종료
     *
     * @param activity  엑티비티
     * @param dialog    다이얼로그
     */
    private static void dismissDialog(Activity activity, Dialog dialog) {
        if (activity.isDestroyed()) {
            return;
        }
        if (null != dialog && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 배터리 잔량 아이콘
     *
     * @param level
     */
    //Steve_20190812  //Retrieved to display battery bar
    private void displayBatteryLevel(int level) {
        LogUtil.e(TAG, "displayBatteryLevel() -> level : " + level);
        int rate = level / 10;
        LogUtil.d(TAG, "displayBatteryLevel() -> rate : " + rate);
        switch(rate) {
            case 0:
            case 1:
                ((ImageView) findViewById(R.id.app_bar_main_iv_battery)).setImageResource(R.drawable.ic_battery_empty);
                break;
            case 2:
            case 3:
                (( ImageView ) findViewById(R.id.app_bar_main_iv_battery)).setImageResource(R.drawable.ic_battery_low);
                break;
            case 4:
            case 5:
                (( ImageView ) findViewById(R.id.app_bar_main_iv_battery)).setImageResource(R.drawable.ic_battery_medium);
                break;
            case 6:
            case 7:
            case 8:
                (( ImageView ) findViewById(R.id.app_bar_main_iv_battery)).setImageResource(R.drawable.ic_battery_high);
                break;
            case 9:
            case 10:
                (( ImageView ) findViewById(R.id.app_bar_main_iv_battery)).setImageResource(R.drawable.ic_battery_full);
                break;

        }
//        ((TextView) findViewById(R.id.app_bar_main_tv_bettery_level)).setText(batteryLevel + "%");
    }

    /**
     * 배터리 아이콘, 레벨 보이기
     */
//    private void visibleBatteyIconNLevel() {
//        LogUtil.i(TAG, "visibleBatteyIconNLevel() -> Show!!");
//        findViewById(R.id.app_bar_main_tv_bettery_level).setVisibility(View.VISIBLE);
//        findViewById(R.id.app_bar_main_iv_battery).setVisibility(View.VISIBLE);
//    }

    /**
     * 배터리 아이콘, 레벨 숨기기
     */
//    private void goneBatteyIconNLevel() {
//        LogUtil.i(TAG, "goneBatteyIconNLevel() -> Gone!!");
//        findViewById(R.id.app_bar_main_tv_bettery_level).setVisibility(View.GONE);
//        findViewById(R.id.app_bar_main_iv_battery).setVisibility(View.GONE);
//    }

    /**
     * 장비 선택 엑티비티
     */
    private void goToSelectDeviceActivity() {
        LogUtil.i(TAG, "goToSelectDeviceActivity() -> Gone!!");
        Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        // 엑티비티 종료
        finish();
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * Quick Guide 엑티비티
     */
    private void goToQuickGuideActivity() {
        LogUtil.i(TAG, "goToQuickGuideActivity() -> Gone!!");

        Intent intent = new Intent(MainActivity.this, QuickGuideActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
    }

    /**
     * 사용자 설정
     */
    private void parseUserSettingData() {
        if (null == App.getInstance().getFirmwareInformationData()) return;

        String hexadecimal = ParserUtil.byteArrayToHexadecimal(App.getInstance().getFirmwareInformationData());
        LogUtil.d(TAG, "parseUserSettingData() -> hexadecimal : " + hexadecimal);

        // TODO 텍스트
        String[] array = hexadecimal.split("\\p{Z}");
        LogUtil.d(TAG, "parseUserSettingData() -> array : " + array);

//        for (String string : array) {
//            LogUtil.d(TAG, "parseUserSettingData() -> string : " + string);
//        }

        // 유효성 체크
        String stx = array[0];
        LogUtil.d(TAG, "parseUserSettingData() -> stx : " + stx);
        String length = array[2];
        LogUtil.d(TAG, "parseUserSettingData() -> length : " + length);
        String etx = array[11];
        LogUtil.d(TAG, "parseUserSettingData() -> etx : " + etx);
        String command = array[1];
        LogUtil.d(TAG, "parseUserSettingData() -> command : " + command);

        // 펌웨어 정보 읽기
        if (Definition.RESPONSE_COMMAND_FIRMWARE_INFORMATION.equals(command)) {
            String binary = "";
            binary += ParserUtil.hexadecimalToBinary(array[3], 8);
            binary += ParserUtil.hexadecimalToBinary(array[4], 8);
            binary += ParserUtil.hexadecimalToBinary(array[5], 8);
            binary += ParserUtil.hexadecimalToBinary(array[6], 8);
            LogUtil.d(TAG, "parseUserSettingData() -> binary : " + binary + ", length : " + binary.length());

            String[] arrBody = ParserUtil.getSettingData(binary);
            if (null == arrBody) {
                LogUtil.e(TAG, "parseUserSettingData() -> arrBody is null.");
                ToastUtil.getInstance().show(
                        MainActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }

            LogUtil.d(TAG, "parseUserSettingData() -> arrBody : " + arrBody + ", length : " + arrBody.length);
            if (Definition.TOTAL_DATA_SETTING_BINARY_LENGTH != arrBody.length) {
                LogUtil.e(TAG, "parseUserSettingData() -> arrBody length is not invalid.");
                ToastUtil.getInstance().show(
                        MainActivity.this,
                        getResources().getString(R.string.unknown_error),
                        false);
                return;
            }
            arrayData = arrBody;

            // 제조사
            String[] temp = new String[8];
            temp[0] = arrayData[Definition.INDEX_VENDOR_1];
            temp[1] = arrayData[Definition.INDEX_VENDOR_2];
            temp[2] = arrayData[Definition.INDEX_VENDOR_3];
            temp[3] = arrayData[Definition.INDEX_VENDOR_4];
            temp[4] = arrayData[Definition.INDEX_VENDOR_5];
            temp[5] = arrayData[Definition.INDEX_VENDOR_6];
            temp[6] = arrayData[Definition.INDEX_VENDOR_7];
            temp[7] = arrayData[Definition.INDEX_VENDOR_8];

            String strVendor = "";
            for (String string : temp) {
                strVendor += string;
            }
            LogUtil.d(TAG, "parseUserSettingData() -> strVendor : " + strVendor);
            vendorCode = Integer.parseInt(ParserUtil.binaryToHexadecimal(strVendor));

            PreferencesUtil.getInstance(MainActivity.this).setVendorCode(vendorCode);
        }
    }
}
