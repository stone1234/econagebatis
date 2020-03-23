import com.flowyun.cornerstone.db.mybatis.wherelogic.WhereLogicContext;
import com.flowyun.cornerstone.db.mybatis.wherelogic.WhereLogicParser;

public class WhereLogicParser2 implements WhereLogicParser {
    @Override
    public String parseWhereLogic(WhereLogicContext whereLogicContext) {
        TestWhereLogic testWhereLogic = (TestWhereLogic)whereLogicContext.getWhereLogicObj();

        return null;
    }
}
