package com.amsavarthan.lvstore;

import android.support.annotation.NonNull;

public class DocId {

    private String docId;

    public <T extends DocId> T withId(@NonNull final String id){
        this.docId=id;
        return (T) this;
    }

}
