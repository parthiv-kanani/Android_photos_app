package com.example.android22.Base;


import java.io.Serializable;


public class Tag implements Serializable {

    private String tagType;


    private String tagValue;


    private boolean singular;


    public Tag(String tagType, String tagValue, boolean singular){
        this.tagType = tagType;
        this.tagValue = tagValue;
        this.singular = singular;
    }


    public String getTagType(){
        return tagType;
    }


    public String getTagValue(){
        return tagValue;
    }


    public boolean getSingularity(){
        return singular;
    }


    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) o;
        return this.tagType.equals(tag.getTagType()) && this.tagValue.equalsIgnoreCase(tag.getTagValue());
    }


    public String toString(){
        return tagType + " : " + tagValue;
    }
}