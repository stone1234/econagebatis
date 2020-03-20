import com.econage.core.db.mybatis.wherelogic.WhereLogicContext;
import com.econage.core.db.mybatis.wherelogic.WhereLogicParser;

public class WhereLogicParser2 implements WhereLogicParser {
    @Override
    public String parseWhereLogic(WhereLogicContext whereLogicContext) {
        TestWhereLogic testWhereLogic = (TestWhereLogic)whereLogicContext.getWhereLogicObj();

        return null;
    }
}
