package com.teamnova.jaycameraapp1;


//필터 버튼 클래스. 필터 버튼 정보를 저장한다. 메인액티비티의 필터리사이클뷰에서 사용된다.
public class FilterBtn {
    //필터이름.
    public String name;

    //메인에서 사용되는 필터테그. 아이템클릭시 태그를 확인해 필터를 적용한다.
    public int TAG;

    public FilterBtn(String name, int TAG) {
        this.name = name;
        this.TAG = TAG;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTAG() {
        return TAG;
    }

    public void setTAG(int TAG) {
        this.TAG = TAG;
    }
}
