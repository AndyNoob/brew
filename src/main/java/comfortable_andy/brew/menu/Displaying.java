package comfortable_andy.brew.menu;

import lombok.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class Displaying {

    protected final String id;
    @Getter(AccessLevel.NONE)
    protected final String displayName;
    protected final String description;

    @NotNull
    public Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(displayName);
    }

    @NotNull
    public Component getDescription() {
        return MiniMessage.miniMessage().deserialize(this.description);
    }

}
