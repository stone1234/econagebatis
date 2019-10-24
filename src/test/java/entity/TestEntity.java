package entity;

import com.econage.core.db.mybatis.annotations.TableDef;
import com.econage.core.db.mybatis.annotations.TableField;
import com.econage.core.db.mybatis.entity.BasicEntity;

@TableDef("test")
public class TestEntity implements BasicEntity {
    private String id;
    @TableField(isFk = true)
    private String fk;
    private String text1;
    private String text2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getFk() {
        return fk;
    }

    public void setFk(String fk) {
        this.fk = fk;
    }
}