package io.unthrottled.doki.icons.jetbrains.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsContexts;
import io.unthrottled.doki.icons.jetbrains.config.Config;
import io.unthrottled.doki.icons.jetbrains.config.DeferredTrueItem;
import io.unthrottled.doki.icons.jetbrains.config.IconConfigListener;
import io.unthrottled.doki.icons.jetbrains.config.IconSettings;
import io.unthrottled.doki.icons.jetbrains.config.IconSettingsModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

public class IconSettingsUI implements SearchableConfigurable, Configurable.NoScroll, DumbAware {

  private final IconSettingsModel iconSettingsModel = IconSettings.createSettingsModule();
  private IconSettingsModel initialIconSettingsModel = IconSettings.createSettingsModule();


  private JPanel rootPane;
  private JComboBox currentThemeWomboComboBox;
  private JCheckBox syncWithDokiThemeCheckBox;
  private JCheckBox UIIconsCheckBox;
  private JCheckBox foldersCheckBox;
  private JCheckBox filesCheckBox;
  private JCheckBox PSICheckBox;

  @Override
  public @NotNull @NonNls String getId() {
    return IconSettings.SETTINGS_ID;
  }

  @Override
  public @NlsContexts.ConfigurableName String getDisplayName() {
    return IconSettings.ICON_SETTINGS_DISPLAY_NAME;
  }

  @Override
  public @Nullable JComponent createComponent() {
    initializeAutoCreatedComponents();
    return rootPane;
  }

  private void initializeAutoCreatedComponents() {
    UIIconsCheckBox.setSelected(initialIconSettingsModel.isUIIcons());
    filesCheckBox.setSelected(initialIconSettingsModel.isFileIcons());
    PSICheckBox.setSelected(initialIconSettingsModel.isPSIIcons());
    foldersCheckBox.setSelected(initialIconSettingsModel.isFolderIcons());
    syncWithDokiThemeCheckBox.setSelected(initialIconSettingsModel.getSyncWithDokiTheme());
  }

  @Override
  public boolean isModified() {
    return !initialIconSettingsModel.equals(iconSettingsModel);
  }

  @Override
  public void apply() throws ConfigurationException {
    Config config = Config.getInstance();
    config.setUIIcons(UIIconsCheckBox.isSelected());
    config.setFileIcons(filesCheckBox.isSelected());
    config.setPSIIcons(PSICheckBox.isSelected());
    config.setFolderIcons(foldersCheckBox.isSelected());
    config.setCurrentThemeId(iconSettingsModel.getCurrentThemeId());

    // When the Doki Theme is installed this defaults to Yes
    // it will be indeterminate until the Doki Theme is installed.
    if (initialIconSettingsModel.getSyncWithDokiTheme() != iconSettingsModel.getSyncWithDokiTheme() &&
      config.getSyncWithDokiTheme() != DeferredTrueItem.NOT_YET_NO) {
      config.setSyncWithDokiTheme(
        iconSettingsModel.getSyncWithDokiTheme() ?
          DeferredTrueItem.YES : DeferredTrueItem.NO
      );
    }

    ApplicationManager.getApplication()
      .getMessageBus()
      .syncPublisher(IconConfigListener.getICON_CONFIG_TOPIC())
      .iconConfigUpdated(config);
    initialIconSettingsModel = iconSettingsModel;
  }

  private void createUIComponents() {
    currentThemeWomboComboBox = IconSettings.INSTANCE.createThemeComboBoxModel(
      () -> this.iconSettingsModel == null ?
        IconSettings.createSettingsModule() :
        iconSettingsModel
    );
  }
}