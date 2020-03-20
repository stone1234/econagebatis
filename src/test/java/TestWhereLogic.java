import com.econage.core.db.mybatis.annotations.WhereLogic;
import com.econage.core.db.mybatis.annotations.WhereLogicField;

@WhereLogic
public class TestWhereLogic {
    @WhereLogicField(wherePart = " text1_ like concat('%',#{text1},'%') ")
    private String text1;

    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }
}
