package org.apache.maven.settings.validation;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblemCollector;
public interface SettingsValidator
{
    void validate( Settings settings, SettingsProblemCollector problems );
}
