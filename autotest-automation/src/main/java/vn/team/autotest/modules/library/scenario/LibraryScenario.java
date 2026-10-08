package vn.team.autotest.modules.library.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.library.page.LibraryPage;
public final class LibraryScenario extends AbstractModuleScenario { protected String route(){return "/library";} protected PwwModulePage page(){return new LibraryPage(driver(),config());} }
