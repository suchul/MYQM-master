package com.itvers.toolbox.activity.main.hotkey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.item.ItemContacts;
import com.itvers.toolbox.item.Key;
import com.itvers.toolbox.util.ContactsUtil;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class HotKeyContactsActivity extends Activity {
    private static final String TAG = HotKeyContactsActivity.class.getSimpleName(); // 디버그 태그

    private ListView listView;                                                      // 리스뷰
    private ContactsAllAdapter adapter;                                             // 명함첩 어댑터
    private ArrayList<ItemContacts> listAll = new ArrayList<>();                    // 주소록 전체 리스트
    private ArrayList<ItemContacts> list = new ArrayList<>();                       // 주소록 리스트
    private ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        LogUtil.d(TAG, "onCreate() -> Start !!!");

        // 프로그레스 바
        progressBar = findViewById(R.id.activity_contacts_pb_progress);

        // 주소록 리스트 초기화
        if (list == null) {
            list = new ArrayList<>();
        } else {
            list.clear();
        }

        // 주소록 전체 리스트 초기화
        if (listAll == null) {
            listAll = new ArrayList<>();
        } else {
            listAll.clear();
        }

        // 어댑터 초기화
        adapter = new ContactsAllAdapter();
        listView = findViewById(R.id.activity_contacts_lv_list);
        listView.setAdapter(adapter);

        // 리스트뷰 클릭 이벤트
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LogUtil.d(TAG, "onItemClick() -> poisition : " + position);
                ItemContacts contacts = list.get(position);
                LogUtil.d(TAG, "onItemClick() -> getHp() : " + contacts.getHp());

                Intent intent = getIntent();
                intent.putExtra(Definition.KEY_HOTKEY_PHONE, contacts.getHp());
                intent.putExtra(Definition.KEY_HOTKEY_TYPE, Key.PHONE);
                setResult(RESULT_OK, intent);

                // 엑티비티 종료
                finish();
                overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit);
            }
        });

        // 스크롤 리스너
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem,
                                 int visibleItemCout,
                                 int totalItemCount) { }
        });

        // 뒤로가기 버튼
        findViewById(R.id.activity_contacts_ll_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 검색 입력창
        ((EditText) findViewById(R.id.activity_contacts_et_search)).addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String search = s.toString().trim();
                // 리스트 초기화
                if (list == null) {
                    list = new ArrayList<>();
                } else {
                    list.clear();
                }

                // 검색어
                String input = s.toString();
                if (TextUtils.isEmpty(search)) {
                    // 주소록 리스트 설정
                    list.addAll(listAll);
                } else {
                    for (int i = 0; i < listAll.size(); i++) {
                        ItemContacts item = listAll.get(i);
                        if (StringUtil.matchString(item.getName(), input)) {
                            list.add(item);
                        }
                    }
                }
                // 리스트뷰 화면 갱신
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        /**
         * 키패드 액션 리스너
         */
        ((EditText) findViewById(R.id.activity_contacts_et_search)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    findViewById(R.id.activity_contacts_btn_search).performClick();
                    return true;
                }
                return false;
            }
        });

        // 이름 검색
        findViewById(R.id.activity_contacts_btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = ((EditText) findViewById(R.id.activity_contacts_et_search)).getText().toString();
                if (StringUtil.isNull(name.trim())) {
                    // 주소록 리스트 초기화
                    if (list == null) {
                        list = new ArrayList<>();
                    } else {
                        list.clear();
                    }
                    // 주소록 리스트 설정
                    list.addAll(listAll);
                } else {
                    int size = list.size();
                    if (size > 0) {
                        ArrayList<ItemContacts> temp = new ArrayList<>();
                        for (int i = 0; i < size; i++) {
                            ItemContacts item = list.get(i);
                            if (item.getName() != null) {
                                String s = item.getName();
                                if (s.contains(name)) {
                                    temp.add(item);
                                }
                            }
                        }
                        // 주소록 리스트 초기화
                        if (list == null) {
                            list = new ArrayList<>();
                        } else {
                            list.clear();
                        }
                        // 주소록 리스트 설정
                        list.addAll(temp);

                        // 이름순 정렬
                        Collections.sort(list, new NameCompare());
                    }
                }
                // 키패드 내리기
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        hideKeypad(HotKeyContactsActivity.this, (EditText) findViewById(R.id.activity_contacts_et_search));
                    }
                });
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 주소록 가져오기
                list = ContactsUtil.getContactsList(HotKeyContactsActivity.this);
                // 주소록 리스트가 있으면
                if (list.size() > 0) {
                    // 이름순 정렬 (가나다순)
                    Collections.sort(list, new NameCompare());
                    // 전체 주소록 리스트에 저장
                    listAll.addAll(list);
                }
                LogUtil.d(TAG, "onCreate() -> list : " + list.size());
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 키보드 내리기
     *
     * @param editText
     */
    protected void hideKeypad(Context context, final EditText editText) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    /**
     * 뷰홀더
     *
     * @author Dongnam
     */
    private class ViewHolder {
        TextView tvName;
        TextView tvProfile;
        TextView tvHp;
        ImageView ivProfile;
    }

    /**
     * 주소록 어댑터
     *
     * @author Dongnam
     */
    public class ContactsAllAdapter extends BaseAdapter {
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
                convertView = View.inflate(HotKeyContactsActivity.this, R.layout.row_list_contacts, null);

                holder = new ViewHolder();
                // 프로필 이미지
                holder.ivProfile = convertView.findViewById(R.id.row_list_contacts_iv_profile);
                // 프로필 이름
                holder.tvProfile = convertView.findViewById(R.id.row_list_contacts_tv_profile);
                // 이름
                holder.tvName = convertView.findViewById(R.id.row_list_contacts_tv_name);
                // 휴대폰 번호
                holder.tvHp = convertView.findViewById(R.id.row_list_contacts_tv_hp);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ItemContacts item = list.get(position);
            String name = item.getName();   // 이름
            String hp = item.getHp();       // 휴대전화번호

            // 이름
            holder.tvName.setText(name);
            // 프로필 이름
            holder.tvProfile.setText(item.getName().substring(0, 1));
            // 휴대폰 번호
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.tvHp.setText(PhoneNumberUtils.formatNumber(hp, Locale.getDefault().getCountry()));
            }
            return convertView;
        }
    }

    /**
     * 이름순 정렬
     *
     * @author Dongnam
     */
    static class NameCompare implements Comparator<ItemContacts> {
        @Override
        public int compare(ItemContacts lhs, ItemContacts rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }

    /**
     * 백버튼
     */
    @Override
    public void onBackPressed() {
        finish();
    }
}
