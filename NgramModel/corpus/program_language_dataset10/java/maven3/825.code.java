package org.apache.maven.settings.building;
import java.util.List;
import org.apache.maven.settings.Settings;
public interface SettingsBuildingResult
{
    Settings getEffectiveSettings();
    List<SettingsProblem> getProblems();
}
