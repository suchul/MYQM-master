package com.itvers.toolbox.activity.main.hotkey;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.item.ItemPackage;
import com.itvers.toolbox.item.Key;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.PackageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HotKeyPackageActivity extends Activity {

    private static final String TAG = HotKeyPackageActivity.class.getSimpleName();

    private List<ItemPackage> list = new ArrayList<>();

    private ProgressBar progressBar;
    private PackageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);

        // 프로그레스 바
        progressBar = findViewById(R.id.activity_package_pb_progress);

        // 뒤로 가기
        findViewById(R.id.activity_package_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });

        // 설치된 패키지 리스트 어댑터
        adapter = new PackageListAdapter();
        ListView listView = findViewById(R.id.activity_package_lv_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ItemPackage item = list.get(position);
                Intent intent = getIntent();
                intent.putExtra(Definition.KEY_HOTKEY_TYPE, Key.APP);
                intent.putExtra(Definition.KEY_PACKAGE_NAME, item.getPackageName());
                intent.putExtra(Definition.KEY_APP_TITLE, item.getAppTitle());
                setResult(Activity.RESULT_OK, intent);

                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                list = PackageUtil.getInstalledPackageList(HotKeyPackageActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.i(TAG, "onResume() -> Start !!!");
    }

    /**
     * 설치된 패키지 리스트 어댑터
     */
    private class PackageListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(HotKeyPackageActivity.this, R.layout.row_list_package, null);
                holder = new ViewHolder();

                holder.ivAppIcon = convertView.findViewById(R.id.row_list_app_icon);
                holder.tvAppName = convertView.findViewById(R.id.row_list_app_name);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ItemPackage item = list.get(position);
            String appName = item.getAppTitle();
            LogUtil.d(TAG, "PackageListAdapter() -> appName : " + appName);
            Drawable icon = item.getIcon();
            LogUtil.d(TAG, "PackageListAdapter() -> icon : " + icon);
            holder.tvAppName.setText(appName);
            holder.ivAppIcon.setImageDrawable(icon);
            return convertView;
        }

        /**
         * 뷰홀더
         */
        private class ViewHolder {
            ImageView ivAppIcon;
            TextView tvAppName;
        }
    }
}
