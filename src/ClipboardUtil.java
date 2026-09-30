import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

class ClipboardUtil {
    public static boolean copyWithAutoClear(String text, int seconds) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection selection = new StringSelection(text);
            clipboard.setContents(selection, selection);

            new Thread(() -> {
                try {
                    Thread.sleep(seconds * 1000L);
                    StringSelection clear = new StringSelection("");
                    clipboard.setContents(clear, clear);
                } catch (Exception ignored) {
                }
            }).start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
