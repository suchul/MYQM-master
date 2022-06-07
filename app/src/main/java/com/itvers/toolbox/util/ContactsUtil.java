package com.itvers.toolbox.util;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.itvers.toolbox.item.ItemContacts;

import java.util.ArrayList;

public class ContactsUtil {
    private static final String TAG = ContactsUtil.class.getSimpleName();   // 디버그 태그


    /**
     * 주소록 리스트 가져오기 (이름)
     *
     * @param context
     * @return
     */
    public static ArrayList<ItemContacts> getContactsList(Context context) {
        ArrayList<ItemContacts> list = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
            int idxId = cursor.getColumnIndex(ContactsContract.Contacts._ID);

            while (cursor.moveToNext()) {
                ItemContacts item = new ItemContacts();
                int isPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (isPhone != 1) continue;
                long _id = cursor.getLong(idxId);
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (!TextUtils.isEmpty(name)) {
                    name = name.replaceAll("#", "");
                    if (!TextUtils.isEmpty(name)) {
                        item.setId(_id);
                        item.setName(name);
                        item.setHp(getPhoneNumber(context, _id));
                        list.add(item);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return list;
    }

    /**
     * 휴대전화번호 가져오기 (_id)
     *
     * @param context
     * @param _id
     * @return
     */
    public static String getPhoneNumber(Context context, Long _id) {
        String phoneNumber = "";
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{String.valueOf(_id)},
                null);
        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (!TextUtils.isEmpty(number)) {
                phoneNumber += number.replaceAll("-", "");
                phoneNumber += ",";
                LogUtil.d(TAG, "getPhoneNumber() -> phoneNumber : " + phoneNumber);
            }
        }
        cursor.close();

        if (phoneNumber.length() > 0) phoneNumber = phoneNumber.substring(0, phoneNumber.length()-1);

        return phoneNumber;
    }
}
