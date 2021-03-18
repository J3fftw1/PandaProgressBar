import com.intellij.ide.ui.LafManager;
import javax.swing.UIManager;


public class PandaApplicationComponent {
    public PandaApplicationComponent() {
        LafManager.getInstance().addLafManagerListener(__ -> updateProgressBarUi());
        updateProgressBarUi();
    }

    private void updateProgressBarUi() {
        UIManager.put("ProgressBarUI", PandaProgressBarUi.class.getName());
        UIManager.getDefaults().put(PandaProgressBarUi.class.getName(), PandaProgressBarUi.class);
    }
}
