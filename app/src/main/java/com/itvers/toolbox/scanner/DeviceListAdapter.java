package com.itvers.toolbox.scanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.itvers.toolbox.R;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class DeviceListAdapter extends BaseAdapter {
    private static final int TYPE_TITLE = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_EMPTY = 2;

    private final ArrayList <ExtendedBluetoothDevice> listBondedValues = new ArrayList <>();
    private final ArrayList <ExtendedBluetoothDevice> listValues = new ArrayList <>();
    private final Context context;

    public DeviceListAdapter(Context context) {
        this.context = context;
    }

    /**
     * 연결된 장비 검색
     *
     * @param devices
     */
    public void addBondedDevices(final ArrayList <BluetoothDevice> devices) {
        final List <ExtendedBluetoothDevice> bondedDevices = listBondedValues;
        for (BluetoothDevice device : devices) {
            bondedDevices.add(new ExtendedBluetoothDevice(device));
        }
        notifyDataSetChanged();
    }

    /**
     * 연결된 장비 리스트 업데이트
     *
     * @param results
     */
    public void update(final List <ScanResult> results) {
        for (final ScanResult result : results) {
            final ExtendedBluetoothDevice device = extendedBluetoothDevice(result);
            if (device == null) {
                listValues.add(new ExtendedBluetoothDevice(result));
            } else {
                device.deviceName = result.getScanRecord() != null ? result.getScanRecord().getDeviceName() : null;
                device.rssi = result.getRssi();
            }
        }
        notifyDataSetChanged();
    }

    private ExtendedBluetoothDevice extendedBluetoothDevice(final ScanResult result) {
        for (final ExtendedBluetoothDevice device : listBondedValues)
            if (device.matches(result)) return device;
        for (final ExtendedBluetoothDevice device : listValues)
            if (device.matches(result)) return device;
        return null;
    }

    /**
     * 장비 리스트 초기화
     */
    public void clearDevices() {
        listValues.clear();
        notifyDataSetChanged();
    }

    /**
     * 연결된 장비 개수
     *
     * @return
     */
    @Override
    public int getCount() {
        final int bondedCount = listBondedValues.size() + 1;
        final int availableCount = listValues.isEmpty() ? 2 : listValues.size() + 1;
        if (bondedCount == 1) return availableCount;
        return bondedCount + availableCount;
    }

    /**
     * 리스트 아이템
     *
     * @param position
     * @return
     */
    @Override
    public Object getItem(int position) {
        final int bondedCount = listBondedValues.size() + 1;
        if (listBondedValues.isEmpty()) {
            if (position == 0) {
                return R.string.scanner_subtitle_not_bonded;
            } else {
                return listValues.get(position - 1);
            }
        } else {
            if (position == 0) return R.string.scanner_subtitle_bonded;
            if (position < bondedCount) return listBondedValues.get(position - 1);
            if (position == bondedCount) return R.string.scanner_subtitle_not_bonded;
            return listValues.get(position - bondedCount - 1);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == TYPE_ITEM;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_TITLE;

        if (!listBondedValues.isEmpty() && position == listBondedValues.size() + 1)
            return TYPE_TITLE;

        if (position == getCount() - 1 && listValues.isEmpty())
            return TYPE_EMPTY;

        return TYPE_ITEM;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View oldView, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final int type = getItemViewType(position);

        View view = oldView;
        switch (type) {
            case TYPE_EMPTY:
                if (null == view) {
                    view = inflater.inflate(R.layout.layout_list_device_empty, parent, false);
                }
                break;
            case TYPE_TITLE:
                if (null == view) {
                    view = inflater.inflate(R.layout.layout_list_device_title, parent, false);
                }
                final TextView title = (TextView) view;
                title.setText((Integer) getItem(position));
                break;
            default:
                if (null == view) {
                    view = inflater.inflate(R.layout.layout_list_device_row, parent, false);
                    final ViewHolder holder = new ViewHolder();
                    holder.deviceName = view.findViewById(R.id.layout_list_device_row_name);
                    holder.deviceAddress = view.findViewById(R.id.layout_list_device_row_address);
                    holder.rssi = view.findViewById(R.id.layout_list_device_row_rssi);
                    view.setTag(holder);
                }

                final ExtendedBluetoothDevice device = ( ExtendedBluetoothDevice ) getItem(position);
                final ViewHolder holder = ( ViewHolder ) view.getTag();
                final String name = device.deviceName;
                holder.deviceName.setText(name != null ? name : context.getString(R.string.not_available));
                holder.deviceAddress.setText(device.bluetoothDevice.getAddress());
                if (!device.isBonded || device.rssi != ExtendedBluetoothDevice.NO_RSSI) {
                    final int rssiPercent = ( int ) (100.0f * (127.0f + device.rssi) / (127.0f + 20.0f));
                    holder.rssi.setImageLevel(rssiPercent);
                    holder.rssi.setVisibility(View.VISIBLE);
                } else {
                    holder.rssi.setVisibility(View.GONE);
                }
                break;
        }
        return view;
    }

    /**
     * 뷰 홀더
     */
    private class ViewHolder {
        private TextView deviceName;
        private TextView deviceAddress;
        private ImageView rssi;
    }
}
