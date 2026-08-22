import java.awt.Color;

public class AppTheme {
    public final String name;
    public final Color header;
    public final Color primary;
    public final Color accent;
    public final Color ink;
    public final Color muted;
    public final Color background;
    public final Color surface;
    public final Color border;
    public final Color outputBackground;
    public final Color outputForeground;
    public final Color secondaryButton;
    public final Color headerSubtext;
    public final Color tableForeground;
    public final Color tableBackground;
    public final boolean dark;

    private AppTheme(String name, boolean dark, Color header, Color primary, Color accent, Color ink, Color muted,
            Color background, Color surface, Color border, Color outputBackground, Color outputForeground,
            Color secondaryButton, Color headerSubtext, Color tableForeground, Color tableBackground) {
        this.name = name;
        this.dark = dark;
        this.header = header;
        this.primary = primary;
        this.accent = accent;
        this.ink = ink;
        this.muted = muted;
        this.background = background;
        this.surface = surface;
        this.border = border;
        this.outputBackground = outputBackground;
        this.outputForeground = outputForeground;
        this.secondaryButton = secondaryButton;
        this.headerSubtext = headerSubtext;
        this.tableForeground = tableForeground;
        this.tableBackground = tableBackground;
    }

    public static AppTheme light() {
        return new AppTheme("Light", false,
            new Color(18, 31, 48),
            new Color(28, 143, 137),
            new Color(239, 170, 71),
            new Color(37, 48, 61),
            new Color(105, 119, 132),
            new Color(247, 249, 247),
            Color.WHITE,
            new Color(220, 227, 225),
            new Color(18, 31, 48),
            new Color(230, 240, 238),
            new Color(232, 238, 235),
            new Color(176, 197, 208),
            new Color(37, 48, 61),
            Color.WHITE);
    }

    public static AppTheme dark() {
        return new AppTheme("Dark", true,
            new Color(10, 18, 28),
            new Color(36, 168, 160),
            new Color(245, 183, 88),
            new Color(230, 236, 242),
            new Color(156, 170, 184),
            new Color(22, 30, 40),
            new Color(32, 42, 54),
            new Color(52, 66, 82),
            new Color(12, 20, 30),
            new Color(214, 226, 235),
            new Color(44, 56, 70),
            new Color(156, 176, 194),
            new Color(230, 236, 242),
            new Color(32, 42, 54));
    }

    public AppTheme toggle() {
        return dark ? light() : dark();
    }
}
