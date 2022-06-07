package com.itvers.toolbox.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import com.itvers.toolbox.common.Definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Dialog {
    public static final String TAG = Dialog.class.getSimpleName();
    private static volatile Dialog singletonInstance = null;    // 싱글턴 인스턴스
    private AlertDialog alert;                                  // 다이얼로그

    public interface DialogOnClickListener {                    // 다이얼로그 클릭 리스너 인터페이스
        void OnItemClickResult(HashMap <String, Object> hashMap);
    }

    /**
     * 싱글턴 인스턴스
     *
     * @return instance
     */
    public static Dialog getInstance() {
        if (null == singletonInstance) {
            synchronized (Dialog.class) {
                if (null == singletonInstance) {
                    singletonInstance = new Dialog();
                }
            }
        }
        return singletonInstance;
    }

    /**
     * 단일 버튼
     *
     * @param activity   엑티비티
     * @param title      타이틀 문자
     * @param message    메시지 문자
     * @param positive   '예' 버튼 문자
     * @param cancelable 최소 가능 여부
     * @param listener   클릭 리스너
     */
    public void showSingle(Activity activity,
                           String title,
                           String message,
                           String positive,
                           boolean cancelable,
                           DialogOnClickListener listener) {
        // 다이얼로그 종료
        dismiss();

        // 다이얼로그 클릭 리스너 등록
        final DialogOnClickListener dialogOnClickListener = listener;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(cancelable);
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap <String, Object> hashmap = new HashMap <>();
                        hashmap.put(Definition.KEY_DIALOG_SINGLE, Definition.DIALOG_BUTTON_POSITIVE);
                        dialogOnClickListener.OnItemClickResult(hashmap);
                    }
                });
        alert = builder.show();
    }

    /**
     * 듀얼 버튼
     *
     * @param activity   엑티비티
     * @param title      타이틀 문자
     * @param message    메시지 문자
     * @param positive   '예' 버튼 문자
     * @param negative   '아니오' 버튼 문자
     * @param cancelable 최소 가능 여부
     * @param listener   클릭 리스너
     */
    public void showDual(Activity activity,
                         String title,
                         String message,
                         String positive,
                         String negative,
                         boolean cancelable,
                         DialogOnClickListener listener) {

        // 다이얼로그 종료
        dismiss();

        // 다이얼로그 클릭 리스너 등록
        final DialogOnClickListener dialogOnClickListener = listener;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(cancelable);
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap <String, Object> hashmap = new HashMap <>();
                        hashmap.put(Definition.KEY_DIALOG_DUAL, Definition.DIALOG_BUTTON_POSITIVE);
                        dialogOnClickListener.OnItemClickResult(hashmap);
                    }
                });
        builder.setNegativeButton(negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap <String, Object> hashmap = new HashMap <>();
                        hashmap.put(Definition.KEY_DIALOG_DUAL, Definition.DIALOG_BUTTON_NEGATIVE);
                        dialogOnClickListener.OnItemClickResult(hashmap);
                    }
                });
        alert = builder.show();
    }

    /**
     * 트리플 버튼
     *
     * @param activity   엑티비티
     * @param title      타이틀 문자
     * @param message    메시지 문자
     * @param positive   '예' 버튼 문자
     * @param neutral    '기타' 버튼 문자
     * @param negative   '아니오' 버튼 문자
     * @param cancelable 최소 가능 여부
     * @param listener   클릭 리스너
     */
    public void showTriple(Activity activity,
                           String title,
                           String message,
                           String positive,
                           String neutral,
                           String negative,
                           boolean cancelable,
                           DialogOnClickListener listener) {

        // 다이얼로그 종료
        dismiss();

        // 다이얼로그 클릭 리스너 등록
        final DialogOnClickListener dialogOnClickListener = listener;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(cancelable);
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap <String, Object> hashmap = new HashMap <>();
                        hashmap.put(Definition.KEY_DIALOG_TRIPLE, Definition.DIALOG_BUTTON_POSITIVE);
                        dialogOnClickListener.OnItemClickResult(hashmap);
                    }
                });
        builder.setNeutralButton(neutral,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap <String, Object> hashmap = new HashMap <>();
                        hashmap.put(Definition.KEY_DIALOG_TRIPLE, Definition.DIALOG_BUTTON_NETURAL);
                        dialogOnClickListener.OnItemClickResult(hashmap);
                    }
                });
        builder.setNegativeButton(negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap <String, Object> hashmap = new HashMap <>();
                        hashmap.put(Definition.KEY_DIALOG_TRIPLE, Definition.DIALOG_BUTTON_NEGATIVE);
                        dialogOnClickListener.OnItemClickResult(hashmap);
                    }
                });
        alert = builder.show();
    }

    /**
     * 리스트
     *
     * @param activity   엑티비티
     * @param title      타이틀 문자
     * @param cancelable 최소 가능 여부
     * @param list       아이템 리스트
     * @param listener   클릭 리스너
     */
    public void showList(Activity activity,
                         String title,
                         boolean cancelable,
                         List <String> list,
                         DialogOnClickListener listener) {

        // 다이얼로그 종료
        dismiss();

        // 다이얼로그 클릭 리스너 등록
        final DialogOnClickListener dialogOnClickListener = listener;

        final CharSequence[] items = list.toArray(new String[list.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setCancelable(cancelable);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int pos) {
                String item = items[pos].toString();
                HashMap <String, Object> hashmap = new HashMap <>();
                hashmap.put(Definition.KEY_DIALOG_LIST, item);
                dialogOnClickListener.OnItemClickResult(hashmap);
            }
        });
        builder.show();
    }

    /**
     * 단일 입력
     *
     * @param activity   엑티비티
     * @param title      타이틀 문자
     * @param message    메시지 문자
     * @param positive   '예' 버튼 문자
     * @param negative   '아니오' 버튼 문자
     * @param inputType  입력 타입
     * @param cancelable 최소 가능 여부
     * @param listener   클릭 리스너
     */
    public void showSingleInput(Activity activity,
                                String title,
                                String message,
                                String positive,
                                String negative,
                                int inputType,
                                boolean cancelable,
                                DialogOnClickListener listener) {

        // 다이얼로그 종료
        dismiss();

        // 다이얼로그 클릭 리스너 등록
        final DialogOnClickListener dialogOnClickListener = listener;

        final EditText edittext = new EditText(activity);
        edittext.setInputType(inputType);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setView(edittext);
        builder.setCancelable(cancelable);
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap <String, Object> hashmap = new HashMap <>();
                        hashmap.put(Definition.KEY_DIALOG_SINGLE_INPUT, edittext.getText().toString());
                        dialogOnClickListener.OnItemClickResult(hashmap);
                    }
                });
        builder.setNegativeButton(negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

//    public void showMultipleInput(Activity activity,
//                                   String title,
//                                   String message,
//                                   String positive,
//                                   String negative,
//                                   boolean cancelable,
//                                   DialogOnClickListener listener) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
//        LayoutInflater inflater = activity.getLayoutInflater();
//        View view = inflater.inflate(R.layout.dialog_login, null);
//        builder.setView(view);
//        final Button submit = (Button) view.findViewById(R.id.buttonSubmit);
//        final EditText email = (EditText) view.findViewById(R.id.edittextEmailAddress);
//        final EditText password = (EditText) view.findViewById(R.id.edittextPassword);
//
//        final AlertDialog dialog = builder.create();
//        submit.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                String strEmail = email.getText().toString();
//                String strPassword = password.getText().toString();
//                dialog.dismiss();
//            }
//        });
//        dialog.show();
//    }

    /**
     * 단일 선택
     *
     * @param activity   엑티비티
     * @param title      타이틀 문자
     * @param positive   '예' 버튼 문자
     * @param negative   '아니오' 버튼 문자
     * @param cancelable 최소 가능 여부
     * @param list       아이템 리스트
     * @param listener   클릭 리스너
     */
    public void showSingleSelection(Activity activity,
                                    String title,
                                    String positive,
                                    String negative,
                                    boolean cancelable,
                                    final List <String> list,
                                    DialogOnClickListener listener) {

        // 다이얼로그 종료
        dismiss();

        // 다이얼로그 클릭 리스너 등록
        final DialogOnClickListener dialogOnClickListener = listener;

        final CharSequence[] items = list.toArray(new String[list.size()]);

        final List SelectedItems = new ArrayList();

        int defaultItem = 0;
        SelectedItems.add(defaultItem);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setCancelable(cancelable);
        builder.setSingleChoiceItems(items, defaultItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SelectedItems.clear();
                        SelectedItems.add(which);
                    }
                });
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!SelectedItems.isEmpty()) {
                            int index = ( int ) SelectedItems.get(0);
                            HashMap <String, Object> hashmap = new HashMap <>();
                            hashmap.put(Definition.KEY_DIALOG_SINGLE_INPUT, list.get(index).toString());
                            dialogOnClickListener.OnItemClickResult(hashmap);
                        }
                    }
                });
        builder.setNegativeButton(negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    /**
     * 다중 선택
     *
     * @param activity   엑티비티
     * @param title      타이틀 문자
     * @param positive   '예' 버튼 문자
     * @param negative   '아니오' 버튼 문자
     * @param cancelable 최소 가능 여부
     * @param list       아이템 리스트
     * @param listener   클릭 리스너
     */
    public void showMultipleSelection(Activity activity,
                                      String title,
                                      String positive,
                                      String negative,
                                      boolean cancelable,
                                      final List <String> list,
                                      DialogOnClickListener listener) {

        // 다이얼로그 종료
        dismiss();

        // 다이얼로그 클릭 리스너 등록
        final DialogOnClickListener dialogOnClickListener = listener;

        final CharSequence[] items = list.toArray(new String[list.size()]);

        final List SelectedItems = new ArrayList();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setCancelable(cancelable);
        builder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                        if (isChecked) {
                            SelectedItems.add(which);
                        } else if (SelectedItems.contains(which)) {
                            SelectedItems.remove(Integer.valueOf(which));
                        }
                    }
                });
        builder.setPositiveButton(positive,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String selections[] = new String[SelectedItems.size()];
                        for (int i = 0; i < SelectedItems.size(); i++) {
                            int index = ( int ) SelectedItems.get(i);
                            selections[i] = list.get(index);
                            HashMap <String, Object> hashmap = new HashMap <>();
                            hashmap.put(Definition.KEY_DIALOG_MULTIPLE_INPUT, selections);
                            dialogOnClickListener.OnItemClickResult(hashmap);
                        }
                    }
                });
        builder.setNegativeButton(negative,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    /**
     * 다이얼로그 종료
     */
    public void dismiss() {
        if (null != alert) alert.dismiss();
    }
}
