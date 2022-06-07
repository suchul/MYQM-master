package com.itvers.toolbox.common;

import java.util.Arrays;

public class VendorType {
    private final static String TAG = VendorType.class.getSimpleName(); // 디버그 태그

    public static int getVendorType(int vendorCode) {
        Arrays.sort(Definition.VENDOR_TYPE_1);
        int index = Arrays.binarySearch(Definition.VENDOR_TYPE_1, vendorCode);  // Short Key
        if (index >= 0) {
            return 1;
        }

        index = Arrays.binarySearch(Definition.VENDOR_TYPE_2, vendorCode);      // Double Key
        if (index >= 0) {
            return 2;
        }

        index = Arrays.binarySearch(Definition.VENDOR_TYPE_3, vendorCode);      // Long Key
        if (index >= 0) {
            return 3;
        }
        return 0;
    }
}
