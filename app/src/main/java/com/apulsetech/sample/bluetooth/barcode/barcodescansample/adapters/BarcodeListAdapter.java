package com.apulsetech.sample.bluetooth.barcode.barcodescansample.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.apulsetech.lib.barcode.type.BarcodeType;
import com.apulsetech.lib.util.LogUtil;
import com.apulsetech.sample.bluetooth.barcode.barcodescansample.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BarcodeListAdapter extends BaseAdapter {

    private static final String TAG = "BarcodeListAdapter";
    private static final boolean D = true;

    public static final int SORT_TYPE_NONE = 0;
    public static final int SORT_TYPE_BARCODE = 1;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<BarcodeListItem> mList;
    private final HashMap<String, BarcodeListItem> mBarcodeHashMap = new HashMap<>();
    private final HashMap<BarcodeType, BarcodeListItem> mBarcodeTypeHashMap = new HashMap<>();

    private boolean mFilterEnabled = false;
    private boolean mHighlighEnabled = false;
    private int mSortType = SORT_TYPE_NONE;
    private boolean mReverseSortEnabled = false;
    private boolean mHighlightEnabled = false;

    private int totalCount;

    public BarcodeListAdapter(Context context) {
        super();

        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mList = new ArrayList<>();

        totalCount = 0;
    }

    public void clear() {
        mList.clear();
        totalCount = 0;
        mBarcodeTypeHashMap.clear();
        mBarcodeHashMap.clear();
        notifyDataSetChanged();
    }

    private static class BarcodeListItem {
        private final BarcodeType mType;
        private final String mBarcode;
        private int mCount;

        public BarcodeListItem(BarcodeType type, String barcode) {
            super();
            mType = type;
            mBarcode = barcode;
            mCount = 1;
        }

        public BarcodeType getType() {return mType;}
        public String getBarcode() {return mBarcode;}
        public int getCount() {return mCount;}

        public boolean compare(BarcodeType type, String barcode) {
            return ((mType == type) && mBarcode.equals(barcode));
        }

        public void increaseCount() {mCount++;}
    }

    public int addItem(BarcodeType type, String barcode, boolean isFilterEnabled) {
        mFilterEnabled = isFilterEnabled;

        BarcodeListItem item;
        int position;

        if(isFilterEnabled) {
            if ((item = findItem(type, barcode)) != null) {
                item.increaseCount();
                if (mSortType == SORT_TYPE_NONE) {
                    mList.remove(item);
                    mList.add(item);
                } else {
                    sort();
                }
    } else {
        item = new BarcodeListItem(type, barcode);
        mBarcodeHashMap.put(barcode, item);
        mBarcodeTypeHashMap.put(type, item);
        mList.add(item);
        sort();
    }
} else {
        item = new BarcodeListItem(type, barcode);
        mList.add(item);
        sort();
        }
        position = mList.size() - 1;

        notifyDataSetChanged();
        totalCount++;
        return position;
        }

private void sort() {
        Comparator<BarcodeListItem> comparator;

        if(mSortType == SORT_TYPE_BARCODE) {
            comparator = mBarcodeComparator;
        }else {
            return;
        }

        Collections.sort(mList, comparator);
        if(mReverseSortEnabled) {
            Collections.reverse(mList);
        }
    }


    private final Comparator<BarcodeListItem> mBarcodeComparator = new Comparator<BarcodeListItem>() {
        @Override
        public int compare(BarcodeListItem barcodeItem, BarcodeListItem t1) {
            String value = barcodeItem.mBarcode;
            String ValueT1 = t1.mBarcode;
            int valueLength = value.length();
            int valueT1Length = ValueT1.length();
            if(valueLength > valueT1Length) {
                return 1;
            } else if ( valueLength == valueT1Length) {
                return value.compareTo(ValueT1);
            }
            return -1;
        }
    };

    private BarcodeListItem findItem(BarcodeType type, String barcode) {
        BarcodeListItem itemByType = mBarcodeTypeHashMap.get(type);
        BarcodeListItem itemByBarcode = mBarcodeHashMap.get(barcode);
        if((itemByType != null) && (itemByBarcode != null)) {
            return itemByBarcode;
        }
        return null;
    }

    public void enableReverseSort(boolean enabled) {
        if (enabled == mReverseSortEnabled) {
            return;
        }

        LogUtil.log(LogUtil.LV_D, D, TAG, "enableReverseSort() enableReverseSort=" + enabled);
        mReverseSortEnabled = enabled;
        sort();
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<String> getBarcodeValueList() {
        if(mList.isEmpty()) {
            return null;
        }

        List<String> barcodeValueList = new ArrayList<>();
        for(BarcodeListItem barcodeItem : mList) {
            barcodeValueList.add(barcodeItem.mBarcode);
        }
        return barcodeValueList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BarcodeViewHolder holder;

        if(null == convertView) {
            convertView = mInflater.inflate(R.layout.barcode_tag_list_item, parent, false);
            holder = new BarcodeViewHolder(convertView);
        } else {
            holder = (BarcodeViewHolder) convertView.getTag();   // 초기화된 converView를 (SetTag된) 불러와 holder에 넣는다.
        }

        holder.setItem(mList.get(position));    // 그 홀더에 리스트에 있는 바코드 정보를 세팅한다.

        if (mHighlightEnabled) {
            if ((position == (mList.size() - 1)) && (mSortType == SORT_TYPE_NONE)) {
                convertView.setBackgroundColor(
                        mContext.getResources().getColor(R.color.color_list_item_focused));
            } else {
                convertView.setBackgroundColor(
                        mContext.getResources().getColor(R.color.color_list_item_normal));
            }
        }

        return convertView;
    }

    public void enableHightlight(boolean enabled) {
        if (mHighlightEnabled && !enabled) {
            notifyDataSetChanged();
        }

        mHighlightEnabled = enabled;
    }


    private class BarcodeViewHolder {
        private final TextView mType;
        private final TextView mBarcode;
        private final TextView mCount;

        public BarcodeViewHolder(View parent) {
            mType = (TextView)parent.findViewById(R.id.sub_value);
            mBarcode = (TextView)parent.findViewById(R.id.main_value);
            mCount = (TextView)parent.findViewById(R.id.duplicated_count);
            parent.setTag(this);
        }

        public void setItem(BarcodeListItem item) {
            mType.setText(item.getType().toString());
            mBarcode.setText(item.getBarcode());
            if(mFilterEnabled) {
                mCount.setText(String.format(Locale.US, "%d", item.getCount()));
            }
        }
    }
}
