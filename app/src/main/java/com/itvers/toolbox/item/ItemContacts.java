package com.itvers.toolbox.item;

public class ItemContacts {
    public long _id;            // 인덱스
    public String name = "";    // 이름
    public String hp = "";      // 휴대전화번호

    public long getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getHp() {
        return hp;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }
}