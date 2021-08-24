package AST.node;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by dinht_000 on 3/29/2017.
 */

public class VisibleElementNode extends JavaNode {

    public static final String PUBLIC_MODIFIER = "public";
    public static final String DEFAULT_MODIFIER = "default";
    public static final String PROTECTED_MODIFIER = "protected";
    public static final String PRIVATE_MODIFIER = "private";

    @JsonProperty("visibility")
    private String visibility = DEFAULT_MODIFIER;

    @JsonProperty("isStatic")
    private boolean isStatic = false;

    @JsonProperty("isStatic")
    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public static String getPublicModifier() {
        return PUBLIC_MODIFIER;
    }

    public static String getDefaultModifier() {
        return DEFAULT_MODIFIER;
    }

    public static String getProtectedModifier() {
        return PROTECTED_MODIFIER;
    }

    public static String getPrivateModifier() {
        return PRIVATE_MODIFIER;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
}
