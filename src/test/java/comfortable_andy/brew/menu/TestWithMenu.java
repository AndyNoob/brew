package comfortable_andy.brew.menu;

import org.junit.jupiter.api.BeforeEach;

public abstract class TestWithMenu {

    protected final FakeChestInv inv = new FakeChestInv();
    protected Menu menu;

    @BeforeEach
    protected void before() {
        menu = new Menu("bruh", "bruh", "bruh");
    }

}
