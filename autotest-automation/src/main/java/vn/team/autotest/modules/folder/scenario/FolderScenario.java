package vn.team.autotest.modules.folder.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.folder.page.FolderPage;
public final class FolderScenario extends AbstractModuleScenario { protected String route(){return "/folders";} protected PwwModulePage page(){return new FolderPage(driver(),config());} }
