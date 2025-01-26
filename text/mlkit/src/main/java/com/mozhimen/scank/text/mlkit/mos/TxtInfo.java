package com.mozhimen.scank.text.mlkit.mos;


import android.annotation.SuppressLint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TxtInfo {
    private String text = "";//
    private String sname = "";
    private String scard = "";
    private Integer iage=0;
    private String Id;
    private Integer x;
    private Integer x2;
    private Integer y;
    private Integer y2;
    private Integer h; //字体总高度
    private Integer w; //字体总宽度
    private Integer h1; //字体高度
    private Integer hh; //字体高度
    private Integer By; //字体高度
    private Integer w1; //字体宽度
    private Integer t;
    private Integer score;//分数
    private String sClass = "";
    public String sDesc = "";
    public float rotateLen = 0;

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getScard() {
        return scard;
    }

    public void setScard(String scard) {
        this.scard = scard;
    }

    public Integer getIage() {
        return iage;
    }

    public void setIage(Integer iage) {
        this.iage = iage;
    }

    public String getsDesc() {
        return sDesc;
    }

    public void setsDesc(String sDesc) {
        this.sDesc = sDesc;
    }

    public String getsClass() {
        return sClass;
    }

    public void setsClass(String sClass) {
        this.sClass = sClass;
    }


    public Integer getT() {
        return t;
    }

    public void setT(Integer t) {
        this.t = t;
    }

    public String getId() {
        return this.Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public void setx(Integer x) {
        this.x = x;
    }

    public Integer getx() {
        return this.x;
    }

    public void settext(String text) {
        this.text = text;
    }

    public String gettext() {
        return this.text;
    }

    public void sety(Integer y) {
        this.y = y;
    }

    public Integer gety() {
        return this.y;
    }

    public void seth(Integer h) {
        this.h = h;
    }

    public Integer geth() {
        return this.h;
    }

    public void setw(Integer w) {
        this.w = w;
    }

    public Integer getw() {
        return this.w;
    }

    public void setscore(Integer score) {
        this.score = score;
    }

    public Integer getscore() {
        return this.score;
    }

    public static LinkedHashMap<String, TxtInfo> sortmy(
            LinkedHashMap<String, TxtInfo> hashMap, Integer itype) {

        LinkedHashMap<String, TxtInfo> linkedHashMap = new LinkedHashMap<String, TxtInfo>();
        Set<Map.Entry<String, TxtInfo>> entrySet = hashMap.entrySet();
        ArrayList<Map.Entry<String, TxtInfo>> arrayList = new ArrayList<>(entrySet);
        Collections.sort(arrayList, new Comparator<Map.Entry<String, TxtInfo>>() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public int compare(Map.Entry<String, TxtInfo> obj1,
                               Map.Entry<String, TxtInfo> obj2) {
                // TODO Auto-generated method stub
                if (itype == 0)
                    return obj1.getValue().gety() - obj2.getValue().gety();
                if (itype == 1)
                    return obj1.getValue().getx() - obj2.getValue().getx();
                return 0;
            }

        });
        for (int i = 0; i < arrayList.size(); i++) {
            Map.Entry<String, TxtInfo> entry = arrayList.get(i);
            linkedHashMap.put(entry.getKey(), entry.getValue());
        }

        return linkedHashMap;
    }

    public static String maptojson(LinkedHashMap<String, TxtInfo> mHashMap) {
        JSONArray mNodeArray = new JSONArray();
        for (String key : mHashMap.keySet()) {
            JSONObject nodejson = new JSONObject();

            TxtInfo txtinfo = mHashMap.get(key);
            try {
                nodejson.put("text", key);
                nodejson.put("x", txtinfo.getx());
                nodejson.put("y", txtinfo.gety());
                nodejson.put("w", txtinfo.getw());
                nodejson.put("h", txtinfo.geth());
                nodejson.put("w1", txtinfo.getW1());
                nodejson.put("h1", txtinfo.getH1());
                mNodeArray.put(nodejson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mNodeArray.toString();
    }

    public Integer getX2() {
        return x2;
    }

    public void setX2(Integer x2) {
        this.x2 = x2;
    }

    public Integer getY2() {
        return y2;
    }

    public void setY2(Integer y2) {
        this.y2 = y2;
    }

    public static String mapdel(LinkedHashMap<String, TxtInfo> mHashMap) {

        return null;
    }


    public Integer getH1() {
        return h1;
    }

    public void setH1(Integer h1) {
        this.h1 = h1;
    }

    public Integer getW1() {
        return w1;
    }

    public void setW1(Integer w1) {
        this.w1 = w1;
    }

    public Integer getHh() {
        return hh;
    }

    public void setHh(Integer hh) {
        this.hh = hh;
    }

    public Integer getBy() {
        return By;
    }

    public void setBy(Integer by) {
        By = by;
    }

    public float getRotateLen() {
        return rotateLen;
    }

    public void setRotateLen(float rotateLen) {
        this.rotateLen = rotateLen;
    }
}
