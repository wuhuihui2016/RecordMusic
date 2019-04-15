package com.whh.recordmusic.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wuhuihui on 2019/3/26.
 */

public class Utils {


    public static String dirPath = Environment.getExternalStorageDirectory() + "/recordMusic/";

    //隐藏键盘
    public static void hideInput(Activity activity) {
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
    }


    /**
     * 面试测试题
     * {1,4,2,1,3,2,1,4}按出现次数从大到小排序
     * @param datas
     */
    public static void comparable(List<Integer> datas) {
        //{1,4,2,1,3,2,1,4}
        List<Integer> result = new ArrayList<>();
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        for (int i = 0; i < datas.size(); i++) {
            if (!result.contains(datas.get(i))) {
                result.add(datas.get(i));
            }
        }
        Log.i("comparable result", "" + result.size());


        int num = 0;
        for (int i = 0; i < result.size(); i++) {
            for (int j = 0; j < datas.size(); j++) {
                if (result.get(i) == datas.get(j)) {
                    num++;
                    Log.i("comparable i,j", result.get(i) + "==" + datas.get(j) + "->" + num);
                    hashMap.put(result.get(i), num);
                }
                if (j == (datas.size() - 1)) {
                    num = 0;
                }
            }
        }

        List<Integer> nums = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            nums.add(hashMap.get(result.get(i)));
        }

        Collections.sort(nums);
        Collections.reverse(nums);
        Log.i("comparable hashMap", hashMap.size() + "");
        List<Integer> list = new ArrayList<>();
        for (int j = 0; j < result.size(); j++) {
            for (int i = 0; i < nums.size(); i++) {
                if (!list.contains(result.get(j))) {
                    if (hashMap.get(result.get(j)) == nums.get(i)) {
                        list.add(result.get(j));
                        Log.i("comparable", result.get(j) + " 出现了 " + nums.get(i) + " 次");
                    }
                }
            }

        }


    }

}
