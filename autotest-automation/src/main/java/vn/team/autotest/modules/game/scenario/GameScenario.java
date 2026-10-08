package vn.team.autotest.modules.game.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.game.page.GamePage;
public final class GameScenario extends AbstractModuleScenario { protected String route(){return "/games";} protected PwwModulePage page(){return new GamePage(driver(),config());} }
