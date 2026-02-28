package mod.ckenja.cyninja.core.action;

public abstract class ActionBehaviour {
    public Action action;

    public ActionBehaviour(Action action) {
        this.action = action;
    }

    public void initialize() {
    }

    public void tick() {
    }
}
