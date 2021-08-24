package AST.node;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by cuong on 3/28/2017.
 */
public class AbstractableElementNode extends VisibleElementNode {

    protected boolean isAbstract = false;

    @JsonProperty("isAbstract")
    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }
}
