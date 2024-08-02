package comfortable_andy.brew.menu.actions;

public interface MenuAction {

    public enum ActionType {
        LEFT,
        RIGHT,
        MIDDLE,
        OFF,
        NUMBER,
        DROP
    }

    public enum ActionModifier {
        SHIFT,
        DOUBLE
    }

}
