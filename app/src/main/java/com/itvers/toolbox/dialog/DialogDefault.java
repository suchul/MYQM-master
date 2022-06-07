package com.itvers.toolbox.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.util.LogUtil;

public class DialogDefault extends DialogFragment {
    private static final String TAG = DialogDefault.class.getSimpleName();
    private DefaultListener defaultListener = null;

    public interface DefaultListener {
        void OnConfirmListener();
        void OnCancelListener();
    }

    public void setListener(DefaultListener defaultListener) {
        this.defaultListener = defaultListener;
    }

    public static void show(FragmentManager fragmentManager,
                            Bundle bundle,
                            DefaultListener defaultListener) {
        DialogDefault dialogDefault = new DialogDefault();
        dialogDefault.setListener(defaultListener);
        dialogDefault.setArguments(bundle);
        dialogDefault.show(fragmentManager, "");
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreateDialog() -> Start !!!");
        final android.app.Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.dialog_default);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Bundle bundle = getArguments();
        int type = bundle.getInt(Definition.KEY_DIALOG_TYPE);
        LogUtil.d(TAG, "onCreateDialog() -> type : " + type);

        switch (type) {
            case Definition.TYPE_DIALOG_SELECT_DUAL:
                ((TextView) dialog.findViewById(R.id.dialog_default_tv_description)).setText(getResources().getString(R.string.choose_how_to_enter_your_phone_number));
                ((Button) dialog.findViewById(R.id.dialog_default_btn_confirm)).setText(getResources().getString(R.string.manual_input));
                ((Button) dialog.findViewById(R.id.dialog_default_btn_cancel)).setText(getResources().getString(R.string.phonebook));
                break;
            case Definition.TYPE_DIALOG_RESET_HOTKEY:
                ((TextView) dialog.findViewById(R.id.dialog_default_tv_description)).setText(getResources().getString(R.string.delete_all_hotkeys));
                ((Button) dialog.findViewById(R.id.dialog_default_btn_confirm)).setText(getResources().getString(R.string.reset));
                ((Button) dialog.findViewById(R.id.dialog_default_btn_cancel)).setText(getResources().getString(R.string.close));
                break;
            case Definition.TYPE_DIALOG_BLE_ENABLE:
                ((TextView) dialog.findViewById(R.id.dialog_default_tv_description)).setText(getResources().getString(R.string.allow_turn_on_Bluetooth));
                ((Button) dialog.findViewById(R.id.dialog_default_btn_confirm)).setText(getResources().getString(R.string.confirm));
                ((Button) dialog.findViewById(R.id.dialog_default_btn_cancel)).setText(getResources().getString(R.string.close));
                break;
            default:
                dismiss();
                break;
        }

        // 저장 버튼
        dialog.findViewById(R.id.dialog_default_btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultListener != null) {
                    defaultListener.OnConfirmListener();
                    dismiss();
                }
            }
        });

        // 취소 버튼
        dialog.findViewById(R.id.dialog_default_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (defaultListener != null) {
                    defaultListener.OnCancelListener();
                    dismiss();
                }
            }
        });
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * 키보드 내리기
     *
     * @param editText
     */
    protected void hideKeypad(Context context, final EditText editText) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
}
