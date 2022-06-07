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
import android.widget.EditText;
import android.widget.TextView;

import com.itvers.toolbox.R;
import com.itvers.toolbox.common.Definition;
import com.itvers.toolbox.util.LogUtil;
import com.itvers.toolbox.util.StringUtil;
import com.itvers.toolbox.util.ToastUtil;

import static android.text.InputType.TYPE_CLASS_PHONE;
import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_URI;

public class DialogInput extends DialogFragment {
    private static final String TAG = DialogInput.class.getSimpleName();
    private InputListener inputListener = null;
    int type = -1;

    public interface InputListener {
        void OnConfirmListener(String input);
        void OnCancelListener();
    }

    public void setListener(InputListener inputListener) {
        this.inputListener = inputListener;
    }

    public static void show(FragmentManager fragmentManager,
                            Bundle bundle,
                            InputListener inputListener) {
        DialogInput dialogInput = new DialogInput();
        dialogInput.setListener(inputListener);
        dialogInput.setArguments(bundle);
        dialogInput.show(fragmentManager, "");
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreateDialog() -> Start !!!");
        final android.app.Dialog dialog = new Dialog(getActivity());
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.dialog_input);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Bundle bundle = getArguments();
        type = bundle.getInt(Definition.KEY_DIALOG_TYPE);
        LogUtil.d(TAG, "onCreateDialog() -> type : " + type);

        switch (type) {
            case Definition.TYPE_DIALOG_INPUT_WEB:
                ((TextView) dialog.findViewById(R.id.dialog_input_tv_description)).setText(getResources().getString(R.string.please_enter_a_web_address));
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setHint(getResources().getString(R.string.please_enter_a_web_address));
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_URI );

                String url = bundle.getString(Definition.KEY_HOTKEY_URL);
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setText(url);
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setSelection(((EditText) dialog.findViewById(R.id.dialog_input_et_input)).length());
                dialog.findViewById(R.id.dialog_input_et_input).post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.findViewById(R.id.dialog_input_et_input).setFocusableInTouchMode(true);
                        dialog.findViewById(R.id.dialog_input_et_input).requestFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(dialog.findViewById(R.id.dialog_input_et_input),0);
                    }
                });
                break;
            case Definition.TYPE_DIALOG_INPUT_PHONE_NUMBER:
                ((TextView) dialog.findViewById(R.id.dialog_input_tv_description)).setText(getResources().getString(R.string.please_enter_your_phone_number));
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setHint(getResources().getString(R.string.please_enter_your_phone_number));
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setInputType(TYPE_CLASS_PHONE);

                String phoneNumber = bundle.getString(Definition.KEY_HOTKEY_PHONE);
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setText(phoneNumber);
                ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).setSelection(((EditText) dialog.findViewById(R.id.dialog_input_et_input)).length());
                dialog.findViewById(R.id.dialog_input_et_input).post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.findViewById(R.id.dialog_input_et_input).setFocusableInTouchMode(true);
                        dialog.findViewById(R.id.dialog_input_et_input).requestFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(dialog.findViewById(R.id.dialog_input_et_input),0);
                    }
                });
                break;
            default:
                dismiss();
                break;
        }

        // 저장 버튼
        dialog.findViewById(R.id.dialog_input_btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String input = ((EditText) dialog.findViewById(R.id.dialog_input_et_input)).getText().toString();
                if (StringUtil.isNull(input.trim())) {
                    String message = "";
                    if (type == Definition.TYPE_DIALOG_INPUT_WEB) {
                       message = getResources().getString(R.string.web_address_field_is_blank);
                    } else if (type == Definition.TYPE_DIALOG_INPUT_PHONE_NUMBER) {
                       message = getResources().getString(R.string.phone_number_field_is_blank);
                    }
                    ToastUtil.getInstance().show(getActivity(), message, false);
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideKeypad(getActivity(), ((EditText) dialog.findViewById(R.id.dialog_input_et_input)));
                    }
                });

                if (inputListener != null) {
                    inputListener.OnConfirmListener(input);
                    dismiss();
                }
            }
        });

        // 취소 버튼
        dialog.findViewById(R.id.dialog_input_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputListener != null) {
                    inputListener.OnCancelListener();
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
