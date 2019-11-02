package com.feng.elasticsearch.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.util.Date;

//@Mapping(mappingPath = "elasticsearch_mapping.json")//设置mapping
//@Setting(settingPath = "elasticsearch_setting.json")//设置setting
@Document(indexName = "docs", type = "test-doc")
public class TestDoc implements Serializable {

    @Id
    private String id;

//    @Field(type = FieldType.Text, store = true, analyzer = "ik_smart", searchAnalyzer = "ik_max_word")
    @Field(type = FieldType.Text, store = true)
    private String name;

//    @Field(type = FieldType.Text, store = true, analyzer = "ik_smart", searchAnalyzer = "ik_max_word")
    @Field(type = FieldType.Text, store = true)
    private String content;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Keyword)
    private String roleId;

    /**
     * 需要在elasticsearch/plugins/下添加组件elasticsearch-analysis-ik（还可以添加elasticsearch-analysis-pinyin、elasticsearch-analysis-stconvert）组件
     */
    @Field(type = FieldType.Date)
    private Date date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public TestDoc() {
    }

    public TestDoc(String id, String name, String content, String type, String roleId, Date date) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.type = type;
        this.roleId = roleId;
        this.date = date;
    }

    @Override
    public String toString() {
        return "TestDoc{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", roleId='" + roleId + '\'' +
                ", date=" + date +
                '}';
    }
}