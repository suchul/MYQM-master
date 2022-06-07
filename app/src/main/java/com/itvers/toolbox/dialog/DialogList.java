package com.itvers.toolbox.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.util.LogUtil;

public class DialogList extends DialogFragment {

    private static final String TAG = DialogList.class.getSimpleName();

    private ListListener listListener;
    private String[] list;

    private ListView listView;
    private ListlistAdapter listAdapter;

    public interface ListListener {
        void OnItemClickListener(int position, String item);
    }

    public void setListener(ListListener listListener) {
        this.listListener = listListener;
    }

    public static void show(FragmentManager fragmentManager,
                            Bundle bundle,
                            ListListener listListener) {
        DialogList dialogHotKey = new DialogList();
        dialogHotKey.setListener(listListener);
        dialogHotKey.setArguments(bundle);
        dialogHotKey.show(fragmentManager, "");
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreateDialog() -> Start !!!");

        final android.app.Dialog dialog = new Dialog(getActivity());

        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_list);

        dialog.show();

        Bundle bundle = getArguments();
        int type = bundle.getInt(Definition.KEY_DIALOG_TYPE);
        LogUtil.d(TAG, "onCreateDialog() -> type : " + type);

        switch (type) {
            case Definition.TYPE_DIALOG_HOTKEY:
                list = getResources().getStringArray(R.array.select_key);
                break;
            default:
                dismiss();
                break;
        }

        listView = dialog.findViewById(R.id.dialog_list_lv_content);
        listAdapter = new ListlistAdapter();
        listView.setAdapter(listAdapter);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                if (null != listListener) listListener.OnItemClickListener(position, list[position]);
                dismiss();
            }
        });
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * 그룹 리스트 어댑터
     */
    public class ListlistAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.length;
        }

        @Override
        public Object getItem(int position) {
            return list[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.row_list_dialog, null);
                holder = new ViewHolder();
                holder.tvItem = convertView.findViewById(R.id.row_dialog_list_tv_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvItem.setText(list[position]);
            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        private class ViewHolder {
            TextView tvItem;
        }
    }
}
